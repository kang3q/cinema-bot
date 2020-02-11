package com.bot.cinemabot.model.megabox;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class MegaboxResponse {
	private List<TicketItem> storePrdtPageList;

	public List<MegaboxTicket> convertTickets(final String detailUrl) {
		return this.storePrdtPageList.stream()
				.map(item -> new MegaboxTicket(item.getPrdtNm(), String.valueOf(item.getPrdtNormAmt()), "", detailUrl + item.getCmbndKindNo(), item.getCmbndKindNo(), "Y".equals(item.getSoldoutAt()) || item.getPrdtRmainQty() == 0))
				.collect(Collectors.toList());
	}
}

@Data
class TicketItem {
	private int pageIndex;         // 1,
	private String prdtClCd;       // "CPC03",
	private String cmbndKindNo;    // "20001236",
	private String prdtNm;         // "[정직한 후보] 앵콜! 1+1 관람권",
	private String prdtCompsDesc;  // "[정직한 후보] 앵콜! 1+1 관람권",
	private int prdtImgNo;         // 1011308,
	private String prdtBadgeCd;    // "CPB02",
	private int prdtNormAmt;       // 9000,
	private int prdtExpoAmt;       // 9000,
	private String imgPathNm;      // "/SharedImg/store/2020/02/10/c78aN5LxTef5O99Gdp6QHSI1MiopGgV1.jpg",
	private int prdtTotSellLmtQty; // 2000,
	private int prdtRmainQty;      // 1862,
	private String soldoutAt;      // "N"

}
