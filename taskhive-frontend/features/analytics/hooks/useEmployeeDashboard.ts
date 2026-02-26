import { useQuery } from "@tanstack/react-query";
import { fetchEmployeeDashboard } from "../services/analyticsService";

export const EMPLOYEE_DASHBOARD_KEY = "employee-dashboard";

export function useEmployeeDashboard() {
    return useQuery({
        queryKey: [EMPLOYEE_DASHBOARD_KEY],
        queryFn: fetchEmployeeDashboard,
        staleTime: 60 * 1000,
    });
}
