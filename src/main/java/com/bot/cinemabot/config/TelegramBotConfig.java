package com.bot.cinemabot.config;

import com.bot.cinemabot.service.CgvService;
import com.bot.cinemabot.service.LotteCinemaService;
import com.bot.cinemabot.service.MegaboxService;
import com.bot.cinemabot.utils.PingPong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Configuration
public class TelegramBotConfig {

    @Value("${spring.bot.telegram.token}")
    private String token;

    @Value("${spring.bot.telegram.username}")
    private String username;

    @Bean
    public PingPong pingPong(CgvService cgvService, LotteCinemaService lotteCinemaService, MegaboxService megaboxService) {
        PingPong pingPong = new PingPong();
        pingPong.setToken(token);
        pingPong.setUsername(username);
        pingPong.setCgvService(cgvService);
        pingPong.setLotteCinemaService(lotteCinemaService);
        pingPong.setMegaboxService(megaboxService);

        // Telegram API에 봇 등록
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi();
            botsApi.registerBot(pingPong);
            log.info("텔레그램 봇이 성공적으로 등록되었습니다: {}", username);
        } catch (TelegramApiException e) {
            log.error("텔레그램 봇 등록 실패: {}", e.getMessage(), e);
        }

        return pingPong;
    }
}
