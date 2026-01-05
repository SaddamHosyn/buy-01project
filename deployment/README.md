# Jenkins CI/CD Pipeline Documentation

Complete guide for setting up and using Jenkins CI/CD pipeline for the Buy-01 E-Commerce Platform.

---

## 📑 Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [Quick Start](#quick-start)
4. [Detailed Setup](#detailed-setup)
5. [Pipeline Stages](#pipeline-stages)
6. [Configuration](#configuration)
7. [Testing](#testing)
8. [Troubleshooting](#troubleshooting)
9. [Best Practices](#best-practices)

---

## 🎯 Overview

This Jenkins pipeline provides:

- **Automated builds** triggered by Git commits
- **Comprehensive testing** (JUnit + Karma/Jasmine)
- **Docker image management** (build, tag, push)
- **Automated deployment** with health checks
- **Rollback strategy** on failures
- **Multi-channel notifications** (Slack + Email)
- **Security scanning** for secrets and vulnerabilities

**Pipeline Flow:**

```
Git Push → Jenkins Auto-Trigger → Checkout → Backend Tests → Frontend Tests
→ Build JARs → Build Docker Images → Push to Registry → Deploy → Health Check
→ Notify (Success/Failure) → Rollback (if needed)
```

---

## 📋 Prerequisites

### Required Software

- ✅ **Docker** 20.10+ and Docker Compose 2.0+
- ✅ **Git** 2.30+
- ✅ **GitHub Account** with repository access
- ✅ **Docker Hub Account** (for image registry)

### Optional

- Slack workspace (for notifications)
- Email server/SMTP (for email notifications)

### System Requirements

- **RAM:** 8GB minimum (16GB recommended)
- **Disk:** 20GB free space
- **CPU:** 4 cores minimum
- **OS:** Linux, macOS, or Windows with WSL2

---

## 🚀 Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/SaddamHosyn/buy-01project.git
cd buy-01project
```

### 2. Create Docker Network

```bash
docker network create buy-01-network
```

### 3. Start Jenkins

```bash
cd deployment
docker-compose up -d
```

### 4. Wait for Initialization

```bash
# Jenkins takes 2-3 minutes to start
docker logs -f jenkins-master

# Look for: "Jenkins is fully up and running"
```

### 5. Access Jenkins

- **URL:** http://localhost:8086

### 6. Get Jenkins Administrator Password

Jenkins requires an initial admin password for first-time setup. Retrieve it using:

```bash
docker exec jenkins-master cat /var/jenkins_home/secrets/initialAdminPassword
```

**Alternative methods:**

```bash
# Method 1: View in logs
docker logs jenkins-master 2>&1 | grep -A 2 "Please use the following password"

# Method 2: PowerShell (Windows)
docker exec jenkins-master cat /var/jenkins_home/secrets/initialAdminPassword
```

**Example output:**

```
7fbb4d4812cf418c9df5c5744e85704f
```

Copy this password and paste it into the "Administrator password" field on the Jenkins setup page.

### 7. Complete Setup Wizard

Follow on-screen instructions (see [Detailed Setup](#detailed-setup))

---

## 🔧 Detailed Setup

### Step 1: Initial Jenkins Configuration

1. **Access Jenkins**

   - Navigate to http://localhost:8086
   - Enter the initial admin password

2. **Install Plugins**

   - Choose "Install suggested plugins"
   - **Additional required plugins:**
     - Git Plugin
     - Pipeline Plugin
     - Docker Pipeline
     - JUnit Plugin
     - Email Extension Plugin
     - Slack Notification Plugin (optional)
     - Blue Ocean (optional, for better UI)

3. **Create Admin User**

   ```
   Username: admin
   Password: [secure-password]
   Full Name: CI/CD Admin
   Email: admin@example.com
   ```

4. **Configure Jenkins URL**
   - Jenkins URL: `http://localhost:8086`
   - System Admin Email: `admin@example.com`

### Step 2: Configure Security

Navigate to: **Manage Jenkins → Security**

1. **Enable Security:**

   - ✅ Enable security
   - Security Realm: Jenkins' own user database
   - Authorization: Logged-in users can do anything

2. **For Production (Recommended):**
   - Authorization: Matrix-based security
   - Configure roles (see [JENKINS-SECURITY.md](JENKINS-SECURITY.md))

### Step 3: Add Credentials

Navigate to: **Manage Jenkins → Credentials → System → Global credentials**

#### Docker Hub Credentials

```
Kind: Username with password
Scope: Global
ID: docker-hub-credentials
Username: [your-docker-hub-username]
Password: [your-docker-hub-password]
Description: Docker Hub credentials for image push
```

#### Slack Webhook (Optional)

```
Kind: Secret text
Scope: Global
ID: slack-webhook-url
Secret: https://hooks.slack.com/services/YOUR/WEBHOOK/URL
Description: Slack webhook for build notifications
```

#### Email Credentials (Optional)

```
Kind: Username with password
Scope: Global
ID: email-credentials
Username: [smtp-username]
Password: [smtp-password]
Description: SMTP credentials for email notifications
```

**💡 How to get Slack webhook:**

1. Go to https://api.slack.com/apps
2. Create new app → From scratch
3. Add "Incoming Webhooks" feature
4. Create webhook for your channel
5. Copy webhook URL

### Step 4: Configure Email (Optional)

Navigate to: **Manage Jenkins → System → Extended E-mail Notification**

```
SMTP Server: smtp.gmail.com
SMTP Port: 587
Use TLS: ✅ Enabled
Credentials: [select email-credentials]
Default Content Type: text/html
Default Recipients: team@example.com
Reply-To Address: noreply@jenkins.example.com
```

**For Gmail:**

- Enable 2FA on your Google account
- Generate App Password: https://myaccount.google.com/apppasswords
- Use app password in credentials

### Step 5: Create Pipeline Job

1. **Create New Job**

   - Dashboard → New Item
   - Name: `buy-01-cicd-pipeline`
   - Type: Pipeline
   - Click OK

2. **Configure Job**

   **General:**

   - ✅ GitHub project
   - Project URL: `https://github.com/SaddamHosyn/buy-01project`
   - ✅ This project is parameterized (handled by Jenkinsfile)

   **Build Triggers:**

   - ✅ Poll SCM (handled by Jenkinsfile)
   - Schedule: `H/5 * * * *` (every 5 minutes)

   **Pipeline:**

   ```
   Definition: Pipeline script from SCM
   SCM: Git
   Repository URL: https://github.com/SaddamHosyn/buy-01project.git
   Credentials: [leave empty for public repo]
   Branch: */main
   Script Path: deployment/Jenkinsfile
   ```

3. **Save Configuration**

### Step 6: First Build

1. **Manual Trigger**

   - Click "Build with Parameters"
   - DEPLOY_ENV: `local`
   - SKIP_TESTS: `false`
   - FORCE_DEPLOY: `false`
   - Click "Build"

2. **Monitor Build**

   - View console output: Build → Console Output
   - Or use Blue Ocean: Open Blue Ocean link

3. **Verify Success**
   - All stages should be green ✅
   - Check deployed services:
     ```bash
     docker ps
     curl http://localhost:8761  # Service Registry
     curl http://localhost:8080  # API Gateway
     ```

---

## 📊 Pipeline Stages

### Stage 1: Checkout

**Duration:** ~10 seconds

**What it does:**

- Clones latest code from GitHub
- Checks out specified branch (jenkindev)
- Wipes workspace for clean build

**Console Output:**

```
📦 Checking out code...
Cloning into workspace...
Commit: a1b2c3d4 (Latest commit message)
```

### Stage 2: Backend Tests

**Duration:** ~2-3 minutes

**What it does:**

- Runs JUnit tests for 3 microservices
- Executes: `mvn clean test`
- Services tested: user-service, product-service, media-service
- Publishes test results

**Can be skipped:** Yes (with `SKIP_TESTS=true`)

**Console Output:**

```
🧪 Running backend tests...
[user-service] Tests run: 2, Failures: 0, Errors: 0
[product-service] Tests run: 2, Failures: 0, Errors: 0
[media-service] Tests run: 2, Failures: 0, Errors: 0
✅ Backend tests passed!
```

### Stage 3: Frontend Tests

**Duration:** ~3-4 minutes

**What it does:**

- Installs npm dependencies
- Runs Karma/Jasmine tests
- Generates code coverage
- Uses ChromeHeadless for CI

**Can be skipped:** Yes (with `SKIP_TESTS=true`)

**Console Output:**

```
🧪 Running frontend tests (Karma/Jasmine)...
Installing dependencies...
Running tests with ChromeHeadlessCI...
Chrome Headless: Executed X of X SUCCESS
✅ Frontend tests passed!
```

### Stage 4: Secret Scanning (New)

**Duration:** ~10 seconds

**What it does:**

- Scans code for accidentally committed secrets
- Detects API keys, passwords, tokens
- Fails build if secrets found

**Patterns detected:**

- AWS keys
- Private keys
- Passwords in config files
- API tokens

### Stage 5: Code Quality (New)

**Duration:** ~1-2 minutes

**What it does:**

- Runs Checkstyle for Java
- Runs PMD for code analysis
- Generates quality reports

**Can be skipped:** No (but warnings don't fail build)

### Stage 6: Build Backend JARs

**Duration:** ~3-4 minutes

**What it does:**

- Compiles Java source code
- Packages into JAR files
- Services: service-registry, user-service, product-service, media-service
- Executes: `mvn clean package -DskipTests`

**Output:**

```
📦 Building backend JAR files...
[service-registry] BUILD SUCCESS
[user-service] BUILD SUCCESS
[product-service] BUILD SUCCESS
[media-service] BUILD SUCCESS
```

### Stage 7: Build Docker Images

**Duration:** ~5-7 minutes

**What it does:**

- Builds 6 Docker images in parallel
- Tags with build number and :latest
- Images: service-registry, api-gateway, user-service, product-service, media-service, frontend

**Parallel execution saves ~70% time**

**Output:**

```
🐳 Building Docker Images in parallel with tag: 42...
✅ service-registry:42 built
✅ api-gateway:42 built
✅ user-service:42 built
✅ product-service:42 built
✅ media-service:42 built
✅ frontend:42 built
```

### Stage 8: Push to Docker Hub

**Duration:** ~2-3 minutes

**What it does:**

- Authenticates with Docker Hub
- Pushes all 6 images (versioned + latest)
- Executes in parallel

**Output:**

```
🚀 Pushing images to Docker Hub in parallel...
Pushing hussainsaddam/buy-01-service-registry:42
Pushing hussainsaddam/buy-01-service-registry:latest
[... all services ...]
```

### Stage 9: Deploy

**Duration:** ~1-2 minutes

**What it does:**

- Stops existing containers
- Pulls latest images
- Starts services with docker-compose
- Waits 45s for initialization
- Runs health checks

**Health checks:**

- ✅ Service Registry running
- ✅ API Gateway running
- ✅ User Service running
- ✅ Product Service running
- ✅ Media Service running
- ✅ Frontend running

**Output:**

```
🚀 Deploying to local environment...
Stopping old containers...
Starting new containers...
⏳ Waiting for services to start...
🔍 Running health checks...
✅ All services are running!
```

### Stage 10: Rollback on Failure

**Duration:** ~1-2 minutes

**When it runs:** Only on deployment failure

**What it does:**

- Detects deployment failure
- Pulls previous build images (BUILD_NUMBER - 1)
- Re-tags as :latest
- Redeploys with docker-compose
- Restores last known good state

**Output:**

```
🔄 Rolling back to previous version: 41...
Pulling previous images...
Redeploying...
✅ Rollback completed to build 41
```

---

## ⚙️ Configuration

### Pipeline Parameters

**DEPLOY_ENV** (Choice)

- Options: `local`, `staging`, `production`
- Default: `local`
- Description: Target deployment environment

**SKIP_TESTS** (Boolean)

- Default: `false`
- Description: Skip tests for faster builds (use with caution)
- Use case: Hotfix deployments

**FORCE_DEPLOY** (Boolean)

- Default: `false`
- Description: Force deployment even if no changes
- Use case: Re-deploy after infrastructure changes

### Environment Variables

Edit in Jenkinsfile:

```groovy
environment {
    DOCKER_USER          = credentials('docker-hub-username')  // From credentials
    REGISTRY_CREDENTIALS = 'docker-hub-credentials'
    BUILD_VERSION        = "${env.BUILD_NUMBER}"
}
```

### Docker Registry

**To use different registry:**

```groovy
environment {
    DOCKER_REGISTRY = 'your-registry.io'
    DOCKER_USER     = 'your-username'
}
```

Update image names:

```groovy
sh "docker build -t ${DOCKER_REGISTRY}/${DOCKER_USER}/service-name:${BUILD_VERSION} ."
```

---

## 🧪 Testing

### Test Automatic Triggers

```bash
# Make a change
echo "# Test" >> README.md

# Commit and push
git add README.md
git commit -m "test: auto-trigger"
git push origin main

# Wait up to 5 minutes
# Jenkins will automatically start build
```

### Test Error Handling

```bash
cd deployment

# Create intentional test failure
./test-error-handling.sh

# Commit and push to trigger build
git add .
git commit -m "test: error handling"
git push

# Observe:
# - Build stops at Backend Tests stage
# - Clear error message shown
# - Slack notification sent
# - No deployment occurs

# Restore
./restore-all.sh
```

### Test Rollback Strategy

```bash
cd deployment

# Corrupt deployment configuration
./test-rollback.sh

# Trigger Jenkins build

# Observe:
# - Deployment fails
# - Rollback stage activates
# - Previous version restored
# - Services running previous build

# Restore
cp docker-compose.yml.backup docker-compose.yml
```

### Manual Testing

```bash
# Build without tests (fast)
Build with Parameters:
- DEPLOY_ENV: local
- SKIP_TESTS: true
- FORCE_DEPLOY: false

# Full build with all checks
Build with Parameters:
- DEPLOY_ENV: local
- SKIP_TESTS: false
- FORCE_DEPLOY: false

# Force redeployment
Build with Parameters:
- DEPLOY_ENV: local
- SKIP_TESTS: false
- FORCE_DEPLOY: true
```

---

## 🔍 Troubleshooting

### Issue: Jenkins won't start

**Symptoms:**

```bash
docker ps
# jenkins-master not running
```

**Solution:**

```bash
# Check logs
docker logs jenkins-master

# Common issues:
# 1. Port 8086 already in use
docker ps -a | grep 8086
# Kill process using port

# 2. Insufficient memory
# Increase Docker memory limit (8GB+)

# 3. Volume permissions
docker-compose down
docker volume rm deployment_jenkins_home
docker-compose up -d
```

### Issue: Build fails at test stage

**Symptoms:**

```
Tests run: X, Failures: Y, Errors: Z
BUILD FAILURE
```

**Solution:**

```bash
# Run tests locally
cd user-service
mvn clean test

# Check test logs
cat target/surefire-reports/*.txt

# Fix failing tests
# Commit and push fix
```

### Issue: Docker push authentication failed

**Symptoms:**

```
unauthorized: authentication required
```

**Solution:**

```bash
# Verify credentials in Jenkins
Manage Jenkins → Credentials
Check 'docker-hub-credentials' exists

# Test manually
docker login
Username: your-username
Password: your-password

# Update credentials if needed
```

### Issue: Deployment health check fails

**Symptoms:**

```
❌ service-registry is not running
Deployment health check failed!
```

**Solution:**

```bash
# Check container logs
docker logs buy-01-service-registry

# Common issues:
# 1. Service crashed on startup
# 2. Port conflict
# 3. Dependency not ready (MongoDB, Kafka)

# Check all services
docker ps -a

# Restart manually
cd deployment
docker-compose down
docker-compose up -d

# Check logs
docker-compose logs -f
```

### Issue: Rollback fails

**Symptoms:**

```
Previous build not found
```

**Solution:**

```bash
# Check if previous images exist
docker images | grep buy-01

# If not, manually tag current as previous
docker tag hussainsaddam/buy-01-service-registry:latest \
           hussainsaddam/buy-01-service-registry:41

# Or redeploy from specific version
cd deployment
docker-compose down
docker pull hussainsaddam/buy-01-service-registry:stable
docker-compose up -d
```

### Issue: Notifications not sent

**Symptoms:**

- No Slack/email notifications

**Solution:**

```bash
# Test Slack webhook
curl -X POST -H 'Content-type: application/json' \
  --data '{"text":"Test message"}' \
  https://hooks.slack.com/services/YOUR/WEBHOOK/URL

# Check Jenkins credentials
Manage Jenkins → Credentials
Verify 'slack-webhook-url' exists and is correct

# Check email configuration
Manage Jenkins → System → Extended E-mail Notification
Send test email

# Verify Jenkinsfile has correct credential IDs
```

### Issue: Workspace permission denied

**Symptoms:**

```
Permission denied: /var/jenkins_home/workspace
```

**Solution:**

```bash
# Fix Jenkins home permissions
docker exec -it jenkins-master bash
chown -R jenkins:jenkins /var/jenkins_home
exit

# Restart Jenkins
docker-compose restart jenkins-master
```

---

## ✅ Best Practices

### Security

- ✅ Use Jenkins credentials for all secrets
- ✅ Enable CSRF protection
- ✅ Configure matrix-based security
- ✅ Rotate credentials regularly
- ✅ Enable audit logs
- ✅ Use `--password-stdin` for Docker login

### Performance

- ✅ Use parallel execution for independent tasks
- ✅ Enable Docker layer caching
- ✅ Archive only necessary artifacts
- ✅ Clean workspace after build
- ✅ Use node labels for distributed builds

### Reliability

- ✅ Always have rollback strategy
- ✅ Implement health checks
- ✅ Fail fast on critical errors
- ✅ Use parameterized builds
- ✅ Test pipeline changes in dev first

### Maintenance

- ✅ Regular Jenkins updates
- ✅ Plugin updates
- ✅ Backup Jenkins home regularly
- ✅ Monitor disk space
- ✅ Review failed builds weekly

### CI/CD Pipeline

- ✅ Run tests before deployment
- ✅ Use semantic versioning
- ✅ Tag Docker images properly
- ✅ Keep builds fast (<15 min)
- ✅ Notify team on failures

---

## 📚 Additional Resources

- **Audit Checklist:** [AUDIT-CHECKLIST.md](AUDIT-CHECKLIST.md)
- **Security Guide:** [JENKINS-SECURITY.md](JENKINS-SECURITY.md)
- **Main README:** [../README.md](../README.md)
- **Jenkins Documentation:** https://www.jenkins.io/doc/
- **Docker Documentation:** https://docs.docker.com/
- **GitHub Repository:** https://github.com/SaddamHosyn/buy-01project

---

## 🆘 Support

For issues or questions:

1. Check troubleshooting section above
2. Review Jenkins console output
3. Check Docker logs: `docker logs <container-name>`
4. Open GitHub issue: https://github.com/SaddamHosyn/buy-01project/issues

---

**Happy CI/CD! 🚀**

_Last updated: January 2026_
