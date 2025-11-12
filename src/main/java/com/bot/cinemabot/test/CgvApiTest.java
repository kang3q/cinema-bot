package com.bot.cinemabot.test;

import com.bot.cinemabot.model.cgv.CgvApiResponse;
import com.bot.cinemabot.model.cgv.CgvItem;
import com.google.gson.Gson;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CgvApiTest {

    private static final String CGV_SECRET_KEY = "ydqXY0ocnFLmJGHr_zNzFcpjwAsXq_8JcBNURAkRscg";
    private static final String CGV_API_URL = "https://event.cgv.co.kr/evt/evt/evt/searchEvtListForPage?coCd=A420&evntCtgryLclsCd=03&sscnsChoiYn=N&expnYn=N&expoChnlCd=01&startRow=0&listCount=10";

    public static void main(String[] args) {
        try {
            System.out.println("=== CGV REST API 테스트 ===\n");

            // 현재 timestamp 생성 (초 단위)
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            // URL 파싱하여 pathname 추출
            URL url = new URL(CGV_API_URL);
            String pathname = url.getPath();

            // GET 요청이므로 body는 빈 문자열
            String body = "";

            // 서명 생성
            String signature = generateSignature(pathname, body, timestamp);

            System.out.println("요청 정보:");
            System.out.println("- URL: " + CGV_API_URL);
            System.out.println("- X-TIMESTAMP: " + timestamp);
            System.out.println("- X-SIGNATURE: " + signature);
            System.out.println();

            // REST API 호출
            Connection connection = Jsoup.connect(CGV_API_URL)
                .header("Accept", "application/json")
                .header("Accept-Language", "ko-KR")
                .header("X-TIMESTAMP", timestamp)
                .header("X-SIGNATURE", signature)
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36")
                .ignoreContentType(true)
                .ignoreHttpErrors(true);

            Connection.Response response = connection.execute();

            System.out.println("응답 정보:");
            System.out.println("- Status Code: " + response.statusCode());
            System.out.println("- Status Message: " + response.statusMessage());
            System.out.println();

            if (response.statusCode() != 200) {
                System.err.println("API 호출 실패!");
                return;
            }

            String jsonResponse = response.body();
            System.out.println("응답 본문 (처음 500자):");
            System.out.println(jsonResponse.substring(0, Math.min(500, jsonResponse.length())));
            System.out.println("...\n");

            // API 응답 파싱
            CgvApiResponse apiResponse = new Gson().fromJson(jsonResponse, CgvApiResponse.class);

            if (apiResponse == null) {
                System.err.println("API 응답 파싱 실패!");
                return;
            }

            System.out.println("파싱 결과:");
            System.out.println("- statusCode: " + apiResponse.getStatusCode());
            System.out.println("- statusMessage: " + apiResponse.getStatusMessage());

            if (apiResponse.getData() != null) {
                System.out.println("- totalCount: " + apiResponse.getData().getTotalCount());
                System.out.println("- listCount: " + apiResponse.getData().getListCount());
                System.out.println("- list size: " +
                    (apiResponse.getData().getList() != null ? apiResponse.getData().getList().size() : 0));
                System.out.println();

                if (apiResponse.getData().getList() != null && !apiResponse.getData().getList().isEmpty()) {
                    System.out.println("첫 번째 이벤트:");
                    CgvItem firstItem = apiResponse.getData().getList().get(0);
                    System.out.println("- 이벤트 번호: " + firstItem.getEvntNo());
                    System.out.println("- 이벤트 이름: " + firstItem.getEvntNm());
                    System.out.println("- 시작일: " + firstItem.getEvntStartDt());
                    System.out.println("- 종료일: " + firstItem.getEvntEndDt());
                    System.out.println("- 이미지 URL: " + firstItem.getImageUrl());
                    System.out.println("- 링크: " + firstItem.getLink());
                    System.out.println("- Description: " + firstItem.getEvntNm());
                }
            }

        } catch (Exception e) {
            System.err.println("테스트 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String generateSignature(String pathname, String body, String timestamp) {
        try {
            String message = timestamp + "|" + pathname + "|" + body;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                CGV_SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
