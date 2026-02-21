// Employee Types matching SRS v2.3 and Backend DTOs

export type EmployeeStatus = 'PENDING' | 'ACTIVE' | 'INACTIVE' | 'DELETED';

export interface Employee {
    id: string;
    userId?: string;
    firstName: string;
    lastName: string;
    email: string;
    phone?: string;
    department: string;
    designation: string;
    managerId?: string;
    managerName?: string;
    joinDate: string;
    photoUrl?: string;
    status: EmployeeStatus;
    version: number;
    createdAt: string;
    updatedAt: string;
    createdBy?: string;
    updatedBy?: string;
    isDeleted: boolean;
}

export interface CreateEmployeeRequest {
    firstName: string;
    lastName: string;
    email: string;
    phone?: string;
    department: string;
    designation: string;
    managerId?: string;
    joinDate: string;
}

export interface UpdateEmployeeRequest {
    firstName?: string;
    lastName?: string;
    phone?: string;
    department?: string;
    designation?: string;
    managerId?: string;
    joinDate?: string;
}

export interface EmployeeListResponse {
    content: Employee[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}

export interface EmployeeSearchRequest {
    query?: string;
    department?: string;
    designation?: string;
    status?: EmployeeStatus;
    page?: number;
    size?: number;
}
