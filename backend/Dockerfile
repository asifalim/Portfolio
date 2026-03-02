# ─── Build Stage ───────────────────────────────────
FROM gradle:8.6-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle* ./
COPY src ./src
RUN gradle bootJar -x test --no-daemon

# ─── Runtime Stage ─────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /app/build/libs/*[0-9].jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
