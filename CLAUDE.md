# Cinema Bot 프로젝트 컨텍스트
중요 변경사항은 컨텍스트 업데이트 하도록 사용자에게 제시하기

## 프로젝트 개요
영화관(롯데시네마, CGV, 메가박스) 1+1 관람권 정보를 크롤링하여 텔레그램 채널로 자동 알림하는 봇

## 기술 스택
- Java 17, Spring Boot 3.2.1, Maven
- jsoup (웹 크롤링), telegram-spring-boot-starter 0.22

## 핵심 구조
```
scheduler/CinemaScheduler.java    # 2분마다 크롤링 실행
service/
  ├── LotteCinemaService.java     # 롯데시네마 크롤링
  ├── CgvService.java             # CGV 크롤링
  └── MegaboxService.java         # 메가박스 크롤링
utils/
  ├── Telegram.java               # 텔레그램 메시지 전송
  └── PingPong.java               # 텔레그램 봇 명령어 처리
repo/GoogleSpreadSheetsRepo.java  # Google Sheets 저장
resources/application.yml         # 설정 (local/production 프로파일)
```

## 프로파일
- `local`: 개발 환경
- `production`: 운영 환경 (토큰은 환경변수 `CINEMA_BOT_TOKEN`으로 전달)

## 텔레그램 채널
- 운영: `@cinema1p1`

---

## 최근 작업 내역

### 2025-12-04: Telegram 연결 에러 로그 제거
**문제:** `DefaultBotSession`에서 Long Polling 중 발생하는 일시적 네트워크 에러 로그 과다 출력
**해결:** `application.yml`에 `org.telegram.telegrambots.updatesreceivers.DefaultBotSession: OFF` 설정 추가 (자동 재연결되므로 불필요)
