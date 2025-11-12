# Buy 01 - E-commerce Microservices Platform

## Project Overview

This project involves building an end-to-end e-commerce platform using Spring Boot microservices on the backend. Users can register as clients or sellers, with sellers managing products and associated media (images). The backend is designed with service decomposition, inter-service communication, JWT-based security, and MongoDB persistence.

**Role Play:** You are a full-stack engineer designing a small but realistic marketplace composed of independently deployable services. Your mission: deliver a secure, observable, and scalable platform where clients browse products and sellers manage their catalog and media.

## Learning Objectives

- Design and implement **Spring Boot microservices** (User, Product, Media).
- Implement **JWT** with **Spring Security** for role-based access.
- Enforce **secure file uploads** with validation and size limits.
- Model and persist data in **MongoDB**.
- Build a **secure**, **observable**, and **scalable** platform.

## Project Architecture

This project is structured as a set of Spring Boot microservices, communicating through a central API Gateway.

- **`user-service`**: Handles user authentication, registration (CLIENT or SELLER roles), and profile management.
- **`product-service`**: Manages CRUD operations for products, including associating them with media.
- **`media-service`**: Responsible for secure image upload, download, and deletion, including validation.
- **`api-gateway`**: Routes all external traffic to the appropriate microservices.
- **`service-registry`**: (Currently not actively used in this direct testing setup, but part of the microservice architecture for service discovery).

**Technologies Used (Backend):**

- **Spring Boot:** For building microservices.
- **Spring Security:** For authentication and authorization (JWT).
- **MongoDB:** For data persistence.
- **Apache Maven:** For build automation.

## Current State / Implemented APIs

All core API functionalities for the User, Product, and Media services have been implemented and integrated into their respective microservice modules.

- **User Service APIs:** Authentication (register, login) and user profile management (`/me`).
- **Product Service APIs:** Public browsing of products, secure seller-only CRUD operations (create, update, delete), and associating media with products.
- **Media Service APIs:** Secure seller-only image uploads and deletions, and public image downloads.
- **Observability:** All services expose `/actuator/health` endpoints.

## Getting Started

Follow these steps to build, run, and test the microservices locally.

### Prerequisites

- **Java Development Kit (JDK) 24**
- **Apache Maven 3.x**
- **MongoDB (Community Edition or equivalent)**: Ensure a MongoDB instance is running locally (default: `localhost:27017`). The `start_app.sh` script will attempt to start a local MongoDB instance in `./data/db`.
- **Git** (for cloning the repository)

### Build the Project

1.  Clone the repository:
    ```bash
    git clone [YOUR_REPOSITORY_URL]
    cd buy-01
    ```
2.  Make the provided scripts executable:
    ```bash
    chmod +x start_app.sh
    chmod +x stop_app.sh
    ```
3.  The `start_app.sh` script will automatically perform `mvn clean install` for all services. If you wish to build manually:
    ```bash
    mvn clean install
    ```

### Running the Application

To simplify local testing, the `api-gateway` is currently configured for **direct routing** to each microservice (hardcoded `https://localhost:port`) instead of using service discovery (`lb://`). The `service-registry` is therefore not needed for this test setup.

Use the provided scripts to start and stop all microservices:

- **To start all services:**

  ```bash
  ./start_app.sh
  ```

  _(The script will attempt to start MongoDB, build the project, and then launch user-service, product-service, media-service, and api-gateway in separate background processes.)_

- **To stop all running services:**
  ```bash
  ./stop_app.sh
  ```
  _(This will kill processes started by `start_app.sh`, including MongoDB if successfully launched by the script.)_

### Start/Stop MongoDB (local)

Recommended — Homebrew-managed service (one-line):

Start MongoDB:

```bash
brew services start mongodb/brew/mongodb-community@6.0
```

Stop MongoDB:

```bash
brew services stop mongodb/brew/mongodb-community@6.0
```

Manual (project-local DB files)

Start in foreground (Ctrl+C to stop):

```bash
mkdir -p ./data/db
mongod --dbpath ./data/db --bind_ip 127.0.0.1 --port 27017
```

Stop a manually started or forked mongod:

```bash
# if started with --fork
mongod --dbpath ./data/db --shutdown

# or kill the process (replace PID if needed)
pkill -f mongod
```

---

## How to Test the APIs (Detailed Instructions)

