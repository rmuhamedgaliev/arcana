FROM alpine:latest

# Install dependencies
RUN apk add --no-cache wget ca-certificates bash

# Install Eclipse Temurin JDK 23
RUN mkdir -p /opt/java && \
    wget -O /tmp/jdk.tar.gz https://github.com/adoptium/temurin23-binaries/releases/download/jdk-23%2B36/OpenJDK23U-jdk_x64_alpine-linux_hotspot_23_36.tar.gz && \
    tar -xzf /tmp/jdk.tar.gz -C /opt/java --strip-components=1 && \
    rm /tmp/jdk.tar.gz

# Set JAVA_HOME and add Java to PATH
ENV JAVA_HOME=/opt/java
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Set working directory
WORKDIR /app

# Copy the application JAR
COPY build/libs/arcana-bot.jar /app/arcana-bot.jar

# Create volume for games directory
VOLUME /app/games

# Set environment variables
ENV GAMES_DIRECTORY=/app/games

# Run the application
CMD ["java", "-jar", "/app/arcana-bot.jar"]
