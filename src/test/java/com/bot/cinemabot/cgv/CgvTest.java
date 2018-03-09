package com.bot.cinemabot.cgv;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.bot.cinemabot.model.cgv.CgvItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CgvTest {

    @Test
    public void test1 () throws IOException {

        Document cgv = Jsoup.connect("http://www.cgv.co.kr/culture-event/event").get();

        String html = cgv.html();
//        System.out.println(html);

        Pattern pattern = Pattern.compile("var jsonData ?= ?(\\[.+\\]);?");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            String json = matcher.group(1);
            System.out.println(json);
            List<CgvItem> items = new Gson().fromJson(json, new TypeToken<List<CgvItem>>(){}.getType());
            System.out.println(items);
        }

    }

}
