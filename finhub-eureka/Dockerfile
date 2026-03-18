# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
COPY finhub-eureka/pom.xml finhub-eureka/pom.xml
COPY finhub-gateway/pom.xml finhub-gateway/pom.xml
COPY finhub-user/pom.xml finhub-user/pom.xml
COPY finhub-banking/pom.xml finhub-banking/pom.xml
COPY finhub-investment/pom.xml finhub-investment/pom.xml
COPY finhub-payment/pom.xml finhub-payment/pom.xml
COPY finhub-insurance/pom.xml finhub-insurance/pom.xml
COPY finhub-search/pom.xml finhub-search/pom.xml
COPY finhub-notification/pom.xml finhub-notification/pom.xml

COPY finhub-eureka/src finhub-eureka/src
RUN mvn clean package -pl finhub-eureka -DskipTests -B --no-transfer-progress

# Stage 2: Run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/finhub-eureka/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
