package com.bot.cinemabot.magbox;

import com.bot.cinemabot.model.megabox.MegaboxResponse;
import com.bot.cinemabot.model.megabox.MegaboxTicket;
import com.bot.cinemabot.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.springframework.util.LinkedMultiValueMap;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class magaboxTest {
	Pattern pattern = Pattern.compile("\\d{4}.(?!\\d)*\\d{2}.(?!\\d)*\\d{2}(?:~|\\s)*\\d{4}.(?!\\d)*\\d{2}.(?!\\d)*\\d{2}"); // 2020.02.11 ~ 2020.03.02

	@Test
	public void test1_리스트조회 () throws IOException {
		System.out.println("test1_리스트조회");
		LinkedMultiValueMap data = new LinkedMultiValueMap();
		String response = Utils.restTemplate.postForObject("https://m.megabox.co.kr/on/oh/ohd/StoreMain/selectStoreMainList.do", data, String.class);
		MegaboxResponse res = Utils.gson.fromJson(response, MegaboxResponse.class);
		List<MegaboxTicket> allTickets = res.convertTickets("detailUrl");
		System.out.println("-> allTickets: " + Utils.gson.toJson(allTickets));
		List<MegaboxTicket> onePlusOneTickets = allTickets.stream()
				.filter(t -> t.getName().replaceAll("\\s+", "").contains("1+1"))
				.collect(Collectors.toList());
		System.out.println("-> 1+1 티켓: " + Utils.gson.toJson(onePlusOneTickets));
	}

	@Test
	public void test2_상세정보조회 () throws IOException {
		System.out.println("\ntest2_상세정보조회");
		String itemCode = "20001239";
		Document megaboxDocument = Jsoup
				.connect("https://m.megabox.co.kr/on/oh/ohd/StoreDtl/selectStoreDtl.do")
//				.data("cmbndKindNo", itemCode) // 예전 1+1 티켓 -> 기간이 지나서 조회가 안되네, 아래 일반 관람권으로 대체
				.data("prdtClCd", "CPC02")
				.data("prdtNo", "1271")
				.post();
		String name = megaboxDocument.select(".prod-info .tit").text().trim();
		System.out.println("-> name: " + name);
		String price = megaboxDocument.select(".prod-info .price .roboto").text().trim();
		System.out.println("-> price: " + price);
		String date = megaboxDocument.select(".prod-info-detail > span").text();
		System.out.println("-> date: " + date);
		Matcher matcher = pattern.matcher(date);
		if (matcher.find()) {
			System.out.println("-> date: " + matcher.group());
		}
		System.out.println("-> 1+1티켓: " + Utils.gson.toJson(new MegaboxTicket(name, price, date, itemCode, itemCode, false)));
	}
}
