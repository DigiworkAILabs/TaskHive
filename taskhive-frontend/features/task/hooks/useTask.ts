import { useState, useEffect, useCallback } from 'react';
import { taskService } from '../services/taskService';
import { Task } from '../types/task.types';

export const useTask = (id: string) => {
    const [task, setTask] = useState<Task | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchTask = useCallback(async () => {
        if (!id) return;
        setIsLoading(true);
        setError(null);
        try {
            const data = await taskService.getById(id);
            setTask(data);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load task');
        } finally {
            setIsLoading(false);
        }
    }, [id]);

    useEffect(() => { fetchTask(); }, [fetchTask]);

    return { task, isLoading, error, refetch: fetchTask };
};
