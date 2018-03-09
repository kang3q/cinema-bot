package com.bot.cinemabot.utils;

import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

public class Utils {

    static final public Gson gson = new Gson();

    static final public RestTemplate restTemplate = new RestTemplate();

}
