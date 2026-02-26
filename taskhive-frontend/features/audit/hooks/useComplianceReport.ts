import { useQuery } from "@tanstack/react-query";
import { fetchComplianceReport } from "../services/auditService";
import { ComplianceReportType } from "../types/audit.types";

export const COMPLIANCE_REPORT_KEY = "compliance-report";

export interface ComplianceReportParams {
    reportType: ComplianceReportType;
    startDate: string;
    endDate: string;
    page: number;
}

export function useComplianceReport(params: ComplianceReportParams | null) {
    return useQuery({
        queryKey: [COMPLIANCE_REPORT_KEY, params],
        queryFn: () =>
            fetchComplianceReport(
                params!.reportType,
                params!.startDate,
                params!.endDate,
                params!.page
            ),
        enabled: !!params,
        staleTime: 0,
    });
}
