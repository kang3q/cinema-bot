package com.bot.cinemabot.lotte;

import com.bot.cinemabot.model.lotte.LotteCinemaResponse;
import com.bot.cinemabot.model.lotte.ProductItem;
import com.bot.cinemabot.utils.Utils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.stream.Collectors;

public class LotteTest {

	@Test
	public void test1 () {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("paramList",
				"{\"MethodName\":\"GetLCMallMain\",\"channelType\":\"MW\",\"osType\":\"Chrome\",\"osVersion\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36\",\"multiLanguageID\":\"KR\",\"menuID\":\"3\"}");
//		"{\"MethodName\":\"CinemaMallGiftItemList\",\"channelType\":\"HO\",\"osType\":\"Chrome\",\"osVersion\":\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36\",\"multiLanguageID\":\"KR\",\"classificationCode\":\"20\"}");
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		String jsonResponse = Utils.restTemplate.postForObject("http://www.lottecinema.co.kr/LCWS/CinemaMall/CinemaMallData.aspx?nocashe=0.2966092977934629", request, String.class);

		// 1차 데이터
		System.out.println(jsonResponse);

		LotteCinemaResponse lotteCinemaResponse = Utils.gson.fromJson(jsonResponse, LotteCinemaResponse.class);

		System.out.println(Utils.gson.toJson(lotteCinemaResponse));

		List<ProductItem> onePlusOneTickets = lotteCinemaResponse.getLCMall_Main_Items().getProduct_Items().getItems()
				.stream()
				.filter(item -> 20 == item.getDisplayLargeClassificationCode())
				.filter(item -> 40 == item.getCombiItmDivCd())
				.filter(item -> item.getDisplayItemName().contains("1+1") || item.getDisplayItemName().contains("얼리버드"))
				.collect(Collectors.toList());

		System.out.println(Utils.gson.toJson(onePlusOneTickets));
	}

	@Test
	public void test2_상세정보조회 () {
//		{
//			'MethodName': 'GetLCMallDetail',
//			'channelType': 'MW',
//			'osType': 'Chrome',
//			'osVersion': 'Mozilla/5.0 (Linux; Android 7.0; SM-G935K Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/61.0.3163.98 Mobile Safari/537.36 wshop/1.0',
//			'multiLanguageID': 'KR',
//			'menuID': '3',
//			'itemID': '1903140001',
//			'classificationCode': '20',
//		}
//		String paramListFormat = "{\"MethodName\":\"GetLCMallDetail\",\"channelType\":\"MW\",\"osType\":\"Chrome\",\"osVersion\":\"Mozilla/5.0 (Linux; Android 7.0; SM-G935K Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/61.0.3163.98 Mobile Safari/537.36 wshop/1.0\",\"multiLanguageID\":\"KR\",\"menuID\":\"%s\",\"itemID\":\"%s\",\"classificationCode\":\"%s\"}";
//		String paramListStr = String.format(paramListFormat, onePlusOneTickets.get(0).getMenuId(), onePlusOneTickets.get(0).getDisplayItemID(), onePlusOneTickets.get(0).getDisplayLargeClassificationCode());

		String itemID = "1904300001";
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("paramList",
				"{\"MethodName\":\"GetLCMallDetail\",\"channelType\":\"MW\",\"osType\":\"Chrome\",\"osVersion\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36\",\"multiLanguageID\":\"KR\",\"menuID\":\"3\",\"itemID\":\"" + itemID + "\",\"classificationCode\":\"20\"}"
		);
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
		String jsonResponse = Utils.restTemplate.postForObject("http://www.lottecinema.co.kr/LCWS/CinemaMall/CinemaMallData.aspx?nocashe=0.2966092977934629", request, String.class);

		// 1차 데이터
		System.out.println(jsonResponse);

		LotteCinemaResponse lotteCinemaResponse = Utils.gson.fromJson(jsonResponse, LotteCinemaResponse.class);

		System.out.println(Utils.gson.toJson(lotteCinemaResponse));

		ProductItem item =lotteCinemaResponse.getLCMall_Detail_Items().getProduct_Items().getItems().get(0);

		System.out.println(Utils.gson.toJson(item));
		System.out.println(item.getUseRestrictionsDayName()); // 사용기간


	}

}
