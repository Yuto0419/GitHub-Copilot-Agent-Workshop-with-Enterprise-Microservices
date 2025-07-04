# Payment Cart Service

Payment and shopping cart management microservice for the Ski Shop E-commerce Platform

## Main Features Provided

### Feature List

- **Cart Management**: Shopping cart lifecycle management with item operations
- **Payment Processing**: Secure payment gateway integration and transaction handling
- **Session Management**: Cart persistence and user session management
- **Payment Gateway Integration**: Multi-provider payment processing (Stripe primary)
- **Transaction Tracking**: Payment status monitoring and history management
- **Refund Processing**: Return and cancellation payment handling
- **Security Compliance**: PCI DSS compliance and data protection
- **Cart Expiration**: Automated cart cleanup and expiration management
- **Event Integration**: Seamless integration with order and inventory services

## Service Endpoints

| HTTP Method | Path | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/v1/cart/items` | Add item to cart | Authenticated |
| PUT | `/api/v1/cart/items/{itemId}` | Update cart item | Authenticated |
| DELETE | `/api/v1/cart/items/{itemId}` | Remove cart item | Authenticated |
| GET | `/api/v1/cart` | Get user cart | Authenticated |
| DELETE | `/api/v1/cart` | Clear cart | Authenticated |
| POST | `/api/v1/payments/intent` | Create payment intent | Authenticated |
| POST | `/api/v1/payments/{paymentId}/process` | Process payment | Authenticated |
| GET | `/api/v1/payments/{paymentId}` | Get payment status | Authenticated |
| GET | `/api/v1/payments/history` | Get payment history | Authenticated |
| POST | `/api/v1/payments/{paymentId}/refund` | Process refund | Authenticated |
| POST | `/api/v1/payments/webhook` | Handle payment webhook | Public |

## Technology Stack

- **Java**: 21 LTS
- **Spring Boot**: 3.5.3
- **Spring Cloud Stream**: Kafka integration for event-driven architecture
- **Database**: PostgreSQL for cart and payment data
- **Cache**: Redis for session management and data caching
- **Payment Gateway**: Stripe API (primary gateway)
- **ORM**: Spring Data JPA with Hibernate
- **API Documentation**: SpringDoc OpenAPI
- **Metrics**: Micrometer with Prometheus
- **Containerization**: Docker
- **Cloud Platform**: Azure Container Apps

## Environment Variables

| Variable Name | Description | Default Value |
|---------------|-------------|---------------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/skishop_payment` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `postgres` |
| `SPRING_DATA_REDIS_HOST` | Redis host | `localhost` |
| `SPRING_DATA_REDIS_PORT` | Redis port | `6379` |
| `SPRING_DATA_REDIS_PASSWORD` | Redis password (if needed) | - |
| `SPRING_CLOUD_STREAM_KAFKA_BINDER_BROKERS` | Kafka broker addresses | `localhost:9092` |
| `STRIPE_API_KEY` | Stripe API secret key | - |
| `STRIPE_WEBHOOK_SECRET` | Stripe webhook signing secret | - |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | JWT issuer URI for authentication | `http://localhost:8080/auth/realms/skishop` |

### Profiles

- **local**: Local development environment (default settings for localhost)
- **test**: Testing environment (in-memory database, mocked payment gateway)
- **production**: Production environment (Azure services enabled)

Profile settings are configured in `application.yml` and `application-{profile}.yml` files.

## Local Development Environment Setup and Verification

### Local Compilation and Execution

1. **Install Dependencies**

```bash
cd payment-cart-service
mvn clean install
```

1. **Configure Environment Variables**

Create a `.env` file or set environment variables:

```bash
export STRIPE_API_KEY="your-stripe-api-key"
export STRIPE_WEBHOOK_SECRET="your-stripe-webhook-secret"
```

1. **Start Required Services**

Start PostgreSQL, Redis, and Kafka:

```bash
# Using docker-compose for dependent services
docker-compose up -d postgres redis zookeeper kafka
```

1. **Start Application**

```bash
mvn spring-boot:run
```

1. **Access via Browser**

