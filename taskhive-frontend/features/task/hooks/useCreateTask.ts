import { useState } from 'react';
import { taskService } from '../services/taskService';
import { CreateTaskData, Task } from '../types/task.types';
import { useRouter } from 'next/navigation';

export const useCreateTask = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);
    const router = useRouter();

    const createTask = async (data: CreateTaskData): Promise<Task | null> => {
        setIsLoading(true);
        setError(null);
        setSuccess(false);
        try {
            const task = await taskService.create(data);
            setSuccess(true);
            router.push('/admin/tasks');
            return task;
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to create task');
            return null;
        } finally {
            setIsLoading(false);
        }
    };

    return { createTask, isLoading, error, success };
};
