package com.bot.cinemabot;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.bot.cinemabot.model.CinemaItem;
import com.bot.cinemabot.model.CinemaMallItem;
import com.bot.cinemabot.model.CinemaResponse;
import com.bot.cinemabot.model.DisplayItem;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LotteCinemaScheduler {

    @Value("${telegram.token}")
    private String token;
    @Value("${telegram.chatId}")
    private String chatId;
    @Value("${telegram.sendMessageUrl}")
    private String sendMessageUrl;

    final private RestTemplate restTemplate = new RestTemplate();
    final private Gson gson = new Gson();

    final private AtomicInteger callCount = new AtomicInteger(0);
    final private AtomicInteger lastCount = new AtomicInteger(0);
    private List<CinemaItem> cache1p1Tickets;

    final private boolean TEST = false;

    @PostConstruct
    public void hi() {
        sendMessageUrl = String.format(sendMessageUrl, token, chatId);
        sendMessage("시네마봇 재시작 되었습니다.");
        cache1p1Tickets = Collections.synchronizedList(getCinemaData().getCinemaMallItemLists().getItems().getItems());
    }

    @PreDestroy
    public void bye() {
        sendMessage("시네마봇 종료 되었습니다.");
    }

    @Scheduled(initialDelay = 1_000, fixedDelayString = "${schedule.fixedDelay}")
    public void aJob() {
        CinemaResponse data = getCinemaData();
        CinemaMallItem cinemaMallItems = data.getCinemaMallItemLists();
        int allTicketsCount = ticketsCount(cinemaMallItems);
        List<CinemaItem> onePlusOneTickets = get1p1Tickets(cinemaMallItems);

        if (allTicketsCount == -1) {
            log.debug(gson.toJson(data));
        } else if (isChangedTicket(onePlusOneTickets)) {
            updateCache(onePlusOneTickets, allTicketsCount);
            CinemaItem movieItem = first1p1Ticket(cinemaMallItems);
            sendMessage(String.format("%s\n%s원\n1+1관람권:%s, 영화관람권:%s\n%s",
                    movieItem.getDisplayItemName(), movieItem.getDiscountSellPrice(),
                    onePlusOneTickets.size(), lastCount, movieItem.getItemImageUrl())
            );
        }

        log.info("호출횟수:{}, 영화관람권:{}, 1+1관람권:{}", callCount.incrementAndGet(), lastCount, onePlusOneTickets.size());
    }

    private void updateCache(List<CinemaItem> tickets, int allTicketsCount) {
        lastCount.set(allTicketsCount);
        cache1p1Tickets.clear();
        cache1p1Tickets.addAll(tickets);
    }

    private boolean isChangedTicket(List<CinemaItem> tickets) {
        return !cache1p1Tickets
                .stream()
                .allMatch(item ->
                        tickets.stream().anyMatch(t -> t.getDisplayItemName().equals(item.getDisplayItemName()))
                );
    }

    private int ticketsCount(CinemaMallItem cinemaMallItems) {
        return cinemaMallItems.getCinemaMallClassifications().getItems()
                .stream()
                .filter(item -> "20".equals(item.getDisplayLargeClassificationCode()))
                .filter(item -> "10".equals(item.getDisplayMiddleClassificationCode()))
                .findFirst()
                .map(DisplayItem::getItemCount)
                .orElse(-1);
    }

    private List<CinemaItem> get1p1Tickets(CinemaMallItem cinemaMallItems) {
        return cinemaMallItems.getItems().getItems()
                .stream()
                .filter(item -> "20".equals(item.getDisplayLargeClassificationCode()))
                .filter(item -> "10".equals(item.getDisplayMiddleClassificationCode()))
                .filter(item -> "영화관람권".equals(item.getDisplayMiddleClassificationName()))
                .filter(item -> item.getDisplayItemName().contains("1+1"))
                .collect(Collectors.toList());
    }

    private CinemaItem first1p1Ticket(CinemaMallItem cinemaMallItems) {
        return get1p1Tickets(cinemaMallItems)
                .stream()
                .findFirst()
                .orElse(new CinemaItem());
    }

    private CinemaResponse getCinemaData() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("paramList",
                "{\"MethodName\":\"CinemaMallGiftItemList\",\"channelType\":\"HO\",\"osType\":\"Chrome\",\"osVersion\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36\",\"multiLanguageID\":\"KR\",\"classificationCode\":\"20\"}");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        String jsonResponse = restTemplate.postForObject("http://www.lottecinema.co.kr/LCWS/CinemaMall/CinemaMallData.aspx", request, String.class);
        return gson.fromJson(jsonResponse, CinemaResponse.class);
    }

    private void sendMessage(String message) {
        log.info(message);
        if (!TEST) {
            String telegramSendMessageUrl = sendMessageUrl + message;
            String response = restTemplate.getForObject(telegramSendMessageUrl, String.class);
            if (!response.contains("\"ok\":true")) {
                log.error("텔레그램 메시지 전송 실패. {}", response);
            }
        }
    }

}
