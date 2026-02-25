import { useState } from 'react';
import { taskService } from '../services/taskService';
import { UpdateTaskStatusData, Task } from '../types/task.types';

export const useUpdateTaskStatus = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const updateStatus = async (id: string, data: UpdateTaskStatusData): Promise<Task | null> => {
        setIsLoading(true);
        setError(null);
        try {
            const task = await taskService.updateStatus(id, data);
            return task;
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to update status');
            return null;
        } finally {
            setIsLoading(false);
        }
    };

    return { updateStatus, isLoading, error };
};
