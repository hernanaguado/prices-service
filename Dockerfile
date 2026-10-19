# syntax=docker/dockerfile:1

# Build stage: JDK 25 plus the project's Maven Wrapper, so the image uses the same Maven version as local builds.
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn
COPY prices-domain prices-domain
COPY prices-application prices-application
COPY prices-infrastructure prices-infrastructure
COPY prices-boot prices-boot

# Tests are not run here because ./mvnw verify already runs them before an image is built. The cache mount
# keeps the Maven repository between builds, so dependencies are only downloaded once.
RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && ./mvnw -B -q package -DskipTests

# Runtime stage: JRE only, on Alpine, to keep the image small.
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app

COPY --from=build /workspace/prices-boot/target/prices-boot-*.jar app.jar

USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
