#!/bin/bash

# Dev Container Setup Fix for Alpine Linux
echo "🔧 Setting up development environment for Alpine Linux..."

# Update package index
echo "📦 Updating package index..."
sudo apk update

# Install essential development tools if not already installed
echo "🛠️ Installing essential development tools..."
sudo apk add --no-cache \
    openjdk21 \
    openjdk21-jdk \
    maven \
    curl \
    wget \
    bash \
    git \
    openssh-client \
    postgresql-client \
    redis \
    tree \
    nano \
    vim \
    htop \
    jq \
    netcat-openbsd

# Set up Java environment
echo "☕ Setting up Java environment..."
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
export PATH=$JAVA_HOME/bin:$PATH

# Add to profile for persistence
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-openjdk' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc

# Create useful aliases
echo "📝 Setting up useful aliases..."
cat >> ~/.bashrc << 'EOF'

# Ski Shop Development Aliases
alias ll="ls -la"
alias la="ls -la"
alias ..="cd .."
alias grep="grep --color=auto"
alias mvn-clean="mvn clean compile"
alias mvn-test="mvn test"
alias mvn-package="mvn clean package -DskipTests"
alias mvn-run="mvn spring-boot:run"
alias build="./build.sh"
alias logs="tail -f logs/*.log"
alias services="./start-services.sh"
alias stop-services="./stop-services.sh"

# Git shortcuts
alias gs="git status"
alias ga="git add"
alias gc="git commit"
alias gp="git push"
alias gl="git log --oneline"

# Docker aliases (when available)
alias dps="docker ps"
alias dlogs="docker logs -f"

echo "🚀 Ski Shop Development Environment Ready!"
EOF

# Create logs directory if it doesn't exist
mkdir -p /workspaces/java-skishop-microservices/logs

# Verify installation
echo ""
echo "🔍 Verifying installation..."
echo "Java version:"
java --version
echo ""
echo "Maven version:"
mvn --version
echo ""

echo "✅ Dev container setup completed successfully!"
echo ""
echo "📚 Next steps:"
echo "   1. Run './build.sh' to build the project"
echo "   2. Run './start-services.sh' to start services"
echo "   3. Use 'source ~/.bashrc' to reload aliases"
echo ""
echo "🔧 Available commands:"
echo "   ./build.sh          - Build project"
echo "   ./build.sh package  - Create JAR files"
echo "   ./build.sh test     - Run tests"
echo "   ./start-services.sh - Start all services"
echo "   ./stop-services.sh  - Stop all services"
