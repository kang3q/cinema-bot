# 1️⃣ Java 17 기반 Maven 이미지로 빌드 단계
FROM maven:3.9.5-eclipse-temurin-17 AS builder

WORKDIR /app

# 프로젝트 파일 복사
COPY . .

# Maven 빌드 (테스트 생략)
RUN mvn clean package -Dmaven.test.skip=true

# 2️⃣ 경량 런타임 단계 (빌드 산출물만 복사)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 빌드된 jar만 복사
COPY --from=builder /app/target/*.jar app.jar

# 환경변수 설정 (필요시 docker run 시 오버라이드)
ENV SPRING_PROFILES_ACTIVE=production

# 실행 명령 (Java 17 모듈 시스템 호환을 위한 JVM 옵션 포함)
ENTRYPOINT ["java", "--add-opens", "java.base/java.lang=ALL-UNNAMED", "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED", "-jar", "app.jar"]
