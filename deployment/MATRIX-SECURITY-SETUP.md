# 🔐 Matrix-Based Security Setup Guide

Complete guide to configure granular permissions in Jenkins using Matrix Authorization Strategy.

---

## 🎯 What You'll Achieve

**Before:** All logged-in users have full access 😱  
**After:** Granular role-based permissions (Admin, Developer, Viewer) 🔒

---

## 📋 Why Matrix-Based Security?

### Current Setup (Simple)

```
✅ Authenticated users: Full access
❌ Anonymous users: No access
```

**Problem:** Developers can delete jobs, change system settings, access credentials!

### With Matrix Security

```
✅ Admins: Full access to everything
✅ Developers: Build jobs, view results (no deletion)
✅ Viewers: Read-only access
❌ No one has more access than they need (principle of least privilege)
```

---

## 🚀 Step-by-Step Setup (10 minutes)

### Step 1: Access Jenkins Security Settings

1. **Log in to Jenkins:**

   ```
   http://localhost:8086
   ```

2. **Navigate to Security:**

   ```
   Dashboard → Manage Jenkins → Security
   ```

3. **Scroll to "Authorization" section**

---

### Step 2: Enable Matrix-Based Security

1. **Select Authorization Strategy:**

   ```
   ○ Anyone can do anything (❌ Never use)
   ○ Legacy mode (❌ Deprecated)
   ○ Logged-in users can do anything (← Current, not ideal)
   ● Matrix-based security (✅ Select this)
   ○ Project-based Matrix Authorization Strategy
   ```

   **Choose "Matrix-based security"** for now (simpler).  
   Use "Project-based" later if you want per-job permissions.

2. **You'll see a permission matrix table appear**

---

### Step 3: Configure Admin User (CRITICAL!)

**⚠️ IMPORTANT: Configure admin first or you'll lock yourself out!**

1. **Add your admin user:**

   - In the "User/group to add:" field
   - Enter: `admin` (or your username)
   - Click **Add user**

2. **Grant all permissions to admin:**
   - **Check the "Administer" checkbox in the "Overall" column**
   - This automatically grants all permissions
   - **Verify admin has ALL checkboxes selected**

**Your matrix should look like this:**

```
User/Group   | Overall | Credentials | Agent | Job | Run | View | SCM | ...
-------------|---------|-------------|-------|-----|-----|------|-----|----
admin        |    ✓    |      ✓      |   ✓   |  ✓  |  ✓  |  ✓   |  ✓  | ...
             | (Administer checkbox grants all)
```

