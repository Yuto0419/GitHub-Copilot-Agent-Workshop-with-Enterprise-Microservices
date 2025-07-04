#!/bin/bash

# Post-start script for dev container
echo "🔄 Running post-start setup..."

# Ensure all services are healthy
echo "🏥 Checking service health..."

# Check if all expected ports are responding
services=(
    "postgres:5432"
    "redis:6379" 
    "kafka:9092"
    "elasticsearch:9200"
)

for service in "${services[@]}"; do
    host=$(echo $service | cut -d: -f1)
    port=$(echo $service | cut -d: -f2)
    
    if nc -z $host $port; then
        echo "✅ $service is responding"
    else
        echo "⚠️  $service is not responding"
    fi
done

# Display current Java version
echo "☕ Java version:"
java -version

# Display Maven version
echo "📦 Maven version:"
mvn --version

# Check if project can be compiled
echo "🔨 Checking if project compiles..."
if mvn -q compile > /dev/null 2>&1; then
    echo "✅ Project compiles successfully"
else
    echo "⚠️  Project compilation issues detected. Check dependencies."
fi

echo "✨ Post-start setup complete!"
