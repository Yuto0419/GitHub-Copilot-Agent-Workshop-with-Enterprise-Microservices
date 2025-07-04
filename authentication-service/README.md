# Authentication Service

Identity and Access Management for Ski Shop E-commerce Platform

## Main Features Provided

### Feature List

- **Secure Authentication**: User login and registration with Microsoft Entra ID integration
- **JWT Token Management**: Secure token generation and validation for API access
- **Multi-Factor Authentication**: TOTP-based MFA support
- **Role-Based Access Control**: Flexible RBAC system with role hierarchy
- **Session Management**: Secure session handling with Redis
- **Password Management**: Reset, change, and secure password storage
- **OAuth2/OpenID Connect**: Integration with external identity providers
- **Security Monitoring**: Comprehensive audit logging and suspicious activity detection
- **Account Protection**: Rate limiting, account lockout, and security policies

## Service Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/v1/auth/login` | User login with credentials | Public |
| POST | `/api/v1/auth/refresh` | Refresh access token | Authenticated |
| POST | `/api/v1/auth/logout` | User logout | Authenticated |
| POST | `/api/v1/auth/validate` | Validate token | Authenticated |
| GET | `/api/v1/auth/me` | Get current user info | Authenticated |
| POST | `/api/v1/auth/mfa/verify` | Verify MFA code | Public |
| POST | `/api/v1/auth/mfa/setup` | Setup MFA for user | Authenticated |
| DELETE | `/api/v1/auth/mfa/disable` | Disable MFA | Authenticated |
| POST | `/api/v1/auth/password/reset` | Request password reset | Public |
| POST | `/api/v1/auth/password/confirm` | Confirm password reset | Public |
| PUT | `/api/v1/auth/password/change` | Change password | Authenticated |
| POST | `/api/auth/users` | Register new user | Public |
| DELETE | `/api/auth/users/{userId}` | Soft delete user | Authenticated (Admin) |
| GET | `/oauth2/authorization/azure` | Initiate Azure AD OAuth2 flow | Public |
| GET | `/login/oauth2/code/azure` | OAuth2 callback | Public |
| GET | `/actuator/health` | Service health check | Public |
| GET | `/actuator/prometheus` | Prometheus metrics | Authenticated (Admin) |

## Technology Stack

- **Java**: 21 LTS
- **Spring Boot**: 3.5.3
- **Spring Security**: OAuth2, JWT
- **Spring Data JPA**: Database access
- **Spring Data Redis**: Session management
- **Microsoft Entra ID**: Azure AD integration
- **Database**: PostgreSQL 16+
- **Cache**: Redis 7.2+
- **Event Streaming**: Apache Kafka / Azure Service Bus
- **Containerization**: Docker
- **Cloud Platform**: Azure Container Apps

## Environment Variables

| Variable Name | Description | Default Value |
|---------------|-------------|---------------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `local` |
| `DB_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/skishop_auth` |
| `DB_USERNAME` | Database username | `auth_user` |
| `DB_PASSWORD` | Database password | `auth_password` |
| `DDL_AUTO` | Hibernate DDL auto setting | `update` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis password | - |
| `AZURE_TENANT_ID` | Azure AD tenant ID | - |
| `AZURE_CLIENT_ID` | Azure AD client ID | - |
| `AZURE_CLIENT_SECRET` | Azure AD client secret | - |
| `AZURE_APP_ID_URI` | Azure AD app ID URI | `api://app-id` |
| `JWT_SECRET` | JWT signing secret | - |
| `JWT_ISSUER` | JWT issuer name | `SkiShop-Auth` |
| `JWT_ACCESS_EXPIRATION` | JWT access token expiry (seconds) | `3600` |
| `JWT_REFRESH_EXPIRATION` | JWT refresh token expiry (seconds) | `604800` |
| `LOG_LEVEL` | Application log level | `INFO` |
| `SECURITY_LOG_LEVEL` | Security log level | `INFO` |
| `SKISHOP_AUTH_ENABLE` | Enable authentication checks | `true` |
| `SKISHOP_MFA_ENABLE` | Enable MFA functionality | `false` |

### Profiles

- **local**: Local development environment (in-memory or local databases)
- **dev**: Development environment (shared development resources)
- **test**: Testing environment (test databases and mocked external services)
- **prod**: Production environment (Azure services and full security)

Profile settings are configured in `application.yml` and `application-{profile}.yml` files.

## Local Development Environment Setup and Verification

### Local Compilation and Execution

1. **Install Dependencies**

```bash
cd authentication-service
mvn clean install
```

1. **Configure Environment Variables**

