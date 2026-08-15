FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN mvn -B package -DskipTests
FROM eclipse-temurin:17-jre
WORKDIR /app
# 256 MB 展示容器使用低額外負擔的 GC，並限制 metaspace、code cache 與 thread stack。
# 正式環境可透過 runtime JAVA_TOOL_OPTIONS 覆蓋這組展示基線。
ENV JAVA_TOOL_OPTIONS="-XX:+UseSerialGC -XX:InitialRAMPercentage=15.0 -XX:MaxRAMPercentage=40.0 -XX:MaxMetaspaceSize=80m -XX:ReservedCodeCacheSize=32m -Xss512k"
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && useradd --system --uid 10001 --no-create-home appuser \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/target/create-api-*.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
