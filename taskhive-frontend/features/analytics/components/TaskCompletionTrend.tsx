"use client";

import { useCompletionTrend } from "../hooks/useAdminDashboard";
import {
    LineChart, Line, XAxis, YAxis, CartesianGrid,
    Tooltip, ResponsiveContainer, Area, AreaChart,
} from "recharts";
import { Loader2 } from "lucide-react";

const CustomTooltip = ({ active, payload, label }: any) => {
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
            <div style={{ color: "#a1a1aa", marginBottom: "4px" }}>{label}</div>
            <div style={{ color: "#f97316", fontWeight: 600 }}>
                {payload[0].value} task{payload[0].value !== 1 ? "s" : ""} completed
            </div>
        </div>
    );
};

export default function TaskCompletionTrend() {
    const { data, isLoading } = useCompletionTrend();

    if (isLoading) {
        return (
            <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "300px" }}>
                <Loader2 size={28} color="#f97316" className="animate-spin" />
            </div>
        );
    }

    const chartData = (data || []).map((d) => ({
        date: new Date(d.date).toLocaleDateString("en-US", { month: "short", day: "numeric" }),
        count: d.count,
    }));

    return (
        <div
            style={{
                backgroundColor: "#161616",
                border: "1px solid #1f1f1f",
                borderRadius: "16px",
                padding: "24px",
            }}
        >
            <h3 style={{ color: "#ffffff", fontSize: "15px", fontWeight: 600, margin: "0 0 20px 0" }}>
                Completion Trend (30 Days)
            </h3>
            {chartData.length === 0 ? (
                <div style={{ color: "#52525b", textAlign: "center", padding: "60px 0", fontSize: "14px" }}>
                    No completed tasks in the last 30 days
                </div>
            ) : (
                <ResponsiveContainer width="100%" height={300}>
                    <AreaChart data={chartData} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
                        <defs>
                            <linearGradient id="colorTrend" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="5%" stopColor="#f97316" stopOpacity={0.3} />
                                <stop offset="95%" stopColor="#f97316" stopOpacity={0} />
                            </linearGradient>
                        </defs>
                        <CartesianGrid strokeDasharray="3 3" stroke="#1f1f1f" />
                        <XAxis
                            dataKey="date"
                            tick={{ fill: "#71717a", fontSize: 11 }}
                            axisLine={{ stroke: "#2a2a2a" }}
                            tickLine={false}
                        />
                        <YAxis
                            tick={{ fill: "#71717a", fontSize: 11 }}
                            axisLine={{ stroke: "#2a2a2a" }}
                            tickLine={false}
                            allowDecimals={false}
                        />
                        <Tooltip content={<CustomTooltip />} />
                        <Area
                            type="monotone"
                            dataKey="count"
                            stroke="#f97316"
                            strokeWidth={2.5}
                            fill="url(#colorTrend)"
                            dot={{ fill: "#f97316", strokeWidth: 0, r: 3 }}
                            activeDot={{ fill: "#f97316", strokeWidth: 2, stroke: "#161616", r: 5 }}
                        />
                    </AreaChart>
                </ResponsiveContainer>
            )}
        </div>
    );
}
