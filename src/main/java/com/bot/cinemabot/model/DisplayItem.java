package com.bot.cinemabot.model;

import lombok.Data;

@Data
public class DisplayItem {
    private String DisplayLargeClassificationCode;
    private String DisplayMiddleClassificationCode;
    private String DisplayMiddleClassificationName;
    private int ItemCount;
}
