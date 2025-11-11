# 🎬 Cinema Bot

영화관 1+1 관람권 자동 알림 봇

## 📝 프로젝트 소개

Cinema Bot은 국내 주요 영화관(롯데시네마, CGV, 메가박스)의 1+1 관람권 정보를 실시간으로 모니터링하고, 새로운 할인 티켓이 등록되면 텔레그램 채널로 자동으로 알림을 보내주는 Spring Boot 기반 애플리케이션입니다.

### 주요 기능

- 🎫 **실시간 티켓 모니터링**: 롯데시네마, CGV, 메가박스의 1+1 관람권 자동 크롤링
- 📢 **텔레그램 알림**: 새로운 할인 티켓 발견 시 텔레그램 채널로 즉시 알림
- ⏰ **스케줄러**: 평일(월~금) 08:00~20:59 사이 2분마다 자동 실행
- 💾 **데이터 저장**: Google Sheets API를 통한 티켓 정보 자동 저장
- 🤖 **텔레그램 봇**: `/list`, `/ping` 명령어를 통한 현재 1+1 티켓 조회

## 📂 프로젝트 구조

```
cinema-bot/
├── src/
│   ├── main/
│   │   ├── java/com/bot/cinemabot/
│   │   │   ├── CinemaBotApplication.java      # 메인 애플리케이션
│   │   │   ├── scheduler/
│   │   │   │   └── CinemaScheduler.java        # 스케줄러 (2분마다 실행)
│   │   │   ├── service/
│   │   │   │   ├── LotteCinemaService.java     # 롯데시네마 크롤링
│   │   │   │   ├── CgvService.java             # CGV 크롤링
│   │   │   │   └── MegaboxService.java         # 메가박스 크롤링
│   │   │   ├── utils/
│   │   │   │   ├── Telegram.java               # 텔레그램 메시지 전송
│   │   │   │   ├── PingPong.java               # 텔레그램 봇 명령어 처리
│   │   │   │   └── Utils.java                  # 공통 유틸리티
│   │   │   ├── repo/
│   │   │   │   └── GoogleSpreadSheetsRepo.java # Google Sheets 저장
│   │   │   ├── model/                          # 데이터 모델
│   │   │   └── websocket/                      # WebSocket 설정 (옵션)
│   │   └── resources/
│   │       └── application.yml                 # 설정 파일
│   └── test/                                   # 테스트 코드
├── pom.xml                                     # Maven 의존성 관리
├── Dockerfile                                  # Docker 이미지 빌드
├── run.sh                                      # 실행 스크립트
├── stop.sh                                     # 종료 스크립트
└── update.sh                                   # 업데이트 스크립트
```

## 🛠 기술 스택

- **Java**: 17
- **Spring Boot**: 3.2.1
- **Build Tool**: Maven 3.9.x
- **Libraries**:
  - jsoup 1.17.2 (웹 크롤링)
  - telegram-spring-boot-starter 0.22 (텔레그램 봇 API)
  - Lombok 1.18.34 (코드 간소화)
  - Gson (JSON 파싱)

## 📋 요구사항

