# 🗄️ Step 1 — PostgreSQL Database Setup

> This guide walks you through installing **PostgreSQL 16** on Windows and creating the TaskHive database.  
> **Estimated Time:** 15–20 minutes

---

## Table of Contents

1. [Download PostgreSQL](#1-download-postgresql)
2. [Install PostgreSQL](#2-install-postgresql)
3. [Verify Installation](#3-verify-installation)
4. [Create the TaskHive Database](#4-create-the-taskhive-database)
5. [Test the Connection](#5-test-the-connection)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. Download PostgreSQL

> [!IMPORTANT]
> Download **PostgreSQL version 16** specifically. Other versions may cause compatibility issues.

**Download Link:**  
👉 [https://www.enterprisedb.com/downloads/postgres-postgresql-downloads](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads)

1. Open the link above in your browser.
2. Find the row for **Version 16.x** (e.g., 16.8).
3. Click the **Windows x86-64** download button.
4. Save the installer file (e.g., `postgresql-16.8-1-windows-x64.exe`) to your **Downloads** folder.

---

## 2. Install PostgreSQL

1. **Double-click** the downloaded `.exe` file to start the installer.
2. If Windows asks *"Do you want to allow this app to make changes?"* — click **Yes**.

### Installation Steps

| Step | Action |
|---:|---|
| Welcome Screen | Click **Next** |
| Installation Directory | Leave the default path (`C:\Program Files\PostgreSQL\16`) → Click **Next** |
| Select Components | ✅ Make sure **all 4 are checked**: PostgreSQL Server, pgAdmin 4, Stack Builder, Command Line Tools → Click **Next** |
| Data Directory | Leave the default → Click **Next** |
| Password | **Enter a password for the `postgres` user** → ⚠️ **WRITE THIS PASSWORD DOWN — you will need it later!** → Click **Next** |
| Port | Leave the default port as **5432** → Click **Next** |
| Locale | Leave as **Default locale** → Click **Next** |
| Pre Installation Summary | Click **Next** |
| Ready to Install | Click **Next** → Wait for installation to complete |
| Completing | **Uncheck** "Launch Stack Builder at exit" → Click **Finish** |

> [!CAUTION]
> **Remember your password!** If you forget it, you will need to reinstall PostgreSQL. This password is needed in the backend configuration.

---

## 3. Verify Installation

### Option A: Using pgAdmin (Graphical Tool)

1. Press the **Windows key** on your keyboard.
2. Type `pgAdmin` and press **Enter**.
3. pgAdmin will open in your web browser.
4. The first time you open it, it will ask you to set a **master password** — enter any password you like (this is just for pgAdmin, not the database).
5. On the left sidebar, you should see **Servers → PostgreSQL 16**. Click to expand it.
6. Enter the password you set during installation.
7. If you can see the server without errors, PostgreSQL is working! ✅

### Option B: Using Command Prompt

1. Press `Win + R`, type `cmd`, and press **Enter**.
2. Run this command:

```
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres
```

3. Enter the password you set during installation.
4. You should see a prompt like `postgres=#`. Type `\q` and press **Enter** to exit.

---

## 4. Create the TaskHive Database

> [!NOTE]
> The backend uses **Flyway** to automatically create all database tables when it first starts. You only need to create the **empty database** here.

### Using pgAdmin (Recommended)

1. Open **pgAdmin 4** (search for it in the Start menu).
2. In the left sidebar, expand **Servers → PostgreSQL 16**.
3. Enter your password if prompted.
4. Right-click on **Databases** → select **Create → Database...**
5. In the **Database** field, type: `taskhive`
6. Leave the **Owner** as `postgres`.
7. Click **Save**.

### Using Command Prompt (Alternative)

1. Open **Command Prompt** (press `Win + R`, type `cmd`, press Enter).
2. Run:

```
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres
```

3. Enter your password.
4. Run this SQL command:

```sql
CREATE DATABASE taskhive;
```

5. You should see `CREATE DATABASE` as confirmation.
6. Type `\q` and press Enter to exit.

---

## 5. Test the Connection

Let's verify the database was created:

1. Open **Command Prompt**.
2. Run:

```
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d taskhive
```

3. Enter your password.
4. You should see `taskhive=#` as the prompt. This means the database exists and is accessible!
5. Type `\q` and press Enter to exit.

---

## 6. Troubleshooting

### ❌ "psql is not recognized as an internal or external command"

The PostgreSQL `bin` directory is not in your system PATH. You can either:
- Use the full path: `"C:\Program Files\PostgreSQL\16\bin\psql.exe"`
- Or add PostgreSQL to your PATH:
  1. Press `Win + R`, type `sysdm.cpl`, press Enter.
  2. Go to the **Advanced** tab → click **Environment Variables**.
  3. Under **System variables**, find **Path** → click **Edit**.
  4. Click **New** → paste: `C:\Program Files\PostgreSQL\16\bin`
  5. Click **OK** on all windows.
  6. **Close and reopen** your Command Prompt.

### ❌ "connection refused" or "could not connect to server"

PostgreSQL service may not be running:
1. Press `Win + R`, type `services.msc`, press Enter.
2. Search for **postgresql-x64-16** in the list.
3. If the Status is not **Running**, right-click it → select **Start**.

### ❌ "password authentication failed"

You entered the wrong password. If you can't remember:
1. Open **pgAdmin 4** and try there (it may have saved the password).
2. As a last resort, reinstall PostgreSQL and set a new password.

---

## ✅ What You Should Have After This Step

- [x] PostgreSQL 16 installed and running on port `5432`
- [x] pgAdmin 4 available for database management
- [x] An empty database called `taskhive` created
- [x] Your PostgreSQL password noted down safely

---

## ➡️ Next Step

Continue to **[02_BACKEND_SETUP.md](./02_BACKEND_SETUP.md)** to set up the Java backend server.

---

*Document Version: 1.0 | Last Updated: March 2026*
