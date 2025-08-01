#!/bin/bash

# Post-create script for dev container setup
echo "🚀 Setting up Ski Shop Microservices development environment..."

# Wait for services to be ready
echo "⏳ Waiting for infrastructure services to start..."
sleep 20

# Check service availability
echo "🔍 Checking service health..."

# Check PostgreSQL
if command -v pg_isready &> /dev/null; then
    echo "Checking PostgreSQL connection..."
    for i in {1..30}; do
        if pg_isready -h postgres -p 5432 -U skishop_user -d skishop > /dev/null 2>&1; then
            echo "✅ PostgreSQL is ready"
            break
        fi
        echo "Waiting for PostgreSQL... ($i/30)"
        sleep 3
    done
fi

# Check Redis
if command -v redis-cli &> /dev/null; then
    echo "Checking Redis connection..."
    for i in {1..30}; do
        if redis-cli -h redis -p 6379 -a redis_password ping > /dev/null 2>&1; then
            echo "✅ Redis is ready"
            break
        fi
        echo "Waiting for Redis... ($i/30)"
        sleep 3
    done
fi

# Check Kafka
if command -v kafka-topics &> /dev/null; then
    echo "Checking Kafka connection..."
    for i in {1..30}; do
        if kafka-topics --bootstrap-server kafka:9092 --list > /dev/null 2>&1; then
            echo "✅ Kafka is ready"
            break
        fi
        echo "Waiting for Kafka... ($i/30)"
        sleep 5
    done
fi

# Check Elasticsearch
echo "Checking Elasticsearch connection..."
for i in {1..30}; do
    if curl -f http://elasticsearch:9200/_cluster/health > /dev/null 2>&1; then
        echo "✅ Elasticsearch is ready"
        break
    fi
    echo "Waiting for Elasticsearch... ($i/30)"
    sleep 5
done

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
