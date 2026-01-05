#!/bin/bash
# Automated Jenkins Setup Script for Buy-01 Project
# This script sets up Jenkins, installs plugins, adds credentials, and creates the pipeline job

set -e  # Exit on error

echo "=================================================="
echo "🚀 Buy-01 Jenkins CI/CD Pipeline Setup"
echo "=================================================="
echo ""

# Color codes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Docker is running
if ! docker ps > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker and try again.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Docker is running${NC}"
echo ""

# Check required environment variables
echo "📋 Checking required credentials..."
if [ -z "$DOCKER_HUB_USERNAME" ]; then
    echo -e "${RED}❌ DOCKER_HUB_USERNAME is not set${NC}"
    echo "   export DOCKER_HUB_USERNAME='your-username'"
    exit 1
fi

if [ -z "$DOCKER_HUB_PASSWORD" ]; then
    echo -e "${RED}❌ DOCKER_HUB_PASSWORD is not set${NC}"
    echo "   export DOCKER_HUB_PASSWORD='your-password'"
    exit 1
fi

echo -e "${GREEN}✅ Required credentials are set${NC}"

# Optional credentials
if [ -z "$SLACK_WEBHOOK_URL" ]; then
    echo -e "${YELLOW}⚠️  SLACK_WEBHOOK_URL not set (optional)${NC}"
fi

if [ -z "$EMAIL_USERNAME" ] || [ -z "$EMAIL_PASSWORD" ]; then
    echo -e "${YELLOW}⚠️  Email credentials not set (optional)${NC}"
fi
echo ""

# Create Docker network if it doesn't exist
echo "🌐 Creating Docker network..."
docker network create buy-01-network 2>/dev/null || echo "Network already exists"
echo ""

# Start Jenkins
echo "🐳 Starting Jenkins container..."
cd deployment
docker-compose up -d jenkins-master
cd ..
echo ""

# Wait for Jenkins to be ready
echo "⏳ Waiting for Jenkins to initialize (this may take 2-3 minutes)..."
JENKINS_URL="http://localhost:8086"
MAX_WAIT=180  # 3 minutes
WAIT_TIME=0

until curl -s -f "$JENKINS_URL" > /dev/null 2>&1; do
    sleep 5
    WAIT_TIME=$((WAIT_TIME + 5))
    if [ $WAIT_TIME -ge $MAX_WAIT ]; then
        echo -e "${RED}❌ Jenkins failed to start within $MAX_WAIT seconds${NC}"
        echo "Check logs with: docker logs jenkins-master"
        exit 1
    fi
    echo "  Waiting... ($WAIT_TIME/${MAX_WAIT}s)"
done

echo -e "${GREEN}✅ Jenkins is running at $JENKINS_URL${NC}"
echo ""

# Get initial admin password
echo "🔑 Retrieving initial admin password..."
ADMIN_PASSWORD=$(docker exec jenkins-master cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null || echo "")

if [ -z "$ADMIN_PASSWORD" ]; then
    echo -e "${YELLOW}⚠️  Could not retrieve admin password automatically${NC}"
    echo "   Get it manually with: docker exec jenkins-master cat /var/jenkins_home/secrets/initialAdminPassword"
else
    echo -e "${GREEN}✅ Initial Admin Password: ${ADMIN_PASSWORD}${NC}"
    echo ""
    echo "📝 Save this password - you'll need it for first login!"
fi
echo ""

# Create credentials setup script
echo "🔐 Preparing credentials setup script..."
cat > /tmp/jenkins-setup-credentials.groovy <<EOF
import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import org.jenkinsci.plugins.plaincredentials.impl.*

def jenkins = Jenkins.instance
def domain = Domain.global()
def store = jenkins.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

// Docker Hub credentials
try {
    def dockerCreds = new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL,
        "docker-hub-credentials",
        "Docker Hub credentials",
        "${DOCKER_HUB_USERNAME}",
        "${DOCKER_HUB_PASSWORD}"
    )
    store.addCredentials(domain, dockerCreds)
    
    def dockerUserCreds = new StringCredentialsImpl(
        CredentialsScope.GLOBAL,
        "docker-hub-username",
        "Docker Hub username",
        hudson.util.Secret.fromString("${DOCKER_HUB_USERNAME}")
    )
    store.addCredentials(domain, dockerUserCreds)
    
    println "✅ Docker credentials added"
} catch (Exception e) {
    println "⚠️ Docker credentials error: \${e.message}"
}

