#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

echo "--- Building all microservices. This may take a moment... ---"
mvn clean install -DskipTests
echo "--- Build complete ---"

# Create a file to store PIDs for the stop script
PID_FILE=".pids"
> "$PID_FILE"

echo "--- Starting Service Registry (Eureka) ---"
nohup java -jar service-registry/target/service-registry-*.jar > service-registry.log 2>&1 &
echo $! >> "$PID_FILE"
echo "Service Registry starting in background. Waiting 15 seconds for it to initialize..."
sleep 5

echo "--- Starting User Service ---"
nohup java -jar user-service/target/user-service-*.jar > user-service.log 2>&1 &
echo $! >> "$PID_FILE"

echo "--- Starting Product Service ---"
nohup java -jar product-service/target/product-service-*.jar > product-service.log 2>&1 &
echo $! >> "$PID_FILE"

echo "--- Starting Media Service ---"
nohup java -jar media-service/target/media-service-*.jar > media-service.log 2>&1 &
echo $! >> "$PID_FILE"

# Wait a few seconds for services to register with Eureka
echo "--- Waiting 5 seconds for services to register... ---"
sleep 5

echo "--- Starting API Gateway ---"
nohup java -jar api-gateway/target/api-gateway-*.jar > api-gateway.log 2>&1 &
echo $! >> "$PID_FILE"

echo ""
echo "--- All services started successfully! ---"
echo "API Gateway is running on port 8080."
echo "You can view logs in the *.log files (e.g., 'tail -f api-gateway.log')."
echo "PIDs of running services are stored in .pids"
