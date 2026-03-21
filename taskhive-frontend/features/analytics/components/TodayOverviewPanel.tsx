'use client';

import React from 'react';
import { useTodayOverview } from '../hooks/useTodayOverview';
import { RefreshCw, AlertTriangle, CheckCircle2, Clock, Activity, Hourglass } from 'lucide-react';

/**
 * P1.5 — Today's overview panel for the admin dashboard.
 * Auto-refreshes every 5 minutes.
 */

interface StatConfig {
    label: string;
    value: number;
    color: string;
    bg: string;
    accent: string;
    icon: React.ReactNode;
}

export const TodayOverviewPanel: React.FC = () => {
    const { overview, anomalies, isLoading, error, refresh } = useTodayOverview();

    if (isLoading && !overview) {
        return (
            <div style={{ padding: '24px', textAlign: 'center', color: '#71717a', fontSize: '13px' }}>
                Loading today's overview...
            </div>
        );
    }

    if (error && !overview) {
        return (
            <div style={{ padding: '16px', background: 'rgba(239,68,68,0.05)', border: '1px solid rgba(239,68,68,0.15)', borderRadius: '10px', fontSize: '13px', color: '#ef4444' }}>
                {error}
            </div>
        );
    }

    const stats: StatConfig[] = overview ? [
        {
            label: 'Assigned Today',
            value: overview.totalAssignedToday,
            color: '#d4d4d8',
            bg: 'rgba(255,255,255,0.04)',
            accent: '#71717a',
            icon: <Activity size={16} color="#71717a" />,
        },
        {
            label: 'Completed',
            value: overview.completedToday,
            color: '#22c55e',
            bg: 'rgba(34,197,94,0.06)',
            accent: '#22c55e',
            icon: <CheckCircle2 size={16} color="#22c55e" />,
        },
        {
            label: 'In Progress',
            value: overview.inProgress,
            color: '#3b82f6',
            bg: 'rgba(59,130,246,0.06)',
            accent: '#3b82f6',
            icon: <Clock size={16} color="#3b82f6" />,
        },
        {
            label: 'Pending Approval',
            value: overview.pendingApproval,
            color: '#f59e0b',
            bg: 'rgba(245,158,11,0.06)',
            accent: '#f59e0b',
            icon: <Hourglass size={16} color="#f59e0b" />,
        },
        {
            label: 'Overdue',
            value: overview.overdueToday,
            color: '#ef4444',
            bg: 'rgba(239,68,68,0.06)',
            accent: '#ef4444',
            icon: <AlertTriangle size={16} color="#ef4444" />,
        },
        {
            label: 'Late Submissions',
            value: overview.lateSubmissionsToday,
            color: '#f97316',
            bg: 'rgba(249,115,22,0.06)',
            accent: '#f97316',
            icon: <AlertTriangle size={16} color="#f97316" />,
        },
    ] : [];

    return (
        <div>
            {/* Panel wrapper */}
            <div
                style={{
                    backgroundColor: '#161616',
                    border: '1px solid #1f1f1f',
                    borderRadius: '20px',
                    overflow: 'hidden',
                    boxShadow: '0 4px 24px rgba(0,0,0,0.2)',
                }}
            >
                {/* Panel header */}
                <div
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        padding: '20px 24px',
                        borderBottom: '1px solid #1f1f1f',
                    }}
                >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div
                            style={{
                                width: '36px', height: '36px', borderRadius: '10px',
                                backgroundColor: 'rgba(249,115,22,0.1)',
                                display: 'flex', alignItems: 'center', justifyContent: 'center',
                            }}
                        >
                            <Activity size={18} color="#f97316" />
                        </div>
                        <div>
                            <span style={{ fontSize: '16px', fontWeight: 700, color: '#ffffff' }}>Today's Overview</span>
                            <span
                                style={{
                                    marginLeft: '10px', fontSize: '11px', color: '#22c55e',
                                    backgroundColor: 'rgba(34,197,94,0.1)', padding: '2px 8px',
                                    borderRadius: '999px', fontWeight: 600,
                                }}
                            >
                                • Live
                            </span>
                        </div>
                    </div>
                    <button
                        onClick={refresh}
                        title="Refresh overview"
                        style={{
                            background: 'none', border: '1px solid #2a2a2a', cursor: 'pointer',
                            color: '#71717a', padding: '7px', borderRadius: '8px',
                            transition: 'all 0.15s', display: 'flex', alignItems: 'center', justifyContent: 'center',
                        }}
                        onMouseEnter={(e) => { e.currentTarget.style.color = '#ffffff'; e.currentTarget.style.borderColor = '#3f3f46'; }}
                        onMouseLeave={(e) => { e.currentTarget.style.color = '#71717a'; e.currentTarget.style.borderColor = '#2a2a2a'; }}
                    >
                        <RefreshCw size={14} />
                    </button>
                </div>

                {/* Stats grid — fills full width */}
                {overview && (
                    <div
                        style={{
                            display: 'grid',
                            gridTemplateColumns: 'repeat(6, 1fr)',
                            gap: '0',
                        }}
                    >
                        {stats.map((s, idx) => (
                            <div
                                key={s.label}
                                style={{
                                    display: 'flex',
                                    flexDirection: 'column',
                                    gap: '8px',
                                    padding: '20px 20px',
                                    backgroundColor: s.bg,
                                    borderRight: idx < stats.length - 1 ? '1px solid #1f1f1f' : undefined,
                                    borderTop: '3px solid transparent',
                                    borderImage: `linear-gradient(to right, ${s.accent}40, transparent) 1`,
                                    position: 'relative',
                                    transition: 'background-color 0.15s',
                                }}
                                onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = s.bg.replace('0.06', '0.10').replace('0.04', '0.07'))}
                                onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = s.bg)}
                            >
                                {/* Top accent bar */}
                                <div
                                    style={{
                                        position: 'absolute',
                                        top: 0, left: 0, right: 0, height: '2px',
                                        background: `linear-gradient(90deg, ${s.accent}, transparent)`,
                                    }}
                                />
                                <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                    {s.icon}
                                    <span
                                        style={{
                                            fontSize: '10px', color: '#71717a', fontWeight: 600,
                                            textTransform: 'uppercase', letterSpacing: '0.6px',
                                        }}
                                    >
                                        {s.label}
                                    </span>
                                </div>
                                <span
                                    style={{
                                        fontSize: '30px', fontWeight: 800, color: s.color,
                                        lineHeight: 1, letterSpacing: '-1px',
                                    }}
                                >
                                    {s.value}
                                </span>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Anomaly alerts (outside the card, below it) */}
            {anomalies.length > 0 && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '12px' }}>
                    {anomalies.map((alert) => (
                        <div
                            key={alert.ruleId}
                            style={{
                                display: 'flex', alignItems: 'flex-start', gap: '10px',
                                padding: '12px 16px',
                                background: alert.severity === 'HIGH'
                                    ? 'rgba(239,68,68,0.06)'
                                    : 'rgba(245,158,11,0.06)',
                                border: `1px solid ${alert.severity === 'HIGH'
                                    ? 'rgba(239,68,68,0.2)'
                                    : 'rgba(245,158,11,0.2)'}`,
                                borderRadius: '10px',
                            }}
                        >
                            <AlertTriangle
                                size={14}
                                color={alert.severity === 'HIGH' ? '#ef4444' : '#f59e0b'}
                                style={{ flexShrink: 0, marginTop: '1px' }}
                            />
                            <div>
                                <span
                                    style={{
                                        fontSize: '11px', fontWeight: 700,
                                        color: alert.severity === 'HIGH' ? '#ef4444' : '#f59e0b',
                                        marginRight: '8px',
                                    }}
                                >
                                    {alert.ruleId}
                                </span>
                                <span style={{ fontSize: '12px', color: '#a1a1aa' }}>{alert.description}</span>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};
