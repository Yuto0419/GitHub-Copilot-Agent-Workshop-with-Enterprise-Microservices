# Point Service

Customer loyalty point management microservice for Ski Shop E-commerce Platform

## Main Features Provided

### Feature List

- **Point Management**: Point earning, redemption, transfer, and balance tracking
- **Tier System**: Customer tier calculation, upgrade processing, and benefit management
- **Point Lifecycle**: Automated point expiration processing and notifications
- **Transaction History**: Complete audit trail of all point-related activities
- **Analytics & Reporting**: Point usage analytics and tier progression reporting
- **Event Integration**: Seamless integration with order, payment, and user management services
- **Scheduled Processing**: Automated batch processing for point expiry and tier upgrades

## Service Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/points/award` | Award points to user | Admin |
| GET | `/api/points/balance/{userId}` | Get point balance | Authenticated |
| GET | `/api/points/history/{userId}` | Get point history | Authenticated |
| GET | `/api/points/history/{userId}/range` | Get point history by date range | Authenticated |
| POST | `/api/points/redeem` | Redeem points | Authenticated |
| GET | `/api/points/expiring/{userId}` | Get expiring points | Authenticated |
| POST | `/api/points/transfer` | Transfer points between users | Authenticated |
| POST | `/api/points/process-expired` | Process expired points | Admin |
| GET | `/api/tiers/user/{userId}` | Get user tier info | Authenticated |
| GET | `/api/tiers` | Get all tier definitions | Public |
| GET | `/api/tiers/{tierLevel}` | Get specific tier definition | Public |
| GET | `/api/tiers/upgrade-eligibility/{userId}` | Check upgrade eligibility | Authenticated |
| POST | `/api/tiers/process-upgrades` | Process all tier upgrades | Admin |

## Technology Stack

- **Java**: 21 LTS
- **Spring Boot**: 3.5.3
- **Spring Data JPA**: PostgreSQL data access
- **Spring Data Redis**: Caching
- **Spring Security**: Authentication and authorization
- **Spring Cloud Stream**: Kafka event integration
- **Quartz Scheduler**: Scheduled tasks for point expiry
- **PostgreSQL**: Primary database for point and tier data
- **Redis**: Caching layer for performance optimization
- **Kafka**: Event streaming for service integration
- **Flyway**: Database migration
- **MapStruct**: Object mapping
- **Lombok**: Boilerplate reduction
- **Micrometer**: Metrics collection
- **Containerization**: Docker
- **Cloud Platform**: Azure Container Apps

## Environment Variables

| Variable Name | Description | Default Value |
|---------------|-------------|---------------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `local` |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/skishop_points` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `points_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `points_password` |
| `SPRING_REDIS_HOST` | Redis host | `localhost` |
| `SPRING_REDIS_PORT` | Redis port | `6379` |
| `SPRING_REDIS_PASSWORD` | Redis password | - |
| `SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS` | Kafka broker addresses | `localhost:9092` |
| `POINT_EXPIRY_CRON` | Cron expression for point expiry job | `0 0 1 * * ?` |
| `TIER_UPGRADE_CRON` | Cron expression for tier upgrade job | `0 0 2 * * ?` |
| `DEFAULT_POINT_EXPIRY_DAYS` | Default point validity period in days | `365` |
| `JWT_ISSUER_URI` | JWT issuer URI for authentication | `http://localhost:8080/realms/skishop` |
| `CACHE_TTL_BALANCE` | Cache TTL for point balance (seconds) | `60` |
| `CACHE_TTL_TIER` | Cache TTL for tier information (seconds) | `300` |
| `LOG_LEVEL_ROOT` | Root logger level | `INFO` |
| `LOG_LEVEL_APP` | Application logger level | `DEBUG` |

### Profiles

- **local**: Local development environment (default settings for localhost)
- **test**: Testing environment (in-memory database, mocked services)
- **production**: Production environment (Azure services enabled)

Profile settings are configured in `application.yml` and `application-{profile}.yml` files.

## Local Development Environment Setup and Verification

### Local Compilation and Execution

1. **Install Dependencies**

```bash
cd point-service
mvn clean install
```

1. **Configure Environment Variables**

