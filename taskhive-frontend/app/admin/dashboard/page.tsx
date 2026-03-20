'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { employeeService } from '@/features/employee/services/employeeService';
import { useEmployees } from '@/features/employee/hooks/useEmployees';
import { useRouter } from 'next/navigation';
import { TodayOverviewPanel } from '@/features/analytics/components/TodayOverviewPanel';  // P1.5
import {
    Users, UserCheck, Clock, Loader2,
    ArrowRight, Building2, UserCircle2
} from 'lucide-react';

export default function DashboardPage() {
    const router = useRouter();
    const [stats, setStats] = useState({ total: 0, active: 0, pending: 0 });
    const [statsLoading, setStatsLoading] = useState(true);

    // Fetch recent employees (latest 5)
    const { employees, isLoading: employeesLoading } = useEmployees({ size: 5, sortBy: 'createdAt', sortDir: 'desc' });

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const data = await employeeService.list({ page: 0, size: 1 });
                const total = data.totalElements;

                const activeData = await employeeService.list({ page: 0, size: 1, status: 'ACTIVE' });
                const active = activeData.totalElements;

                const pendingData = await employeeService.list({ page: 0, size: 1, status: 'PENDING' });
                const pending = pendingData.totalElements;

                setStats({ total, active, pending });
            } catch {
                // Silently fail
            } finally {
                setStatsLoading(false);
            }
        };
        fetchStats();
    }, []);

    const isLoading = statsLoading || employeesLoading;

    if (isLoading && stats.total === 0) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
                <Loader2 size={32} color="#f97316" className="animate-spin" />
            </div>
        );
    }

    return (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }}>
            {/* Welcome Section */}
            <div>
                <h1 style={{ fontSize: '24px', fontWeight: 800, color: '#ffffff', margin: '0 0 8px 0', letterSpacing: '-0.5px' }}>
                    Welcome to <span style={{ color: '#f97316' }}>TaskHive</span> Dashboard
                </h1>
                <p style={{ color: '#71717a', fontSize: '15px', margin: 0 }}>
                    Manage your team and track system performance in real-time.
                </p>
            </div>

            {/* P1.5 — Today's Overview + Anomaly Alerts */}
            <TodayOverviewPanel />

            {/* Stat Cards Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '20px' }}>
                <DashboardStatCard
                    icon={<Users size={24} color="#6366f1" />}
                    iconBg="rgba(99,102,241,0.12)"
                    label="Total Employees"
                    value={stats.total}
                    description="Total registered staff"
                />
                <DashboardStatCard
                    icon={<UserCheck size={24} color="#22c55e" />}
                    iconBg="rgba(34,197,94,0.12)"
                    label="Active Members"
                    value={stats.active}
                    description="Verified and active"
                />
                <DashboardStatCard
                    icon={<Clock size={24} color="#eab308" />}
                    iconBg="rgba(234,179,8,0.12)"
                    label="Employee Status Pending "
                    value={stats.pending}
                    description="Awaiting activation"
                />
            </div>

            {/* Recent Employees Section */}
            <div
                style={{
                    backgroundColor: '#161616',
                    border: '1px solid #1f1f1f',
                    borderRadius: '20px',
                    overflow: 'hidden',
                    boxShadow: '0 4px 24px rgba(0,0,0,0.2)'
                }}
            >
                <div
                    style={{
                        padding: '24px 30px',
                        borderBottom: '1px solid #1f1f1f',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center'
                    }}
                >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <div style={{ width: '36px', height: '36px', borderRadius: '10px', backgroundColor: 'rgba(249,115,22,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                            <UserCircle2 size={20} color="#f97316" />
                        </div>
                        <h2 style={{ fontSize: '18px', fontWeight: 700, color: '#ffffff', margin: 0 }}>Recent Employees</h2>
                    </div>
                    <Link
                        href="/admin/employees"
                        style={{
                            fontSize: '13px',
                            color: '#f97316',
                            textDecoration: 'none',
                            fontWeight: 600,
                            display: 'flex',
                            alignItems: 'center',
                            gap: '6px'
                        }}
                    >
                        View All <ArrowRight size={14} />
                    </Link>
                </div>

                <div style={{ overflowX: 'auto' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                            <tr style={{ textAlign: 'left', backgroundColor: '#1a1a1a' }}>
                                <th style={{ padding: '16px 30px', color: '#71717a', fontSize: '12px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '1px' }}>Name</th>
                                <th style={{ padding: '16px 30px', color: '#71717a', fontSize: '12px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '1px' }}>Department</th>
                                <th style={{ padding: '16px 30px', color: '#71717a', fontSize: '12px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '1px' }}>Designation</th>
                                <th style={{ padding: '16px 30px', color: '#71717a', fontSize: '12px', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '1px' }}>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {employees.length > 0 ? (
                                employees.map((emp, idx) => (
                                    <tr
                                        key={emp.id}
                                        onClick={() => router.push(`/admin/employees/${emp.id}`)}
                                        style={{
                                            borderBottom: idx === employees.length - 1 ? 'none' : '1px solid #1f1f1f',
                                            transition: 'background-color 0.2s',
                                            cursor: 'pointer'
                                        }}
                                        className="hover:bg-white/5"
                                    >
                                        <td style={{ padding: '18px 30px' }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                                                <div style={{ width: '32px', height: '32px', borderRadius: '50%', backgroundColor: '#2a2a2a', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#f97316', fontWeight: 700, fontSize: '12px' }}>
                                                    {emp.firstName.charAt(0)}{emp.lastName.charAt(0)}
                                                </div>
                                                <div style={{ display: 'flex', flexDirection: 'column' }}>
                                                    <span style={{ color: '#ffffff', fontWeight: 600, fontSize: '14px' }}>{emp.firstName} {emp.lastName}</span>
                                                    <span style={{ color: '#52525b', fontSize: '12px' }}>{emp.email}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td style={{ padding: '18px 30px', color: '#a1a1aa', fontSize: '14px' }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                                <Building2 size={14} /> {emp.department}
                                            </div>
                                        </td>
                                        <td style={{ padding: '18px 30px', color: '#a1a1aa', fontSize: '14px' }}>{emp.designation}</td>
                                        <td style={{ padding: '18px 30px' }}>
                                            <span
                                                style={{
                                                    padding: '4px 10px',
                                                    borderRadius: '8px',
                                                    fontSize: '11px',
                                                    fontWeight: 700,
                                                    backgroundColor: emp.status === 'ACTIVE' ? 'rgba(34,197,94,0.1)' : 'rgba(234,179,8,0.1)',
                                                    color: emp.status === 'ACTIVE' ? '#22c55e' : '#eab308',
                                                    border: `1px solid ${emp.status === 'ACTIVE' ? 'rgba(34,197,94,0.2)' : 'rgba(234,179,8,0.2)'}`
                                                }}
                                            >
                                                {emp.status}
                                            </span>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={4} style={{ padding: '40px', textAlign: 'center', color: '#71717a' }}>
                                        No employees found.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}

function DashboardStatCard({ icon, iconBg, label, value, description }: {
    icon: React.ReactNode; iconBg: string; label: string; value: number; description: string;
}) {
    return (
        <div
            style={{
                backgroundColor: '#161616',
                border: '1px solid #1f1f1f',
                borderRadius: '20px',
                padding: '24px',
                position: 'relative',
                overflow: 'hidden'
            }}
        >
            <div
                style={{
                    width: '48px', height: '48px', borderRadius: '14px',
                    backgroundColor: iconBg, display: 'flex', alignItems: 'center', justifyContent: 'center',
                    marginBottom: '20px',
                }}
            >
                {icon}
            </div>
            <div style={{ color: '#71717a', fontSize: '14px', fontWeight: 500, marginBottom: '4px' }}>{label}</div>
            <div style={{ color: '#ffffff', fontSize: '32px', fontWeight: 800, marginBottom: '4px' }}>{value.toLocaleString()}</div>
            <div style={{ color: '#52525b', fontSize: '11px' }}>{description}</div>

            {/* Subtle bottom gradient deco */}
            <div style={{ position: 'absolute', bottom: 0, left: 0, right: 0, height: '3px', background: `linear-gradient(90deg, transparent, ${iconBg}, transparent)` }} />
        </div>
    );
}

