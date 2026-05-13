#!/bin/bash
# Build all services and start with Docker Compose

echo "==> Building all services..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
  echo "Build failed. Fix errors and retry."
  exit 1
fi

echo "==> Starting all services with Docker Compose..."
docker-compose up --build -d

echo ""
echo "Services starting up. Access points:"
echo "  Eureka Dashboard : http://localhost:8761"
echo "  API Gateway      : http://localhost:8888"
echo "  Auth Service     : http://localhost:9091"
echo "  Catalog Service  : http://localhost:9092"
echo "  Order Service    : http://localhost:9093"
echo "  Admin Service    : http://localhost:9094"
echo "  Payment Service  : http://localhost:9095"
echo "  Notification     : http://localhost:9096"
