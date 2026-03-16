# 📦 Step 7 — Android WebView App Setup

> This guide walks you through setting up the **Android WebView** wrapper — a lightweight Android app that wraps the TaskHive Next.js web frontend into a native Android application.  
> **Estimated Time:** 15–20 minutes

---

## Table of Contents

1. [What Is the WebView App?](#1-what-is-the-webview-app)
2. [Prerequisites Check](#2-prerequisites-check)
3. [Open the Project in Android Studio](#3-open-the-project-in-android-studio)
4. [Configure the Base URL](#4-configure-the-base-url)
5. [Sync & Build the Project](#5-sync--build-the-project)
6. [Run the WebView App](#6-run-the-webview-app)
7. [Troubleshooting](#7-troubleshooting)

---

## 1. What Is the WebView App?

The WebView app is a **thin Android wrapper** that loads the TaskHive web frontend (Next.js) inside a native Android WebView component. Think of it as a "browser in an app" that:

- Opens your Next.js web app in a full-screen native Android container
- Supports JavaScript and DOM storage
- Handles back navigation within the web app
- Provides a native app icon and launch experience

> [!NOTE]
> This is **not** a standalone mobile app. It requires the **Next.js frontend** (Step 3) to be running and accessible from the device/emulator.

---

## 2. Prerequisites Check

| Tool | Required | Already installed if you completed... |
|---|---|---|
| **Android Studio** | ✅ Yes | Step 5 (Flutter) or Step 6 (React Native) |
| **JDK 21** | ✅ Yes | Step 2 (Backend) |

If you haven't installed Android Studio yet, follow the instructions in [05_FLUTTER_SETUP.md — Section 3](./05_FLUTTER_SETUP.md#3-install-android-studio).

### Project Details

| Setting | Value |
|---|---|
| Android Gradle Plugin (AGP) | 9.0.1 |
| Gradle | 9.2.1 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 (Android 16) |
| Compile SDK | 36 |
| Java Compatibility | 11 |

---

## 3. Open the Project in Android Studio

1. Open **Android Studio** from the Start menu.
2. Click **"Open"** (or File → Open).
3. Navigate to and select the folder:

```
D:\Internship\TaskHive\taskhive-webview
```

> ⚠️ Replace with **your actual project path**.

4. Click **OK**.
5. Android Studio will start syncing the project with Gradle. Wait for it to finish (you'll see the progress bar at the bottom).

> [!WARNING]
> The first sync may take 5–10 minutes as it downloads Gradle 9.2.1 and all Android dependencies.

---

## 4. Configure the Base URL

The WebView app loads a URL configured in `strings.xml`. You need to make sure it points to the correct address.

### Find the File

Navigate to:
```
taskhive-webview\app\src\main\res\values\strings.xml
```

### Update the URL

Open `strings.xml` and check the `base_url` value:

```xml
<resources>
    <string name="app_name">TaskHive</string>
    <string name="base_url">http://10.0.2.2:3000</string>
</resources>
```

> [!IMPORTANT]
> **For Android Emulator:** Use `http://10.0.2.2:3000` — the `10.0.2.2` address maps to your computer's `localhost` from inside the emulator.
>
> **For Physical Device:** Use your computer's local IP address instead. Find it by running `ipconfig` in Command Prompt and looking for the `IPv4 Address` (e.g., `http://192.168.1.100:3000`).

Make sure the port matches where your Next.js frontend is running (default: `3000`).

---

## 5. Sync & Build the Project

### In Android Studio:

1. Click **File → Sync Project with Gradle Files** (or click the 🐘 elephant icon with a refresh arrow in the toolbar).
2. Wait for sync to complete — you should see **"BUILD SUCCESSFUL"** in the Build output window.

If the sync completes without errors, the project is ready to run! ✅

---

## 6. Run the WebView App

### Option A: Run on Emulator

1. In Android Studio, click the **Device Manager** (phone icon on the right sidebar).
2. Click ▶ to start an emulator (use the one you created in earlier steps).
3. Wait for the emulator to boot up.
4. In the toolbar, select the emulator from the device dropdown.
5. Click the **▶ Run** button (green play button) or press `Shift + F10`.
6. Wait for the app to build and install (first build takes 2-3 minutes).
7. The app should open and display the TaskHive web interface! ✅

> [!IMPORTANT]
> Make sure the **Next.js frontend is running** (`npm run dev` in the `taskhive-frontend` folder) before launching the WebView app. Otherwise, you'll see a blank screen or an error.

### Option B: Run on Physical Device

1. Enable **Developer Options** and **USB Debugging** on your phone.
2. Connect your phone via USB.
3. Your device should appear in the device dropdown in Android Studio's toolbar.
4. Select your device → click ▶ Run.
5. The app installs and launches on your phone.

### Option C: Build from Command Line

1. Open Command Prompt.
2. Navigate to the project:
```
cd /d "D:\Internship\TaskHive\taskhive-webview"
```
3. Build the debug APK:
```
gradlew.bat assembleDebug
```
4. The APK will be at: `app\build\outputs\apk\debug\app-debug.apk`
5. Install on a connected device/emulator:
```
adb install app\build\outputs\apk\debug\app-debug.apk
```

---

## 7. Troubleshooting

### ❌ "Gradle sync failed" in Android Studio

- Make sure you have **JDK 21** installed and `JAVA_HOME` is set (see Step 2).
- In Android Studio: File → Settings → Build → Gradle → set **Gradle JDK** to your JDK 21 installation.
- Try: File → Invalidate Caches → Restart.

### ❌ "SDK not found" or "SDK version XX not installed"

1. In Android Studio: File → Settings → Languages & Frameworks → Android SDK.
2. Check the box for **Android 16.0 (API 36)**.
3. Click **Apply** → **OK** and wait for the download.

### ❌ Blank screen or "Webpage not available"

- The Next.js frontend is not running. Start it:
```
cd /d "D:\Internship\TaskHive\taskhive-frontend"
npm run dev
```
- Check the `base_url` in `strings.xml` is correct.
- For emulator: use `http://10.0.2.2:3000`.
- For physical device: use your computer's IP address.

### ❌ "INSTALL_FAILED_UPDATE_INCOMPATIBLE"

A previous version of the app with a different signing key is installed. Uninstall it first:
```
adb uninstall com.digiwork.taskhive
```
Then try running again.

---

## ✅ What You Should Have After This Step

- [x] WebView project opened in Android Studio
- [x] Base URL configured in `strings.xml`
- [x] Gradle sync and build successful
- [x] WebView app running on emulator or physical device
- [x] TaskHive web interface visible inside the native app

---

## 🎉 Setup Complete!

Congratulations! You've completed the full TaskHive project setup. Here's a summary of everything you installed:

| Component | URL/Location | Status |
|---|---|---|
| PostgreSQL Database | `localhost:5432` | ✅ Running |
| Spring Boot Backend | `http://localhost:8080` | ✅ Running |
| Next.js Frontend | `http://localhost:3000` | ✅ Running |
| ML Inference Server | `http://localhost:8000` | ✅ Running |
| Flutter Mobile App | Android Emulator / Device | ✅ Built |
| React Native App | Android Emulator / Device | ✅ Built |
| WebView App | Android Emulator / Device | ✅ Built |

### Running Everything Together

To run the full system, open **4 separate Command Prompt windows** and run:

| Window | Directory | Command |
|---:|---|---|
| 1 | `taskhive-backend` | `mvnw.cmd spring-boot:run` |
| 2 | `taskhive-frontend` | `npm run dev` |
| 3 | `ML\inference-server` | `venv\Scripts\activate` then `python main.py` |
| 4 | *(optional)* | Mobile app via Android Studio or CLI |

> [!TIP]
> Remember: PostgreSQL runs as a **Windows service** in the background — you don't need a terminal for it. Verify it's running in `services.msc`.

---

*Document Version: 1.0 | Last Updated: March 2026*
