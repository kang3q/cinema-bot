package com.bot.cinemabot.ppomppu;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

public class PpomppuTest {

    public String getPageNo(Element title) {
        String href = title.parent().attr("href");
        MultiValueMap<String, String> params = UriComponentsBuilder.fromUriString(href).build().getQueryParams();
        return params.get("no").get(0);
    }

    @Test
    public void test() throws IOException {
        Document cgv = Jsoup.connect("http://www.ppomppu.co.kr/zboard/zboard.php?id=ppomppu").get();
        Elements elements = cgv.select("td[valign=\"middle\"] > a > font");

        elements.stream()
//                .filter(element -> element.text().contains("옥션"))
                .forEach(element -> System.out.println(
                        String.format("[%s] %s", getPageNo(element), element.text())
                ));
    }

}
