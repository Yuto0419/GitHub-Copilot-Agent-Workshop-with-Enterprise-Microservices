# User Management Service

User account management and authentication microservice for the Ski Shop E-commerce Platform

## Main Features Provided

### Feature List

- **User Registration**: Complete user registration and account creation
- **Authentication**: Secure user authentication and token generation
- **Profile Management**: User profile information management
- **Role-Based Authorization**: Role and permission-based access control
- **User Preferences**: Personalized user settings management
- **Activity Tracking**: User activity logging and analysis
- **Email Verification**: Account verification through email
- **Password Management**: Secure password handling and reset functionality
- **Admin Management**: Administrative interface for user management

## Service Endpoints

### User Management API

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/users` | Register new user | Public |
| GET | `/api/users/{id}` | Get user information | Authenticated |
| PUT | `/api/users/{id}` | Update user information | Authenticated (Self/Admin) |
| DELETE | `/api/users/{id}` | Delete user | Authenticated (Self/Admin) |
| GET | `/api/users/me` | Get current user info | Authenticated |
| PUT | `/api/users/me/password` | Change password | Authenticated |
| POST | `/api/users/verify-email` | Verify email address | Token Required |
| POST | `/api/users/resend-verification` | Resend verification email | Public |
| GET | `/api/users/check-email` | Check email existence | Public |

### User Preferences API

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| GET | `/api/users/{id}/preferences` | Get user preferences | Authenticated (Self/Admin) |
| GET | `/api/users/{id}/preferences/{key}` | Get specific preference | Authenticated (Self/Admin) |
| PUT | `/api/users/{id}/preferences/{key}` | Update preference | Authenticated (Self/Admin) |
| DELETE | `/api/users/{id}/preferences/{key}` | Delete preference | Authenticated (Self/Admin) |

### User Activity API

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| GET | `/api/users/{id}/activities` | Get user activities | Authenticated (Self/Admin) |
| GET | `/api/users/me/activities` | Get current user activities | Authenticated |

### Admin API

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| GET | `/api/admin/users` | Get all users | Authenticated (Admin) |
| POST | `/api/admin/users/{id}/roles` | Update user roles | Authenticated (Admin) |
| PUT | `/api/admin/users/{id}/status` | Update user status | Authenticated (Admin) |
| GET | `/api/admin/roles` | Get all roles | Authenticated (Admin) |
| POST | `/api/admin/roles` | Create new role | Authenticated (Admin) |
| PUT | `/api/admin/roles/{id}` | Update role | Authenticated (Admin) |
| DELETE | `/api/admin/roles/{id}` | Delete role | Authenticated (Admin) |

## Technology Stack

- **Java**: 21 LTS
- **Spring Boot**: 3.5.3
- **Spring Security**: JWT token-based authentication
- **Spring Data JPA**: Database access with Hibernate
- **Spring Cloud Stream**: Kafka integration for event-driven architecture
- **Database**: PostgreSQL for persistent storage
- **Cache**: Redis for session management and data caching
- **QueryDSL**: Type-safe SQL queries
- **MapStruct**: Object mapping
- **Flyway**: Database migration
- **Jasypt**: Configuration encryption
- **API Documentation**: SpringDoc OpenAPI
- **Metrics**: Micrometer with Prometheus
- **Containerization**: Docker
- **Cloud Platform**: Azure Container Apps

## Environment Variables

| Variable Name | Description | Default Value |
|---------------|-------------|---------------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/skishop_user` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `skishop_user` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `password` |
| `SPRING_DATA_REDIS_HOST` | Redis host | `localhost` |
| `SPRING_DATA_REDIS_PORT` | Redis port | `6379` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `localhost:9092` |
| `SKISHOP_AUTHFUNC_ENABLE` | Enable authentication functionality | `true` |
| `TOKEN_SECRET` | JWT token signing secret | - |
| `TOKEN_EXPIRATION` | JWT token expiration in seconds | `86400` |
| `SPRING_MAIL_HOST` | Email server host | `smtp.gmail.com` |
| `SPRING_MAIL_PORT` | Email server port | `587` |
| `SPRING_MAIL_USERNAME` | Email server username | - |
| `SPRING_MAIL_PASSWORD` | Email server password | - |
| `LOGGING_LEVEL_COM_SKISHOP` | Application logging level | `INFO` |

