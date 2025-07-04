# Multi-stage Dockerfile for Payment Cart Service

# Stage 1: Build stage
FROM maven:3.9.6-openjdk-21-slim AS build

WORKDIR /app

# Copy Maven files first for better layer caching
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime stage
FROM openjdk:21-jdk-slim

# Create app user for security
RUN addgroup --system app && adduser --system --group app

# Set working directory
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy the built JAR from build stage
COPY --from=build /app/target/payment-cart-service-*.jar app.jar

# Create logs directory
RUN mkdir -p logs && chown -R app:app /app

# Switch to app user
USER app

# Expose the port
EXPOSE 8084

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
