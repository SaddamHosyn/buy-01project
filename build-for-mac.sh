#!/bin/bash
# Build script for Mac ARM64 architecture

echo "🍎 Building Buy-01 Project for Mac (ARM64)..."
echo ""

# Build all services
echo "📦 Building backend services..."
cd /Users/saddam.hussain/Desktop/buy-01project

# Build JARs using Maven
echo "Building with Maven..."
./mvnw clean package -DskipTests

# Build Docker images for ARM64
echo ""
echo "🐳 Building Docker images for ARM64..."

# Service Registry
docker build -t hussainsaddam/buy-01-service-registry:latest ./service-registry

# API Gateway
docker build -t hussainsaddam/buy-01-api-gateway:latest ./api-gateway

# User Service
docker build -t hussainsaddam/buy-01-user-service:latest ./user-service

# Product Service
docker build -t hussainsaddam/buy-01-product-service:latest ./product-service

# Media Service
docker build -t hussainsaddam/buy-01-media-service:latest ./media-service

# Frontend
cd buy-01-ui
echo ""
echo "🎨 Building Frontend..."
npm install
npm run build
docker build -t hussainsaddam/buy-01-frontend:latest .
cd ..

echo ""
echo "✅ All images built successfully for Mac ARM64!"
echo ""
echo "🚀 To start the application, run:"
echo "   cd deployment && docker-compose up -d"
