import { useState, useEffect, useCallback } from 'react';
import { taskService } from '../services/taskService';
import { TaskListItem, TaskFilters, PageResponse } from '../types/task.types';

export const useTasks = (initialFilters?: Partial<TaskFilters>) => {
    const [tasks, setTasks] = useState<TaskListItem[]>([]);
    const [pagination, setPagination] = useState({ page: 0, size: 10, totalElements: 0, totalPages: 0, last: true });
    const [filters, setFilters] = useState<TaskFilters>({ page: 0, size: 10, ...initialFilters });
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchTasks = useCallback(async (overrideFilters?: TaskFilters) => {
        setIsLoading(true);
        setError(null);
        try {
            const activeFilters = overrideFilters || filters;
            const data: PageResponse<TaskListItem> = await taskService.list(activeFilters);
            setTasks(data.content);
            setPagination({ page: data.page, size: data.size, totalElements: data.totalElements, totalPages: data.totalPages, last: data.last });
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load tasks');
        } finally {
            setIsLoading(false);
        }
    }, [filters]);

    useEffect(() => { fetchTasks(); }, [fetchTasks]);

    const updateFilters = (newFilters: Partial<TaskFilters>) => {
        setFilters((prev) => ({ ...prev, ...newFilters, page: newFilters.page ?? 0 }));
    };

    const goToPage = (page: number) => setFilters((prev) => ({ ...prev, page }));

    return { tasks, pagination, filters, isLoading, error, fetchTasks, updateFilters, goToPage };
};