- **Swagger UI**: [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html)
- **API Documentation**: [http://localhost:8084/v3/api-docs](http://localhost:8084/v3/api-docs)
- **Health Check**: [http://localhost:8084/actuator/health](http://localhost:8084/actuator/health)

### Run Microservices with Docker Compose

1. **Build and Start Containers**

```bash
# Start all services including the application
docker-compose up --build --profile full-stack

# Start monitoring tools (Prometheus & Grafana)
docker-compose up --profile monitoring
```

1. **Access via Browser**

- **Payment Cart Service**: [http://localhost:8084](http://localhost:8084)
- **Swagger UI**: [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html)
- **Prometheus**: [http://localhost:9090](http://localhost:9090)
- **Grafana**: [http://localhost:3000](http://localhost:3000) (admin/admin)

## Production Environment Setup and Verification

Primary deployment target: Azure Container Apps
Related external resources: Azure Database for PostgreSQL, Azure Cache for Redis, Azure Service Bus

### 1. Setup Related External Resources

**Azure Database for PostgreSQL**:

```bash
# Create PostgreSQL server
az postgres server create \
  --name ski-shop-payment-db \
  --resource-group rg-ski-shop \
  --location eastus \
  --admin-user postgresadmin \
  --admin-password <your-secure-password> \
  --sku-name GP_Gen5_2

# Create database
az postgres db create \
  --name skishop_payment \
  --server-name ski-shop-payment-db \
  --resource-group rg-ski-shop
```

**Azure Cache for Redis**:

```bash
az redis create \
  --name ski-shop-payment-redis \
  --resource-group rg-ski-shop \
  --location eastus \
  --sku Standard \
  --vm-size C1
```

**Azure Service Bus**:

```bash
# Create Service Bus namespace
az servicebus namespace create \
  --name ski-shop-servicebus \
  --resource-group rg-ski-shop \
  --location eastus \
  --sku Standard

# Create topics for cart and payment events
az servicebus topic create \
  --name cart.items \
  --namespace-name ski-shop-servicebus \
  --resource-group rg-ski-shop

az servicebus topic create \
  --name payment.intents \
  --namespace-name ski-shop-servicebus \
  --resource-group rg-ski-shop
```

### 2. Azure Container Apps Environment Setup and Deployment

```bash
# Set environment variables
export AZURE_SUBSCRIPTION_ID="your-subscription-id"
export AZURE_LOCATION="eastus"
export AZURE_ENV_NAME="payment-cart-prod"

# Initialize Azure Developer CLI
azd init --template payment-cart-service

# Set secrets
azd env set SPRING_DATASOURCE_PASSWORD "your-db-password" --secret
azd env set STRIPE_API_KEY "your-stripe-api-key" --secret
azd env set STRIPE_WEBHOOK_SECRET "your-stripe-webhook-secret" --secret

# Deploy infrastructure and application
azd up
```

### 3. Access Azure Container Apps Instance via Browser

After deployment, access the service at the provided Azure Container Apps URL:

- **Service URL**: [https://payment-cart-prod.internal.azurecontainerapps.io](https://payment-cart-prod.internal.azurecontainerapps.io)
- **Health Check**: [https://payment-cart-prod.internal.azurecontainerapps.io/actuator/health](https://payment-cart-prod.internal.azurecontainerapps.io/actuator/health)

### 4. Azure CLI Environment Setup Script

```bash
#!/bin/bash
# setup-azure-environment.sh

# Variables
RESOURCE_GROUP="rg-ski-shop-payment"
LOCATION="eastus"
APP_NAME="payment-cart-service"
CONTAINER_REGISTRY="acrskishop"
DB_SERVER="ski-shop-payment-db"
REDIS_CACHE="ski-shop-payment-redis"

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
  --image "$CONTAINER_REGISTRY.azurecr.io/payment-cart-service:latest" \
  --target-port 8084 \
  --ingress 'external' \
  --env-vars \
    SPRING_PROFILES_ACTIVE="production" \
    SPRING_DATASOURCE_URL="jdbc:postgresql://$DB_SERVER.postgres.database.azure.com:5432/skishop_payment"

echo "Deployment completed. Check the Azure portal for the application URL."
```

## Service Feature Verification Methods

### curl Commands for Testing and Expected Results

### 1. Health Check

```bash
curl -X GET http://localhost:8084/actuator/health

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

### 2. Add Item to Cart

```bash
curl -X POST http://localhost:8084/api/v1/cart/items \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-jwt-token" \
  -d '{
    "productId": "550e8400-e29b-41d4-a716-446655440000",
    "quantity": 2,
    "productDetails": {
      "name": "Ski Jacket",
      "size": "L",
      "color": "Blue"
    }
  }'

# Expected Response:
{
  "success": true,
  "message": "Item added to cart successfully",
  "data": {
    "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "userId": "user-123",
    "items": [
      {
        "id": "cart-item-uuid",
        "productId": "550e8400-e29b-41d4-a716-446655440000",
        "quantity": 2,
        "unitPrice": 15000,
        "totalPrice": 30000,
        "productDetails": {
          "name": "Ski Jacket",
          "size": "L",
          "color": "Blue"
        }
      }
    ],
    "totalAmount": 30000,
    "currency": "JPY",
    "itemCount": 1
  }
}
```

### 3. Create Payment Intent

```bash
curl -X POST http://localhost:8084/api/v1/payments/intent \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-jwt-token" \
  -d '{
    "cartId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "paymentMethod": "card",
    "currency": "JPY"
  }'

# Expected Response:
{
  "success": true,
  "message": "Payment intent created successfully",
  "data": {
    "paymentId": "payment-uuid",
    "clientSecret": "pi_xxx_secret_xxx",
    "amount": 30000,
    "currency": "JPY",
    "status": "PENDING"
  }
}
```

## Integration with Other Microservices

### Event-Based Integration

The Payment Cart Service integrates with other microservices through Kafka/Service Bus events:

- **Published Events**:
  - CartItemAdded, CartItemUpdated, CartItemRemoved, CartCleared
  - PaymentIntentCreated, PaymentProcessed, PaymentFailed, RefundProcessed

- **Subscribed Events**:
  - UserRegistered (from User Management Service)
  - InventoryUpdated, ProductPriceUpdated (from Inventory Management Service)
  - OrderCompleted (from Sales Management Service)

### Direct API Integration

- **API Gateway**: All endpoints are exposed through the API Gateway
- **Authentication Service**: JWT token validation for secured endpoints
- **Inventory Management**: Product availability verification before adding to cart
- **Sales Management**: Order creation after successful payment

## Monitoring and Observability

### Health Checks and Metrics

- **Health Endpoints**: `/actuator/health` for service health status
- **Metrics Endpoints**: `/actuator/prometheus` for Prometheus metrics scraping
- **Info Endpoint**: `/actuator/info` for application information

### Logging Configuration

- **Log Format**: JSON structured logging for better parsing
- **Log Levels**: Configurable through environment variables
- **Log Storage**: Container logs collected by Azure Monitor

### Alerting

- **Payment Failures**: Alerts on high payment failure rate
- **System Health**: Alerts on service unavailability
- **Performance**: Alerts on high response time

## Troubleshooting Guide

### Common Issues and Resolutions

1. **Payment Gateway Connection Issues**:
   - Check Stripe API key validity
   - Verify network connectivity to Stripe APIs
   - Inspect gateway response logs for errors

2. **Database Connection Problems**:
   - Verify PostgreSQL connection string
   - Check database server status
   - Ensure database user has proper permissions

3. **Kafka/Redis Connectivity**:
   - Verify broker addresses and connectivity
   - Check authentication settings if applicable
   - Inspect connection pool settings

### Logging Locations

- **Application Logs**: `/app/logs/application.log` in container
- **Access Logs**: `/app/logs/access.log` in container
- **Error Logs**: `/app/logs/error.log` in container

## Developer Information

### Repository Structure

- `/src/main/java/com/skishop/payment`: Java source code
- `/src/main/resources`: Configuration files
- `/src/test`: Test cases
- `/docker`: Docker related files
- `/monitoring`: Prometheus and Grafana configuration

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
| 1.0.0 | 2025-07-04 | Initial release with cart and payment functionality |
