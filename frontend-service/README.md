# Frontend Service

Web UI interface for Ski Shop E-commerce Platform

## Main Features Provided

### Feature List

- **Responsive Web Interface**: Modern responsive web UI using Bootstrap 5
- **User Authentication**: User login, registration, and profile management
- **Product Browsing**: Browse, search, and filter products by category and attributes
- **Shopping Cart**: Add, update, and remove products in shopping cart
- **Checkout Process**: Complete order placement with payment integration
- **Order Management**: View order history and track order status
- **AI Chat Integration**: Interactive chat support with product recommendations
- **User Profile**: Manage personal information, addresses, and preferences
- **Admin Dashboard**: Administrative interface for inventory and order management

## Service Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| GET | `/` | Home page | Public |
| GET | `/products` | Product list page | Public |
| GET | `/products/{id}` | Product detail page | Public |
| GET | `/cart` | Shopping cart page | Public |
| POST | `/cart/add` | Add product to cart | Public |
| PUT | `/cart/update` | Update cart item | Public |
| DELETE | `/cart/remove` | Remove cart item | Public |
| GET | `/checkout` | Checkout page | Authenticated |
| POST | `/checkout` | Process order | Authenticated |
| GET | `/profile` | User profile page | Authenticated |
| GET | `/orders` | Order history | Authenticated |
| GET | `/orders/{id}` | Order detail | Authenticated |
| GET | `/chat` | AI chat interface | Public |
| POST | `/api/chat/send` | Send chat message | Public |
| GET | `/admin` | Admin dashboard | Admin role |
| GET | `/admin/products` | Product management | Admin role |
| GET | `/admin/orders` | Order management | Admin role |
| PUT | `/admin/orders/{id}/status` | Update order status | Admin role |
| GET | `/admin/customers` | Customer management | Admin role |
| GET | `/admin/reports` | Reports page | Admin role |

## Technology Stack

- **Java**: 21 LTS
- **Spring Boot**: 3.5.3
- **Thymeleaf**: Server-side templating engine
- **Bootstrap**: 5.3.2 (WebJars)
- **jQuery**: 3.7.1 (WebJars)
- **Font Awesome**: 6.4.0 (WebJars)
- **Spring Security**: OAuth2 client integration
- **Spring WebFlux**: WebClient for API calls
- **Caffeine**: High-performance caching
- **Apache HttpClient**: HTTP client for API calls
- **Docker**: Container deployment
- **Cloud Platform**: Azure Container Apps

## Environment Variables

| Variable Name | Description | Default Value |
|---------------|-------------|---------------|
| `SPRING_PROFILES_ACTIVE` | Spring Boot active profile | `local` |
| `API_GATEWAY_URL` | API Gateway service URL | `http://api-gateway:8090` |
| `SKISHOP_AUTH_ENABLED` | Enable/disable authentication | `false` |
| `AZURE_CLIENT_ID` | Azure Client ID for OAuth | - |
| `AZURE_CLIENT_SECRET` | Azure Client Secret for OAuth | - |
| `AZURE_TENANT_ID` | Azure Tenant ID for OAuth | - |
| `REDIS_HOST` | Redis host for session storage | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `CACHE_TTL_PRODUCTS` | Cache TTL for products (seconds) | `300` |
| `CACHE_TTL_CATEGORIES` | Cache TTL for categories (seconds) | `1800` |
| `SESSION_TIMEOUT` | Session timeout in seconds | `1800` |
| `LOG_LEVEL_ROOT` | Root logger level | `INFO` |
| `LOG_LEVEL_SKISHOP` | Application logger level | `DEBUG` |

### Profiles

- **local**: Local development environment (default settings for localhost)
- **test**: Testing environment (mocked external services)
- **production**: Production environment (Azure services enabled)

Profile settings are configured in `application.yml` and `application-{profile}.yml` files.

## Local Development Environment Setup and Verification

### Local Compilation and Execution

1. **Install Dependencies**

```bash
cd frontend-service
mvn clean install
```

1. **Configure Environment Variables**

Create a `.env` file or set environment variables:

```bash
export API_GATEWAY_URL="http://localhost:8090"
export SKISHOP_AUTH_ENABLED=false
```

1. **Start Required Services**

Start API Gateway service or use the provided mock:

```bash
# Start with docker-compose
docker-compose up -d api-gateway
```

1. **Start Application**

```bash
mvn spring-boot:run
```

1. **Access via Browser**