### Profiles

- **local**: Local development environment (default settings for localhost)
- **test**: Testing environment (in-memory database, mocked external services)
- **prod**: Production environment (Azure services enabled)

Profile settings are configured in `application.yml` and `application-{profile}.yml` files.

## Local Development Environment Setup and Verification

### Local Compilation and Execution

1. **Install Dependencies**

```bash
cd user-management-service
mvn clean install
```

1. **Configure Environment Variables**

Create a `.env` file or set environment variables:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/skishop_user"
export SPRING_DATASOURCE_USERNAME="skishop_user"
export SPRING_DATASOURCE_PASSWORD="password"
export TOKEN_SECRET="your-secret-key"
```

1. **Start Required Services**

Start PostgreSQL and Redis:

```bash
docker-compose up -d postgres redis
```

1. **Start Application**

```bash
mvn spring-boot:run
```

1. **Access via Browser**

- **Swagger UI**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **API Documentation**: [http://localhost:8081/api-docs](http://localhost:8081/api-docs)
- **Health Check**: [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)

### Run Microservices with Docker Compose

1. **Build and Start Containers**

```bash
docker-compose up --build
```

1. **Access via Browser**

- **User Management Service**: [http://localhost:8081](http://localhost:8081)
- **Swagger UI**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- **pgAdmin**: [http://localhost:5050](http://localhost:5050) (admin@skishop.com/admin)

## Production Environment Setup and Verification

Primary deployment target: Azure Container Apps
Related external resources: Azure Database for PostgreSQL, Azure Cache for Redis, Azure Service Bus, Azure Key Vault

### 1. Setup Related External Resources

**Azure Database for PostgreSQL**:

```bash
# Create PostgreSQL flexible server
az postgres flexible-server create \
  --name "skishop-user-db" \
  --resource-group "rg-skishop" \
  --location "East US" \
  --admin-user "skishop_admin" \
  --admin-password "<your-secure-password>" \
  --sku-name "Standard_B1ms" \
  --version "15" \
  --storage-size 32 \
  --database-name "skishop_user"
```

**Azure Cache for Redis**:

```bash
az redis create \
  --name "skishop-user-redis" \
  --resource-group "rg-skishop" \
  --location "East US" \
  --sku "Basic" \
  --vm-size "C0"
```

**Azure Key Vault**:

```bash
# Create Key Vault
az keyvault create \
  --name "skishop-user-kv" \
  --resource-group "rg-skishop" \
  --location "East US"

# Add secrets
az keyvault secret set \
  --vault-name "skishop-user-kv" \
  --name "DB-PASSWORD" \
  --value "<your-secure-password>"

az keyvault secret set \
  --vault-name "skishop-user-kv" \
  --name "TOKEN-SECRET" \
  --value "<your-jwt-secret>"
```

### 2. Azure Container Registry

```bash
# Create Azure Container Registry
az acr create \
  --name "skishopregistry" \
  --resource-group "rg-skishop" \
  --sku "Basic" \
  --admin-enabled true
```

### 3. Build and Push Docker Image

```bash
# Build Docker image
docker build -t user-management-service:latest .

# Tag the image for ACR
docker tag user-management-service:latest skishopregistry.azurecr.io/user-management-service:latest

# Log in to ACR
az acr login --name skishopregistry

# Push the image to ACR
docker push skishopregistry.azurecr.io/user-management-service:latest
```

### 4. Deploy to Azure Container Apps

```bash
# Create Container Apps environment
az containerapp env create \
  --name "skishop-env" \
  --resource-group "rg-skishop" \
  --location "East US"

