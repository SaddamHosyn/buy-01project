# 🔗 GitHub Webhook Setup Guide

Complete guide to configure GitHub webhook for instant Jenkins builds (no more polling delays!).

---

## 🎯 What You'll Achieve

**Before:** Jenkins polls GitHub every 2 minutes ⏰  
**After:** Jenkins builds instantly on push ⚡ (typically < 5 seconds)

---

## 📋 Prerequisites

- ✅ Jenkins running and accessible
- ✅ GitHub repository with admin access
- ✅ GitHub Webhook Plugin installed in Jenkins (usually pre-installed)

---

## 🚀 Step-by-Step Setup

### Step 1: Make Jenkins Accessible to GitHub

**Option A: Public Jenkins (Recommended for production)**

If Jenkins is publicly accessible (e.g., `https://jenkins.yourcompany.com`):

- ✅ Already ready for webhooks
- Use your public URL in webhook configuration

**Option B: Local Jenkins (For development/testing)**

If Jenkins is on localhost, you need to expose it temporarily:

**Using ngrok (Free, Easy):**

```bash
# Install ngrok
brew install ngrok  # macOS
# or download from https://ngrok.com/download

# Start ngrok tunnel
ngrok http 8086

# You'll see output like:
# Forwarding: https://abc123.ngrok.io -> http://localhost:8086
# Copy this URL (you'll need it in Step 2)
```

**Keep ngrok running in a separate terminal!**

---

### Step 2: Configure Jenkins (1 minute)

**A. Install GitHub Plugin (if not already installed)**

1. Go to: **Manage Jenkins** → **Plugins** → **Available**
2. Search: `GitHub Plugin`
3. Install and restart Jenkins if needed

**B. No additional Jenkins configuration needed!**

The GitHub plugin automatically exposes the webhook endpoint:

```
YOUR_JENKINS_URL/github-webhook/
```

---

### Step 3: Configure GitHub Webhook (2 minutes)

1. **Go to Your GitHub Repository:**

   ```
   https://github.com/SaddamHosyn/buy-01project
   ```

2. **Open Settings:**

   - Click **Settings** tab (top right)
   - Click **Webhooks** (left sidebar)
   - Click **Add webhook** button

3. **Configure Webhook:**

   **Payload URL:**

   ```
   For public Jenkins:
   https://your-jenkins-domain.com:8086/github-webhook/

   For local Jenkins with ngrok:
   https://abc123.ngrok.io/github-webhook/

   ⚠️ Don't forget the trailing slash!
   ```

   **Content type:**

   ```
   application/json
   ```

   **Secret:** (Optional but recommended)

   ```
   Leave blank for now (or set a strong secret)
   ```

   **Which events would you like to trigger this webhook?**

   ```
   ○ Just the push event (Select this)
   ```

   **Active:**

   ```
   ☑️ Active (checked)
   ```

4. **Click "Add webhook"**

5. **Verify:**
   - GitHub will immediately test the webhook
   - Look for a green checkmark ✅ next to your webhook
   - If you see a red X, click the webhook to see error details

---

### Step 4: Update Jenkinsfile (Already Done! ✅)

The Jenkinsfile has already been updated with webhook support.

**Current configuration:**

```groovy
// ⚡ WEBHOOK TRIGGER (Instant builds instead of polling)
// Configure GitHub webhook first (see deployment/GITHUB-WEBHOOK-SETUP.md)
// Then uncomment the line below:
// triggers {
//     githubPush()
// }

// 🔄 Temporary: Using SCM polling (remove after webhook is configured)
triggers {
    pollSCM('H/2 * * * *')
}
```

**After webhook is working, you'll update this to:**

```groovy
// ⚡ WEBHOOK TRIGGER (Instant builds)
triggers {
    githubPush()
}
```

---

### Step 5: Test the Webhook (1 minute)

**A. Test from GitHub:**

1. Go to: **Settings** → **Webhooks** → Your webhook
2. Scroll down to **Recent Deliveries**
3. Click **Redeliver** on the test payload
4. Check response (should be 200 OK)

**B. Test with a real commit:**

```bash
cd /Users/saddam.hussain/Desktop/buy-01project

# Make a small change
echo "# Testing webhook" >> README.md

# Commit and push
git add README.md
git commit -m "test: webhook trigger"
git push origin main

# Check Jenkins - build should start within 5 seconds!
```

