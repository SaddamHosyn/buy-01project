Microservices split

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
