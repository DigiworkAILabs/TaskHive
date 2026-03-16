# 🐝 TaskHive — Project Overview & Setup Master Guide

> **TaskHive** is an Enterprise Task Management System built with a modern, multi-platform architecture.  
> This document is the **starting point** for setting up the entire project on a brand-new Windows laptop.

---

## 📦 What Is TaskHive?

TaskHive is a full-stack task management platform with:

| Layer | Technology | Purpose |
|---|---|---|
| **Backend API** | Java 21 + Spring Boot 3.5.11 | REST API, Authentication, Business Logic, WebSockets |
| **Web Frontend** | Next.js 16 + React 19 + TypeScript | Admin & Employee Web Dashboard |
| **Mobile (Flutter)** | Flutter 3.24+ / Dart 3.4+ | Cross-platform mobile app (iOS & Android) |
| **Mobile (React Native)** | React Native 0.84.1 | Alternative cross-platform mobile app |
| **Android WebView** | Native Android (Java) | Lightweight Android wrapper for the web app |
| **ML Service** | Python 3.14 + FastAPI | Task priority prediction, completion time estimation, workload balancing |
| **Database** | PostgreSQL 16 | Primary relational database with Flyway migrations |

---

## 🗂️ Project Folder Structure

```
TaskHive/
├── taskhive-backend/        → Spring Boot API Server
├── taskhive-frontend/       → Next.js Web Dashboard
├── taskhive_flutter/        → Flutter Mobile App
├── taskhive-ReactNative/    → React Native Mobile App
├── taskhive-webview/        → Android WebView Wrapper
├── ML/
│   ├── inference-server/    → FastAPI ML Prediction Server
│   ├── training/            → Model Training Scripts & Data Generators
│   └── pipeline/            → Full Pipeline Runner
├── docs/
│   └── setup/               → 📍 YOU ARE HERE — Setup Guides
└── tests/                   → Test suites
```

---

## 🚀 Setup Order (Follow This Sequence!)

> [!IMPORTANT]
> You **must** set up the components in this exact order. Each step depends on the previous one.

| Step | Doc File | What You'll Install |
|---:|---|---|
| **1** | [01_DATABASE_SETUP.md](./01_DATABASE_SETUP.md) | PostgreSQL 16 Database Server |
| **2** | [02_BACKEND_SETUP.md](./02_BACKEND_SETUP.md) | Java 21 (JDK), Maven, Spring Boot Backend |
| **3** | [03_FRONTEND_NEXTJS_SETUP.md](./03_FRONTEND_NEXTJS_SETUP.md) | Node.js 22, Next.js Web Frontend |
| **4** | [04_ML_SETUP.md](./04_ML_SETUP.md) | Python 3.14, FastAPI ML Service |
| **5** | [05_FLUTTER_SETUP.md](./05_FLUTTER_SETUP.md) | Flutter SDK, Dart, Mobile App |
| **6** | [06_REACTNATIVE_SETUP.md](./06_REACTNATIVE_SETUP.md) | React Native CLI, Mobile App |
| **7** | [07_WEBVIEW_SETUP.md](./07_WEBVIEW_SETUP.md) | Android Studio, WebView App |

---

## ⚠️ Before You Begin — Pre-Requisites

### Minimum System Requirements

| Resource | Minimum |
|---|---|
| **OS** | Windows 10 (64-bit) or Windows 11 |
| **RAM** | 16 GB (8 GB bare minimum) |
| **Disk Space** | 50 GB free space |
| **Internet** | Stable broadband connection |

### Things to Know

1. **You do NOT need to set up ALL components.** If you only need the web app, set up steps 1 → 2 → 3. If you also want ML predictions, add step 4.
2. **Secret keys have been removed.** You will need to provide your own database password, SMTP email credentials, and JWT secret. Each guide tells you exactly where and how.
3. **All download links point to the exact versions used in this project.** Do not use different versions unless you know what you're doing.

---

## 🔐 Configuration Files You Need to Update

After setting up each component, you'll need to add your own credentials. Here's a summary:

| File | Secrets to Configure |
|---|---|
| `taskhive-backend/src/main/resources/application-dev.properties` | DB password, SMTP email & password, JWT secret |
| `taskhive-frontend/.env.local` | API URL (usually no change needed for local) |
| `ML/inference-server/.env` | No secrets, just server config |
| `taskhive_flutter/assets/env/.env.dev` | API URL for Flutter app |

> [!TIP]
> Each setup guide has a dedicated **"Configure Secrets"** section with exact instructions.

---

## 🛟 Getting Help

If you get stuck at any step:
1. **Re-read the error message carefully** — most issues are path or version mismatches
2. **Check that the previous step completed successfully** — e.g., is PostgreSQL running before you start the backend?
3. **Restart your terminal/command prompt** after installing new software — environment variables need a fresh terminal to take effect

---

*Document Version: 1.0 | Last Updated: March 2026*
