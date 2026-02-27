'use client';

import React from 'react';
import { useMyTasks } from '@/features/task/hooks/useMyTasks';
import { ClipboardList, Clock, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { TaskListItem } from '@/features/task/types/task.types';

export default function EmployeeDashboard() {
    const { tasks, isLoading, error } = useMyTasks({ page: 0, size: 100 });

    const stats = {
        total: tasks.length,
        todo: tasks.filter(t => t.status === 'TODO').length,
        inProgress: tasks.filter(t => t.status === 'IN_PROGRESS').length,
        completed: tasks.filter(t => t.status === 'DONE').length,
    };

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
                    Employee Dashboard
                </h1>
                <p style={{ color: '#71717a', fontSize: '15px', margin: 0 }}>
                    Welcome back! Here&apos;s an overview of your assigned tasks.
                </p>
            </div>

            {/* Error */}
            {error && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', backgroundColor: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.25)', borderRadius: '10px', padding: '12px 16px', marginBottom: '24px', color: '#f87171', fontSize: '13px' }}>
                    <AlertCircle size={15} />
                    {error}
                </div>
            )}

            {/* Stat Cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '12px', marginBottom: '28px' }}>
                <DashboardStatCard
                    icon={<ClipboardList size={20} color="#6366f1" />}
                    iconBg="rgba(99,102,241,0.12)"
                    label="Total Assigned"
                    value={stats.total}
                />
                <DashboardStatCard
                    icon={<Clock size={20} color="#f59e0b" />}
                    iconBg="rgba(245,158,11,0.12)"
                    label="In Progress"
                    value={stats.inProgress}
                />
                <DashboardStatCard
                    icon={<CheckCircle2 size={20} color="#22c55e" />}
                    iconBg="rgba(34,197,94,0.12)"
                    label="Completed"
                    value={stats.completed}
                />
                <DashboardStatCard
                    icon={<AlertCircle size={20} color="#ef4444" />}
                    iconBg="rgba(239,68,68,0.12)"
                    label="Pending Action"
                    value={stats.todo}
                />
            </div>

            {/* Recent Tasks */}
            <div style={{ backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '16px', padding: '16px' }}>
                <h3 style={{ fontSize: '16px', fontWeight: 600, color: '#ffffff', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <ClipboardList size={18} color="#f97316" />
                    Recent Tasks
                </h3>

                {tasks.length === 0 ? (
                    <div style={{ padding: '40px', textAlign: 'center', color: '#71717a', fontSize: '14px' }}>
                        No tasks assigned to you yet.
                    </div>
                ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                        {tasks.slice(0, 5).map((task) => (
                            <TaskRow key={task.id} task={task} />
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

function DashboardStatCard({ icon, iconBg, label, value }: {
    icon: React.ReactNode; iconBg: string; label: string; value: number;
}) {
    return (
        <div style={{ backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '16px', padding: '24px' }}>
            <div style={{ width: '40px', height: '40px', borderRadius: '10px', backgroundColor: iconBg, display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: '16px' }}>
                {icon}
            </div>
            <div style={{ color: '#71717a', fontSize: '13px', marginBottom: '4px' }}>{label}</div>
            <div style={{ color: '#ffffff', fontSize: '24px', fontWeight: 700 }}>{value}</div>
        </div>
    );
}

function TaskRow({ task }: { task: TaskListItem }) {
    const router = useRouter();
    const getStatusColor = (status: string) => {
        switch (status) {
            case 'DONE': return '#22c55e';
            case 'IN_PROGRESS': return '#3b82f6';
            case 'IN_REVIEW': return '#a855f7';
            case 'CANCELLED': return '#ef4444';
            default: return '#71717a';
        }
    };

    return (
        <div
            onClick={() => router.push(`/employee/tasks/${task.id}`)}
            style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '12px 16px',
                borderRadius: '10px',
                backgroundColor: 'rgba(255,255,255,0.02)',
                border: '1px solid transparent',
                transition: 'all 0.2s',
                cursor: 'pointer'
            }}
            className="hover:bg-white/5"
        >
            <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                <span style={{ fontSize: '14px', fontWeight: 500, color: '#ffffff' }}>{task.title}</span>
                <span style={{ fontSize: '11px', color: '#52525b' }}>Due: {new Date(task.dueDate).toLocaleDateString()}</span>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <span style={{ fontSize: '10px', fontWeight: 600, padding: '2px 8px', borderRadius: '4px', backgroundColor: `${getStatusColor(task.status)}15`, color: getStatusColor(task.status), border: `1px solid ${getStatusColor(task.status)}30` }}>
                    {task.status}
                </span>
            </div>
        </div>
    );
}
