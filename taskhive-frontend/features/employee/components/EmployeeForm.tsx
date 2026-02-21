'use client';

import React from 'react';
import { useForm } from 'react-hook-form';
import { useCreateEmployee } from '../hooks/useCreateEmployee';
import { CreateEmployeeRequest } from '../types/employee.types';
import { User, Mail, Hash, Building2, Briefcase, Plus, X } from 'lucide-react';
import { Button } from '@/features/auth/components/ui/Button';
import { cn } from '@/shared/utils/cn';

interface EmployeeFormProps {
    onSuccess?: () => void;
    onCancel?: () => void;
}

export default function EmployeeForm({ onSuccess, onCancel }: EmployeeFormProps) {
    const { register, handleSubmit, formState: { errors } } = useForm<CreateEmployeeRequest>();
    const { createEmployee, loading, error } = useCreateEmployee();

    const onSubmit = async (data: CreateEmployeeRequest) => {
        try {
            await createEmployee({
                ...data,
                joinDate: new Date().toISOString().split('T')[0], // Simple default for demo
            });
            if (onSuccess) onSuccess();
        } catch (err) {
            // Error is handled by hook
        }
    };

    return (
        <div className="bg-[#161616] rounded-2xl border border-[#1f1f1f] p-8 relative overflow-hidden">
            <div className="absolute top-0 left-0 w-1 h-full bg-orange-500" />

            <div className="flex items-center justify-between mb-8">
                <div className="flex items-center gap-3">
                    <div className="p-2 bg-orange-500/10 rounded-lg">
                        <Plus className="w-5 h-5 text-orange-500" />
                    </div>
                    <h3 className="text-xl font-bold text-white tracking-tight">Register New Employee</h3>
                </div>
                {onCancel && (
                    <button onClick={onCancel} className="p-2 hover:bg-zinc-800 rounded-full text-zinc-500 hover:text-white transition-colors">
                        <X className="w-5 h-5" />
                    </button>
                )}
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="grid grid-cols-3 gap-x-6 gap-y-6">
                <div className="space-y-2">
                    <label className="text-xs font-bold text-zinc-500 uppercase tracking-widest px-1">Full Name</label>
                    <div className="relative group">
                        <User className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-600 group-focus-within:text-orange-500 transition-colors" />
                        <input
                            {...register('firstName', { required: true })}
                            placeholder="Full Name"
                            className="w-full h-12 bg-[#111111] border border-[#2a2a2a] rounded-xl pl-11 pr-4 text-sm text-white focus:outline-none focus:border-orange-500/50 transition-all placeholder:text-zinc-700"
                        />
                    </div>
                </div>

                <div className="space-y-2">
                    <label className="text-xs font-bold text-zinc-500 uppercase tracking-widest px-1">Email Address</label>
                    <div className="relative group">
                        <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-600 group-focus-within:text-orange-500 transition-colors" />
                        <input
                            {...register('email', { required: true, pattern: /^\S+@\S+$/i })}
                            placeholder="email@hive.com"
                            className="w-full h-12 bg-[#111111] border border-[#2a2a2a] rounded-xl pl-11 pr-4 text-sm text-white focus:outline-none focus:border-orange-500/50 transition-all placeholder:text-zinc-700"
                        />
                    </div>
                </div>

                <div className="space-y-2">
                    <label className="text-xs font-bold text-zinc-500 uppercase tracking-widest px-1">Employee ID</label>
                    <div className="relative group">
                        <Hash className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-600 group-focus-within:text-orange-500 transition-colors" />
                        <input
                            placeholder="EMP-001"
                            className="w-full h-12 bg-[#111111] border border-[#2a2a2a] rounded-xl pl-11 pr-4 text-sm text-white focus:outline-none focus:border-orange-500/50 transition-all placeholder:text-zinc-700"
                        />
                    </div>
                </div>

                <div className="space-y-2">
                    <label className="text-xs font-bold text-zinc-500 uppercase tracking-widest px-1">Department</label>
                    <div className="relative group">
                        <Building2 className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-600 group-focus-within:text-orange-500 transition-colors" />
                        <select
                            {...register('department', { required: true })}
                            className="w-full h-12 bg-[#111111] border border-[#2a2a2a] rounded-xl pl-11 pr-4 text-sm text-white focus:outline-none focus:border-orange-500/50 transition-all appearance-none cursor-pointer"
                        >
                            <option value="">Select Department</option>
                            <option value="Engineering">Engineering</option>
                            <option value="Design">Design</option>
                            <option value="Marketing">Marketing</option>
                            <option value="HR">HR</option>
                        </select>
                        <div className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none">
                            <svg className="w-4 h-4 text-zinc-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" /></svg>
                        </div>
                    </div>
                </div>

                <div className="space-y-2">
                    <label className="text-xs font-bold text-zinc-500 uppercase tracking-widest px-1">Role</label>
                    <div className="relative group">
                        <Briefcase className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-600 group-focus-within:text-orange-500 transition-colors" />
                        <select
                            {...register('designation', { required: true })}
                            className="w-full h-12 bg-[#111111] border border-[#2a2a2a] rounded-xl pl-11 pr-4 text-sm text-white focus:outline-none focus:border-orange-500/50 transition-all appearance-none cursor-pointer"
                        >
                            <option value="">Select Role</option>
                            <option value="Lead">Lead</option>
                            <option value="Senior">Senior</option>
                            <option value="Junior">Junior</option>
                            <option value="Intern">Intern</option>
                        </select>
                        <div className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none">
                            <svg className="w-4 h-4 text-zinc-500" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" /></svg>
                        </div>
                    </div>
                </div>

                <div className="flex items-end">
                    <Button
                        type="submit"
                        disabled={loading}
                        className="w-full h-12 rounded-xl flex items-center justify-center gap-2 text-[13px] font-bold uppercase tracking-wider"
                    >
                        {loading ? 'Processing...' : (
                            <>
                                <Plus className="w-4 h-4" />
                                Add Employee
                            </>
                        )}
                    </Button>
                </div>

                {error && <div className="col-span-3 text-red-500 text-xs mt-2 px-1">{error}</div>}
            </form>
        </div>
    );
}
