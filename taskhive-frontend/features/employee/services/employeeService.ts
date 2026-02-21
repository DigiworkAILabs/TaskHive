import apiClient from '@/shared/services/api/apiClient';
import { ENDPOINTS } from '@/shared/services/api/endpoints';
import {
    Employee,
    EmployeeListItem,
    CreateEmployeeData,
    UpdateEmployeeData,
    EmployeeFilters,
    PageResponse,
} from '../types/employee.types';

export const employeeService = {
    // POST /employees — Create employee (ADMIN only)
    create: async (data: CreateEmployeeData): Promise<Employee> => {
        const response = await apiClient.post(ENDPOINTS.EMPLOYEES.BASE, data);
        return response.data.data;
    },

    // GET /employees — List with pagination + filters (ADMIN only)
    list: async (filters: EmployeeFilters): Promise<PageResponse<EmployeeListItem>> => {
        const params: Record<string, string | number> = {
            page: filters.page,
            size: filters.size,
        };
        if (filters.name) params.name = filters.name;
        if (filters.email) params.email = filters.email;
        if (filters.department) params.department = filters.department;
        if (filters.status) params.status = filters.status;
        if (filters.sortBy) params.sortBy = filters.sortBy;
        if (filters.sortDir) params.sortDir = filters.sortDir;

        const response = await apiClient.get(ENDPOINTS.EMPLOYEES.BASE, { params });
        return response.data.data;
    },

    // GET /employees/{id} — Get employee detail
    getById: async (id: string): Promise<Employee> => {
        const response = await apiClient.get(ENDPOINTS.EMPLOYEES.DETAIL(id));
        return response.data.data;
    },

    // PUT /employees/{id} — Update employee (ADMIN only)
    update: async (id: string, data: UpdateEmployeeData): Promise<Employee> => {
        const response = await apiClient.put(ENDPOINTS.EMPLOYEES.DETAIL(id), data);
        return response.data.data;
    },

    // DELETE /employees/{id} — Soft delete (ADMIN only)
    delete: async (id: string): Promise<void> => {
        await apiClient.delete(ENDPOINTS.EMPLOYEES.DETAIL(id));
    },

    // PATCH /employees/{id}/activate — Activate (ADMIN only)
    activate: async (id: string): Promise<Employee> => {
        const response = await apiClient.patch(ENDPOINTS.EMPLOYEES.ACTIVATE(id));
        return response.data.data;
    },

    // PATCH /employees/{id}/deactivate — Deactivate (ADMIN only)
    deactivate: async (id: string): Promise<Employee> => {
        const response = await apiClient.patch(ENDPOINTS.EMPLOYEES.DEACTIVATE(id));
        return response.data.data;
    },

    // POST /employees/{id}/photo — Upload profile photo (max 5MB, JPG/PNG/WebP)
    uploadPhoto: async (id: string, file: File): Promise<string> => {
        const formData = new FormData();
        formData.append('file', file);
        const response = await apiClient.post(ENDPOINTS.EMPLOYEES.UPLOAD_PHOTO(id), formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
        return response.data.data;
    },

    // GET /employees/{id}/photo — Get profile photo URL
    getPhoto: async (id: string): Promise<string> => {
        const response = await apiClient.get(ENDPOINTS.EMPLOYEES.GET_PHOTO(id));
        return response.data.data;
    },

    // GET /employees/search?query= — Search by name, email, department, designation
    search: async (query: string, page: number = 0, size: number = 10): Promise<PageResponse<EmployeeListItem>> => {
        const response = await apiClient.get(ENDPOINTS.EMPLOYEES.SEARCH, {
            params: { query, page, size },
        });
        return response.data.data;
    },
};
