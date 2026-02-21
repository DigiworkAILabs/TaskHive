'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
    LayoutDashboard,
    CheckSquare,
    Users,
    BarChart3,
    Settings,
    MoreVertical
} from 'lucide-react';
import { cn } from '@/shared/utils/cn';

const NAV_ITEMS = [
    { label: 'Dashboard', icon: LayoutDashboard, href: '/admin/dashboard' },
    { label: 'MANAGEMENT', section: true },
    { label: 'Tasks', icon: CheckSquare, href: '/admin/tasks', badge: 12 },
    { label: 'Employees', icon: Users, href: '/admin/employees' },
    { label: 'Reports', icon: BarChart3, href: '/admin/reports' },
    { label: 'SETTINGS', section: true },
    { label: 'Configuration', icon: Settings, href: '/admin/configuration' },
];

export default function Sidebar() {
    const pathname = usePathname();

    return (
        <aside className="fixed left-0 top-0 h-screen w-[200px] bg-[#0a0a0a] border-r border-[#1f1f1f] flex flex-col z-50">
            {/* Logo */}
            <div className="p-6 flex items-center gap-3">
                <div className="w-8 h-8 bg-orange-500 rounded-lg flex items-center justify-center text-white font-bold text-lg shadow-lg shadow-orange-500/20">
                    H
                </div>
                <span className="text-white font-semibold text-lg tracking-tight">TaskHive</span>
            </div>

            {/* Navigation */}
            <nav className="flex-1 px-4 py-4 space-y-1">
                {NAV_ITEMS.map((item, index) => {
                    if (item.section) {
                        return (
                            <div key={index} className="pt-4 pb-2 px-3 text-[10px] font-bold text-zinc-500 tracking-wider uppercase">
                                {item.label}
                            </div>
                        );
                    }

                    const isActive = pathname.startsWith(item.href || '');
                    const Icon = item.icon!;

                    return (
                        <Link
                            key={item.href}
                            href={item.href!}
                            className={cn(
                                "flex items-center justify-between px-3 py-2.5 rounded-xl transition-all duration-200 group",
                                isActive
                                    ? "bg-orange-500/10 text-orange-500 shadow-sm"
                                    : "text-zinc-400 hover:text-white hover:bg-zinc-900"
                            )}
                        >
                            <div className="flex items-center gap-3">
                                <Icon className={cn("w-[20px] h-[20px]", isActive ? "text-orange-500" : "text-zinc-500 group-hover:text-zinc-300")} />
                                <span className="text-[13px] font-medium">{item.label}</span>
                            </div>
                            {item.badge && (
                                <span className="px-1.5 py-0.5 rounded-md bg-[#2a2a2a] text-[#f97316] text-[10px] font-bold">
                                    {item.badge}
                                </span>
                            )}
                        </Link>
                    );
                })}
            </nav>

            {/* User Profile */}
            <div className="p-4 border-t border-[#1f1f1f]">
                <div className="flex items-center justify-between p-2 rounded-xl hover:bg-zinc-900 transition-colors cursor-pointer group">
                    <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-full bg-gradient-to-tr from-orange-400 to-orange-600 flex items-center justify-center text-white font-bold text-[13px]">
                            AM
                        </div>
                        <div className="flex flex-col min-w-0">
                            <span className="text-[13px] font-semibold text-white truncate">Alex Morgan</span>
                            <span className="text-[11px] text-zinc-500 truncate">admin@hive.com</span>
                        </div>
                    </div>
                    <MoreVertical className="w-4 h-4 text-zinc-500 group-hover:text-white" />
                </div>
            </div>
        </aside>
    );
}