Create a `.env` file or set environment variables:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/skishop_points"
export SPRING_DATASOURCE_USERNAME="points_user"
export SPRING_DATASOURCE_PASSWORD="points_password"
export SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS="localhost:9092"
```

1. **Start Required Services**

Start PostgreSQL, Redis, and Kafka:

```bash
# PostgreSQL
docker run -d --name point-postgres -p 5432:5432 \
  -e POSTGRES_DB=skishop_points \
  -e POSTGRES_USER=points_user \
  -e POSTGRES_PASSWORD=points_password \
  postgres:15-alpine

# Redis
docker run -d --name point-redis -p 6379:6379 redis:latest

# Kafka & Zookeeper
docker run -d --name point-kafka -p 2181:2181 -p 9092:9092 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e ZOOKEEPER_CONNECT=localhost:2181 \
  wurstmeister/kafka:latest
```

1. **Start Application**

```bash
mvn spring-boot:run
```

1. **Access via Browser**

- **Swagger UI**: [http://localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html)
- **API Documentation**: [http://localhost:8085/v3/api-docs](http://localhost:8085/v3/api-docs)
- **Health Check**: [http://localhost:8085/actuator/health](http://localhost:8085/actuator/health)

### Run Microservices with Docker Compose

1. **Build and Start Containers**

```bash
docker-compose up --build
```

1. **Access via Browser**

- **Point Service**: [http://localhost:8085](http://localhost:8085)
- **Swagger UI**: [http://localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html)

## Production Environment Setup and Verification

Primary deployment target: Azure Container Apps
Related external resources: Azure Database for PostgreSQL, Azure Cache for Redis, Azure Event Hubs (Kafka)

### 1. Setup Related External Resources

**Azure Database for PostgreSQL**:

```bash
# Create PostgreSQL flexible server
az postgres flexible-server create \
  --name "ski-shop-points-db" \
  --resource-group "rg-ski-shop" \
  --location "East US" \
  --admin-user "points_admin" \
  --admin-password "YourSecurePassword123!" \
  --sku-name "Standard_B1ms" \
  --tier "Burstable" \
  --storage-size 32 \
  --version "15"

# Create database
az postgres flexible-server db create \
  --database-name "skishop_points" \
  --server-name "ski-shop-points-db" \
  --resource-group "rg-ski-shop"
```

**Azure Cache for Redis**:

```bash
az redis create \
  --name "ski-shop-points-redis" \
  --resource-group "rg-ski-shop" \
  --location "East US" \
  --sku "Basic" \
  --vm-size "C0"
```

**Azure Event Hubs (Kafka)**:

```bash
# Create Event Hubs namespace
az eventhubs namespace create \
  --name "ski-shop-events" \
  --resource-group "rg-ski-shop" \
  --location "East US" \
  --sku "Standard"

# Create event hubs for point service topics
az eventhubs eventhub create \
  --name "points-events" \
  --namespace-name "ski-shop-events" \
  --resource-group "rg-ski-shop" \
  --partition-count 4 \
  --message-retention 1
```

### 2. Azure Container Apps Environment Setup and Deployment

```bash
# Set environment variables
export AZURE_SUBSCRIPTION_ID="your-subscription-id"
export AZURE_LOCATION="eastus"
export AZURE_ENV_NAME="points-prod"

# Initialize Azure Developer CLI
azd init --template point-service

# Set secrets
azd env set SPRING_DATASOURCE_PASSWORD "YourSecurePassword123!" --secret
azd env set SPRING_REDIS_PASSWORD "YourRedisPassword" --secret

# Deploy infrastructure and application
azd up
```

### 3. Access Azure Container Apps Instance via Browser

After deployment, access the service at the provided Azure Container Apps URL:

- **Service URL**: [https://points-prod.internal.azurecontainerapps.io](https://points-prod.internal.azurecontainerapps.io)
- **Health Check**: [https://points-prod.internal.azurecontainerapps.io/actuator/health](https://points-prod.internal.azurecontainerapps.io/actuator/health)

### 4. Azure CLI Environment Setup Script

```bash
#!/bin/bash
# setup-azure-environment.sh

