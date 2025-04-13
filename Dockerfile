
LABEL authors="Sarah Henia"
# Build stage
FROM maven:3.8.6-openjdk-8 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:8-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/gestion-station-ski-1.0.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
