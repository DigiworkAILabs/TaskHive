'use client';

import React from 'react';
import { useMyTasks } from '@/features/task/hooks/useMyTasks';
import { TaskCard } from '@/features/task/components/TaskCard';
import { useRouter } from 'next/navigation';
import { Loader2, ClipboardList, AlertCircle, Filter, Search } from 'lucide-react';
import { TaskStatus, TaskPriority } from '@/features/task/types/task.types';

export default function EmployeeTasksPage() {
    const { tasks, pagination, filters, isLoading, error, updateFilters } = useMyTasks();
    const router = useRouter();

    const stats = {
        total: pagination.totalElements,
        todo: tasks.filter(t => t.status === 'TODO').length,
        inProgress: tasks.filter(t => t.status === 'IN_PROGRESS').length,
        done: tasks.filter(t => t.status === 'DONE').length
    };

    return (
        <div>
            <div style={{ marginBottom: '24px' }}>
                <h1 style={{ fontSize: '22px', fontWeight: 700, color: '#ffffff', margin: 0 }}>My Tasks</h1>
                <p style={{ color: '#71717a', fontSize: '14px', marginTop: '4px' }}>Track and manage your assigned tasks</p>
            </div>

            {/* Simple Stats */}
            <div style={{ display: 'flex', gap: '16px', marginBottom: '24px', flexWrap: 'wrap' }}>
                <QuickStat label="Total" value={stats.total} color="#ffffff" />
                <QuickStat label="To Do" value={stats.todo} color="#a1a1aa" />
                <QuickStat label="In Progress" value={stats.inProgress} color="#3b82f6" />
                <QuickStat label="Completed" value={stats.done} color="#22c55e" />
            </div>

            {/* Filter Bar */}
            <div
                style={{
                    backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '14px',
                    padding: '16px 20px', display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '20px', flexWrap: 'wrap',
                }}
            >
                <Filter size={15} color="#71717a" />
                <select
                    value={filters.status || ''}
                    onChange={(e) => updateFilters({ status: e.target.value as TaskStatus || undefined })}
                    style={{
                        padding: '8px 12px', borderRadius: '8px', border: '1px solid #2a2a2a',
                        backgroundColor: '#111111', color: '#ffffff', fontSize: '13px', cursor: 'pointer', outline: 'none',
                    }}
                >
                    <option value="">All Statuses</option>
                    <option value="TODO">To Do</option>
                    <option value="IN_PROGRESS">In Progress</option>
                    <option value="IN_REVIEW">In Review</option>
                    <option value="DONE">Done</option>
                </select>

                <select
                    value={filters.priority || ''}
                    onChange={(e) => updateFilters({ priority: e.target.value as TaskPriority || undefined })}
                    style={{
                        padding: '8px 12px', borderRadius: '8px', border: '1px solid #2a2a2a',
                        backgroundColor: '#111111', color: '#ffffff', fontSize: '13px', cursor: 'pointer', outline: 'none',
                    }}
                >
                    <option value="">All Priorities</option>
                    <option value="LOW">Low</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HIGH">High</option>
                    <option value="CRITICAL">Critical</option>
                </select>
            </div>

            {/* Error State */}
            {error && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', backgroundColor: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.25)', borderRadius: '10px', padding: '12px 16px', marginBottom: '20px', color: '#f87171', fontSize: '13px' }}>
                    <AlertCircle size={15} /> {error}
                </div>
            )}

            {/* Task Table */}
            <div style={{ backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '14px', overflow: 'hidden' }}>
                {isLoading ? (
                    <div style={{ display: 'flex', justifyContent: 'center', padding: '60px' }}>
                        <Loader2 size={24} color="#f97316" className="animate-spin" />
                    </div>
                ) : tasks.length === 0 ? (
                    <div style={{ padding: '60px', textAlign: 'center', color: '#52525b' }}>
                        <ClipboardList size={40} style={{ margin: '0 auto 12px', opacity: 0.4 }} />
                        <p style={{ margin: 0 }}>No tasks found</p>
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                            <thead>
                                <tr style={{ borderBottom: '1px solid #1f1f1f' }}>
                                    <th style={{ padding: '14px 16px', width: '48px' }}></th>
                                    {['Title', 'Priority', 'Status', 'Assigned To', 'Due Date', ''].map(h => (
                                        <th key={h} style={{ padding: '14px 16px', textAlign: 'left', fontSize: '11px', fontWeight: 600, color: '#f97316', textTransform: 'uppercase' }}>{h}</th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody>
                                {tasks.map(task => (
                                    <TaskCard
                                        key={task.id}
                                        task={task}
                                        selected={false}
                                        onSelect={() => { }}
                                        onView={(id) => router.push(`/employee/tasks/${id}`)}
                                        onEdit={() => { }} // Employees can't edit basic task info
                                        onDelete={() => { }} // Employees can't delete
                                    />
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
}

function QuickStat({ label, value, color }: { label: string, value: number, color: string }) {
    return (
        <div style={{ backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '12px', padding: '16px 20px', flex: '1 1 120px' }}>
            <div style={{ fontSize: '12px', color: '#71717a', marginBottom: '4px' }}>{label}</div>
            <div style={{ fontSize: '20px', fontWeight: 700, color }}>{value}</div>
        </div>
    );
}
