package com.bot.cinemabot.scheduler;

import com.bot.cinemabot.service.CgvService;
import com.bot.cinemabot.service.LotteCinemaService;
import com.bot.cinemabot.service.MegaboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CinemaScheduler {

	private final CgvService cgvService;
	private final LotteCinemaService lotteCinemaService;
	private final MegaboxService megaboxService;

	@Autowired
	public CinemaScheduler(CgvService cgvService, LotteCinemaService lotteCinemaService, MegaboxService megaboxService) {
		this.cgvService = cgvService;
		this.lotteCinemaService = lotteCinemaService;
		this.megaboxService = megaboxService;
	}

	@Scheduled(cron = "0 0/2 08-20 ? * MON-FRI", zone = "Asia/Seoul") // 월~금, 매월, 아무 날이나, 08:00 ~ 20:59, 2분마다, 0초에
	public void lotteCinema() {
		lotteCinemaService.aJob();
	}

	@Scheduled(cron = "20 0/2 08-20 ? * MON-FRI", zone = "Asia/Seoul") // 월~금, 매월, 아무 날이나, 08:00 ~ 20:59, 2분마다, 20초에
	public void cgv() throws IOException {
		cgvService.aJob();
	}

	@Scheduled(cron = "40 0/2 08-20 ? * MON-FRI", zone = "Asia/Seoul") // 월~금, 매월, 아무 날이나, 08:00 ~ 20:59, 2분마다, 40초에
	public void megabox() throws IOException {
		megaboxService.aJob();
	}

}
