"use client";

import { useState } from "react";
import { useReportExport, useReportDownload } from "../hooks/useReportExport";
import { ReportType } from "../types/analytics.types";
import { FileDown, Loader2, CheckCircle2, Download, FileSpreadsheet } from "lucide-react";

const REPORT_OPTIONS: { value: ReportType; label: string; description: string }[] = [
    {
        value: "TASK_SUMMARY",
        label: "Task Summary",
        description: "Export all tasks with status, priority, assignee, and dates",
    },
    {
        value: "EMPLOYEE_PERFORMANCE",
        label: "Employee Performance",
        description: "Export employee metrics: tasks assigned, completed, on-time rate",
    },
];

export default function ReportExport() {
    const [selectedType, setSelectedType] = useState<ReportType>("TASK_SUMMARY");
    const exportMutation = useReportExport();
    const downloadMutation = useReportDownload();

    const handleExport = () => {
        exportMutation.mutate(selectedType);
    };

    const handleDownload = () => {
        if (exportMutation.data?.reportId) {
            downloadMutation.mutate(exportMutation.data.reportId);
        }
    };

    return (
        <div
            style={{
                backgroundColor: "#161616",
                border: "1px solid #1f1f1f",
                borderRadius: "16px",
                padding: "24px",
            }}
        >
            <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "20px" }}>
                <div
                    style={{
                        width: "36px", height: "36px", borderRadius: "10px",
                        backgroundColor: "rgba(249,115,22,0.12)", display: "flex",
                        alignItems: "center", justifyContent: "center",
                    }}
                >
                    <FileSpreadsheet size={18} color="#f97316" />
                </div>
                <div>
                    <h3 style={{ color: "#ffffff", fontSize: "15px", fontWeight: 600, margin: 0 }}>
                        Export Reports
                    </h3>
                    <p style={{ color: "#71717a", fontSize: "12px", margin: 0 }}>
                        Generate and download CSV reports
                    </p>
                </div>
            </div>

            {/* Report type selector */}
            <div style={{ display: "flex", flexDirection: "column", gap: "10px", marginBottom: "20px" }}>
                {REPORT_OPTIONS.map((option) => (
                    <label
                        key={option.value}
                        style={{
                            display: "flex",
                            alignItems: "flex-start",
                            gap: "12px",
                            padding: "14px 16px",
                            borderRadius: "12px",
                            border: `1px solid ${selectedType === option.value ? "#f97316" : "#2a2a2a"}`,
                            backgroundColor: selectedType === option.value ? "rgba(249,115,22,0.06)" : "#111111",
                            cursor: "pointer",
                            transition: "all 0.15s",
                        }}
                    >
                        <input
                            type="radio"
                            name="reportType"
                            value={option.value}
                            checked={selectedType === option.value}
                            onChange={() => setSelectedType(option.value)}
                            style={{
                                accentColor: "#f97316",
                                marginTop: "2px",
                                width: "16px",
                                height: "16px",
                            }}
                        />
                        <div>
                            <div style={{ color: "#ffffff", fontSize: "13px", fontWeight: 600 }}>
                                {option.label}
                            </div>
                            <div style={{ color: "#71717a", fontSize: "12px", marginTop: "2px" }}>
                                {option.description}
                            </div>
                        </div>
                    </label>
                ))}
            </div>

            {/* Actions */}
            <div style={{ display: "flex", alignItems: "center", gap: "12px", flexWrap: "wrap" }}>
                <button
                    onClick={handleExport}
                    disabled={exportMutation.isPending}
                    style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "8px",
                        padding: "10px 20px",
                        borderRadius: "12px",
                        border: "none",
                        background: "linear-gradient(135deg, #f97316 0%, #ea6c10 100%)",
                        color: "#ffffff",
                        fontSize: "13px",
                        fontWeight: 600,
                        cursor: exportMutation.isPending ? "not-allowed" : "pointer",
                        opacity: exportMutation.isPending ? 0.7 : 1,
                        boxShadow: "0 4px 16px rgba(249,115,22,0.3)",
                        transition: "opacity 0.15s",
                    }}
                >
                    {exportMutation.isPending ? (
                        <Loader2 size={16} className="animate-spin" />
                    ) : (
                        <FileDown size={16} />
                    )}
                    {exportMutation.isPending ? "Generating..." : "Generate Report"}
                </button>

                {/* Download button (appears after successful export) */}
                {exportMutation.isSuccess && exportMutation.data && (
                    <button
                        onClick={handleDownload}
                        disabled={downloadMutation.isPending}
                        style={{
                            display: "flex",
                            alignItems: "center",
                            gap: "8px",
                            padding: "10px 20px",
                            borderRadius: "12px",
                            border: "1px solid #22c55e",
                            background: "rgba(34,197,94,0.1)",
                            color: "#22c55e",
                            fontSize: "13px",
                            fontWeight: 600,
                            cursor: downloadMutation.isPending ? "not-allowed" : "pointer",
                            transition: "all 0.15s",
                        }}
                    >
                        {downloadMutation.isPending ? (
                            <Loader2 size={16} className="animate-spin" />
                        ) : (
                            <Download size={16} />
                        )}
                        Download CSV
                    </button>
                )}
            </div>

            {/* Success message */}
            {exportMutation.isSuccess && (
                <div
                    style={{
                        marginTop: "14px",
                        display: "flex",
                        alignItems: "center",
                        gap: "8px",
                        color: "#22c55e",
                        fontSize: "12px",
                    }}
                >
                    <CheckCircle2 size={14} />
                    Report generated successfully. Click &quot;Download CSV&quot; to save.
                </div>
            )}

            {/* Error message */}
            {exportMutation.isError && (
                <div
                    style={{
                        marginTop: "14px",
                        color: "#ef4444",
                        fontSize: "12px",
                    }}
                >
                    Failed to generate report. Please try again.
                </div>
            )}
        </div>
    );
}
