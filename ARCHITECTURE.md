# 🏗 Cinema Bot 아키텍처 문서

## 📐 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                     Cinema Bot System                        │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌────────────────┐                                          │
│  │   Scheduler    │                                          │
│  │ (CronJob 2분)  │                                          │
│  └────────┬───────┘                                          │
│           │                                                   │
│           ├──────────┬──────────┬──────────┐                │
│           ▼          ▼          ▼          ▼                │
│  ┌─────────────┐ ┌──────┐ ┌─────────┐ ┌─────────┐         │
│  │    Lotte    │ │ CGV  │ │ Megabox │ │ Other   │         │
│  │  Service    │ │Service│ │ Service │ │Services │         │
│  └──────┬──────┘ └───┬──┘ └────┬────┘ └─────────┘         │
│         │            │         │                             │
│         └────────────┴─────────┘                             │
│                      │                                        │
│                      ▼                                        │
│         ┌────────────────────────┐                          │
│         │   Change Detection     │                          │
│         │   (Cache Comparison)   │                          │
│         └────────────┬───────────┘                          │
│                      │                                        │
│                      ▼                                        │
│         ┌────────────────────────┐                          │
│         │  Telegram Notification │                          │
│         │  & Google Sheets Save  │                          │
│         └────────────────────────┘                          │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## 🔄 데이터 흐름

### 1. 스케줄링 및 크롤링
```
CinemaScheduler
    │
    ├─> @Scheduled(cron) → 평일 08:00-20:59, 2분마다 실행
    │
    ├─> LotteCinemaService.aJob()  (0초)
    ├─> CgvService.aJob()          (20초)
    └─> MegaboxService.aJob()      (40초)
```

### 2. 각 서비스의 데이터 처리 흐름
```
Service Layer
    │
    ├─> 1. 영화관 API/웹사이트 크롤링
    │       ├─ HTTP 요청 (RestTemplate/Jsoup)
    │       └─ JSON/HTML 파싱
    │
    ├─> 2. 1+1 티켓 필터링
    │       ├─ 키워드 검색 ("1+1", "원플러스원", "얼리버드")
    │       └─ 데이터 정제
    │
    ├─> 3. 캐시 비교 (변경 감지)
    │       ├─ 이전 캐시와 현재 데이터 비교
    │       ├─ Collections.synchronizedList 사용
    │       └─ Stream API를 이용한 차이 검출
    │
    ├─> 4. 신규 티켓 발견 시
    │       ├─ 상세 정보 조회 (필요 시)
    │       ├─ MessageFormat 객체 생성
    │       └─ 알림 전송
    │
    └─> 5. 캐시 업데이트
            └─ 현재 데이터로 캐시 갱신
```

## 📦 계층 구조 (Layered Architecture)

```
┌─────────────────────────────────────────┐
│        Presentation Layer               │
│  - WebSocketController (Optional)       │
│  - PingPong (Telegram Bot Commands)    │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│          Service Layer                  │
│  - LotteCinemaService                   │
│  - CgvService                           │
│  - MegaboxService                       │
│  - Business Logic & Cache Management    │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│       Infrastructure Layer              │
│  - Telegram (Message Sender)            │
│  - GoogleSpreadSheetsRepo               │
│  - Utils (RestTemplate, Gson)           │
│  - External APIs (Cinema, Telegram)     │
└─────────────────────────────────────────┘
```

## 🔧 주요 컴포넌트 상세

### 1. CinemaScheduler
**역할**: 정기적인 크롤링 작업 스케줄링

**특징**:
- Spring `@Scheduled` 사용
- Cron 표현식으로 실행 주기 제어
- 각 영화관 서비스를 20초 간격으로 순차 실행 (API 부하 분산)

**코드 위치**: `scheduler/CinemaScheduler.java`

```java
@Scheduled(cron = "0 0/2 08-20 ? * MON-FRI")  // 롯데 - 0초
@Scheduled(cron = "20 0/2 08-20 ? * MON-FRI") // CGV - 20초
@Scheduled(cron = "40 0/2 08-20 ? * MON-FRI") // 메가박스 - 40초
```

### 2. Service Layer (LotteCinemaService, CgvService, MegaboxService)

**공통 구조**:
```java
class XxxService {
    // 의존성
    @Autowired Telegram telegram;

    // 설정값
    @Value("${spring.bot.cinema.xxx.api}") String apiUrl;

    // 캐시
    private List<TicketItem> cache1p1Tickets;

    // 초기화
    @PostConstruct
    void init() { /* 초기 데이터 로드 */ }

    // 메인 작업
    public void aJob() {
        1. API 호출 및 데이터 파싱
        2. 1+1 티켓 필터링
        3. 변경 감지
        4. 알림 전송
        5. 캐시 업데이트
    }
}
```

**각 서비스별 특징**:

#### LotteCinemaService
- **API 방식**: POST 요청 (application/x-www-form-urlencoded)
- **응답 형식**: JSON
- **필터 조건**:
  - `DisplayLargeClassificationCode == 20` (관람권)
  - `CombiItmDivCd == 40` (콤보상품)
  - 제목에 "1+1" 또는 "얼리버드" 포함

#### CgvService
- **API 방식**: HTML 페이지 크롤링 (Jsoup)
- **데이터 추출**: JavaScript 변수 `jsonData` 파싱
- **필터 조건**: 제목에 "1+1" 또는 "원플러스원" 포함
- **추가 작업**: 상세 페이지에서 사용기간 정보 추가 크롤링

