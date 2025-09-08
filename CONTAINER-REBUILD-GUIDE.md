# 🔄 Dev Container Rebuild Guide

## Overview

This guide will help you rebuild the dev container for the Ski Shop Microservices project. The container has been optimized for **Alpine Linux** to provide better performance and smaller size.

## 🚀 Quick Rebuild Steps

### Method 1: Using the Rebuild Script (Recommended)

```bash
# Use Alpine Linux configuration (recommended)
./rebuild-container.sh alpine

# OR use Ubuntu configuration
./rebuild-container.sh ubuntu
```

### Method 2: Manual VS Code Commands

1. **Open Command Palette**: `Ctrl+Shift+P` (or `Cmd+Shift+P` on Mac)
2. **Run**: `Dev Containers: Rebuild Container`
3. **Wait** for the rebuild to complete

## 📋 Container Configurations

### 🏔️ Alpine Linux (Recommended)
- **File**: `.devcontainer/devcontainer-alpine.json`
- **Dockerfile**: `.devcontainer/Dockerfile.alpine`
- **Benefits**:
  - Smaller container size (~200MB vs ~2GB)
  - Faster startup time
  - Better resource efficiency
  - Native Alpine package management

### 🐧 Ubuntu (Original)
- **File**: `.devcontainer/devcontainer.json` (original)
- **Dockerfile**: `.devcontainer/Dockerfile`
- **Features**:
  - Uses dev container features
  - More familiar environment
  - Larger size but more tools

## 🔧 What's Included

### Development Tools
- ✅ **Java 21** (OpenJDK)
- ✅ **Maven 3.9.9**
- ✅ **Git**
- ✅ **Node.js & npm**
- ✅ **Essential CLI tools**

### VS Code Extensions
- ✅ **Java Extension Pack**
- ✅ **Spring Boot Tools**
- ✅ **Maven for Java**
- ✅ **Docker Support**
- ✅ **GitHub Copilot**
- ✅ **GitLens**

### Build Scripts
- ✅ `./build.sh` - Main build script
- ✅ `./start-services.sh` - Start all services
- ✅ `./stop-services.sh` - Stop all services
- ✅ `./verify-setup.sh` - Verify container setup

## 🧪 Testing After Rebuild

Run the verification script to ensure everything works:

```bash
./verify-setup.sh
```

Test the build system:

```bash
# Quick compilation test
./build.sh quick

# Full build with JAR creation
./build.sh package

# Run tests
./build.sh test
```

## 🚨 Troubleshooting

### If Java is not found:
```bash
# Check Java installation
java --version

# If missing, install manually
sudo apk add openjdk21 openjdk21-jdk
```

### If Maven is not found:
```bash
# Check Maven installation
mvn --version

# If missing, install manually
sudo apk add maven
```

### If build fails:
```bash
# Clean and rebuild
./build.sh clean

# Check for errors
./verify-setup.sh
```

### Reset to Alpine configuration:
```bash
./rebuild-container.sh alpine
```

## 📚 Build Commands Reference

| Command | Description |
|---------|-------------|
| `./build.sh` | Default: clean compile |
| `./build.sh clean` | Clean and compile all modules |
| `./build.sh package` | Build JAR files |
| `./build.sh test` | Run all tests |
| `./build.sh install` | Install to local Maven repo |
| `./build.sh quick` | Quick compile (skip tests) |
| `./build.sh help` | Show help |

## 🔗 Port Configuration

The following ports are forwarded for development:

| Port | Service |
|------|---------|
| 8080 | Authentication Service / Frontend |
| 8090 | API Gateway |
| 8081 | User Management Service |
| 8082 | Inventory Management Service |
| 8083 | Sales Management Service |
| 8084 | Payment & Cart Service |
| 8085 | Point Service |
| 8086 | Coupon Service |
| 8087 | AI Support Service |

## 🎯 Next Steps

After successful rebuild:

1. **Verify setup**: `./verify-setup.sh`
2. **Build project**: `./build.sh package`
3. **Start services**: `./start-services.sh`
4. **Check logs**: `tail -f logs/*.log`

## 📞 Support

If you encounter issues:

1. Check the verification script output
2. Try rebuilding with `./rebuild-container.sh alpine`
3. Check VS Code Dev Container logs
4. Ensure Docker is running properly

---

**✅ Your dev container is now optimized for Ski Shop Microservices development!**