# Variables
RESOURCE_GROUP="rg-ski-shop-points"
LOCATION="eastus"
APP_NAME="point-service"
CONTAINER_REGISTRY="acrskishop"
DB_SERVER="ski-shop-points-db"
REDIS_CACHE="ski-shop-points-redis"

# Create resource group
az group create --name $RESOURCE_GROUP --location $LOCATION

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
  --image "$CONTAINER_REGISTRY.azurecr.io/point-service:latest" \
  --target-port 8085 \
  --ingress 'external' \
  --env-vars \
    SPRING_PROFILES_ACTIVE="production" \
    SPRING_DATASOURCE_URL="jdbc:postgresql://$DB_SERVER.postgres.database.azure.com:5432/skishop_points" \
    SPRING_DATASOURCE_USERNAME="points_admin" \
    SPRING_REDIS_HOST="$REDIS_CACHE.redis.cache.windows.net" \
    SPRING_REDIS_PORT="6380" \
    SPRING_REDIS_SSL="true"

echo "Deployment completed. Check the Azure portal for the application URL."
```

## Service Feature Verification Methods

### curl Commands for Testing and Expected Results

### 1. Health Check

```bash
curl -X GET http://localhost:8085/actuator/health

# Expected Response:
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "kafka": {"status": "UP"}
  }
}
```

### 2. Get User Point Balance

```bash
curl -X GET "http://localhost:8085/api/points/balance/550e8400-e29b-41d4-a716-446655440000" \
  -H "Authorization: Bearer your-jwt-token"

# Expected Response:
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "totalEarned": 5670,
  "totalRedeemed": 4420,
  "currentBalance": 1250,
  "expiringPoints": 100,
  "tierLevel": "silver",
  "tierName": "Silver",
  "pointsToNextTier": 750,
  "nextTier": "gold"
}
```

### 3. Award Points

```bash
curl -X POST "http://localhost:8085/api/points/award" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-admin-token" \
  -d '{
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "amount": 100,
    "reason": "Purchase reward",
    "referenceId": "order-12345",
    "expiryDays": 365
  }'

# Expected Response:
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "transactionType": "EARNED",
  "amount": 100,
  "balanceAfter": 1350,
  "reason": "Purchase reward",
  "referenceId": "order-12345",
  "expiresAt": "2026-07-04T23:59:59Z",
  "isExpired": false,
  "createdAt": "2025-07-04T10:30:00Z"
}
```

### 4. Get User Tier Information

```bash
curl -X GET "http://localhost:8085/api/tiers/user/550e8400-e29b-41d4-a716-446655440000" \
  -H "Authorization: Bearer your-jwt-token"

