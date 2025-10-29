# buy-01

## Microservices split

This repository now contains three standalone Spring Boot service skeletons extracted from the original monolith.

- `user-service` (port 8081)
- `product-service` (port 8082)
- `media-service` (port 8083)

Each service contains a minimal `pom.xml`, a `SpringBootApplication` entrypoint, the model and repository for that service, and an `application.properties` that sets a unique port. You should configure MongoDB connection strings per-service as needed.

How to build (from repo root):

Open a PowerShell terminal and run for each service directory:

    cd .\user-service; mvn -DskipTests package
    cd ..\product-service; mvn -DskipTests package
    cd ..\media-service; mvn -DskipTests package

Or run them directly from your IDE. Each service is a standalone Spring Boot application and can be started independently.

## How to Run

1. Build jars once

```
# from repo root
.\mvnw.cmd -DskipTests package

# OR `mvn -DskipTests package`
```

2. Run jars (each in its own window so logs are visible):

```
Start-Process powershell -ArgumentList '-NoExit','-Command','java -jar ..\buy-01\service-registry\target\service-registry-0.0.1-SNAPSHOT.jar'
Start-Process powershell -ArgumentList '-NoExit','-Command','java -jar ..\buy-01\user-service\target\user-service-0.0.1-SNAPSHOT.jar'
Start-Process powershell -ArgumentList '-NoExit','-Command','java -jar ..\buy-01\product-service\target\product-service-0.0.1-SNAPSHOT.jar'
Start-Process powershell -ArgumentList '-NoExit','-Command','java -jar ..\buy-01\media-service\target\media-service-0.0.1-SNAPSHOT.jar'
Start-Process powershell -ArgumentList '-NoExit','-Command','java -jar ..\buy-01\api-gateway\target\api-gateway-0.0.1-SNAPSHOT.jar'
```

for bash:

```
# Open a new terminal window for each service and run the following commands:

# Service Registry
bash -c "java -jar ../buy-01/service-registry/target/service-registry-0.0.1-SNAPSHOT.jar"

# User Service
bash -c "java -jar ../buy-01/user-service/target/user-service-0.0.1-SNAPSHOT.jar"

# Product Service
bash -c "java -jar ../buy-01/product-service/target/product-service-0.0.1-SNAPSHOT.jar"

# Media Service
bash -c "java -jar ../buy-01/media-service/target/media-service-0.0.1-SNAPSHOT.jar"

# API Gateway
bash -c "java -jar ../buy-01/api-gateway/target/api-gateway-0.0.1-SNAPSHOT.jar"

```