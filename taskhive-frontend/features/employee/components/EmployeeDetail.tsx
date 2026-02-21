'use client';

import React from 'react';
import { useEmployee } from '../hooks/useEmployee';
import { useUpdateEmployee } from '../hooks/useUpdateEmployee';
import { Employee } from '../types/employee.types';
import {
    Building2,
    Mail,
    Phone,
    Calendar,
    User,
    ShieldCheck,
    ArrowLeft,
    Edit2
} from 'lucide-react';
import { Button } from '@/features/auth/components/ui/Button';
import ProfilePhotoUpload from './ProfilePhotoUpload';
import Link from 'next/link';

interface EmployeeDetailProps {
    id: string;
}

export default function EmployeeDetail({ id }: EmployeeDetailProps) {
    const { data: employee, loading, error, refetch } = useEmployee(id);
    const { updateEmployee, loading: updating } = useUpdateEmployee();

    if (loading) return <div className="p-12 text-zinc-500">Loading employee details...</div>;
    if (error) return <div className="p-12 text-red-500">Error: {error}</div>;
    if (!employee) return <div className="p-12 text-zinc-500">Employee not found.</div>;

    return (
        <div className="space-y-8">
            {/* Breadcrumb & Actions */}
            <div className="flex items-center justify-between">
                <Link
                    href="/admin/employees"
                    className="flex items-center gap-2 text-zinc-500 hover:text-white transition-colors group"
                >
                    <div className="p-1.5 rounded-lg bg-zinc-900 group-hover:bg-zinc-800 transition-colors">
                        <ArrowLeft className="w-4 h-4" />
                    </div>
                    <span className="text-sm font-semibold uppercase tracking-widest">Back to Directory</span>
                </Link>
                <Button className="flex items-center gap-2 rounded-xl px-6">
                    <Edit2 className="w-4 h-4" />
                    Edit Profile
                </Button>
            </div>

            <div className="grid grid-cols-3 gap-8">
                {/* Left Column: Profile Card */}
                <div className="col-span-1 space-y-6">
                    <div className="bg-[#161616] border border-[#1f1f1f] rounded-2xl p-8 text-center relative overflow-hidden">
                        <div className="absolute top-0 left-0 w-full h-24 bg-gradient-to-br from-orange-500/20 to-transparent" />

                        <div className="relative mb-6">
                            <ProfilePhotoUpload
                                employeeId={employee.id}
                                currentPhotoUrl={employee.photoUrl}
                                onUploadSuccess={refetch}
                            />
                        </div>

                        <h2 className="text-2xl font-bold text-white mb-1 uppercase tracking-tight">{employee.firstName} {employee.lastName}</h2>
                        <p className="text-sm font-medium text-orange-500 uppercase tracking-widest mb-6">{employee.designation}</p>

                        <div className="flex items-center justify-center gap-4 py-4 border-y border-[#1f1f1f]">
                            <div className="text-center px-4">
                                <span className="block text-xl font-bold text-zinc-100 italic">4.8</span>
                                <span className="text-[10px] font-bold text-zinc-500 uppercase tracking-tighter">Rating</span>
                            </div>
                            <div className="w-px h-8 bg-[#1f1f1f]" />
                            <div className="text-center px-4">
                                <span className="block text-xl font-bold text-zinc-100 italic">156</span>
                                <span className="text-[10px] font-bold text-zinc-500 uppercase tracking-tighter">Tasks</span>
                            </div>
                        </div>

                        <div className="mt-8 space-y-4 text-left">
                            <InfoRow icon={Building2} label="Department" value={employee.department} />
                            <InfoRow icon={Mail} label="Email" value={employee.email} />
                            <InfoRow icon={Phone} label="Phone" value={employee.phone || 'Not provided'} />
                            <InfoRow icon={Calendar} label="Join Date" value={new Date(employee.joinDate).toLocaleDateString()} />
                        </div>
                    </div>
                </div>

                {/* Right Column: Detailed Info & Activities */}
                <div className="col-span-2 space-y-8">
                    <div className="bg-[#161616] border border-[#1f1f1f] rounded-2xl p-8">
                        <h3 className="text-lg font-bold text-white mb-8 flex items-center gap-2">
                            <ShieldCheck className="w-5 h-5 text-orange-500" />
                            Employment Information
                        </h3>

                        <div className="grid grid-cols-2 gap-x-12 gap-y-8">
                            <DetailBlock label="Employee ID" value={`EMP-${employee.id.slice(-4).toUpperCase()}`} />
                            <DetailBlock label="Current Status" value={employee.status} isStatus />
                            <DetailBlock label="Direct Manager" value={employee.managerName || 'None assigned'} />
                            <DetailBlock label="Account Type" value="Employee Access" />
                            <DetailBlock label="Last Updated" value={new Date(employee.updatedAt).toLocaleString()} />
                            <DetailBlock label="Created By" value={employee.createdBy || 'System'} />
                        </div>
                    </div>

                    <div className="bg-[#161616] border border-[#1f1f1f] rounded-2xl p-8">
                        <h3 className="text-lg font-bold text-white mb-6">Recent Activity</h3>
                        <div className="space-y-6">
                            {[1, 2, 3].map(i => (
                                <div key={i} className="flex gap-4 p-4 rounded-xl bg-[#0a0a0a]/50 border border-[#1f1f1f]">
                                    <div className="w-10 h-10 rounded-full bg-zinc-800 flex items-center justify-center shrink-0">
                                        <Calendar className="w-4 h-4 text-zinc-500" />
                                    </div>
                                    <div>
                                        <h4 className="text-sm font-semibold text-zinc-200">Completed task "Redesign Dashboard UI"</h4>
                                        <p className="text-[11px] text-zinc-500 mt-1">2 hours ago • Project: Lumina Revamp</p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

function InfoRow({ icon: Icon, label, value }: { icon: any, label: string, value: string }) {
    return (
        <div className="flex items-center gap-3">
            <div className="p-1.5 rounded-lg bg-zinc-900">
                <Icon className="w-3.5 h-3.5 text-zinc-500" />
            </div>
            <div className="min-w-0">
                <span className="block text-[10px] font-bold text-zinc-600 uppercase tracking-widest">{label}</span>
                <span className="block text-xs text-zinc-300 font-medium truncate">{value}</span>
            </div>
        </div>
    );
}

function DetailBlock({ label, value, isStatus }: { label: string, value: string, isStatus?: boolean }) {
    return (
        <div className="space-y-1.5">
            <span className="block text-[11px] font-bold text-zinc-500 uppercase tracking-widest">{label}</span>
            {isStatus ? (
                <span className="inline-block px-3 py-1 bg-green-500/10 text-green-500 text-[10px] font-black rounded-lg uppercase tracking-widest">
                    {value}
                </span>
            ) : (
                <span className="block text-sm font-bold text-zinc-100 tracking-tight">{value}</span>
            )}
        </div>
    );
}
