# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Kopiere zuerst die POM und lade Abhängigkeiten herunter (für Caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Kopiere den Quellcode und baue die Anwendung (inklusive Tests)
COPY src ./src
RUN mvn clean package

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Kopiere die gebaute JAR aus der Build-Stage
COPY --from=build /app/target/packops-backend-0.0.1-SNAPSHOT.jar app.jar

# Exponiere den Port
EXPOSE 8080

# Starte die Anwendung
ENTRYPOINT ["java", "-jar", "app.jar"]
