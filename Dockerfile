# Build backend
FROM maven:3.9-amazoncorretto-17 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Build frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app
COPY src/js/package*.json ./
RUN npm install
COPY src/js ./
RUN npm run build

# Final image
FROM amazoncorretto:17-alpine
WORKDIR /app

# Install nginx and curl for healthchecks
RUN apk add --no-cache nginx curl

# Create necessary directories
RUN mkdir -p /run/nginx /var/log/nginx

# Copy backend
COPY --from=backend-build /app/target/*.jar app.jar

# Copy frontend
COPY --from=frontend-build /app/build /usr/share/nginx/html
COPY src/js/nginx.conf /etc/nginx/http.d/default.conf

# Copy start script
COPY start-services.sh /app/
RUN chmod +x /app/start-services.sh

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

EXPOSE 80
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s \
    CMD curl -f http://localhost/api/actuator/health || exit 1

CMD ["/app/start-services.sh"] 