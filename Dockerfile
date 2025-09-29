# --- build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 의존성 캐시
COPY pom.xml ./
RUN mvn -q -B -DskipTests dependency:go-offline

# 소스 빌드 + 실행 가능 부트 JAR 재패키징
COPY src ./src
RUN mvn -q -B -DskipTests package spring-boot:repackage

# target에 생긴 JAR/WAR 중 첫 번째를 app.jar로 복사(확실하게 경로 고정)
RUN set -eux; \
    ls -al /app/target; \
    f=$(ls -1 /app/target/*.[jw]ar | head -n 1); \
    echo "Detected artifact: $f"; \
    cp "$f" /app/app.jar; \
    ls -al /app

# --- run ---
FROM eclipse-temurin:21-jre
ENV TZ=Asia/Seoul
WORKDIR /app
COPY --from=build /app/app.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-lc","java $JAVA_OPTS -jar /app/app.jar"]
