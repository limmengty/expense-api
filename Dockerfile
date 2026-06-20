# ============================================================
# expense-api / Dockerfile
# Multi-stage: Maven build (JDK 21) → Eclipse Temurin JRE runtime
# ============================================================

# ---- Build Stage ----
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper first — separate layer so deps cache unless pom changes
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build fat jar
COPY src ./src
RUN ./mvnw package -DskipTests -B && \
    mv target/*.jar app.jar

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Non-root user (UID 1000 matches K8s securityContext.runAsUser)
RUN addgroup -g 1000 -S appgroup && \
    adduser -u 1000 -S appuser -G appgroup

COPY --from=builder --chown=appuser:appgroup /app/app.jar app.jar

USER appuser

EXPOSE 8080

# Start-period 60s allows Liquibase migrations + Spring context init
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
