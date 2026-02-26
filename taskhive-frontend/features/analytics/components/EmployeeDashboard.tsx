"use client";

import { useEmployeeDashboard } from "../hooks/useEmployeeDashboard";
import {
    ListTodo, Play, Eye, CheckCircle2,
    TrendingUp, Clock, Loader2, ClipboardList,
} from "lucide-react";

const cards = [
    { key: "totalTasks", label: "Total Tasks", icon: ClipboardList, color: "#6366f1", bg: "rgba(99,102,241,0.12)" },
    { key: "todoTasks", label: "To Do", icon: ListTodo, color: "#eab308", bg: "rgba(234,179,8,0.12)" },
    { key: "inProgressTasks", label: "In Progress", icon: Play, color: "#3b82f6", bg: "rgba(59,130,246,0.12)" },
    { key: "inReviewTasks", label: "In Review", icon: Eye, color: "#a855f7", bg: "rgba(168,85,247,0.12)" },
    { key: "completedTasks", label: "Completed", icon: CheckCircle2, color: "#22c55e", bg: "rgba(34,197,94,0.12)" },
] as const;

export default function EmployeeDashboard() {
    const { data, isLoading } = useEmployeeDashboard();

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
            {/* Task status cards */}
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(160px, 1fr))", gap: "14px" }}>
                {cards.map((card) => {
                    const Icon = card.icon;
                    const value = data[card.key as keyof typeof data];
                    return (
                        <div
                            key={card.key}
                            style={{
                                backgroundColor: "#161616",
                                border: "1px solid #1f1f1f",
                                borderRadius: "16px",
                                padding: "22px",
                                transition: "border-color 0.2s",
                            }}
                            onMouseEnter={(e) => (e.currentTarget.style.borderColor = "#2a2a2a")}
                            onMouseLeave={(e) => (e.currentTarget.style.borderColor = "#1f1f1f")}
                        >
                            <div
                                style={{
                                    width: "42px", height: "42px", borderRadius: "12px",
                                    backgroundColor: card.bg, display: "flex",
                                    alignItems: "center", justifyContent: "center", marginBottom: "12px",
                                }}
                            >
                                <Icon size={20} color={card.color} />
                            </div>
                            <div style={{ color: "#71717a", fontSize: "12px", marginBottom: "4px" }}>{card.label}</div>
                            <div style={{ color: "#ffffff", fontSize: "28px", fontWeight: 700 }}>
                                {Number(value)}
                            </div>
                        </div>
                    );
                })}
            </div>

            {/* Performance metrics */}
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))", gap: "14px" }}>
                <div
                    style={{
                        backgroundColor: "#161616",
                        border: "1px solid #1f1f1f",
                        borderRadius: "16px",
                        padding: "22px",
                    }}
                >
                    <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                        <div
                            style={{
                                width: "42px", height: "42px", borderRadius: "12px",
                                backgroundColor: "rgba(249,115,22,0.12)", display: "flex",
                                alignItems: "center", justifyContent: "center",
                            }}
                        >
                            <TrendingUp size={20} color="#f97316" />
                        </div>
                        <div>
                            <div style={{ color: "#71717a", fontSize: "12px" }}>On-Time Rate</div>
                            <div style={{ color: "#ffffff", fontSize: "24px", fontWeight: 700 }}>
                                {Number(data.onTimeCompletionRate).toFixed(1)}%
                            </div>
                        </div>
                    </div>
                </div>
                <div
                    style={{
                        backgroundColor: "#161616",
                        border: "1px solid #1f1f1f",
                        borderRadius: "16px",
                        padding: "22px",
                    }}
                >
                    <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                        <div
                            style={{
                                width: "42px", height: "42px", borderRadius: "12px",
                                backgroundColor: "rgba(168,85,247,0.12)", display: "flex",
                                alignItems: "center", justifyContent: "center",
                            }}
                        >
                            <Clock size={20} color="#a855f7" />
                        </div>
                        <div>
                            <div style={{ color: "#71717a", fontSize: "12px" }}>Avg Completion</div>
                            <div style={{ color: "#ffffff", fontSize: "24px", fontWeight: 700 }}>
                                {Number(data.avgCompletionHours).toFixed(1)} hrs
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
