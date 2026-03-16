# ☕ Step 2 — Backend Setup (Java 21 + Spring Boot)

> This guide walks you through installing **Java 21**, setting up the Spring Boot backend, and configuring your secrets.  
> **Estimated Time:** 25–35 minutes

---

## Table of Contents

1. [Install Java 21 (JDK)](#1-install-java-21-jdk)
2. [Verify Java Installation](#2-verify-java-installation)
3. [Configure Backend Secrets](#3-configure-backend-secrets)
4. [Build & Run the Backend](#4-build--run-the-backend)
5. [Verify the Backend Is Running](#5-verify-the-backend-is-running)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. Install Java 21 (JDK)

> [!IMPORTANT]
> You need **Java 21 (LTS)** specifically. Do NOT install Java 17 or Java 22 — this project requires Java 21.

**Download Link:**  
👉 https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz

### Download Steps

1. Open the link above.
2. Look for the latest **JDK 21** version (e.g., `jdk-21.0.6+7`).
3. Click the **.msi** installer link (under the **Windows x64** row).
4. Save the file to your Downloads folder.

### Install Steps

1. **Double-click** the downloaded `.msi` file.
2. Click **Next** on the welcome screen.
3. On the **Custom Setup** screen:
   - ✅ Make sure **"Set JAVA_HOME variable"** is enabled (click the icon next to it → select **"Will be installed on local hard drive"**)
   - ✅ Make sure **"Add to PATH"** is also enabled
4. Click **Next** → **Install** → Wait → **Finish**.

> [!NOTE]
> The Temurin installer automatically sets `JAVA_HOME` and adds Java to your system PATH. No manual configuration needed!

---

## 2. Verify Java Installation

1. **Close any open Command Prompt windows** (environment variables need a fresh terminal).
2. Open a **new Command Prompt** (`Win + R` → type `cmd` → press Enter).
3. Run:

```
java -version
```

You should see output like:
```
openjdk version "21.0.6" 2025-01-21 LTS
OpenJDK Runtime Environment Temurin-21.0.6+7 (build 21.0.6+7-LTS)
OpenJDK 64-Bit Server VM Temurin-21.0.6+7 (build 21.0.6+7-LTS, mixed mode, sharing)
```

4. Also verify `JAVA_HOME`:

```
echo %JAVA_HOME%
```

This should print something like: `C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot`

> [!WARNING]
> If `java -version` shows a different version or says "not recognized", close Command Prompt completely and reopen it. If it still doesn't work, see the Troubleshooting section.

---

## 3. Configure Backend Secrets

> [!CAUTION]
> The backend configuration file contains **placeholder values** that you MUST replace with your own credentials before running the server.

### 📂 File to Edit

| Property | Value |
|---|---|
| **File Name** | `application-dev.properties` |
| **Full Path** | `TaskHive\taskhive-backend\src\main\resources\application-dev.properties` |
| **What it does** | Contains ALL backend configuration: database connection, email settings, JWT secret, admin credentials, server port, ML service URL |
| **Format** | Each line is a `key=value` pair. Lines starting with `#` are comments (ignored) |

### How to Open

1. Open **File Explorer**.
2. Navigate to your project folder → `taskhive-backend` → `src` → `main` → `resources`.
3. Find the file `application-dev.properties`.
4. Right-click it → **Open with** → **Notepad** (or any text editor).

> [!TIP]
> You can also open it by pressing `Win + R`, pasting the full path, and pressing Enter.

### Update These Values

Find and replace each of the following:

#### 🔒 Database Password (Line ~6)

```properties
spring.datasource.password=YOUR_POSTGRES_PASSWORD_HERE
```

Replace `YOUR_POSTGRES_PASSWORD_HERE` with the password you set in Step 1 when you installed PostgreSQL.

#### 🔒 JWT Secret Key (Line ~23)

```properties
jwt.secret=YOUR_SECURE_SECRET_KEY_HERE_MUST_BE_AT_LEAST_64_CHARACTERS_LONG_MAKE_IT_RANDOM
```

Replace with any long random string (at least 64 characters). You can generate one at: [https://www.uuidgenerator.net/](https://www.uuidgenerator.net/) — just paste 2-3 UUIDs together.

#### 🔒 SMTP Email Configuration (Lines ~57-60)

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_EMAIL@gmail.com
spring.mail.password=YOUR_APP_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

> [!TIP]
> **How to get a Gmail App Password:**
> 1. Go to [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
> 2. Sign in to your Google account
> 3. You may need to enable 2-Step Verification first at [https://myaccount.google.com/signinoptions/two-step-verification](https://myaccount.google.com/signinoptions/two-step-verification)
> 4. Go back to App Passwords → select **Mail** → select **Windows Computer** → click **Generate**
> 5. Copy the 16-character password
> 6. Paste it as `spring.mail.password` value

#### 🔒 Default Admin Password (Line ~38)

```properties
app.admin.password=YourSecureAdminPassword@123
```

This is the password for the first admin account. Change it to something you'll remember.

### Save the File

Press `Ctrl + S` to save after making all changes.

---

## 4. Build & Run the Backend

> [!NOTE]
> The project includes a **Maven Wrapper** (`mvnw.cmd`) so you do NOT need to install Maven separately. The wrapper downloads the correct Maven version automatically.

### Steps

1. Open **Command Prompt** (`Win + R` → type `cmd` → Enter).
2. Navigate to the backend folder by running:

```
cd /d "D:\Internship\TaskHive\taskhive-backend"
```

> ⚠️ Replace `D:\Internship\TaskHive` with **your actual project path** if different.

3. **First time only** — Build the project (this downloads all dependencies; may take 5-10 minutes on first run):

```
mvnw.cmd clean install -DskipTests
```

> Wait until you see `BUILD SUCCESS` in the output.

4. **Run the backend server:**

```
mvnw.cmd spring-boot:run
```

Wait for the startup to complete. You'll see in the output:
```
Started TaskhiveApplication in X.XXX seconds
```

> [!NOTE]
> **Flyway Migrations:** The first time the backend starts, it will automatically create all 22 database tables in your `taskhive` database. You don't need to run any SQL scripts manually!

---

## 5. Verify the Backend Is Running

### Check in Browser

1. Open your web browser.
2. Go to: **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**
3. You should see the **Swagger UI** — an interactive API documentation page.

### Check Health Endpoint

Go to: **[http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)**

You should see:
```json
{"status":"UP"}
```

If you see both of these, the backend is running correctly! ✅

---

## 6. Troubleshooting

### ❌ "'java' is not recognized as an internal or external command"

Java was not added to PATH correctly.

**Manual Fix:**
1. Press `Win + R` → type `sysdm.cpl` → press Enter.
2. Go to **Advanced** tab → **Environment Variables**.
3. Under **System variables**, click **New**:
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot` (find the actual path in `C:\Program Files\Eclipse Adoptium\`)
4. Find **Path** in System variables → click **Edit** → click **New** → add: `%JAVA_HOME%\bin`
5. Click **OK** on all windows → **close and reopen** Command Prompt.

### ❌ "FAILURE: Build failed" or "Connection refused" during build

- Make sure **PostgreSQL is running** (check in `services.msc` — look for `postgresql-x64-16`).
- Make sure the `taskhive` database exists (see Step 1).
- Check that your password in `application-dev.properties` (📂 `TaskHive\taskhive-backend\src\main\resources\application-dev.properties`, Line 6) matches your PostgreSQL password.

### ❌ "Address already in use: bind" on port 8080

Another application is using port 8080. Find and stop it:
```
netstat -ano | findstr :8080
taskkill /PID <PID_NUMBER> /F
```

Or change the port in `application-dev.properties` (📂 `TaskHive\taskhive-backend\src\main\resources\application-dev.properties`, Line 72):
```properties
server.port=8081
```

> [!WARNING]
> If you change the backend port, you must also update the port in:
> - 📂 `TaskHive\taskhive-frontend\.env.local` → `NEXT_PUBLIC_API_URL` and `NEXT_PUBLIC_WS_URL`
> - 📂 `TaskHive\taskhive_flutter\assets\env\.env.dev` → `API_BASE_URL`, `WS_BASE_URL`, `BASE_URL`

### ❌ "Flyway migration error: Found non-empty schema"

This happens if the database already has tables from a previous installation:
1. Drop the database and recreate it:
```
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres
DROP DATABASE taskhive;
CREATE DATABASE taskhive;
\q
```
2. Restart the backend.

---

## ✅ What You Should Have After This Step

- [x] Java 21 (Temurin) installed with `JAVA_HOME` set
- [x] `application-dev.properties` configured with your own passwords
- [x] Backend running on `http://localhost:8080`
- [x] Swagger UI accessible at `http://localhost:8080/swagger-ui/index.html`
- [x] All 22 database tables created automatically by Flyway

---

## ➡️ Next Step

Continue to **[03_FRONTEND_NEXTJS_SETUP.md](./03_FRONTEND_NEXTJS_SETUP.md)** to set up the web dashboard.

---

*Document Version: 1.0 | Last Updated: March 2026*
