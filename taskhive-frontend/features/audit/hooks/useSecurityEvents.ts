import { useQuery, keepPreviousData } from "@tanstack/react-query";
import { fetchSecurityEvents } from "../services/auditService";

export const SECURITY_EVENTS_KEY = "security-events";

export function useSecurityEvents(page: number) {
    return useQuery({
        queryKey: [SECURITY_EVENTS_KEY, page],
        queryFn: () => fetchSecurityEvents(page),
        placeholderData: keepPreviousData,
        staleTime: 30 * 1000,
    });
}
