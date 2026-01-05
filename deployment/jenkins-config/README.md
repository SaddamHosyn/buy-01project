# Jenkins Job Configuration

This directory contains Jenkins job configuration and setup scripts for easy pipeline deployment.

---

## 📋 Job DSL Script

Use this Job DSL script to automatically create the CI/CD pipeline job.

### Prerequisites

1. **Install Job DSL Plugin:**

   ```
   Manage Jenkins → Plugins → Available
   Search: "Job DSL"
   Install and restart
   ```

2. **Create Seed Job:**

   ```
   Dashboard → New Item
   Name: seed-job
   Type: Freestyle project
   ```

3. **Configure Seed Job:**

   - Build → Add build step → Process Job DSLs
   - Use the provided DSL script: `jenkins-job-dsl.groovy`
   - Save

4. **Run Seed Job:**
   - Click "Build Now"
   - This will create the `buy-01-cicd-pipeline` job

---

## 🔧 Manual Job Configuration (XML)

If you prefer manual configuration, use the provided XML export.

### Import Job from XML

**Method 1: Via CLI**

```bash
# Download Jenkins CLI
wget http://localhost:8086/jnlpJars/jenkins-cli.jar

# Create job from XML
cat jenkins-job-config.xml | java -jar jenkins-cli.jar -s http://localhost:8086 \
  -auth admin:your-token create-job buy-01-cicd-pipeline
```

**Method 2: Via UI**

```
1. Copy jenkins-job-config.xml content
2. Dashboard → New Item
3. Name: buy-01-cicd-pipeline
4. Type: Pipeline
5. OK
6. Configure → Pipeline section
7. Definition: Pipeline script from SCM
8. SCM: Git
9. Repository URL: https://github.com/SaddamHosyn/buy-01project.git
10. Branch: */main
11. Script Path: deployment/Jenkinsfile
12. Save
```

---

## 📝 Quick Setup Commands

```bash
# 1. Start Jenkins
cd deployment
docker-compose up -d

# 2. Get initial password
docker exec jenkins-master cat /var/jenkins_home/secrets/initialAdminPassword

# 3. Access Jenkins
# http://localhost:8086

# 4. Complete setup wizard
# Install suggested plugins

# 5. Add credentials (see JENKINS-SECURITY.md)
# - docker-hub-credentials (Username/Password)
# - docker-hub-username (Secret text)
# - slack-webhook-url (Secret text) - Optional
# - email-credentials (Username/Password) - Optional

# 6. Create pipeline job using one of the methods above

# 7. Run first build
# Build with Parameters → Build
```

---

## 🔄 Export Current Job Configuration

If you modify the job and want to export the configuration:

**Via CLI:**

```bash
java -jar jenkins-cli.jar -s http://localhost:8086 \
  -auth admin:your-token get-job buy-01-cicd-pipeline > jenkins-job-config-backup.xml
```

**Via UI:**

```
1. Navigate to job
2. Configure
3. Copy all settings manually
4. Or use "Job Configuration History" plugin
```

---

## 📦 Included Files

- **jenkins-job-dsl.groovy** - Job DSL script for automated job creation
- **jenkins-job-config.xml** - XML export of pipeline job configuration
- **setup-credentials.groovy** - Script to add credentials programmatically
- **README.md** - This file

---

## 🚀 Automated Setup Script

For fully automated setup, run:

```bash
./jenkins-setup.sh
```

This script will:

1. Start Jenkins container
2. Wait for initialization
3. Install required plugins
4. Create credentials (from environment variables)
5. Create pipeline job
6. Trigger first build

**Required environment variables:**

```bash
export DOCKER_HUB_USERNAME="your-username"
export DOCKER_HUB_PASSWORD="your-password"
export SLACK_WEBHOOK_URL="https://hooks.slack.com/services/XXX/YYY/ZZZ"  # Optional
export EMAIL_USERNAME="smtp-username"  # Optional
export EMAIL_PASSWORD="smtp-password"  # Optional
```

---

## 📞 Support

For setup issues, see:

- [Main README](../README.md)
- [Jenkins Documentation](README.md)
- [Security Guide](JENKINS-SECURITY.md)
- [Audit Checklist](AUDIT-CHECKLIST.md)