#### MegaboxService
- **API 방식**: POST 요청 (JSON)
- **응답 형식**: JSON
- **필터 조건**: 제목에 "1+1" 포함 (공백 제거 후 검색)
- **추가 작업**: 상세 페이지에서 가격, 날짜 정보 추가 크롤링

### 3. Telegram Utils

**Telegram.java** - 메시지 전송
```java
// 주요 메서드
- init(): 봇 시작 알림
- destroy(): 봇 종료 알림
- sendMessageToBot(): 관리자에게 메시지
- sendMessageToChannel(): 채널에 공개 알림
- sendHTMLToChannel(): HTML 포맷 메시지
```

**PingPong.java** - 봇 명령어 처리
```java
// 상속: TelegramLongPollingBot
// 지원 명령어:
- /list: 모든 1+1 티켓 조회
- /ping: 봇 상태 확인
```

### 4. 데이터 모델

#### MessageFormat
```java
class MessageFormat {
    String platform;      // "롯데시네마", "CGV", "메가박스"
    String title;         // 티켓 제목
    String dateRange;     // 사용 기간
    String price;         // 가격
    String count1p1;      // 현재 1+1 티켓 수
    String countAll;      // 전체 티켓 수
    String buyUrl;        // 구매 링크
    boolean disableWebPagePreview;
    String key;           // 고유 식별자
}
```

#### Cinema-specific Models
- **Lotte**: `LotteCinemaResponse`, `ProductItem`, `MenuItem`
- **CGV**: `CgvItem`
- **Megabox**: `MegaboxResponse`, `MegaboxTicket`, `TicketItem`

## 🔐 보안 및 설정

### 프로파일 관리
```yaml
# local 프로파일
spring:
  config:
    activate:
      on-profile: local
  devtools:
    livereload:
      enabled: true

# production 프로파일
spring:
  config:
    activate:
      on-profile: production
```

### 민감 정보 관리
- 텔레그램 봇 토큰: 환경변수 또는 application.yml
- API 엔드포인트: application.yml에 설정
- Google Sheets URL: application.yml에 설정

## 🎯 변경 감지 알고리즘

### 기본 원리
```java
// Stream API를 이용한 변경 감지
boolean isChanged = !newTickets.stream()
    .allMatch(newTicket ->
        cache.stream().anyMatch(oldTicket ->
            oldTicket.getId().equals(newTicket.getId())
        )
    );
```

### 각 영화관별 비교 키
- **Lotte**: `DisplayItemName` (상품명)
- **CGV**: `Idx` (고유 인덱스)
- **Megabox**: `Name` (상품명)

## 🚀 성능 최적화

### 1. 캐싱 전략
- `Collections.synchronizedList` 사용으로 Thread-safe 보장
- 메모리 기반 캐시 (애플리케이션 재시작 시 초기화)

### 2. 스케줄링 분산
- 각 영화관 크롤링을 20초씩 분산 실행
- API 서버 부하 분산

### 3. 비동기 처리
- Google Sheets 저장: `@Async` 사용
- 메인 로직과 분리하여 성능 영향 최소화

## 📊 모니터링 포인트

### 로그 레벨
- **INFO**: 일반 작업 로그 (알림 전송, 티켓 발견)
- **DEBUG**: 상세 디버깅 정보 (API 응답, 파싱 결과)
- **ERROR**: 오류 발생 시 (API 실패, 파싱 오류)

### 주요 메트릭
- 호출 횟수 (각 서비스별 `callCount`)
- 캐시된 티켓 수
- 1+1 티켓 수
- 변경 감지 여부

## 🔮 확장 가능성

### 1. 새로운 영화관 추가
```java
// 1. Service 클래스 생성
@Service
public class NewCinemaService {
    // 기본 구조 동일
}

// 2. Scheduler에 추가
@Scheduled(cron = "...")
public void newCinema() {
    newCinemaService.aJob();
}
```

### 2. 알림 채널 추가
- Slack, Discord 등 다른 메신저 플랫폼 연동 가능
- Interface 기반 Notification 추상화 고려

### 3. 데이터베이스 연동
- 현재: 메모리 캐시 + Google Sheets
- 개선: PostgreSQL/MySQL 등 RDBMS 연동
- 이력 관리 및 통계 기능 추가

## 🐛 알려진 제약사항

1. **메모리 캐시**: 애플리케이션 재시작 시 캐시 초기화
2. **단일 인스턴스**: 현재 다중 인스턴스 배포 미지원
3. **크롤링 안정성**: 웹사이트 구조 변경 시 수동 수정 필요
4. **에러 핸들링**: 일부 예외 처리 개선 필요

## 📚 기술 스택 상세

### Spring Boot 3.2.1
- **Spring Core**: IoC/DI 컨테이너
- **Spring Scheduling**: Cron 기반 스케줄링
- **Spring Web**: REST API 호출
- **Spring WebSocket**: 실시간 통신 (옵션)
- **Spring Boot DevTools**: 개발 편의성

### 주요 라이브러리
- **Jsoup**: HTML 파싱 및 크롤링
- **Telegram Bot API**: 텔레그램 봇 기능
- **Gson**: JSON 직렬화/역직렬화
- **Lombok**: 보일러플레이트 코드 감소

## 🔄 배포 전략

### Docker 기반 배포
1. Multi-stage build로 이미지 크기 최소화
2. Eclipse Temurin JRE Alpine 사용
3. 환경변수를 통한 설정 주입

### CI/CD Pipeline
1. GitHub Actions 트리거 (master push)
2. SSH를 통한 원격 서버 배포
3. 무중단 배포 (run.sh 스크립트)

---

**문서 버전**: 1.0
**최종 업데이트**: 2025-11-11
**작성자**: Cinema Bot Team
