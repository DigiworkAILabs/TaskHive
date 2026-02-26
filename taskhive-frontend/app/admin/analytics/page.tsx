"use client";

import AdminDashboard from "@/features/analytics/components/AdminDashboard";
import TaskDistributionChart from "@/features/analytics/components/TaskDistributionChart";
import TaskCompletionTrend from "@/features/analytics/components/TaskCompletionTrend";
import EmployeePerformanceTable from "@/features/analytics/components/EmployeePerformanceTable";
import ReportExport from "@/features/analytics/components/ReportExport";
import { BarChart3 } from "lucide-react";

export default function AnalyticsPage() {
    return (
        <div className="min-h-screen bg-[#121212] text-white p-3 sm:p-6 space-y-6 sm:space-y-8">
            {/* Header */}
            <div className="flex flex-col gap-2">
                <div className="flex items-center gap-3">
                    <div className="p-2 sm:p-2.5 bg-orange-500/10 rounded-xl border border-orange-500/20">
                        <BarChart3 className="text-[#f97316]" size={24} />
                    </div>
                    <h1 className="text-xl sm:text-3xl font-extrabold tracking-tight">
                        Analytics <span className="text-[#f97316]">& Reports</span>
                    </h1>
                </div>
                <p className="text-[#a3a3a3] text-xs sm:text-sm max-w-2xl ml-1">
                    Monitor task performance, employee productivity, and generate exportable reports.
                </p>
            </div>

            {/* Admin Dashboard Stats */}
            <section>
                <h2 style={{ color: "#a1a1aa", fontSize: "12px", fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.5px", marginBottom: "12px" }}>
                    Overview
                </h2>
                <AdminDashboard />
            </section>

            {/* Charts */}
            <section>
                <h2 style={{ color: "#a1a1aa", fontSize: "12px", fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.5px", marginBottom: "12px" }}>
                    Task Distribution
                </h2>
                <TaskDistributionChart />
            </section>

            {/* Completion Trend */}
            <section>
                <h2 style={{ color: "#a1a1aa", fontSize: "12px", fontWeight: 700, textTransform: "uppercase", letterSpacing: "0.5px", marginBottom: "12px" }}>
                    Completion Trend
                </h2>
                <TaskCompletionTrend />
            </section>

            {/* Employee Performance */}
            <section>
                <EmployeePerformanceTable />
            </section>

            {/* Report Export */}
            <section>
                <ReportExport />
            </section>
        </div>
    );
}
