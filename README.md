# 🎬 Cinema Bot

영화관 1+1 관람권 자동 알림 봇

## 📝 프로젝트 소개

Cinema Bot은 국내 주요 영화관(롯데시네마, CGV, 메가박스)의 1+1 관람권 정보를 실시간으로 모니터링하고, 새로운 할인 티켓이 등록되면 텔레그램 채널로 자동으로 알림을 보내주는 Spring Boot 기반 애플리케이션입니다.

### 주요 기능

- 🎫 **실시간 티켓 모니터링**: 롯데시네마, CGV, 메가박스의 1+1 관람권 자동 크롤링
- 📢 **텔레그램 알림**: 새로운 할인 티켓 발견 시 텔레그램 채널로 즉시 알림

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

## ⚙️ 설정

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

## 📄 라이선스

이 프로젝트는 개인 프로젝트입니다.
