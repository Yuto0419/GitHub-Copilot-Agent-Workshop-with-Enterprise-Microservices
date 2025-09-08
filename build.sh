#!/bin/bash

# Build script for Ski Shop Microservices
echo "🔨 Building Ski Shop Microservices..."

# Function to display help
show_help() {
    echo "Usage: $0 [OPTION]"
    echo "Build script for Ski Shop Microservices"
    echo ""
    echo "Options:"
    echo "  clean        Clean and compile all modules"
    echo "  test         Run tests for all modules"
    echo "  package      Build JAR files for all modules"
    echo "  install      Install artifacts to local repository"
    echo "  quick        Quick compilation (skip tests)"
    echo "  help         Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0           # Default: clean compile"
    echo "  $0 package   # Build JAR files"
    echo "  $0 test      # Run all tests"
}

# Default action if no argument provided
ACTION=${1:-clean}

case $ACTION in
    clean)
        echo "🧹 Cleaning and compiling all modules..."
        mvn clean compile
        ;;
    test)
        echo "🧪 Running tests for all modules..."
        mvn clean test
        ;;
    package)
        echo "📦 Building JAR files for all modules..."
        mvn clean package -DskipTests
        ;;
    install)
        echo "📥 Installing artifacts to local repository..."
        mvn clean install
        ;;
    quick)
        echo "⚡ Quick compilation (no tests)..."
        mvn compile -DskipTests
        ;;
    help)
        show_help
        exit 0
        ;;
    *)
        echo "❌ Unknown option: $ACTION"
        echo "Use '$0 help' for usage information."
        exit 1
        ;;
esac

if [ $? -eq 0 ]; then
    echo "✅ Build completed successfully!"
else
    echo "❌ Build failed!"
    exit 1
fi
