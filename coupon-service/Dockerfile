FROM openjdk:21-jdk-slim

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY target/coupon-service-1.0.0-SNAPSHOT.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs

# Expose the port
EXPOSE 8088

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8088/actuator/health || exit 1

# Set JVM options
ENV JAVA_OPTS="-Xmx1g -Xms512m -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
