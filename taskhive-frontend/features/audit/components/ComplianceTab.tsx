"use client";

import { useState } from "react";
import {
  ComplianceReportParams,
  useComplianceReport,
} from "../hooks/useComplianceReport";
import { ComplianceReportType } from "../types/audit.types";
import { COMPLIANCE_REPORT_LABELS, formatAuditDate } from "../utils/auditUtils";
import {
  Skeleton,
  SimpleSelect,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "./ui/AuditUI";
import { Button } from "@/features/auth/components/ui/Button";
import { Input } from "@/features/auth/components/ui/Input";
import {
  FileText,
  Download,
  BarChart3,
  CheckCircle2,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import { getActionColor, getSecurityEventColor } from "../utils/auditUtils";
import { Badge } from "./ui/AuditUI";

export default function ComplianceTab() {
  const [reportType, setReportType] = useState<ComplianceReportType | "">("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [page, setPage] = useState(0);
  const [appliedParams, setAppliedParams] = useState<ComplianceReportParams | null>(null);

  const { data, isLoading, isFetching, isError } = useComplianceReport(appliedParams);

  // Sync page state with appliedParams
  const handleGenerate = () => {
    if (reportType && startDate && endDate) {
      setPage(0);
      setAppliedParams({
        reportType: reportType as ComplianceReportType,
        startDate,
        endDate,
        page: 0,
      });
    }
  };

  const handlePageChange = (newPage: number) => {
    setPage(newPage);
    if (appliedParams) {
      setAppliedParams({ ...appliedParams, page: newPage });
    }
  };

  const handleExport = () => {
    if (!appliedParams) return;

    // Use backend CSV export endpoint
    const baseUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api/v1";
    const url = `${baseUrl}/audit/compliance/report?reportType=${appliedParams.reportType}&startDate=${appliedParams.startDate}&endDate=${appliedParams.endDate}&format=csv`;

    // Open in new tab to trigger download
    window.open(url, '_blank');
  };

  const renderReportLoading = () => (
    <div className="space-y-6">
      <Skeleton className="h-32 w-full" />
      <Skeleton className="h-64 w-full" />
    </div>
  );

  return (
    <div className="space-y-8">
      {/* Configuration Bar */}
      <div className="bg-[#1e1e1e] border border-[#2f2f2f] rounded-lg p-6 shadow-sm">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-5 items-end">
          <div className="md:col-span-2">
            <SimpleSelect
              label="Report Configuration"
              value={reportType}
              placeholder="Select report type..."
              options={Object.entries(COMPLIANCE_REPORT_LABELS).map(
                ([key, value]) => ({
                  value: key,
                  label: value.label,
                })
              )}
              onChange={(val) => setReportType(val as ComplianceReportType)}
            />
          </div>
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-[#a3a3a3] ml-1">
              Start Date
            </label>
            <Input
              type="datetime-local"
              value={startDate}
              max={endDate || undefined}
              onChange={(e) => setStartDate(e.target.value)}
              className="bg-[#111111] border-[#2a2a2a] text-white"
            />
          </div>
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-[#a3a3a3] ml-1">
              End Date
            </label>
            <Input
              type="datetime-local"
              value={endDate}
              min={startDate || undefined}
              onChange={(e) => setEndDate(e.target.value)}
              className="bg-[#111111] border-[#2a2a2a] text-white"
            />
          </div>
        </div>

        {reportType && (
          <div className="mt-4 p-3 bg-[#161616] rounded-md border border-[#2a2a2a]/40">
            <p className="text-xs text-[#a3a3a3]">
              <span className="font-bold text-[#f97316]">Report Scope: </span>
              {COMPLIANCE_REPORT_LABELS[reportType as ComplianceReportType].description}
            </p>
          </div>
        )}

        <div className="flex justify-end mt-6">
          <Button
            onClick={handleGenerate}
            disabled={!reportType || !startDate || !endDate}
            className="h-10 px-8 gap-2 bg-[#f97316] text-white hover:bg-orange-600 border-none rounded-lg font-medium transition-all"
          >
            <BarChart3 size={16} />
            Generate Report
          </Button>
        </div>
      </div>

      {/* Report Output */}
      <div className="min-h-[400px]">
        {isLoading ? (
          renderReportLoading()
        ) : !appliedParams ? (
          <div className="flex flex-col items-center justify-center py-24 text-center space-y-4 opacity-30">
            <div className="p-5 rounded-full bg-[#1e1e1e] border border-[#2f2f2f]">
              <FileText size={48} />
            </div>
            <p className="text-sm max-w-sm text-white">
              Configure parameters above to generate a downloadable compliance audit report.
            </p>
          </div>
        ) : isError ? (
          <div className="bg-red-950/20 border border-red-800/30 rounded-lg p-10 text-red-400 text-center space-y-3">
            <p>Error generating report. Check your date range and try again.</p>
            <Button
              variant="outline"
              onClick={handleGenerate}
              className="border-red-900/50 hover:bg-red-900/20 text-red-400"
            >
              Retry
            </Button>
          </div>
        ) : (
          <div className="space-y-6">
            {/* Summary Card */}
            <div className="bg-[#f97316] rounded-xl p-6 text-white shadow-xl shadow-orange-950/20">
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
                <div className="space-y-1">
                  <h4 className="text-xl font-bold flex items-center gap-2">
                    <CheckCircle2 size={24} />
                    Report Ready
                  </h4>
                  <p className="text-orange-100/80 text-sm">
                    {data.totalElements.toLocaleString()} records identified.
                    Showing page {data.page + 1} of {data.totalPages}.
                  </p>
                </div>
                <div className="flex gap-3">
                  <div className="bg-white/10 p-3 rounded-lg border border-white/20 text-center flex-1 md:flex-none md:min-w-[120px]">
                    <div className="text-[10px] uppercase font-bold text-orange-200/60">From</div>
                    <div className="text-sm font-semibold">{appliedParams.startDate.replace('T', ' ')}</div>
                  </div>
                  <div className="bg-white/10 p-3 rounded-lg border border-white/20 text-center flex-1 md:flex-none md:min-w-[120px]">
                    <div className="text-[10px] uppercase font-bold text-orange-200/60">To</div>
                    <div className="text-sm font-semibold">{appliedParams.endDate.replace('T', ' ')}</div>
                  </div>
                </div>
                <Button
                  onClick={handleExport}
                  className="bg-white text-[#f97316] hover:bg-zinc-100 border-none h-12 px-6 gap-2 font-bold shadow-lg"
                >
                  <Download size={18} />
                  Download CSV
                </Button>
              </div>
            </div>

            {/* Preview Table */}
            <div className={`bg-[#1e1e1e] border border-[#2f2f2f] rounded-lg overflow-hidden shadow-sm transition-opacity ${isFetching ? 'opacity-50' : 'opacity-100'}`}>
              <div className="p-4 border-b border-[#2f2f2f] flex items-center justify-between">
                <h5 className="text-xs font-bold uppercase tracking-widest text-[#555]">
                  Data Preview
                </h5>
                <div className="flex items-center gap-4">
                  <div className="flex items-center gap-2">
                    <Button
                      variant="outline"
                      size="icon"
                      className="h-8 w-8 bg-transparent border-[#2f2f2f] text-[#a3a3a3] hover:text-white"
                      disabled={data.page === 0}
                      onClick={() => handlePageChange(data.page - 1)}
                    >
                      <ChevronLeft size={16} />
                    </Button>
                    <span className="text-xs text-[#a3a3a3] font-medium min-w-[60px] text-center">
                      Page {data.page + 1} / {data.totalPages}
                    </span>
                    <Button
                      variant="outline"
                      size="icon"
                      className="h-8 w-8 bg-transparent border-[#2f2f2f] text-[#a3a3a3] hover:text-white"
                      disabled={data.last}
                      onClick={() => handlePageChange(data.page + 1)}
                    >
                      <ChevronRight size={16} />
                    </Button>
                  </div>
                </div>
              </div>
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow className="bg-[#252525]/20 hover:bg-[#252525]/20">
                      {appliedParams.reportType === "SECURITY_INCIDENT" ? (
                        <>
                          <TableHead className="text-[10px]">EVENT TYPE</TableHead>
                          <TableHead className="text-[10px]">SUCCESS</TableHead>
                          <TableHead className="text-[10px]">IP ADDRESS</TableHead>
                          <TableHead className="text-[10px]">TIMESTAMP</TableHead>
                          <TableHead className="text-[10px]">DETAILS</TableHead>
                        </>
                      ) : (
                        <>
                          <TableHead className="text-[10px]">ACTION</TableHead>
                          <TableHead className="text-[10px]">ACTOR</TableHead>
                          <TableHead className="text-[10px]">ENTITY</TableHead>
                          <TableHead className="text-[10px]">ENTITY ID</TableHead>
                          <TableHead className="text-[10px]">TIMESTAMP</TableHead>
                        </>
                      )}
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {data.content.map((item: any) => (
                      <TableRow key={item.id} className="hover:bg-[#252525]/30 group">
                        {appliedParams.reportType === "SECURITY_INCIDENT" ? (
                          <>
                            <TableCell>
                              <Badge className={getSecurityEventColor(item.eventType)}>
                                {item.eventType}
                              </Badge>
                            </TableCell>
                            <TableCell>
                              <Badge className={item.success ? "bg-green-500/10 text-green-500 border-green-500/20" : "bg-red-500/10 text-red-500 border-red-500/20"}>
                                {item.success ? "SUCCESS" : "FAILED"}
                              </Badge>
                            </TableCell>
                            <TableCell className="text-xs text-[#a3a3a3]">{item.ipAddress}</TableCell>
                            <TableCell className="text-xs text-[#a3a3a3]">{formatAuditDate(item.timestamp)}</TableCell>
                            <TableCell className="text-xs text-[#777] italic truncate max-w-xs">{JSON.stringify(item.details)}</TableCell>
                          </>
                        ) : (
                          <>
                            <TableCell>
                              <Badge className={getActionColor(item.action)}>
                                {item.action}
                              </Badge>
                            </TableCell>
                            <TableCell className="text-xs text-[#a3a3a3] font-medium">{item.actorEmail}</TableCell>
                            <TableCell className="text-xs text-[#a3a3a3] uppercase font-bold text-[10px] opacity-70">{item.entityType}</TableCell>
                            <TableCell className="text-xs text-[#555] font-mono">{item.entityId.substring(0, 8)}...</TableCell>
                            <TableCell className="text-xs text-[#a3a3a3]">{formatAuditDate(item.createdAt)}</TableCell>
                          </>
                        )}
                      </TableRow>
                    ))}
                    {data.content.length === 0 && (
                      <TableRow>
                        <TableCell colSpan={5} className="py-12 text-center text-[#555] italic">
                          No records found for the selected criteria.
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
