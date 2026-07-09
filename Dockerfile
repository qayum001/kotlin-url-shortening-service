# syntax=docker/dockerfile:1

# ---- build stage ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy the wrapper + build scripts first so dependency resolution can be cached
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

# Build the executable (Spring Boot) jar, skipping tests
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ---- run stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Only the fat jar (bootJar), never the *-plain.jar
COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
