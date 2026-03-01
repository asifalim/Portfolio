# ─── Build Stage ───────────────────────────────────
FROM gradle:8.6-jdk21 AS builder
WORKDIR /app
COPY build.gradle settings.gradle* ./
COPY src ./src
RUN gradle build -x test --no-daemon

# ─── Runtime Stage ─────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /app/build/libs/*[!plain].jar app.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT}"]
