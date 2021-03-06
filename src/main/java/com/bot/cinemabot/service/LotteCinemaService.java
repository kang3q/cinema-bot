package com.bot.cinemabot.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.bot.cinemabot.model.MessageFormat;
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

    @Value("${spring.bot.cinema.lotte.api}")
    private String lotte;

    final private AtomicInteger callCount = new AtomicInteger(0);
    final private AtomicInteger cacheAllTicketsCount = new AtomicInteger(0);
    private List<ProductItem> cache1p1Tickets;

    @PostConstruct
    private void init() {
        List<ProductItem> onePlusOneTickets = initOnePlusOneTickets();
        String message = String.format("롯데시네마\n1+1 관람권: %s", onePlusOneTickets.size());
        telegram.sendMessageToBot(message);
    }

    public List<ProductItem> initOnePlusOneTickets() {
        LotteCinemaResponse data = getCinemaData();
        LCMallMainItems lcMallMainItems = data.getLCMall_Main_Items();
        List<ProductItem> onePlusOneTickets = get1p1Tickets(lcMallMainItems);
        cache1p1Tickets = Collections.synchronizedList(onePlusOneTickets);
        return onePlusOneTickets;
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
                         "https://www.lottecinema.co.kr/NLCMW/CinemaMall/Detail?ItemId=%s&ClassificationCode=%s&MenuId=%s",
                        movieItemDetail.getDisplayItemID(), movieItemDetail.getDisplayLargeClassificationCode(), movieItemDetail.getMenuId()
                );
                MessageFormat format = new MessageFormat("롯데시네마", movieItemDetail.getDisplayItemName(), movieItemDetail.getUseRestrictionsDayName(), String.valueOf(movieItemDetail.getDiscountSellPrice()), String.valueOf(onePlusOneTickets.size()), String.valueOf(cacheAllTicketsCount), buyLink, true, Utils.generateKey());
                telegram.sendMessageToChannel(format);
            }
            updateCache(onePlusOneTickets);
        }

        // log.info("롯데시네마\t- 호출횟수:{}, 영화관람권:{}, 1+1관람권:{}, isChangedTicket:{}",
        //         callCount.incrementAndGet(), cacheAllTicketsCount, cache1p1Tickets.size(), isChangedTicket);
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
