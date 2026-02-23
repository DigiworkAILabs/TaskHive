import { useState, useEffect } from 'react';
import { employeeService } from '../services/employeeService';
import { Employee } from '../types/employee.types';

export const useEmployee = (id: string) => {
    const [employee, setEmployee] = useState<Employee | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchEmployee = async () => {
        if (!id) return;
        setIsLoading(true);
        setError(null);
        try {
            const data = await employeeService.getById(id);
            setEmployee(data);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load employee');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchEmployee();
    }, [id]);

    return { employee, isLoading, error, refetch: fetchEmployee };
};
