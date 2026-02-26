import { useQuery, keepPreviousData } from "@tanstack/react-query";
import { fetchAuditLogs } from "../services/auditService";
import { AuditSearchParams } from "../types/audit.types";

export const AUDIT_LOGS_KEY = "audit-logs";

export function useAuditLogs(page: number, searchParams: AuditSearchParams) {
    return useQuery({
        queryKey: [AUDIT_LOGS_KEY, page, searchParams],
        queryFn: () => fetchAuditLogs(page, searchParams),
        placeholderData: keepPreviousData,
        staleTime: 30 * 1000,
    });
}
