#!/bin/bash

# Container rebuild script for Ski Shop Microservices
echo "🔄 Rebuilding Dev Container for Ski Shop Microservices..."

# Function to display help
show_help() {
    echo "Usage: $0 [OPTION]"
    echo "Rebuild script for Ski Shop Microservices dev container"
    echo ""
    echo "Options:"
    echo "  alpine       Use Alpine Linux configuration (recommended)"
    echo "  ubuntu       Use original Ubuntu configuration"
    echo "  current      Rebuild current configuration"
    echo "  help         Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0           # Default: alpine configuration"
    echo "  $0 alpine    # Use Alpine Linux setup"
    echo "  $0 ubuntu    # Use Ubuntu setup"
}

# Default action if no argument provided
CONFIG=${1:-alpine}

case $CONFIG in
    alpine)
        echo "🏔️ Using Alpine Linux configuration..."
        
        # Copy the Alpine configuration as the main config
        if [ -f ".devcontainer/devcontainer-alpine.json" ]; then
            cp .devcontainer/devcontainer-alpine.json .devcontainer/devcontainer.json
            echo "✅ Copied Alpine configuration to devcontainer.json"
        else
            echo "❌ Alpine configuration not found!"
            exit 1
        fi
        
        # Ensure Alpine Dockerfile exists
        if [ ! -f ".devcontainer/Dockerfile.alpine" ]; then
            echo "❌ Alpine Dockerfile not found! Creating it..."
            cat > .devcontainer/Dockerfile.alpine << 'EOF'
FROM alpine:3.21

# Install base packages
RUN apk update && apk add --no-cache \
    openjdk21 \
    openjdk21-jdk \
    maven \
    bash \
    curl \
    wget \
    git \
    openssh-client \
    postgresql-client \
    redis \
    tree \
    nano \
    vim \
    htop \
    jq \
    netcat-openbsd \
    nodejs \
    npm \
    python3 \
    py3-pip \
    && rm -rf /var/cache/apk/*

# Set environment variables
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk
ENV MAVEN_HOME=/usr/share/java/maven-3
ENV PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH

# Create workspace directory
WORKDIR /workspace

# Set up git safe directory and create useful aliases
RUN git config --global --add safe.directory /workspace \
    && echo 'alias ll="ls -la"' >> /root/.bashrc \
    && echo 'alias la="ls -la"' >> /root/.bashrc \
    && echo 'alias ..="cd .."' >> /root/.bashrc \
    && echo 'alias build="./build.sh"' >> /root/.bashrc

# Keep container running
CMD ["sleep", "infinity"]
EOF
            echo "✅ Created Alpine Dockerfile"
        fi
        ;;
        
    ubuntu)
        echo "🐧 Using Ubuntu configuration..."
        
        # Restore original Ubuntu configuration
        if [ -f ".devcontainer/devcontainer.json.backup" ]; then
            cp .devcontainer/devcontainer.json.backup .devcontainer/devcontainer.json
            echo "✅ Restored Ubuntu configuration"
        else
            echo "⚠️ No backup found, using current configuration"
        fi
        ;;
        
    current)
        echo "🔄 Rebuilding with current configuration..."
        # No configuration changes needed
        ;;
        
    help)
        show_help
        exit 0
        ;;
        
    *)
        echo "❌ Unknown option: $CONFIG"
        echo "Use '$0 help' for usage information."
        exit 1
        ;;
esac

echo ""
echo "📋 Next steps to rebuild the container:"
echo ""
echo "1. 🔄 Reload VS Code window:"
echo "   - Press Ctrl+Shift+P (or Cmd+Shift+P on Mac)"
echo "   - Type: 'Developer: Reload Window'"
echo "   - Press Enter"
echo ""
echo "2. 🏗️ Rebuild container:"
echo "   - Press Ctrl+Shift+P (or Cmd+Shift+P on Mac)"
echo "   - Type: 'Dev Containers: Rebuild Container'"
echo "   - Press Enter"
echo ""
echo "3. ⏳ Wait for the rebuild to complete"
echo ""
echo "4. ✅ Test the setup:"
echo "   - Run: ./build.sh"
echo "   - Run: java --version"
echo "   - Run: mvn --version"
echo ""
echo "🎯 The container will rebuild with the $CONFIG configuration!"

if [ "$CONFIG" = "alpine" ]; then
    echo ""
    echo "🏔️ Alpine Linux Benefits:"
    echo "   - Smaller container size"
    echo "   - Faster startup time"
    echo "   - Better resource efficiency"
    echo "   - Native package management with apk"
fi
