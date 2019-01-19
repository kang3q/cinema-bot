package com.bot.cinemabot.model.megabox;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class MegaboxResponse {
	private List<TicketItem> list;

	public List<MegaboxTicket> convertTickets(final String detailUrl) {
		return this.list.stream()
				.map(item -> new MegaboxTicket(item.getItemDetailName(), item.getDisplayPrice_txt(), "", detailUrl + item.getItemCode(), item.getRemain_cnt() == 0))
				.collect(Collectors.toList());
	}
}

@Data
class TicketItem {
	private String displayPrice_txt;
	private String itemCode;
	private String itemDetailName;
	private int remain_cnt;
}
