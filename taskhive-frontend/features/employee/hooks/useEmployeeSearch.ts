import { useState, useCallback } from 'react';
import { employeeService } from '../services/employeeService';
import { EmployeeListResponse, EmployeeSearchRequest } from '../types/employee.types';

export function useEmployeeSearch() {
    const [data, setData] = useState<EmployeeListResponse | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const searchEmployees = useCallback(async (params: EmployeeSearchRequest) => {
        setLoading(true);
        try {
            const result = await employeeService.searchEmployees(params);
            setData(result);
            setError(null);
        } catch (err: any) {
            setError(err.message || 'Search failed');
        } finally {
            setLoading(false);
        }
    }, []);

    return { data, searchEmployees, loading, error };
}
