#!/bin/bash

# Microservices startup script
echo "🚀 Starting Ski Shop Microservices..."

# Function to start a specific service
start_service() {
    local service_name=$1
    local port=$2
    
    echo "Starting $service_name on port $port..."
    cd "$service_name"
    
    # Build the service if needed
    if [ ! -d "target/classes" ] || [ "pom.xml" -nt "target/classes" ]; then
        echo "Building $service_name..."
        mvn clean compile -q
    fi
    
    # Start the service in background
    nohup mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=$port" > ../logs/$service_name.log 2>&1 &
    echo $! > ../logs/$service_name.pid
    
    echo "✅ $service_name started (PID: $(cat ../logs/$service_name.pid))"
    cd ..
}

# Create logs directory
mkdir -p logs

# Start infrastructure services first if not running
echo "🔍 Checking infrastructure services..."
cd .devcontainer
if ! docker-compose ps | grep -q "Up"; then
    echo "Starting infrastructure services..."
    docker-compose up -d postgres redis kafka elasticsearch
    sleep 30
fi
cd ..

# Wait for infrastructure services to be ready
echo "⏳ Waiting for infrastructure services..."
sleep 15

echo "🌟 Starting microservices..."

# Start services in dependency order
start_service "authentication-service" 8080
sleep 10

start_service "user-management-service" 8081
sleep 5

start_service "inventory-management-service" 8082
sleep 5

start_service "sales-management-service" 8083
sleep 5

start_service "payment-cart-service" 8084
sleep 5

start_service "point-service" 8085
sleep 5

start_service "coupon-service" 8086
sleep 5

start_service "ai-support-service" 8087
sleep 5

start_service "api-gateway" 8090
sleep 5

echo ""
echo "🎉 All services started!"
echo ""
echo "📍 Service URLs:"
echo "  - API Gateway:              http://localhost:8090"
echo "  - Authentication Service:   http://localhost:8080"
echo "  - User Management:          http://localhost:8081"
echo "  - Inventory Management:     http://localhost:8082"
echo "  - Sales Management:         http://localhost:8083"
echo "  - Payment & Cart:           http://localhost:8084"
echo "  - Point Service:            http://localhost:8085"
echo "  - Coupon Service:           http://localhost:8086"
echo "  - AI Support Service:       http://localhost:8087"
echo ""
echo "📋 Service Management:"
echo "  - View logs: tail -f logs/<service-name>.log"
echo "  - Stop service: kill \$(cat logs/<service-name>.pid)"
echo "  - Stop all: ./stop-services.sh"
echo ""
