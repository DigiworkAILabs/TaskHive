"use client";

import { useEmployeePerformance } from "../hooks/useAdminDashboard";
import { Loader2, Users, BrainCircuit } from "lucide-react";
import { useProductivityScore } from "../../ml/hooks/useProductivityScore";

export default function EmployeePerformanceTable() {
    const { data, isLoading } = useEmployeePerformance();

    if (isLoading) {
        return (
            <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "200px" }}>
                <Loader2 size={28} color="#f97316" className="animate-spin" />
            </div>
        );
    }

    if (!data || data.length === 0) {
        return (
            <div
                style={{
                    backgroundColor: "#161616",
                    border: "1px solid #1f1f1f",
                    borderRadius: "16px",
                    padding: "24px",
                }}
            >
                <h3 style={{ color: "#ffffff", fontSize: "15px", fontWeight: 600, margin: "0 0 16px 0" }}>
                    Employee Performance
                </h3>
                <div style={{ color: "#52525b", textAlign: "center", padding: "40px 0", fontSize: "14px" }}>
                    No performance data available
                </div>
            </div>
        );
    }

    return (
        <div
            style={{
                backgroundColor: "#161616",
                border: "1px solid #1f1f1f",
                borderRadius: "16px",
                padding: "24px",
                overflow: "hidden",
            }}
        >
            <div style={{ display: "flex", alignItems: "center", gap: "10px", marginBottom: "20px" }}>
                <div
                    style={{
                        width: "36px", height: "36px", borderRadius: "10px",
                        backgroundColor: "rgba(6,182,212,0.12)", display: "flex",
                        alignItems: "center", justifyContent: "center",
                    }}
                >
                    <Users size={18} color="#06b6d4" />
                </div>
                <h3 style={{ color: "#ffffff", fontSize: "15px", fontWeight: 600, margin: 0 }}>
                    Employee Performance
                </h3>
            </div>

            <div style={{ overflowX: "auto" }}>
                <table style={{ width: "100%", borderCollapse: "collapse", minWidth: "500px" }}>
                    <thead>
                        <tr
                            style={{
                                borderBottom: "1px solid #2a2a2a",
                            }}
                        >
                            {["Employee", "Assigned", "Completed", "On-Time Rate", "Avg Hours", "ML Productivity"].map((h) => (
                                <th
                                    key={h}
                                    style={{
                                        textAlign: h === "Employee" ? "left" : "center",
                                        padding: "12px 16px",
                                        fontSize: "11px",
                                        fontWeight: 700,
                                        color: "#f97316",
                                        textTransform: "uppercase",
                                        letterSpacing: "0.5px",
                                    }}
                                >
                                    {h}
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {data.map((emp, idx) => (
                            <tr
                                key={emp.employeeId}
                                style={{
                                    borderBottom: idx < data.length - 1 ? "1px solid #1f1f1f" : "none",
                                    transition: "background-color 0.15s",
                                }}
                                onMouseEnter={(e) =>
                                    (e.currentTarget.style.backgroundColor = "rgba(255,255,255,0.02)")
                                }
                                onMouseLeave={(e) =>
                                    (e.currentTarget.style.backgroundColor = "transparent")
                                }
                            >
                                <td style={{ padding: "14px 16px" }}>
                                    <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
                                        <div
                                            style={{
                                                width: "32px", height: "32px", borderRadius: "50%",
                                                backgroundColor: "#1f1f1f", border: "2px solid #2a2a2a",
                                                display: "flex", alignItems: "center", justifyContent: "center",
                                                fontSize: "12px", fontWeight: 600, color: "#f97316",
                                                flexShrink: 0,
                                            }}
                                        >
                                            {emp.employeeName
                                                .split(" ")
                                                .map((n) => n.charAt(0))
                                                .join("")
                                                .substring(0, 2)
                                                .toUpperCase()}
                                        </div>
                                        <span style={{ color: "#ffffff", fontSize: "13px", fontWeight: 500 }}>
                                            {emp.employeeName}
                                        </span>
                                    </div>
                                </td>
                                <td style={{ textAlign: "center", color: "#a1a1aa", fontSize: "13px", padding: "14px 16px" }}>
                                    {emp.tasksAssigned}
                                </td>
                                <td style={{ textAlign: "center", color: "#22c55e", fontSize: "13px", fontWeight: 600, padding: "14px 16px" }}>
                                    {emp.tasksCompleted}
                                </td>
                                <td style={{ textAlign: "center", padding: "14px 16px" }}>
                                    <span
                                        style={{
                                            display: "inline-block",
                                            padding: "4px 10px",
                                            borderRadius: "8px",
                                            fontSize: "12px",
                                            fontWeight: 600,
                                            backgroundColor:
                                                emp.onTimeRate >= 80
                                                    ? "rgba(34,197,94,0.12)"
                                                    : emp.onTimeRate >= 50
                                                        ? "rgba(234,179,8,0.12)"
                                                        : "rgba(239,68,68,0.12)",
                                            color:
                                                emp.onTimeRate >= 80
                                                    ? "#22c55e"
                                                    : emp.onTimeRate >= 50
                                                        ? "#eab308"
                                                        : "#ef4444",
                                        }}
                                    >
                                        {Number(emp.onTimeRate).toFixed(1)}%
                                    </span>
                                </td>
                                <td style={{ textAlign: "center", color: "#a1a1aa", fontSize: "13px", padding: "14px 16px" }}>
                                    {Number(emp.avgCompletionHours).toFixed(1)}h
                                </td>
                                <td style={{ textAlign: "center", padding: "14px 16px" }}>
                                    <ProductivityScoreCell employeeId={emp.employeeId} />
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
}
function ProductivityScoreCell({ employeeId }: { employeeId: string }) {
    const { scoreData, isLoading } = useProductivityScore(employeeId);

    if (isLoading) return <div className="h-4 w-12 bg-slate-800 animate-pulse rounded mx-auto" />;
    if (!scoreData) return <span className="text-slate-600">--</span>;

    const { grade } = scoreData;
    const colors = {
        'A': '#22c55e',
        'B': '#3b82f6',
        'C': '#eab308',
        'D': '#f97316',
        'F': '#ef4444'
    };

    return (
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' }}>
            <span
                style={{
                    display: "inline-block",
                    padding: "2px 8px",
                    borderRadius: "6px",
                    fontSize: "11px",
                    fontWeight: 800,
                    backgroundColor: `${colors[grade]}20`,
                    color: colors[grade],
                    border: `1px solid ${colors[grade]}30`
                }}
            >
                GRADE {grade}
            </span>
            {grade === 'A' && <BrainCircuit size={12} color="#22c55e" />}
        </div>
    );
}
