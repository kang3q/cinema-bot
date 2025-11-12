package com.bot.cinemabot.model.cgv;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class CgvItem {
    // API 응답 필드
    private String coCd;
    private String evntNo;
    private String evntNm;
    private String snsEvntNm;
    private String ordtmExpoYn;
    private String ntcPerdSamYn;
    private int prty;
    private String evntStartDt;
    private String evntEndDt;
    private String evntCtgryLclsCd;
    private String evntCtgryMclsCd;
    private String evntCtgrySclsCd;
    private String lagBanrMediaDivCd;
    private String lagBanrPhyscFilePathnm;
    private String lagBanrPhyscFnm;
    private String lagBanrAccbWordCont;
    private String mduBanrMediaDivCd;
    private String mduBanrPhyscFilePathnm;
    private String mduBanrPhyscFnm;
    private String mduBanrAccbWordCont;
    private String addEvntChoiYn;
    private String addEvntTypCd;
    private String addEvntInfregYn;
    private int queryCnt;
    private String evntCwrtTypCd;
    private String evntLnkUrl;

    // 기존 코드 호환성을 위한 메서드
    public int getIdx() {
        // evntNo를 정수로 변환 (예: "202510313355" → 숫자)
        if (evntNo != null && !evntNo.isEmpty()) {
            try {
                return Integer.parseInt(evntNo);
            } catch (NumberFormatException e) {
                return evntNo.hashCode(); // 변환 실패시 hashCode 사용
            }
        }
        return 0;
    }

    public String getImageUrl() {
        // lagBanrPhyscFilePathnm + lagBanrPhyscFnm을 결합하여 이미지 URL 생성
        if (lagBanrPhyscFilePathnm != null && lagBanrPhyscFnm != null) {
            return "https://cdn.cgv.co.kr/" + lagBanrPhyscFilePathnm + "/" + lagBanrPhyscFnm;
        }
        return "";
    }

    public String getLink() {
        if (evntNo != null) {
            return "https://cgv.co.kr/evt/eventDetail?evntNo=" + evntNo;
        }
        return "";
    }
}