# Create Container App
az containerapp create \
  --name "user-management-service" \
  --resource-group "rg-skishop" \
  --environment "skishop-env" \
  --image "skishopregistry.azurecr.io/user-management-service:latest" \
  --registry-server "skishopregistry.azurecr.io" \
  --target-port 8081 \
  --ingress "external" \
  --min-replicas 1 \
  --max-replicas 3 \
  --enable-dapr \
  --dapr-app-id "user-management-service" \
  --env-vars \
    "SPRING_PROFILES_ACTIVE=prod" \
    "SPRING_DATASOURCE_URL=jdbc:postgresql://skishop-user-db.postgres.database.azure.com:5432/skishop_user?sslmode=require" \
    "SPRING_DATASOURCE_USERNAME=skishop_admin" \
    "SPRING_DATA_REDIS_HOST=skishop-user-redis.redis.cache.windows.net" \
    "SPRING_DATA_REDIS_PORT=6380" \
    "SPRING_DATA_REDIS_SSL=true"
```

### 5. Set Secrets using Key Vault Reference

```bash
# Set up managed identity for Key Vault access
az containerapp identity assign \
  --name "user-management-service" \
  --resource-group "rg-skishop" \
  --system-assigned

# Get the principal ID
principalId=$(az containerapp identity show \
  --name "user-management-service" \
  --resource-group "rg-skishop" \
  --query principalId --output tsv)

# Grant Key Vault access
az keyvault set-policy \
  --name "skishop-user-kv" \
  --resource-group "rg-skishop" \
  --object-id "$principalId" \
  --secret-permissions get list

# Set secrets
az containerapp secret set \
  --name "user-management-service" \
  --resource-group "rg-skishop" \
  --secrets \
    "db-password=keyvaultref:https://skishop-user-kv.vault.azure.net/secrets/DB-PASSWORD" \
    "token-secret=keyvaultref:https://skishop-user-kv.vault.azure.net/secrets/TOKEN-SECRET" \
    "redis-password=keyvaultref:https://skishop-user-kv.vault.azure.net/secrets/REDIS-PASSWORD"
```

### 6. Verify Deployment

```bash
# Get Container App URL
az containerapp show \
  --name "user-management-service" \
  --resource-group "rg-skishop" \
  --query properties.configuration.ingress.fqdn \
  --output tsv

# Check health endpoint
curl https://<container-app-url>/actuator/health
```

## Service Feature Verification Methods

### User Registration Flow

```bash
# Register a new user
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecurePass123!",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "090-1234-5678",
    "birthDate": "1990-01-01",
    "gender": "MALE"
  }'

# Expected Response:
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User",
  "phoneNumber": "090-1234-5678",
  "birthDate": "1990-01-01",
  "gender": "MALE",
  "status": "PENDING_VERIFICATION",
  "emailVerified": false,
  "phoneVerified": false,
  "createdAt": "2025-07-04T10:30:00Z"
}
```

### User Authentication Flow

```bash
# Note: Authentication is handled by an external service, but the user-management-service
# provides the user data needed for authentication. This is a conceptual flow:

# 1. User registers (as shown above)
# 2. User verifies email with token sent to their email
# 3. User logs in through authentication service
# 4. With the JWT token, user can access protected endpoints:

# Get current user profile
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer <jwt-token>"

# Expected Response:
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "email": "test@example.com",
  "firstName": "Test",
  "lastName": "User",
  "phoneNumber": "090-1234-5678",
  "birthDate": "1990-01-01",
  "gender": "MALE",
  "status": "ACTIVE",
  "emailVerified": true,
  "phoneVerified": false,
  "role": {
    "id": "e47ac10b-58cc-4372-a567-0e02b2c3d123",
    "name": "CUSTOMER"
  },
  "createdAt": "2025-07-04T10:30:00Z",
  "updatedAt": "2025-07-04T11:15:00Z"
}
```

### User Preferences Management

```bash
# Set a user preference
curl -X PUT http://localhost:8081/api/users/f47ac10b-58cc-4372-a567-0e02b2c3d479/preferences/theme \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "value": "dark",
    "type": "STRING"
  }'

