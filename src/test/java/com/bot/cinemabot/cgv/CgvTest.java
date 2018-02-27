package com.bot.cinemabot.cgv;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class CgvTest {

    @Test
    public void test1 () throws IOException {

        Document cgv = Jsoup.connect("http://www.cgv.co.kr/culture-event/event/#2").get();

        String html = cgv.html();
//        System.out.println(html);

        Pattern pattern = Pattern.compile("var jsonData ?= ?(\\[.+\\]);?");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            System.out.println(matcher.group(1));
        }

    }

}
