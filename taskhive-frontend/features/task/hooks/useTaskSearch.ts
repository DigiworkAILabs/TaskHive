import { useState, useCallback } from 'react';
import { taskService } from '../services/taskService';
import { TaskListItem, PageResponse } from '../types/task.types';

export const useTaskSearch = () => {
    const [results, setResults] = useState<TaskListItem[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [query, setQuery] = useState('');

    const search = useCallback(async (q: string) => {
        setQuery(q);
        if (!q.trim()) { setResults([]); return; }
        setIsLoading(true);
        setError(null);
        try {
            const data: PageResponse<TaskListItem> = await taskService.search(q);
            setResults(data.content);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Search failed');
        } finally {
            setIsLoading(false);
        }
    }, []);

    return { results, isLoading, error, query, search };
};