3. **Click "Save" at the bottom** (don't add more users yet!)

4. **Test you can still access Jenkins!**
   - If locked out, restart Jenkins and try again

---

### Step 4: Add Developer Role

Now add developer users with limited permissions:

1. **Go back to Security settings:**

   ```
   Manage Jenkins → Security → Authorization
   ```

2. **Add developer user:**

   - Enter username: `developer1` (or actual username)
   - Click **Add user**

3. **Grant developer permissions:**

   Check these boxes for `developer1`:

   **Overall:**

   - ☑️ Read

   **Job:**

   - ☑️ Build
   - ☑️ Cancel
   - ☑️ Read
   - ☑️ Workspace (view workspace files)
   - ☐ Configure (❌ cannot modify jobs)
   - ☐ Create (❌ cannot create jobs)
   - ☐ Delete (❌ cannot delete jobs)

   **Run:**

   - ☑️ Replay (re-run pipeline)
   - ☑️ Update (for build parameters)

   **View:**

   - ☑️ Configure
   - ☑️ Create
   - ☑️ Delete
   - ☑️ Read

   **SCM:**

   - ☑️ Tag (Git tagging)

   **Credentials:**

   - ☐ Create (❌ cannot create)
   - ☐ Delete (❌ cannot delete)
   - ☐ Update (❌ cannot modify)
   - ☑️ View (read-only)

4. **Matrix should now look like:**

```
User/Group   | Overall | Credentials | Job         | Run    | View   | ...
-------------|---------|-------------|-------------|--------|--------|----
admin        | Admin✓  | All ✓       | All ✓       | All ✓  | All ✓  | ...
developer1   | Read ✓  | View ✓      | Build,Read✓ | All ✓  | All ✓  | ...
```

5. **Click "Save"**

---

### Step 5: Add Viewer Role (Read-Only)

For team members who only need to view builds:

1. **Add viewer user:**

   - Enter: `viewer1`
   - Click **Add user**

2. **Grant minimal permissions:**

   **Overall:**

   - ☑️ Read

   **Job:**

   - ☑️ Read (can view job configuration)
   - ☐ Build (❌ cannot trigger builds)
   - ☐ Cancel (❌ cannot cancel builds)

   **Run:**

   - ☑️ Artifacts (can download build artifacts)

   **View:**

   - ☑️ Read

   **All other categories:**

   - ☐ Unchecked (read-only access only)

3. **Matrix now looks like:**

```
User/Group   | Overall | Job    | Run       | View   | Everything Else
-------------|---------|--------|-----------|--------|----------------
admin        | Admin✓  | All ✓  | All ✓     | All ✓  | All ✓
developer1   | Read ✓  | Build✓ | All ✓     | All ✓  | Limited
viewer1      | Read ✓  | Read ✓ | Artifacts | Read ✓ | None ❌
```

4. **Click "Save"**

---

### Step 6: Configure Anonymous Access (Optional)

**For public dashboards (e.g., build status display):**

1. **Add anonymous user:**

   - Enter: `anonymous`
   - Click **Add user**

2. **Grant minimal read permissions:**
   - **Overall:** Read ✓
   - **Job:** Read ✓
   - **View:** Read ✓
   - **Everything else:** Unchecked

**⚠️ Only do this if you want public access to your Jenkins!**

---

### Step 7: Test Permissions

**A. Test as Admin:**

```bash
# Log in as admin
# Try: Create job, delete job, configure system
# Expected: ✅ All operations succeed
```

**B. Test as Developer:**

```bash
# Log in as developer1
# Try: Build job → ✅ Works
# Try: Delete job → ❌ Access Denied
# Try: Manage Jenkins → ❌ Access Denied
# Try: View credentials → ✅ Can see list (but not secrets)
```

**C. Test as Viewer:**

```bash
# Log in as viewer1
# Try: View job → ✅ Works
# Try: Build job → ❌ Access Denied
# Try: View console output → ✅ Works
# Try: Download artifacts → ✅ Works
```

---

## 📊 Recommended Permission Matrix

### Quick Reference

| Permission           | Admin | Developer | Viewer         | Anonymous    |
| -------------------- | ----- | --------- | -------------- | ------------ |
| **Overall**          |
| Administer           | ✓     | ❌        | ❌             | ❌           |
| Read                 | ✓     | ✓         | ✓              | ✓ (optional) |
| **Credentials**      |
| View                 | ✓     | ✓         | ❌             | ❌           |
| Create/Update/Delete | ✓     | ❌        | ❌             | ❌           |
| **Job**              |
| Build                | ✓     | ✓         | ❌             | ❌           |
| Cancel               | ✓     | ✓         | ❌             | ❌           |
| Configure            | ✓     | ❌        | ❌             | ❌           |
| Create               | ✓     | ❌        | ❌             | ❌           |
| Delete               | ✓     | ❌        | ❌             | ❌           |
| Read                 | ✓     | ✓         | ✓              | ✓ (optional) |
| Workspace            | ✓     | ✓         | ❌             | ❌           |
| **Run**              |
| All                  | ✓     | ✓         | Artifacts only | ❌           |
| **View**             |
| All                  | ✓     | ✓         | Read only      | ✓ (optional) |

---

## 🔐 Advanced: Group-Based Permissions

Instead of adding users individually, create groups:

### Step 1: Enable LDAP/Active Directory (Optional)

If your organization uses LDAP/AD:

1. **Manage Jenkins → Security → Security Realm**
2. **Select:** LDAP or Active Directory
3. **Configure:** Your directory server details
4. **Groups are automatically imported**

### Step 2: Add Group Permissions

Instead of individual users, add groups:

```
User/group to add: admins       (AD group)
User/group to add: developers   (AD group)
User/group to add: viewers      (AD group)
```

Apply same permission matrix as before.

---

## 🎯 Project-Based Matrix (Advanced)

For per-job permissions:

### When to Use:

- Different teams work on different jobs
- Some jobs are sensitive (production deployments)
- You want job owners to manage their own jobs

### Setup:

1. **Manage Jenkins → Security → Authorization**
2. **Select:** Project-based Matrix Authorization Strategy
3. **Set global defaults** (like before)
4. **In each job:** Configure → Enable project-based security
5. **Override global permissions** for that job

**Example:**

```
Job: production-deploy
- Admins: All permissions
- Prod team: Build, read
- Dev team: Read only
```

---

## 🔍 Troubleshooting

### Locked Out of Jenkins!

**Solution:**

1. **Stop Jenkins:**

   ```bash
   cd /Users/saddam.hussain/Desktop/buy-01project/deployment
   docker-compose stop jenkins-master
   ```

2. **Disable security temporarily:**

   ```bash
   docker exec -it jenkins-master bash
   # Inside container:
   sed -i 's/<useSecurity>true<\/useSecurity>/<useSecurity>false<\/useSecurity>/' /var/jenkins_home/config.xml
   exit
   ```

3. **Restart Jenkins:**

   ```bash
   docker-compose up -d jenkins-master
   ```

4. **Access Jenkins without login**

5. **Reconfigure security properly**

---

### Permission Denied for Developer

**Check:**

1. User added to matrix?
2. Correct permissions checked?
3. User logged in with correct username?
4. Try logout/login

---

### Users Can't See Any Jobs

**Fix:**

Ensure users have:

- **Overall: Read** ✓
- **Job: Read** ✓
- **View: Read** ✓

---

## ✅ Security Best Practices

### 1. Principle of Least Privilege

- ✅ Give minimum permissions needed
- ✅ Review permissions quarterly
- ❌ Don't make everyone admin

### 2. Separate Accounts

- ✅ Different accounts for people and automation
- ✅ Use API tokens for scripts
- ❌ Don't share admin passwords

### 3. Audit Regularly

**Enable audit logging:**

1. **Install plugin:** Audit Trail
2. **Configure:** Manage Jenkins → Audit Trail
3. **Log all actions** by all users

**Review logs monthly:**

```bash
docker exec jenkins-master cat /var/jenkins_home/audit-trail.log
```

### 4. Rotate Credentials

- Admin password: Every 90 days
- API tokens: Every 180 days
- Service accounts: Every 90 days

---

## 📋 Verification Checklist

After setup:

- [ ] Admin can do everything
- [ ] Developer can build but not delete jobs
- [ ] Viewer can only read (no builds)
- [ ] Anonymous has no access (or minimal)
- [ ] Tested login for each role
- [ ] Audit trail plugin installed
- [ ] Password policy documented

---

## 🎉 You're Done!

**Your Jenkins now has enterprise-grade security! 🔒**

**Permission summary:**

- 👑 **Admins:** Full control
- 👨‍💻 **Developers:** Build and view
- 👀 **Viewers:** Read-only access
- 🚫 **Anonymous:** No access (secure!)

---

## 📞 Next Steps

1. **Test all roles:** Log in as each user type
2. **Document your matrix:** Save screenshot
3. **Train your team:** Share this guide
4. **Review quarterly:** Audit permissions every 3 months

**Related Guides:**

- GitHub Webhook Setup: `GITHUB-WEBHOOK-SETUP.md`
- Security Overview: `JENKINS-SECURITY.md`
- Audit Report: `JENKINS_AUDIT_REPORT.md`

---

**Last Updated:** January 15, 2026  
**Security Level:** ⭐⭐⭐⭐⭐ Enterprise-Grade
