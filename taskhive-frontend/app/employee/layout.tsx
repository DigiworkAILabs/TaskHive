'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/features/auth/store/authStore';
import { authService } from '@/features/auth/services/authService';
import {
    LayoutDashboard, ClipboardList, Settings, LogOut, Menu, X,
} from 'lucide-react';
import { NotificationBell } from '@/features/notification/components/NotificationBell';

// ── Sidebar nav items ───────────────────────────────────────────────────────

const navItems = [
    { label: 'Dashboard', icon: LayoutDashboard, href: '/employee/dashboard', enabled: true },
    { label: 'My Tasks', icon: ClipboardList, href: '/employee/tasks', enabled: true },
    { label: 'Security', icon: Settings, href: '/employee/settings/security', enabled: true },
];

// ── TaskHive Logo ───────────────────────────────────────────────────────────

function Logo() {
    return (
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '0 8px' }}>
            <div
                style={{
                    width: '36px',
                    height: '36px',
                    borderRadius: '10px',
                    background: 'linear-gradient(135deg, #f97316 0%, #ea6c10 100%)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: '0 4px 12px rgba(249,115,22,0.3)',
                    flexShrink: 0,
                }}
            >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                    <rect x="3" y="3" width="7" height="7" rx="1.5" fill="white" />
                    <rect x="14" y="3" width="7" height="7" rx="1.5" fill="white" opacity="0.6" />
                    <rect x="3" y="14" width="7" height="7" rx="1.5" fill="white" opacity="0.6" />
                    <rect x="14" y="14" width="7" height="7" rx="1.5" fill="white" opacity="0.4" />
                </svg>
            </div>
            <span style={{ fontSize: '18px', fontWeight: 700, color: '#ffffff' }}>TaskHive</span>
        </div>
    );
}

// ── Main Layout ─────────────────────────────────────────────────────────────

