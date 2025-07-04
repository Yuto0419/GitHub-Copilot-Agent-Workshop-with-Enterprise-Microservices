FROM openjdk:21-jre-slim

LABEL maintainer="Azure SkiShop Team"
LABEL version="1.0.0"
LABEL description="Azure SkiShop Frontend Service"

# Set working directory
WORKDIR /app

# Copy application JAR file
COPY target/frontend-service-*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

# JVM options for production environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

# Set Spring profile to production
ENV SPRING_PROFILES_ACTIVE=production
