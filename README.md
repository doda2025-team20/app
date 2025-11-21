# SMS Checker / Frontend

The frontend provides a browser-based interface for sending classification requests to the backend model service.

The application is implemented with Spring Boot and exposes a single REST endpoint plus a simple UI.
It requires Java 25+ (tested with 25.0.1).
All classification requests are forwarded to the backend. You must set the MODEL_HOST environment variable so the frontend knows where that backend is running.

Typical local development start:
1. Update `.m2/settings.xml` with your GitHub username and [PAT](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens)
2. Install maven dependencies
    ```bash
    mvn -s ./.m2/settings.xml clean install
    ```
3. Start app
    ```bash
    MODEL_HOST="http://localhost:8081" mvn spring-boot:run
    ```

The application listens on port 8080. After startup, open:

```bash
http://localhost:8080/sms
```

## Running the frontend via Docker

The container image for the app repository is published to GitHub Container Registry (GHCR).

Pull the image
```bash
docker pull ghcr.io/doda2025-team20/app:latest
```

If the repository is private, authenticate first:

```bash
echo "<GHCR_PAT>" | docker login ghcr.io -u <github-username> --password-stdin
```

Run the container
```bash
docker run \
  -p 8080:8080 \
  -e MODEL_HOST="http://model-service:8081" \
  ghcr.io/doda2025-team20/app:latest
```

The frontend will start on port 8080 inside the container, mapped to port 8080 on your host.

## Runtime configuration

The runtime container provides default values:

```bash
ENV MODEL_HOST="http://localhost:8081"
ENV APP_PORT=8080
```

You can override these when running the container:

```bash
docker run -e MODEL_HOST="http://other-host:9000" ...
```

## Notes

The Dockerfile uses a two-stage build:

1. A Maven builder image (maven:3.9-eclipse-temurin-25) to compile and package the application.
2. A lightweight runtime image (eclipse-temurin:25-jre) containing only the built JAR.

This keeps the final image small and avoids shipping build tools or secrets.
Only the final JAR from the build stage is copied into the runtime image.
No cached Maven data, no build artifacts, and no secrets are included, supporting secure and predictable deployments.
