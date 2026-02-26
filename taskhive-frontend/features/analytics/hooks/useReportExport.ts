import { useMutation } from "@tanstack/react-query";
import { exportReport, downloadReport } from "../services/analyticsService";
import { ReportType, ReportExportResponse } from "../types/analytics.types";

export function useReportExport() {
    return useMutation<ReportExportResponse, Error, ReportType>({
        mutationFn: (reportType) => exportReport(reportType),
    });
}

export function useReportDownload() {
    return useMutation<void, Error, string>({
        mutationFn: (reportId) => downloadReport(reportId),
    });
}
