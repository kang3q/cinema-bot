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

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class CinemaBotApplication {

	static {
		// Telegram Bot API Context 초기화
		ApiContext.register(BotSession.class, DefaultBotSession.class);
		ApiContextInitializer.init();
	}

	public static void main(String[] args) {
		//		SpringApplication.run(CinemaBotApplication.class, args);
		SpringApplication application = new SpringApplication(CinemaBotApplication.class);
		application.addListeners(new ApplicationPidFileWriter());
		application.run(args);
	}
}