- **Frontend UI**: [http://localhost:3000](http://localhost:3000)
- **Swagger UI**: [http://localhost:3000/swagger-ui.html](http://localhost:3000/swagger-ui.html)
- **Health Check**: [http://localhost:3000/actuator/health](http://localhost:3000/actuator/health)

### Run Microservices with Docker Compose

1. **Build and Start Containers**

```bash
docker-compose up --build
```

1. **Access via Browser**

- **Frontend Service**: [http://localhost:3000](http://localhost:3000)
- **Swagger UI**: [http://localhost:3000/swagger-ui.html](http://localhost:3000/swagger-ui.html)

## Production Environment Setup and Verification

Primary deployment target: Azure Container Apps
Related external resources: Azure Entra ID, Azure Application Insights, Azure Redis Cache

### 1. Setup Related External Resources

**Azure Entra ID**:

```bash
# Create Azure Entra ID App Registration
az ad app create --display-name "SkiShop Frontend" \
  --sign-in-audience "AzureADMyOrg" \
  --web-redirect-uris "https://frontend-prod.internal.azurecontainerapps.io/login/oauth2/code/azure"

# Create a service principal for the app
az ad sp create --id <app-id>

# Create a client secret
az ad app credential reset --id <app-id> --credential-description "SkiShop Secret"
```

**Azure Redis Cache**:

```bash
az redis create \
  --name "ski-shop-redis" \
  --resource-group "rg-ski-shop" \
  --location "East US" \
  --sku "Standard" \
  --vm-size "C1"
```

### 2. Azure Container Apps Environment Setup and Deployment

```bash
# Set environment variables
export AZURE_SUBSCRIPTION_ID="your-subscription-id"
export AZURE_LOCATION="eastus"
export AZURE_ENV_NAME="frontend-prod"

# Initialize Azure Developer CLI
azd init --template frontend-service

# Set secrets
azd env set AZURE_CLIENT_ID "your-azure-client-id" --secret
azd env set AZURE_CLIENT_SECRET "your-azure-client-secret" --secret
azd env set AZURE_TENANT_ID "your-azure-tenant-id" --secret
azd env set API_GATEWAY_URL "https://api-gateway-prod.internal.azurecontainerapps.io"
azd env set SKISHOP_AUTH_ENABLED "true"

# Deploy infrastructure and application
azd up
```

### 3. Access Azure Container Apps Instance via Browser

After deployment, access the service at the provided Azure Container Apps URL:

- **Service URL**: [https://frontend-prod.internal.azurecontainerapps.io](https://frontend-prod.internal.azurecontainerapps.io)
- **Health Check**: [https://frontend-prod.internal.azurecontainerapps.io/actuator/health](https://frontend-prod.internal.azurecontainerapps.io/actuator/health)

### 4. Azure CLI Environment Setup Script

```bash
#!/bin/bash
# setup-azure-environment.sh

# Variables
RESOURCE_GROUP="rg-ski-shop-frontend"
LOCATION="eastus"
APP_NAME="frontend-service"
CONTAINER_REGISTRY="acrskishop"
APP_INSIGHTS_NAME="ski-shop-insights"

# Create resource group
az group create --name $RESOURCE_GROUP --location $LOCATION

# Create Container Registry
az acr create \
  --name $CONTAINER_REGISTRY \
  --resource-group $RESOURCE_GROUP \
  --sku "Standard" \
  --location $LOCATION

# Create Application Insights
az monitor app-insights component create \
  --app $APP_INSIGHTS_NAME \
  --resource-group $RESOURCE_GROUP \
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
  --image "$CONTAINER_REGISTRY.azurecr.io/frontend-service:latest" \
  --target-port 3000 \
  --ingress 'external' \
  --env-vars \
    SPRING_PROFILES_ACTIVE="production" \
    API_GATEWAY_URL="https://api-gateway-prod.internal.azurecontainerapps.io" \
    SKISHOP_AUTH_ENABLED="true"

echo "Deployment completed. Check the Azure portal for the application URL."
```

## Service Feature Verification Methods

### Browser Testing

### 1. Home Page Access

1. Open a web browser and navigate to [http://localhost:3000](http://localhost:3000)
2. Verify the home page loads with product categories and featured products
3. Verify the navigation menu functions correctly

### 2. Product Browsing

1. Navigate to [http://localhost:3000/products](http://localhost:3000/products)
2. Verify products are displayed in a grid format
3. Test filtering by category and sorting options
4. Test search functionality with various queries

### 3. Shopping Cart

1. Navigate to a product detail page
2. Add the product to cart
3. Verify the cart counter updates
4. Navigate to cart page and verify the item is displayed
5. Test quantity adjustment and remove functionality

### 4. User Authentication

1. Click on "Login" button
2. Test login with valid credentials
3. Test login with invalid credentials
4. Test registration process
5. Verify authentication state persists across pages

## Integration with Other Microservices

This service integrates with the following backend APIs:

- **API Gateway**: Central entry point for all backend services
- **Authentication Service**: User authentication and authorization
- **User Management Service**: User profile and preferences  
- **Inventory Management Service**: Product information and availability
- **Sales Management Service**: Order history and purchase patterns
- **Payment Cart Service**: Shopping cart operations
- **Point Service**: User point balance and transactions
- **Coupon Service**: Available promotions and discount codes
- **AI Support Service**: AI-powered chat and recommendations

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

Response includes connectivity status to backend services.

### Metrics

```url
GET /actuator/metrics
GET /actuator/prometheus
```

Available metrics:

- Page view counts
- Session duration
- API call latency
- Error rates by endpoint
- Cache hit ratio

### Log Levels

- **Development environment**: DEBUG
- **Production environment**: INFO

Key loggers:

- `com.skishop.frontend`: Frontend service operations
- `org.springframework.web`: Web request handling
- `org.springframework.security`: Authentication and authorization

## Troubleshooting

### Common Issues

### 1. API Gateway Connection Failure

```text
ERROR: Failed to connect to API Gateway
Solution: Check API_GATEWAY_URL environment variable and ensure the API Gateway service is running
```

### 2. Authentication Issues

```text
ERROR: OAuth2 authentication failure
Solution: Verify AZURE_CLIENT_ID, AZURE_CLIENT_SECRET, and AZURE_TENANT_ID environment variables
```

### 3. Template Rendering Error

```text
ERROR: Error rendering Thymeleaf template
Solution: Check for template syntax errors, restart with devtools to refresh templates
```

### 4. Session Management Issues

```text
ERROR: Session persistence failure
Solution: Verify Redis connection settings and ensure Redis is running
```

## Developer Information

### Directory Structure

```text
frontend-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/skishop/frontend/
│   │   │       ├── controller/          # Web controllers
│   │   │       │   ├── HomeController.java
│   │   │       │   ├── ProductController.java
│   │   │       │   ├── CartController.java
│   │   │       │   ├── CheckoutController.java
│   │   │       │   ├── ProfileController.java
│   │   │       │   └── AdminController.java
│   │   │       ├── service/             # Business logic services
│   │   │       │   ├── ProductService.java
│   │   │       │   ├── CartService.java
│   │   │       │   ├── OrderService.java
│   │   │       │   ├── UserService.java
│   │   │       │   └── ChatService.java
│   │   │       ├── client/              # API clients
│   │   │       │   ├── ApiGatewayClient.java
│   │   │       │   ├── ProductClient.java
│   │   │       │   ├── CartClient.java
│   │   │       │   ├── OrderClient.java
│   │   │       │   └── ChatClient.java
│   │   │       ├── dto/                 # Data transfer objects
│   │   │       ├── config/              # Configuration classes
│   │   │       │   ├── WebClientConfig.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── CacheConfig.java
│   │   │       │   └── WebConfig.java
│   │   │       └── FrontendServiceApplication.java
│   │   └── resources/
│   │       ├── templates/               # Thymeleaf templates
│   │       │   ├── fragments/           # Reusable fragments
│   │       │   ├── layout/              # Layout templates
│   │       │   ├── home/                # Home page templates
│   │       │   ├── product/             # Product related templates
│   │       │   ├── cart/                # Cart related templates
│   │       │   ├── checkout/            # Checkout related templates
│   │       │   ├── user/                # User related templates
│   │       │   ├── admin/               # Admin related templates
│   │       │   └── error/               # Error page templates
│   │       ├── static/                  # Static resources
│   │       │   ├── css/                 # CSS files
│   │       │   ├── js/                  # JavaScript files
│   │       │   ├── images/              # Image files
│   │       │   └── favicon.ico          # Favicon
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
  - Basic product browsing and cart functionality
  - User authentication with Azure Entra ID
  - Integration with backend microservices
  - Responsive UI with Bootstrap 5
