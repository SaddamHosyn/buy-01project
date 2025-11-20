# Buy-01 E-Commerce Platform 🛒

A full-stack microservices-based e-commerce platform built with **Spring Boot** and **Angular**, featuring event-driven architecture with **Apache Kafka** and secure **HTTPS** communication.

## 🏗️ Architecture Overview

This project implements a modern microservices architecture with the following components:

### Backend Services (Spring Boot 3.5.6 + Java 17)

- **API Gateway** (Port 8443) - HTTPS entry point with SSL/TLS termination
- **Service Registry** (Port 8761) - Eureka service discovery
- **User Service** (Port 8081) - User authentication and management
- **Product Service** (Port 8082) - Product catalog and inventory
- **Media Service** (Port 8083) - File uploads and media handling

### Frontend

- **Angular 20** (Port 4200) - Modern SPA with Material Design

### Infrastructure

- **Apache Kafka** - Event-driven messaging for cascade operations
- **Zookeeper** - Kafka coordination
- **MongoDB 6.0** - NoSQL database (separate DB per service)

## ✨ Key Features

- 🔐 **JWT Authentication** with role-based access control (SELLER, CLIENT, ADMIN)
- 🔒 **HTTPS/SSL** security with PKCS12 keystore
- 📨 **Event-Driven Architecture** using Kafka for cascade deletions
- 🎯 **Service Discovery** with Eureka for dynamic service registration
- 🗄️ **Database per Service** pattern with MongoDB
- 📁 **File Upload/Download** capabilities for product media
- 🎨 **Modern UI** with Angular Material components
- 🐳 **Docker Compose** for easy deployment

## 🚀 Quick Start

### Prerequisites

- **Java 17** or higher
- **Node.js 18+** and npm
- **Maven 3.6+**
- **Docker & Docker Compose** (for infrastructure)

### Option 1: Run with Docker (Recommended for Backend)

1. **Start infrastructure and backend services:**

```bash
docker-compose up -d
```

2. **Start frontend:**

```bash
cd buy-01-ui
npm install
npm start
```

3. **Access the application:**
   - Frontend: http://localhost:4200
   - API Gateway: https://localhost:8443 (accept self-signed certificate)
   - Eureka Dashboard: http://localhost:8761

### Option 2: Run Without Docker

1. **Start infrastructure only:**

```bash
# Start Kafka, Zookeeper, and MongoDB
docker-compose up -d zookeeper kafka mongodb
```

2. **Build all services:**

```bash
mvn clean install
```

3. **Start backend services using shell scripts:**

```bash
# Start all services
./start_all.sh

# Or start individually
./start_app.sh
```

4. **Start frontend:**

```bash
cd buy-01-ui
npm install
npm start
```

### Option 3: Manual Service Startup

**Terminal 1 - Service Registry:**

```bash
cd service-registry
mvn spring-boot:run
```

**Terminal 2 - API Gateway:**

```bash
cd api-gateway
mvn spring-boot:run
```

**Terminal 3 - User Service:**

```bash
cd user-service
mvn spring-boot:run
```

**Terminal 4 - Product Service:**

```bash
cd product-service
mvn spring-boot:run
```

**Terminal 5 - Media Service:**

```bash
cd media-service
mvn spring-boot:run
```

**Terminal 6 - Frontend:**

```bash
cd buy-01-ui
npm start
```

## 📊 Service Ports

| Service          | Port  | Protocol | Description         |
| ---------------- | ----- | -------- | ------------------- |
| Frontend         | 4200  | HTTP     | Angular application |
| API Gateway      | 8443  | HTTPS    | Main entry point    |
| Service Registry | 8761  | HTTP     | Eureka dashboard    |
| User Service     | 8081  | HTTP     | Internal service    |
| Product Service  | 8082  | HTTP     | Internal service    |
| Media Service    | 8083  | HTTP     | Internal service    |
| MongoDB          | 27017 | TCP      | Database            |
| Kafka            | 9092  | TCP      | Message broker      |
| Zookeeper        | 2182  | TCP      | Kafka coordination  |

## 🔄 Event-Driven Flow

The system uses Kafka for cascade deletion operations:

```
User Deletion → Kafka Topic: user.deleted → Product Service
                                          ↓
                              Delete User's Products → Kafka Topic: product.deleted
                                                      ↓
                                                  Media Service
                                                      ↓
                                              Delete Product Media Files
```

## 🗂️ Project Structure

```
buy-01/
├── api-gateway/          # HTTPS gateway with routing
├── service-registry/     # Eureka server
├── user-service/         # User management & auth
├── product-service/      # Product catalog
├── media-service/        # File management
├── buy-01-ui/           # Angular frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/        # Core services & guards
│   │   │   ├── features/    # Feature modules
│   │   │   └── shared/      # Shared components
│   │   └── environments/    # Environment configs
├── docker-compose.yml    # Infrastructure setup
├── pom.xml              # Maven parent POM
└── README.md
```

## 🛠️ Technologies Used

### Backend

- Spring Boot 3.5.6
- Spring Cloud (Eureka, Gateway)
- Spring Security with JWT
- Spring Data MongoDB
- Spring Kafka
- Maven

### Frontend

- Angular 20
- Angular Material
- RxJS
- TypeScript 5.9

### Infrastructure

- Apache Kafka
- MongoDB 6.0
- Docker & Docker Compose

## 🔐 Security Features

- **HTTPS/TLS**: All external communication encrypted via API Gateway
- **JWT Tokens**: Stateless authentication with role-based authorization
- **CORS**: Configured to allow frontend access
- **SSL Termination**: API Gateway handles SSL, internal services use HTTP
- **Self-Signed Certificate**: Included for development (keystore.p12)

## 🧪 Testing

**Backend Tests:**

```bash
mvn test
```

**Frontend Tests:**

```bash
cd buy-01-ui
npm test
```

## 📝 Environment Variables

Key environment variables (configured in `docker-compose.yml`):

```yaml
SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
SPRING_DATA_MONGODB_URI: mongodb://root:example@mongodb:27017/{dbname}?authSource=admin
EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://service-registry:8761/eureka/
```

## 🐛 Troubleshooting

**Issue: Services can't connect to Kafka**

```bash
# Check Kafka is running
docker ps | grep kafka
# Check Kafka logs
docker-compose logs kafka
```

**Issue: MongoDB connection failed**

```bash
# Verify MongoDB is running
docker ps | grep mongodb
# Test connection
docker exec -it mongodb mongosh -u root -p example
```

**Issue: Frontend can't reach backend**

- Ensure API Gateway is running on port 8443
- Accept the self-signed certificate in your browser
- Check CORS settings in `api-gateway/application.yml`

**Issue: Services not registering with Eureka**

- Wait 30 seconds after startup
- Check Eureka dashboard: http://localhost:8761

## 🛑 Stopping the Application

**Stop all Docker services:**

```bash
docker-compose down
```

**Stop shell-started services:**

```bash
./stop_all.sh
```

## 👥 Default Users

After first run, you can register users via the frontend or use the API:

**Register via API:**

```bash
curl -X POST https://localhost:8443/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "seller1",
    "email": "seller@example.com",
    "password": "password123",
    "role": "SELLER"
  }'
```

## 📚 API Documentation

Access API endpoints through the gateway:

**Authentication:**

- POST `/api/auth/register` - Register new user
- POST `/api/auth/login` - Login and get JWT token

**Users:**

- GET `/api/users` - Get all users (ADMIN only)
- GET `/api/users/{id}` - Get user by ID
- DELETE `/api/users/{id}` - Delete user (triggers cascade)

**Products:**

- GET `/api/products` - Get all products
- POST `/api/products` - Create product (SELLER only)
- DELETE `/api/products/{id}` - Delete product (triggers cascade)

**Media:**

- POST `/api/media/upload` - Upload file
- GET `/api/media/download/{filename}` - Download file

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is developed for educational purposes.

## 👨‍💻 Authors

- [@jedi](https://github.com/jeeeeedi) [@oafilali](https://github.com/oafilali) [@Anastasia](https://github.com/...) [@SaddamHosyn](https://github.com/SaddamHosyn)

---

**Built with ❤️ using Spring Boot and Angular**
