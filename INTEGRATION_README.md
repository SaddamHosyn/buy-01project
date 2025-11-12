# Buy-01 E-Commerce Platform - Quick Start Guide

## 🎯 What You Have

A **fully integrated** microservices-based e-commerce platform with:

- ✅ Angular 18 Frontend (Standalone Components)
- ✅ Spring Boot Microservices Backend
- ✅ Netflix Eureka Service Discovery
- ✅ API Gateway with CORS configured
- ✅ MongoDB for data persistence
- ✅ JWT Authentication
- ✅ File Upload System
- ✅ Automated startup/shutdown scripts

## 🚀 Quick Start (3 Steps)

### Prerequisites

Make sure you have installed:

- ✅ **Java 17+** (for Spring Boot)
- ✅ **Maven 3.6+** (for building Java services)
- ✅ **Node.js 18+** (for Angular)
- ✅ **MongoDB** (for database)

### Step 1: Start MongoDB

```bash
# macOS (using Homebrew)
brew services start mongodb-community

# OR manually
mongod --config /opt/homebrew/etc/mongod.conf

# Verify MongoDB is running
mongosh  # Should connect successfully
```

### Step 2: Install Frontend Dependencies

```bash
cd buy-01-ui
npm install
cd ..
```

### Step 3: Start Everything!

```bash
./start_all.sh
```

That's it! The script will:

1. ✅ Check MongoDB is running
2. ✅ Start Service Registry (Eureka) - http://localhost:8761
3. ✅ Start User Service - Port 8081
4. ✅ Start Product Service - Port 8082
5. ✅ Start Media Service - Port 8083
6. ✅ Start API Gateway - http://localhost:8080
7. ✅ Start Angular Frontend - http://localhost:4200

**Wait about 60 seconds for all services to start completely.**

## 🌐 Access the Application

- **Frontend:** http://localhost:4200
- **API Gateway:** http://localhost:8080
- **Eureka Dashboard:** http://localhost:8761

## 🛑 Stop Everything

```bash
./stop_all.sh
```

Gracefully stops all services in reverse order.

## 📋 What Changed During Integration?

See the complete integration documentation: **[INTEGRATION_CHANGES.md](./INTEGRATION_CHANGES.md)**

### Quick Summary:

- ✅ **Removed:** ~600 lines of mock/simulation code
- ✅ **Added:** Real API integration with Spring Boot
- ✅ **Fixed:** CORS configuration for cross-origin requests
- ✅ **Created:** Environment-based configuration
- ✅ **Implemented:** Real multipart file upload
- ✅ **Automated:** Service startup and shutdown

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                 Angular Frontend (4200)                 │
│              Standalone Components + Signals            │
└──────────────────────────┬──────────────────────────────┘
                           │ HTTP Requests
                           ▼
┌─────────────────────────────────────────────────────────┐
│               API Gateway (8080)                        │
│           Routes + CORS + Load Balancing                │
└──────────────────────────┬──────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│        Service Registry - Eureka (8761)                 │
│            Service Discovery + Health Check             │
└─────────┬──────────────┬──────────────┬─────────────────┘
          │              │              │
          ▼              ▼              ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ User Service │  │Product Service│  │Media Service │
│   (8081)     │  │   (8082)      │  │   (8083)     │
│ - Auth       │  │ - Products    │  │ - Images     │
│ - JWT        │  │ - CRUD        │  │ - Upload     │
└──────┬───────┘  └──────┬────────┘  └──────┬───────┘
       │                 │                   │
       ▼                 ▼                   ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│   MongoDB    │  │   MongoDB    │  │   MongoDB    │
│   userdb     │  │  productdb   │  │   mediadb    │
└──────────────┘  └──────────────┘  └──────────────┘
```

## 🔑 Key Features

### Authentication & Authorization

- JWT-based authentication
- Role-based access control (SELLER, CLIENT)
- Secure password storage
- Token refresh on page reload

### Product Management

- Public product browsing
- Seller dashboard for product CRUD
- Product-media association
- Client-side filtering for seller's products

### Media/Image Upload

- Real multipart/form-data upload
- File validation (type, size, extension)
- 2MB file size limit
- Supports: JPG, PNG, WebP
- Parallel upload support

### Microservices Pattern

- Service discovery with Eureka
- API Gateway routing
- Independent service scaling
- MongoDB per service (Database per Service pattern)

## 📁 Project Structure

```
buy-01/
├── api-gateway/              # Spring Cloud Gateway (Port 8080)
├── service-registry/         # Netflix Eureka (Port 8761)
├── user-service/            # User & Auth Service (Port 8081)
├── product-service/         # Product Service (Port 8082)
├── media-service/           # Media/Upload Service (Port 8083)
├── buy-01-ui/              # Angular Frontend (Port 4200)
├── start_all.sh            # 🚀 Start everything
├── stop_all.sh             # 🛑 Stop everything
├── INTEGRATION_CHANGES.md  # 📖 Detailed integration docs
├── logs/                   # Service logs (auto-created)
└── pids/                   # Process IDs (auto-created)
```

## 🧪 Testing the Integration

### 1. Test User Registration

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "seller@test.com",
    "password": "password123",
    "name": "Test Seller",
    "role": "SELLER"
  }'
```

