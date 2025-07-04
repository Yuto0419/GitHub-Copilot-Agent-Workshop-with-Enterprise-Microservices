# Sales Management Service

A comprehensive microservice for managing order processing, sales analysis, return/exchange processing, shipping, and sales history management for the Ski Shop E-commerce Platform.

## Main Features Provided

### Feature List

- **Order Management**: Complete order lifecycle management including creation, status updates, and cancellation
- **Shipment Management**: Shipping arrangement, tracking, and delivery status updates
- **Return Processing**: Return request handling, approval, and refund processing
- **Sales Analytics**: Sales data analysis, reporting, and visualization
- **Report Generation**: Export sales reports in various formats (Excel, PDF)
- **Event-Driven Integration**: Seamless integration with other microservices via Kafka events

## Service Endpoints

### Order Management API

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| GET | `/api/v1/orders/{orderId}` | Get order details | Authenticated |
| GET | `/api/v1/orders/number/{orderNumber}` | Get order by number | Authenticated |
| GET | `/api/v1/orders/customer/{customerId}` | Get customer order history | Authenticated |
| POST | `/api/v1/orders` | Create order | Authenticated |
| PUT | `/api/v1/orders/{orderId}/status` | Update order status | Authenticated (Admin) |
| PUT | `/api/v1/orders/{orderId}/cancel` | Cancel order | Authenticated |
| GET | `/api/v1/orders/search` | Search orders | Authenticated (Admin) |

### Shipment Management API

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| GET | `/api/v1/shipments` | Get shipment list | Authenticated (Admin) |
| GET | `/api/v1/shipments/{id}` | Get shipment details | Authenticated |
| POST | `/api/v1/shipments` | Create shipment | Authenticated (Admin) |
| PUT | `/api/v1/shipments/{id}/status` | Update shipment status | Authenticated (Admin) |
| GET | `/api/v1/shipments/order/{orderId}` | Get order shipment info | Authenticated |
| PUT | `/api/v1/shipments/{id}/tracking` | Update tracking info | Authenticated (Admin) |

### Return Management API

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| GET | `/api/v1/returns` | Get return list | Authenticated (Admin) |
| GET | `/api/v1/returns/{id}` | Get return details | Authenticated |
| POST | `/api/v1/returns` | Create return request | Authenticated |
| PUT | `/api/v1/returns/{id}/status` | Update return status | Authenticated (Admin) |
| GET | `/api/v1/returns/order/{orderId}` | Get order return info | Authenticated |

### Reports API

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| GET | `/api/v1/reports/sales` | Get sales report | Authenticated (Admin) |
| GET | `/api/v1/reports/products` | Get product sales report | Authenticated (Admin) |
| GET | `/api/v1/reports/export/sales` | Export sales report | Authenticated (Admin) |
| GET | `/api/v1/reports/shipping` | Get shipping report | Authenticated (Admin) |
| GET | `/api/v1/reports/returns` | Get return analysis report | Authenticated (Admin) |

## Technology Stack

- **Java**: 21 LTS
- **Framework**: Spring Boot 3.5.3
- **Build Tool**: Maven 3.9.x
- **Database**: PostgreSQL 16
- **Cache**: Redis 7.2+
- **Message Queue**: Apache Kafka 7.4.0
- **Search Engine**: Elasticsearch 8.12
- **ORM**: Hibernate/JPA with QueryDSL
- **API Documentation**: SpringDoc OpenAPI 2.3.0
- **Monitoring**: Micrometer with Prometheus
- **Cloud Platform**: Azure Container Apps
- **Report Generation**: Apache POI 5.4.0, iTextPDF 5.5.13.3
- **Testing**: JUnit 5, TestContainers 1.19.3, Spring Boot Test

## Environment Variables

| Variable Name | Description | Default Value |
|---------------|-------------|---------------|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/skishop_sales` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database password | `password` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka bootstrap servers | `localhost:9092` |
| `SPRING_REDIS_HOST` | Redis host | `localhost` |
| `SPRING_REDIS_PORT` | Redis port | `6379` |
| `SPRING_ELASTICSEARCH_URIS` | Elasticsearch URI | `http://localhost:9200` |
| `APP_ORDER_EXPIRY_HOURS` | Order expiry time in hours | `24` |
| `APP_SHIPPING_FREE_SHIPPING_THRESHOLD` | Free shipping order value threshold | `5000` |
| `APP_SHIPPING_DEFAULT_SHIPPING_FEE` | Default shipping fee | `500` |
| `APP_RETURN_ALLOWED_DAYS` | Return period in days | `30` |
| `AZURE_CLIENT_ID` | Azure managed identity client ID | - |
| `AZURE_TENANT_ID` | Azure tenant ID | - |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |

