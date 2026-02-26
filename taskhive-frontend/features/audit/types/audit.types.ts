export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    page: number; // Postman says "page", not "number"
    size: number;
    first: boolean;
    last: boolean;
}

export interface AuditLog {
    id: string;
    actorId: string | null;
    actorEmail: string;
    action: string;
    entityType: string;
    entityId: string;
    beforeState: Record<string, unknown> | null;
    afterState: Record<string, unknown> | null;
    ipAddress: string;
    userAgent: string;
    createdAt: string; // ISO 8601 string
}

export type SecurityEventType = "LOGIN_FAILED" | "ACCOUNT_LOCKED" | "UNAUTHORIZED_ACCESS";

export interface SecurityEvent {
    id: string;
    eventType: SecurityEventType;
    userId: string | null;
    ipAddress: string;
    success: boolean;
    details: Record<string, unknown> | null;
    timestamp: string; // ISO 8601 string
}

export type AuditAction =
    | "USER_LOGIN"
    | "USER_LOGOUT"
    | "PASSWORD_CHANGED"
    | "PASSWORD_RESET_REQUESTED"
    | "ACCOUNT_ACTIVATED"
    | "EMPLOYEE_CREATED"
    | "EMPLOYEE_UPDATED"
    | "EMPLOYEE_ACTIVATED"
    | "EMPLOYEE_DEACTIVATED"
    | "EMPLOYEE_DELETED"
    | "TASK_CREATED"
    | "TASK_UPDATED"
    | "TASK_STATUS_CHANGED"
    | "TASK_ASSIGNED"
    | "TASK_COMMENT_ADDED";

export type EntityType = "USER" | "EMPLOYEE" | "TASK" | "NOTIFICATION";

export type ComplianceReportType = "USER_ACCESS" | "DATA_MODIFICATION" | "SECURITY_INCIDENT";

export interface AuditSearchParams {
    startDate?: string; // ISO datetime string, optional
    endDate?: string; // ISO datetime string, optional
    action?: string; // optional
    entityType?: string; // optional
    actorEmail?: string; // optional
    ipAddress?: string; // optional
}

export const AUDIT_ACTION_OPTIONS = [
    { value: "USER_LOGIN", label: "User Login" },
    { value: "USER_LOGOUT", label: "User Logout" },
    { value: "PASSWORD_CHANGED", label: "Password Changed" },
    { value: "PASSWORD_RESET_REQUESTED", label: "Password Reset Requested" },
    { value: "ACCOUNT_ACTIVATED", label: "Account Activated" },
    { value: "EMPLOYEE_CREATED", label: "Employee Created" },
    { value: "EMPLOYEE_UPDATED", label: "Employee Updated" },
    { value: "EMPLOYEE_ACTIVATED", label: "Employee Activated" },
    { value: "EMPLOYEE_DEACTIVATED", label: "Employee Deactivated" },
    { value: "EMPLOYEE_DELETED", label: "Employee Deleted" },
    { value: "TASK_CREATED", label: "Task Created" },
    { value: "TASK_UPDATED", label: "Task Updated" },
    { value: "TASK_STATUS_CHANGED", label: "Task Status Changed" },
    { value: "TASK_ASSIGNED", label: "Task Assigned" },
    { value: "TASK_COMMENT_ADDED", label: "Task Comment Added" },
] as const;

export const ENTITY_TYPE_OPTIONS = [
    { value: "USER", label: "User" },
    { value: "EMPLOYEE", label: "Employee" },
    { value: "TASK", label: "Task" },
    { value: "NOTIFICATION", label: "Notification" },
] as const;
