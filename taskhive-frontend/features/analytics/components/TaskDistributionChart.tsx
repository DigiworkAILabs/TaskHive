"use client";

import { useTaskDistribution, useTaskByPriority } from "../hooks/useAdminDashboard";
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from "recharts";
import { Loader2 } from "lucide-react";

const STATUS_COLORS: Record<string, string> = {
    TODO: "#eab308",
    IN_PROGRESS: "#3b82f6",
    IN_REVIEW: "#a855f7",
    DONE: "#22c55e",
    CANCELLED: "#6b7280",
};

const PRIORITY_COLORS: Record<string, string> = {
    LOW: "#06b6d4",
    MEDIUM: "#eab308",
    HIGH: "#f97316",
    CRITICAL: "#ef4444",
};

const CustomTooltip = ({ active, payload }: any) => {
    if (!active || !payload?.length) return null;
    return (
        <div
            style={{
                backgroundColor: "#1e1e1e",
                border: "1px solid #2f2f2f",
                borderRadius: "10px",
                padding: "10px 14px",
                fontSize: "13px",
            }}
        >
            <span style={{ color: payload[0].payload.fill, fontWeight: 600 }}>
                {payload[0].name}
            </span>
            <span style={{ color: "#a1a1aa", marginLeft: "8px" }}>{payload[0].value}</span>
        </div>
    );
};

const renderLabel = ({ name, percent }: any) =>
    percent > 0.05 ? `${(percent * 100).toFixed(0)}%` : "";

export default function TaskDistributionChart() {
    const { data: statusData, isLoading: statusLoading } = useTaskDistribution();
    const { data: priorityData, isLoading: priorityLoading } = useTaskByPriority();

    if (statusLoading || priorityLoading) {
        return (
            <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "300px" }}>
                <Loader2 size={28} color="#f97316" className="animate-spin" />
            </div>
        );
    }

    const statusChartData = (statusData || []).map((d) => ({
        name: d.status.replace("_", " "),
        value: d.count,
        fill: STATUS_COLORS[d.status] || "#71717a",
    }));

    const priorityChartData = (priorityData || []).map((d) => ({
        name: d.status,
        value: d.count,
        fill: PRIORITY_COLORS[d.status] || "#71717a",
    }));

    return (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))", gap: "16px" }}>
            {/* By Status — Pie Chart */}
            <div
                style={{
                    backgroundColor: "#161616",
                    border: "1px solid #1f1f1f",
                    borderRadius: "16px",
                    padding: "24px",
                }}
            >
                <h3 style={{ color: "#ffffff", fontSize: "15px", fontWeight: 600, margin: "0 0 20px 0" }}>
                    Tasks by Status
                </h3>
                {statusChartData.length === 0 ? (
                    <div style={{ color: "#52525b", textAlign: "center", padding: "40px 0", fontSize: "14px" }}>
                        No task data available
                    </div>
                ) : (
                    <ResponsiveContainer width="100%" height={280}>
                        <PieChart>
                            <Pie
                                data={statusChartData}
                                cx="50%"
                                cy="50%"
                                outerRadius={100}
                                dataKey="value"
                                label={renderLabel}
                                labelLine={false}
                                stroke="#161616"
                                strokeWidth={2}
                            >
                                {statusChartData.map((entry, i) => (
                                    <Cell key={i} fill={entry.fill} />
                                ))}
                            </Pie>
                            <Tooltip content={<CustomTooltip />} />
                            <Legend
                                wrapperStyle={{ fontSize: "12px", color: "#a1a1aa", paddingTop: "12px" }}
                                iconType="circle"
                                iconSize={8}
                            />
                        </PieChart>
                    </ResponsiveContainer>
                )}
            </div>

            {/* By Priority — Donut Chart */}
            <div
                style={{
                    backgroundColor: "#161616",
                    border: "1px solid #1f1f1f",
                    borderRadius: "16px",
                    padding: "24px",
                }}
            >
                <h3 style={{ color: "#ffffff", fontSize: "15px", fontWeight: 600, margin: "0 0 20px 0" }}>
                    Tasks by Priority
                </h3>
                {priorityChartData.length === 0 ? (
                    <div style={{ color: "#52525b", textAlign: "center", padding: "40px 0", fontSize: "14px" }}>
                        No task data available
                    </div>
                ) : (
                    <ResponsiveContainer width="100%" height={280}>
                        <PieChart>
                            <Pie
                                data={priorityChartData}
                                cx="50%"
                                cy="50%"
                                innerRadius={55}
                                outerRadius={100}
                                dataKey="value"
                                label={renderLabel}
                                labelLine={false}
                                stroke="#161616"
                                strokeWidth={2}
                            >
                                {priorityChartData.map((entry, i) => (
                                    <Cell key={i} fill={entry.fill} />
                                ))}
                            </Pie>
                            <Tooltip content={<CustomTooltip />} />
                            <Legend
                                wrapperStyle={{ fontSize: "12px", color: "#a1a1aa", paddingTop: "12px" }}
                                iconType="circle"
                                iconSize={8}
                            />
                        </PieChart>
                    </ResponsiveContainer>
                )}
            </div>
        </div>
    );
}