Create a `.env` file or set environment variables:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/skishop_auth"
export DB_USERNAME="auth_user"
export DB_PASSWORD="auth_password"
export JWT_SECRET="your-secure-jwt-secret-key-at-least-64-characters-long-for-hs512-algorithm"
```

1. **Start Required Services**

Start PostgreSQL and Redis:

```bash
# PostgreSQL
docker run -d --name auth-postgres -p 5432:5432 \
  -e POSTGRES_DB=skishop_auth \
  -e POSTGRES_USER=auth_user \
  -e POSTGRES_PASSWORD=auth_password \
  postgres:15-alpine

# Redis
docker run -d --name auth-redis -p 6379:6379 redis:7-alpine
```

1. **Start Application**

```bash
mvn spring-boot:run
```

1. **Access via Browser**

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **API Documentation**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **Health Check**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

### Run Microservices with Docker Compose

1. **Build and Start Containers**

```bash
docker-compose up --build
```

1. **Access via Browser**

- **Authentication Service**: [http://localhost:8080](http://localhost:8080)
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## Production Environment Setup and Verification

Primary deployment target: Azure Container Apps
Related external resources: Azure Database for PostgreSQL, Azure Redis Cache, Azure Entra ID, Azure Service Bus

### 1. Setup Related External Resources

**Azure Database for PostgreSQL**:

```bash
# Create Azure PostgreSQL
az postgres flexible-server create \
  --name "ski-shop-auth-db" \
  --resource-group "rg-ski-shop" \
  --location "East US" \
  --admin-user "auth_admin" \
  --admin-password "YourSecurePassword123!" \
  --sku-name "Standard_D2s_v3" \
  --tier "GeneralPurpose" \
  --storage-size 128 \
  --version "16"

# Configure firewall
az postgres flexible-server firewall-rule create \
  --name "AllowAllAzureServices" \
  --resource-group "rg-ski-shop" \
  --server-name "ski-shop-auth-db" \
  --start-ip-address "0.0.0.0" \
  --end-ip-address "0.0.0.0"
```

**Azure Redis Cache**:

```bash
az redis create \
  --name "ski-shop-auth-redis" \
  --resource-group "rg-ski-shop" \
  --location "East US" \
  --sku "Standard" \
  --vm-size "C1"
```

**Azure Entra ID App Registration**:

```bash
# Create App Registration
az ad app create \
  --display-name "SkiShop-Auth" \
  --sign-in-audience "AzureADandPersonalMicrosoftAccount" \
  --web-redirect-uris "https://ski-shop-auth.azurecontainerapps.io/login/oauth2/code/azure"
```

### 2. Azure Container Apps Environment Setup and Deployment

```bash
# Set environment variables
export AZURE_SUBSCRIPTION_ID="your-subscription-id"
export AZURE_LOCATION="eastus"
export AZURE_ENV_NAME="auth-prod"

# Initialize Azure Developer CLI
azd init --template authentication-service

# Set secrets
azd env set DB_PASSWORD "YourSecurePassword123!" --secret
azd env set AZURE_CLIENT_SECRET "your-azure-client-secret" --secret
azd env set JWT_SECRET "your-secure-jwt-secret-key-at-least-64-characters-long-for-hs512-algorithm" --secret
azd env set REDIS_PASSWORD "your-redis-password" --secret

# Deploy infrastructure and application
azd up
```

### 3. Access Azure Container Apps Instance via Browser

After deployment, access the service at the provided Azure Container Apps URL:

- **Service URL**: [https://auth-prod.internal.azurecontainerapps.io](https://auth-prod.internal.azurecontainerapps.io)
- **Health Check**: [https://auth-prod.internal.azurecontainerapps.io/actuator/health](https://auth-prod.internal.azurecontainerapps.io/actuator/health)

### 4. Azure CLI Environment Setup Script

```bash
#!/bin/bash
# setup-azure-environment.sh

# Variables
RESOURCE_GROUP="rg-ski-shop-auth"
LOCATION="eastus"
APP_NAME="authentication-service"
CONTAINER_REGISTRY="acrskishop"
POSTGRES_SERVER_NAME="ski-shop-auth-db"
REDIS_CACHE_NAME="ski-shop-auth-redis"

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
  --image "$CONTAINER_REGISTRY.azurecr.io/authentication-service:latest" \
  --target-port 8080 \
  --ingress 'external' \
  --env-vars \
    SPRING_PROFILES_ACTIVE="prod" \
    DB_URL="jdbc:postgresql://$POSTGRES_SERVER_NAME.postgres.database.azure.com:5432/skishop_auth" \
    DB_USERNAME="auth_admin" \
    REDIS_HOST="$REDIS_CACHE_NAME.redis.cache.windows.net" \
    REDIS_PORT="6380" \
    REDIS_SSL="true" \
    JWT_ISSUER="SkiShop-Auth-Production"

