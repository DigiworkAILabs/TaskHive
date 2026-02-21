import { useState, useEffect, useCallback } from 'react';
import { employeeService } from '../services/employeeService';
import { EmployeeListItem, EmployeeFilters, PageResponse } from '../types/employee.types';

export const useEmployees = (initialFilters?: Partial<EmployeeFilters>) => {
    const [employees, setEmployees] = useState<EmployeeListItem[]>([]);
    const [pagination, setPagination] = useState({ page: 0, size: 10, totalElements: 0, totalPages: 0, last: true });
    const [filters, setFilters] = useState<EmployeeFilters>({
        page: 0,
        size: 10,
        ...initialFilters,
    });
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchEmployees = useCallback(async (overrideFilters?: EmployeeFilters) => {
        setIsLoading(true);
        setError(null);
        try {
            const activeFilters = overrideFilters || filters;
            const data: PageResponse<EmployeeListItem> = await employeeService.list(activeFilters);
            setEmployees(data.content);
            setPagination({
                page: data.page,
                size: data.size,
                totalElements: data.totalElements,
                totalPages: data.totalPages,
                last: data.last,
            });
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load employees');
        } finally {
            setIsLoading(false);
        }
    }, [filters]);

    useEffect(() => {
        fetchEmployees();
    }, [fetchEmployees]);

    const updateFilters = (newFilters: Partial<EmployeeFilters>) => {
        setFilters((prev) => ({ ...prev, ...newFilters, page: newFilters.page ?? 0 }));
    };

    const goToPage = (page: number) => {
        setFilters((prev) => ({ ...prev, page }));
    };

    return { employees, pagination, filters, isLoading, error, fetchEmployees, updateFilters, goToPage };
};
