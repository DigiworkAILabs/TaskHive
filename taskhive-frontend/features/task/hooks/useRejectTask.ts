'use client';

import { useState } from 'react';
import { taskService } from '../services/taskService';
import { Task } from '../types/task.types';

export const useRejectTask = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const rejectTask = async (id: string, reason: string): Promise<Task | null> => {
        setIsLoading(true);
        setError(null);
        try {
            const task = await taskService.rejectTask(id, reason);
            return task;
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to reject task');
            return null;
        } finally {
            setIsLoading(false);
        }
    };

    return { rejectTask, isLoading, error };
};
