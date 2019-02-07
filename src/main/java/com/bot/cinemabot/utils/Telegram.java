package com.bot.cinemabot.utils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import com.bot.cinemabot.model.socket.Greeting;
import com.bot.cinemabot.utils.Utils;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Slf4j
@Component
public class Telegram {

//    @Autowired
//    private SimpMessagingTemplate template;

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
        try {
            String telegramSendMessageUrl = sendMessageUrl + String.format("?chat_id=%s&text=%s", chatId, URLEncoder.encode(message, "UTF-8"));
            String response = Utils.restTemplate.getForObject(telegramSendMessageUrl, String.class);
//            template.convertAndSend("/topic/greetings", new Greeting(message));
            if (!response.contains("\"ok\":true")) {
                log.error("텔레그램 메시지 전송 실패. {}", response);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
//            template.convertAndSend("/topic/greetings", new Greeting(message));
            if (!response.contains("\"ok\":true")) {
                log.error("텔레그램 메시지 전송 실패. {}", response);
            }
        }
    }

}
