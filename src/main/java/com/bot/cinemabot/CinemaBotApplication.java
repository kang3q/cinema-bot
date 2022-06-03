package com.bot.cinemabot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class CinemaBotApplication {

	public static void main(String[] args) {
		//		SpringApplication.run(CinemaBotApplication.class, args);
		SpringApplication application = new SpringApplication(CinemaBotApplication.class);
		application.addListeners(new ApplicationPidFileWriter());
		application.run(args);
	}
}
