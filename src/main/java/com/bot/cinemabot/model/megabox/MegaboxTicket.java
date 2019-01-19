package com.bot.cinemabot.model.megabox;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MegaboxTicket {
	private String name;
	private String price;
	private String date;
	private String link;
	private boolean isSoldOut;
}
