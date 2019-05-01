package com.bot.cinemabot.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.bot.cinemabot.model.lotte.MenuItem;
import com.bot.cinemabot.model.lotte.ProductItem;
import com.bot.cinemabot.model.lotte.LCMallMainItems;
import com.bot.cinemabot.model.lotte.LotteCinemaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import com.bot.cinemabot.utils.Telegram;
import com.bot.cinemabot.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LotteCinemaService {

    @Autowired
    private Telegram telegram;

    @Value("${bot.cinema.lotte.api}")
    private String lotte;

    final private AtomicInteger callCount = new AtomicInteger(0);
    final private AtomicInteger cacheAllTicketsCount = new AtomicInteger(0);
    private List<ProductItem> cache1p1Tickets;

    @PostConstruct
    private void init() {
        LotteCinemaResponse data = getCinemaData();
        LCMallMainItems lcMallMainItems = data.getLCMall_Main_Items();
        int allTicketsCount = allTicketsCount(lcMallMainItems);
        List<ProductItem> onePlusOneTickets = get1p1Tickets(lcMallMainItems);
        cache1p1Tickets = Collections.synchronizedList(onePlusOneTickets);

        telegram.sendMessageToBot("롯데시네마\n모든 관람권: %s\n1+1 관람권: %s",
                allTicketsCount, onePlusOneTickets.size());
    }

    public void aJob() {
        LotteCinemaResponse data = getCinemaData();
        LCMallMainItems lcMallMainItems = data.getLCMall_Main_Items();
        List<ProductItem> onePlusOneTickets = get1p1Tickets(lcMallMainItems);
        int allTicketsCount = allTicketsCount(lcMallMainItems);

        updateCache(allTicketsCount);

        boolean isChangedTicket = isChangedTicket(onePlusOneTickets);

        if (allTicketsCount == -1) {
            log.debug(Utils.gson.toJson(data));
        } else if (isChangedTicket) {
            ProductItem movieItem = getNew1p1Ticket(lcMallMainItems);
            if (!StringUtils.isEmpty(movieItem.getDisplayItemName())) {
                ProductItem movieItemDetail = getDetailCinemaData(movieItem.getDisplayItemID());
                String buyLink = String.format(
                         "http://www.lottecinema.co.kr/LCMW/Contents/Cinema-Mall/e-shop-detail.aspx?displayItemID=%s&displayMiddleClassification=%s&displayMenuID=%s",
                        movieItemDetail.getDisplayItemID(), movieItemDetail.getDisplayLargeClassificationCode(), movieItemDetail.getMenuId()
                );
                telegram.sendMessageToChannel("롯데시네마\n%s\n%s\n%s원\n1+1관람권:%s, 영화관람권:%s\n구매링크:%s", //\n\n이미지:%s",
                        movieItemDetail.getDisplayItemName(), movieItemDetail.getUseRestrictionsDayName(), movieItemDetail.getDiscountSellPrice(),
                        onePlusOneTickets.size(), cacheAllTicketsCount, buyLink //, movieItemDetail.getItemImageNm()
                );
            }
            updateCache(onePlusOneTickets);
        }

        log.info("롯데시네마\t- 호출횟수:{}, 영화관람권:{}, 1+1관람권:{}, isChangedTicket:{}",
                callCount.incrementAndGet(), cacheAllTicketsCount, cache1p1Tickets.size(), isChangedTicket);
    }

    private boolean isChangedTicket(List<ProductItem> newTickets) {
        boolean a = !newTickets
                .stream()
                .allMatch(newTicket ->
                        cache1p1Tickets.stream().anyMatch(oldTicket -> oldTicket.getDisplayItemName().equals(newTicket.getDisplayItemName()))
                );
        boolean b = !cache1p1Tickets
                .stream()
                .allMatch(oldTicket ->
                        newTickets.stream().anyMatch(newTicket -> newTicket.getDisplayItemName().equals(oldTicket.getDisplayItemName()))
                );
        return a || b;
    }

    private int allTicketsCount(LCMallMainItems lcMallMainItems) {
        return lcMallMainItems.getMenu_Items().getItems()
                .stream()
                .filter(menuItem -> menuItem.getMenuId() == 3 && menuItem.getMenuTitle().equals("관람권"))
                .findFirst()
                .map(MenuItem::getProdCount)
                .orElse(-1);
    }

    private List<ProductItem> get1p1Tickets(LCMallMainItems lcMallMainItems) {
        return lcMallMainItems.getProduct_Items().getItems()
                .stream()
                .filter(item -> 20 == item.getDisplayLargeClassificationCode())
                .filter(item -> 40 == item.getCombiItmDivCd())
                .filter(item -> item.getDisplayItemName().contains("1+1") || item.getDisplayItemName().contains("얼리버드"))
                .collect(Collectors.toList());
    }

    private ProductItem getNew1p1Ticket(LCMallMainItems lcMallMainItems) {
        return get1p1Tickets(lcMallMainItems)
                .stream()
                .filter(newTicket -> cache1p1Tickets.stream()
                        .noneMatch(oldTicket -> oldTicket.getDisplayItemName().equals(newTicket.getDisplayItemName())))
                .findFirst()
                .orElse(new ProductItem());
    }

    private LotteCinemaResponse getCinemaData() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("paramList",
                "{\"MethodName\":\"GetLCMallMain\",\"channelType\":\"MW\",\"osType\":\"Chrome\",\"osVersion\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36\",\"multiLanguageID\":\"KR\",\"menuID\":\"3\"}");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        String jsonResponse = Utils.restTemplate.postForObject(lotte, request, String.class);
        return Utils.gson.fromJson(jsonResponse, LotteCinemaResponse.class);
    }

    private ProductItem getDetailCinemaData(String itemID) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("paramList",
                 "{\"MethodName\":\"GetLCMallDetail\",\"channelType\":\"MW\",\"osType\":\"Chrome\",\"osVersion\":\"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36\",\"multiLanguageID\":\"KR\",\"menuID\":\"3\",\"itemID\":\"" + itemID + "\",\"classificationCode\":\"20\"}"
        );

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

        String jsonResponse = Utils.restTemplate.postForObject(lotte, request, String.class);
        LotteCinemaResponse response = Utils.gson.fromJson(jsonResponse, LotteCinemaResponse.class);
        return response.getLCMall_Detail_Items().getProduct_Items().getItems().get(0);
    }

    private void updateCache(int allTicketsCount) {
        if (cacheAllTicketsCount.get() != allTicketsCount) {
            cacheAllTicketsCount.set(allTicketsCount);
        }
    }

    private void updateCache(List<ProductItem> tickets) {
        cache1p1Tickets.clear();
        cache1p1Tickets.addAll(tickets);
    }

}
