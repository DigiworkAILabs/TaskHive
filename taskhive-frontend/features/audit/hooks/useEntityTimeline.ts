import { useQuery } from "@tanstack/react-query";
import { fetchEntityTimeline } from "../services/auditService";

export const ENTITY_TIMELINE_KEY = "entity-timeline";

export function useEntityTimeline(
    entityType: string | null,
    entityId: string | null,
    page: number = 0
) {
    return useQuery({
        queryKey: [ENTITY_TIMELINE_KEY, entityType, entityId, page],
        queryFn: () => fetchEntityTimeline(entityType!, entityId!, page),
        enabled: !!(entityType && entityId),
        staleTime: 60 * 1000,
    });
}
