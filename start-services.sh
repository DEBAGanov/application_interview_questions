#!/bin/sh

# Создаем директорию для логов nginx
mkdir -p /var/log/nginx
touch /var/log/nginx/access.log /var/log/nginx/error.log

# Запуск nginx в фоновом режиме
nginx

# Ждем запуска nginx
sleep 2

# Проверяем статус nginx
nginx -t

# Запуск Spring Boot приложения с логированием
java -jar app.jar > /app/application.log 2>&1 &

# Ждем запуска приложения
sleep 10

# Проверяем логи
tail -f /app/application.log 