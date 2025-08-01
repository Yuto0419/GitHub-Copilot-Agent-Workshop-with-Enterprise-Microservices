# Multi-stage Dockerfile for Payment Cart Service

# Stage 1: Build stage
FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven files first for better layer caching
COPY pom.xml ./

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Azure SkiShop Team"
LABEL version="1.0.0"
LABEL description="Azure SkiShop Inventory Management Service"

# Install curl for health checks
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/payment-cart-service-*.jar app.jar

# Create logs directory
RUN mkdir -p logs 

# Expose the port
EXPOSE 8084

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
