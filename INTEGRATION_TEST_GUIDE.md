# Backend-Frontend Integration Testing Guide

## Quick Verification Script

Run this anytime to check if everything is working:
```bash
cd ~/Desktop/buy-01
bash /tmp/verify_integration.sh
```

## Manual Testing Steps

### 1. Check All Services Are Running
```bash
# Should show 6 processes
lsof -ti:8761,8080,8081,8082,8083,4200 | wc -l
```

### 2. Test Backend APIs Directly

**a) Test API Gateway Health:**
```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

**b) Test Products API:**
```bash
curl http://localhost:8080/api/products | python3 -m json.tool | head -30
# Expected: JSON array of products
```

**c) Test Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"seller1","password":"password"}'
# Expected: JSON with token and user info
```

### 3. Test Frontend-Backend Connection

**Open Browser:**
1. Go to: http://localhost:4200
2. Open Developer Tools (F12)
3. Go to Network tab
4. Refresh the page

**What to check:**
- ✅ No CORS errors in console
- ✅ API calls go to `http://localhost:8080/api/*`
- ✅ Status codes are 200 (or 401 for auth required endpoints)

### 4. Test Complete User Flow

**In the browser (http://localhost:4200):**

1. **Register a new user:**
   - Click "Register"
   - Fill form with SELLER role
   - Submit
   - Check Network tab: `POST http://localhost:8080/api/auth/register`

2. **Login:**
   - Use username/password
   - Check Network tab: `POST http://localhost:8080/api/auth/login`
   - Should receive token and redirect

3. **View Products:**
   - Go to product listing
   - Check Network tab: `GET http://localhost:8080/api/products`
   - Images should load from: `http://localhost:8080/api/media/images/*`

4. **Create Product (as Seller):**
   - Click "Add Product"
   - Fill form and upload images
   - Check Network tab for multiple calls:
     - `POST http://localhost:8080/api/media/images` (upload images)
     - `POST http://localhost:8080/api/products` (create product)
     - `PUT http://localhost:8080/api/products/*/media/*` (associate)

### 5. Test Eureka Service Discovery

**Open Eureka Dashboard:**
```bash
open http://localhost:8761
```

**Should see 4 registered services:**
- API-GATEWAY
- USER-SERVICE
- PRODUCT-SERVICE
- MEDIA-SERVICE

### 6. Test CORS Configuration

```bash
curl -H "Origin: http://localhost:4200" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -X OPTIONS http://localhost:8080/api/products -I
```

**Look for these headers:**
```
Access-Control-Allow-Origin: http://localhost:4200
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: *
```

### 7. Check Service Logs

If something doesn't work, check the logs:

```bash
# API Gateway
tail -f ~/Desktop/buy-01/logs/api-gateway.log

# User Service
tail -f ~/Desktop/buy-01/logs/user-service.log

# Product Service
tail -f ~/Desktop/buy-01/logs/product-service.log

# Media Service
tail -f ~/Desktop/buy-01/logs/media-service.log

# Frontend
tail -f ~/Desktop/buy-01/logs/frontend.log
```

## Common Issues and Solutions

### Issue: CORS errors in browser console
**Solution:** Check API Gateway CORS configuration
```bash
# Check if API Gateway is running
curl http://localhost:8080/actuator/health
```

### Issue: API calls return 404
**Solution:** Services may not be registered with Eureka yet
```bash
# Wait 30 seconds and check Eureka
curl http://localhost:8761/eureka/apps | grep -o "<name>.*</name>"
```

### Issue: Images not loading
**Solution:** Check Media Service and file paths
```bash
# Test media service directly
curl http://localhost:8083/media/images/{some-id}
```

### Issue: Authentication not working
**Solution:** Check JWT token in localStorage
```javascript
// In browser console:
localStorage.getItem('token')
localStorage.getItem('currentUser')
```

## Integration Checklist

- [ ] All 6 services running
- [ ] Eureka shows 4 registered services
- [ ] API Gateway health check passes
- [ ] Frontend loads without errors
- [ ] Can register new user
- [ ] Can login successfully
- [ ] Can view products
- [ ] Can create product (as seller)
- [ ] Images display correctly
- [ ] No CORS errors in console
- [ ] Network tab shows calls to localhost:8080

## Quick Commands

**Start everything:**
```bash
cd ~/Desktop/buy-01
bash start_all.sh
```

**Stop everything:**
```bash
cd ~/Desktop/buy-01
bash stop_all.sh
```

**Restart everything:**
```bash
cd ~/Desktop/buy-01
bash stop_all.sh && sleep 3 && bash start_all.sh
```

**Check what's running:**
```bash
lsof -ti:8761,8080,8081,8082,8083,4200
```

**Run verification:**
```bash
bash /tmp/verify_integration.sh
```

---

**Last Updated:** $(date)
