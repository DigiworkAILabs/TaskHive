import apiClient from "@/shared/services/api/apiClient";
import {
    AuditLog,
    AuditSearchParams,
    ComplianceReportType,
    Page,
    SecurityEvent,
} from "../types/audit.types";

export const PAGE_SIZE = 15;

/**
 * fetchAuditLogs
 * logic:
 * - Check if any field in params has a truthy value.
 * - If NO fields are truthy -> GET /api/v1/audit/logs
 * - If ANY field is truthy -> GET /api/v1/audit/logs/search
 */
export async function fetchAuditLogs(
    page: number,
    params: AuditSearchParams
): Promise<Page<AuditLog>> {
    const hasParams = Object.values(params).some((val) => !!val);

    const endpoint = hasParams ? "/audit/logs/search" : "/audit/logs";

    const queryParams: Record<string, any> = {
        page,
        size: PAGE_SIZE,
    };

    if (hasParams) {
        if (params.startDate) queryParams.startDate = params.startDate;
        if (params.endDate) queryParams.endDate = params.endDate;
        if (params.action) queryParams.action = params.action;
        if (params.entityType) queryParams.entityType = params.entityType;
        if (params.actorEmail) queryParams.actorEmail = params.actorEmail;
        if (params.ipAddress) queryParams.ipAddress = params.ipAddress;
    }

    const response = await apiClient.get(endpoint, { params: queryParams });
    return response.data.data;
}

export async function fetchSecurityEvents(page: number): Promise<Page<SecurityEvent>> {
    const response = await apiClient.get("/audit/security-events", {
        params: {
            page,
            size: PAGE_SIZE,
        },
    });
    return response.data.data;
}

export async function fetchEntityTimeline(
    entityType: string,
    entityId: string,
    page: number = 0
): Promise<Page<AuditLog>> {
    const response = await apiClient.get(`/audit/logs/entity/${entityType}/${entityId}`, {
        params: { page, size: PAGE_SIZE }
    });
    return response.data.data;
}

export async function fetchComplianceReport(
    reportType: ComplianceReportType,
    startDate: string,
    endDate: string,
    page: number = 0
): Promise<Page<any>> {
    const response = await apiClient.get("/audit/compliance/report", {
        params: {
            reportType,
            startDate,
            endDate,
            format: "json", // Explicitly request JSON format
            page,
            size: PAGE_SIZE,
        },
    });
    return response.data.data;
}
