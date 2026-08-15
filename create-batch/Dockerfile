FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN mvn -B package -DskipTests
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --uid 10001 --no-create-home appuser
COPY --from=build /workspace/target/create-batch-*.jar app.jar
USER 10001
ENTRYPOINT ["java","-jar","/app/app.jar"]
