package com.bot.cinemabot.utils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.bot.cinemabot.model.MessageFormat;
import com.bot.cinemabot.repo.GoogleSpreadSheetsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

import com.bot.cinemabot.model.socket.Greeting;
import com.bot.cinemabot.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class Telegram {

    // @Autowired
    // private SimpMessagingTemplate template;
    @Autowired
    private GoogleSpreadSheetsRepo googleSpreadSheetsRepo;

    @Value("${spring.bot.telegram.token}")
    private String token;
    @Value("${spring.bot.telegram.chatId}")
    private String chatId;
    @Value("${spring.bot.telegram.channel}")
    private String channel;
    @Value("${spring.bot.telegram.api.sendMessage}")
    private String sendMessageUrl;
    @Value("${spring.profiles}")
    private String profile;

    @PostConstruct
    public void init() {
        sendMessageUrl = String.format(sendMessageUrl, token);
        sendMessageToBot(String.format("[%s] 시네마봇 재시작 되었습니다.", profile));
    }

    @PreDestroy
    public void destroy() {
        sendMessageToBot(String.format("[%s] 시네마봇 종료 되었습니다.", profile));
    }

    public void sendMessageToBot(String message, Object... obj) {
        message = String.format(message, obj);
        log.info("\n" + message);
        LinkedMultiValueMap data = new LinkedMultiValueMap();
        data.add("chat_id", chatId);
        data.add("text", message);
        String response = Utils.restTemplate.postForObject(sendMessageUrl, data, String.class);
        // template.convertAndSend("/topic/greetings", new Greeting(message));
        if (!response.contains("\"ok\":true")) {
            log.error("텔레그램 메시지 전송 실패. {}", response);
        }
    }

    public void sendMessageToChannel(String message, Object... obj) {
        message = String.format(message, obj);
        log.info("\n" + message);
        LinkedMultiValueMap data = new LinkedMultiValueMap();
        data.add("chat_id", channel);
        data.add("text", message);
        String response = Utils.restTemplate.postForObject(sendMessageUrl, data, String.class);
        // template.convertAndSend("/topic/greetings", new Greeting(message));
		if (!response.contains("\"ok\":true")) {
			log.error("텔레그램 메시지 전송 실패. {}", response);
		}
    }

    public void sendMessageToChannel(final MessageFormat mf) {
        googleSpreadSheetsRepo.save(mf);
        String message = mf.convertText4AD();
        log.info("\n" + message);
        LinkedMultiValueMap data = new LinkedMultiValueMap();
        data.add("chat_id", channel);
        data.add("text", message);
        data.add("disable_web_page_preview", mf.isDisableWebPagePreview());
        String response = Utils.restTemplate.postForObject(sendMessageUrl, data, String.class);
        // template.convertAndSend("/topic/greetings", new Greeting(message));
        if (!response.contains("\"ok\":true")) {
            log.error("텔레그램 메시지 전송 실패. {}", response);
        }
    }

    public void sendHTMLToChannel(final MessageFormat mf) {
        googleSpreadSheetsRepo.save(mf);
        String message = mf.convertHTML();
        log.info("\n" + message);
        LinkedMultiValueMap data = new LinkedMultiValueMap();
        data.add("chat_id", channel);
        data.add("text", message);
        data.add("parse_mode", "html");
        data.add("disable_web_page_preview", mf.isDisableWebPagePreview());
        String response = Utils.restTemplate.postForObject(sendMessageUrl, data, String.class);
        // template.convertAndSend("/topic/greetings", new Greeting(message));
        if (!response.contains("\"ok\":true")) {
            log.error("텔레그램 메시지 전송 실패. {}", response);
        }
    }

}
