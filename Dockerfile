# Build stage
FROM maven:3.8.6-eclipse-temurin-8 AS build
LABEL authors="Sarah Henia"
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src /app/src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:8-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8089
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --quiet --tries=1 --spider http://localhost:8089/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]