# Expected Response:
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "tierLevel": "silver",
  "tierName": "Silver",
  "totalPointsEarned": 15670,
  "currentPoints": 1250,
  "tierUpgradedAt": "2025-01-01T10:30:00Z",
  "nextTier": "gold",
  "pointsToNextTier": 9330,
  "benefits": {
    "free_shipping_threshold": 75,
    "birthday_bonus": 1000,
    "early_access": true
  },
  "pointMultiplier": 1.25
}
```

## Integration with Other Microservices

This service integrates with the following microservices:

- **API Gateway**: Authentication and routing
- **User Management Service**: User information and registration events
- **Sales Management Service**: Order completion events for point awarding
- **Payment Cart Service**: Payment events for point bonuses
- **Coupon Service**: Point-based promotions

### Event-Based Integration

The Point Service integrates with other microservices through Kafka events:

- **Published Events**:
  - PointsAwarded
  - PointsRedeemed
  - PointsExpired
  - PointsTransferred
  - TierUpgraded
  - TierDowngraded

- **Subscribed Events**:
  - OrderCompleted (from Sales Management Service)
  - UserRegistered (from User Management Service)
  - PaymentProcessed (from Payment Service)
  - ReturnProcessed (from Sales Management Service)

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

## Monitoring & Logging

### Health Check

```url
GET /actuator/health
```

Response includes database, Redis, and Kafka connectivity status.

### Metrics

```url
GET /actuator/metrics
GET /actuator/prometheus
```

Available metrics:

- Point transaction counts (earned, redeemed, expired)
- Tier upgrade frequency
- Cache hit/miss ratio
- API response times
- Point balance distribution by tier

### Log Levels

- **Development environment**: DEBUG
- **Production environment**: INFO

Key loggers:

- `com.skishop.points`: Main application logger
- `com.skishop.points.scheduler`: Scheduled job execution
- `com.skishop.points.events`: Event publishing and consuming
- `org.springframework.cache`: Cache operations

## Troubleshooting

### Common Issues

#### 1. Database Connection Issues

```text
ERROR: Failed to connect to PostgreSQL database
Solution: Check database connection parameters and ensure the database server is running
```

#### 2. Kafka Connection Problems

```text
ERROR: Could not connect to Kafka broker
Solution: Verify Kafka broker addresses and ensure Kafka is running
```

#### 3. Redis Connectivity Issues

```text
ERROR: Redis connection failure
Solution: Check Redis host, port, and password settings
```

#### 4. Scheduled Jobs Not Running

```text
WARN: Scheduled job not triggered
Solution: Verify quartz scheduler configuration and check for overlapping job executions
```

#### 5. Point Calculation Discrepancies

```text
ERROR: Point balance mismatch
Solution: Run reconciliation query to verify transaction history against current balance
```

## Developer Information

### Directory Structure

```text
point-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/skishop/points/
│   │   │       ├── controller/          # REST API controllers
│   │   │       │   ├── PointController.java
│   │   │       │   └── TierController.java
│   │   │       ├── service/             # Business logic services
│   │   │       │   ├── PointService.java
│   │   │       │   ├── TierService.java
│   │   │       │   └── EventService.java
│   │   │       ├── repository/          # Data repositories
│   │   │       │   ├── PointTransactionRepository.java
│   │   │       │   ├── PointRedemptionRepository.java
│   │   │       │   ├── PointExpiryRepository.java
│   │   │       │   ├── UserTierRepository.java
│   │   │       │   └── TierDefinitionRepository.java
│   │   │       ├── entity/              # Entity classes
│   │   │       │   ├── PointTransaction.java
│   │   │       │   ├── PointRedemption.java
│   │   │       │   ├── PointExpiry.java
│   │   │       │   ├── UserTier.java
│   │   │       │   └── TierDefinition.java
│   │   │       ├── dto/                 # Data transfer objects
│   │   │       │   ├── PointTransactionDto.java
│   │   │       │   ├── PointBalanceResponse.java
│   │   │       │   ├── PointAwardRequest.java
│   │   │       │   ├── PointRedemptionRequest.java
│   │   │       │   └── UserTierDto.java
│   │   │       ├── event/               # Event classes
│   │   │       │   ├── PointEvent.java
│   │   │       │   ├── TierEvent.java
│   │   │       │   ├── publisher/
│   │   │       │   └── consumer/
│   │   │       ├── scheduler/           # Scheduled jobs
│   │   │       │   ├── PointExpiryJob.java
│   │   │       │   └── TierUpgradeJob.java
│   │   │       ├── config/              # Configuration classes
│   │   │       │   ├── JpaConfig.java
│   │   │       │   ├── CacheConfig.java
│   │   │       │   ├── KafkaConfig.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   └── QuartzConfig.java
│   │   │       ├── exception/           # Exception handling
│   │   │       │   ├── PointServiceException.java
│   │   │       │   ├── InsufficientPointsException.java
│   │   │       │   └── GlobalExceptionHandler.java
│   │   │       └── PointServiceApplication.java
│   │   └── resources/
│   │       ├── db/migration/            # Flyway migration scripts
│   │       ├── application.yml          # Main configuration
│   │       ├── application-local.yml    # Local environment config
│   │       └── application-prod.yml     # Production environment config
│   └── test/
│       ├── java/                        # Unit and integration tests
│       └── resources/                   # Test resources
├── docker-compose.yml                   # Local development setup
├── Dockerfile                           # Container image definition
├── pom.xml                              # Maven configuration
└── README.md                            # This file
```

## Change History

- **v1.0.0** (2025-07-01): Initial release
  - Point earning and redemption functionality
  - Tier management system with Bronze, Silver, Gold, and Platinum tiers
  - Point expiration processing
  - Event-driven integration with other microservices
  - REST API for point and tier management
