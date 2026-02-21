'use client';

import React from 'react';
import Header from '@/shared/components/Header';
import { useAdminDashboard } from '../hooks/useAdminDashboard';
import {
    Users,
    Clock,
    RotateCcw,
    CheckCircle2,
    MoreHorizontal,
    Plus
} from 'lucide-react';
import { cn } from '@/shared/utils/cn';
import { Button } from '@/features/auth/components/ui/Button';
import Link from 'next/link';

export default function AdminDashboard() {
    const { data, loading, error } = useAdminDashboard();

    if (loading) return <div className="p-8 text-zinc-500">Loading dashboard...</div>;
    if (error) return <div className="p-8 text-red-500">Error: {error}</div>;
    if (!data) return null;

    return (
        <div className="flex-1 flex flex-col h-full bg-[#0a0a0a]">
            <Header
                title="Dashboard Overview"
                subtitle="Welcome back, here's what's happening today."
                actionButton={
                    <Link href="/admin/employees/new">
                        <Button className="flex items-center gap-2 px-5 rounded-xl">
                            <Plus className="w-4 h-4" />
                            Add Employee
                        </Button>
                    </Link>
                }
            />

            <div className="flex-1 p-8 flex gap-8">
                {/* Left Column: Stats & Recent Tasks */}
                <div className="flex-[2] space-y-8">
                    {/* Stat Cards Row */}
                    <div className="grid grid-cols-4 gap-4">
                        <StatCard
                            label="TOTAL EMPLOYEES"
                            value={data.totalEmployees}
                            icon={Users}
                            trend={`+${data.employeesGrowth}%`}
                            trendSubtitle="vs last month"
                            trendColor="text-green-500"
                        />
                        <StatCard
                            label="PENDING TASKS"
                            value={data.pendingTasks}
                            icon={Clock}
                            warning={data.pendingTasksStatus === 'attention'}
                            warningText="Attention needed"
                        />
                        <StatCard
                            label="IN PROGRESS"
                            value={data.inProgressTasks}
                            icon={RotateCcw}
                            subtitle="On track"
                            subtitleColor="text-blue-500"
                        />
                        <StatCard
                            label="COMPLETED"
                            value={data.completedTasks}
                            icon={CheckCircle2}
                            trend={`+${data.completedTasksGrowth}%`}
                            trendSubtitle="vs last week"
                            trendColor="text-green-500"
                        />
                    </div>

                    {/* Recent Assigned Tasks Table */}
                    <div className="bg-[#161616] rounded-2xl border border-[#1f1f1f] overflow-hidden">
                        <div className="px-6 py-5 flex items-center justify-between border-b border-[#1f1f1f]">
                            <h3 className="text-lg font-semibold text-white">Recent Assigned Tasks</h3>
                            <Link href="/admin/tasks" className="text-sm font-medium text-orange-500 hover:text-orange-400">View All</Link>
                        </div>
                        <table className="w-full text-left">
                            <thead>
                                <tr className="text-[12px] font-bold text-zinc-500 uppercase tracking-widest border-b border-[#1f1f1f]">
                                    <th className="px-6 py-4">Task Name</th>
                                    <th className="px-6 py-4">Assignee</th>
                                    <th className="px-6 py-4">Due Date</th>
                                    <th className="px-6 py-4">Status</th>
                                    <th className="px-6 py-4 text-center">Action</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-[#1f1f1f]">
                                {data.recentTasks.map((task) => (
                                    <tr key={task.id} className="group hover:bg-zinc-900/50 transition-colors">
                                        <td className="px-6 py-4">
                                            <span className="text-sm font-semibold text-zinc-100">{task.title}</span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="w-8 h-8 rounded-full bg-zinc-800 flex items-center justify-center overflow-hidden">
                                                    {task.assigneeAvatar ? (
                                                        <img src={task.assigneeAvatar} alt={task.assigneeName} className="w-full h-full object-cover" />
                                                    ) : (
                                                        <span className="text-[10px] font-bold text-white">{task.assigneeName.split(' ').map(n => n[0]).join('')}</span>
                                                    )}
                                                </div>
                                                <span className="text-xs text-zinc-300 font-medium">{task.assigneeName}</span>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <span className="text-xs text-zinc-400 font-medium">{new Date(task.dueDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span>
                                        </td>
                                        <td className="px-6 py-4">
                                            <StatusBadge status={task.status} />
                                        </td>
                                        <td className="px-6 py-4 text-center">
                                            <button className="p-1 rounded-md text-zinc-500 hover:text-white transition-colors">
                                                <MoreHorizontal className="w-4 h-4" />
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>

                {/* Right Column: Panels */}
                <div className="flex-1 space-y-8">
                    {/* Task Distribution Panel */}
                    <div className="bg-[#161616] rounded-2xl border border-[#1f1f1f] p-6">
                        <h3 className="text-lg font-semibold text-white mb-6">Task Distribution</h3>
                        <div className="space-y-6">
                            {data.taskDistribution.map((dist) => (
                                <div key={dist.category} className="space-y-2">
                                    <div className="flex items-center justify-between text-xs font-bold uppercase tracking-wider">
                                        <span className="text-zinc-500">{dist.category}</span>
                                        <span className="text-white">{dist.percentage}%</span>
                                    </div>
                                    <div className="h-1.5 w-full bg-zinc-800 rounded-full overflow-hidden">
                                        <div
                                            className={cn(
                                                "h-full rounded-full transition-all duration-1000",
                                                dist.category === 'Marketing' ? 'bg-orange-400' :
                                                    dist.category === 'Development' ? 'bg-blue-500' :
                                                        'bg-purple-500'
                                            )}
                                            style={{ width: `${dist.percentage}%` }}
                                        />
                                    </div>
                                </div>
                            ))}
                        </div>

                        <div className="mt-8 pt-8 border-t border-[#1f1f1f] flex items-center justify-between">
                            <div className="flex -space-x-2">
                                {[1, 2, 3, 4].map(i => (
                                    <div key={i} className="w-7 h-7 rounded-full border-2 border-[#161616] bg-zinc-800" />
                                ))}
                                <div className="w-7 h-7 rounded-full border-2 border-[#161616] bg-zinc-700 flex items-center justify-center text-[8px] font-bold text-white">+5</div>
                            </div>
                            <Link href="/admin/employees" className="text-xs font-bold text-orange-500 hover:text-orange-400 uppercase tracking-tighter">Manage Team</Link>
                        </div>
                    </div>

                    {/* Upcoming Deadlines Panel */}
                    <div className="bg-[#161616] rounded-2xl border border-[#1f1f1f] p-6">
                        <h3 className="text-lg font-semibold text-white mb-6">Upcoming Deadlines</h3>
                        <div className="space-y-6">
                            {data.upcomingDeadlines.map((deadline) => (
                                <div key={deadline.id} className="flex gap-4 group cursor-pointer">
                                    <div className={cn("mt-1.5 w-2 h-2 rounded-full shrink-0 shadow-lg", deadline.color)} />
                                    <div className="space-y-1">
                                        <h4 className="text-sm font-semibold text-zinc-200 group-hover:text-white transition-colors">{deadline.title}</h4>
                                        <p className="text-[11px] text-zinc-500 font-medium">{deadline.dueDate}</p>
                                    </div>
                                </div>
                            ))}
                        </div>
                        <button className="w-full mt-8 py-3 bg-[#2a2a2a]/30 hover:bg-[#2a2a2a]/50 text-zinc-400 hover:text-white text-xs font-bold rounded-xl transition-all border border-[#2a2a2a]/50 uppercase tracking-widest">
                            View Calendar
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

function StatCard({ label, value, icon: Icon, trend, trendSubtitle, trendColor, warning, warningText, subtitle, subtitleColor }: any) {
    return (
        <div className="bg-[#161616] rounded-2xl border border-[#1f1f1f] p-6 space-y-4">
            <div className="flex items-center justify-between">
                <span className="text-[10px] font-bold text-zinc-500 tracking-widest uppercase">{label}</span>
                <div className="p-2 rounded-lg bg-zinc-900/50">
                    <Icon className="w-5 h-5 text-zinc-400" />
                </div>
            </div>
            <div>
                <div className="text-3xl font-bold text-white tracking-tight">{value}</div>
                <div className="flex items-center gap-1.5 mt-1">
                    {trend ? (
                        <>
                            <span className={cn("text-[11px] font-bold", trendColor)}>{trend}</span>
                            <span className="text-[11px] font-medium text-zinc-600">{trendSubtitle}</span>
                        </>
                    ) : warning ? (
                        <span className="text-[11px] font-bold text-yellow-500 flex items-center gap-1">
                            <span className="w-1 h-1 rounded-full bg-yellow-500" /> {warningText}
                        </span>
                    ) : (
                        <span className={cn("text-[11px] font-bold flex items-center gap-1", subtitleColor)}>
                            <span className={cn("w-1 h-1 rounded-full", subtitleColor.replace('text', 'bg'))} /> {subtitle}
                        </span>
                    )}
                </div>
            </div>
        </div>
    );
}

function StatusBadge({ status }: { status: string }) {
    const styles: any = {
        'DONE': 'bg-green-500/10 text-green-500',
        'IN_PROGRESS': 'bg-orange-500/10 text-orange-500',
        'TODO': 'bg-blue-500/10 text-blue-500',
        'IN_REVIEW': 'bg-purple-500/10 text-purple-500',
        'PENDING': 'bg-zinc-500/10 text-zinc-500',
    };

    return (
        <span className={cn("px-2.5 py-1 rounded-md text-[10px] font-bold uppercase tracking-wider", styles[status] || styles['PENDING'])}>
            {status.replace('_', ' ')}
        </span>
    );
}
