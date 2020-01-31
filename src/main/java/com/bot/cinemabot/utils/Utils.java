package com.bot.cinemabot.utils;

import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    static final public Gson gson = new Gson();

    static final public RestTemplate restTemplate = new RestTemplate();

    static final public SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

    static public String generateKey () {
        return df.format(new Date());
    }

}
