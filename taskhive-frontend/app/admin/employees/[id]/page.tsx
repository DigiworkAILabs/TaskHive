'use client';

import React from 'react';
import { useParams } from 'next/navigation';
import EmployeeDetail from '@/features/employee/components/EmployeeDetail';
import Header from '@/shared/components/Header';

export default function EmployeeDetailPage() {
    const params = useParams();
    const id = params.id as string;

    return (
        <div className="flex-1 flex flex-col h-full bg-[#0a0a0a]">
            <Header
                title="Employee Directory"
                subtitle="Individual profile and performance metrics."
            />
            <div className="flex-1 p-8 overflow-y-auto">
                <EmployeeDetail id={id} />
            </div>
        </div>
    );
}