# Get user preferences
curl -X GET http://localhost:8081/api/users/f47ac10b-58cc-4372-a567-0e02b2c3d479/preferences \
  -H "Authorization: Bearer <jwt-token>"

# Expected Response:
[
  {
    "key": "theme",
    "value": "dark",
    "type": "STRING",
    "createdAt": "2025-07-04T12:00:00Z",
    "updatedAt": "2025-07-04T12:00:00Z"
  },
  {
    "key": "newsletter",
    "value": "true",
    "type": "BOOLEAN",
    "createdAt": "2025-07-04T12:01:00Z",
    "updatedAt": "2025-07-04T12:01:00Z"
  }
]
```

## Integration with Other Microservices

### Event-Based Integration

The User Management Service integrates with other microservices through Kafka events:

- **Published Events**:
  - user.created: When a new user is registered
  - user.updated: When user information is updated
  - user.deleted: When a user is deleted
  - user.verified: When email verification is completed
  - user.login: When a user logs in
  - user.password_changed: When a password is changed
  - user.role_changed: When a user's role is changed

- **Subscribed Events**:
  - order.completed: Record user activity when order is completed
  - payment.completed: Record user activity when payment is processed
  - product.viewed: Record user activity when product is viewed

### Direct API Integration

- **Authentication Service**: User authentication and token validation
- **API Gateway**: Exposes endpoints and handles routing
- **All Other Services**: Retrieve user information and validate permissions

## Monitoring and Observability

### Health Checks and Metrics

- **Health Endpoints**: `/actuator/health` for service health status
- **Metrics Endpoints**: `/actuator/prometheus` for Prometheus metrics scraping
- **Info Endpoint**: `/actuator/info` for application information

### Key Metrics to Monitor

- **Active User Count**: Number of active users
- **Registration Rate**: New user registrations per hour
- **Authentication Success Rate**: Successful authentication percentage
- **API Response Time**: Average response time for API endpoints
- **Error Rate**: Rate of errors across all endpoints

### Logging Configuration

- **Log Format**: JSON structured logging for better parsing
- **Log Levels**: Configurable through environment variables (INFO for production)
- **Log Storage**: Container logs collected by Azure Monitor
- **PII Protection**: Personal Identifiable Information is masked in logs

## Troubleshooting Guide

### Common Issues and Resolutions

1. **Database Connection Issues**:
   - Check PostgreSQL connection string
   - Verify database server status
   - Ensure database user has proper permissions

2. **Redis Connectivity**:
   - Verify Redis host and port configuration
   - Check Redis server status
   - Ensure password is correct if authentication is enabled

3. **User Registration Failures**:
   - Check for duplicate email addresses
   - Verify email service configuration
   - Check password strength requirements

4. **Authentication Problems**:
   - Verify JWT token configuration
   - Check token expiration settings
   - Ensure role assignments are correct

### Logging Locations

- **Application Logs**: `/app/logs/application.log` in container
- **Access Logs**: `/app/logs/access.log` in container
- **Error Logs**: `/app/logs/error.log` in container

## Developer Information

### Repository Structure

- `/src/main/java/com/skishop/user`: Java source code
  - `/controller`: REST API controllers
  - `/service`: Business logic services
  - `/repository`: Data repositories
  - `/entity`: JPA entities
  - `/dto`: Data transfer objects
  - `/event`: Event publishing and handling
  - `/config`: Configuration classes
  - `/security`: Security-related components
  - `/exception`: Custom exceptions
- `/src/main/resources`: Configuration files
- `/src/test`: Test cases
- `/docker`: Docker related files

### Development Workflow

1. Fork and clone the repository
2. Set up local development environment
3. Make changes and add tests
4. Run local tests
5. Submit pull request

### Contribution Guidelines

- Follow Java code style guidelines
- Write unit and integration tests
- Document API changes
- Update README.md for significant changes

## Change History

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2025-07-04 | Initial release with user registration, profile management, and role-based authorization |
