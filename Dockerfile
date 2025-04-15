# Étape 1: Construction de l'application avec Maven
FROM maven:3.8.8-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml ./
RUN mvn dependency:go-offline --no-transfer-progress

COPY src ./src

RUN mvn clean package -DskipTests --no-transfer-progress

# Étape 2: Image finale avec le jar compilé
FROM openjdk:17-jdk-slim

WORKDIR /app

# 👇 On récupère le jar depuis l'étape "builder"
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
