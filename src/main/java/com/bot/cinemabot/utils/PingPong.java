package com.bot.cinemabot.utils;

import com.bot.cinemabot.model.cgv.CgvItem;
import com.bot.cinemabot.model.lotte.ProductItem;
import com.bot.cinemabot.model.megabox.MegaboxTicket;
import com.bot.cinemabot.service.CgvService;
import com.bot.cinemabot.service.LotteCinemaService;
import com.bot.cinemabot.service.MegaboxService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.util.StringUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Telegram Bot - PingPong
 *
 * 텔레그램 명령어를 처리하는 봇입니다.
 * - /ping: "pong" 응답
 * - /list: 현재 1+1 영화 티켓 목록 조회
 */
@Slf4j
@Setter  // Lombok으로 모든 setter 메서드 자동 생성
public class PingPong extends TelegramLongPollingBot {

    private CgvService cgvService;
    private LotteCinemaService lotteCinemaService;
    private MegaboxService megaboxService;
    private String token;
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
            String msg = null;
            if ("/list".equals(text)) {
                try {
                    String cgv = getCgvMovieTitles();
                    String lotte = getLotteMovieTitles();
                    String megabox = getMegaboxMovieTitles();
                    msg = Stream.of(cgv, lotte, megabox)
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining("\n\n"));
                    if (StringUtils.isEmpty(msg)) {
                        msg = "1+1 영화가 없습니다.";
                    }
                } catch (Exception e) {
                    response.setText(e.getMessage());
                    executeSend(response, chatId, text);
                    return;
                }
            } else if ("/ping".equals(text)) {
                msg = "pong";
            }

            if (Objects.nonNull(msg)) {
                response.setText(msg);
                executeSend(response, chatId, text);
            }
        }
    }

    private String getMegaboxMovieTitles() {
        List<MegaboxTicket> tickets = megaboxService.initOnePlusOneTickets();
        if (tickets.isEmpty()) return null;
        return "메가박스\n" + tickets
                .stream()
                .map(MegaboxTicket::getName)
                .collect(Collectors.joining("\n"));
    }

    private String getLotteMovieTitles() {
        List<ProductItem> tickets = lotteCinemaService.initOnePlusOneTickets();
        if (tickets.isEmpty()) return null;
        return "롯데시네마\n" + tickets
                .stream()
                .map(ProductItem::getDisplayItemName)
                .collect(Collectors.joining("\n"));
    }

    private String getCgvMovieTitles() throws IOException {
        List<CgvItem> tickets = cgvService.initOnePlusOneTickets();
        if (tickets.isEmpty()) return null;
        return "CGV\n" + tickets
                .stream()
                .map(CgvItem::getEvntNm)
                .collect(Collectors.joining("\n"));
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
