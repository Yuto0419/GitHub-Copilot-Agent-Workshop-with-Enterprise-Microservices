# Ski Shop Microservices - Dev Container Development Guide

## Overview

This project provides a comprehensive microservices development environment using Dev Containers. All dependent services (PostgreSQL, Redis, Kafka, Elasticsearch) are automatically set up as containers, creating an integrated development environment that's ready to use out of the box.

## Prerequisites

- **Visual Studio Code**
- **Docker Desktop**
- **Dev Containers Extension** (`ms-vscode-remote.remote-containers`)

## Setup Instructions

### 1. Configure Environment Variables

```bash
# Copy .devcontainer/.env.example to .devcontainer/.env
cp .devcontainer/.env.example .devcontainer/.env

# Edit configuration values as needed (especially Azure OpenAI settings)
vim .devcontainer/.env
```

### 2. Start the Dev Container

1. Open the project root in VS Code
2. Open the Command Palette (`Ctrl/Cmd + Shift + P`)
3. Run `Dev Containers: Reopen in Container`
4. On first startup, container images will be automatically built and dependent services will start

### 3. Verify Setup

Once the Dev Container is running, the following services will be automatically available:

| Service | Port | Description |
|---------|------|-------------|
| PostgreSQL | 5432 | Main database |
| Redis | 6379 | Cache & session management |
| Kafka | 9092 | Message broker |
| Elasticsearch | 9200 | Search engine |
| Prometheus | 9090 | Metrics collection |
| Grafana | 3001 | Metrics visualization |
| MailHog | 8025 | Email testing |

## Development Workflow

### Building the Project

```bash
# Compile all modules
mvn clean compile

# Package all modules (skip tests)
mvn clean package -DskipTests

# Build specific module
mvn clean compile -pl authentication-service
```

### Starting Services

```bash
# Start a specific service
mvn spring-boot:run -pl authentication-service

# Run in background
nohup mvn spring-boot:run -pl authentication-service > logs/auth.log 2>&1 &
```

### Running Tests

```bash
# Run all tests
mvn test

# Test specific module
mvn test -pl ai-support-service

# Run integration tests
mvn verify
```

### Database Operations

```bash
# Connect to PostgreSQL
psql -h postgres -U skishop_user -d skishop_auth

# Connect to Redis
redis-cli -h redis -p 6379 -a redis_password

# Initialize database
psql -h postgres -U skishop_user -d skishop_auth -f authentication-service/init-auth-db.sql
```

### Kafka Operations

```bash
# List topics
kafka-topics --bootstrap-server kafka:9092 --list

# Send messages
kafka-console-producer --bootstrap-server kafka:9092 --topic user-events

# Consume messages
kafka-console-consumer --bootstrap-server kafka:9092 --topic user-events --from-beginning
```

## Service Startup Order

Recommended startup sequence:

1. **Infrastructure Services** (automatically started)
   - PostgreSQL, Redis, Kafka, Elasticsearch

2. **Authentication Service**

   ```bash
   mvn spring-boot:run -pl authentication-service
   ```

3. **User Management Service**

   ```bash
   mvn spring-boot:run -pl user-management-service
   ```

4. **Business Services**

   ```bash
   mvn spring-boot:run -pl inventory-management-service
   mvn spring-boot:run -pl sales-management-service
   mvn spring-boot:run -pl payment-cart-service
   mvn spring-boot:run -pl point-service
   mvn spring-boot:run -pl coupon-service
   mvn spring-boot:run -pl ai-support-service
   ```

5. **API Gateway**

   ```bash
   mvn spring-boot:run -pl api-gateway
   ```

6. **Frontend Service**

   ```bash
   mvn spring-boot:run -pl frontend-service
   ```

## Environment Verification and Troubleshooting

### Service Status Check

```bash
# Docker Compose service status
docker-compose ps

# Service logs
docker-compose logs -f postgres
docker-compose logs -f kafka

# Health checks
curl http://localhost:8080/actuator/health
curl http://localhost:9200/_cluster/health
```

### Development Commands

```bash
# Clear Maven cache
mvn dependency:purge-local-repository

# Reset Docker volumes
docker-compose down -v
docker-compose up -d

# Monitor logs
tail -f logs/*.log
```

## VS Code Extensions

The Dev Container automatically installs the following extensions:

- **Java Development**: Java Extension Pack, Spring Boot Extensions
- **Database**: PostgreSQL Client, MongoDB Client
- **API Development**: REST Client, OpenAPI
- **Containers**: Docker, Remote Containers
- **Azure**: Azure Tools
- **Git**: GitHub Pull Requests, GitLens

## Debug Configuration

Debug ports are configured for each service:

- Authentication Service: 5005
- User Management Service: 5006
- API Gateway: 5007

To start with debugging:

```bash
mvn spring-boot:run -pl authentication-service -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

## Important Notes

1. **Initial Startup Time**: First startup takes longer due to Docker image downloads and builds
2. **Resource Usage**: Running all services simultaneously requires 8GB+ RAM
3. **Port Conflicts**: Ensure no local services are using the same ports
4. **Environment Variables**: Manage sensitive information like Azure OpenAI API keys in the `.env` file

## Useful Aliases

The Dev Container includes these helpful aliases:

```bash
alias mvn-clean="mvn clean compile"
alias mvn-test="mvn test"
alias mvn-package="mvn clean package -DskipTests"
alias dc="docker-compose"
alias dcu="docker-compose up -d"
alias dcd="docker-compose down"
alias dcl="docker-compose logs -f"
```
