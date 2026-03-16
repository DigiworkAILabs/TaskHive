# 🤖 Step 4 — ML Service Setup (Python 3.14 + FastAPI)

> This guide walks you through installing **Python 3.14**, setting up the Machine Learning inference server, training ML models, and running the prediction API.  
> **Estimated Time:** 20–30 minutes

---

## Table of Contents

1. [Install Python 3.14](#1-install-python-314)
2. [Verify Python Installation](#2-verify-python-installation)
3. [Set Up Virtual Environment](#3-set-up-virtual-environment)
4. [Install ML Dependencies](#4-install-ml-dependencies)
5. [Train the ML Models](#5-train-the-ml-models)
6. [Configure the ML Server](#6-configure-the-ml-server)
7. [Run the ML Server](#7-run-the-ml-server)
8. [Verify the ML Server Is Running](#8-verify-the-ml-server-is-running)
9. [Troubleshooting](#9-troubleshooting)

---

## 1. Install Python 3.14

> [!IMPORTANT]
> Download **Python 3.14** specifically. The ML dependencies (scikit-learn 1.5.0, numpy 1.26.4) are tested with Python 3.14. Other major versions may cause compatibility issues.

**Download Link:**  
👉 [https://www.python.org/downloads/release/python-3140/](https://www.python.org/downloads/release/python-3140/)

### Download Steps

1. Open the link above.
2. Scroll down to the **Files** section.
3. Click **Windows installer (64-bit)** to download.
4. Save the file to your Downloads folder.

### Install Steps

1. **Double-click** the downloaded `.exe` file.
2. ⚠️ **VERY IMPORTANT:** At the bottom of the first screen, **check the box** ✅ **"Add python.exe to PATH"**.
3. Click **"Install Now"** (the recommended option).
4. Wait for installation to complete → Click **Close**.

> [!CAUTION]
> If you **DO NOT** check "Add python.exe to PATH", Python commands won't work in Command Prompt. If you missed this step, uninstall and reinstall Python with the box checked.

---

## 2. Verify Python Installation

1. **Close any open Command Prompt windows.**
2. Open a **new Command Prompt** (`Win + R` → type `cmd` → Enter).
3. Run:

```
python --version
```

Expected output:
```
Python 3.14.0
```

4. Also verify pip:

```
pip --version
```

Expected output (something like):
```
pip 24.0 from C:\... (python 3.14)
```

If both show correct versions, the installation was successful! ✅

---

## 3. Set Up Virtual Environment

> [!NOTE]
> A **virtual environment** is an isolated Python environment. This prevents conflicts with other Python projects on your computer.

1. Open **Command Prompt**.
2. Navigate to the ML directory:

```
cd /d "D:\Internship\TaskHive\ML"
```

> ⚠️ Replace `D:\Internship\TaskHive` with **your actual project path** if different.

3. Create a virtual environment:

```
python -m venv venv
```

4. **Activate** the virtual environment:

```
venv\Scripts\activate
```

You'll see `(venv)` at the beginning of your command prompt line, like:
```
(venv) D:\Internship\TaskHive\ML>
```

> [!WARNING]
> You must **activate the virtual environment** every time you open a new Command Prompt window to work with the ML service. Run `venv\Scripts\activate` each time.

---

## 4. Install ML Dependencies

There are two sets of dependencies — one for **training** and one for the **inference server**.

### Step 4a: Install Training Dependencies

Make sure the virtual environment is activated (you see `(venv)` in the prompt).

```
pip install -r training\requirements.txt
```

### Step 4b: Install Inference Server Dependencies

```
pip install -r inference-server\requirements.txt
```

Both commands will download and install the required packages. Wait for each to complete.

### Installed Packages Summary

| Package | Version | Purpose |
|---|---|---|
| fastapi | 0.111.0 | Web API framework for ML server |
| uvicorn | 0.30.1 | ASGI server to run FastAPI |
| scikit-learn | 1.5.0 | Machine learning library |
| numpy | 1.26.4 | Numerical computing |
| pandas | 2.2.2 | Data manipulation |
| pydantic | 2.7.0 | Data validation |
| joblib | 1.4.2 | Model serialization |

---

## 5. Train the ML Models

> [!IMPORTANT]
> The ML server needs **trained model files** (`.pkl` files) to make predictions. You must run the training pipeline to generate these models.

The project includes 4 ML models:

| Model | File | Purpose |
|---|---|---|
| Priority Predictor | `priority_model.pkl` | Suggests task priority (LOW/MEDIUM/HIGH/CRITICAL) |
| Completion Time Estimator | `completion_model.pkl` | Predicts estimated task completion time |
| Workload Balancer | `workload_model.pkl` | Recommends optimal task distribution |
| Productivity Scorer | `productivity_model.pkl` | Scores employee productivity |

### Run the Training Pipeline

1. Make sure you're in the `ML` directory with the virtual environment activated.
2. Run the full pipeline:

```
python pipeline\run_pipeline.py
```

This will:
1. **Generate synthetic training data** (4 data generators)
2. **Train all 4 models** (4 training scripts)

Wait for the output:
```
✅ All models trained successfully!
```

3. Verify the model files were created:

```
dir inference-server\artifacts
```

You should see 4 `.pkl` files:
```
completion_model.pkl
priority_model.pkl
productivity_model.pkl
workload_model.pkl
```

---

## 6. Configure the ML Server

### Create the `.env` File

1. Navigate to the inference server directory:

```
cd inference-server
```

2. Copy the example configuration:

```
copy .env.example .env
```

3. Open the `.env` file in Notepad:

```
notepad .env
```

4. The default content should be:

```env
APP_NAME=TaskHive ML Inference Server
APP_VERSION=1.0.0
HOST=0.0.0.0
PORT=8000
DEBUG=false
MODEL_DIR=./models
LOG_LEVEL=INFO
```

> [!NOTE]
> The default values work fine for local development. You don't need to change anything unless you want to use a different port.

5. Save and close the file.

---

## 7. Run the ML Server

1. Make sure you're in the `ML\inference-server` directory with the virtual environment activated.
2. Start the server:

```
python main.py
```

Or alternatively:

```
uvicorn main:app --reload --port 8000
```

You should see output like:
```
[Startup] Loading ML models …
[Loader] Loaded model: priority (from priority_model.pkl)
[Loader] Loaded model: completion (from completion_model.pkl)
[Loader] Loaded model: workload (from workload_model.pkl)
[Loader] Loaded model: productivity (from productivity_model.pkl)
[Startup] ML server ready.
INFO:     Uvicorn running on http://0.0.0.0:8000 (Press CTRL+C to quit)
```

---

## 8. Verify the ML Server Is Running

### Check Health Endpoint

Open your browser and go to: **[http://localhost:8000/health](http://localhost:8000/health)**

You should see:
```json
{"status": "healthy", "models_loaded": ["priority", "completion", "workload", "productivity"]}
```

### Check API Documentation

Go to: **[http://localhost:8000/docs](http://localhost:8000/docs)**

You should see the **FastAPI Swagger UI** with all available ML endpoints:
- `POST /ml/predict/task-priority`
- `POST /ml/predict/completion-time`
- `POST /ml/recommend/workload-balance`
- `POST /ml/score/employee`

If both pages load correctly, the ML server is working! ✅

> [!TIP]
> The Spring Boot backend automatically connects to the ML server at `http://localhost:8000`. Make sure **both** the backend (port 8080) and the ML server (port 8000) are running for full functionality.

---

## 9. Troubleshooting

### ❌ "'python' is not recognized as an internal or external command"

Python was not added to PATH during installation. Fix:
1. Uninstall Python (Settings → Apps → Python 3.14 → Uninstall).
2. Re-download and reinstall Python.
3. ⚠️ **This time, check the box "Add python.exe to PATH"** on the first installer screen.
4. Close and reopen Command Prompt.

### ❌ "ModuleNotFoundError: No module named 'fastapi'"

Your virtual environment is not activated. Run:
```
cd /d "D:\Internship\TaskHive\ML"
venv\Scripts\activate
pip install -r inference-server\requirements.txt
```

### ❌ "FileNotFoundError" when loading models

The model `.pkl` files are missing. Run the training pipeline:
```
cd /d "D:\Internship\TaskHive\ML"
venv\Scripts\activate
python pipeline\run_pipeline.py
```

### ❌ "Address already in use" on port 8000

Another application is using port 8000. Either:
- Find and stop it:
```
netstat -ano | findstr :8000
taskkill /PID <PID_NUMBER> /F
```
- Or change the port in `.env` to `8001` and update `ml.service.url` in the backend's `application-dev.properties` to `http://localhost:8001`.

### ❌ "ERROR: Could not install packages" during pip install

Try upgrading pip first:
```
python -m pip install --upgrade pip
```
Then retry the `pip install -r requirements.txt` command.

---

## ✅ What You Should Have After This Step

- [x] Python 3.14 installed with pip
- [x] Virtual environment created and activated
- [x] All ML dependencies installed
- [x] 4 ML models trained and saved as `.pkl` files
- [x] ML inference server running on `http://localhost:8000`
- [x] Health check and API docs accessible

---

## ➡️ Next Step

Continue to **[05_FLUTTER_SETUP.md](./05_FLUTTER_SETUP.md)** to set up the Flutter mobile app.

> [!NOTE]
> **Steps 5, 6, and 7 are mobile apps.** If you only need the web version, you can stop here! The web app (Steps 1-4) is fully functional on its own.

---

*Document Version: 1.0 | Last Updated: March 2026*
