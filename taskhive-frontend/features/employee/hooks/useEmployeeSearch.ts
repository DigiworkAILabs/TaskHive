import { useState, useEffect, useCallback } from 'react';
import { employeeService } from '../services/employeeService';
import { EmployeeListItem, PageResponse } from '../types/employee.types';

export const useEmployeeSearch = (debounceMs: number = 400) => {
    const [query, setQuery] = useState('');
    const [results, setResults] = useState<EmployeeListItem[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const search = useCallback(async (searchQuery: string) => {
        if (!searchQuery.trim()) {
            setResults([]);
            return;
        }
        setIsLoading(true);
        setError(null);
        try {
            const data: PageResponse<EmployeeListItem> = await employeeService.search(searchQuery);
            setResults(data.content);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Search failed');
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        const timer = setTimeout(() => {
            search(query);
        }, debounceMs);
        return () => clearTimeout(timer);
    }, [query, debounceMs, search]);

    return { query, setQuery, results, isLoading, error };
};
