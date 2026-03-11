import requests
import json

url = "http://localhost:8000/ml/predict/completion-time"
payload = {
    "task_title": "Test Task",
    "task_description": "Test Description",
    "priority": "HIGH",
    "emp_on_time_rate": 0.8,
    "emp_avg_hours_high": 5.0,
    "emp_avg_hours_medium": 3.0,
    "emp_active_tasks": 2
}

try:
    response = requests.post(url, json=payload)
    print(f"Status: {response.status_code}")
    print(f"Response: {json.dumps(response.json(), indent=2)}")
except Exception as e:
    print(f"Error: {e}")