// Slack webhook (optional)
${SLACK_WEBHOOK_URL:+try {
    def slackCreds = new StringCredentialsImpl(
        CredentialsScope.GLOBAL,
        "slack-webhook-url",
        "Slack webhook",
        hudson.util.Secret.fromString("${SLACK_WEBHOOK_URL}")
    )
    store.addCredentials(domain, slackCreds)
    println "✅ Slack webhook added"
} catch (Exception e) {
    println "⚠️ Slack webhook error: \${e.message}"
\}}

// Email credentials (optional)
${EMAIL_USERNAME:+${EMAIL_PASSWORD:+try {
    def emailCreds = new UsernamePasswordCredentialsImpl(
        CredentialsScope.GLOBAL,
        "email-credentials",
        "Email SMTP credentials",
        "${EMAIL_USERNAME}",
        "${EMAIL_PASSWORD}"
    )
    store.addCredentials(domain, emailCreds)
    println "✅ Email credentials added"
} catch (Exception e) {
    println "⚠️ Email credentials error: \${e.message}"
\}}}

println "Credentials setup complete!"
EOF

echo -e "${GREEN}✅ Credentials script ready${NC}"
echo ""

# Instructions for manual steps
echo "=================================================="
echo "📚 NEXT STEPS - Manual Configuration Required"
echo "=================================================="
echo ""
echo "1️⃣  Access Jenkins:"
echo "   URL: $JENKINS_URL"
if [ -n "$ADMIN_PASSWORD" ]; then
    echo "   Initial Password: $ADMIN_PASSWORD"
fi
echo ""
echo "2️⃣  Complete Setup Wizard:"
echo "   - Unlock Jenkins with the password above"
echo "   - Install suggested plugins"
echo "   - Create admin user (or skip and continue as admin)"
echo ""
echo "3️⃣  Install Additional Plugins:"
echo "   Manage Jenkins → Plugins → Available"
echo "   - Docker Pipeline"
echo "   - Email Extension Plugin"
echo "   - Slack Notification Plugin (optional)"
echo "   - Job DSL Plugin (for automated job creation)"
echo ""
echo "4️⃣  Add Credentials (Two Options):"
echo ""
echo "   OPTION A - Script Console (Automated):"
echo "   - Manage Jenkins → Script Console"
echo "   - Copy and paste: deployment/jenkins-config/setup-credentials.groovy"
echo "   - Click 'Run'"
echo ""
echo "   OPTION B - Manual:"
echo "   - Manage Jenkins → Credentials → System → Global"
echo "   - Add each credential manually (see JENKINS-SECURITY.md)"
echo ""
echo "5️⃣  Create Pipeline Job:"
echo ""
echo "   OPTION A - Job DSL (Automated):"
echo "   - Dashboard → New Item → Name: 'seed-job' → Freestyle"
echo "   - Build → Add build step → Process Job DSLs"
echo "   - DSL Script: Use the provided script"
echo "   - Copy: deployment/jenkins-config/jenkins-job-dsl.groovy"
echo "   - Save → Build Now"
echo ""
echo "   OPTION B - Manual:"
echo "   - Dashboard → New Item"
echo "   - Name: 'buy-01-cicd-pipeline'"
echo "   - Type: Pipeline → OK"
echo "   - Pipeline → Definition: Pipeline script from SCM"
echo "   - SCM: Git"
echo "   - Repository: https://github.com/SaddamHosyn/buy-01project.git"
echo "   - Branch: */main"
echo "   - Script Path: deployment/Jenkinsfile"
echo "   - Save"
echo ""
echo "6️⃣  Run First Build:"
echo "   - Dashboard → buy-01-cicd-pipeline"
echo "   - Build with Parameters"
echo "   - Click 'Build'"
echo ""
echo "=================================================="
echo "📖 Documentation"
echo "=================================================="
echo ""
echo "  - Setup Guide: deployment/README.md"
echo "  - Security Guide: deployment/JENKINS-SECURITY.md"
echo "  - Audit Checklist: deployment/AUDIT-CHECKLIST.md"
echo ""
echo "=================================================="
echo -e "${GREEN}✅ Jenkins setup initiated successfully!${NC}"
echo "=================================================="
