'use client';

import React, { useState } from 'react';
import Header from '@/shared/components/Header';
import EmployeeForm from './EmployeeForm';
import EmployeeList from './EmployeeList';
import { Plus } from 'lucide-react';
import { Button } from '@/features/auth/components/ui/Button';

export default function EmployeeManagementView() {
    const [showForm, setShowForm] = useState(false);
    const [refreshKey, setRefreshKey] = useState(0);

    const handleEmployeeCreated = () => {
        setShowForm(false);
        setRefreshKey(prev => prev + 1);
    };

    return (
        <div className="flex-1 flex flex-col h-full bg-[#0a0a0a]">
            <Header
                title="Employee Management"
                subtitle="Manage directory, access roles, and status."
                actionButton={
                    !showForm && (
                        <Button
                            onClick={() => setShowForm(true)}
                            className="flex items-center gap-2 px-5 rounded-xl"
                        >
                            <Plus className="w-4 h-4" />
                            Add Employee
                        </Button>
                    )
                }
            />

            <div className="flex-1 p-8 space-y-8 overflow-y-auto">
                {showForm && (
                    <EmployeeForm
                        onSuccess={handleEmployeeCreated}
                        onCancel={() => setShowForm(false)}
                    />
                )}

                <div className="bg-[#161616] rounded-2xl border border-[#1f1f1f] shadow-sm">
                    <div className="px-6 py-5 border-b border-[#1f1f1f] flex items-center justify-between">
                        <h3 className="text-lg font-semibold text-white">Directory List</h3>
                        <div className="flex items-center gap-3">
                            <button className="px-4 py-2 bg-[#2a2a2a]/40 hover:bg-[#2a2a2a]/60 text-zinc-400 hover:text-white text-xs font-bold rounded-xl transition-all border border-[#2a2a2a]/50 flex items-center gap-2 uppercase tracking-widest">
                                <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><path d="M22 3H2l8 9v7l4 3v-10L22 3z" /></svg>
                                Filter
                            </button>
                            <button className="px-4 py-2 bg-[#2a2a2a]/40 hover:bg-[#2a2a2a]/60 text-zinc-400 hover:text-white text-xs font-bold rounded-xl transition-all border border-[#2a2a2a]/50 flex items-center gap-2 uppercase tracking-widest">
                                <svg className="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3" /></svg>
                                Export
                            </button>
                        </div>
                    </div>
                    <EmployeeList key={refreshKey} />
                </div>
            </div>
        </div>
    );
}
