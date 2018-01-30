package com.bot.cinemabot;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.bot.cinemabot.model.CinemaResponse;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LotteCinemaScheduler {

    final private RestTemplate restTemplate = new RestTemplate();
    final private Gson gson = new Gson();

    final private AtomicInteger lastCount = new AtomicInteger(0);
    final private AtomicInteger callCount = new AtomicInteger(0);

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000 * 10)
    public void aJob() {
        AtomicInteger count = new AtomicInteger(0);
        CinemaResponse data = getCinemaData();

        data.getCinemaMallItemLists().getCinemaMallClassifications().getItems()
                .stream()
                .filter(item -> "20".equals(item.getDisplayLargeClassificationCode()))
                .filter(item -> "10".equals(item.getDisplayMiddleClassificationCode()))
                .findFirst()
                .ifPresent(item -> count.set(item.getItemCount()));

        if (count.get() != lastCount.get()) {
            lastCount.set(count.get());
            sendMessage("얼리버드 1+1");
            log.info("신규영화 생겼다.");
        }
        log.info("호출횟수:{}, 현재영화갯수:{}", callCount.incrementAndGet(), lastCount);
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
        String telegramURL =
                String.format("https://api.telegram.org/bot477314226:AAHNHCnV9nfMIwMEGaUzam_CvQBNJ9PrFZ8/sendMessage?chat_id=451573335&text=%s",
                        message);
        restTemplate.getForObject(telegramURL, String.class);
    }

}
