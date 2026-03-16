# 🌐 Step 3 — Next.js Frontend Setup

> This guide walks you through installing **Node.js 22**, setting up the Next.js 16 web frontend, and running the dashboard.  
> **Estimated Time:** 15–20 minutes

---

## Table of Contents

1. [Install Node.js 22](#1-install-nodejs-22)
2. [Verify Node.js Installation](#2-verify-nodejs-installation)
3. [Configure Environment Variables](#3-configure-environment-variables)
4. [Install Dependencies](#4-install-dependencies)
5. [Run the Frontend](#5-run-the-frontend)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. Install Node.js 22

> [!IMPORTANT]
> Download **Node.js 22 LTS** specifically. This project requires Node.js version 22 or higher.

**Download Link:**  
👉 [https://nodejs.org/en/download/](https://nodejs.org/en/download/)

### Download Steps

1. Open the link above.
2. Click on the **LTS** tab (this should show version 22.x.x).
3. Click the **Windows Installer (.msi)** for **64-bit**.
4. Save the file to your Downloads folder.

### Install Steps

1. **Double-click** the downloaded `.msi` file.
2. Click **Next** on the welcome screen.
3. Accept the license agreement → Click **Next**.
4. Leave the default installation path (`C:\Program Files\nodejs`) → Click **Next**.
5. On the **Custom Setup** screen, leave all defaults → Click **Next**.
6. ✅ **Check the box** that says *"Automatically install the necessary tools..."* → Click **Next**.
7. Click **Install** → Wait → Click **Finish**.

> [!NOTE]
> Node.js installation includes **npm** (Node Package Manager) automatically. You do NOT need to install npm separately.

---

## 2. Verify Node.js Installation

1. **Close any open Command Prompt windows.**
2. Open a **new Command Prompt** (`Win + R` → type `cmd` → Enter).
3. Run these commands:

```
node -v
```

Expected output (version 22 or higher):
```
v22.14.0
```

```
npm -v
```

Expected output (version 10 or higher):
```
10.9.2
```

If both commands show version numbers, the installation was successful! ✅

---

## 3. Configure Environment Variables

The frontend needs to know where the backend API is running. This is configured in a file called `.env.local`.

### 📂 File to Edit

| Property | Value |
|---|---|
| **File Name** | `.env.local` |
| **Full Path** | `TaskHive\taskhive-frontend\.env.local` |
| **What it does** | Tells the frontend where the backend API server is running |

### How to Open

1. Open **File Explorer**.
2. Navigate to: `TaskHive\taskhive-frontend\`
3. Look for a file called `.env.local`.
4. Right-click it → **Open with** → **Notepad**.

> [!NOTE]
> Files starting with a dot (`.env.local`) may be hidden on some Windows configurations.  
> To show hidden files: In File Explorer, click **View** → check **Hidden items**.

### Verify / Edit the Contents

The file should contain exactly these two lines:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_WS_URL=http://localhost:8080/ws
```

| Variable | Purpose | When to change |
|---|---|---|
| `NEXT_PUBLIC_API_URL` | Backend REST API address | Only if you changed the backend port from `8080` |
| `NEXT_PUBLIC_WS_URL` | Backend WebSocket address | Only if you changed the backend port from `8080` |

> [!TIP]
> If you changed the backend port to `8081` in Step 2 (`server.port=8081` in `application-dev.properties`), update both URLs here to use `8081` as well.

### If the File Doesn't Exist — Create It

1. Open **Notepad**.
2. Paste these two lines:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_WS_URL=http://localhost:8080/ws
```
3. Click **File → Save As**.
4. Navigate to `TaskHive\taskhive-frontend\`.
5. In the **File name** field, type: `.env.local` (including the leading dot).
6. Change **Save as type** to **All Files (*.*)**.
7. Click **Save**.

### 📂 How This File Is Used in the Code

The `.env.local` values are read by the Next.js app in these files:

| Code File | Line | What it reads |
|---|---|---|
| `taskhive-frontend\shared\services\api\apiClient.ts` | Line 5 | `process.env.NEXT_PUBLIC_API_URL` → uses it as the base URL for all API calls |
| `taskhive-frontend\features\notification\services\notificationService.ts` | Line 7 | `process.env.NEXT_PUBLIC_API_URL` → uses it for notification endpoints |

---

## 4. Install Dependencies

1. Open **Command Prompt** (`Win + R` → type `cmd` → Enter).
2. Navigate to the frontend folder:

```
cd /d "D:\Internship\TaskHive\taskhive-frontend"
```

> ⚠️ Replace `D:\Internship\TaskHive` with **your actual project path** if different.

3. Install all dependencies:

```
npm install
```

> This will take 2–5 minutes on the first run. It downloads all required packages.  
> Wait until you see a message like `added XXX packages in XXs`.

> [!WARNING]
> If you see **WARN** messages during install, that's usually fine. Only **ERROR** messages are a problem.

---

## 5. Run the Frontend

1. In the same Command Prompt window, run:

```
npm run dev
```

2. Wait for the output:
```
  ▲ Next.js 16.1.6
  - Local:        http://localhost:3000
  - Environments: .env.local

 ✓ Ready in X.Xs
```

3. Open your web browser and go to: **[http://localhost:3000](http://localhost:3000)**

4. You should see the **TaskHive** login page! ✅

> [!IMPORTANT]
> Make sure the **backend server is running** (Step 2) before trying to log in. The frontend communicates with the backend API.

### Default Login Credentials

Use the admin account that was created automatically:

| Field | Value |
|---|---|
| Email | `admin@taskhive.com` |
| Password | *(the password you set in `app.admin.password` in Step 2)* |

> [!NOTE]
> The admin password is configured in the backend file:  
> 📂 `TaskHive\taskhive-backend\src\main\resources\application-dev.properties` → line `app.admin.password=...`

---

## 6. Troubleshooting

### ❌ "'node' is not recognized as an internal or external command"

Node.js was not added to PATH. Fix:
1. Press `Win + R` → type `sysdm.cpl` → press Enter.
2. Go to **Advanced** tab → **Environment Variables**.
3. Under **System variables**, find **Path** → click **Edit**.
4. Click **New** → add: `C:\Program Files\nodejs`
5. Click **OK** on all windows → close and reopen Command Prompt.

### ❌ "npm ERR! code ENOENT" during `npm install`

You're not in the correct directory. Make sure you're in:
```
TaskHive\taskhive-frontend\
```

Run `dir` and check that you see `package.json` in the file list.

### ❌ "Port 3000 is already in use"

Another application is using port 3000. You can:
- Stop the other application, OR
- Run the frontend on a different port:
```
npx next dev -p 3001
```

### ❌ "Network Error" or API calls fail in the browser

- Make sure the backend is running on `http://localhost:8080`.
- Check that `.env.local` (📂 `TaskHive\taskhive-frontend\.env.local`) has the correct API URL.
- Open browser DevTools (`F12`) → **Console** tab to see detailed error messages.

### ❌ "Module not found" errors

Dependencies may not be installed correctly. Run:
```
rd /s /q node_modules
del package-lock.json
npm install
```

---

## ✅ What You Should Have After This Step

- [x] Node.js 22 installed with npm
- [x] `.env.local` configured with correct API URLs
- [x] All npm dependencies installed
- [x] Frontend running on `http://localhost:3000`
- [x] Login page visible in the browser

---

## 📂 Files Summary — What Was Configured in This Step

| File | Full Path | What Was Configured |
|---|---|---|
| `.env.local` | `TaskHive\taskhive-frontend\.env.local` | Backend API URL & WebSocket URL |

---

## ➡️ Next Step

Continue to **[04_ML_SETUP.md](./04_ML_SETUP.md)** to set up the ML prediction service.

---

*Document Version: 1.1 | Last Updated: March 2026*
