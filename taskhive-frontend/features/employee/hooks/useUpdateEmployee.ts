import { useState } from 'react';
import { employeeService } from '../services/employeeService';
import { UpdateEmployeeData, Employee } from '../types/employee.types';

export const useUpdateEmployee = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);

    const updateEmployee = async (id: string, data: UpdateEmployeeData): Promise<Employee | null> => {
        setIsLoading(true);
        setError(null);
        setSuccess(false);
        try {
            const updated = await employeeService.update(id, data);
            setSuccess(true);
            return updated;
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to update employee');
            return null;
        } finally {
            setIsLoading(false);
        }
    };

    return { updateEmployee, isLoading, error, success };
};
