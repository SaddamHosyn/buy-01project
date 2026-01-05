# Jenkins Security Configuration Guide

Comprehensive security setup for Jenkins CI/CD pipeline.

---

## 📑 Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Authorization](#authorization)
4. [Credentials Management](#credentials-management)
5. [Network Security](#network-security)
6. [Audit & Compliance](#audit--compliance)
7. [Best Practices](#best-practices)

---

## 🔒 Overview

### Security Layers

```
┌─────────────────────────────────────────┐
│  1. Network Security (HTTPS, Firewall) │
├─────────────────────────────────────────┤
│  2. Authentication (Who are you?)       │
├─────────────────────────────────────────┤
│  3. Authorization (What can you do?)    │
├─────────────────────────────────────────┤
│  4. Credentials (Secrets management)    │
├─────────────────────────────────────────┤
│  5. Audit (What did you do?)            │
└─────────────────────────────────────────┘
```

### Security Checklist

- ✅ Authentication enabled
- ✅ Role-based access control (RBAC)
- ✅ Credentials encrypted
- ✅ CSRF protection enabled
- ✅ Agent-to-controller security
- ✅ Audit trail maintained
- ✅ No anonymous access
- ✅ Pipeline scripts from SCM

---

## 🔐 Authentication

### Initial Setup

**1. Enable Security**

Navigate to: **Manage Jenkins → Security**

```
☑️ Enable security
```

**2. Configure Security Realm**

**Option A: Jenkins' own user database (Recommended for small teams)**

```
Security Realm: Jenkins' own user database
☑️ Allow users to sign up
```

**Option B: LDAP (Recommended for enterprise)**

```
Security Realm: LDAP
Server: ldap://your-ldap-server:389
Root DN: dc=example,dc=com
User search base: ou=users
User search filter: uid={0}
Manager DN: cn=admin,dc=example,dc=com
Manager Password: [password]
```

**Option C: Active Directory**

```
Security Realm: Active Directory
Domain: EXAMPLE.COM
Domain Controllers: dc1.example.com,dc2.example.com
```

### User Management

**Create Admin User**

```bash
# During initial setup wizard
Username: admin
Password: [strong-password]
Full Name: Jenkins Administrator
Email: admin@example.com
```

**Add Additional Users**

Navigate to: **Manage Jenkins → Users → Create User**

```
Username: developer1
Password: [strong-password]
Full Name: John Developer
Email: john@example.com
```

**Password Policy (Recommended)**

- Minimum 12 characters
- Mix of uppercase, lowercase, numbers, symbols
- Change every 90 days
- No password reuse (last 5)

### Two-Factor Authentication (2FA)

**1. Install Plugin**

```
Manage Jenkins → Plugins → Available
Search: "OTP" or "Two Factor Authentication"
Install and restart
```

**2. Enable for Users**

```
Each user: Profile → Configure
Enable Two Factor Authentication
Scan QR code with authenticator app
```

### API Tokens

**Generate User API Token**

```
User Menu → Configure → API Token
Add new Token
Name: "CLI Access"
Generate → Copy token (shown once!)
```

**Use API Token**

```bash
# CLI access
java -jar jenkins-cli.jar -s http://localhost:8086 -auth username:token-value list-jobs

# REST API
curl -u username:token-value http://localhost:8086/api/json
```

---

## 👮 Authorization

### Authorization Strategies

**1. Matrix-Based Security (Recommended)**

Navigate to: **Manage Jenkins → Security → Authorization**

```
Authorization: Matrix-based security
```

**Permission Matrix:**

| Permission      | Admin | Developer | QA  | Viewer |
| --------------- | ----- | --------- | --- | ------ |
| **Overall**     |
| Administer      | ✅    | ❌        | ❌  | ❌     |
| Read            | ✅    | ✅        | ✅  | ✅     |
| **Credentials** |
| Create          | ✅    | ❌        | ❌  | ❌     |
| Update          | ✅    | ❌        | ❌  | ❌     |
| View            | ✅    | ✅        | ❌  | ❌     |
| Delete          | ✅    | ❌        | ❌  | ❌     |
| **Job**         |
| Create          | ✅    | ✅        | ❌  | ❌     |
| Configure       | ✅    | ✅        | ❌  | ❌     |
| Build           | ✅    | ✅        | ✅  | ❌     |
| Read            | ✅    | ✅        | ✅  | ✅     |
| Delete          | ✅    | ❌        | ❌  | ❌     |
| Workspace       | ✅    | ✅        | ❌  | ❌     |
| **View**        |
| Create          | ✅    | ✅        | ❌  | ❌     |
| Configure       | ✅    | ✅        | ❌  | ❌     |
| Read            | ✅    | ✅        | ✅  | ✅     |

**Implementation:**

```
1. Manage Jenkins → Security → Authorization
2. Select "Matrix-based security"
3. Add user/group
4. Check appropriate permissions
5. Save
```

**2. Project-Based Matrix (For multi-team)**

```
Authorization: Project-based Matrix Authorization Strategy

Global permissions: (minimal)
Admin group: Overall/Administer

Per-project permissions:
Team A: Can configure/build jobs in folder "team-a"
Team B: Can configure/build jobs in folder "team-b"
```

**Setup:**

```
1. Install "Role-based Authorization Strategy" plugin
2. Manage Jenkins → Security → Authorization
3. Select "Project-based Matrix Authorization Strategy"
4. Set global permissions (minimal)
5. In each job: Enable project-based security
6. Set job-specific permissions
```

**3. Role-Based Strategy (Advanced)**

**Install Plugin:**

```
Manage Jenkins → Plugins → Available
Search: "Role-based Authorization Strategy"
Install and restart
```

**Configure Roles:**

```
Manage Jenkins → Security → Manage and Assign Roles

Manage Roles:
Global roles:
- admin: (all permissions)
- developer: Job.*, View.Read
- viewer: Overall.Read, Job.Read

Project roles:
- team-a-developer: Pattern: team-a-.*
- team-b-developer: Pattern: team-b-.*

Assign Roles:
admin-user → admin
dev-user-1 → developer, team-a-developer
dev-user-2 → developer, team-b-developer
qa-user → viewer
```

### Anonymous Access

**Disable Anonymous Access (Recommended)**

```
Manage Jenkins → Security → Authorization
Matrix-based security
Remove "Anonymous" user or uncheck all permissions
```

**Limited Anonymous Access (Public projects)**

```
Anonymous permissions:
☑️ Overall: Read
☑️ Job: Read
☐ All other permissions: Disabled
```

---

## 🔑 Credentials Management

### Credentials Types

**1. Username with Password**

```
Use for: Docker Hub, Git repositories, SMTP
Scope: Global or System
ID: docker-hub-credentials
Username: your-username
Password: your-password
```

**2. Secret Text**

```
Use for: API tokens, webhook URLs
Scope: Global
ID: slack-webhook-url
Secret: https://hooks.slack.com/services/XXX/YYY/ZZZ
```

**3. SSH Username with Private Key**

```
Use for: Git over SSH, remote servers
Scope: Global
ID: github-ssh-key
Username: git
Private Key: [paste private key or upload file]
Passphrase: [if key is encrypted]
```

**4. Secret File**

```
Use for: Configuration files, certificates
Scope: Global
ID: kubeconfig
File: [upload kubeconfig file]
```

**5. Certificate**

```
Use for: Client certificates, keystores
Scope: Global
ID: ssl-certificate
Certificate: [upload .p12 or .jks file]
Password: [keystore password]
```

### Adding Credentials

**Via UI (Recommended):**

```
1. Manage Jenkins → Credentials
2. Click domain (e.g., "Global")
3. Add Credentials
4. Select Kind
5. Fill in details
6. Set ID (important for Jenkinsfile reference)
7. Save
```

**Via CLI:**

```bash
# Docker Hub credentials
cat > docker-creds.xml <<EOF
<com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl>
  <scope>GLOBAL</scope>
  <id>docker-hub-credentials</id>
  <username>your-username</username>
  <password>your-password</password>
  <description>Docker Hub credentials</description>
</com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl>
EOF

cat docker-creds.xml | java -jar jenkins-cli.jar -s http://localhost:8086 \
  -auth admin:token create-credentials-by-xml system::system::jenkins

# Slack webhook
cat > slack-webhook.xml <<EOF
<org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl>
  <scope>GLOBAL</scope>
  <id>slack-webhook-url</id>
  <secret>https://hooks.slack.com/services/XXX/YYY/ZZZ</secret>
  <description>Slack webhook for notifications</description>
</org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl>
EOF

cat slack-webhook.xml | java -jar jenkins-cli.jar -s http://localhost:8086 \
  -auth admin:token create-credentials-by-xml system::system::jenkins
```

**Via Groovy Script:**

```groovy
// Run in Manage Jenkins → Script Console
import jenkins.model.Jenkins
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*

def domain = Domain.global()
def store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

// Docker Hub credentials
def dockerCreds = new UsernamePasswordCredentialsImpl(
  CredentialsScope.GLOBAL,
  "docker-hub-credentials",
  "Docker Hub credentials",
  "your-username",
  "your-password"
)
store.addCredentials(domain, dockerCreds)

// Slack webhook
def slackCreds = new StringCredentialsImpl(
  CredentialsScope.GLOBAL,
  "slack-webhook-url",
  "Slack webhook",
  hudson.util.Secret.fromString("https://hooks.slack.com/services/XXX/YYY/ZZZ")
)
store.addCredentials(domain, slackCreds)

println "Credentials added successfully!"
```

### Required Credentials for Buy-01 Project

**1. Docker Hub Credentials**

```
Kind: Username with password
ID: docker-hub-credentials
Scope: Global
Username: [your-docker-hub-username]
Password: [your-docker-hub-password]
Description: Docker Hub credentials for image push
```

**How to get:**

1. Sign up at https://hub.docker.com
2. Verify email
3. Use username and password

**2. Slack Webhook (Optional)**

```
Kind: Secret text
ID: slack-webhook-url
Scope: Global
Secret: https://hooks.slack.com/services/YOUR/WEBHOOK/URL
Description: Slack webhook for build notifications
```

**How to get:**

1. Go to https://api.slack.com/apps
2. Create new app
3. Select workspace
4. Add "Incoming Webhooks" feature
5. Activate incoming webhooks
6. Add new webhook to workspace
7. Select channel
8. Copy webhook URL

**3. Email Credentials (Optional)**

```
Kind: Username with password
ID: email-credentials
Scope: Global
Username: [smtp-username]
Password: [smtp-app-password]
Description: SMTP credentials for email notifications
```

**For Gmail:**

1. Enable 2FA on Google account
2. Go to https://myaccount.google.com/apppasswords
3. Generate app password for "Mail"
4. Use app password (not account password)

**4. Docker Hub Username (New - for Jenkinsfile)**

```
Kind: Secret text
ID: docker-hub-username
Scope: Global
Secret: your-docker-hub-username
Description: Docker Hub username for image tagging
```

### Using Credentials in Pipeline

**Username/Password:**

```groovy
withCredentials([usernamePassword(
    credentialsId: 'docker-hub-credentials',
    usernameVariable: 'DOCKER_USER',
    passwordVariable: 'DOCKER_PASS'
)]) {
    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
}
```

**Secret Text:**

```groovy
withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_URL')]) {
    sh 'curl -X POST -H "Content-type: application/json" --data \'{"text":"Message"}\' ${SLACK_URL}'
}
```

**Multiple Credentials:**

```groovy
withCredentials([
    usernamePassword(credentialsId: 'docker-hub', usernameVariable: 'USER', passwordVariable: 'PASS'),
    string(credentialsId: 'api-token', variable: 'TOKEN')
]) {
    sh 'docker login -u $USER -p $PASS'
    sh 'curl -H "Authorization: Bearer $TOKEN" https://api.example.com'
}
```

### Credentials Best Practices

- ✅ Never commit credentials to Git
- ✅ Use Jenkins credentials for all secrets
- ✅ Set appropriate scope (Global vs System)
- ✅ Use meaningful IDs (referenced in Jenkinsfile)
- ✅ Add descriptions for documentation
- ✅ Rotate credentials regularly (90 days)
- ✅ Audit credential usage
- ✅ Delete unused credentials
- ✅ Use `--password-stdin` for Docker login
- ✅ Clear environment variables after use

---

## 🌐 Network Security

### HTTPS Setup

**1. Generate Self-Signed Certificate (Development)**

```bash
# Generate keystore
keytool -genkey -keyalg RSA -alias jenkins -keystore jenkins.jks \
  -storepass changeit -keysize 2048 -dname "CN=localhost, OU=Jenkins, O=Company, L=City, S=State, C=US"

# Copy to Jenkins
docker cp jenkins.jks jenkins-master:/var/jenkins_home/
```

**2. Configure Jenkins for HTTPS**

Edit `docker-compose.yml`:

```yaml
services:
  jenkins-master:
    environment:
      - JENKINS_OPTS=--httpPort=-1 --httpsPort=8443 --httpsKeyStore=/var/jenkins_home/jenkins.jks --httpsKeyStorePassword=changeit
    ports:
      - "8443:8443"
```

**3. Use Production Certificate (Recommended)**

```bash
# Get Let's Encrypt certificate
certbot certonly --standalone -d jenkins.example.com

# Convert to JKS format
openssl pkcs12 -export -in fullchain.pem -inkey privkey.pem -out jenkins.p12 -name jenkins
keytool -importkeystore -srckeystore jenkins.p12 -srcstoretype PKCS12 -destkeystore jenkins.jks
```

### Firewall Rules

**Allow only necessary ports:**

```bash
# UFW (Ubuntu)
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 8086/tcp  # Jenkins HTTP (behind reverse proxy)
sudo ufw deny 8761/tcp   # Block Eureka from internet
sudo ufw deny 8080/tcp   # Block API Gateway from internet
sudo ufw enable

# iptables
iptables -A INPUT -p tcp --dport 22 -j ACCEPT
iptables -A INPUT -p tcp --dport 8086 -j ACCEPT
iptables -A INPUT -p tcp --dport 8761 -j DROP
iptables -A INPUT -p tcp --dport 8080 -j DROP
```

### Reverse Proxy (Production)

**Nginx Configuration:**

```nginx
server {
    listen 80;
    server_name jenkins.example.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name jenkins.example.com;

    ssl_certificate /etc/ssl/certs/jenkins.crt;
    ssl_certificate_key /etc/ssl/private/jenkins.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;

    location / {
        proxy_pass http://localhost:8086;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # WebSocket support
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

---

## 📊 Audit & Compliance

### Audit Trail

**1. Enable Audit Plugin**

```
Manage Jenkins → Plugins → Available
Search: "Audit Trail"
Install and restart
```

**2. Configure Audit Trail**

```
Manage Jenkins → System → Audit Trail

Loggers:
☑️ Log file
  Log Location: /var/jenkins_home/logs/audit.log
  Log File Size: 10 MB
  Log File Count: 5

☑️ Syslog
  Syslog Server: syslog.example.com:514

☑️ Database (optional)
```

**3. What Gets Logged**

- User login/logout
- Job configuration changes
- Job builds (start/stop/result)
- Credential access
- System configuration changes
- Plugin installations

**4. Review Audit Logs**

```bash
# View audit log
docker exec jenkins-master tail -f /var/jenkins_home/logs/audit.log

# Search for specific user
docker exec jenkins-master grep "user=john" /var/jenkins_home/logs/audit.log

# Search for credential access
docker exec jenkins-master grep "credentials" /var/jenkins_home/logs/audit.log
```

### Compliance Reports

**Generate Security Report:**

```groovy
// Script Console
import jenkins.model.Jenkins

def jenkins = Jenkins.instance

println "=== Jenkins Security Report ==="
println "Users: ${jenkins.securityRealm.allUsers.size()}"
println "Jobs: ${jenkins.getAllItems(Job.class).size()}"
println "Credentials: ${com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(com.cloudbees.plugins.credentials.common.StandardCredentials.class).size()}"
println "Plugins: ${jenkins.pluginManager.plugins.size()}"

// Check security settings
println "\n=== Security Settings ==="
println "CSRF Protection: ${jenkins.crumbIssuer != null}"
println "Agent Security: ${jenkins.injector.getInstance(jenkins.security.s2m.AdminWhitelistRule).masterKillSwitch}"
println "Anonymous Access: ${jenkins.authorizationStrategy.toString()}"
```

### Backup & Recovery

**Automated Backup:**

```bash
# Backup Jenkins home
docker exec jenkins-master tar czf /tmp/jenkins-backup.tar.gz /var/jenkins_home
docker cp jenkins-master:/tmp/jenkins-backup.tar.gz ./jenkins-backup-$(date +%Y%m%d).tar.gz

# Backup important files only
docker exec jenkins-master tar czf /tmp/jenkins-config-backup.tar.gz \
  /var/jenkins_home/*.xml \
  /var/jenkins_home/jobs \
  /var/jenkins_home/users \
  /var/jenkins_home/secrets

# Restore
docker cp jenkins-backup-20260105.tar.gz jenkins-master:/tmp/
docker exec jenkins-master tar xzf /tmp/jenkins-backup-20260105.tar.gz -C /
docker restart jenkins-master
```

**Backup Schedule (Cron):**

```bash
# Add to crontab
0 2 * * * /opt/jenkins/backup.sh

# backup.sh
#!/bin/bash
BACKUP_DIR="/backups/jenkins"
DATE=$(date +%Y%m%d-%H%M%S)
docker exec jenkins-master tar czf /tmp/jenkins-backup.tar.gz /var/jenkins_home
docker cp jenkins-master:/tmp/jenkins-backup.tar.gz $BACKUP_DIR/jenkins-$DATE.tar.gz
find $BACKUP_DIR -name "jenkins-*.tar.gz" -mtime +30 -delete  # Keep 30 days
```

---

## ✅ Best Practices

### Security Hardening Checklist

**Authentication & Authorization:**

- ✅ Enable security
- ✅ Disable anonymous access (or minimal read-only)
- ✅ Use matrix-based or role-based security
- ✅ Enforce strong passwords
- ✅ Enable 2FA for admins
- ✅ Regular user access reviews

**Credentials:**

- ✅ Store all secrets in Jenkins credentials
- ✅ Use appropriate credential scopes
- ✅ Never log passwords to console
- ✅ Rotate credentials every 90 days
- ✅ Delete unused credentials
- ✅ Audit credential access

**Network:**

- ✅ Use HTTPS in production
- ✅ Configure firewall rules
- ✅ Use reverse proxy (nginx/apache)
- ✅ Enable CSRF protection
- ✅ Configure agent-to-controller security

**Pipeline Security:**

- ✅ Pipeline scripts from SCM (not inline)
- ✅ Use `withCredentials` blocks
- ✅ Validate user inputs
- ✅ Avoid `sh "command $USER_INPUT"`
- ✅ Use parameterized builds safely
- ✅ Scan for secrets in code

**System:**

- ✅ Keep Jenkins updated
- ✅ Keep plugins updated
- ✅ Regular security audits
- ✅ Enable audit logging
- ✅ Automated backups
- ✅ Disaster recovery plan

**Monitoring:**

- ✅ Monitor failed login attempts
- ✅ Alert on configuration changes
- ✅ Track credential usage
- ✅ Monitor system resources
- ✅ Review audit logs weekly

### Security Maintenance Schedule

**Daily:**

- Monitor failed builds
- Check system health

**Weekly:**

- Review audit logs
- Check for failed logins
- Monitor disk space

**Monthly:**

- Update Jenkins
- Update plugins
- Review user permissions
- Test backups
- Security scan

**Quarterly:**

- Rotate credentials
- User access review
- Disaster recovery test
- Security audit
- Compliance review

---

## 📞 Support

For security concerns:

- **Documentation:** [README.md](README.md), [AUDIT-CHECKLIST.md](AUDIT-CHECKLIST.md)
- **Jenkins Security:** https://www.jenkins.io/doc/book/security/
- **Report Vulnerabilities:** security@jenkins.io

---

**Stay Secure! 🔒**

_Last updated: January 2026_
