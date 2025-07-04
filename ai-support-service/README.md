# AI Support Service

AI-powered support features for Ski Shop E-commerce Platform

## Main Features Provided

### Feature List

- **AI Chat Support**: Intelligent chatbot customer service using Azure OpenAI and LangChain4j
- **Product Recommendations**: Personalized product recommendations based on user behavior and preferences
- **Semantic Search**: Enhanced search functionality with natural language processing
- **Search Enhancement**: Automatic query improvement and autocomplete suggestions
- **Visual Search**: Product search capabilities using images
- **Analytics & Insights**: User behavior analytics and recommendation explanations

## Service Endpoints

| HTTP Method | Endpoint | Description | Authorization |
|-------------|----------|-------------|---------------|
| POST | `/api/v1/chat/message` | Send chat message | Authenticated |
| POST | `/api/v1/chat/recommend` | Chat recommendation | Authenticated |
| POST | `/api/v1/chat/advice` | Chat advice | Authenticated |
| GET | `/api/v1/chat/conversations/{userId}` | Get conversation history | Authenticated |
| DELETE | `/api/v1/chat/conversations/{conversationId}` | Delete conversation | Authenticated |
| POST | `/api/v1/chat/feedback` | Chat feedback | Authenticated |
| GET | `/api/v1/recommendations/{userId}` | Get user recommendations | Authenticated |
| GET | `/api/v1/recommendations/similar/{productId}` | Similar product recommendations | Public |
| GET | `/api/v1/recommendations/trending` | Trending products | Public |
| GET | `/api/v1/recommendations/category/{category}` | Category recommendations | Public |
| POST | `/api/v1/recommendations/feedback` | Recommendation feedback | Authenticated |
| GET | `/api/v1/recommendations/explain/{userId}/{productId}` | Explain recommendation | Authenticated |
| POST | `/api/v1/search/semantic` | Semantic search | Public |
| GET | `/api/v1/search/autocomplete` | Autocomplete | Public |
| GET | `/api/v1/search/suggest` | Search suggestions | Public |
| POST | `/api/v1/search/visual` | Visual search | Public |

## Technology Stack

- **Java**: 21 LTS
- **Spring Boot**: 3.5.3
- **LangChain4j**: 1.1.0 (Azure OpenAI integration)
- **Azure OpenAI Service**: GPT-4o, GPT-3.5-turbo, text-embedding-3-small
- **Database**: MongoDB (conversation history, user preferences)
- **Cache**: Redis (recommendation caching, session management)
- **Vector Database**: Pinecone or Azure Cognitive Search
- **Event Streaming**: Apache Kafka
- **Containerization**: Docker
- **Cloud Platform**: Azure Container Apps

## Environment Variables

| Variable Name | Description | Default Value |
|---------------|-------------|---------------|
| `AZURE_OPENAI_KEY` | Azure OpenAI API key | - |
| `AZURE_OPENAI_ENDPOINT` | Azure OpenAI endpoint URL | `https://your-resource.openai.azure.com/` |
| `AZURE_OPENAI_DEPLOYMENT_NAME` | Chat model deployment name | `gpt-4o` |
| `AZURE_OPENAI_EMBEDDING_DEPLOYMENT_NAME` | Embedding model deployment name | `text-embedding-3-small` |
| `MONGODB_CONNECTION_STRING` | MongoDB connection string | `mongodb://localhost:27017/ai_support_db` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis password | - |
| `KAFKA_BROKERS` | Kafka broker addresses | `localhost:9092` |
| `JWT_ISSUER_URI` | JWT issuer URI for authentication | `http://localhost:8080/realms/skishop` |
| `PINECONE_API_KEY` | Pinecone vector database API key | - |
| `PINECONE_INDEX_NAME` | Pinecone index name | `ski-shop-products` |
| `AZURE_SEARCH_ENDPOINT` | Azure Cognitive Search endpoint | - |
| `AZURE_SEARCH_API_KEY` | Azure Cognitive Search API key | - |

### Profiles

- **local**: Local development environment (default settings for localhost)
- **test**: Testing environment (mocked external services)
- **production**: Production environment (Azure services enabled)

Profile settings are configured in `application.yml` and `application-{profile}.yml` files.

## Local Development Environment Setup and Verification

### Local Compilation and Execution

1. **Install Dependencies**

```bash
cd ai-support-service
mvn clean install
```

1. **Configure Environment Variables**

Create a `.env` file or set environment variables:

