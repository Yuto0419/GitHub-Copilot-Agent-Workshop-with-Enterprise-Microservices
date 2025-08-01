FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Azure SkiShop Team"
LABEL version="1.0.0"
LABEL description="Azure SkiShop AI Support Service"

# Install curl for health checks
RUN apk add --no-cache curl

# Set working directory
WORKDIR /app

# Copy application JAR file
COPY target/ai-support-service-*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs

# Expose the port
EXPOSE 8087

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8087/actuator/health || exit 1

# Set JVM options
ENV JAVA_OPTS="-Xmx1g -Xms512m -Djava.security.egd=file:/dev/./urandom"

# Set Spring profile to production
ENV SPRING_PROFILES_ACTIVE=production

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
