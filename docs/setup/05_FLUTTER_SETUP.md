# 📱 Step 5 — Flutter Mobile App Setup

> This guide walks you through installing **Flutter SDK 3.24+**, setting up the Dart environment, and running the TaskHive Flutter mobile app.  
> **Estimated Time:** 30–45 minutes

---

## Table of Contents

1. [Install Git](#1-install-git)
2. [Install Flutter SDK](#2-install-flutter-sdk)
3. [Install Android Studio](#3-install-android-studio)
4. [Set Up Android Emulator](#4-set-up-android-emulator)
5. [Verify Flutter Installation](#5-verify-flutter-installation)
6. [Configure the Flutter App](#6-configure-the-flutter-app)
7. [Install Dependencies & Generate Code](#7-install-dependencies--generate-code)
8. [Run the Flutter App](#8-run-the-flutter-app)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. Install Git

> [!NOTE]
> Flutter requires **Git** to be installed. If you already have Git installed, skip to Step 2.

**Download Link:**  
👉 [https://git-scm.com/download/win](https://git-scm.com/download/win)

1. Click **"64-bit Git for Windows Setup"** to download.
2. Run the installer.
3. **Click Next on every screen** — the default settings are fine.
4. On the "Adjusting your PATH" screen, select **"Git from the command line and also from 3rd-party software"** (this is the default) → Continue clicking Next → Install → Finish.

### Verify Git

Open a new Command Prompt and run:
```
git --version
```

You should see something like: `git version 2.47.1.windows.1`

---

## 2. Install Flutter SDK

> [!IMPORTANT]
> This project requires **Flutter 3.24.0 or higher** with **Dart 3.4.0 or higher**.

**Download Link:**  
👉 https://docs.flutter.dev/install

### Download Steps

1. Open the link above.
2. Click **"Download Flutter SDK"** — this downloads a `.zip` file (about 1.1 GB).
3. Save it to your Downloads folder.

### Install Steps

1. Create a folder for Flutter. We recommend: `C:\flutter`
   - Open **File Explorer** → go to `C:\` → right-click → **New → Folder** → name it `flutter`.

2. **Extract** the downloaded `.zip` file into `C:\flutter`.
   - Right-click the `.zip` file → **Extract All** → browse to `C:\flutter` → **Extract**.
   - After extraction, you should have: `C:\flutter\flutter\bin\flutter.bat`

3. **Add Flutter to your system PATH:**
   1. Press `Win + R` → type `sysdm.cpl` → press Enter.
   2. Go to **Advanced** tab → click **Environment Variables**.
   3. Under **System variables**, find **Path** → click **Edit**.
   4. Click **New** → add: `C:\flutter\flutter\bin`
   5. Click **OK** on all windows.

4. **Close and reopen** Command Prompt.

5. Verify:
```
flutter --version
```

Expected output (version 3.24 or higher):
```
Flutter 3.24.x • channel stable
Framework • revision xxxxxxx
Engine • revision xxxxxxx
Tools • Dart 3.5.x
```

---

## 3. Install Android Studio

> [!IMPORTANT]
> Android Studio is required for the **Android SDK**, the **Android Emulator**, and **Gradle** support. You don't need to write code in Android Studio — it's just for the tools.

**Download Link:**  
👉 [https://developer.android.com/studio](https://developer.android.com/studio)

### Download Steps

1. Open the link above.
2. Click **"Download Android Studio"**.
3. Accept the terms → the download starts automatically.

### Install Steps

1. Run the downloaded `.exe` file.
2. Click **Next** on the welcome screen.
3. Leave all components checked ✅ (Android Studio, Android Virtual Device) → **Next**.
4. Leave the default install paths → **Next** → **Install** → Wait → **Finish**.
5. Android Studio opens → choose **"Do not import settings"** → **OK**.
6. Click **Next** through the setup wizard.
7. Choose **"Standard"** setup type → **Next**.
8. Choose a UI theme (your preference) → **Next**.
9. Click **Accept** on the license agreements → **Next** → Wait for downloads → **Finish**.

### Accept Android Licenses

Open Command Prompt and run:
```
flutter doctor --android-licenses
```

Type `y` and press Enter for each license prompt.

---

## 4. Set Up Android Emulator

1. Open **Android Studio**.
2. On the welcome screen, click **"More Actions"** (or **Tools** menu) → **"Device Manager"**.
3. Click **"Create Virtual Device"**.
4. Choose **"Pixel 6"** (or any phone) → **Next**.
5. Click **"Download"** next to a system image (recommended: **API 34, x86_64**) → Wait → **Finish**.
6. Click **Next** → **Finish**.
7. Click the **▶ Play** button next to your virtual device to launch it.

> [!TIP]
> The emulator needs to be **running** before you can launch the Flutter app on it.

---

## 5. Verify Flutter Installation

Run the Flutter doctor to check everything is set up:

```
flutter doctor
```

You should see checkmarks ✅ for:
```
[✓] Flutter (Channel stable, 3.24.x)
[✓] Android toolchain
[✓] Android Studio
[✓] Connected device (1 available)  ← (if emulator is running)
```

> [!NOTE]
> It's okay if you have ✗ for **Chrome**, **Visual Studio**, or **Xcode** — those are not needed for this project.

---

## 6. Configure the Flutter App

The Flutter app uses `.env` (environment) files to know where the backend server is running. You need to create these files.

### 📂 Files to Create

| File | Full Path | What It Does |
|---|---|---|
| `.env.dev` | `TaskHive\taskhive_flutter\assets\env\.env.dev` | Development environment config — backend API URL for the Android emulator |
| `.env.prod` | `TaskHive\taskhive_flutter\assets\env\.env.prod` | Production environment config — backend API URL for deployed server |

### 📂 How These Files Are Used in the Code

These environment variables are loaded and used in these source files:

| Code File | Full Path | What it reads |
|---|---|---|
| `main.dart` | `TaskHive\taskhive_flutter\lib\main.dart` (Line 18) | `dotenv.load(fileName: 'assets/env/.env.dev')` — loads the `.env.dev` file at app startup |
| `dio_client.dart` | `TaskHive\taskhive_flutter\lib\core\network\dio_client.dart` (Line 25) | `dotenv.env['API_BASE_URL']` → uses it as the base URL for all REST API calls |
| `stomp_client.dart` | `TaskHive\taskhive_flutter\lib\core\network\stomp_client.dart` (Line 29) | `dotenv.env['WS_BASE_URL']` → uses it for WebSocket connections |
| `task_attachments_section.dart` | `TaskHive\taskhive_flutter\lib\features\task\presentation\widgets\task_attachments_section.dart` (Line 93) | `dotenv.get('BASE_URL')` → uses it to build full file download URLs |

### Step-by-Step: Create the `.env.dev` File

1. Open **File Explorer** and navigate to:
   ```
   TaskHive\taskhive_flutter\assets\
   ```

2. Check if an `env` folder exists. If NOT, create it:
   - Right-click inside the `assets` folder → **New → Folder** → name it `env`

3. **Choose the correct configuration for your device:**

Open **Notepad** and paste **ONE** of the following options based on how you plan to run the app. Do not copy all three!

► **Option A: Running on an Android Emulator** (Recommended)
`10.0.2.2` is a special IP that allows the emulator to connect to your computer's backend.
```env
API_BASE_URL=http://10.0.2.2:8080/api/v1
WS_BASE_URL=ws://10.0.2.2:8080/ws
APP_NAME=TaskHive Dev
APP_ENV=dev
```

► **Option B: Running on a Web Browser or Desktop**
These platforms can reach your computer's backend directly using `localhost`.
```env
API_BASE_URL=http://localhost:8080/api/v1
WS_BASE_URL=ws://localhost:8080/ws
APP_NAME=TaskHive Dev
APP_ENV=dev
```

► **Option C: Running on a Physical Phone** (Must be on same WiFi)
*First, find your PC's IP address (open Command Prompt and type `ipconfig`, look for IPv4 Address like `192.168.1.x`).* Replace `<YOUR-PC-IP>` with that exact number.
```env
API_BASE_URL=http://<YOUR-PC-IP>:8080/api/v1
WS_BASE_URL=ws://<YOUR-PC-IP>:8080/ws
APP_NAME=TaskHive Dev
APP_ENV=dev
```

4. Save the file:
   - Click **File → Save As**
   - Navigate to: `TaskHive\taskhive_flutter\assets\env\`
   - In the **File name** field, type: `.env.dev`
   - Change **Save as type** to **All Files (*.*)**
   - Click **Save**

### Step-by-Step: Create the `.env.prod` File

Repeat the same process. Create a new file with production values:

```env
API_BASE_URL=http://10.0.2.2:8080/api/v1
WS_BASE_URL=ws://10.0.2.2:8080/ws
APP_NAME=TaskHive
APP_ENV=prod

# NOTE: For production deployment, replace the URLs above with your actual server addresses
# Example: API_BASE_URL=https://api.yourdomain.com/api/v1
```

Save as: `.env.prod` in the same `assets\env\` folder.

> [!TIP]
> **For production deployment**: replace the URLs with your actual deployed server addresses (e.g., `https://api.yourdomain.com/api/v1`).

### 📂 Verify the Asset Registration

The `.env` files must be registered in `pubspec.yaml` to be included in the app build.

| File | Full Path |
|---|---|
| `pubspec.yaml` | `TaskHive\taskhive_flutter\pubspec.yaml` |

Check lines 75–78 in this file — they should already contain:

```yaml
flutter:
  uses-material-design: true
  assets:
    - assets/images/
    - assets/env/.env.dev
    - assets/env/.env.prod
```

> [!NOTE]
> If these lines are missing, add them under the `flutter:` section. The indentation (2 spaces) is critical in YAML files.

---

## 7. Install Dependencies & Generate Code

1. Open **Command Prompt**.
2. Navigate to the Flutter app:

```
cd /d "D:\Internship\TaskHive\taskhive_flutter"
```

> ⚠️ Replace `D:\Internship\TaskHive` with **your actual project path** if different.

3. Get all Flutter dependencies:

```
flutter pub get
```

Wait for: `Got dependencies!`

4. **Generate serialization code** (required — the app uses Freezed and JSON serializable):

```
dart run build_runner build --delete-conflicting-outputs
```

Wait for: `Succeeded after X.Xs with X outputs`

> [!IMPORTANT]
> If you skip the code generation step, the app **will not compile**. Many model classes rely on generated `.g.dart` and `.freezed.dart` files.

---

## 8. Run the Flutter App

### Option A: Run on Android Emulator (Recommended)

1. Make sure the **Android Emulator is running** (launched from Android Studio Device Manager).
2. In Command Prompt, from the `taskhive_flutter` directory:

```
flutter run
```

3. Wait for the app to build and launch (first run takes 2-5 minutes).
4. The app should appear on the emulator! ✅

### Option B: Run on Chrome (Web Mode)

```
flutter run -d chrome
```

### Option C: Run on a Physical Android Device

1. Enable **Developer Options** on your phone:
   - Settings → About Phone → tap **Build Number** 7 times.
2. Enable **USB Debugging**:
   - Settings → Developer Options → turn on **USB Debugging**.
3. Connect your phone via USB cable.
4. Run:
```
flutter devices
```
5. You should see your device listed. Then:
```
flutter run
```

---

## 9. Troubleshooting

### ❌ "'flutter' is not recognized"

Flutter is not in your PATH. Fix:
1. Make sure you extracted Flutter to `C:\flutter\flutter\`
2. Press `Win + R` → `sysdm.cpl` → Advanced → Environment Variables → Path → Edit → New → add `C:\flutter\flutter\bin`
3. Close and reopen Command Prompt.

### ❌ "No connected devices"

- Make sure the Android emulator is running.
- Or connect a physical device with USB debugging enabled.
- Run `flutter devices` to see available devices.

### ❌ "Gradle build failed" or "FAILURE: Build failed"

- Make sure **Android Studio** is installed and you've accepted the licenses (`flutter doctor --android-licenses`).
- Try:
```
flutter clean
flutter pub get
dart run build_runner build --delete-conflicting-outputs
flutter run
```

### ❌ "Could not find the generated class..."

Code generation hasn't been run. Execute:
```
dart run build_runner build --delete-conflicting-outputs
```

### ❌ "Connection refused" when app tries to reach backend

- Make sure the backend is running on port 8080 (📂 `TaskHive\taskhive-backend\src\main\resources\application-dev.properties` → `server.port=8080`).
- For **emulator**: use `10.0.2.2` (not `localhost`) in the `.env.dev` file.
- For **physical device**: use your computer's local IP address (find it with `ipconfig` in Command Prompt).
- Check that the `.env.dev` file exists at: 📂 `TaskHive\taskhive_flutter\assets\env\.env.dev`

---

## ✅ What You Should Have After This Step

- [x] Git installed
- [x] Flutter SDK 3.24+ installed with Dart 3.4+
- [x] Android Studio installed with Android SDK
- [x] Android emulator configured and working
- [x] `.env.dev` and `.env.prod` files created
- [x] All Flutter dependencies installed
- [x] Code generation complete
- [x] App running on emulator or device

---

## 📂 Files Summary — What Was Configured in This Step

| File | Full Path | What Was Configured |
|---|---|---|
| `.env.dev` | `TaskHive\taskhive_flutter\assets\env\.env.dev` | API base URL, WebSocket URL, Base URL (created new) |
| `.env.prod` | `TaskHive\taskhive_flutter\assets\env\.env.prod` | Same as above for production (created new) |
| `pubspec.yaml` | `TaskHive\taskhive_flutter\pubspec.yaml` | Verified asset registration (no changes needed) |

---

## ➡️ Next Step

Continue to **[06_REACTNATIVE_SETUP.md](./06_REACTNATIVE_SETUP.md)** to set up the React Native mobile app.

> [!NOTE]
> **React Native** is an **alternative** mobile app. You do NOT need both Flutter and React Native — choose whichever your team prefers.

---

*Document Version: 1.1 | Last Updated: March 2026*
