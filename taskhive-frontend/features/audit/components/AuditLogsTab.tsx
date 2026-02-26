"use client";

import React, { useState } from "react";
import { useAuditLogs } from "../hooks/useAuditLogs";
import {
  AuditSearchParams,
  AUDIT_ACTION_OPTIONS,
  ENTITY_TYPE_OPTIONS,
} from "../types/audit.types";
import {
  formatAuditDate,
  getActionColor,
  truncateUuid,
} from "../utils/auditUtils";
import { PAGE_SIZE } from "../services/auditService";
import {
  Badge,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  SimpleSelect,
} from "./ui/AuditUI";
import { Button } from "@/features/auth/components/ui/Button";
import { Input } from "@/features/auth/components/ui/Input";
import { ChevronDown, ChevronUp, RotateCcw, Search } from "lucide-react";

export default function AuditLogsTab() {
  const [page, setPage] = useState(0);
  const [pendingFilters, setPendingFilters] = useState<AuditSearchParams>({
    startDate: "",
    endDate: "",
    action: "",
    entityType: "",
    actorEmail: "",
    ipAddress: "",
  });
  const [appliedFilters, setAppliedFilters] = useState<AuditSearchParams>({
    startDate: "",
    endDate: "",
    action: "",
    entityType: "",
    actorEmail: "",
    ipAddress: "",
  });
  const [expandedRowId, setExpandedRowId] = useState<string | null>(null);

  const { data, isLoading, isFetching, isError } = useAuditLogs(
    page,
    appliedFilters
  );

  const handleApplyFilters = () => {
    setAppliedFilters(pendingFilters);
    setPage(0);
  };

  const handleClearFilters = () => {
    const emptyFilters = {
      startDate: "",
      endDate: "",
      action: "",
      entityType: "",
      actorEmail: "",
      ipAddress: "",
    };
    setPendingFilters(emptyFilters);
    setAppliedFilters(emptyFilters);
    setPage(0);
  };

  const toggleRow = (id: string) => {
    setExpandedRowId(expandedRowId === id ? null : id);
  };

  const renderLoadingState = () => (
    <>
      {[...Array(8)].map((_, i) => (
        <TableRow key={i}>
          {[...Array(7)].map((_, j) => (
            <TableCell key={j}>
              <Skeleton className="h-4 w-full" />
            </TableCell>
          ))}
        </TableRow>
      ))}
    </>
  );

  return (
    <div className="space-y-6">
      {/* Filter Bar */}
      <div className="bg-[#1e1e1e] border border-[#2f2f2f] rounded-lg p-4 shadow-sm">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-[#a3a3a3] ml-1">
              From Date
            </label>
            <Input
              type="datetime-local"
              value={pendingFilters.startDate || ""}
              max={pendingFilters.endDate || undefined}
              onChange={(e) =>
                setPendingFilters({ ...pendingFilters, startDate: e.target.value })
              }
              className="bg-[#111111] border-[#2a2a2a] text-sm text-white"
            />
          </div>
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-[#a3a3a3] ml-1">
              To Date
            </label>
            <Input
              type="datetime-local"
              value={pendingFilters.endDate || ""}
              min={pendingFilters.startDate || undefined}
              onChange={(e) =>
                setPendingFilters({ ...pendingFilters, endDate: e.target.value })
              }
              className="bg-[#111111] border-[#2a2a2a] text-sm text-white"
            />
          </div>
          <SimpleSelect
            label="Action"
            value={pendingFilters.action || ""}
            placeholder="All Actions"
            options={AUDIT_ACTION_OPTIONS.map((opt) => ({
              value: opt.value,
              label: opt.label,
            }))}
            onChange={(val) =>
              setPendingFilters({ ...pendingFilters, action: val })
            }
          />
          <SimpleSelect
            label="Entity Type"
            value={pendingFilters.entityType || ""}
            placeholder="All Types"
            options={ENTITY_TYPE_OPTIONS.map((opt) => ({
              value: opt.value,
              label: opt.label,
            }))}
            onChange={(val) =>
              setPendingFilters({ ...pendingFilters, entityType: val })
            }
          />
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-[#a3a3a3] ml-1">
              Actor Email
            </label>
            <Input
              type="email"
              placeholder="Filter by email..."
              value={pendingFilters.actorEmail || ""}
              onChange={(e) =>
                setPendingFilters({ ...pendingFilters, actorEmail: e.target.value })
              }
              className="bg-[#111111] border-[#2a2a2a] text-sm"
            />
          </div>
          <div className="flex flex-col gap-1.5">
            <label className="text-xs font-medium text-[#a3a3a3] ml-1">
              IP Address
            </label>
            <Input
              type="text"
              placeholder="Filter by IP..."
              value={pendingFilters.ipAddress || ""}
              onChange={(e) =>
                setPendingFilters({ ...pendingFilters, ipAddress: e.target.value })
              }
              className="bg-[#111111] border-[#2a2a2a] text-sm"
            />
          </div>
        </div>

        <div className="flex justify-end gap-2 mt-4">
          <Button
            variant="outline"
            onClick={handleClearFilters}
            className="h-9 px-4 gap-2 border-[#2a2a2a] hover:bg-[#252525]"
          >
            <RotateCcw size={14} />
            Clear
          </Button>
          <Button
            onClick={handleApplyFilters}
            className="h-9 px-4 gap-2 bg-[#f97316] text-white hover:bg-orange-600 border-none"
          >
            <Search size={14} />
            Apply Filters
          </Button>
        </div>
      </div>

      {/* Record Count */}
      {data && !isLoading && (
        <div className="text-xs text-[#a3a3a3] font-medium">
          Showing {page * PAGE_SIZE + 1}–
          {Math.min((page + 1) * PAGE_SIZE, data.totalElements)} of{" "}
          {data.totalElements.toLocaleString()} logs
        </div>
      )}

      {/* Error State */}
      {isError && (
        <div className="bg-red-950/30 border border-red-800/50 rounded-lg p-4 text-red-400 text-sm">
          Failed to load audit logs. Please try again.
        </div>
      )}

      {/* Table Card */}
      <div className="bg-[#1e1e1e] border border-[#2f2f2f] rounded-lg overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-[#252525]/50 hover:bg-[#252525]/50 border-none">
                <TableHead>Actor Email</TableHead>
                <TableHead>Action</TableHead>
                <TableHead>Entity Type</TableHead>
                <TableHead>Entity ID</TableHead>
                <TableHead>IP Address</TableHead>
                <TableHead>Timestamp</TableHead>
                <TableHead className="w-10">{" "}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading || isFetching ? (
                renderLoadingState()
              ) : data?.content.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center py-12 text-[#555]">
                    No audit logs found for the selected filters.
                  </TableCell>
                </TableRow>
              ) : (
                data?.content.map((log) => (
                  <React.Fragment key={log.id}>
                    <TableRow
                      className="cursor-pointer hover:bg-[#252525] transition-colors"
                      onClick={() => toggleRow(log.id)}
                    >
                      <TableCell className="font-medium text-[#e5e5e5]">
                        {log.actorEmail ? (
                          log.actorEmail
                        ) : (
                          <span className="text-[#555] italic text-xs">System</span>
                        )}
                      </TableCell>
                      <TableCell>
                        <Badge
                          style={{
                            backgroundColor: `${getActionColor(log.action)}26`,
                            color: getActionColor(log.action),
                            borderColor: `${getActionColor(log.action)}4d`,
                          }}
                        >
                          {log.action}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-[#a3a3a3]">
                        {log.entityType}
                      </TableCell>
                      <TableCell className="font-mono text-xs text-[#a3a3a3]">
                        {truncateUuid(log.entityId)}
                      </TableCell>
                      <TableCell className="font-mono text-xs text-[#a3a3a3]">
                        {log.ipAddress || <span className="text-[#555] italic">—</span>}
                      </TableCell>
                      <TableCell className="text-[#a3a3a3]">
                        {formatAuditDate(log.createdAt)}
                      </TableCell>
                      <TableCell>
                        {expandedRowId === log.id ? (
                          <ChevronUp size={16} className="text-[#555]" />
                        ) : (
                          <ChevronDown size={16} className="text-[#555]" />
                        )}
                      </TableCell>
                    </TableRow>

                    {/* Expanded Content */}
                    {expandedRowId === log.id && (
                      <TableRow className="bg-[#161616] hover:bg-[#161616]">
                        <TableCell colSpan={7} className="p-6">
                          <div className="space-y-4">
                            <div className="flex flex-col gap-1">
                              <span className="text-xs font-semibold text-[#f97316] uppercase tracking-wider">
                                Entity ID
                              </span>
                              <span className="font-mono text-sm text-[#e5e5e5]">
                                {log.entityId}
                              </span>
                            </div>

                            <div className="flex flex-col gap-1">
                              <span className="text-xs font-semibold text-[#f97316] uppercase tracking-wider">
                                User Agent
                              </span>
                              <span className="text-xs text-[#a3a3a3] italic">
                                {log.userAgent}
                              </span>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pt-2">
                              <div className="space-y-2">
                                <span className="text-xs font-semibold text-[#555] uppercase tracking-wider">
                                  Before State
                                </span>
                                <pre className="bg-[#141414] border border-[#2f2f2f] rounded p-3 text-[11px] font-mono text-[#a3a3a3] overflow-auto max-h-48 scrollbar-thin scrollbar-thumb-[#2f2f2f]">
                                  {log.beforeState
                                    ? JSON.stringify(log.beforeState, null, 2)
                                    : "—"}
                                </pre>
                              </div>
                              <div className="space-y-2">
                                <span className="text-xs font-semibold text-[#555] uppercase tracking-wider">
                                  After State
                                </span>
                                <pre className="bg-[#141414] border border-[#2f2f2f] rounded p-3 text-[11px] font-mono text-[#a3a3a3] overflow-auto max-h-48 scrollbar-thin scrollbar-thumb-[#2f2f2f]">
                                  {log.afterState
                                    ? JSON.stringify(log.afterState, null, 2)
                                    : "—"}
                                </pre>
                              </div>
                            </div>
                          </div>
                        </TableCell>
                      </TableRow>
                    )}
                  </React.Fragment>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </div>

      {/* Pagination */}
      {data && data.totalPages > 1 && (
        <div className="flex justify-between items-center mt-4 px-1">
          <div className="text-xs text-[#a3a3a3]">
            Page <span className="text-[#e5e5e5] font-medium">{page + 1}</span> of{" "}
            <span className="text-[#e5e5e5] font-medium">{data.totalPages}</span>
          </div>
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={page === 0}
              onClick={() => setPage((p) => p - 1)}
              className="h-8 border-[#2a2a2a] hover:bg-[#252525] disabled:opacity-30"
            >
              Previous
            </Button>
            <Button
              variant="outline"
              size="sm"
              disabled={data.last}
              onClick={() => setPage((p) => p + 1)}
              className="h-8 border-[#2a2a2a] hover:bg-[#252525] disabled:opacity-30"
            >
              Next
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
