#!/bin/bash

# Post-create script for dev container setup
echo "🚀 Setting up Ski Shop Microservices development environment..."

# Wait for services to be ready
echo "⏳ Waiting for infrastructure services to start..."
sleep 10

# Check service availability
echo "🔍 Checking service health..."

# Check PostgreSQL
if command -v pg_isready &> /dev/null; then
    until pg_isready -h postgres -p 5432 -U skishop_user -d skishop; do
        echo "Waiting for PostgreSQL..."
        sleep 2
    done
    echo "✅ PostgreSQL is ready"
fi

# Check Redis
if command -v redis-cli &> /dev/null; then
    until redis-cli -h redis -p 6379 -a redis_password ping > /dev/null 2>&1; do
        echo "Waiting for Redis..."
        sleep 2
    done
    echo "✅ Redis is ready"
fi

# Check Kafka
if command -v kafka-topics &> /dev/null; then
    until kafka-topics --bootstrap-server kafka:9092 --list > /dev/null 2>&1; do
        echo "Waiting for Kafka..."
        sleep 5
    done
    echo "✅ Kafka is ready"
fi

# Check Elasticsearch
until curl -f http://elasticsearch:9200/_cluster/health > /dev/null 2>&1; do
    echo "Waiting for Elasticsearch..."
    sleep 5
done
echo "✅ Elasticsearch is ready"

# Download dependencies for all modules
echo "📦 Downloading Maven dependencies..."
mvn dependency:go-offline -q

# Create Kafka topics if they don't exist
echo "📢 Setting up Kafka topics..."
kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic user-events --partitions 3 --replication-factor 1
kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic order-events --partitions 3 --replication-factor 1
kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic inventory-events --partitions 3 --replication-factor 1
kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic payment-events --partitions 3 --replication-factor 1
kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic notification-events --partitions 3 --replication-factor 1

echo "📋 Available Kafka topics:"
kafka-topics --bootstrap-server kafka:9092 --list

# Set up database schemas if init scripts are available
echo "🗄️ Setting up database schemas..."
if [ -f "/workspace/scripts/init-databases.sql" ]; then
    echo "Database initialization script found, schemas should be created automatically"
else
    echo "No database initialization script found, you may need to create schemas manually"
fi

# Display useful information
echo ""
echo "🎉 Development environment setup complete!"
echo ""
echo "📍 Available Services:"
echo "  - PostgreSQL:     localhost:5432 (user: skishop_user, pass: skishop_password)"
echo "  - Redis:          localhost:6379 (pass: redis_password)"
echo "  - Kafka:          localhost:9092"
echo "  - Elasticsearch:  localhost:9200"
echo "  - Prometheus:     localhost:9090"
echo "  - Grafana:        localhost:3001 (admin/admin)"
echo "  - MailHog:        localhost:8025"
echo ""
echo "🔧 Useful Commands:"
echo "  mvn clean compile                    # Compile all modules"
echo "  mvn clean package -DskipTests       # Package all modules"
echo "  mvn spring-boot:run -pl <module>    # Run specific service"
echo "  docker-compose logs -f <service>    # View service logs"
echo "  kafka-topics --bootstrap-server kafka:9092 --list  # List Kafka topics"
echo ""
echo "🚀 Ready for development!"
