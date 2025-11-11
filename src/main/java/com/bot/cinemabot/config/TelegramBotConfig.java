package com.bot.cinemabot.config;

import com.bot.cinemabot.service.CgvService;
import com.bot.cinemabot.service.LotteCinemaService;
import com.bot.cinemabot.service.MegaboxService;
import com.bot.cinemabot.utils.PingPong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Telegram Bot Configuration
 *
 * PingPong 봇을 생성하고 Telegram API에 등록합니다.
 * 구버전 Telegram Bot API(4.6)를 사용하므로 수동 빈 생성이 필요합니다.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class TelegramBotConfig {

    @Value("${spring.bot.telegram.token}")
    private String token;

    @Value("${spring.bot.telegram.username}")
    private String username;

    private final CgvService cgvService;
    private final LotteCinemaService lotteCinemaService;
    private final MegaboxService megaboxService;

    @Bean
    public PingPong pingPong() {
        PingPong pingPong = createPingPong();
        registerBot(pingPong);
        return pingPong;
    }

    /**
     * PingPong 봇 인스턴스를 생성하고 의존성을 주입합니다.
     */
    private PingPong createPingPong() {
        PingPong pingPong = new PingPong();
        pingPong.setToken(token);
        pingPong.setUsername(username);
        pingPong.setCgvService(cgvService);
        pingPong.setLotteCinemaService(lotteCinemaService);
        pingPong.setMegaboxService(megaboxService);
        log.info("PingPong 봇 생성 완료: {}", username);
        return pingPong;
    }

    /**
     * Telegram API에 봇을 등록합니다.
     * 등록 실패 시에도 애플리케이션은 계속 실행됩니다 (스케줄러는 정상 동작).
     */
    private void registerBot(PingPong pingPong) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi();
            botsApi.registerBot(pingPong);
            log.info("텔레그램 봇이 성공적으로 등록되었습니다: {}", username);
        } catch (TelegramApiException e) {
            log.error("텔레그램 봇 등록 실패 - 봇이 메시지를 받을 수 없습니다: {}", e.getMessage(), e);
            // 에러 발생 시에도 애플리케이션은 계속 실행 (스케줄러는 동작)
        }
    }
}
