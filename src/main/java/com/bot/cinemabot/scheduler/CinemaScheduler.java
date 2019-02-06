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

	@Scheduled(initialDelay = 1_000, fixedDelayString = "${bot.schedule.fixedDelay}")
	public void lotteCinema() {
		lotteCinemaService.aJob();
	}

	@Scheduled(initialDelay = 20_000, fixedDelayString = "${bot.schedule.fixedDelay}")
	public void cgv() throws IOException {
		cgvService.aJob();
	}

	@Scheduled(initialDelay = 40_000, fixedDelayString = "${bot.schedule.fixedDelay}")
	public void megabox() throws IOException {
		megaboxService.aJob();
	}

}
