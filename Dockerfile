# -------- BUILD STAGE --------
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /app

# Copy Maven project files
COPY pom.xml .
COPY src ./src

# Build the Spring Boot jar
RUN mvn -B package -DskipTests

# -------- RUN STAGE --------
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Environment variable for backend location
ENV MODEL_HOST="http://localhost:8081"
ENV APP_PORT=8080

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
