# Используйте OpenJDK 24 из Quay.io
FROM quay.io/lib/openjdk:24-jdk

# Проверьте версию Java и систему
RUN java -version
RUN cat /etc/os-release || echo "OS release info not found"
RUN which apk && echo "Alpine detected" || which yum && echo "RHEL/CentOS detected" || which apt-get && echo "Debian/Ubuntu detected" || echo "Unknown package manager"

# Установите bash если его нет (скорее всего Alpine Linux)
RUN if command -v apk >/dev/null 2>&1; then \
        apk add --no-cache bash curl; \
    fi

# Java уже настроена в базовом образе eclipse-temurin
# Но можно явно указать для ясности
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Создайте пользователя для безопасности
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Установите рабочую директорию
WORKDIR /app

# Скопируйте JAR файл
COPY build/libs/arcana-bot.jar /app/arcana-bot.jar

# Создайте директорию для игр и установите права
RUN mkdir -p /app/games && chown -R appuser:appuser /app

# Переключитесь на непривилегированного пользователя
USER appuser

# Создайте том для директории игр
VOLUME ["/app/games"]

# Установите переменные окружения
ENV GAMES_DIRECTORY=/app/games
ENV BOT_USERNAME=""
ENV TELEGRAM_BOT_TOKEN=""

# Откройте порт если нужен (для веб-хуков или метрик)
# EXPOSE 8080

# Запустите приложение
CMD ["java", "-jar", "/app/arcana-bot.jar"]
