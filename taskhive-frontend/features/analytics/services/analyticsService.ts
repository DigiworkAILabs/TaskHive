import apiClient from "@/shared/services/api/apiClient";
import { ENDPOINTS } from "@/shared/services/api/endpoints";
import {
    AdminDashboardData,
    EmployeeDashboardData,
    EmployeePerformance,
    ReportExportResponse,
    ReportType,
    TaskCompletionTrend,
    TaskDistribution,
} from "../types/analytics.types";

// ─── Admin Dashboard ────────────────────────────────────────────────────────

export async function fetchAdminDashboard(): Promise<AdminDashboardData> {
    const response = await apiClient.get(ENDPOINTS.ANALYTICS.DASHBOARD_ADMIN);
    return response.data.data;
}

// ─── Employee Dashboard ─────────────────────────────────────────────────────

export async function fetchEmployeeDashboard(): Promise<EmployeeDashboardData> {
    const response = await apiClient.get(ENDPOINTS.ANALYTICS.DASHBOARD_EMPLOYEE);
    return response.data.data;
}

// ─── Task Distribution (by status) ──────────────────────────────────────────

export async function fetchTaskDistribution(): Promise<TaskDistribution[]> {
    const response = await apiClient.get(ENDPOINTS.ANALYTICS.TASK_DISTRIBUTION);
    return response.data.data;
}

// ─── Task Distribution (by priority) ────────────────────────────────────────

export async function fetchTaskByPriority(): Promise<TaskDistribution[]> {
    const response = await apiClient.get(ENDPOINTS.ANALYTICS.TASK_BY_PRIORITY);
    return response.data.data;
}

// ─── Completion Trend (30-day) ──────────────────────────────────────────────

export async function fetchCompletionTrend(): Promise<TaskCompletionTrend[]> {
    const response = await apiClient.get(ENDPOINTS.ANALYTICS.COMPLETION_TREND);
    return response.data.data;
}

// ─── Employee Performance ───────────────────────────────────────────────────

export async function fetchEmployeePerformance(): Promise<EmployeePerformance[]> {
    const response = await apiClient.get(ENDPOINTS.ANALYTICS.EMPLOYEE_PERFORMANCE);
    return response.data.data;
}

// ─── Report Export ──────────────────────────────────────────────────────────

export async function exportReport(reportType: ReportType): Promise<ReportExportResponse> {
    const response = await apiClient.post(ENDPOINTS.ANALYTICS.REPORT_EXPORT, { reportType });
    return response.data.data;
}

// ─── Report Download ────────────────────────────────────────────────────────

export async function downloadReport(reportId: string): Promise<void> {
    const response = await apiClient.get(ENDPOINTS.ANALYTICS.REPORT_DOWNLOAD(reportId), {
        responseType: "blob",
    });

    // Trigger browser download
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement("a");
    link.href = url;

    // Extract filename from Content-Disposition header or use fallback
    const disposition = response.headers["content-disposition"];
    const filename = disposition
        ? disposition.split("filename=")[1]?.replace(/"/g, "")
        : `report_${reportId}.csv`;

    link.setAttribute("download", filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
}
