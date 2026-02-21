import { useState } from 'react';
import { employeeService } from '../services/employeeService';
import { CreateEmployeeRequest } from '../types/employee.types';

export function useCreateEmployee() {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const createEmployee = async (data: CreateEmployeeRequest) => {
        setLoading(true);
        setError(null);
        try {
            const result = await employeeService.createEmployee(data);
            return result;
        } catch (err: any) {
            setError(err.message || 'Failed to create employee');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    return { createEmployee, loading, error };
}
