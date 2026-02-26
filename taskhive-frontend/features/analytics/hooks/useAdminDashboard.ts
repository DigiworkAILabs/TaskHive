import { useQuery } from "@tanstack/react-query";
import {
    fetchAdminDashboard,
    fetchTaskDistribution,
    fetchTaskByPriority,
    fetchCompletionTrend,
    fetchEmployeePerformance,
} from "../services/analyticsService";

export const ADMIN_DASHBOARD_KEY = "admin-dashboard";
export const TASK_DISTRIBUTION_KEY = "task-distribution";
export const TASK_PRIORITY_KEY = "task-priority";
export const COMPLETION_TREND_KEY = "completion-trend";
export const EMPLOYEE_PERFORMANCE_KEY = "employee-performance";

export function useAdminDashboard() {
    return useQuery({
        queryKey: [ADMIN_DASHBOARD_KEY],
        queryFn: fetchAdminDashboard,
        staleTime: 60 * 1000,
    });
}

export function useTaskDistribution() {
    return useQuery({
        queryKey: [TASK_DISTRIBUTION_KEY],
        queryFn: fetchTaskDistribution,
        staleTime: 60 * 1000,
    });
}

export function useTaskByPriority() {
    return useQuery({
        queryKey: [TASK_PRIORITY_KEY],
        queryFn: fetchTaskByPriority,
        staleTime: 60 * 1000,
    });
}

export function useCompletionTrend() {
    return useQuery({
        queryKey: [COMPLETION_TREND_KEY],
        queryFn: fetchCompletionTrend,
        staleTime: 60 * 1000,
    });
}

export function useEmployeePerformance() {
    return useQuery({
        queryKey: [EMPLOYEE_PERFORMANCE_KEY],
        queryFn: fetchEmployeePerformance,
        staleTime: 60 * 1000,
    });
}
