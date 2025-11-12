package com.bot.cinemabot.service;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import jakarta.annotation.PostConstruct;

import com.bot.cinemabot.model.MessageFormat;
import com.bot.cinemabot.utils.Utils;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.bot.cinemabot.utils.Telegram;
import com.bot.cinemabot.model.cgv.CgvItem;
import com.bot.cinemabot.model.cgv.CgvApiResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CgvService {

    private static final String CGV_SECRET_KEY = "ydqXY0ocnFLmJGHr_zNzFcpjwAsXq_8JcBNURAkRscg";

    @Autowired
    private Telegram telegram;

    @Value("${spring.bot.cinema.cgv.api}")
    private String cgv;

    final private AtomicInteger callCount = new AtomicInteger(0);
    private List<CgvItem> cache1p1Tickets;

    @PostConstruct
    private void init() {
        try {
            List<CgvItem> onePlusOneTickets = initOnePlusOneTickets();
            String message = String.format("CGV\n1+1관람권: %s", onePlusOneTickets.size());
            telegram.sendMessageToBot(message);
        } catch (Exception e) {
            log.error("CGV 초기화 실패: {}", e.getMessage(), e);
            cache1p1Tickets = Collections.synchronizedList(Collections.emptyList());
            telegram.sendMessageToBot("CGV 초기화 실패 (스케줄러에서 재시도됩니다): " + e.getMessage());
        }
    }

    public List<CgvItem> initOnePlusOneTickets() throws IOException {
        List<CgvItem> onePlusOneTickets = get1p1Tickets();
        cache1p1Tickets = Collections.synchronizedList(onePlusOneTickets);
        return onePlusOneTickets;
    }

    public void aJob() {
        try {
            List<CgvItem> onePlusOneTickets = get1p1Tickets();

            if (onePlusOneTickets.isEmpty()) {
                return;
            }

            boolean isChangedTicket = isChangedTickets(onePlusOneTickets);
            int c = 0;

            if (isChangedTicket) {
                CgvItem newTicket = getNew1p1Ticket(onePlusOneTickets);
                String buyLink = newTicket.getLink();
                String period = newTicket.getEvntEndDt();
                MessageFormat format = new MessageFormat("CGV", newTicket.getEvntNm(), period, "", String.valueOf(onePlusOneTickets.size()), "", buyLink, true, Utils.generateKey());
                telegram.sendMessageToChannel(format);
                c = 1;
                cache1p1Tickets.clear();
                cache1p1Tickets.addAll(onePlusOneTickets);
            }

            // log.info("CGV   \t- 호출횟수:{}, 지난관람권:{}, 1+1관람권:{}, isChangedTicket:{}",
            //         callCount.incrementAndGet(), cache1p1Tickets.size(), c, isChangedTicket);
        } catch (Exception e) {
            log.error("CGV 스케줄 작업 실패: {}", e.getMessage(), e);
        }
    }

    private CgvItem getNew1p1Ticket(List<CgvItem> onePlusOneTickets) {
        return onePlusOneTickets.stream()
                .filter(nextTickets -> cache1p1Tickets.stream().noneMatch(prevTickets -> prevTickets.getIdx() == nextTickets.getIdx()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("1+1 티켓 없음!!"));
    }

    private boolean isChangedTickets(List<CgvItem> next1p1Tickets) {
        if (cache1p1Tickets == null || cache1p1Tickets.isEmpty()) {
            return true;
        }
        return !next1p1Tickets.stream()
                .allMatch(nextTicket -> cache1p1Tickets.stream().anyMatch(prevTicket -> prevTicket.getIdx() == nextTicket.getIdx()));
    }

    /**
     * CGV API 호출을 위한 HMAC-SHA256 서명 생성
     *
     * @param pathname URL의 경로 부분
     * @param body 요청 body (GET 요청이면 빈 문자열)
     * @param timestamp Unix timestamp (초 단위)
     * @return Base64로 인코딩된 HMAC-SHA256 서명
     */
    private String generateSignature(String pathname, String body, String timestamp) {
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
            log.error("CGV 서명 생성 실패: {}", e.getMessage(), e);
            return "";
        }
    }

    private List<CgvItem> getCgvTicketsData() throws IOException {
        try {
            // 현재 timestamp 생성 (초 단위)
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

            // URL 파싱하여 pathname 추출
            URL url = new URL(cgv);
            String pathname = url.getPath();

            // GET 요청이므로 body는 빈 문자열
            String body = "";

            // 서명 생성
            String signature = generateSignature(pathname, body, timestamp);

            // REST API 호출
            Connection connection = Jsoup.connect(cgv)
                .header("Accept", "application/json")
                .header("Accept-Language", "ko-KR")
                .header("X-TIMESTAMP", timestamp)
                .header("X-SIGNATURE", signature)
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36")
                .ignoreContentType(true)
                .ignoreHttpErrors(true);

            Connection.Response response = connection.execute();

            // 응답 상태 코드 확인
            if (response.statusCode() != 200) {
                log.error("CGV API 호출 실패. Status: {}, Message: {}", response.statusCode(), response.statusMessage());
                return Collections.emptyList();
            }

            // JSON 응답 받기
            String jsonResponse = response.body();

            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                log.error("CGV API 응답이 비어있습니다.");
                return Collections.emptyList();
            }

            log.debug("CGV API Response: {}", jsonResponse);

            try {
                // API 응답을 CgvApiResponse로 파싱
                CgvApiResponse apiResponse = new Gson().fromJson(jsonResponse, CgvApiResponse.class);

                if (apiResponse == null) {
                    log.error("CGV API 응답 파싱 결과가 null입니다.");
                    return Collections.emptyList();
                }

                if (apiResponse.getStatusCode() != 0) {
                    log.error("CGV API 에러. StatusCode: {}, Message: {}",
                        apiResponse.getStatusCode(), apiResponse.getStatusMessage());
                    return Collections.emptyList();
                }

                if (apiResponse.getData() == null || apiResponse.getData().getList() == null) {
                    log.error("CGV API data 또는 list가 null입니다.");
                    return Collections.emptyList();
                }

                log.info("CGV API 조회 성공. TotalCount: {}, ListCount: {}",
                    apiResponse.getData().getTotalCount(),
                    apiResponse.getData().getList().size());

                return apiResponse.getData().getList();
            } catch (JsonSyntaxException e) {
                log.error("CGV json 파싱 실패! json: {}", jsonResponse, e);
                return Collections.emptyList();
            }
        } catch (Exception e) {
            log.error("CGV 데이터 조회 실패: {}", e.getMessage(), e);
            throw new IOException("CGV 데이터 조회 실패", e);
        }
    }

    private List<CgvItem> get1p1Tickets() throws IOException {
        return getCgvTicketsData().stream()
                .filter(this::is1p1Ticket)
                .sorted((o1, o2) -> o2.getIdx() - o1.getIdx())
                .collect(Collectors.toList());
    }

    private boolean is1p1Ticket(CgvItem cgvItem) {
        return cgvItem.getEvntNm().contains("1+1") || cgvItem.getEvntNm().contains("원플러스원");
    }

    private String getPeriod(String buyLink) {
        try {
            Document cgv = Jsoup.connect(buyLink).get();
            Elements elements = cgv.select("em.date");
            elements.select("span").remove();
            return elements.text();
        } catch (IOException e) {
            log.debug("CGV 사용기간 조회 실패. {}", buyLink);
        }
        return "CGV 사용기간 조회 실패.";
    }

}
