# ---- build (uses BuildKit secret if provided) ----
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app

# Copy Maven project files
COPY pom.xml .
COPY src ./src

# Use BuildKit secret mount (if present) and instruct mvn to use it with -s.
# DO NOT cp the secret into the image.
# If no secret is provided, we fall back to a plain mvn (will fail bcs private repo is needed).
RUN --mount=type=secret,id=m2 \
    sh -c 'if [ -f /run/secrets/m2 ]; then \
              echo "BuildKit secret found — using /run/secrets/m2"; \
              mvn -s /run/secrets/m2 -B package -DskipTests; \
           else \
              echo "No BuildKit secret present — running mvn without custom settings"; \
              mvn -B package -DskipTests; \
           fi'

# ---- final runtime image ----
FROM eclipse-temurin:25-jre AS final
WORKDIR /app

# Copy only the built jar (no secrets)
COPY --from=builder /app/target/*.jar app.jar

ENV MODEL_HOST="http://localhost:8081"
ENV APP_PORT=8080
EXPOSE ${APP_PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]
