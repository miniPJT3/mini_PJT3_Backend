# 1단계: 빌드 (JDK 사용)
FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

# 라이브러리 다운로드 단계 캐싱 (Maven은 pom.xml을 먼저 복사)
COPY pom.xml .
# 종로성 라이브러리 미리 다운로드 (빌드 속도 향상)
RUN mvn dependency:go-offline -B

# 소스 코드 복사 및 빌드
COPY src src
RUN mvn package -DskipTests

# 2단계: 실행 (JRE + Alpine Linux 사용)
# - 용량이 작고 공격 표면(Attack Surface)이 좁아 보안에 유리합니다.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 보안: Root 권한이 아닌 일반 유저로 실행
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
# AWS Secrets Manager에서 값을 가져오기 위한 환경 변수 공간 확보
ENTRYPOINT ["java", "-Xmx512M", "-jar", "app.jar"]