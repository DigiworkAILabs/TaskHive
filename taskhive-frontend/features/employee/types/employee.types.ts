// ── Employee Response (full detail from GET /employees/{id}) ─────────────────
export interface Employee {
    id: string;
    userId: string;
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
    department: string;
    designation: string;
    managerId: string | null;
    managerName: string | null;
    joinDate: string;
    photoUrl: string | null;
    status: EmployeeStatus;
    createdAt: string;
    updatedAt: string;
}

// ── Employee List Item (from GET /employees) ────────────────────────────────
export interface EmployeeListItem {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
    department: string;
    designation: string;
    status: EmployeeStatus;
    photoUrl: string | null;
    joinDate: string;
}

// ── Create Employee DTO ─────────────────────────────────────────────────────
export interface CreateEmployeeData {
    firstName: string;
    lastName: string;
    email: string;
    phone?: string;
    department?: string;
    designation?: string;
    joinDate?: string;
    managerId?: string;
}

// ── Update Employee DTO ─────────────────────────────────────────────────────
export interface UpdateEmployeeData {
    firstName?: string;
    lastName?: string;
    phone?: string;
    department?: string;
    designation?: string;
    joinDate?: string;
    managerId?: string;
}

// ── Filters for listing ─────────────────────────────────────────────────────
export interface EmployeeFilters {
    name?: string;
    email?: string;
    department?: string;
    status?: string;
    page: number;
    size: number;
    sortBy?: string;
    sortDir?: 'asc' | 'desc';
}

// ── Paginated response wrapper ──────────────────────────────────────────────
export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

// ── Status enum ─────────────────────────────────────────────────────────────
export type EmployeeStatus = 'ACTIVE' | 'INACTIVE' | 'PENDING';
