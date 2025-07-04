FROM mcr.microsoft.com/devcontainers/java:21

# Install additional packages
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
        build-essential \
        curl \
        git \
        htop \
        jq \
        nano \
        postgresql-client \
        python3 \
        python3-pip \
        redis-tools \
        tree \
        unzip \
        vim \
        wget \
    && apt-get autoremove -y && apt-get clean -y \
    # Install Node.js (for frontend if needed)
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/* \
    # Install Docker CLI
    && curl -fsSL https://get.docker.com | sh \
    # Install Docker Compose
    && curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose \
    && chmod +x /usr/local/bin/docker-compose \
    # Install Kafka CLI tools
    && wget https://downloads.apache.org/kafka/2.8.2/kafka_2.13-2.8.2.tgz \
    && tar -xzf kafka_2.13-2.8.2.tgz \
    && mv kafka_2.13-2.8.2 /opt/kafka \
    && rm kafka_2.13-2.8.2.tgz \
    && ln -s /opt/kafka/bin/kafka-topics.sh /usr/local/bin/kafka-topics \
    && ln -s /opt/kafka/bin/kafka-console-producer.sh /usr/local/bin/kafka-console-producer \
    && ln -s /opt/kafka/bin/kafka-console-consumer.sh /usr/local/bin/kafka-console-consumer \
    # Install useful development tools
    && npm install -g @angular/cli @vue/cli create-react-app \
    # Install Maven (latest version)
    && wget https://downloads.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz \
    && tar -xzf apache-maven-3.9.6-bin.tar.gz \
    && mv apache-maven-3.9.6 /opt/maven \
    && rm apache-maven-3.9.6-bin.tar.gz \
    && ln -s /opt/maven/bin/mvn /usr/local/bin/mvn

# Set environment variables
ENV MAVEN_HOME=/opt/maven
ENV PATH=$PATH:$MAVEN_HOME/bin
ENV JAVA_HOME=/usr/local/sdkman/candidates/java/current
ENV KAFKA_HOME=/opt/kafka

# Create workspace directory
WORKDIR /workspace

# Set up git safe directory, configure Maven settings, and create useful aliases (merged RUN)
RUN git config --global --add safe.directory /workspace && \
    mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"' >> /root/.m2/settings.xml && \
    echo '          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"' >> /root/.m2/settings.xml && \
    echo '          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0' >> /root/.m2/settings.xml && \
    echo '                              http://maven.apache.org/xsd/settings-1.0.0.xsd">' >> /root/.m2/settings.xml && \
    echo '  <localRepository>/root/.m2/repository</localRepository>' >> /root/.m2/settings.xml && \
    echo '  <interactiveMode>false</interactiveMode>' >> /root/.m2/settings.xml && \
    echo '  <offline>false</offline>' >> /root/.m2/settings.xml && \
    echo '</settings>' >> /root/.m2/settings.xml && \
    echo 'alias ll="ls -la"' >> /root/.bashrc && \
    echo 'alias la="ls -la"' >> /root/.bashrc && \
    echo 'alias ..="cd .."' >> /root/.bashrc && \
    echo 'alias grep="grep --color=auto"' >> /root/.bashrc && \
    echo 'alias mvn-clean="mvn clean compile"' >> /root/.bashrc && \
    echo 'alias mvn-test="mvn test"' >> /root/.bashrc && \
    echo 'alias mvn-package="mvn clean package -DskipTests"' >> /root/.bashrc && \
    echo 'alias dc="docker-compose"' >> /root/.bashrc && \
    echo 'alias dcu="docker-compose up -d"' >> /root/.bashrc && \
    echo 'alias dcd="docker-compose down"' >> /root/.bashrc && \
    echo 'alias dcl="docker-compose logs -f"' >> /root/.bashrc

# Keep container running
CMD ["sleep", "infinity"]
