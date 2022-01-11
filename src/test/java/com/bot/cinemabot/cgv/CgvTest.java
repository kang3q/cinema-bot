package com.bot.cinemabot.cgv;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bot.cinemabot.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import com.bot.cinemabot.model.cgv.CgvItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CgvTest {

    @Test
    public void test1_리스트조회() throws IOException {
        System.out.println("test1_리스트조회");

        Document cgv = Jsoup.connect("http://www.cgv.co.kr/culture-event/event").get();

        String html = cgv.html();
//        System.out.println(html);

        Pattern pattern = Pattern.compile("var ( +)?jsonData( +)?=( +)?(\\[(.+)?\\])( +)?;?");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            String json = matcher.group(4);
            System.out.println("-> json: " + json);
            List<CgvItem> items = new Gson().fromJson(json, new TypeToken<List<CgvItem>>() {}.getType());
            System.out.println("-> items: " + Utils.gson.toJson(items));
        }

    }

    @Test
    public void test2_상세정보조회() throws IOException {
        Document cgv = Jsoup.connect("http://www.cgv.co.kr/culture-event/event/detail-view.aspx?idx=17674&menu=0").get();
        Elements elements = cgv.select("em.date");

        System.out.println("\ntest2_상세정보조회");
        System.out.println("-> " + elements.text());
        System.out.println("-> " + elements.select("span").remove());
        System.out.println("-> " + elements.text());
    }

}
