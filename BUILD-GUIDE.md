# 🏗️ Building the Ski Shop Microservices Workspace

## Quick Start

The dev container configuration had issues with Alpine Linux compatibility. Here's how to build and run the project:

### 1. **Setup Development Environment** (One-time setup)
```bash
# Run the setup script to install required tools
./setup-dev-env.sh

# Reload your shell configuration
source ~/.bashrc
```

### 2. **Build the Project**
```bash
# Simple build (recommended for first time)
./build.sh

# Or use Maven directly
mvn clean compile
```

### 3. **Available Build Options**
```bash
./build.sh clean    # Clean and compile (default)
./build.sh test     # Run all tests
./build.sh package  # Create JAR files
./build.sh install  # Install to local repository
./build.sh quick    # Quick compile (skip tests)
./build.sh help     # Show help
```

## 🔧 Dev Container Fix

The original dev container was designed for Ubuntu/Debian but you're running Alpine Linux. Two options:

### Option A: Use Current Alpine Setup (Recommended)
The project is now configured to work with Alpine Linux:
- ✅ Java 21 installed
- ✅ Maven 3.9.9 installed  
- ✅ Build scripts created
- ✅ All dependencies resolved

### Option B: Fix Dev Container for Future Use
Use the Alpine-compatible Dockerfile:
```bash
# Copy the Alpine Dockerfile over the original
cp .devcontainer/Dockerfile.alpine .devcontainer/Dockerfile
```

## 🚀 Running Services

After building successfully:

```bash
# Start all microservices
./start-services.sh

# Check service status
./start-services.sh status

# Stop all services
./stop-services.sh
```

## 📋 Service Ports

- Frontend Service: 8080
- API Gateway: 8090
- Authentication Service: 8080
- User Management: 8081
- Inventory Management: 8082
- Sales Management: 8083
- Payment & Cart: 8084
- Point Service: 8085
- Coupon Service: 8086
- AI Support: 8087

## 🛠️ Troubleshooting

### Build Issues
```bash
# Clean everything and rebuild
mvn clean
./build.sh clean

# Check Java/Maven versions
java --version
mvn --version
```

### Missing Dependencies
```bash
# Reinstall development tools
./setup-dev-env.sh
```

### Service Issues
```bash
# Check logs
tail -f logs/*.log

# Check running processes
ps aux | grep java
```

## 📝 Development Workflow

1. **Make changes** to your code
2. **Build** with `./build.sh`
3. **Test** with `./build.sh test`
4. **Package** with `./build.sh package`
5. **Run services** with `./start-services.sh`

## 🎯 Key Files

- `build.sh` - Main build script
- `setup-dev-env.sh` - Environment setup
- `start-services.sh` - Start all services
- `stop-services.sh` - Stop all services
- `pom.xml` - Maven configuration
- `.devcontainer/` - Dev container configs

---

**✅ The workspace is now ready for development!**
