package com.bot.cinemabot;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import com.bot.cinemabot.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Telegram {

    @Value("${bot.telegram.token}")
    private String token;
    @Value("${bot.telegram.chatId}")
    private String chatId;
    @Value("${bot.telegram.channel}")
    private String channel;
    @Value("${bot.telegram.api.sendMessage}")
    private String sendMessageUrl;
    @Value("${test}")
    private String TEST;

    @PostConstruct
    public void init() {
        channel = "@" + channel;
        sendMessageUrl = String.format(sendMessageUrl, token);
        sendMessageToBot("시네마봇 재시작 되었습니다.");
    }

    @PreDestroy
    public void destroy() {
        sendMessageToBot("시네마봇 종료 되었습니다.");
    }

    public void sendMessageToBot(String message, Object... obj) {
        message = String.format(message, obj);
        log.info(message);
        String telegramSendMessageUrl = sendMessageUrl + String.format("?chat_id=%s&text=%s", chatId, message);
        String response = Utils.restTemplate.getForObject(telegramSendMessageUrl, String.class);
        if (!response.contains("\"ok\":true")) {
            log.error("텔레그램 메시지 전송 실패. {}", response);
        }
    }

    public void sendMessageToChannel(String message, Object... obj) {
        message = String.format(message, obj);
        log.info(message);
        if (!Boolean.valueOf(TEST)) {
            LinkedMultiValueMap data = new LinkedMultiValueMap();
            data.add("chat_id", channel);
            data.add("text", message);
            String response = Utils.restTemplate.postForObject(sendMessageUrl, data, String.class);
            if (!response.contains("\"ok\":true")) {
                log.error("텔레그램 메시지 전송 실패. {}", response);
            }
        }
    }

}
