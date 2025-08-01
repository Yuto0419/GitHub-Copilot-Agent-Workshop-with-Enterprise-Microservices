#!/bin/bash

# Stop all microservices
echo "🛑 Stopping all microservices..."

# Create logs directory if it doesn't exist
mkdir -p logs

# Function to stop a service
stop_service() {
    local service_name=$1
    local pid_file="logs/$service_name.pid"
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if ps -p $pid > /dev/null 2>&1; then
            echo "Stopping $service_name (PID: $pid)..."
            kill $pid
            sleep 2
            if ps -p $pid > /dev/null 2>&1; then
                echo "Force stopping $service_name..."
                kill -9 $pid
            fi
        fi
        rm -f "$pid_file"
        echo "✅ $service_name stopped"
    else
        echo "⚠️  No PID file found for $service_name"
    fi
}

# Stop all services
stop_service "api-gateway"
stop_service "ai-support-service"
stop_service "coupon-service"
stop_service "point-service"
stop_service "payment-cart-service"
stop_service "sales-management-service"
stop_service "inventory-management-service"
stop_service "user-management-service"
stop_service "authentication-service"

# Also kill any remaining Java processes related to Spring Boot
echo "🧹 Cleaning up any remaining Spring Boot processes..."
pkill -f "spring-boot:run" || true

echo ""
echo "✅ All services stopped!"
echo ""
