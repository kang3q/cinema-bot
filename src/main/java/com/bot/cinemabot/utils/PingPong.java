package com.bot.cinemabot.utils;

import com.bot.cinemabot.service.CgvService;
import com.bot.cinemabot.service.LotteCinemaService;
import com.bot.cinemabot.service.MegaboxService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import com.bot.cinemabot.model.socket.Greeting;

import java.io.IOException;

/**
 * Created by 1004w455 on 2018. 4. 16..
 */
@Slf4j
@Component
public class PingPong extends TelegramLongPollingBot {

//    @Autowired
//    private SimpMessagingTemplate template;

    @Autowired
    private CgvService cgvService;

    @Autowired
    private LotteCinemaService lotteCinemaService;

    @Autowired
    private MegaboxService megaboxService;

    @Value("${spring.bot.telegram.token}")
    private String token;

    @Value("${spring.bot.telegram.username}")
    private String username;

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            SendMessage response = new SendMessage();
            Long chatId = message.getChatId();
            response.setChatId(chatId);
            String text = message.getText();

            if ("영화".equals(text)) {
                try {
                    response.setText(String.format("%s\n\n%s\n\n%s", cgvService.checkStatus(), lotteCinemaService.checkStatus(), megaboxService.checkStatus()));
                } catch (IOException e) {
                    response.setText(e.getMessage());
                }
            } else {
                response.setText(text);
            }
            executeSend(response, chatId, text);
        }
    }

    private void executeSend(SendMessage response, Long chatId, String text) {
        try {
            execute(response);
            // template.convertAndSend("/topic/greetings", new Greeting(text));
            log.info("Sent message \"{}\" to {}", text, chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message \"{}\" to {} due to error: {}", text, chatId, e.getMessage());
        }
    }

}
