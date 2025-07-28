# 멀티스테이지 빌드를 위한 Dockerfile
# Stage 1: 빌드 스테이지
FROM gradle:8.7-jdk21 AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 캐시를 위한 설정
COPY gradle gradle
COPY gradlew gradlew.bat
COPY build.gradle settings.gradle gradle.properties* ./

# 의존성 다운로드 (캐시 레이어 최적화)
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN gradle build -x test --no-daemon

# Stage 2: 실행 스테이지
FROM eclipse-temurin:21-jre-jammy

# 메타데이터 설정
LABEL maintainer="Boardly Team"
LABEL description="Boardly Backend Application"
LABEL version="0.0.1-SNAPSHOT"

# 사용자 생성 (보안을 위해 root가 아닌 사용자 사용)
RUN groupadd -r boardly && useradd -r -g boardly boardly

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 소유권 변경
RUN chown -R boardly:boardly /app

# 사용자 전환
USER boardly

# 포트 노출
EXPOSE 8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM 옵션 설정
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=docker -jar app.jar"] 