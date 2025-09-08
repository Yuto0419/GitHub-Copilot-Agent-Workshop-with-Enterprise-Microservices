#!/usr/bin/env bash
set -euo pipefail

echo "🚀 Setting up Ski Shop Microservices development environment (post-create) ..."

export SHELL=/bin/bash
export PATH=$JAVA_HOME/bin:$PATH

# Resolve workspace folder (WORKDIR already set to /workspaces/java-skishop-microservices)
WS_DIR="${WORKSPACE_FOLDER:-/workspaces/java-skishop-microservices}"

LOG_DIR="${WS_DIR}/logs"

# Append helpful aliases once (idempotent)
if ! grep -q "Ski Shop Development Aliases" ~/.bashrc 2>/dev/null; then
cat >> ~/.bashrc <<'EOF'

# Ski Shop Development Aliases
alias ll="ls -la"
alias la="ls -la"
alias ..="cd .."
alias build="./build.sh"
alias services="./start-services.sh"
alias stop-services="./stop-services.sh"
alias logs="tail -f logs/*.log"

# Maven shortcuts
alias mvn-clean="mvn clean compile"
alias mvn-test="mvn test"
alias mvn-package="mvn clean package -DskipTests"
alias mvn-run="mvn spring-boot:run"

# Git shortcuts
alias gs="git status"
alias ga="git add"
alias gc="git commit"
alias gp="git push"
alias gl="git log --oneline"

echo "🚀 Ski Shop Development Environment Ready!"
EOF
fi

mkdir -p "${LOG_DIR}"
chmod +x "${WS_DIR}"/*.sh 2>/dev/null || true

echo "🔍 Verifying installation..."
echo "Java version:"; java --version || true
echo "Maven version:"; mvn --version || true

echo "✅ Base shell customization done. Evaluating optional steps..."

CHECK_INFRA_SERVICES=${CHECK_INFRA_SERVICES:-true}
MAVEN_GO_OFFLINE=${MAVEN_GO_OFFLINE:-once}

echo "⚙️  Flags -> CHECK_INFRA_SERVICES=${CHECK_INFRA_SERVICES} | MAVEN_GO_OFFLINE=${MAVEN_GO_OFFLINE}"

run_infra_checks() {
    echo "⏳ Waiting for infrastructure services to start..."
    sleep 5
    echo "🔍 Checking service health..."

    if command -v pg_isready &> /dev/null; then
        echo "Checking PostgreSQL connection..."
        for i in {1..30}; do
            if pg_isready -h postgres -p 5432 -U skishop_user -d skishop > /dev/null 2>&1; then
                echo "✅ PostgreSQL is ready"; break; fi
            echo "Waiting for PostgreSQL... ($i/30)"; sleep 2; done
    else
        echo "ℹ️  pg_isready not found, skipping PostgreSQL check"
    fi

    if command -v redis-cli &> /dev/null; then
        echo "Checking Redis connection..."
        for i in {1..20}; do
            if redis-cli -h redis -p 6379 -a redis_password ping > /dev/null 2>&1; then
                echo "✅ Redis is ready"; break; fi
            echo "Waiting for Redis... ($i/20)"; sleep 2; done
    else
        echo "ℹ️  redis-cli not found, skipping Redis check"
    fi

    if command -v kafka-topics &> /dev/null; then
        echo "Checking Kafka connection..."
        for i in {1..30}; do
            if kafka-topics --bootstrap-server kafka:9092 --list > /dev/null 2>&1; then
                echo "✅ Kafka is reachable"; break; fi
            echo "Waiting for Kafka... ($i/30)"; sleep 3; done
    else
        echo "ℹ️  kafka-topics not found, skipping Kafka connectivity check"
    fi

    echo "Checking Elasticsearch connection..."
    for i in {1..20}; do
        if curl -fsS http://elasticsearch:9200/_cluster/health > /dev/null 2>&1; then
            echo "✅ Elasticsearch healthy"; break; fi
        echo "Waiting for Elasticsearch... ($i/20)"; sleep 3; done
}

maybe_create_kafka_topics() {
    if command -v kafka-topics &> /dev/null; then
        echo "📢 Setting up Kafka topics (idempotent)..."
        local topics=(user-events order-events inventory-events payment-events notification-events)
        for t in "${topics[@]}"; do
            kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic "$t" --partitions 3 --replication-factor 1 || true
        done
        echo "📋 Available Kafka topics:"; kafka-topics --bootstrap-server kafka:9092 --list || true
    else
        echo "ℹ️  kafka-topics CLI 未インストールのためトピック作成スキップ"
    fi
}

maybe_go_offline() {
    case "${MAVEN_GO_OFFLINE}" in
        false|off|no)
            echo "⏭  Skipping mvn dependency:go-offline (flag=${MAVEN_GO_OFFLINE})";;
        once)
            local marker="${WS_DIR}/.mvn_go_offline_done"
            if [ -f "$marker" ]; then
                echo "⏭  Skipping go-offline (already done once). Remove $marker to force rerun."; return 0; fi
            echo "📦 Running mvn dependency:go-offline (once)..."; mvn -q dependency:go-offline || echo "⚠️  go-offline encountered issues"; touch "$marker";;
        always|true|yes)
            echo "📦 Running mvn dependency:go-offline (always)..."; mvn -q dependency:go-offline || echo "⚠️  go-offline encountered issues";;
        *)
            echo "ℹ️  Unknown MAVEN_GO_OFFLINE='${MAVEN_GO_OFFLINE}', treating as 'once'"; MAVEN_GO_OFFLINE=once; maybe_go_offline; return;;
    esac
}

if [ "${CHECK_INFRA_SERVICES}" = "true" ] || [ "${CHECK_INFRA_SERVICES}" = "1" ]; then
    run_infra_checks
    maybe_create_kafka_topics
else
    echo "⏭  Infra health checks skipped (CHECK_INFRA_SERVICES=${CHECK_INFRA_SERVICES})"
fi

maybe_go_offline

# Set up database schemas if init scripts are available
echo "🗄️ Setting up database schemas..."
if [ -f "${WS_DIR}/scripts/init-databases.sql" ]; then
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
echo "🚀 Ready for development! (post-create complete)"
