FROM eclipse-temurin:17-jdk-jammy

# Устанавливаем sqlite3 для доступа к БД из контейнера (по желанию)
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        sqlite3 \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Рабочая директория
WORKDIR /app

# Создаём каталог для SQLite и даём права
RUN mkdir -p /app/data && chmod 777 /app/data

# Копируем собранный jar
COPY build/libs/*.jar app.jar

# Опциональные ENV-переменные для токена и имени бота
# (можно переопределить при запуске)
#ENV TELEGRAM_TOKEN=""
#ENV TELEGRAM_USERNAME=""

# Запуск Spring Boot-приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
