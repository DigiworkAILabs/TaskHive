"use client";

import { useAdminDashboard } from "../hooks/useAdminDashboard";
import {
    BarChart3, CheckCircle2, AlertTriangle, ListTodo,
    TrendingUp, Clock, Users, Loader2,
} from "lucide-react";

const statCards = [
    { key: "totalTasks", label: "Total Tasks", icon: ListTodo, color: "#6366f1", bg: "rgba(99,102,241,0.12)" },
    { key: "activeTasks", label: "Active Tasks", icon: BarChart3, color: "#3b82f6", bg: "rgba(59,130,246,0.12)" },
    { key: "overdueTasks", label: "Overdue Tasks", icon: AlertTriangle, color: "#ef4444", bg: "rgba(239,68,68,0.12)" },
    { key: "completedTasks", label: "Completed", icon: CheckCircle2, color: "#22c55e", bg: "rgba(34,197,94,0.12)" },
] as const;

const secondaryCards = [
    { key: "completionRate", label: "Completion Rate", icon: TrendingUp, color: "#f97316", bg: "rgba(249,115,22,0.12)", suffix: "%" },
    { key: "avgCompletionHours", label: "Avg Completion", icon: Clock, color: "#a855f7", bg: "rgba(168,85,247,0.12)", suffix: " hrs" },
    { key: "totalEmployees", label: "Total Employees", icon: Users, color: "#06b6d4", bg: "rgba(6,182,212,0.12)", suffix: "" },
] as const;

export default function AdminDashboard() {
    const { data, isLoading } = useAdminDashboard();

    if (isLoading) {
        return (
            <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "200px" }}>
                <Loader2 size={28} color="#f97316" className="animate-spin" />
            </div>
        );
    }

    if (!data) return null;

    return (
        <div style={{ display: "flex", flexDirection: "column", gap: "16px" }}>
            {/* Primary stat cards */}
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: "16px" }}>
                {statCards.map((card) => {
                    const Icon = card.icon;
                    const value = data[card.key as keyof typeof data];
                    return (
                        <div
                            key={card.key}
                            style={{
                                backgroundColor: "#161616",
                                border: "1px solid #1f1f1f",
                                borderRadius: "16px",
                                padding: "24px",
                                transition: "border-color 0.2s",
                            }}
                            onMouseEnter={(e) => (e.currentTarget.style.borderColor = "#2a2a2a")}
                            onMouseLeave={(e) => (e.currentTarget.style.borderColor = "#1f1f1f")}
                        >
                            <div
                                style={{
                                    width: "48px", height: "48px", borderRadius: "14px",
                                    backgroundColor: card.bg, display: "flex",
                                    alignItems: "center", justifyContent: "center", marginBottom: "14px",
                                }}
                            >
                                <Icon size={22} color={card.color} />
                            </div>
                            <div style={{ color: "#71717a", fontSize: "13px", marginBottom: "4px" }}>{card.label}</div>
                            <div style={{ color: "#ffffff", fontSize: "32px", fontWeight: 700 }}>
                                {Number(value).toLocaleString()}
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* Secondary stat cards */}
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: "16px" }}>
                {secondaryCards.map((card) => {
                    const Icon = card.icon;
                    const raw = data[card.key as keyof typeof data];
                    const value = typeof raw === "number" ? raw.toFixed(1) : raw;
                    return (
                        <div
                            key={card.key}
                            style={{
                                backgroundColor: "#161616",
                                border: "1px solid #1f1f1f",
                                borderRadius: "16px",
                                padding: "24px",
                                transition: "border-color 0.2s",
                            }}
                            onMouseEnter={(e) => (e.currentTarget.style.borderColor = "#2a2a2a")}
                            onMouseLeave={(e) => (e.currentTarget.style.borderColor = "#1f1f1f")}
                        >
                            <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                                <div
                                    style={{
                                        width: "44px", height: "44px", borderRadius: "12px",
                                        backgroundColor: card.bg, display: "flex",
                                        alignItems: "center", justifyContent: "center",
                                    }}
                                >
                                    <Icon size={20} color={card.color} />
                                </div>
                                <div>
                                    <div style={{ color: "#71717a", fontSize: "12px" }}>{card.label}</div>
                                    <div style={{ color: "#ffffff", fontSize: "24px", fontWeight: 700 }}>
                                        {value}{card.suffix}
                                    </div>
                                </div>
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}
