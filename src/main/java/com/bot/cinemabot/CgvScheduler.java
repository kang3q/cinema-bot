package com.bot.cinemabot;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bot.cinemabot.model.cgv.CgvItem;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CgvScheduler {

    @Autowired
    private Telegram telegram;

    @Value("${bot.cinema.cgv.api}")
    private String cgv;

    final private AtomicInteger callCount = new AtomicInteger(0);
    private List<CgvItem> cache1p1Tickets;

    @PostConstruct
    public void init() throws IOException {
        cache1p1Tickets = Collections.synchronizedList(get1p1Tickets());
        telegram.sendMessageToBot("CGV\n모든 1+1관람권: %s", cache1p1Tickets.size());
    }

    @Scheduled(initialDelay = 30_000, fixedDelayString = "${bot.schedule.fixedDelay}")
    public void aJob() throws IOException {
        List<CgvItem> onePlusOneTickets = get1p1Tickets();
        boolean isChangedTicket = isChangedTickets(onePlusOneTickets);
        int c = 0;

        if (isChangedTicket) {
            CgvItem newTicket = getNew1p1Ticket(onePlusOneTickets);
            String buyLink = cgv + newTicket.getLink().substring(1);
            String period = getPeriod(buyLink);
            telegram.sendMessageToChannel("CGV\n오후 2시 판매시작!\n%s\n%s\n%s원\n1+1관람권:%s\n구매링크:%s\n\n이미지:%s",
                    newTicket.getDescription(), period, "가격", onePlusOneTickets.size(), buyLink, newTicket.getImageUrl()
            );
            c = 1;
            cache1p1Tickets.clear();
            cache1p1Tickets.addAll(onePlusOneTickets);
        }

        log.info("CGV      \t- 호출횟수:{}, 지난관람권:{}, 1+1관람권:{}, isChangedTicket:{}",
                callCount.incrementAndGet(), cache1p1Tickets.size(), c, isChangedTicket);
    }

    private CgvItem getNew1p1Ticket(List<CgvItem> onePlusOneTickets) {
        return onePlusOneTickets.stream()
                .filter(nextTickets -> cache1p1Tickets.stream().noneMatch(prevTickets -> prevTickets.getIdx() == nextTickets.getIdx()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("1+1 티켓 없음!!"));
    }

    private boolean isChangedTickets(List<CgvItem> next1p1Tickets) {
        return !next1p1Tickets.stream()
                .allMatch(nextTicket -> cache1p1Tickets.stream().anyMatch(prevTicket -> prevTicket.getIdx() == nextTicket.getIdx()));
    }

    private List<CgvItem> getCgvTicketsData() throws IOException {
        Document cgvDocument = Jsoup.connect(cgv).get();
        String html = cgvDocument.html();
        Pattern pattern = Pattern.compile("var ( +)?jsonData( +)?=( +)?(\\[(.+)?\\])( +)?;?");
        Matcher matcher = pattern.matcher(html);
        String json = null;
        if (matcher.find()) {
            json = matcher.group(4);
        }
        try {
            return new Gson().fromJson(json, new TypeToken<List<CgvItem>>() {}.getType());
        } catch (JsonSyntaxException e) {
            log.debug("CGV json 파싱 실패! {}", json);
        }
        log.debug(html);
        throw new IllegalArgumentException("cgv 데이터 조회 실패!");
    }

    private List<CgvItem> get1p1Tickets() throws IOException {
        return getCgvTicketsData().stream()
                .filter(this::is1p1Ticket)
                .sorted((o1, o2) -> o2.getIdx() - o1.getIdx())
                .collect(Collectors.toList());
    }

    private boolean is1p1Ticket(CgvItem cgvItem) {
        return cgvItem.getDescription().contains("1+1") || cgvItem.getDescription().contains("원플러스원");
    }

    private String getPeriod(String buyLink) {
        try {
            Document cgv = Jsoup.connect(buyLink).get();
            Elements elements = cgv.select("em.date");
            elements.select("span").remove();
            return elements.text();
        } catch (IOException e) {
            log.debug("CGV 사용기간 조회 실패. {}", buyLink);
        }
        return "CGV 사용기간 조회 실패.";
    }

}
