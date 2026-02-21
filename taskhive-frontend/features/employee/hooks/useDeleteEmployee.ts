import { useState } from 'react';
import { employeeService } from '../services/employeeService';

export function useDeleteEmployee() {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const deleteEmployee = async (id: string) => {
        setLoading(true);
        setError(null);
        try {
            await employeeService.deleteEmployee(id);
        } catch (err: any) {
            setError(err.message || 'Failed to delete employee');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    return { deleteEmployee, loading, error };
}
