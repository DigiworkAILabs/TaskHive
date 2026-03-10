import requests
import sys

def check_health():
    url = "http://localhost:8000/health"
    try:
        response = requests.get(url, timeout=5)
        if response.status_code == 200:
            data = response.json()
            print("✅ ML Server is healthy!")
            print(f"Status: {data.get('status')}")
            print(f"Models Loaded: {data.get('models_loaded')}")
            print(f"Version: {data.get('version')}")
        else:
            print(f"❌ ML Server returned status code: {response.status_code}")
            sys.exit(1)
    except requests.exceptions.ConnectionError:
        print("❌ ML Server is not reachable. Is it running on port 8000?")
        sys.exit(1)
    except Exception as e:
        print(f"❌ An error occurred: {e}")
        sys.exit(1)

if __name__ == "__main__":
    check_health()