```bash
export AZURE_OPENAI_KEY="your-azure-openai-key"
export AZURE_OPENAI_ENDPOINT="https://your-resource.openai.azure.com/"
export MONGODB_CONNECTION_STRING="mongodb://localhost:27017/ai_support_db"
```

1. **Start Required Services**

Start MongoDB and Redis:

```bash
# MongoDB
docker run -d --name mongodb -p 27017:27017 mongo:latest

# Redis
docker run -d --name redis -p 6379:6379 redis:latest
```

1. **Start Application**

```bash
mvn spring-boot:run
```

1. **Access via Browser**

- **Swagger UI**: [http://localhost:8089/swagger-ui.html](http://localhost:8089/swagger-ui.html)
- **API Documentation**: [http://localhost:8089/api-docs](http://localhost:8089/api-docs)
- **Health Check**: [http://localhost:8089/actuator/health](http://localhost:8089/actuator/health)

### Run Microservices with Docker Compose

1. **Build and Start Containers**

```bash
docker-compose up --build
```

1    . **Access via Browser**

- **AI Support Service**: [http://localhost:8089](http://localhost:8089)
- **Swagger UI**: [http://localhost:8089/swagger-ui.html](http://localhost:8089/swagger-ui.html)

## Production Environment Setup and Verification

Primary deployment target: Azure Container Apps
Related external resources: Azure OpenAI Service, Azure Cognitive Search, MongoDB Atlas, Azure Redis Cache, Azure Entra ID

### 1. Setup Related External Resources

**Azure OpenAI Service**:

```bash
# Create Azure OpenAI resource
az cognitiveservices account create \
  --name "ski-shop-openai" \
  --resource-group "rg-ski-shop" \
  --kind "OpenAI" \
  --sku "S0" \
  --location "East US"

# Deploy models
az cognitiveservices account deployment create \
  --name "ski-shop-openai" \
  --resource-group "rg-ski-shop" \
  --deployment-name "gpt-4o" \
  --model-name "gpt-4o" \
  --model-version "2024-05-13" \
  --model-format "OpenAI" \
  --sku-capacity 10 \
  --sku-name "Standard"
```

**MongoDB Atlas**:

- Create MongoDB Atlas cluster
- Configure network access and database users
- Get connection string

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
export AZURE_ENV_NAME="ai-support-prod"

# Initialize Azure Developer CLI
azd init --template ai-support-service

# Set secrets
azd env set MONGODB_CONNECTION_STRING "your-mongodb-connection-string" --secret
azd env set AZURE_OPENAI_KEY "your-openai-key" --secret
azd env set AZURE_OPENAI_ENDPOINT "https://your-openai-endpoint.openai.azure.com/"

# Deploy infrastructure and application
azd up
```

### 3. Access Azure Container Apps Instance via Browser

After deployment, access the service at the provided Azure Container Apps URL:

- **Service URL**: [https://ai-support-prod.internal.azurecontainerapps.io](https://ai-support-prod.internal.azurecontainerapps.io)
- **Health Check**: [https://ai-support-prod.internal.azurecontainerapps.io/actuator/health](https://ai-support-prod.internal.azurecontainerapps.io/actuator/health)

### 4. Azure CLI Environment Setup Script

```bash
#!/bin/bash
# setup-azure-environment.sh

# Variables
RESOURCE_GROUP="rg-ski-shop-ai"
LOCATION="eastus"
APP_NAME="ai-support-service"
CONTAINER_REGISTRY="acrskishop"
OPENAI_ACCOUNT_NAME="ski-shop-openai"

# Create resource group
az group create --name $RESOURCE_GROUP --location $LOCATION

# Create Azure OpenAI
az cognitiveservices account create \
  --name $OPENAI_ACCOUNT_NAME \
  --resource-group $RESOURCE_GROUP \
  --kind "OpenAI" \
  --sku "S0" \
  --location $LOCATION

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
  --image "$CONTAINER_REGISTRY.azurecr.io/ai-support-service:latest" \
  --target-port 8089 \
  --ingress 'external' \
  --env-vars \
    AZURE_OPENAI_ENDPOINT="https://$OPENAI_ACCOUNT_NAME.openai.azure.com/" \
    AZURE_OPENAI_DEPLOYMENT_NAME="gpt-4o"

echo "Deployment completed. Check the Azure portal for the application URL."
```

## Service Feature Verification Methods

### curl Commands for Testing and Expected Results

### 1. Health Check

```bash
curl -X GET http://localhost:8089/actuator/health

# Expected Response:
{
  "status": "UP",
  "components": {
    "diskSpace": {"status": "UP"},
    "mongo": {"status": "UP"},
    "redis": {"status": "UP"}
  }
}
```

### 2. Chat Message

```bash
curl -X POST http://localhost:8089/api/v1/chat/message \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-jwt-token" \
  -d '{
    "userId": "user123",
    "message": "I need help choosing ski equipment",
    "conversationId": "conv123"
  }'

# Expected Response:
{
  "conversationId": "conv123",
  "messageId": "msg456",
  "response": "I'd be happy to help you choose ski equipment! What's your skiing experience level?",
  "timestamp": "2025-07-04T10:30:00Z",
  "confidence": 0.95
}
```

### 3. Product Recommendations

```bash
curl -X GET http://localhost:8089/api/v1/recommendations/user123 \
  -H "Authorization: Bearer your-jwt-token"

# Expected Response:
{
  "userId": "user123",
  "recommendations": [
    {
      "productId": "ski-001",
      "name": "Rossignol Experience 88 Ti",
      "score": 0.92,
      "reason": "Based on your previous purchases and skiing level"
    }
  ],
  "generatedAt": "2025-07-04T10:30:00Z"
}
```

### 4. Semantic Search

```bash
curl -X POST http://localhost:8089/api/v1/search/semantic \
  -H "Content-Type: application/json" \
  -d '{
    "query": "lightweight skis for beginners",
    "limit": 10
  }'

# Expected Response:
{
  "query": "lightweight skis for beginners",
  "results": [
    {
      "productId": "ski-beginner-001",
      "name": "Beginner All-Mountain Ski",
      "relevanceScore": 0.89,
      "description": "Perfect lightweight ski for beginners"
    }
  ],
  "totalResults": 5
}
```

## Integration with Other Microservices

This service integrates with the following backend APIs:

- **Authentication Service**: User authentication and authorization
- **User Management Service**: User profile and preferences  
- **Inventory Management Service**: Product information and availability
- **Sales Management Service**: Order history and purchase patterns
- **Payment Cart Service**: Shopping cart data for recommendations
- **Point Service**: Point balance for recommendation scoring
- **Coupon Service**: Available promotions for chat suggestions

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

Response includes MongoDB, Redis, and Azure OpenAI connectivity status.

### Metrics

```url
GET /actuator/metrics
GET /actuator/prometheus
```

Available metrics:

- AI model inference latency
- Recommendation accuracy
- Chat session duration
- Search query performance
- Cache hit ratio

### Log Levels

- **Development environment**: DEBUG
- **Production environment**: INFO

Key loggers:

- `com.skishop.ai`: AI service operations
- `org.springframework.ai`: Spring AI framework
- `dev.langchain4j`: LangChain4j operations

## Troubleshooting

### Common Issues

### 1. Azure OpenAI Connection Failure

```text
ERROR: Failed to connect to Azure OpenAI service
Solution: Check AZURE_OPENAI_KEY and AZURE_OPENAI_ENDPOINT environment variables
```

### 2. MongoDB Connection Issues

```text
ERROR: Unable to connect to MongoDB
Solution: Verify MongoDB is running and MONGODB_CONNECTION_STRING is correct
```

### 3. Redis Cache Unavailable

```text
WARN: Redis connection failed, falling back to local cache
Solution: Check Redis server status and connection parameters
```

#### 4. Model Deployment Not Found

```text
ERROR: Model deployment 'gpt-4o' not found
Solution: Verify model deployment name in Azure OpenAI Studio
```

## Developer Information

### Directory Structure

```text
ai-support-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/skishop/ai/
│   │   │       ├── controller/          # REST API controllers
│   │   │       │   ├── ChatController.java
│   │   │       │   ├── RecommendationController.java
│   │   │       │   └── SearchController.java
│   │   │       ├── service/             # Business logic services
│   │   │       │   ├── ChatService.java
│   │   │       │   ├── ProductRecommendationAssistant.java
│   │   │       │   └── SearchEnhancementAssistant.java
│   │   │       ├── dto/                 # Data transfer objects
│   │   │       ├── entity/              # JPA entities
│   │   │       ├── repository/          # MongoDB repositories
│   │   │       ├── config/              # Configuration classes
│   │   │       └── AiSupportServiceApplication.java
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
├── pom.xml                             # Maven configuration
└── README.md                           # This file
```

## Change History

- **v1.0.0** (2025-07-04): Initial release
  - AI chat support with Azure OpenAI integration
  - Product recommendation engine
  - Semantic search capabilities
  - LangChain4j integration for enhanced AI workflows
  - Azure Container Apps deployment support
