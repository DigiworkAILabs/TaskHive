import { useState } from 'react';
import { employeeService } from '../services/employeeService';

export const useDeleteEmployee = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);

    const deleteEmployee = async (id: string) => {
        setIsLoading(true);
        setError(null);
        setSuccess(false);
        try {
            await employeeService.delete(id);
            setSuccess(true);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to delete employee');
        } finally {
            setIsLoading(false);
        }
    };

    return { deleteEmployee, isLoading, error, success };
};
