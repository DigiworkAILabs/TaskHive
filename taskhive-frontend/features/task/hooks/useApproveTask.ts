'use client';

import { useState } from 'react';
import { taskService } from '../services/taskService';
import { Task } from '../types/task.types';

export const useApproveTask = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const approveTask = async (id: string): Promise<Task | null> => {
        setIsLoading(true);
        setError(null);
        try {
            const task = await taskService.approveTask(id);
            return task;
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to approve task');
            return null;
        } finally {
            setIsLoading(false);
        }
    };

    return { approveTask, isLoading, error };
};
