package com.bot.cinemabot.magbox;

import com.bot.cinemabot.model.megabox.MegaboxTicket;
import com.bot.cinemabot.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class magaboxTest {
	Pattern pattern = Pattern.compile("\\d{4}.(?!\\d)*\\d{2}.(?!\\d)*\\d{2}(?:~|\\s)*\\d{4}.(?!\\d)*\\d{2}.(?!\\d)*\\d{2}");

	@Test
	public void test1 () throws IOException {
		String itemCode = "20001239";
		Document megaboxDocument = Jsoup
				.connect("https://m.megabox.co.kr/on/oh/ohd/StoreDtl/selectStoreDtl.do")
				.data("cmbndKindNo", itemCode)
				.post();
		String name = megaboxDocument.select(".prod-info .tit").text().trim();
		System.out.println(name);
		String price = megaboxDocument.select(".prod-info .price .roboto").text().trim();
		System.out.println(price);
		String date = megaboxDocument.select(".prod-info-detail > span").text();
		System.out.println(date);
		Matcher matcher = pattern.matcher(date);
		if (matcher.find()) {
			System.out.println(matcher.group());
		}
		System.out.println(Utils.gson.toJson(new MegaboxTicket(name, price, date, itemCode, itemCode, false)));
	}
}
