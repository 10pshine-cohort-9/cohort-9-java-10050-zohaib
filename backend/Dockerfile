# ============================================================
# Backend Dockerfile — Multi-stage build
# Uses Debian-based images (glibc) for WSL2/Docker Desktop compat
# Stage 1: Build the JAR with Maven
# Stage 2: Run with minimal JRE
# ============================================================

# ---------- Stage 1: Build ----------
FROM --platform=linux/amd64 eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Install Maven
RUN apt-get update -q && apt-get install -y -q maven && rm -rf /var/lib/apt/lists/*

# Copy pom.xml first so the dependency layer is cached separately
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build the fat JAR (skip tests — they run in CI)
COPY src ./src
RUN mvn clean package -q -DskipTests -DskipJacoco=true

# ---------- Stage 2: Runtime ----------
FROM --platform=linux/amd64 eclipse-temurin:21-jre-jammy

WORKDIR /app

# Non-root user for security
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# Copy the fat JAR
COPY --from=build /app/target/*.jar app.jar

# Create logs directory with correct ownership
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
