package com.bot.cinemabot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CinemaBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(CinemaBotApplication.class, args);
	}
}
