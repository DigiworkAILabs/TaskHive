"use client";

import { useState } from "react";
import AuditLogsTab from "@/features/audit/components/AuditLogsTab";
import SecurityEventsTab from "@/features/audit/components/SecurityEventsTab";
import EntityTimelineTab from "@/features/audit/components/EntityTimelineTab";
import ComplianceTab from "@/features/audit/components/ComplianceTab";
import { 
  ClipboardList, 
  ShieldAlert, 
  History, 
  FileText, 
  Activity 
} from "lucide-react";
import { cn } from "@/shared/utils/cn";

type TabType = "logs" | "security" | "timeline" | "compliance";

export default function AuditPage() {
  const [activeTab, setActiveTab] = useState<TabType>("logs");

  const tabs = [
    { id: "logs", label: "Audit Logs", icon: ClipboardList },
    { id: "security", label: "Security Events", icon: ShieldAlert },
    { id: "timeline", label: "Entity Timeline", icon: History },
    { id: "compliance", label: "Compliance", icon: FileText },
  ] as const;

  return (
    <div className="min-h-screen bg-[#121212] text-white p-3 sm:p-6 space-y-6 sm:space-y-8">
      {/* Header */}
      <div className="flex flex-col gap-2">
        <div className="flex items-center gap-3">
          <div className="p-2 sm:p-2.5 bg-orange-500/10 rounded-xl border border-orange-500/20">
            <Activity className="text-[#f97316]" size={24} />
          </div>
          <h1 className="text-xl sm:text-3xl font-extrabold tracking-tight">
            Audit <span className="text-[#f97316]">&amp; Compliance</span>
          </h1>
        </div>
        <p className="text-[#a3a3a3] text-xs sm:text-sm max-w-2xl ml-1">
          Monitor system activity, track security incidents, and generate compliance reports.
        </p>
      </div>

      {/* Tabs Navigation */}
      <div className="flex flex-col space-y-6">
        <div className="overflow-x-auto -mx-3 sm:mx-0 px-3 sm:px-0" style={{ scrollbarWidth: 'none' }}>
          <div className="flex items-center gap-1 bg-[#1e1e1e] p-1.5 rounded-2xl border border-[#2f2f2f] w-max sm:w-auto shadow-xl">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              const isActive = activeTab === tab.id;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id as TabType)}
                  className={cn(
                    "flex items-center gap-2 px-3 sm:px-6 py-2.5 sm:py-3 rounded-xl text-xs sm:text-sm font-bold transition-all duration-300 whitespace-nowrap",
                    isActive
                      ? "bg-[#f97316] text-white shadow-lg shadow-orange-950/40 translate-y-[-1px]"
                      : "text-[#a3a3a3] hover:text-white hover:bg-white/5"
                  )}
                >
                  <Icon size={16} />
                  <span className="hidden sm:inline">{tab.label}</span>
                  <span className="sm:hidden">{tab.label.split(' ')[0]}</span>
                </button>
              );
            })}
          </div>
        </div>

        {/* Tab Content */}
        <div className="bg-[#121212] transition-opacity duration-300">
          {activeTab === "logs" && <AuditLogsTab />}
          {activeTab === "security" && <SecurityEventsTab />}
          {activeTab === "timeline" && <EntityTimelineTab />}
          {activeTab === "compliance" && <ComplianceTab />}
        </div>
      </div>
    </div>
  );
}