- Java 17 이상
- Maven 3.6 이상
- 텔레그램 봇 토큰 ([@BotFather](https://t.me/botfather)에서 발급)
- 텔레그램 채널 ID
- Google Sheets Script URL (옵션 - 티켓 정보 저장용)

## 🚀 설치 및 실행

### 1. 프로젝트 클론

```bash
git clone https://github.com/your-repo/cinema-bot.git
cd cinema-bot
```

### 2. 설정 파일 수정

`src/main/resources/application.yml` 파일에서 다음 정보를 설정합니다:

```yaml
spring:
  bot:
    telegram:
      token: YOUR_TELEGRAM_BOT_TOKEN
      chatId: YOUR_CHAT_ID
      channel: "@your_channel"
    sheets:
      google:
        url: YOUR_GOOGLE_SHEETS_SCRIPT_URL
```

### 3. 빌드

```bash
# Java 17 환경 설정 (macOS Homebrew 기준)
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"

# Maven 빌드
mvn clean package -Dmaven.test.skip=true
```

### 4. 실행

#### 로컬 실행 (개발 환경)
```bash
java -jar target/cinema-bot-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

#### 프로덕션 실행
```bash
# 환경변수로 토큰 전달
export CINEMA_BOT_TOKEN=your_bot_token
java -jar target/cinema-bot-0.0.1-SNAPSHOT.jar --spring.profiles.active=production --spring.bot.telegram.token=$CINEMA_BOT_TOKEN
```

#### 백그라운드 실행
```bash
nohup java -jar target/cinema-bot-0.0.1-SNAPSHOT.jar --spring.profiles.active=production &
```

### 5. Docker 실행

```bash
# Docker 이미지 빌드
docker build -t cinema-bot .

# Docker 컨테이너 실행
docker run -d \
  -e CINEMA_BOT_TOKEN=your_bot_token \
  --name cinema-bot \
  cinema-bot
```

## 📱 텔레그램 봇 사용법

### 알림 받기
1. 텔레그램 앱 설치
2. `@cinema1p1` 채널 구독
3. 새로운 1+1 티켓이 등록되면 자동으로 알림을 받습니다

### 봇 명령어
- `/list` - 현재 판매 중인 모든 1+1 관람권 목록 조회
- `/ping` - 봇 상태 확인 (pong 응답)

## ⚙️ 설정

### 스케줄러 설정

`CinemaScheduler.java`에서 크롤링 주기를 변경할 수 있습니다:

```java
// 월~금, 08:00~20:59, 2분마다 실행
@Scheduled(cron = "0 0/2 08-20 ? * MON-FRI")
public void lotteCinema() {
    lotteCinemaService.aJob();
}
```

### 프로파일 설정

- `local`: 개발 환경 (livereload 활성화)
- `production`: 운영 환경

## 🔧 스크립트

### run.sh
봇을 시작하는 스크립트입니다.
```bash
sh run.sh
```

### stop.sh
실행 중인 봇을 종료합니다.
```bash
sh stop.sh
```

### update.sh
코드 변경 후 재배포 시 사용합니다.

## 🤝 CI/CD

GitHub Actions를 통한 자동 배포가 설정되어 있습니다.

- **Workflow**: `.github/workflows/main.yml`
- **트리거**: master 브랜치에 push 또는 PR 시 자동 실행
- **배포**: SSH를 통해 원격 서버에 자동 배포

필요한 GitHub Secrets:
- `HOST`: 배포 서버 호스트
- `USERNAME`: SSH 사용자명
- `PASSWORD`: SSH 비밀번호
- `PORT`: SSH 포트

## 📊 모니터링 대상

### 롯데시네마
- 1+1 관람권
- 얼리버드 티켓

### CGV
- 1+1 관람권
- 원플러스원 티켓

### 메가박스
- 1+1 관람권

## 🔍 동작 원리

1. **스케줄러 실행**: 평일 오전 8시~오후 8시 59분, 2분마다 각 영화관 사이트를 크롤링
2. **데이터 수집**: jsoup을 이용해 각 영화관의 티켓 정보를 파싱
3. **변경 감지**: 이전 캐시와 비교하여 새로운 티켓 추가 감지
4. **알림 전송**: 새로운 티켓 발견 시 텔레그램 API를 통해 채널에 메시지 전송
5. **데이터 저장**: Google Sheets에 티켓 정보 자동 저장

## 📝 로그

애플리케이션 로그는 표준 출력으로 출력됩니다. `nohup`을 사용한 경우 `nohup.out` 파일에 저장됩니다.

## 🐛 트러블슈팅

### Java 버전 오류
```bash
# Java 17 설치 (macOS)
brew install openjdk@17

# JAVA_HOME 설정
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
export PATH="$JAVA_HOME/bin:$PATH"
```

### Maven 빌드 오류
```bash
# Maven 설치
brew install maven

# 의존성 재다운로드
mvn clean install -U
```

### 텔레그램 봇이 응답하지 않을 때
1. 봇 토큰이 올바른지 확인
2. 봇이 채널 관리자 권한을 가지고 있는지 확인
3. 채널 ID가 올바른지 확인 (@username 형식)

## 📄 라이선스

이 프로젝트는 개인 프로젝트입니다.

## 👥 기여

버그 리포트나 기능 제안은 Issues를 통해 제출해주세요.

## 📞 문의

프로젝트 관련 문의사항이 있으시면 Issues를 통해 연락해주세요.

---

**Note**: 본 프로젝트는 개인적인 용도로 개발되었으며, 각 영화관의 이용약관을 준수하여 사용해주시기 바랍니다.
