package com.bot.cinemabot.model.megabox;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MegaboxTicket {
	private String name;
	private String price;
	private String date;
	private String link;
	private String itemCode;
	private boolean isSoldOut;
}
