import { useState, useEffect, useCallback } from 'react';
import { employeeService } from '../services/employeeService';
import { Employee } from '../types/employee.types';

export function useEmployee(id: string) {
    const [data, setData] = useState<Employee | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchEmployee = useCallback(async () => {
        if (!id) return;
        setLoading(true);
        try {
            const result = await employeeService.getEmployee(id);
            setData(result);
            setError(null);
        } catch (err: any) {
            setError(err.message || 'Failed to fetch employee details');
        } finally {
            setLoading(false);
        }
    }, [id]);

    useEffect(() => {
        fetchEmployee();
    }, [fetchEmployee]);

    return { data, loading, error, refetch: fetchEmployee };
}
