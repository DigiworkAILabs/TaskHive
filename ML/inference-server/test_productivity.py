import requests
import json

def test_productivity_endpoint():
    url = "http://localhost:8000/ml/score/employee"
    
    payload = {
        "employee_id": "550e8400-e29b-41d4-a716-446655440000",
        "period_days": 30,
        "tasks_assigned": 20,
        "tasks_completed": 18,
        "tasks_overdue": 1,
        "on_time_rate": 0.88,
        "avg_completion_hours": 4.5,
        "comment_activity": 45,
        "prev_score": 78.5
    }

    print(f"Testing endpoint: {url}")
    print(f"Payload: {json.dumps(payload, indent=2)}")
    
    try:
        response = requests.post(url, json=payload)
        print(f"Status Code: {response.status_code}")
        if response.status_code == 200:
            print("Response Data:")
            print(json.dumps(response.json(), indent=2))
        else:
            print(f"Error: {response.text}")
    except Exception as e:
        print(f"Request failed: {e}")

if __name__ == "__main__":
    # Ensure the server is running before running this
    test_productivity_endpoint()
