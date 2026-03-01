# ─── Build Stage ───────────────────────────────────
FROM gradle:8.6-jdk17 AS builder
WORKDIR /app
COPY build.gradle settings.gradle* ./
COPY backend ./backend
RUN gradle :backend:build -x test --no-daemon

# ─── Runtime Stage ─────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=builder /app/backend/build/libs/*.jar app.jar
USER spring:spring
# Render dynamically assigns the port and passes it as the PORT environment variable.
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
