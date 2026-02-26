"use client";

import { useState } from "react";
import { useSecurityEvents } from "../hooks/useSecurityEvents";
import { formatAuditDate, getSecurityEventColor } from "../utils/auditUtils";
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
} from "./ui/AuditUI";
import { Button } from "@/features/auth/components/ui/Button";
import { ShieldAlert, ShieldCheck, User } from "lucide-react";

export default function SecurityEventsTab() {
  const [page, setPage] = useState(0);

  const { data, isLoading, isFetching, isError } = useSecurityEvents(page);

  const renderLoadingState = () => (
    <>
      {[...Array(6)].map((_, i) => (
        <TableRow key={i}>
          {[...Array(5)].map((_, j) => (
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
      <div className="flex items-center justify-between mb-2">
        <h3 className="text-lg font-semibold text-[#e5e5e5] flex items-center gap-2">
          <ShieldAlert size={20} className="text-[#f97316]" />
          Security Events
        </h3>
        {data && !isLoading && (
          <div className="text-xs text-[#a3a3a3]">
            Total Events:{" "}
            <span className="text-[#e5e5e5] font-medium">
              {data.totalElements.toLocaleString()}
            </span>
          </div>
        )}
      </div>

      {/* Error State */}
      {isError && (
        <div className="bg-red-950/30 border border-red-800/50 rounded-lg p-4 text-red-400 text-sm">
          Failed to load security events. Please try again.
        </div>
      )}

      {/* Table Card */}
      <div className="bg-[#1e1e1e] border border-[#2f2f2f] rounded-lg overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="bg-[#252525]/50 hover:bg-[#252525]/50 border-none">
                <TableHead className="w-12">{" "}</TableHead>
                <TableHead>Event Type</TableHead>
                <TableHead>User / Identifier</TableHead>
                <TableHead>IP Address</TableHead>
                <TableHead>Timestamp</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading || isFetching ? (
                renderLoadingState()
              ) : data?.content.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={5}
                    className="text-center py-12 text-[#555] text-sm"
                  >
                    No security events found.
                  </TableCell>
                </TableRow>
              ) : (
                data?.content.map((event) => (
                  <TableRow
                    key={event.id}
                    className="group"
                    style={{
                      borderLeft: `4px solid ${getSecurityEventColor(
                        event.eventType
                      )}`,
                    }}
                  >
                    <TableCell className="w-12 text-center">
                      {event.success ? (
                        <ShieldCheck size={18} className="text-green-500 mx-auto" />
                      ) : (
                        <ShieldAlert
                          size={18}
                          style={{ color: getSecurityEventColor(event.eventType) }}
                          className="mx-auto"
                        />
                      )}
                    </TableCell>
                    <TableCell className="font-semibold text-sm">
                      <span
                        style={{ color: getSecurityEventColor(event.eventType) }}
                      >
                        {event.eventType.replace("_", " ")}
                      </span>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <User size={14} className="text-[#555]" />
                        <span className="text-[#e5e5e5] text-sm">
                          {event.userId || "Anonymous"}
                        </span>
                      </div>
                    </TableCell>
                    <TableCell className="font-mono text-xs text-[#a3a3a3]">
                      {event.ipAddress}
                    </TableCell>
                    <TableCell className="text-[#a3a3a3] text-sm">
                      {formatAuditDate(event.timestamp)}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </div>

      {/* Pagination */}
      {data && data.totalPages > 1 && (
        <div className="flex justify-between items-center mt-2 px-1">
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
