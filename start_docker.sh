#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

# Build all Spring Boot microservices JARs
for service in service-registry user-service product-service media-service api-gateway; do
  echo "--- Building $service ---"
  (cd $service && mvn clean package)
done

echo "--- Build complete ---"

# Start all services in Docker

echo "--- Starting all services in Docker ---"
docker-compose up --build -d
