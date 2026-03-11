import pandas as pd
import numpy as np
import os

def generate_productivity_data(output_path, num_records=3000):
    """
    Generates synthetic productivity data for 3000 employee periods.
    Logic:
    - completion_rate (35%)
    - on_time_rate (30%)
    - overdue_rate penalty (-20%)
    - engagement (comment_activity) (15%)
    """
    np.random.seed(42)
    
    data = []
    for _ in range(num_records):
        # 1. Inputs
        tasks_assigned = np.random.randint(5, 50)
        tasks_completed = np.random.randint(int(tasks_assigned * 0.5), tasks_assigned + 1)
        
        # Derived
        completion_rate = tasks_completed / tasks_assigned
        
        tasks_overdue = np.random.randint(0, int((tasks_assigned - tasks_completed) * 1.5) + 2)
        tasks_overdue = min(tasks_overdue, tasks_assigned)
        overdue_rate = tasks_overdue / tasks_assigned
        
        on_time_tasks = np.random.randint(int(tasks_completed * 0.4), tasks_completed + 1)
        on_time_rate = on_time_tasks / tasks_completed if tasks_completed > 0 else 0
        
        avg_completion_hours = np.random.uniform(2.0, 12.0)
        comment_activity = np.random.randint(0, 100)
        
        # 2. Logic for labels
        # engagement normalized (max at 50 comments)
        engagement = min(comment_activity / 50.0, 1.0)
        
        # Base Score (0-100)
        score = (completion_rate * 35) + (on_time_rate * 30) - (overdue_rate * 20) + (engagement * 15)
        
        # Scaling and noise
        # Note: the max raw score could be 35+30+15 = 80. Min could be negative.
        # We shift and scale to be more "A-F" friendly
        score = score + 20 # shift up
        score = np.clip(score + np.random.normal(0, 3), 0, 100)
        
        data.append({
            "tasks_assigned": tasks_assigned,
            "tasks_completed": tasks_completed,
            "tasks_overdue": tasks_overdue,
            "on_time_rate": round(on_time_rate, 4),
            "avg_completion_hours": round(avg_completion_hours, 2),
            "comment_activity": comment_activity,
            "score": round(score, 2)
        })
    
    df = pd.DataFrame(data)
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    df.to_csv(output_path, index=False)
    print(f"Generated {num_records} productivity records at {output_path}")

if __name__ == "__main__":
    output_file = os.path.join(os.path.dirname(__file__), "..", "data", "productivity_data.csv")
    generate_productivity_data(output_file)
