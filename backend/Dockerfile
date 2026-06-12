# --- Stage 1 : build du jar avec Gradle wrapper ---
# Note: build.gradle pin un toolchain Java 25 (cf. java.toolchain.languageVersion),
# malgre la stack documentee en Java 21. Garder l'image alignee sur build.gradle.
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

COPY src ./src

RUN ./gradlew bootJar -x test --no-daemon

# --- Stage 2 : image d'execution ---
FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