### Profiles

- **dev**: Local development environment (default settings for localhost)
- **test**: Testing environment (embedded database, mocked external services)
- **prod**: Production environment (Azure services enabled)

Profile settings are configured in `application.yml` and `application-{profile}.yml` files.

## Local Development Environment Setup and Verification

### Local Compilation and Execution

1. **Install Dependencies**

```bash
cd sales-management-service
mvn clean install
```

1. **Configure Environment Variables**

Create a `.env` file or set environment variables:

```bash
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/skishop_sales"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="password"
export SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
```

1. **Start Required Services**

Start PostgreSQL, Redis, and Kafka using the provided docker-compose file:

```bash
docker-compose up -d
```

1. **Verify Database Setup**

```bash
# Check if PostgreSQL is ready
docker exec -it sales-postgres pg_isready -U postgres -d skishop_sales

# Connect to PostgreSQL to verify database exists
docker exec -it sales-postgres psql -U postgres -d skishop_sales -c "\dt"

# If database doesn't exist, create it
docker exec -it sales-postgres psql -U postgres -c "CREATE DATABASE skishop_sales;"
```

1. **Start Application**

```bash
mvn spring-boot:run
```

6. **Access via Browser**

- **Swagger UI**: [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)
- **API Documentation**: [http://localhost:8083/api-docs](http://localhost:8083/api-docs)
- **Health Check**: [http://localhost:8083/actuator/health](http://localhost:8083/actuator/health)

### Run Microservices with Docker Compose

1. **Build and Start Containers**

```bash
docker-compose up --build
```

1. **Access via Browser**

- **Sales Management Service**: [http://localhost:8083](http://localhost:8083)
- **Swagger UI**: [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)
- **Kafka UI**: [http://localhost:8080](http://localhost:8080) (if using the included Kafka UI)

## Production Environment Setup and Verification

Primary deployment target: Azure Container Apps
Related external resources: Azure Database for PostgreSQL, Azure Cache for Redis, Azure Event Hubs (Kafka), Azure Container Registry

### 1. Setup Related External Resources

**Azure Database for PostgreSQL**:

```bash
# Create Azure PostgreSQL flexible server
az postgres flexible-server create \
  --name "skishop-sales-db" \
  --resource-group "rg-skishop" \
  --location "East US" \
  --admin-user "dbadmin" \
  --admin-password "SecurePassword123!" \
  --sku-name "Standard_B1ms" \
  --version "16" \
  --storage-size 32 \
  --database-name "skishop_sales"
```

**Azure Cache for Redis**:

```bash
az redis create \
  --name "skishop-sales-redis" \
  --resource-group "rg-skishop" \
  --location "East US" \
  --sku "Basic" \
  --vm-size "C0"
```

**Azure Event Hubs for Kafka**:

```bash
# Create Azure Event Hubs namespace
az eventhubs namespace create \
  --name "skishop-events" \
  --resource-group "rg-skishop" \
  --location "East US" \
  --sku "Standard" \
  --capacity 1 \
  --enable-kafka true

# Create Event Hubs for each topic
az eventhubs eventhub create \
  --name "sales.orders" \
  --namespace-name "skishop-events" \
  --resource-group "rg-skishop" \
  --partition-count 4 \
  --message-retention 1
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
docker build -t sales-management-service:latest .

# Tag the image for ACR
docker tag sales-management-service:latest skishopregistry.azurecr.io/sales-management-service:latest

# Log in to ACR
az acr login --name skishopregistry

# Push the image to ACR
docker push skishopregistry.azurecr.io/sales-management-service:latest
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
  --name "sales-management-service" \
  --resource-group "rg-skishop" \
  --environment "skishop-env" \
  --image "skishopregistry.azurecr.io/sales-management-service:latest" \
  --registry-server "skishopregistry.azurecr.io" \
  --target-port 8083 \
  --ingress "external" \
  --min-replicas 1 \
  --max-replicas 5 \
  --cpu 1.0 \
  --memory 2.0Gi \
  --env-vars \
    "SPRING_PROFILES_ACTIVE=prod" \
    "SPRING_DATASOURCE_URL=jdbc:postgresql://skishop-sales-db.postgres.database.azure.com:5432/skishop_sales?sslmode=require" \
    "SPRING_REDIS_HOST=skishop-sales-redis.redis.cache.windows.net" \
    "SPRING_KAFKA_BOOTSTRAP_SERVERS=skishop-events.servicebus.windows.net:9093"
```

### 5. Set Secrets

```bash
# Add secrets using Azure Key Vault or environment variables
az containerapp secret set \
  --name "sales-management-service" \
  --resource-group "rg-skishop" \
  --secrets \
    "SPRING_DATASOURCE_USERNAME=dbadmin" \
    "SPRING_DATASOURCE_PASSWORD=SecurePassword123!" \
    "SPRING_REDIS_PASSWORD=YourRedisAccessKey"
```

### 6. Verify Deployment

```bash
# Get Container App URL
az containerapp show \
  --name "sales-management-service" \
  --resource-group "rg-skishop" \
  --query properties.configuration.ingress.fqdn \
  --output tsv

# Check health endpoint
curl https://<container-app-url>/actuator/health
```

## Integration with Other Services

### Kafka Event Schema

#### Events Published

- **OrderCreated**: When a new order is created
- **OrderStatusUpdated**: When order status changes
- **ShipmentCreated**: When shipment information is created
- **ShipmentStatusUpdated**: When shipment status is updated
- **ReturnRequested**: When a return is requested
- **ReturnProcessed**: When return processing is completed

#### Events Subscribed

- **InventoryReserved**: From Inventory Management Service
- **InventoryReservationFailed**: From Inventory Management Service
- **PaymentProcessed**: From Payment Cart Service
- **PaymentFailed**: From Payment Cart Service
- **PointsAwarded**: From Point Management Service

### Integration Testing

To test integration with other services locally:

```bash
# Start all required services using docker-compose
docker-compose -f ../docker-compose.yml up -d

# Run integration tests
mvn verify -P integration-test
```

## Monitoring and Troubleshooting

### Health Checks

The service provides health endpoints via Spring Boot Actuator:

- **Overall Health**: [http://localhost:8083/actuator/health](http://localhost:8083/actuator/health)
- **Database Health**: [http://localhost:8083/actuator/health/db](http://localhost:8083/actuator/health/db)
- **Kafka Health**: [http://localhost:8083/actuator/health/kafka](http://localhost:8083/actuator/health/kafka)
- **Redis Health**: [http://localhost:8083/actuator/health/redis](http://localhost:8083/actuator/health/redis)

### Metrics

Prometheus metrics are available at:

- [http://localhost:8083/actuator/prometheus](http://localhost:8083/actuator/prometheus)

### Key Metrics to Monitor

- **order-creation-rate**: Orders created per minute
- **order-processing-time**: Order processing duration
- **payment-success-rate**: Payment processing success rate
- **inventory-check-time**: Inventory check response time
- **saga-completion-rate**: Saga transaction completion rate
- **api-error-rate**: API error rate

### Logs

Application logs can be found:

- **Development**: Console output
- **Docker**: Docker container logs
- **Production**: Azure Container Apps logs or configured log destination

### Common Issues and Solutions

1. **Database Connection Issues**
   - Check PostgreSQL connection details
   - Verify database exists and is accessible
   - Check network connectivity

2. **Kafka Connection Issues**
   - Verify Kafka is running
   - Check topic configuration
   - Ensure consumer group is properly set

3. **Redis Connectivity**
   - Check Redis host, port, and password
   - Verify Redis is running
   - Test connection manually

4. **Order Processing Failures**
   - Check for error logs during Saga transactions
   - Verify inventory service is responding
   - Check payment service integration

## Developer Information

### Directory Structure

```text
sales-management-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/skishop/sales/
│   │   │       ├── controller/          # REST API controllers
│   │   │       │   ├── OrderController.java
│   │   │       │   ├── ShipmentController.java
│   │   │       │   ├── ReturnController.java
│   │   │       │   └── ReportController.java
│   │   │       ├── service/             # Business logic services
│   │   │       │   ├── OrderService.java
│   │   │       │   ├── ShipmentService.java
│   │   │       │   ├── ReturnService.java
│   │   │       │   └── ReportService.java
│   │   │       ├── event/               # Event handling
│   │   │       │   ├── publisher/
│   │   │       │   └── subscriber/
│   │   │       ├── repository/          # Data repositories
│   │   │       ├── entity/              # JPA entities
│   │   │       ├── dto/                 # Data transfer objects
│   │   │       ├── config/              # Configuration classes
│   │   │       ├── exception/           # Custom exceptions
│   │   │       └── SalesManagementApplication.java
│   │   └── resources/
│   │       ├── application.yml          # Main configuration
│   │       ├── application-dev.yml      # Development config
│   │       └── application-prod.yml     # Production config
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

- **v1.0.0** (2023-07-15): Initial release
  - Order management functionality
  - Shipment tracking and management
  - Return processing
  - Reporting capabilities
  - Event-driven architecture integration
  - Saga pattern implementation for distributed transactions
