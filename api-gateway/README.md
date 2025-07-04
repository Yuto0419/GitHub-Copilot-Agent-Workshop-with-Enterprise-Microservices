# API Gateway Service

Centralized API Gateway for Ski Shop E-commerce Platform

## Main Features Provided

### Feature List

- **Request Routing**: Intelligent routing to - **Request Routing**: Intelligent routing to backend microservices based on URL patterns
- **Authentication & Authorization**: JWT-based authentication with role-based access control
- **Rate Limiting**: Redis-based rate limiting to prevent abuse and ensure fair usage
- **Circuit Breaking**: Resilience4j-based circuit breaker pattern to prevent cascading failures
- **Request/Response Transformation**: Header manipulation, path rewriting, and content filtering
- **Monitoring & Observability**: Metrics collection, distributed tracing, and health checks
- **API Documentation**: Centralized Swagger UI aggregating all service documentation
- **CORS Support**: Configurable cross-origin resource sharing for web applications
- **Event Streaming**: Kafka integration for publishing gateway events and metrics
- **Load Balancing**: Distributes traffic across multiple service instances

## Service Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| GET | `/actuator/health` | Health check endpoint | None |
| GET | `/actuator/info` | Service information | None |
| GET | `/actuator/metrics` | Prometheus metrics | Admin only |
| GET | `/actuator/gateway/routes` | Route configuration | Admin only |
| GET | `/actuator/gateway/filters` | Filter configuration | Admin only |
| GET | `/swagger-ui.html` | API documentation | None |
| GET | `/api-docs` | OpenAPI specification | None |

### Routed Service Endpoints

| Path Pattern | Target Service | Port | Authentication | Roles |
|--------------|----------------|------|----------------|-------|
| `/api/auth/**` | Authentication Service | 8080 | None | - |
| `/api/users/**` | User Management Service | 8081 | Required | Any authenticated |
| `/api/products/**` | Inventory Management Service | 8082 | None | - |
| `/api/inventory/**` | Inventory Management Service | 8082 | Required | ADMIN, MANAGER |
| `/api/orders/**` | Sales Management Service | 8083 | Required | Any authenticated |
| `/api/reports/**` | Sales Management Service | 8083 | Required | ADMIN, MANAGER |
| `/api/cart/**` | Payment & Cart Service | 8084 | Required | Any authenticated |
| `/api/payments/**` | Payment & Cart Service | 8084 | Required | Any authenticated |
| `/api/points/**` | Point Service | 8085 | Required | Any authenticated |
| `/api/coupons/**` | Coupon Service | 8086 | Partial | Varies by endpoint |
| `/api/recommendations/**` | AI Support Service | 8087 | None | - |
| `/api/search/**` | AI Support Service | 8087 | None | - |
| `/api/chat/**` | AI Support Service | 8087 | Required | Any authenticated |
| `/api/analytics/**` | AI Support Service | 8087 | Required | ADMIN, MANAGER |

## Technology Stack

- **Java**: 21 LTS
- **Spring Boot**: 3.5.3
- **Spring Cloud Gateway**: 2025.0.0 (Reactive API gateway)
- **Spring Security**: 6.x (Authentication & authorization)
- **Redis**: 7.2+ (Rate limiting & caching)
- **Apache Kafka**: 7.4+ (Event streaming)
- **Resilience4j**: Latest (Circuit breaker pattern)
- **Micrometer**: Latest (Metrics collection)
- **Maven**: 3.9+ (Build & dependency management)
- **Docker**: Latest (Containerization)
- **Cloud Platform**: Azure Container Apps

## Environment Variables

| Variable Name | Description | Default Value |
|---------------|-------------|---------------|
| `PORT` | Service port number | `8090` |
| `REDIS_HOST` | Redis server hostname | `localhost` |
| `REDIS_PORT` | Redis server port | `6379` |
| `REDIS_PASSWORD` | Redis password | - |
| `KAFKA_BROKERS` | Kafka broker endpoints | `localhost:9092` |
| `JWT_ISSUER_URI` | JWT token issuer | `http://localhost:8080/auth/realms/skishop` |
| `JWT_JWK_SET_URI` | JWT key set endpoint | `http://localhost:8080/auth/realms/skishop/protocol/openid_connect/certs` |
| `AUTH_SERVICE_URL` | Authentication service URL | `http://localhost:8080` |
| `USER_SERVICE_URL` | User management service URL | `http://localhost:8081` |
| `INVENTORY_SERVICE_URL` | Inventory service URL | `http://localhost:8082` |
| `SALES_SERVICE_URL` | Sales management service URL | `http://localhost:8083` |
| `PAYMENT_CART_SERVICE_URL` | Payment & cart service URL | `http://localhost:8084` |
| `POINT_SERVICE_URL` | Point service URL | `http://localhost:8085` |
| `COUPON_SERVICE_URL` | Coupon service URL | `http://localhost:8086` |
| `AI_SUPPORT_SERVICE_URL` | AI support service URL | `http://localhost:8087` |
| `FRONTEND_URL` | Frontend application URL | `http://localhost:3000` |
| `ADMIN_URL` | Admin dashboard URL | `http://localhost:3001` |

