# 1️⃣ Java 8 기반 Maven 이미지로 빌드 단계
FROM maven:3.8.1-openjdk-8 AS builder

WORKDIR /app

# 프로젝트 파일 복사
COPY . .

# Maven 빌드 (테스트 생략)
RUN mvn clean package -Dmaven.test.skip=true

# 2️⃣ 경량 런타임 단계 (빌드 산출물만 복사)
FROM openjdk:8-jdk-alpine

WORKDIR /app

# 빌드된 jar만 복사
COPY --from=builder /app/target/*.jar app.jar

# 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]
