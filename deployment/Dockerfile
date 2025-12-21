# 1. Use a base image with Java 17
FROM openjdk:17-jdk-slim

# 2. Set the working directory inside the container
WORKDIR /app

# 3. Copy the compiled JAR file into the container
# (We assume Maven creates a JAR in the 'target' folder)
COPY target/*.jar app.jar

# 4. Expose the port the app runs on
EXPOSE 8080

# 5. Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
