package com.bot.cinemabot.model.cgv;

import lombok.Data;

@Data
public class CgvApiResponse {
    private int statusCode;
    private String statusMessage;
    private CgvApiData data;
}
