#!/bin/bash

# Функция для проверки и освобождения порта
free_port() {
    local port=$1
    local pid=$(lsof -t -i:${port} 2>/dev/null)
    if [ ! -z "$pid" ]; then
        echo "Stopping process on port ${port}..."
        kill -9 $pid
        sleep 2
    fi
}

# Освобождаем порты перед запуском
free_port 8080
free_port 5432

# Остановить и удалить существующие контейнеры
docker compose down

# Запустить PostgreSQL
docker compose up -d

# Подождать, пока PostgreSQL полностью запустится
echo "Waiting for PostgreSQL to start..."
until docker exec interview_questions_db pg_isready -U postgres; do
  sleep 1
done
echo "PostgreSQL is ready!"

# Запустить Spring Boot приложение
./mvnw clean spring-boot:run