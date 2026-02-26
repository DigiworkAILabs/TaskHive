'use client';

import React, { useEffect, useState } from 'react';
import { employeeService } from '@/features/employee/services/employeeService';
import { Users, UserCheck, Clock, Loader2 } from 'lucide-react';

export default function DashboardPage() {
    const [stats, setStats] = useState({ total: 0, active: 0, pending: 0 });
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const data = await employeeService.list({ page: 0, size: 1 });
                // Get total from pagination
                const total = data.totalElements;

                // Fetch active count
                const activeData = await employeeService.list({ page: 0, size: 1, status: 'ACTIVE' });
                const active = activeData.totalElements;

                // Fetch pending count
                const pendingData = await employeeService.list({ page: 0, size: 1, status: 'PENDING' });
                const pending = pendingData.totalElements;

                setStats({ total, active, pending });
            } catch {
                // Silently fail — dashboard is informational
            } finally {
                setIsLoading(false);
            }
        };
        fetchStats();
    }, []);

    if (isLoading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '300px' }}>
                <Loader2 size={32} color="#f97316" className="animate-spin" />
            </div>
        );
    }

    return (
        <div>
            {/* Welcome */}
            <div style={{ marginBottom: '32px' }}>
                <h1 style={{ fontSize: '22px', fontWeight: 700, color: '#ffffff', margin: '0 0 8px 0' }}>
                    Welcome to TaskHive
                </h1>
                <p style={{ color: '#71717a', fontSize: '15px', margin: 0 }}>
                    Here&apos;s a quick overview of your team.
                </p>
            </div>

            {/* Stat Cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '16px' }}>
                <DashboardStatCard
                    icon={<Users size={24} color="#6366f1" />}
                    iconBg="rgba(99,102,241,0.12)"
                    label="Total Employees"
                    value={stats.total}
                />
                <DashboardStatCard
                    icon={<UserCheck size={24} color="#22c55e" />}
                    iconBg="rgba(34,197,94,0.12)"
                    label="Active Employees"
                    value={stats.active}
                />
                <DashboardStatCard
                    icon={<Clock size={24} color="#eab308" />}
                    iconBg="rgba(234,179,8,0.12)"
                    label="Pending Activation"
                    value={stats.pending}
                />
            </div>
        </div>
    );
}

function DashboardStatCard({ icon, iconBg, label, value }: {
    icon: React.ReactNode; iconBg: string; label: string; value: number;
}) {
    return (
        <div
            style={{
                backgroundColor: '#161616',
                border: '1px solid #1f1f1f',
                borderRadius: '16px',
                padding: '28px',
            }}
        >
            <div
                style={{
                    width: '52px', height: '52px', borderRadius: '14px',
                    backgroundColor: iconBg, display: 'flex', alignItems: 'center', justifyContent: 'center',
                    marginBottom: '16px',
                }}
            >
                {icon}
            </div>
            <div style={{ color: '#71717a', fontSize: '13px', marginBottom: '6px' }}>{label}</div>
            <div style={{ color: '#ffffff', fontSize: '36px', fontWeight: 700 }}>{value.toLocaleString()}</div>
        </div>
    );
}