### Profiles

- **local**: Local development environment (default settings for localhost)
- **test**: Testing environment (mocked external services)
- **production**: Production environment (Azure services enabled)

Profile settings are configured in `application.yml` and `application-{profile}.yml` files.

## Local Development Environment Setup and Verification

### Local Compilation and Execution

1. **Install Dependencies**

```bash
cd api-gateway
mvn clean install
```

1. **Configure Environment Variables**

Create a `.env` file or set environment variables:

```bash
export REDIS_HOST="localhost"
export KAFKA_BROKERS="localhost:9092"
export JWT_ISSUER_URI="http://localhost:8080/auth/realms/skishop"
```

1. **Start Required Services**

Start Redis and Kafka:

```bash
# Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# Kafka (using Confluent Platform)
docker run -d --name kafka \
  -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  confluentinc/cp-kafka:latest
```

1. **Start Application**

```bash
mvn spring-boot:run
```

1. **Access via Browser**

- **Swagger UI**: [http://localhost:8090/swagger-ui.html](http://localhost:8090/swagger-ui.html)
- **API Documentation**: [http://localhost:8090/api-docs](http://localhost:8090/api-docs)
- **Health Check**: [http://localhost:8090/actuator/health](http://localhost:8090/actuator/health)

### Run Microservices with Docker Compose

1. **Build and Start Containers**

```bash
docker-compose up --build
```

1. **Access via Browser**

- **API Gateway Service**: [http://localhost:8090](http://localhost:8090)
- **Swagger UI**: [http://localhost:8090/swagger-ui.html](http://localhost:8090/swagger-ui.html)

## Production Environment Setup and Verification

Primary deployment target: Azure Container Apps
Related external resources: Azure Redis Cache, Azure Event Hubs, Azure Key Vault, Azure API Management

### 1. Setup Related External Resources

**Azure Redis Cache**:

```bash
# Create Azure Redis Cache
az redis create \
  --name "ski-shop-redis" \
  --resource-group "rg-ski-shop" \
  --location "East US" \
  --sku "Standard" \
  --vm-size "C1"
```

**Azure Event Hubs (Kafka)**:

```bash
# Create Event Hubs namespace
az eventhubs namespace create \
  --name "ski-shop-eventhub" \
  --resource-group "rg-ski-shop" \
  --location "East US" \
  --sku "Standard"

# Create Event Hub
az eventhubs eventhub create \
  --name "gateway-events" \
  --namespace-name "ski-shop-eventhub" \
  --resource-group "rg-ski-shop"
```

**Azure Key Vault**:

```bash
# Create Key Vault
az keyvault create \
  --name "ski-shop-keyvault" \
  --resource-group "rg-ski-shop" \
  --location "East US"
```

### 2. Azure Container Apps Environment Setup and Deployment

```bash
# Set environment variables
export AZURE_SUBSCRIPTION_ID="your-subscription-id"
export AZURE_LOCATION="eastus"
export AZURE_ENV_NAME="api-gateway-prod"

# Initialize Azure Developer CLI
azd init --template api-gateway

# Set secrets
azd env set REDIS_PASSWORD "your-redis-password" --secret
azd env set JWT_SECRET "your-jwt-secret-key" --secret
azd env set REDIS_HOST "ski-shop-redis.redis.cache.windows.net"

# Deploy infrastructure and application
azd up
```

### 3. Access Azure Container Apps Instance via Browser

After deployment, access the service at the provided Azure Container Apps URL:

- **Service URL**: [https://api-gateway-prod.internal.azurecontainerapps.io](https://api-gateway-prod.internal.azurecontainerapps.io)
- **Health Check**: [https://api-gateway-prod.internal.azurecontainerapps.io/actuator/health](https://api-gateway-prod.internal.azurecontainerapps.io/actuator/health)

### 4. Azure CLI Environment Setup Script

```bash
#!/bin/bash
# setup-azure-environment.sh

# Variables
RESOURCE_GROUP="rg-ski-shop-gateway"
LOCATION="eastus"
APP_NAME="api-gateway"
CONTAINER_REGISTRY="acrskishop"
REDIS_NAME="ski-shop-redis"

# Create resource group
az group create --name $RESOURCE_GROUP --location $LOCATION

# Create Azure Redis Cache
az redis create \
  --name $REDIS_NAME \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION \
  --sku "Standard" \
  --vm-size "C1"

# Create Container Registry
az acr create \
  --name $CONTAINER_REGISTRY \
  --resource-group $RESOURCE_GROUP \
  --sku "Standard" \
  --location $LOCATION

# Create Container Apps Environment
az containerapp env create \
  --name "env-ski-shop" \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION

# Deploy application
az containerapp create \
  --name $APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --environment "env-ski-shop" \
  --image "$CONTAINER_REGISTRY.azurecr.io/api-gateway:latest" \
  --target-port 8090 \
  --ingress 'external' \
  --env-vars \
    PORT=8090 \
    REDIS_HOST="$REDIS_NAME.redis.cache.windows.net" \
    KAFKA_BROKERS="ski-shop-eventhub.servicebus.windows.net:9093"

echo "Deployment completed. Check the Azure portal for the application URL."
```

## Service Feature Verification Methods

### curl Commands for Testing and Expected Results

### 1. Health Check

```bash
curl -X GET http://localhost:8090/actuator/health

# Expected Response:
{
  "status": "UP",
  "components": {
    "redis": {"status": "UP"},
    "gateway": {"status": "UP"},
    "circuitBreakers": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

### 2. Route Configuration

```bash
curl -X GET http://localhost:8090/actuator/gateway/routes \
  -H "Authorization: Bearer admin-jwt-token"

# Expected Response:
[
  {
    "route_id": "auth-service",
    "route_definition": {
      "id": "auth-service",
      "uri": "http://localhost:8080",
      "predicates": [{"name": "Path", "args": {"pattern": "/api/auth/**"}}]
    }
  }
]
```

### 3. Authentication Test

```bash
curl -X GET http://localhost:8090/api/users/profile \
  -H "Authorization: Bearer valid-jwt-token"

# Expected Response (forwarded from User Service):
{
  "userId": "user123",
  "username": "john.doe",
  "email": "john.doe@example.com",
  "roles": ["USER"]
}
```

### 4. Rate Limiting Test

```bash
# Test rate limiting (should be limited after configured number of requests)
for i in {1..15}; do
  curl -w "%{http_code}\n" \
    http://localhost:8090/api/products
done

# Expected Response after limit exceeded:
# HTTP 429 Too Many Requests
{
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please try again later.",
  "timestamp": "2025-07-04T10:30:00Z"
}
```

### 5. Circuit Breaker Test

```bash
# Monitor circuit breaker status
curl -X GET http://localhost:8090/actuator/metrics/resilience4j.circuitbreaker.state

# Expected Response:
{
  "name": "resilience4j.circuitbreaker.state",
  "measurements": [
    {"statistic": "VALUE", "value": 0.0}
  ],
  "availableTags": [
    {"tag": "name", "values": ["user-service", "inventory-service"]}
  ]
}
```

## Integration with Other Microservices

This service integrates with all backend microservices:

- **Authentication Service**: JWT token validation and user authentication
- **User Management Service**: User profile and account management
- **Inventory Management Service**: Product catalog and inventory operations
- **Sales Management Service**: Order processing and sales reports
- **Payment Cart Service**: Shopping cart and payment processing
- **Point Service**: Loyalty points management
- **Coupon Service**: Discount and promotional code management
- **AI Support Service**: AI-powered recommendations and chat support

## Testing

### Unit Test Execution

```bash
# Run unit tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Integration Test Execution

```bash
# Run integration tests
mvn test -Dtest="*IT"

# Run all tests including integration tests
mvn verify

# Run tests with Testcontainers
mvn test -Dspring.profiles.active=test
```

### Load Testing

```bash
# Install Apache Bench
brew install httpie

# Run load test
ab -n 1000 -c 10 http://localhost:8090/api/products

# Or use K6 for advanced testing
k6 run --vus 50 --duration 30s load-test.js
```

## Monitoring & Logging

### Health Check

```url
GET /actuator/health
```

Response includes Redis, Gateway routes, and Circuit Breaker status.

### Metrics

```url
GET /actuator/metrics
GET /actuator/prometheus
```

Available metrics:

- Gateway request count and duration
- Route performance metrics
- Circuit breaker state and metrics
- Rate limiter statistics
- JVM and system metrics

### Log Levels

- **Development environment**: DEBUG
- **Production environment**: INFO

Key loggers:

- `com.skishop.gateway`: Gateway service operations
- `org.springframework.cloud.gateway`: Spring Cloud Gateway framework
- `org.springframework.security`: Security and authentication
- `io.github.resilience4j`: Circuit breaker operations

## Troubleshooting

### Common Issues

### 1. Service Unavailable (503 Errors)

```text
ERROR: Backend service unavailable
Solution: Check circuit breaker status and backend service health
```

```bash
# Check circuit breaker status
curl http://localhost:8090/actuator/metrics/resilience4j.circuitbreaker.state

# Check backend service health
curl http://localhost:8081/actuator/health
```

### 2. Rate Limiting Issues

```text
ERROR: Too Many Requests (429)
Solution: Check rate limit configuration and Redis connectivity
```

```bash
# Check Redis connectivity
redis-cli -h localhost ping

# Monitor rate limiter metrics
curl http://localhost:8090/actuator/metrics/spring.cloud.gateway.requests
```

### 3. Authentication Failures

```text
ERROR: Unauthorized (401)
Solution: Verify JWT token and authentication service connectivity
```

```bash
# Validate JWT token
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/auth/userinfo

# Check JWT configuration
curl http://localhost:8090/actuator/configprops | grep jwt
```

### 4. Route Configuration Issues

```text
ERROR: No route found for request
Solution: Check route configuration and service URLs
```

```bash
# List configured routes
curl http://localhost:8090/actuator/gateway/routes

# Check service connectivity
curl http://localhost:8081/actuator/health
```

## Developer Information

### Directory Structure

```text
api-gateway/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/skishop/gateway/
│   │   │       ├── ApiGatewayApplication.java          # Main application class
│   │   │       ├── config/
│   │   │       │   ├── GatewayConfig.java              # Gateway configuration
│   │   │       │   ├── SecurityConfig.java             # Security configuration
│   │   │       │   ├── CorsConfig.java                 # CORS configuration
│   │   │       │   └── RedisConfig.java                # Redis configuration
│   │   │       ├── filter/
│   │   │       │   ├── AuthenticationFilter.java       # JWT authentication filter
│   │   │       │   ├── LoggingFilter.java              # Request/response logging
│   │   │       │   ├── RateLimitFilter.java            # Rate limiting filter
│   │   │       │   └── MetricsFilter.java              # Metrics collection filter
│   │   │       ├── handler/
│   │   │       │   ├── GlobalErrorHandler.java         # Global exception handling
│   │   │       │   └── FallbackHandler.java            # Circuit breaker fallbacks
│   │   │       └── model/
│   │   │           ├── GatewayRequest.java             # Request model
│   │   │           └── GatewayResponse.java            # Response model
│   │   └── resources/
│   │       ├── application.yml                         # Main configuration
│   │       ├── application-local.yml                   # Local development config
│   │       ├── application-production.yml              # Production config
│   │       └── logback-spring.xml                      # Logging configuration
│   └── test/
│       ├── java/com/skishop/gateway/
│       │   ├── GatewayApplicationTests.java            # Integration tests
│       │   ├── filter/
│       │   │   ├── AuthenticationFilterTest.java      # Filter unit tests
│       │   │   └── RateLimitFilterTest.java
│       │   └── integration/
│       │       ├── GatewayIntegrationTest.java         # End-to-end tests
│       │       └── SecurityIntegrationTest.java        # Security tests
│       └── resources/
│           ├── application-test.yml                    # Test configuration
│           └── test-data/                              # Test fixtures
├── docker-compose.yml                                  # Local development setup
├── Dockerfile                                          # Container image definition
├── pom.xml                                            # Maven configuration
└── README.md                                          # This file
```

## Change History

- **v1.0.0** (2025-07-04): Initial release
  - Spring Cloud Gateway implementation with reactive programming model
  - JWT-based authentication and authorization
  - Redis-based rate limiting and caching
  - Circuit breaker pattern with Resilience4j
  - Comprehensive monitoring and observability
  - Docker containerization support
  - Azure deployment configuration
  - API documentation aggregation
  - CORS support for web applications
  - Event streaming with Kafka integration
  - Load balancing and failover capabilities
