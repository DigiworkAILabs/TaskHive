import apiClient from '@/shared/services/api/apiClient';
import { ENDPOINTS } from '@/shared/services/api/endpoints';
import {
    Employee,
    CreateEmployeeRequest,
    UpdateEmployeeRequest,
    EmployeeListResponse,
    EmployeeSearchRequest
} from '../types/employee.types';

export const employeeService = {
    getEmployees: async (params?: { page?: number; size?: number }) => {
        const response = await apiClient.get<EmployeeListResponse>(ENDPOINTS.EMPLOYEE.LIST, { params });
        return response.data;
    },

    getEmployee: async (id: string) => {
        const response = await apiClient.get<Employee>(ENDPOINTS.EMPLOYEE.DETAIL(id));
        return response.data;
    },

    createEmployee: async (data: CreateEmployeeRequest) => {
        const response = await apiClient.post<Employee>(ENDPOINTS.EMPLOYEE.CREATE, data);
        return response.data;
    },

    updateEmployee: async (id: string, data: UpdateEmployeeRequest) => {
        const response = await apiClient.put<Employee>(ENDPOINTS.EMPLOYEE.UPDATE(id), data);
        return response.data;
    },

    deleteEmployee: async (id: string) => {
        await apiClient.delete(ENDPOINTS.EMPLOYEE.DELETE(id));
    },

    activateEmployee: async (id: string) => {
        const response = await apiClient.patch<Employee>(ENDPOINTS.EMPLOYEE.ACTIVATE(id));
        return response.data;
    },

    deactivateEmployee: async (id: string) => {
        const response = await apiClient.patch<Employee>(ENDPOINTS.EMPLOYEE.DEACTIVATE(id));
        return response.data;
    },

    searchEmployees: async (params: EmployeeSearchRequest) => {
        const response = await apiClient.get<EmployeeListResponse>(ENDPOINTS.EMPLOYEE.SEARCH, { params });
        return response.data;
    },

    uploadPhoto: async (id: string, file: File) => {
        const formData = new FormData();
        formData.append('file', file);
        const response = await apiClient.post<string>(ENDPOINTS.EMPLOYEE.PHOTO(id), formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
        return response.data;
    }
};
