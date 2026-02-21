'use client';

import React from 'react';
import Sidebar from '@/shared/components/Sidebar';
import './admin.css';

export default function AdminLayout({
    children,
}: {
    children: React.ReactNode;
}) {
    return (
        <div className="admin-layout">
            <Sidebar />
            <main className="admin-main">
                {children}
            </main>
        </div>
    );
}
