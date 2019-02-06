package com.bot.cinemabot.service;

import com.bot.cinemabot.utils.Telegram;
import com.bot.cinemabot.model.megabox.MegaboxResponse;
import com.bot.cinemabot.model.megabox.MegaboxTicket;
import com.bot.cinemabot.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MegaboxService {

	@Autowired
	private Telegram telegram;

	@Value("${bot.cinema.megabox.api}")
	private String megabox;
	@Value("${bot.cinema.megabox.detailUrl}")
	private String detailUrl;

	private List<MegaboxTicket> cache1p1Tickets;
	final private AtomicInteger callCount = new AtomicInteger(0);

	@PostConstruct
	private void init() {
		List<MegaboxTicket> allTickets = getMegaboxTickets();
		List<MegaboxTicket> onePlusOneTickets = filtered1p1Tickets(allTickets);
		cache1p1Tickets = Collections.synchronizedList(onePlusOneTickets);
		telegram.sendMessageToBot("메가박스\n모든 관람권: %s\n1+1 관람권: %s",
				allTickets.size(), onePlusOneTickets.size());
	}

	public void aJob() throws IOException {
		List<MegaboxTicket> allTickets = getMegaboxTickets();
		List<MegaboxTicket> onePlusOneTickets = filtered1p1Tickets(allTickets);
		boolean isChangedTicket = isChangedTicket(onePlusOneTickets);
		if (!onePlusOneTickets.isEmpty() && isChangedTicket) {
			MegaboxTicket new1p1Tickets = getNew1p1Ticket(onePlusOneTickets);
			if (!StringUtils.isEmpty(new1p1Tickets.getName())) {
				MegaboxTicket newTickets = getDetailInfo(new1p1Tickets.getItemCode());
				telegram.sendMessageToBot("메가박스\n%s\n%s\n%s원\n개별사용 불가\n1+1관람권:%s, 영화관람권:%s\n구매링크:%s\n", //\n\n이미지:%s",
						newTickets.getName(), newTickets.getDate(), newTickets.getPrice(),
						onePlusOneTickets.size(), allTickets.size(), newTickets.getLink()
				);
			}
			updateCache(onePlusOneTickets);
		}

		log.info("메가박스\t- 호출횟수:{}, 영화관람권:{}, 1+1관람권:{}, isChangedTicket:{}",
				callCount.incrementAndGet(), allTickets.size(), cache1p1Tickets.size(), isChangedTicket);
	}

	private boolean isChangedTicket(List<MegaboxTicket> newTickets) {
		boolean a = !newTickets
				.stream()
				.allMatch(newTicket ->
						cache1p1Tickets.stream().anyMatch(oldTicket -> oldTicket.getName().equals(newTicket.getName()))
				);
		boolean b = !cache1p1Tickets
				.stream()
				.allMatch(oldTicket ->
						newTickets.stream().anyMatch(newTicket -> newTicket.getName().equals(oldTicket.getName()))
				);
		return a || b;
	}

	private void updateCache(List<MegaboxTicket> tickets) {
		cache1p1Tickets.clear();
		cache1p1Tickets.addAll(tickets);
	}

	private List<MegaboxTicket> getMegaboxTickets() {
		LinkedMultiValueMap data = new LinkedMultiValueMap();
		data.add("_command", "Store.getList");
		data.add("siteCode", "43");
		data.add("majorCode", "03");
		data.add("minorCode", "00");
		data.add("size", "small");
		String response = Utils.restTemplate.postForObject(megabox, data, String.class);
		MegaboxResponse res = Utils.gson.fromJson(response, MegaboxResponse.class);
		return res.convertTickets(detailUrl);
	}

	private MegaboxTicket getDetailInfo(@PathVariable String itemCode) throws IOException {
		Document megaboxDocument = Jsoup.connect(detailUrl + itemCode).get();
		String date = megaboxDocument.select("#content .good_info .date time").text();
		String price = megaboxDocument.select("#content #displayPrice").val();
		String name = megaboxDocument.select("#form_pay input[name=itemName]").val();
		return new MegaboxTicket(name, price, date, detailUrl + itemCode, itemCode, false);
	}

	private List<MegaboxTicket> filtered1p1Tickets(List<MegaboxTicket> tickets) {
		return tickets.stream()
				.filter(t -> removeSpace(t.getName()).contains("1+1"))
				.collect(Collectors.toList());
	}

	private String removeSpace(String name) {
		return name.replaceAll("\\s+", "");
	}

	private MegaboxTicket getNew1p1Ticket(List<MegaboxTicket> new1p1Tickets) {
		return new1p1Tickets
				.stream()
				.filter(newTicket -> cache1p1Tickets.stream()
						.noneMatch(oldTicket -> oldTicket.getName().equals(newTicket.getName())))
				.findFirst()
				.orElse(new MegaboxTicket());
	}

}

