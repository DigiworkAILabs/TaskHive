# 📱 Step 6 — React Native Mobile App Setup

> This guide walks you through setting up the **React Native 0.84.1** mobile app with the React Native CLI.  
> **Estimated Time:** 30–45 minutes

---

## Table of Contents

1. [Prerequisites Check](#1-prerequisites-check)
2. [Install JDK 21](#2-install-jdk-21)
3. [Install Node.js 22](#3-install-nodejs-22)
4. [Install Android Studio](#4-install-android-studio)
5. [Configure Environment Variables](#5-configure-environment-variables)
6. [Set Up Android Emulator](#6-set-up-android-emulator)
7. [Install Project Dependencies](#7-install-project-dependencies)
8. [Configure API Base URL](#8-configure-api-base-url)
9. [Run the React Native App](#9-run-the-react-native-app)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Prerequisites Check

Before starting, check if you already have these tools from previous steps:

| Tool | Already installed if you completed... | How to check |
|---|---|---|
| **Java 21 (JDK)** | Step 2 (Backend Setup) | `java -version` |
| **Node.js 22** | Step 3 (Next.js Setup) | `node -v` |
| **Android Studio** | Step 5 (Flutter Setup) | Open from Start menu |

If you see the correct versions when running the check commands, **skip** those installation steps below.

---

## 2. Install JDK 21

> Skip this if you already installed Java 21 in [Step 2 (Backend Setup)](./02_BACKEND_SETUP.md#1-install-java-21-jdk).

**Download Link:**  
👉 [https://adoptium.net/temurin/releases/?os=windows&arch=x64&package=jdk&version=21](https://adoptium.net/temurin/releases/?os=windows&arch=x64&package=jdk&version=21)

Follow the installation steps in [02_BACKEND_SETUP.md — Section 1](./02_BACKEND_SETUP.md#1-install-java-21-jdk).

---

## 3. Install Node.js 22

> Skip this if you already installed Node.js in [Step 3 (Next.js Setup)](./03_FRONTEND_NEXTJS_SETUP.md#1-install-nodejs-22).

**Download Link:**  
👉 [https://nodejs.org/en/download/](https://nodejs.org/en/download/)

Follow the installation steps in [03_FRONTEND_NEXTJS_SETUP.md — Section 1](./03_FRONTEND_NEXTJS_SETUP.md#1-install-nodejs-22).

> [!IMPORTANT]
> This project requires **Node.js >= 22.11.0** as specified in the project config.

### 📂 Where This Requirement Comes From

| File | Full Path | Line |
|---|---|---|
| `package.json` | `TaskHive\taskhive-ReactNative\package.json` | Line 77: `"node": ">= 22.11.0"` |

---

## 4. Install Android Studio

> Skip this if you already installed Android Studio in [Step 5 (Flutter Setup)](./05_FLUTTER_SETUP.md#3-install-android-studio).

**Download Link:**  
👉 [https://developer.android.com/studio](https://developer.android.com/studio)

Follow the installation steps in [05_FLUTTER_SETUP.md — Section 3](./05_FLUTTER_SETUP.md#3-install-android-studio).

---

## 5. Configure Environment Variables

React Native requires specific environment variables to find the Android SDK and Java.

> [!IMPORTANT]
> This step is **critical**. If these variables are not set, the build will fail.

### Set Up ANDROID_HOME

1. Press `Win + R` → type `sysdm.cpl` → press Enter.
2. Go to **Advanced** tab → click **Environment Variables**.
3. Under **User variables** (top section), click **New**:
   - Variable name: `ANDROID_HOME`
   - Variable value: `C:\Users\<YOUR_USERNAME>\AppData\Local\Android\Sdk`
   
   > Replace `<YOUR_USERNAME>` with your Windows username. To find it, open Command Prompt and run `echo %USERNAME%`.

4. Now find **Path** under **User variables** → click **Edit** → add these **four** entries (click **New** for each):

```
%ANDROID_HOME%\emulator
%ANDROID_HOME%\platform-tools
%ANDROID_HOME%\cmdline-tools\latest\bin
%ANDROID_HOME%\tools
```

5. **Verify JAVA_HOME** is set under **System variables**:
   - It should already be set from Step 2. If not, create it:
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot` (check the actual folder name under `C:\Program Files\Eclipse Adoptium\`)

6. Click **OK** on all windows.
7. **Close and reopen** Command Prompt.

### Verify Environment Variables

Run these in the new Command Prompt:

```
echo %ANDROID_HOME%
```
Should print a path like: `C:\Users\YourName\AppData\Local\Android\Sdk`

```
echo %JAVA_HOME%
```
Should print a path like: `C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot`

```
adb --version
```
Should print: `Android Debug Bridge version 1.x.x`

### 📂 Android SDK Configuration in the Project

The React Native project has these Android SDK settings pre-configured:

| Property | Value | Configured In |
|---|---|---|
| `buildToolsVersion` | `36.0.0` | `TaskHive\taskhive-ReactNative\android\build.gradle` (Line 3) |
| `compileSdkVersion` | `36` | `TaskHive\taskhive-ReactNative\android\build.gradle` (Line 5) |
| `targetSdkVersion` | `36` | `TaskHive\taskhive-ReactNative\android\build.gradle` (Line 6) |
| `minSdkVersion` | `24` (Android 7.0) | `TaskHive\taskhive-ReactNative\android\build.gradle` (Line 4) |
| `ndkVersion` | `27.1.12297006` | `TaskHive\taskhive-ReactNative\android\build.gradle` (Line 7) |
| `kotlinVersion` | `2.1.20` | `TaskHive\taskhive-ReactNative\android\build.gradle` (Line 8) |
| Gradle Version | `9.0.0` | `TaskHive\taskhive-ReactNative\android\gradle\wrapper\gradle-wrapper.properties` (Line 3) |

> [!NOTE]
> You do NOT need to change any of these values. They are listed here for reference only.

---

## 6. Set Up Android Emulator

> Skip if you already set up an emulator in [Step 5 (Flutter Setup)](./05_FLUTTER_SETUP.md#4-set-up-android-emulator).

1. Open **Android Studio**.
2. Click **"More Actions"** → **"Device Manager"**.
3. Click **"Create Virtual Device"**.
4. Choose **"Pixel 6"** → **Next**.
5. Download **API 34 (x86_64)** system image → **Next** → **Finish**.
6. Click **▶ Play** to launch the emulator.

---

## 7. Install Project Dependencies

1. Open **Command Prompt**.
2. Navigate to the React Native project:

```
cd /d "D:\Internship\TaskHive\taskhive-ReactNative"
```

> ⚠️ Replace `D:\Internship\TaskHive` with **your actual project path** if different.

3. Install all dependencies:

```
npm install
```

> This downloads all packages listed in `package.json`. It may take 3–5 minutes on first run.  
> Wait for: `added XXX packages in XXs`

> [!WARNING]
> If npm gives errors about **peer dependency conflicts**, try:
> ```
> npm install --legacy-peer-deps
> ```

### Key Dependencies Installed

| Package | Version | Purpose |
|---|---|---|
| react-native | 0.84.1 | Core mobile framework |
| react | 19.2.3 | UI library |
| @react-navigation/* | 7.x | Screen navigation |
| axios | 1.13.6 | HTTP client for API calls |
| @tanstack/react-query | 5.90.21 | Data fetching & caching |
| zustand | 5.0.11 | State management |
| react-native-reanimated | 4.2.2 | Animations |
| @react-native-firebase/* | 23.8.6 | Push notifications |

---

## 8. Configure API Base URL

> [!NOTE]
> The React Native app's network layer is in early setup phase. The `src\core\network\` directory currently contains only a `.gitkeep` placeholder file. This means the API client setup will need to be completed before the app can communicate with the backend.

### 📂 Network Configuration Directory

| Directory | Full Path | Status |
|---|---|---|
| Network Module | `TaskHive\taskhive-ReactNative\src\core\network\` | Placeholder only (`.gitkeep`) |

### When the API client is implemented, you'll need to configure these values:

| Setting | Value (for Emulator) | Value (for Physical Device) |
|---|---|---|
| Backend API URL | `http://10.0.2.2:8080/api/v1` | `http://<YOUR_COMPUTER_IP>:8080/api/v1` |
| WebSocket URL | `ws://10.0.2.2:8080/ws` | `ws://<YOUR_COMPUTER_IP>:8080/ws` |

> [!TIP]
> To find your computer's IP address, run `ipconfig` in Command Prompt and look for the `IPv4 Address` under your active network adapter (e.g., `192.168.1.100`).

### 📂 Related Backend Configuration

The backend server URL and port are configured in:

| File | Full Path | Key Properties |
|---|---|---|
| `application-dev.properties` | `TaskHive\taskhive-backend\src\main\resources\application-dev.properties` | `server.port=8080` (Line 72) |

---

## 9. Run the React Native App

### Start Metro Bundler

1. In Command Prompt (from the `taskhive-ReactNative` directory):

```
npm start
```

This starts the **Metro bundler** — a JavaScript build tool. You'll see:
```
info Welcome to React Native v0.84
info Starting dev server on port 8081...
```

> [!NOTE]
> Keep this terminal window **open** — Metro must stay running while the app is in use.

### Run on Android Emulator

2. Open a **second Command Prompt** window.
3. Navigate to the project:

```
cd /d "D:\Internship\TaskHive\taskhive-ReactNative"
```

4. Make sure the Android emulator is running (check Android Studio Device Manager).
5. Run:

```
npm run android
```

This will:
1. Build the app using Gradle (first build takes 5-10 minutes)
2. Install the app on the emulator
3. Launch the app

You should see the **TaskHive app** on the emulator! ✅

### Run on Physical Android Device

1. Enable **Developer Options** on your phone:
   - Settings → About Phone → tap **Build Number** 7 times.
2. Enable **USB Debugging**:
   - Settings → Developer Options → turn on **USB Debugging**.
3. Connect your phone via USB cable.
4. Check the device is detected:
```
adb devices
```
5. Run:
```
npm run android
```

---

## 10. Troubleshooting

### ❌ "SDK location not found"

`ANDROID_HOME` is not set correctly. Refer to [Section 5](#5-configure-environment-variables).

### ❌ "FAILURE: Build failed with an exception" (Gradle)

Try these in order:
```
cd android
gradlew clean
cd ..
npm run android
```

If that doesn't work:
1. Delete the `.gradle` folders:
```
rd /s /q android\.gradle
rd /s /q "%USERPROFILE%\.gradle\caches"
```
2. Retry `npm run android`.

### ❌ "Unable to load script" or Metro bundler errors

1. Make sure Metro bundler is running (`npm start` in a separate terminal).
2. If using a physical device, make sure your phone and computer are on the **same WiFi network**.
3. Shake the device (or press `Ctrl+M` on emulator) → **Settings** → set **Debug server host** to your computer's IP address and port `8081`.

### ❌ "No connected devices found"

- Emulator: Make sure it's running. Open Android Studio → Device Manager → click ▶.
- Physical device: Check USB cable and that USB debugging is enabled.
- Run `adb devices` — your device should appear in the list.

### ❌ "'adb' is not recognized"

The Android platform-tools are not in your PATH. Make sure you added `%ANDROID_HOME%\platform-tools` to your Path variable (Section 5).

### ❌ "Error: unable to resolve module" 

Dependencies might be corrupted. Clean reinstall:
```
rd /s /q node_modules
del package-lock.json
npm install
npm start -- --reset-cache
```

---

## ✅ What You Should Have After This Step

- [x] Java 21, Node.js 22, and Android Studio installed
- [x] `ANDROID_HOME` and `JAVA_HOME` environment variables set
- [x] Android emulator running
- [x] All npm dependencies installed
- [x] React Native app running on emulator or physical device

---

## 📂 Files Summary — Configuration Files in This Module

| File | Full Path | Purpose |
|---|---|---|
| `package.json` | `TaskHive\taskhive-ReactNative\package.json` | Node.js engine requirement (`>= 22.11.0`), all dependencies |
| `build.gradle` (root) | `TaskHive\taskhive-ReactNative\android\build.gradle` | Android SDK versions, Kotlin version, NDK version |
| `build.gradle` (app) | `TaskHive\taskhive-ReactNative\android\app\build.gradle` | Application ID (`com.taskhivern`), signing config |
| `gradle-wrapper.properties` | `TaskHive\taskhive-ReactNative\android\gradle\wrapper\gradle-wrapper.properties` | Gradle version (`9.0.0`) |
| `tsconfig.json` | `TaskHive\taskhive-ReactNative\tsconfig.json` | TypeScript path aliases (`@core/*`, `@features/*`, `@assets/*`) |
| `babel.config.js` | `TaskHive\taskhive-ReactNative\babel.config.js` | Babel module resolver config |
| `metro.config.js` | `TaskHive\taskhive-ReactNative\metro.config.js` | Metro bundler configuration |
| Network module | `TaskHive\taskhive-ReactNative\src\core\network\` | API client setup (placeholder — `.gitkeep` only) |

---

## ➡️ Next Step

Continue to **[07_WEBVIEW_SETUP.md](./07_WEBVIEW_SETUP.md)** to set up the Android WebView wrapper app.

---

*Document Version: 1.1 | Last Updated: March 2026*
