#!/bin/bash

# Container verification script
echo "🔍 Verifying Dev Container Setup..."

# Function to check command availability
check_command() {
    if command -v "$1" &> /dev/null; then
        echo "✅ $1 is available"
        return 0
    else
        echo "❌ $1 is not available"
        return 1
    fi
}

# Function to check version
check_version() {
    echo "📋 $1 version:"
    if command -v "$2" &> /dev/null; then
        $2 --version | head -1
        echo ""
    else
        echo "❌ $1 not found"
        echo ""
    fi
}

echo ""
echo "🔧 Checking essential tools..."
check_command "java"
check_command "mvn"
check_command "git"
check_command "curl"
check_command "apk" || check_command "apt"

echo ""
echo "📋 Version information:"
check_version "Java" "java"
check_version "Maven" "mvn"
check_version "Git" "git"

echo ""
echo "🏗️ Testing build system..."
if [ -f "pom.xml" ]; then
    echo "✅ Maven POM file found"
    
    # Test if we can compile
    echo "🧪 Testing compilation..."
    if mvn compile -q 2>/dev/null; then
        echo "✅ Maven compilation successful"
    else
        echo "⚠️ Maven compilation failed (this might be expected if dependencies aren't cached yet)"
    fi
else
    echo "❌ Maven POM file not found"
fi

echo ""
echo "📁 Checking project structure..."
for service in "frontend-service" "api-gateway" "authentication-service" "user-management-service" "inventory-management-service" "payment-cart-service" "sales-management-service" "point-service" "coupon-service" "ai-support-service"; do
    if [ -d "$service" ]; then
        echo "✅ $service directory found"
    else
        echo "❌ $service directory missing"
    fi
done

echo ""
echo "🚀 Checking build scripts..."
for script in "build.sh" "start-services.sh" "stop-services.sh" "setup-dev-env.sh"; do
    if [ -f "$script" ]; then
        if [ -x "$script" ]; then
            echo "✅ $script is executable"
        else
            echo "⚠️ $script exists but not executable"
        fi
    else
        echo "❌ $script not found"
    fi
done

echo ""
echo "🎯 Container verification complete!"
echo ""
echo "📚 Available commands:"
echo "   ./build.sh          - Build the project"
echo "   ./build.sh package  - Create JAR files"
echo "   ./build.sh test     - Run tests"
echo "   ./start-services.sh - Start all services"
echo "   ./stop-services.sh  - Stop all services"
