import { useState, useEffect, useCallback } from 'react';
import { taskService } from '../services/taskService';
import { TaskListItem, TaskFilters, PageResponse } from '../types/task.types';

export const useMyTasks = (initialFilters?: Partial<TaskFilters>) => {
    const [tasks, setTasks] = useState<TaskListItem[]>([]);
    const [pagination, setPagination] = useState({ page: 0, size: 20, totalElements: 0, totalPages: 0, last: true });
    const [filters, setFilters] = useState<Partial<TaskFilters>>({ page: 0, size: 20, ...initialFilters });
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchMyTasks = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data: PageResponse<TaskListItem> = await taskService.getMyTasks(filters);
            setTasks(data.content);
            setPagination({ page: data.page, size: data.size, totalElements: data.totalElements, totalPages: data.totalPages, last: data.last });
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load tasks');
        } finally {
            setIsLoading(false);
        }
    }, [filters]);

    useEffect(() => { fetchMyTasks(); }, [fetchMyTasks]);

    const updateFilters = (newFilters: Partial<TaskFilters>) => {
        setFilters((prev) => ({ ...prev, ...newFilters, page: newFilters.page ?? 0 }));
    };

    return { tasks, pagination, filters, isLoading, error, fetchMyTasks, updateFilters };
};
