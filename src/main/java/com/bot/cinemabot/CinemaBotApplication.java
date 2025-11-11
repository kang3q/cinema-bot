package com.bot.cinemabot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Cinema Bot Application
 *
 * 영화관 1+1 티켓 정보를 크롤링하여 텔레그램 채널에 알림을 보내는 봇 애플리케이션입니다.
 *
 * 주요 기능:
 * - CGV, 롯데시네마, 메가박스의 1+1 티켓 정보 크롤링
 * - 새로운 티켓 등록 시 텔레그램 채널에 자동 알림
 * - 텔레그램 봇 명령어 (/ping, /list) 지원
 * - 스케줄러: 평일 08:00~20:59, 2분마다 크롤링
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class CinemaBotApplication {

	static {
		/**
		 * Telegram Bot API Context 초기화 (구버전 API 4.6용)
		 *
		 * 정적 블록에서 초기화하여 Spring 빈 생성 전에 ApiContext가 준비되도록 합니다.
		 * - BotSession 구현체를 DefaultBotSession으로 등록
		 * - ApiContextInitializer로 Guice Injector 초기화
		 *
		 * 이 초기화가 없으면 PingPong 빈 생성 시 "No implementation for BotSession" 에러 발생
		 */
		ApiContext.register(BotSession.class, DefaultBotSession.class);
		ApiContextInitializer.init();
	}

	public static void main(String[] args) {
		SpringApplication application = new SpringApplication(CinemaBotApplication.class);
		application.addListeners(new ApplicationPidFileWriter());
		application.run(args);
	}
}
