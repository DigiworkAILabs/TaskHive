// ─── Admin Dashboard ────────────────────────────────────────────────────────

export interface AdminDashboardData {
    totalTasks: number;
    activeTasks: number;
    overdueTasks: number;
    completedTasks: number;
    completionRate: number;
    totalEmployees: number;
    avgCompletionHours: number;
    metricDate: string;
}

// ─── Employee Dashboard ─────────────────────────────────────────────────────

export interface EmployeeDashboardData {
    totalTasks: number;
    todoTasks: number;
    inProgressTasks: number;
    inReviewTasks: number;
    completedTasks: number;
    onTimeCompletionRate: number;
    avgCompletionHours: number;
}

// ─── Task Distribution (pie/donut chart data) ───────────────────────────────

export interface TaskDistribution {
    status: string;
    count: number;
}

// ─── Task Completion Trend (line chart data) ────────────────────────────────

export interface TaskCompletionTrend {
    date: string;
    count: number;
}

// ─── Employee Performance (table data) ──────────────────────────────────────

export interface EmployeePerformance {
    employeeId: string;
    employeeName: string;
    tasksAssigned: number;
    tasksCompleted: number;
    onTimeRate: number;
    avgCompletionHours: number;
}

// ─── Report Export ──────────────────────────────────────────────────────────

export type ReportType = "TASK_SUMMARY" | "EMPLOYEE_PERFORMANCE";

export interface ReportExportResponse {
    reportId: string;
    message: string;
    downloadUrl: string;
}
