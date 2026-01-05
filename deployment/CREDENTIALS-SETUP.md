# Jenkins Credentials Setup Guide

Quick reference for adding credentials to Jenkins.

---

## 🔑 Required Credentials

### 1. Docker Hub Username (Secret Text)

**Steps:**

1. Manage Jenkins → Credentials → System → Global credentials → Add Credentials
2. **Kind:** Secret text
3. **Scope:** Global
4. **Secret:** `your-docker-hub-username`
5. **ID:** `docker-hub-username`
6. **Description:** Docker Hub username for image tagging
7. Click **OK**

### 2. Docker Hub Credentials (Username/Password)

**Steps:**

1. Manage Jenkins → Credentials → System → Global credentials → Add Credentials
2. **Kind:** Username with password
3. **Scope:** Global
4. **Username:** `your-docker-hub-username`
5. **Password:** `your-docker-hub-password`
6. **ID:** `docker-hub-credentials`
7. **Description:** Docker Hub credentials for image push
8. Click **OK**

---

## 📧 Optional Credentials

### 3. Slack Webhook URL (Secret Text) - Optional

**Get Slack Webhook:**

1. Go to https://api.slack.com/apps
2. Create new app
3. Add "Incoming Webhooks" feature
4. Create webhook for your channel
5. Copy webhook URL

**Add to Jenkins:**

1. Manage Jenkins → Credentials → System → Global credentials → Add Credentials
2. **Kind:** Secret text
3. **Scope:** Global
4. **Secret:** `https://hooks.slack.com/services/YOUR/WEBHOOK/URL`
5. **ID:** `slack-webhook-url`
6. **Description:** Slack webhook for build notifications
7. Click **OK**

### 4. Email SMTP Credentials (Username/Password) - Optional

**For Gmail:**

1. Enable 2FA on your Google account
2. Go to https://myaccount.google.com/apppasswords
3. Generate app password for "Mail"
4. Use app password (not your regular password)

**Add to Jenkins:**

1. Manage Jenkins → Credentials → System → Global credentials → Add Credentials
2. **Kind:** Username with password
3. **Scope:** Global
4. **Username:** `your-email@gmail.com`
5. **Password:** `your-app-password`
6. **ID:** `email-credentials`
7. **Description:** SMTP credentials for email notifications
8. Click **OK**

**Configure Email Extension:**

1. Manage Jenkins → System → Extended E-mail Notification
2. **SMTP Server:** `smtp.gmail.com`
3. **SMTP Port:** `587`
4. **Use TLS:** ✅ Enabled
5. **Credentials:** Select `email-credentials`
6. **Default Recipients:** `team@example.com`
7. Click **Save**

---

## ✅ Verification

After adding credentials, verify they're all present:

**Navigate to:** Manage Jenkins → Credentials → System → Global credentials

**You should see:**

- ✅ `docker-hub-username` (Secret text)
- ✅ `docker-hub-credentials` (Username with password)
- ⭕ `slack-webhook-url` (Secret text) - Optional
- ⭕ `email-credentials` (Username with password) - Optional

---

## 🔧 Troubleshooting

**"Credentials not found" error in pipeline:**

- Check that credential ID matches exactly (case-sensitive)
- Verify credential scope is "Global"
- Ensure credential exists in "System" domain

**Email notifications not working:**

- Test SMTP connection: Manage Jenkins → System → Extended E-mail Notification → Test Configuration
- For Gmail, ensure you're using app password, not regular password
- Check firewall allows port 587 (SMTP)

**Slack notifications not working:**

- Test webhook in terminal:
  ```bash
  curl -X POST -H 'Content-type: application/json' \
    --data '{"text":"Test from Jenkins"}' \
    https://hooks.slack.com/services/YOUR/WEBHOOK/URL
  ```
- Verify webhook URL is correct and channel exists

---

## 📚 More Information

See [JENKINS-SECURITY.md](JENKINS-SECURITY.md) for comprehensive security documentation.
