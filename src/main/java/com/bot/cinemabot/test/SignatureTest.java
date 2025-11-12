package com.bot.cinemabot.test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SignatureTest {

    private static final String CGV_SECRET_KEY = "ydqXY0ocnFLmJGHr_zNzFcpjwAsXq_8JcBNURAkRscg";

    public static void main(String[] args) {
        try {
            // REST API 테스트용 값들
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String pathname = "/evt/evt/evt/searchEvtListForPage";
            String body = "";

            // 서명 생성
            String signature = generateSignature(pathname, body, timestamp);

            System.out.println("=== CGV REST API 서명 생성 테스트 ===");
            System.out.println("API URL: https://event.cgv.co.kr/evt/evt/evt/searchEvtListForPage");
            System.out.println("Timestamp: " + timestamp);
            System.out.println("Pathname: " + pathname);
            System.out.println("Body: " + body);
            System.out.println("Message: " + timestamp + "|" + pathname + "|" + body);
            System.out.println("Signature: " + signature);
            System.out.println();
            System.out.println("HTTP 헤더:");
            System.out.println("X-TIMESTAMP: " + timestamp);
            System.out.println("X-SIGNATURE: " + signature);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String generateSignature(String pathname, String body, String timestamp) {
        try {
            // 메시지 생성: ${timestamp}|${pathname}|${body}
            String message = timestamp + "|" + pathname + "|" + body;

            // HMAC-SHA256 서명 생성
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                CGV_SECRET_KEY.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

            // Base64 인코딩
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
