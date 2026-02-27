'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/features/auth/store/authStore';
import { authService } from '@/features/auth/services/authService';
import {
    LayoutDashboard, Users, ClipboardList, BarChart3,
    Settings, LogOut, UserPlus, Activity, Menu, X,
} from 'lucide-react';
import { NotificationBell } from '@/features/notification/components/NotificationBell';

// ── Sidebar nav items ───────────────────────────────────────────────────────

const navItems = [
    { label: 'Dashboard', icon: LayoutDashboard, href: '/admin/dashboard', enabled: true },
    { label: 'Employees', icon: Users, href: '/admin/employees', enabled: true },
    { label: 'Task', icon: ClipboardList, href: '/admin/tasks', enabled: true },
    { label: 'Audit Logs', icon: Activity, href: '/admin/audit', enabled: true },
    { label: 'Analytics', icon: BarChart3, href: '/admin/analytics', enabled: true },
    { label: 'Settings', icon: Settings, href: '#', enabled: false },
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

export default function AdminLayout({ children }: { children: React.ReactNode }) {
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
                <nav style={{ display: 'flex', flexDirection: 'column', gap: '4px', flex: 1 }}>
                    {navItems.map((item) => {
                        const isActive = pathname.startsWith(item.href) && item.href !== '#';
                        const Icon = item.icon;

                        return (
                            <Link
                                key={item.label}
                                href={item.enabled ? item.href : '#'}
                                onClick={(e) => {
                                    if (!item.enabled) e.preventDefault();
                                }}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '12px',
                                    padding: '11px 14px',
                                    borderRadius: '10px',
                                    textDecoration: 'none',
                                    fontSize: '14px',
                                    fontWeight: isActive ? 600 : 400,
                                    color: !item.enabled ? '#3f3f46' : isActive ? '#ffffff' : '#a1a1aa',
                                    backgroundColor: isActive ? 'rgba(249,115,22,0.12)' : 'transparent',
                                    borderLeft: isActive ? '3px solid #f97316' : '3px solid transparent',
                                    transition: 'all 0.15s',
                                    cursor: item.enabled ? 'pointer' : 'not-allowed',
                                    opacity: item.enabled ? 1 : 0.4,
                                }}
                                onMouseEnter={(e) => {
                                    if (item.enabled && !isActive) {
                                        e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.04)';
                                        e.currentTarget.style.color = '#ffffff';
                                    }
                                }}
                                onMouseLeave={(e) => {
                                    if (item.enabled && !isActive) {
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
                            {user ? `${user.firstName} ${user.lastName}` : 'Admin'}
                        </div>
                        <div style={{ fontSize: '11px', color: '#52525b' }}>
                            {user?.role === 'ADMIN' ? 'System Admin' : 'Employee'}
                        </div>
                    </div>

                    {/* Logout */}
                    <button
                        onClick={handleLogout}
                        title="Logout"
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
                className="flex-1 flex flex-col min-w-0"
                style={{
                    marginLeft: sidebarOpen ? (typeof window !== 'undefined' && window.innerWidth >= 768 ? '220px' : '0') : '0',
                    transition: 'margin-left 0.3s ease-in-out'
                }}
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
                            {pathname.includes('/employees/new')
                                ? 'Add New Employee'
                                : pathname.includes('/employees/')
                                    ? 'Employee Profile'
                                    : pathname.includes('/employees')
                                        ? 'Employee Management'
                                        : pathname.includes('/analytics')
                                            ? 'Analytics & Reports'
                                            : pathname.includes('/audit')
                                                ? 'Audit & Compliance'
                                                : pathname.includes('/tasks')
                                                    ? 'Task Management'
                                                    : 'Dashboard'}
                        </h2>
                    </div>

                    <div className="flex items-center gap-2 md:gap-3">
                        {/* System Online badge — hide on very small screens */}
                        <div
                            className="hidden sm:inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full border text-xs font-medium"
                            style={{
                                borderColor: 'rgba(34,197,94,0.25)',
                                color: '#22c55e',
                            }}
                        >
                            <span className="w-1.5 h-1.5 rounded-full bg-green-500" />
                            SYSTEM ONLINE
                        </div>

                        <NotificationBell />

                        {/* Add New Employee button (only on employees list page) */}
                        {pathname === '/admin/employees' && (
                            <Link
                                href="/admin/employees/new"
                                className="hidden sm:flex items-center gap-2 px-4 py-2.5 rounded-xl text-white text-sm font-semibold no-underline"
                                style={{
                                    background: 'linear-gradient(135deg, #f97316 0%, #ea6c10 100%)',
                                    boxShadow: '0 4px 16px rgba(249,115,22,0.3)',
                                }}
                            >
                                <UserPlus size={16} />
                                <span className="hidden lg:inline">Add New Employee</span>
                                <span className="lg:hidden">Add</span>
                            </Link>
                        )}
                        {/* Mobile FAB for Add Employee */}
                        {pathname === '/admin/employees' && (
                            <Link
                                href="/admin/employees/new"
                                className="sm:hidden flex items-center justify-center w-9 h-9 rounded-full text-white"
                                style={{
                                    background: 'linear-gradient(135deg, #f97316 0%, #ea6c10 100%)',
                                    boxShadow: '0 4px 16px rgba(249,115,22,0.3)',
                                }}
                                aria-label="Add New Employee"
                            >
                                <UserPlus size={16} />
                            </Link>
                        )}
                    </div>
                </header>

                {/* Page content */}
                <main className="flex-1 p-4 md:p-7">
                    {children}
                </main>
            </div>
        </div>
    );
}
