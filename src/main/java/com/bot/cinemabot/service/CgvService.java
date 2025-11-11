package com.bot.cinemabot.service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import com.bot.cinemabot.model.MessageFormat;
import com.bot.cinemabot.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.bot.cinemabot.utils.Telegram;
import com.bot.cinemabot.model.cgv.CgvItem;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CgvService {

    @Autowired
    private Telegram telegram;

    @Value("${spring.bot.cinema.cgv.api}")
    private String cgv;

    final private AtomicInteger callCount = new AtomicInteger(0);
    private List<CgvItem> cache1p1Tickets;

    @PostConstruct
    private void init() {
        try {
            List<CgvItem> onePlusOneTickets = initOnePlusOneTickets();
            String message = String.format("CGV\n모든 1+1관람권: %s", onePlusOneTickets.size());
            telegram.sendMessageToBot(message);
        } catch (Exception e) {
            log.error("CGV 초기화 실패: {}", e.getMessage(), e);
            cache1p1Tickets = Collections.synchronizedList(Collections.emptyList());
            telegram.sendMessageToBot("CGV 초기화 실패 (스케줄러에서 재시도됩니다): " + e.getMessage());
        }
    }

    public List<CgvItem> initOnePlusOneTickets() throws IOException {
        List<CgvItem> onePlusOneTickets = get1p1Tickets();
        cache1p1Tickets = Collections.synchronizedList(onePlusOneTickets);
        return onePlusOneTickets;
    }

    public void aJob() {
        try {
            List<CgvItem> onePlusOneTickets = get1p1Tickets();

            if (onePlusOneTickets.isEmpty()) {
                return;
            }

            boolean isChangedTicket = isChangedTickets(onePlusOneTickets);
            int c = 0;

            if (isChangedTicket) {
                CgvItem newTicket = getNew1p1Ticket(onePlusOneTickets);
                String buyLink = newTicket.getLink();
                String period = getPeriod(buyLink);
                MessageFormat format = new MessageFormat("CGV", newTicket.getDescription(), period, "", String.valueOf(onePlusOneTickets.size()), "", buyLink, true, Utils.generateKey());
                telegram.sendMessageToChannel(format);
                c = 1;
                cache1p1Tickets.clear();
                cache1p1Tickets.addAll(onePlusOneTickets);
            }

            // log.info("CGV   \t- 호출횟수:{}, 지난관람권:{}, 1+1관람권:{}, isChangedTicket:{}",
            //         callCount.incrementAndGet(), cache1p1Tickets.size(), c, isChangedTicket);
        } catch (Exception e) {
            log.error("CGV 스케줄 작업 실패: {}", e.getMessage(), e);
        }
    }

    private CgvItem getNew1p1Ticket(List<CgvItem> onePlusOneTickets) {
        return onePlusOneTickets.stream()
                .filter(nextTickets -> cache1p1Tickets.stream().noneMatch(prevTickets -> prevTickets.getIdx() == nextTickets.getIdx()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("1+1 티켓 없음!!"));
    }

    private boolean isChangedTickets(List<CgvItem> next1p1Tickets) {
        if (cache1p1Tickets == null || cache1p1Tickets.isEmpty()) {
            return true;
        }
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

        if (json == null) {
            log.error("CGV HTML에서 jsonData를 찾을 수 없습니다.");
            log.debug("CGV HTML: {}", html);
            return Collections.emptyList();
        }

        try {
            List<CgvItem> result = new Gson().fromJson(json, new TypeToken<List<CgvItem>>() {}.getType());
            if (result == null) {
                log.error("CGV json 파싱 결과가 null입니다. json: {}", json);
                return Collections.emptyList();
            }
            return result;
        } catch (JsonSyntaxException e) {
            log.error("CGV json 파싱 실패! json: {}", json, e);
            return Collections.emptyList();
        }
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
