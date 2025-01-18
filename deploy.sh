#!/bin/bash

# Функция для проверки и освобождения порта
free_port() {
    local port=$1
    echo "Checking port $port..."
    if lsof -i :$port > /dev/null; then
        echo "Port $port is in use. Stopping processes..."
        # Для macOS останавливаем PostgreSQL если он запущен
        if [ $port -eq 5432 ]; then
            brew services stop postgresql@14 2>/dev/null || true
            brew services stop postgresql@15 2>/dev/null || true
            brew services stop postgresql 2>/dev/null || true
        fi
        # Принудительно освобождаем порт
        lsof -ti :$port | xargs kill -9 2>/dev/null || true
        sleep 2
    fi
}

# Освобождаем нужные порты
free_port 5432  # PostgreSQL
free_port 8080  # Backend
free_port 3000  # Frontend

# Остановить существующие контейнеры
docker compose down

# Удалить все неиспользуемые контейнеры, сети и образы
docker system prune -f

# Собрать и запустить все контейнеры
docker compose up --build -d

# Подождать, пока все сервисы запустятся
echo "Waiting for PostgreSQL..."
until docker exec interview_questions_db pg_isready -U postgres 2>/dev/null; do
    sleep 1
done

echo "Waiting for Backend..."
until curl -s http://localhost:8080/actuator/health 2>/dev/null | grep "UP" > /dev/null; do
    sleep 1
done

echo "Waiting for Frontend..."
until curl -s http://localhost:3000 > /dev/null; do
    sleep 1
done

echo "Application is running!"
echo "Frontend: http://localhost:3000"
echo "Backend: http://localhost:8080"