Once all services are running via `./start_app.sh`, you can use `curl` (or Postman/Insomnia) to test the API endpoints through the API Gateway (https://localhost:8443).

**_Important:_**

- Replace `<SELLER_TOKEN>`, `<MEDIA_ID>`, and `<PRODUCT_ID>` with the actual values you get from the API responses.
- For image upload, replace `/path/to/your/image.jpg` with a real path to a small JPG or PNG file on your system.
- Ensure MongoDB is running (the `start_app.sh` script attempts to start it).

---

#### **A. User Service (via API Gateway on port 8443):**

- **1. Register a Client User:**

  - **URL:** `https://localhost:8443/api/auth/register`
  - **Method:** `POST`
  - **Headers:** `Content-Type: application/json`
  - **Body:**
    ```json
    {
      "name": "Client User",
      "email": "client@example.com",
      "password": "password",
      "role": "CLIENT"
    }
    ```
  - **`curl` Command:**
    ```bash
    curl -k -X POST https://localhost:8443/api/auth/register \
    -H "Content-Type: application/json" \
    -d '{"name": "Client User", "email": "client@example.com", "password": "password", "role": "CLIENT"}'
    ```
  - **Expected Response:** `HTTP 200 OK` with a JSON body containing a `token`.

- **2. Register a Seller User:**

  - **URL:** `https://localhost:8443/api/auth/register`
  - **Method:** `POST`
  - **Headers:** `Content-Type: application/json`
  - **Body:**
    ```json
    {
      "name": "Seller User",
      "email": "seller@example.com",
      "password": "password",
      "role": "SELLER"
    }
    ```
  - **`curl` Command:**
    ```bash
    curl -k -X POST https://localhost:8443/api/auth/register \
    -H "Content-Type: application/json" \
    -d '{"name": "Seller User", "email": "seller@example.com", "password": "password", "role": "SELLER"}'
    ```
  - **Expected Response:** `HTTP 200 OK` with a JSON body containing a `token`.

- **3. Login as Seller (to get a fresh token if needed):**

  - **URL:** `https://localhost:8443/api/auth/login`
  - **Method:** `POST`
  - **Headers:** `Content-Type: application/json`
  - **Body:**
    ```json
    {
      "email": "seller@example.com",
      "password": "password"
    }
    ```
  - **`curl` Command:**
    ```bash
    curl -k -X POST https://localhost:8443/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email": "seller@example.com", "password": "password"}'
    ```
  - **Expected Response:** `HTTP 200 OK` with a JSON body containing a `token`. **SAVE THIS TOKEN** as `SELLER_TOKEN` for subsequent authenticated requests.

- **4. Get Seller Profile (`GET /users/me`):**

  - **URL:** `https://localhost:8443/api/users/me`
  - **Method:** `GET`
  - **Headers:** `Authorization: Bearer <SELLER_TOKEN>`
  - **`curl` Command:**
    ```bash
    curl -k -X GET https://localhost:8443/api/users/me \
    -H "Authorization: Bearer <SELLER_TOKEN>"
    ```
  - **Expected Response:** `HTTP 200 OK` with a JSON body containing seller user details.

- **5. Update Seller Profile (`PUT /users/me`):**
  - **URL:** `https://localhost:8443/api/users/me`
  - **Method:** `PUT`
  - **Headers:** `Content-Type: application/json`, `Authorization: Bearer <SELLER_TOKEN>`
  - **Body:**
    ```json
    {
      "name": "Updated Seller Name",
      "avatar": "http://example.com/new_avatar.png"
    }
    ```
  - **`curl` Command:**
    ```bash
    curl -k -X PUT https://localhost:8443/api/users/me \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer <SELLER_TOKEN>" \
    -d '{"name": "Updated Seller Name", "avatar": "http://example.com/new_avatar.png"}'
    ```
  - **Expected Response:** `HTTP 200 OK` with a JSON body containing updated seller user details.

---

#### **B. Media Service (via API Gateway on port 8443):**

- **1. Upload an Image (as Seller):**

  - **URL:** `https://localhost:8443/api/media/images`
  - **Method:** `POST`
  - **Headers:** `Authorization: Bearer <SELLER_TOKEN>`
  - **Body:** `multipart/form-data` with a file named `file`. (Replace `/path/to/your/image.jpg` with a real image path)
  - **`curl` Command:**
    ```bash
    curl -k -X POST https://localhost:8443/api/media/images \
    -H "Authorization: Bearer <SELLER_TOKEN>" \
    -F "file=@/path/to/your/image.jpg"
    ```
  - **Expected Response:** `HTTP 200 OK` with a JSON body containing a `Media` object (e.g., `{"id": "...", "originalFilename": "...", "contentType": "...", "size": ..., "filePath": "...", "userId": "..."}`). **SAVE THE `id`** from this response as `MEDIA_ID`.

- **2. Download the Image:**

  - **URL:** `https://localhost:8443/api/media/images/<MEDIA_ID>`
  - **Method:** `GET`
  - **`curl` Command:**
    ```bash
    curl -k -X GET https://localhost:8443/api/media/images/<MEDIA_ID> \
    --output downloaded_image.jpg
    ```
  - **Expected Response:** `HTTP 200 OK`. An image file should be downloaded to `downloaded_image.jpg` in your current directory.

- **3. Delete the Image (as Seller):**
  - **URL:** `https://localhost:8443/api/media/images/<MEDIA_ID>`
  - **Method:** `DELETE`
  - **Headers:** `Authorization: Bearer <SELLER_TOKEN>`
  - **`curl` Command:**
    ```bash
    curl -k -X DELETE https://localhost:8443/api/media/images/<MEDIA_ID> \
    -H "Authorization: Bearer <SELLER_TOKEN>"
    ```
  - **Expected Response:** `HTTP 204 No Content`.

---

#### **C. Product Service (via API Gateway on port 8443):**

- **1. Get All Products (Public):**

  - **URL:** `https://localhost:8443/api/products`
  - **Method:** `GET`
  - **`curl` Command:**
    ```bash
    curl -k -X GET https://localhost:8443/api/products
    ```
  - **Expected Response:** `HTTP 200 OK` with a JSON array of products (initially empty).

- **2. Create a Product (as Seller):**

  - **URL:** `https://localhost:8443/api/products`
  - **Method:** `POST`
  - **Headers:** `Content-Type: application/json`, `Authorization: Bearer <SELLER_TOKEN>`
  - **Body:**
    ```json
    {
      "name": "My Awesome Product",
      "description": "A very cool product.",
      "price": 99.99,
      "quantity": 100
    }
    ```
  - **`curl` Command:**
    ```bash
    curl -k -X POST https://localhost:8443/api/products \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer <SELLER_TOKEN>" \
    -d '{"name": "My Awesome Product", "description": "A very cool product.", "price": 99.99, "quantity": 100}'
    ```
  - **Expected Response:** `HTTP 200 OK` with a JSON body containing a `Product` object (e.g., `{"id": "...", "name": "...", "userId": "...", "mediaIds": []}`). **SAVE THE `id`** from this response as `PRODUCT_ID`.

- **3. Associate Media with Product (as Seller):**

  - **URL:** `https://localhost:8443/api/products/<PRODUCT_ID>/media/<MEDIA_ID>`
  - **Method:** `POST`
  - **Headers:** `Authorization: Bearer <SELLER_TOKEN>`
  - **`curl` Command:**
    ```bash
    curl -k -X POST https://localhost:8443/api/products/<PRODUCT_ID>/media/<MEDIA_ID> \
    -H "Authorization: Bearer <SELLER_TOKEN>"
    ```
  - **Expected Response:** `HTTP 200 OK` with a JSON body containing the updated `Product` object. The `mediaIds` list within the `Product` object should now include your `MEDIA_ID`.

- **4. Get Product by ID (Public):**

  - **URL:** `https://localhost:8443/api/products/<PRODUCT_ID>`
  - **Method:** `GET`
  - **`curl` Command:**
    ```bash
    curl -k -X GET https://localhost:8443/api/products/<PRODUCT_ID>
    ```
  - **Expected Response:** `HTTP 200 OK` with a JSON body containing the product details.

- **5. Update Product (as Seller):**

  - **URL:** `https://localhost:8443/api/products/<PRODUCT_ID>`
  - **Method:** `PUT`
  - **Headers:** `Content-Type: application/json`, `Authorization: Bearer <SELLER_TOKEN>`
  - **Body:**
    ```json
    {
      "name": "Updated Product Name",
      "description": "Even cooler product.",
      "price": 120.0,
      "quantity": 90
    }
    ```
  - **`curl` Command:**
    ```bash
    curl -k -X PUT https://localhost:8443/api/products/<PRODUCT_ID> \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer <SELLER_TOKEN>" \
    -d '{"name": "Updated Product Name", "description": "Even cooler product.", "price": 120.00, "quantity": 90}'
    ```
  - **Expected Response:** `HTTP 200 OK` with a JSON body containing the updated `Product` object.

- **6. Delete Product (as Seller):**
  - **URL:** `https://localhost:8443/api/products/<PRODUCT_ID>`
  - **Method:** `DELETE`
  - **Headers:** `Authorization: Bearer <SELLER_TOKEN>`
  - **`curl` Command:**
    ```bash
    curl -k -X DELETE https://localhost:8443/api/products/<PRODUCT_ID> \
    -H "Authorization: Bearer <SELLER_TOKEN>"
    ```
  - **Expected Response:** `HTTP 204 No Content`.

---

#### **D. Actuator Health Checks (Public):**

- **User Service Health:**

  - **URL:** `https://localhost:8443/api/users/actuator/health`
  - **Method:** `GET`
  - **`curl` Command:**
    ```bash
    curl -k -X GET https://localhost:8443/api/users/actuator/health
    ```
  - **Expected Response:** `HTTP 200 OK` with `{"status":"UP"}`.

- **Product Service Health:**

  - **URL:** `https://localhost:8443/api/products/actuator/health`
  - **Method:** `GET`
  - **`curl` Command:**
    ```bash
    curl -k -X GET https://localhost:8443/api/products/actuator/health
    ```
  - **Expected Response:** `HTTP 200 OK` with `{"status":"UP"}`.

- **Media Service Health:**
  - **URL:** `https://localhost:8443/api/media/actuator/health`
  - **Method:** `GET`
  - **`curl` Command:**
    ```bash
    curl -k -X GET https://localhost:8443/api/media/actuator/health
    ```
  - **Expected Response:** `HTTP 200 OK` with `{"status":"UP"}`.

---

## Further Enhancements & Future Work

- **Service Discovery Integration:** Configure all microservices to register with the `service-registry` (e.g., Eureka client setup) and update the API Gateway to use `lb://` routing. This is crucial for dynamic scaling and resilience.
- **Externalize JWT Secret Key:** The `SECRET_KEY` in `JwtService` is currently hardcoded. This should be moved to environment variables or a secure configuration server for production environments.
- **Robust Error Handling:** Implement a global exception handler (`@ControllerAdvice`) to provide consistent and meaningful error responses across all services.
- **CORS Configuration:** Configure Cross-Origin Resource Sharing (CORS) in the API Gateway or individual services to allow requests from your frontend application.
- **Frontend Integration:** Develop the Angular frontend to consume these APIs.
- **Kafka Integration:** Implement asynchronous communication for events like `PRODUCT_CREATED` or `IMAGE_UPLOADED` as suggested in the task description.
- **HTTPS:** Secure communication with HTTPS (e.g., using Let's Encrypt).

---

## Running in Docker

This repository includes a Docker Compose setup for local development. Use the compose stack when you want an isolated environment for MongoDB, Zookeeper, Kafka and the microservices.

Start the full Docker Compose stack (builds images if necessary):

```bash
./start_docker.sh
```

Tail logs for a single service (replace <service> with `product-service`, `user-service`, `api-gateway`, etc.):

```bash
docker compose logs -f <service>
```

Or just show the last 10 lines:

```bash
docker compose logs --tail=10 product-service             
docker compose logs --tail=10 kafka
```


Stop only the Docker Compose stack (safe):

```bash
./shutdown_all.sh
```

Stop and also remove local images and volumes (destructive):

```bash
./shutdown_all.sh --cleanup
```

Notes:
- If you previously used `start_app.sh`/`stop_app.sh` to run services as host processes, those scripts manage JVM PIDs under `.pids`. `shutdown_all.sh` only affects the Docker Compose stack and will not kill JVM processes started by `start_app.sh`.
- If ports are already in use on your machine (e.g., host Zookeeper or Kafka), stop the host services (e.g. `brew services stop zookeeper kafka`) or remap the ports in `docker-compose.yml`.

## Kafka — implementation and how to inspect topics/messages

Overview:
- Kafka is used for domain events such as product deletion. In `product-service` a `KafkaTemplate<String,String>` is used to publish events to the `product.deleted` topic. The service currently publishes the product id (string) as the message payload and logs deletion events.

How Kafka is configured (local dev):
- Kafka runs in the compose stack (image: `confluentinc/cp-kafka`) and is wired to Zookeeper in the same compose network.
- The broker is configured to listen on `0.0.0.0:9092` inside the container and advertised as `kafka:9092` for container-to-container communication.

Inspect topics and messages (recommended commands to run from your host):

### List topics from inside the kafka container
```bash
docker exec -it buy-01-kafka-1 /bin/bash -c \
  "/usr/bin/kafka-topics --bootstrap-server localhost:9092 --list"
```

### Consume messages from `product.deleted` (prints headers too)
```bash
docker exec -it buy-01-kafka-1 /bin/bash -c \
  "/usr/bin/kafka-console-consumer --bootstrap-server localhost:9092 --topic product.deleted --from-beginning --property print.headers=true"
```

### Produce a test message (quick check)
```bash
docker exec -it buy-01-kafka-1 /bin/bash -c \
  "echo 'test-product-id' | /usr/bin/kafka-console-producer --broker-list localhost:9092 --topic product.deleted"
```

Notes on what you will see:
- The `kafka-console-consumer` with `--property print.headers=true` prints message headers (if present) alongside the payload. Currently you will likely see only the message payload (product id string).
- `product-service` publishes a simple string payload (the product id) by default.
- Correlate messages to logs by searching the product id in the `product-service` logs (the service logs delete operations).


## License

[Add your license information here]
