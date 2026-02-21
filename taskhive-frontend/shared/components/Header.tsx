'use client';

import React from 'react';
import { Bell, Search } from 'lucide-react';

interface HeaderProps {
    title: string;
    subtitle: string;
    searchPlaceholder?: string;
    actionButton?: React.ReactNode;
}

export default function Header({
    title,
    subtitle,
    searchPlaceholder = "Search tasks, employees...",
    actionButton
}: HeaderProps) {
    return (
        <header className="h-[90px] px-8 flex items-center justify-between border-b border-[#1f1f1f] bg-[#0a0a0a]/50 backdrop-blur-md sticky top-0 z-40">
            <div>
                <h1 className="text-2xl font-bold text-white tracking-tight">{title}</h1>
                <p className="text-sm text-zinc-500 mt-0.5">{subtitle}</p>
            </div>

            <div className="flex items-center gap-5">
                <div className="relative group">
                    <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-zinc-500 group-focus-within:text-orange-500 transition-colors" />
                    <input
                        type="text"
                        placeholder={searchPlaceholder}
                        className="w-[280px] h-10 pl-11 pr-4 bg-[#111111] border border-[#2a2a2a] rounded-xl text-sm text-zinc-200 focus:outline-none focus:border-orange-500/50 focus:ring-1 focus:ring-orange-500/20 transition-all placeholder:text-zinc-600"
                    />
                </div>

                <button className="relative p-2.5 rounded-xl bg-[#111111] border border-[#2a2a2a] text-zinc-400 hover:text-white hover:border-zinc-700 transition-all">
                    <Bell className="w-5 h-5" />
                    <span className="absolute top-2.5 right-2.5 w-2 h-2 bg-orange-500 rounded-full border-2 border-[#111111]"></span>
                </button>

                {actionButton}
            </div>
        </header>
    );
}