### 2. Test User Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "seller@test.com",
    "password": "password123"
  }'
```

### 3. Test Product Creation (requires JWT token)

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "Test Product",
    "description": "A test product",
    "price": 99.99
  }'
```

### 4. Test Image Upload (requires JWT token)

```bash
curl -X POST http://localhost:8080/api/media/images \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/image.jpg"
```

## 🐛 Troubleshooting

### Services won't start

```bash
# Check if MongoDB is running
mongosh

# Check if ports are already in use
lsof -i :8080  # API Gateway
lsof -i :8761  # Eureka
lsof -i :4200  # Angular

# Kill processes if needed
kill -9 PID
```

### CORS errors

- ✅ Already fixed! CORS is configured in API Gateway
- Allowed origins: http://localhost:4200
- If you change the frontend port, update `api-gateway/src/main/resources/application.yml`

### 401 Unauthorized errors

- Make sure you're logged in
- Check that JWT token is in Authorization header
- Token format: `Bearer YOUR_TOKEN_HERE`

### File upload fails

- Check file size (max 2MB)
- Check file type (JPG, PNG, WebP only)
- Make sure you're using FormData, not JSON

### Services not registering with Eureka

- Wait 20-30 seconds after starting Eureka
- Check Eureka dashboard: http://localhost:8761
- Look at service logs in `logs/` directory

## 📊 Monitoring

### Check Service Health

- **Eureka Dashboard:** http://localhost:8761
- **Service Logs:** `tail -f logs/service-name.log`
- **Process Status:** `ps aux | grep java`

### View Logs

```bash
# All services
tail -f logs/*.log

# Specific service
tail -f logs/user-service.log
tail -f logs/api-gateway.log
tail -f logs/frontend.log
```

## 🔄 Development Workflow

### Making Changes

#### Backend Changes:

```bash
# Stop services
./stop_all.sh

# Make your changes
# ...

# Rebuild and restart
./start_all.sh
```

#### Frontend Changes:

```bash
# Angular has hot reload!
# Just save your files, browser will auto-refresh
# No need to restart
```

### Database Reset

```bash
# Connect to MongoDB
mongosh

# Drop databases
use userdb
db.dropDatabase()

use productdb
db.dropDatabase()

use mediadb
db.dropDatabase()

exit
```

## 📚 API Documentation

### Authentication

- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user

### Users

- `GET /api/users/profile` - Get current user profile
- `PUT /api/users/profile` - Update user profile

### Products

- `GET /api/products` - Get all products (public)
- `GET /api/products/{id}` - Get product by ID (public)
- `POST /api/products` - Create product (SELLER only)
- `PUT /api/products/{id}` - Update product (SELLER only)
- `DELETE /api/products/{id}` - Delete product (SELLER only)
- `POST /api/products/{productId}/media/{mediaId}` - Associate media (SELLER only)

### Media

- `POST /api/media/images` - Upload image (SELLER only)
- `GET /api/media/images/{id}` - Get image (public)
- `DELETE /api/media/images/{id}` - Delete image (SELLER only)

## 🎨 Frontend Features

- Modern Angular 18 with Standalone Components
- Signal-based state management
- Reactive forms with validation
- Route guards for authentication
- HTTP interceptors for auth tokens
- Material Design UI components
- Responsive design
- Image upload with validation
- Real-time error handling

## 🔐 Security Features

- JWT token authentication
- Role-based authorization (SELLER, CLIENT)
- Password hashing (BCrypt)
- CORS protection
- File upload validation
- XSS protection
- CSRF protection

## 📈 Performance

- Lazy loading modules
- Service discovery for load balancing
- Connection pooling in MongoDB
- HTTP caching headers
- Optimized bundle sizes
- Signal-based change detection

## 🎓 Learning Resources

- **Spring Boot Microservices:** https://spring.io/microservices
- **Netflix Eureka:** https://github.com/Netflix/eureka
- **Angular Signals:** https://angular.io/guide/signals
- **JWT Authentication:** https://jwt.io/introduction

## 📞 Need Help?

1. Check **[INTEGRATION_CHANGES.md](./INTEGRATION_CHANGES.md)** for detailed integration documentation
2. Review service logs in `logs/` directory
3. Check Eureka dashboard to see registered services
4. Verify MongoDB is running and accessible

## ✅ Quick Verification Checklist

After starting all services, verify:

- [ ] Eureka shows all 3 services registered (User, Product, Media)
- [ ] API Gateway is accessible at http://localhost:8080
- [ ] Frontend loads at http://localhost:4200
- [ ] Can register a new user
- [ ] Can login and receive JWT token
- [ ] Can view products list
- [ ] Can create product (as SELLER)
- [ ] Can upload images
- [ ] No CORS errors in browser console
- [ ] No 401/403 errors when authenticated

---

**Integration Status:** ✅ **COMPLETE**  
**Ready for Development:** ✅ **YES**  
**Production Ready:** ⚠️ **Needs deployment configuration**

Happy Coding! 🚀
