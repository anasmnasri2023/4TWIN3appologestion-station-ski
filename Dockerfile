# Stage 1: Build the application
FROM maven:3.8.6-openjdk-8 AS builder
LABEL authors="Sarah Henia"
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime image
FROM openjdk:8-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
