import { useState } from 'react';
import { taskService } from '../services/taskService';
import { UpdateTaskData, Task } from '../types/task.types';

export const useUpdateTask = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);

    const updateTask = async (id: string, data: UpdateTaskData): Promise<Task | null> => {
        setIsLoading(true);
        setError(null);
        setSuccess(false);
        try {
            const task = await taskService.update(id, data);
            setSuccess(true);
            return task;
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to update task');
            return null;
        } finally {
            setIsLoading(false);
        }
    };

    return { updateTask, isLoading, error, success };
};