**Expected result:**

- 🚀 Jenkins build starts almost immediately
- Build log shows: "Started by GitHub push by SaddamHosyn"

---

### Step 6: Switch from Polling to Webhook (Final step!)

Once webhook is confirmed working:

1. **Open Jenkinsfile:**

   ```bash
   vim /Users/saddam.hussain/Desktop/buy-01project/deployment/Jenkinsfile
   ```

2. **Update the triggers section:**

   ```groovy
   // ⚡ WEBHOOK TRIGGER (Instant builds)
   triggers {
       githubPush()
   }

   // Polling no longer needed - webhook is instant! ⚡
   ```

3. **Commit and push:**

   ```bash
   git add deployment/Jenkinsfile
   git commit -m "chore: switch from polling to webhook triggers"
   git push origin main
   ```

4. **Done! 🎉**

---

## 🔍 Troubleshooting

### Webhook shows Red X in GitHub

**Problem:** "We couldn't deliver this payload"

**Solutions:**

1. **Check Jenkins URL is accessible:**

   ```bash
   # From your machine:
   curl http://localhost:8086/github-webhook/

   # Should return: {"status":"ok"} or similar
   ```

2. **Check ngrok is running** (if using local Jenkins)

3. **Check firewall rules** (ensure port 8086 is open)

4. **Verify URL ends with `/github-webhook/`** (trailing slash required!)

---

### Build not triggering on push

**Check Jenkins job configuration:**

1. Go to: Job → **Configure**
2. Under **Build Triggers**, ensure checked:
   - ☑️ GitHub hook trigger for GITScm polling
3. Under **Source Code Management**, verify:
   - Repository URL matches GitHub repo
   - Branch specifier matches (usually `*/main` or `*/master`)

---

### ngrok URL changes every restart

**Problem:** Free ngrok URLs change on restart

**Solutions:**

1. **Keep ngrok running** during development

2. **Update GitHub webhook URL** if ngrok restarts

3. **Upgrade to ngrok Pro** for persistent URLs ($8/month)

4. **Use public Jenkins** for production (recommended)

---

## 📊 Performance Comparison

| Method      | Trigger Delay | Pros               | Cons                   |
| ----------- | ------------- | ------------------ | ---------------------- |
| **Polling** | 2-5 minutes   | Simple, no setup   | Slow, wastes resources |
| **Webhook** | < 5 seconds   | Instant, efficient | Requires public URL    |

**Recommendation:** Use webhooks for all production environments!

---

## 🔒 Security Best Practices

### 1. Use Webhook Secret (Recommended)

**In GitHub webhook configuration:**

```
Secret: [generate strong random string]
```

**In Jenkins:**

1. Go to: **Manage Jenkins** → **System**
2. Find: **GitHub** section
3. Add: Webhook secret in "Shared secret"

### 2. Use HTTPS (Production)

For production Jenkins:

- ✅ Always use HTTPS
- ✅ Valid SSL certificate
- ❌ Never expose Jenkins on HTTP in production

### 3. Restrict IP Access (Optional)

Configure firewall to only accept webhooks from GitHub IPs:

- https://api.github.com/meta (GitHub's IP ranges)

---

## ✅ Verification Checklist

After setup is complete:

- [ ] Webhook shows green checkmark in GitHub
- [ ] Test push triggers build within 5 seconds
- [ ] Build log shows "Started by GitHub push"
- [ ] Jenkinsfile updated to use `githubPush()` trigger
- [ ] ngrok removed (if using public Jenkins)
- [ ] Webhook secret configured (optional but recommended)

---

## 🎉 You're Done!

**Your Jenkins pipeline now has instant triggers! ⚡**

**Next steps:**

- Configure matrix-based security: See `MATRIX-SECURITY-SETUP.md`
- Review updated audit report: `JENKINS_AUDIT_REPORT.md`

---

## 📞 Need Help?

**Common issues:**

1. Red X in GitHub → Check Jenkins accessibility
2. No build trigger → Verify job configuration
3. ngrok issues → Use public Jenkins or restart ngrok

**Still stuck?** Check Jenkins logs:

```bash
docker logs -f jenkins-master | grep webhook
```

---

**Last Updated:** January 15, 2026  
**Related:** Jenkinsfile, MATRIX-SECURITY-SETUP.md
