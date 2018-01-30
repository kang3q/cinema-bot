package com.bot.cinemabot.model;

import lombok.Data;

@Data
public class CinemaItem {
    private String DisplayLargeClassificationCode; // "20",
    private String DisplayMiddleClassificationCode; // "50",
    private String DisplayMiddleClassificationName; // "베스트",
    private String DisplayItemID; // "1801220003",
    private String DisplayItemName; // "[설패키지]샤롯데팩",
    private String UseRestrictionsDayName; // "구매 후 3 개월",
    private String CustomerBuyRestrictionsName; // "1인 1일 1회",
    private long CurrentSellPrice; // 70000,
    private long DiscountSellPrice; // 60000,
    private int RefundPassibleDayCount; // 93,
    private int PackageYN; // 0,
    private int ConstitutionProductionDivCode; // 0,
    private int OptionTemplateCode; // 0,
    private String ItemImageUrl; // "htt; ////caching.lottecinema.co.kr//Media/WebAdmin/30c29f7ba0354b7cafed14a258fa8242.jpg",
    private String ItemImageAlt; // "[설패키지]샤롯데팩",
    private String DisplayItemDescription; // "",
    private int EarlyBirdYN; // 0,
    private int SortSequence; // 1
}
