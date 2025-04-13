
FROM maven:3.8.6-openjdk-8 AS builder
LABEL authors="Sarah Henia"
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:8-jdk-alpine
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]