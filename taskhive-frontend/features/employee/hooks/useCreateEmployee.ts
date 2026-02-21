import { useState } from 'react';
import { employeeService } from '../services/employeeService';
import { CreateEmployeeData, Employee } from '../types/employee.types';
import { useRouter } from 'next/navigation';

export const useCreateEmployee = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);
    const router = useRouter();

    const createEmployee = async (data: CreateEmployeeData) => {
        setIsLoading(true);
        setError(null);
        setSuccess(false);
        try {
            await employeeService.create(data);
            setSuccess(true);
            router.push('/admin/employees');
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to create employee');
        } finally {
            setIsLoading(false);
        }
    };

    return { createEmployee, isLoading, error, success };
};
