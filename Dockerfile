# 1단계: 빌드 스테이지 (JDK 설치 버전)
# eclipse-temurin은 ARM64를 공식 지원하는 안정적인 베이스 이미지입니다.
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# 빌드 속도 향상을 위해 Gradle 래퍼와 의존성 파일을 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 실행 권한 부여 및 의존성 다운로드 (캐싱 활용)
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# 전체 소스 코드 복사 및 빌드
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# 2단계: 실행 스테이지 (JRE 경량 버전)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일만 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Spring Boot 포트 개방
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]