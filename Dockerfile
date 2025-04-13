
LABEL authors="Sarah Henia"
FROM openjdk:8-jdk-alpine

WORKDIR /app

COPY target/gestion-station-ski-1.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]