echo "Deployment completed. Check the Azure portal for the application URL."
```

## Service Feature Verification Methods

### curl Commands for Testing and Expected Results

### 1. Health Check

```bash
curl -X GET http://localhost:8080/actuator/health

# Expected Response:
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "db": {"status": "UP"},
    "redis": {"status": "UP"}
  }
}
```

### 2. User Login

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "YourPassword123!"
  }'

# Expected Response:
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "USER"
  }
}
```

### 3. Token Validation

```bash
curl -X POST http://localhost:8080/api/v1/auth/validate \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."

# Expected Response:
{
  "valid": true,
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "roles": ["USER"],
  "expiresIn": 3200
}
```

### 4. User Registration

```bash
curl -X POST http://localhost:8080/api/auth/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "SecurePassword123!",
    "firstName": "Jane",
    "lastName": "Smith"
  }'

# Expected Response:
{
  "success": true,
  "message": "User registered successfully",
  "userId": "550e8400-e29b-41d4-a716-446655440001"
}
```

## Integration with Other Microservices

This service integrates with the following microservices:

- **API Gateway**: Authentication and authorization for all requests
- **User Management Service**: User profile management
- **Sales Management Service**: Authorization for order operations
- **Inventory Management Service**: Authorization for inventory operations
- **Payment Cart Service**: Authorization for payment operations
- **Point Service**: Authorization for point transactions
- **Coupon Service**: Authorization for coupon operations

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

Response includes PostgreSQL, Redis, and Azure AD connectivity status.

### Metrics

```url
GET /actuator/metrics
GET /actuator/prometheus
```

Available metrics:

- Authentication attempts (success/failure)
- Token operations (generation/validation)
- User registration and management
- Session activity
- Resource authorization
- Rate limiting statistics

### Log Levels

- **Development environment**: DEBUG
- **Production environment**: INFO

Key loggers:

- `com.skishop.auth`: Authentication service operations
- `org.springframework.security`: Security framework operations
- `org.springframework.web`: Web request handling
- `com.azure.spring`: Azure AD integration

## Troubleshooting

### Common Issues

#### 1. Database Connection Failure

```text
ERROR: Unable to connect to database
Solution: Check DB_URL, DB_USERNAME, and DB_PASSWORD environment variables
```

#### 2. Redis Connection Issues

```text
ERROR: Unable to connect to Redis
Solution: Verify Redis is running and REDIS_HOST/REDIS_PORT are correct
```

#### 3. Invalid JWT Signature

```text
ERROR: JWT signature does not match locally computed signature
Solution: Ensure the same JWT_SECRET is used across all instances
```

#### 4. Azure AD Integration Failure

```text
ERROR: Failed to validate the token
Solution: Check AZURE_TENANT_ID, AZURE_CLIENT_ID, and AZURE_CLIENT_SECRET
```

#### 5. Rate Limiting

```text
ERROR: Too many requests
Solution: Implement exponential backoff in client applications
```

## Developer Information

### Directory Structure

```text
authentication-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/skishop/auth/
│   │   │       ├── controller/          # REST API controllers
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── UserController.java
│   │   │       │   └── MfaController.java
│   │   │       ├── service/             # Business logic services
│   │   │       │   ├── AuthService.java
│   │   │       │   ├── TokenService.java
│   │   │       │   └── MfaService.java
│   │   │       ├── dto/                 # Data transfer objects
│   │   │       ├── entity/              # JPA entities
│   │   │       ├── repository/          # Database repositories
│   │   │       ├── config/              # Configuration classes
│   │   │       └── AuthenticationServiceApplication.java
│   │   └── resources/
│   │       ├── application.yml          # Main configuration
│   │       ├── application-local.yml    # Local environment config
│   │       └── application-test.yml     # Test environment config
│   └── test/
│       ├── java/                        # Unit and integration tests
│       └── resources/
│           └── application-test.yml     # Test configuration
├── docker-compose.yml                   # Local development setup
├── Dockerfile                           # Container image definition
├── pom.xml                              # Maven configuration
└── README.md                            # This file
```

## Change History

- **v1.0.0** (2025-07-04): Initial release
  - Secure authentication with Microsoft Entra ID integration
  - JWT token management and validation
  - Role-based access control
  - User registration and management
  - Session handling with Redis
  - Comprehensive security monitoring
  - Docker containerization and Azure deployment support
