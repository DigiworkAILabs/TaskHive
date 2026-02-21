import { useState } from 'react';
import { employeeService } from '../services/employeeService';
import { UpdateEmployeeRequest } from '../types/employee.types';

export function useUpdateEmployee() {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const updateEmployee = async (id: string, data: UpdateEmployeeRequest) => {
        setLoading(true);
        setError(null);
        try {
            const result = await employeeService.updateEmployee(id, data);
            return result;
        } catch (err: any) {
            setError(err.message || 'Failed to update employee');
            throw err;
        } finally {
            setLoading(false);
        }
    };

    return { updateEmployee, loading, error };
}
