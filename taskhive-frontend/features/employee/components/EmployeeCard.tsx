'use client';

import React from 'react';
import { Employee } from '../types/employee.types';
import { cn } from '@/shared/utils/cn';
import { Mail, Phone, Building2, MapPin } from 'lucide-react';
import Link from 'next/link';

interface EmployeeCardProps {
    employee: Employee;
}

export default function EmployeeCard({ employee }: EmployeeCardProps) {
    return (
        <Link href={`/admin/employees/${employee.id}`} className="block">
            <div className="bg-[#161616] border border-[#1f1f1f] rounded-2xl p-6 transition-all hover:border-orange-500/50 hover:shadow-lg hover:shadow-orange-500/5 group">
                <div className="flex items-start justify-between mb-6">
                    <div className="flex items-center gap-4">
                        <div className="w-12 h-12 rounded-full bg-zinc-800 flex items-center justify-center overflow-hidden border-2 border-zinc-800 group-hover:border-orange-500/20 transition-all">
                            {employee.photoUrl ? (
                                <img src={employee.photoUrl} alt={employee.firstName} className="w-full h-full object-cover" />
                            ) : (
                                <span className="text-sm font-bold text-white">{employee.firstName[0]}{employee.lastName[0]}</span>
                            )}
                        </div>
                        <div>
                            <h3 className="text-base font-bold text-white group-hover:text-orange-500 transition-colors uppercase tracking-tight">
                                {employee.firstName} {employee.lastName}
                            </h3>
                            <p className="text-xs text-zinc-500 font-medium">{employee.designation}</p>
                        </div>
                    </div>
                    <span className={cn(
                        "px-2.5 py-1 rounded-md text-[10px] font-bold uppercase tracking-wider",
                        employee.status === 'ACTIVE' ? 'bg-green-500/10 text-green-500' :
                            employee.status === 'INACTIVE' ? 'bg-red-500/10 text-red-500' :
                                'bg-zinc-500/10 text-zinc-500'
                    )}>
                        {employee.status}
                    </span>
                </div>

                <div className="space-y-3">
                    <div className="flex items-center gap-3 text-zinc-400 group-hover:text-zinc-300">
                        <Building2 className="w-3.5 h-3.5" />
                        <span className="text-xs font-medium">{employee.department}</span>
                    </div>
                    <div className="flex items-center gap-3 text-zinc-400 group-hover:text-zinc-300">
                        <Mail className="w-3.5 h-3.5" />
                        <span className="text-xs font-medium truncate">{employee.email}</span>
                    </div>
                    {employee.phone && (
                        <div className="flex items-center gap-3 text-zinc-400 group-hover:text-zinc-300">
                            <Phone className="w-3.5 h-3.5" />
                            <span className="text-xs font-medium">{employee.phone}</span>
                        </div>
                    )}
                </div>

                <div className="mt-6 pt-6 border-t border-[#1f1f1f] flex items-center justify-between">
                    <span className="text-[10px] font-bold text-zinc-600 uppercase tracking-widest">ID: EMP-{employee.id.slice(-4).toUpperCase()}</span>
                    <span className="text-[10px] font-bold text-zinc-500 tracking-tighter">Joined {new Date(employee.joinDate).toLocaleDateString()}</span>
                </div>
            </div>
        </Link>
    );
}
