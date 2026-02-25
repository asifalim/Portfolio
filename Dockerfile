# ─── Build Stage ───────────────────────────────────
FROM gradle:8.6-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle* ./
COPY src ./src
RUN gradle build -x test --no-daemon

# ─── Runtime Stage ─────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /app/build/libs/*.jar app.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
