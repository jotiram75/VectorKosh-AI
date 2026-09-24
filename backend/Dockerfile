# Stage 1: Build stage with Maven and OpenJDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven POM and source code
COPY pom.xml .
COPY src src

# Package application
RUN mvn clean package -DskipTests

# Stage 2: Minimal Java Runtime Environment for deployment
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built JAR from stage 1
COPY --from=build /app/target/vectordb-backend-1.0.0.jar app.jar

# Render exposes PORT dynamically, default to 8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
