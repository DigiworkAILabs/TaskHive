import { useState, useEffect } from 'react';
import { taskService } from '../services/taskService';
import { TaskListItem } from '../types/task.types';

export const useOverdueTasks = () => {
    const [tasks, setTasks] = useState<TaskListItem[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchOverdue = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await taskService.getOverdue();
            setTasks(data);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load overdue tasks');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => { fetchOverdue(); }, []);

    return { tasks, isLoading, error, refetch: fetchOverdue };
};
