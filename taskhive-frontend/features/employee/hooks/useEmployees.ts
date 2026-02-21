import { useState, useEffect, useCallback } from 'react';
import { employeeService } from '../services/employeeService';
import { Employee, EmployeeListResponse } from '../types/employee.types';
import { MOCK_EMPLOYEES } from '@/shared/utils/mockData';

export function useEmployees(page = 0, size = 10) {
    const [data, setData] = useState<EmployeeListResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchEmployees = useCallback(async () => {
        setLoading(true);
        try {
            // Forcing mock data for UI review even if API succeeds
            // const result = await employeeService.getEmployees({ page, size });
            setData(MOCK_EMPLOYEES);
            setError(null);
        } catch (err: any) {
            console.warn('Employee API failed, using mock data:', err.message);
            setData(MOCK_EMPLOYEES);
            setError(null);
        } finally {
            setLoading(false);
        }
    }, [page, size]);

    useEffect(() => {
        fetchEmployees();
    }, [fetchEmployees]);

    return { data, loading, error, refetch: fetchEmployees };
}
