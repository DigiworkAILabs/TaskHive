"use client";

import { useState } from "react";
import { useEntityTimeline } from "../hooks/useEntityTimeline";
import { ENTITY_TYPE_OPTIONS } from "../types/audit.types";
import { formatAuditDate, getActionColor } from "../utils/auditUtils";
import { Skeleton, SimpleSelect } from "./ui/AuditUI";
import { Button } from "@/features/auth/components/ui/Button";
import { Input } from "@/features/auth/components/ui/Input";
import {
  History,
  Search,
  User,
  Clock,
  ChevronLeft,
  ChevronRight,
  ArrowRight,
} from "lucide-react";

export default function EntityTimelineTab() {
  const [entityType, setEntityType] = useState("");
  const [entityId, setEntityId] = useState("");
  const [page, setPage] = useState(0);
  const [appliedParams, setAppliedParams] = useState<{
    type: string | null;
    id: string | null;
  }>({ type: null, id: null });

  const { data, isLoading, isFetching, isError } = useEntityTimeline(
    appliedParams.type,
    appliedParams.id,
    page
  );

  const handleSearch = () => {
    if (entityType && entityId) {
      setPage(0);
      setAppliedParams({ type: entityType, id: entityId });
    }
  };

  const handlePageChange = (newPage: number) => {
    setPage(newPage);
  };

  const renderTimelineLoading = () => (
    <div className="space-y-8 pl-4 border-l-2 border-[#2a2a2a] ml-4">
      {[...Array(3)].map((_, i) => (
        <div key={i} className="relative">
          <div className="absolute -left-[25px] top-1 w-4 h-4 rounded-full bg-[#2a2a2a]" />
          <div className="space-y-2">
            <Skeleton className="h-4 w-1/4" />
            <Skeleton className="h-10 w-full" />
          </div>
        </div>
      ))}
    </div>
  );

  return (
    <div className="space-y-8">
      {/* Search Bar */}
      <div className="bg-[#1e1e1e] border border-[#2f2f2f] rounded-lg p-5 shadow-sm">
        <div className="flex flex-col md:flex-row gap-4 items-end">
          <div className="flex-1 w-full">
            <SimpleSelect
              label="Entity Type"
              value={entityType}
              placeholder="Select type..."
              options={ENTITY_TYPE_OPTIONS.map((opt) => ({
                value: opt.value,
                label: opt.label,
              }))}
              onChange={setEntityType}
            />
          </div>
          <div className="flex-[2] w-full flex flex-col gap-1.5">
            <label className="text-xs font-medium text-[#a3a3a3] ml-1">
              Entity ID
            </label>
            <Input
              placeholder="Paste specific UUID..."
              value={entityId}
              onChange={(e) => setEntityId(e.target.value)}
              className="bg-[#111111] border-[#2a2a2a] text-white"
            />
          </div>
          <Button
            onClick={handleSearch}
            disabled={!entityType || !entityId}
            className="h-10 px-6 gap-2 bg-[#f97316] text-white hover:bg-orange-600 border-none disabled:opacity-50"
          >
            <Search size={16} />
            Fetch Timeline
          </Button>
        </div>
        <p className="text-[11px] text-[#555] mt-3 italic">
          Tip: You can find Entity IDs in the "Audit Logs" tab for specific
          objects.
        </p>
      </div>

      {/* Timeline Content */}
      <div className="relative min-h-[300px]">
        {isLoading ? (
          renderTimelineLoading()
        ) : !appliedParams.id ? (
          <div className="flex flex-col items-center justify-center py-20 text-center space-y-3 opacity-40">
            <History size={48} className="text-white" />
            <p className="text-sm max-w-xs text-white">
              Enter an Entity ID and type above to view its full modification
              history.
            </p>
          </div>
        ) : isError ? (
          <div className="bg-red-950/20 border border-red-800/30 rounded-lg p-6 text-red-400 text-center">
            Unable to locate timeline for this entity. Verify the ID and try
            again.
          </div>
        ) : data?.content?.length === 0 ? (
          <div className="text-center py-20 text-[#555]">
            No movement found for this entity identifier.
          </div>
        ) : (
          <div className="space-y-6">
            {/* Pagination Top */}
            {data && data.totalPages > 1 && (
              <div className="flex items-center justify-between px-4 pb-4">
                <span className="text-xs text-[#555] font-bold uppercase tracking-wider">
                  Timeline History ({data.totalElements} events)
                </span>
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
            )}

            <div className={`space-y-0 pl-10 border-l-2 border-[#2a2a2a] ml-4 transition-opacity ${isFetching ? 'opacity-50' : 'opacity-100'}`}>
              {data?.content.map((log) => (
                <div key={log.id} className="relative pb-10 last:pb-0">
                  {/* Node Dot */}
                  <div
                    className="absolute -left-[51px] top-1 w-5 h-5 rounded-full border-4 border-[#121212] z-10 shadow-lg shadow-black/40"
                    style={{ backgroundColor: getActionColor(log.action) }}
                  />

                  <div className="bg-[#1e1e1e] border border-[#2f2f2f] rounded-xl p-5 shadow-lg relative transition-transform hover:scale-[1.01] duration-200">
                    {/* Connector triangle */}
                    <div className="absolute -left-[9px] top-2 w-4 h-4 bg-[#1e1e1e] border-l border-t border-[#2f2f2f] rotate-[-45deg]" />

                    <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                      <div className="space-y-4 flex-1">
                        <div className="flex items-center gap-3">
                          <span
                            className="text-xs font-bold px-2 py-0.5 rounded-md uppercase tracking-tight"
                            style={{
                              backgroundColor: `${getActionColor(log.action)}20`,
                              color: getActionColor(log.action),
                            }}
                          >
                            {log.action.replace(/_/g, " ")}
                          </span>
                          <div className="flex items-center gap-1.5 text-[#a3a3a3] text-xs">
                            <Clock size={12} />
                            {formatAuditDate(log.createdAt)}
                          </div>
                        </div>

                        <div className="flex items-center gap-4 text-sm bg-[#161616] p-3 rounded-lg border border-[#2a2a2a]/50">
                          <div className="flex items-center gap-2 group">
                            <div className="w-8 h-8 rounded-full bg-[#2a2a2a] flex items-center justify-center text-[#f97316]">
                              <User size={16} />
                            </div>
                            <div>
                              <div className="text-[10px] text-[#555] font-bold uppercase">
                                Modified By
                              </div>
                              <div className="text-[#e5e5e5] font-medium leading-tight">
                                {log.actorEmail || <span className="text-[#555] italic text-xs">System</span>}
                              </div>
                            </div>
                          </div>
                          <div className="h-8 w-px bg-[#2a2a2a]" />
                          <div className="flex-1">
                            <div className="text-[10px] text-[#555] font-bold uppercase">
                              IP Address
                            </div>
                            <div className="text-[#a3a3a3] text-xs font-mono">
                              {log.ipAddress || "—"}
                            </div>
                          </div>
                        </div>

                        {/* Summary of changes if available */}
                        {log.afterState && (
                          <div className="pt-2 text-xs text-[#a3a3a3] flex flex-wrap gap-2">
                            {Object.keys(log.afterState).slice(0, 4).map((key) => (
                              <span
                                key={key}
                                className="bg-[#252525] border border-[#2f2f2f] px-2 py-1 rounded"
                              >
                                {key}
                              </span>
                            ))}
                            {Object.keys(log.afterState).length > 4 && (
                              <span className="text-[#555] pt-1">
                                +{Object.keys(log.afterState).length - 4} more
                              </span>
                            )}
                          </div>
                        )}
                      </div>

                      <div className="flex items-center justify-center md:pl-6 opacity-20">
                        <ArrowRight size={20} className="text-white" />
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Pagination Bottom */}
            {data && data.totalPages > 1 && (
              <div className="flex justify-center pt-8">
                <div className="flex items-center gap-2 bg-[#1e1e1e] p-1 rounded-lg border border-[#2f2f2f]">
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-[#a3a3a3] hover:text-white"
                    disabled={data.page === 0}
                    onClick={() => handlePageChange(data.page - 1)}
                  >
                    <ChevronLeft size={16} className="mr-1" />
                    Previous
                  </Button>
                  <div className="h-4 w-px bg-[#2f2f2f]" />
                  <span className="px-4 text-xs text-white font-bold">
                    {data.page + 1} / {data.totalPages}
                  </span>
                  <div className="h-4 w-px bg-[#2f2f2f]" />
                  <Button
                    variant="ghost"
                    size="sm"
                    className="text-[#a3a3a3] hover:text-white"
                    disabled={data.last}
                    onClick={() => handlePageChange(data.page + 1)}
                  >
                    Next
                    <ChevronRight size={16} className="ml-1" />
                  </Button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
