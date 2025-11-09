# Use OpenJDK 17 as base image
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy jar file
COPY target/api-gateway-0.0.1-SNAPSHOT.jar /app/api-gateway.jar

# Expose HTTPS port
EXPOSE 8443

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "api-gateway.jar"]
