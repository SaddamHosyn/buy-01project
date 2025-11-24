# SSL/HTTPS Setup for localhost Development

This project uses **mkcert** to generate locally-trusted SSL certificates for HTTPS on localhost. This ensures that your browser (Chrome, Safari, Firefox) trusts the certificates without warnings.

## ✅ What's Already Done

The SSL setup is **already configured**. Here's what was set up:

1. **Generated locally-trusted certificates** using mkcert
2. **Created PKCS12 keystore** at `./certs/keystore.p12` (password: `changeit`)
3. **Configured API Gateway** to use SSL/TLS on port 8443
4. **Mounted certificates** into Docker container
5. **Installed mkcert CA** into your macOS system trust store

## 🚀 Quick Start

The certificates are already generated. Just start the services:

```bash
docker-compose up -d
```

Then visit:

- **Frontend**: http://localhost:4200 (will make API calls to https://localhost:8443)
- **API Gateway**: https://localhost:8443/api/products
- **Eureka Dashboard**: http://localhost:8761

## 🔧 Regenerate Certificates (If Needed)

If you need to regenerate the certificates (e.g., after they expire in 3 years):

```bash
./setup-local-ssl.sh
docker-compose restart api-gateway
```

## 📋 How It Works

### 1. **mkcert** - Local Certificate Authority

- mkcert creates a local CA and installs it in your system's trust store
- Certificates signed by this CA are automatically trusted by:
  - Chrome, Edge, Brave (uses system trust store)
  - Safari (uses system trust store)
  - Firefox (mkcert installs separately into Firefox)

### 2. **Certificate Files**

Located in `./certs/` (gitignored):

- `localhost.pem` - Certificate file
- `localhost-key.pem` - Private key
- `keystore.p12` - PKCS12 keystore for Java/Spring Boot (password: `changeit`)

### 3. **Spring Boot SSL Configuration**

In `api-gateway/src/main/resources/application.properties`:

```properties
server.port=${SERVER_PORT:8443}
server.ssl.key-store=${SERVER_SSL_KEY_STORE:/certs/keystore.p12}
server.ssl.key-store-type=${SERVER_SSL_KEY_STORE_TYPE:PKCS12}
server.ssl.key-store-password=${SERVER_SSL_KEY_STORE_PASSWORD:changeit}
```

### 4. **Docker Compose Mount**

In `docker-compose.yml`:

```yaml
api-gateway:
  volumes:
    - ./certs:/certs:ro # Mount certificates read-only
  environment:
    SERVER_SSL_KEY_STORE: /certs/keystore.p12
    SERVER_SSL_KEY_STORE_PASSWORD: changeit
```

## 🔒 Security Notes

### ⚠️ For Development Only

- These certificates are for **local development only**
- The `./certs/` directory is in `.gitignore` - never commit certificates!
- The mkcert CA is only trusted on **your machine**

### 🏭 For Production

For production deployment, use:

- **Let's Encrypt** (free, automatic renewal)
- **AWS Certificate Manager** (if deploying on AWS)
- **Commercial CA** (DigiCert, GoDaddy, etc.)

## 🛠️ Troubleshooting

### Certificate Not Trusted in Browser

1. **Verify mkcert CA is installed:**

   ```bash
   mkcert -CAROOT
   ```

   This shows where the CA is stored.

2. **Reinstall CA:**

   ```bash
   mkcert -install
   ```

3. **For Firefox specifically:**
   ```bash
   brew install nss  # If not already installed
   mkcert -install
   ```

### Certificate Expired

Certificates are valid for 2 years and 3 months. Regenerate:

```bash
./setup-local-ssl.sh
docker-compose restart api-gateway
```

### Connection Refused on port 8443

Check if API Gateway is running:

```bash
docker ps | grep api-gateway
docker logs buy-01-api-gateway-1 --tail=50
```

### "Given final block not properly padded" Error

This means keystore password mismatch. Verify:

```bash
openssl pkcs12 -info -in ./certs/keystore.p12 -passin pass:changeit -nokeys
```

## 📚 References

- [mkcert GitHub](https://github.com/FiloSottile/mkcert) - Simple zero-config tool for making locally-trusted certificates
- [Spring Boot SSL Documentation](https://docs.spring.io/spring-boot/reference/features/ssl.html)

## ✨ Testing HTTPS

### Command Line

```bash
# Test with curl (should show "SSL certificate verify ok")
curl -v https://localhost:8443/api/products

# Test product listing
curl https://localhost:8443/api/products | jq '.'
```

### Browser

1. Open https://localhost:4200 in Chrome/Safari/Firefox
2. Check the padlock icon in address bar - should show "Connection is secure"
3. Click padlock → Certificate → should show "mkcert development certificate"

No warnings, no errors, no "NET::ERR_CERT_AUTHORITY_INVALID" - just clean HTTPS! 🎉