export default function EmployeeLayout({ children }: { children: React.ReactNode }) {
    const pathname = usePathname();
    const router = useRouter();
    const user = useAuthStore((s) => s.user);
    const clearAuth = useAuthStore((s) => s.clearAuth);
    const [sidebarOpen, setSidebarOpen] = useState(true);

    // Close sidebar on small screens only on route change
    useEffect(() => {
        if (window.innerWidth < 768) {
            setSidebarOpen(false);
        }
    }, [pathname]);

    // Initialize sidebar based on screen size
    useEffect(() => {
        if (window.innerWidth < 768) {
            setSidebarOpen(false);
        }
    }, []);

    const handleLogout = async () => {
        try {
            await authService.logout();
        } catch { /* ignore */ }
        clearAuth();
        router.push('/login');
    };

    return (
        <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: '#0a0a0a' }}>
            {/* ── Skip to Content (WCAG 2.4.1) ─────────────────────────── */}
            <a href="#main-content" className="skip-to-content">
                Skip to content
            </a>

            {/* ── Mobile Overlay ──────────────────────────────────────────── */}
            {sidebarOpen && (
                <div
                    onClick={() => setSidebarOpen(false)}
                    style={{
                        position: 'fixed',
                        inset: 0,
                        backgroundColor: 'rgba(0,0,0,0.6)',
                        zIndex: 40,
                        backdropFilter: 'blur(2px)',
                    }}
                    className="md:hidden"
                />
            )}

            {/* ── Sidebar ──────────────────────────────────────────────────── */}
            <aside
                className={`
                    fixed top-0 left-0 bottom-0 z-50
                    flex flex-col
                    w-[220px] bg-[#111111] border-r border-[#1a1a1a]
                    p-6 pl-4 pr-4
                    transition-transform duration-300 ease-in-out
                    ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
                `}
            >
                {/* Close button on mobile */}
                <button
                    onClick={() => setSidebarOpen(false)}
                    className="md:hidden absolute top-4 right-4 text-zinc-400 hover:text-white"
                    aria-label="Close sidebar"
                >
                    <X size={20} />
                </button>

                {/* Logo */}
                <div style={{ marginBottom: '36px' }}>
                    <Logo />
                </div>

                {/* Nav */}
                <nav aria-label="Main navigation" style={{ display: 'flex', flexDirection: 'column', gap: '4px', flex: 1 }}>
                    {navItems.map((item) => {
                        const isActive = pathname === item.href || (item.href !== '/dashboard' && pathname.startsWith(item.href));
                        const Icon = item.icon;

                        return (
                            <Link
                                key={item.label}
                                href={item.href}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '12px',
                                    padding: '11px 14px',
                                    borderRadius: '10px',
                                    textDecoration: 'none',
                                    fontSize: '14px',
                                    fontWeight: isActive ? 600 : 400,
                                    color: isActive ? '#ffffff' : '#a1a1aa',
                                    backgroundColor: isActive ? 'rgba(249,115,22,0.12)' : 'transparent',
                                    borderLeft: isActive ? '3px solid #f97316' : '3px solid transparent',
                                    transition: 'all 0.15s',
                                    cursor: 'pointer',
                                }}
                                onMouseEnter={(e) => {
                                    if (!isActive) {
                                        e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.04)';
                                        e.currentTarget.style.color = '#ffffff';
                                    }
                                }}
                                onMouseLeave={(e) => {
                                    if (!isActive) {
                                        e.currentTarget.style.backgroundColor = 'transparent';
                                        e.currentTarget.style.color = '#a1a1aa';
                                    }
                                }}
                            >
                                <Icon size={18} color={isActive ? '#f97316' : undefined} />
                                {item.label}
                            </Link>
                        );
                    })}
                </nav>

                {/* User info at bottom */}
                <div
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: '10px',
                        padding: '14px',
                        borderRadius: '12px',
                        backgroundColor: 'rgba(255,255,255,0.03)',
                        marginTop: '12px',
                    }}
                >
                    {/* Avatar */}
                    <div
                        style={{
                            width: '36px',
                            height: '36px',
                            borderRadius: '50%',
                            backgroundColor: '#1f1f1f',
                            border: '2px solid #2a2a2a',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: '13px',
                            fontWeight: 600,
                            color: '#f97316',
                            flexShrink: 0,
                        }}
                    >
                        {user ? `${user.firstName.charAt(0)}${user.lastName.charAt(0)}` : '?'}
                    </div>
                    <div style={{ flex: 1, minWidth: 0 }}>
                        <div style={{ fontSize: '13px', fontWeight: 600, color: '#ffffff', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                            {user ? `${user.firstName} ${user.lastName}` : 'Employee'}
                        </div>
                        <div style={{ fontSize: '11px', color: '#52525b' }}>
                            Employee
                        </div>
                    </div>

                    {/* Logout */}
                    <button
                        onClick={handleLogout}
                        title="Logout"
                        aria-label="Logout"
                        style={{
                            background: 'none', border: 'none', cursor: 'pointer',
                            color: '#52525b', padding: '4px', transition: 'color 0.15s',
                        }}
                        onMouseEnter={(e) => (e.currentTarget.style.color = '#ef4444')}
                        onMouseLeave={(e) => (e.currentTarget.style.color = '#52525b')}
                    >
                        <LogOut size={16} />
                    </button>
                </div>
            </aside>

            {/* ── Main Content ─────────────────────────────────────────────── */}
            <div
                className={`flex-1 flex flex-col min-w-0 transition-[margin-left] duration-300 ease-in-out ${sidebarOpen ? 'md:ml-[220px]' : 'ml-0'}`}
            >
                {/* Top bar */}
                <header
                    className="flex items-center justify-between px-4 py-3 md:px-8 md:py-4 border-b border-[#1a1a1a] bg-[#0a0a0a] sticky top-0 z-20"
                >
                    {/* Left: Hamburger + Title */}
                    <div className="flex items-center gap-3">
                        <button
                            onClick={() => setSidebarOpen(!sidebarOpen)}
                            className="text-zinc-400 hover:text-white p-1"
                            aria-label="Toggle menu"
                        >
                            <Menu size={22} />
                        </button>
                        <h2 className="text-base md:text-lg font-bold text-white m-0 truncate">
                            {pathname === '/employee/dashboard' ? 'Dashboard' : pathname.includes('/employee/tasks') ? 'My Tasks' : 'Settings'}
                        </h2>
                    </div>

                    <div className="flex items-center gap-2 md:gap-3">
                        <div
                            className="hidden sm:inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full border text-xs font-medium"
                            style={{
                                borderColor: 'rgba(34,197,94,0.25)',
                                color: '#22c55e',
                            }}
                        >
                            <span className="w-1.5 h-1.5 rounded-full bg-green-500" />
                            ONLINE
                        </div>

                        <NotificationBell />
                    </div>
                </header>

                {/* Page content */}
                <main id="main-content" className="flex-1 p-4 md:p-7">
                    {children}
                </main>
            </div>
        </div>
    );
}
