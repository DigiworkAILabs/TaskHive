'use client';

import { EmployeeForm } from '@/features/employee/components/EmployeeForm';
import { useCreateEmployee } from '@/features/employee/hooks/useCreateEmployee';
import { CreateEmployeeData } from '@/features/employee/types/employee.types';

export default function NewEmployeePage() {
    const { createEmployee, isLoading, error, success } = useCreateEmployee();

    const handleSubmit = async (data: CreateEmployeeData | any) => {
        await createEmployee(data as CreateEmployeeData);
    };

    return (
        <EmployeeForm
            mode="create"
            onSubmit={handleSubmit}
            isLoading={isLoading}
            error={error}
            success={success}
        />
    );
}
