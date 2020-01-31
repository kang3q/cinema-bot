package com.bot.cinemabot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MessageFormat {

	private String platform;
	private String title;
	private String dateRange;
	private String price;
	private String exist1p1;
	private String existTicket;
	private String buyUrl;
	private boolean disableWebPagePreview;
	private String key;

	public String convertText() {
		return String.format(getTextTemplate(), this.platform, this.title, this.dateRange, this.price, this.exist1p1, this.existTicket, this.buyUrl);
	}

	public String convertText4AD() {
		return String.format("%s\nhttps://cinemabot.duckdns.org/?key=%s", this.title, this.key);
	}

	public String convertHTML() {
		return String.format(getHTMLTemplate(), this.platform, this.title, this.dateRange, this.price, this.exist1p1, this.existTicket, this.buyUrl, this.buyUrl);
	}

	private String getTextTemplate() {
		// TODO 템플릿 파일로 분리를 하자
		switch (this.platform) {
			case "CGV":
				return "%s\n%s\n오후 2시 판매시작!(or 4시)\n%s%s\n1+1영화:%s종%s\n구매링크: %s";
			case "롯데시네마":
				return "%s\n%s\n%s\n%s원\n1+1영화:%s종, 티켓:%s종\n구매링크: %s";
			case "메가박스":
				return "%s\n%s\n%s\n%s원(개별사용 불가)\n1+1영화:%s종, 티켓:%s종\n구매링크: %s";
		}
		throw new IllegalArgumentException(String.format("지원하지 않는 platform 입니다. %s", this.platform));
	}

	private String getHTMLTemplate() {
		// TODO 템플릿 파일로 분리를 하자
		switch (this.platform) {
			case "CGV":
				return "%s\n<b>%s</b>\n오후 2시 판매시작!(or 4시)\n%s%s\n1+1영화: %s종%s\n<a href=\"%s\">%s</a>";
			case "롯데시네마":
				return "%s\n<b>%s</b>\n%s\n%s원\n1+1영화: %s종, 티켓: %s종\n<a href=\"%s\">%s</a>";
			case "메가박스":
				return "%s\n<b>%s</b>\n%s\n%s원(개별사용 불가)\n1+1영화: %s종, 티켓: %s종\n<a href=\"%s\">%s</a>";
		}
		throw new IllegalArgumentException(String.format("지원하지 않는 platform 입니다. %s", this.platform));
	}

}
// text 성공: 롯시, cgv
