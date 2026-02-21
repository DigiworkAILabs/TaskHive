'use client';

import React, { useState } from 'react';
import { useEmployees } from '../hooks/useEmployees';
import { useDeleteEmployee } from '../hooks/useDeleteEmployee';
import { Edit2, MoreVertical, ChevronLeft, ChevronRight } from 'lucide-react';
import { cn } from '@/shared/utils/cn';

export default function EmployeeList() {
    const [page, setPage] = useState(0);
    const { data, loading, error, refetch } = useEmployees(page);
    const { deleteEmployee } = useDeleteEmployee();

    if (loading) return <div className="p-12 text-center text-zinc-500">Loading directory...</div>;
    if (error) return <div className="p-12 text-center text-red-500">Error: {error}</div>;
    if (!data || !data.content || data.content.length === 0) return <div className="p-12 text-center text-zinc-500">No employees found.</div>;

    return (
        <div>
            <table className="w-full text-left">
                <thead>
                    <tr className="text-[12px] font-bold text-zinc-500 uppercase tracking-widest border-b border-[#1f1f1f]">
                        <th className="px-6 py-5">Employee Name</th>
                        <th className="px-6 py-5">Email Address</th>
                        <th className="px-6 py-5">ID</th>
                        <th className="px-6 py-5">Department</th>
                        <th className="px-6 py-5">Role</th>
                        <th className="px-6 py-5">Status</th>
                        <th className="px-6 py-5 text-center">Actions</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-[#1f1f1f]">
                    {data.content.map((employee) => (
                        <tr key={employee.id} className="group hover:bg-zinc-900/50 transition-colors">
                            <td className="px-6 py-5">
                                <div className="flex items-center gap-4">
                                    <div className="w-10 h-10 rounded-full bg-zinc-800 flex items-center justify-center overflow-hidden border border-zinc-700/50">
                                        {employee.photoUrl ? (
                                            <img src={employee.photoUrl} alt={employee.firstName} className="w-full h-full object-cover" />
                                        ) : (
                                            <span className="text-xs font-bold text-white">{employee.firstName[0]}{employee.lastName[0]}</span>
                                        )}
                                    </div>
                                    <div className="flex flex-col">
                                        <span className="text-sm font-bold text-zinc-100">{employee.firstName} {employee.lastName}</span>
                                        <span className="text-[11px] text-zinc-500 font-medium">Joined {new Date(employee.joinDate).toLocaleDateString('en-US', { month: 'short', year: 'numeric' })}</span>
                                    </div>
                                </div>
                            </td>
                            <td className="px-6 py-5">
                                <span className="text-xs text-zinc-400 font-medium">{employee.email}</span>
                            </td>
                            <td className="px-6 py-5">
                                <span className="text-xs text-zinc-500 font-bold font-mono">EMP-{employee.id.slice(-4).toUpperCase()}</span>
                            </td>
                            <td className="px-6 py-5">
                                <span className={cn(
                                    "px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider",
                                    employee.department === 'Design' ? 'bg-blue-500/10 text-blue-500' :
                                        employee.department === 'Engineering' ? 'bg-purple-500/10 text-purple-500' :
                                            employee.department === 'Marketing' ? 'bg-green-500/10 text-green-500' :
                                                'bg-yellow-500/10 text-yellow-500'
                                )}>
                                    {employee.department}
                                </span>
                            </td>
                            <td className="px-6 py-5">
                                <span className="text-xs text-zinc-300 font-semibold">{employee.designation}</span>
                            </td>
                            <td className="px-6 py-5">
                                <label className="status-toggle">
                                    <input type="checkbox" checked={employee.status === 'ACTIVE'} readOnly />
                                    <span className="toggle-slider"></span>
                                </label>
                            </td>
                            <td className="px-6 py-5 text-center">
                                <div className="flex items-center justify-center gap-2">
                                    <button className="p-2 rounded-lg text-zinc-500 hover:text-white hover:bg-zinc-800 transition-all">
                                        <Edit2 className="w-4 h-4" />
                                    </button>
                                    <button className="p-2 rounded-lg text-zinc-500 hover:text-white hover:bg-zinc-800 transition-all">
                                        <MoreVertical className="w-4 h-4" />
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {/* Pagination Footer */}
            <div className="px-8 py-6 border-t border-[#1f1f1f] flex items-center justify-between">
                <span className="text-xs text-zinc-500 font-medium tracking-tight">
                    Showing <span className="text-zinc-300 font-bold">{page * 10 + 1}</span> to <span className="text-zinc-300 font-bold">{Math.min((page + 1) * 10, data.totalElements)}</span> of <span className="text-zinc-300 font-bold">{data.totalElements}</span> results
                </span>

                <div className="flex items-center gap-2">
                    <button
                        onClick={() => setPage(p => Math.max(0, p - 1))}
                        disabled={page === 0}
                        className="px-4 py-2 rounded-xl bg-[#111111] border border-[#2a2a2a] text-xs font-bold text-zinc-500 hover:text-white disabled:opacity-30 transition-all flex items-center gap-2"
                    >
                        <ChevronLeft className="w-4 h-4" />
                        Previous
                    </button>
                    <button
                        onClick={() => setPage(p => p + 1)}
                        disabled={(page + 1) * 10 >= data.totalElements}
                        className="px-4 py-2 rounded-xl bg-[#111111] border border-[#2a2a2a] text-xs font-bold text-zinc-500 hover:text-white disabled:opacity-30 transition-all flex items-center gap-2"
                    >
                        Next
                        <ChevronRight className="w-4 h-4" />
                    </button>
                </div>
            </div>
        </div>
    );
}
