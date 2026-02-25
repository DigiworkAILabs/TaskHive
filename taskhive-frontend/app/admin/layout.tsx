'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/features/auth/store/authStore';
import { authService } from '@/features/auth/services/authService';
import {
    LayoutDashboard, Users, ClipboardList, BarChart3,
    Settings, LogOut, UserPlus,
} from 'lucide-react';
import { NotificationBell } from '@/features/notification/components/NotificationBell';

// ── Sidebar nav items ───────────────────────────────────────────────────────

const navItems = [
    { label: 'Dashboard', icon: LayoutDashboard, href: '/admin/dashboard', enabled: true },
    { label: 'Employees', icon: Users, href: '/admin/employees', enabled: true },
    { label: 'Task', icon: ClipboardList, href: '/admin/tasks', enabled: true },
    { label: 'Reports', icon: BarChart3, href: '#', enabled: false },
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

    const handleLogout = async () => {
        try {
            await authService.logout();
        } catch { /* ignore */ }
        clearAuth();
        router.push('/login');
    };

    return (
        <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: '#0a0a0a' }}>
            {/* ── Sidebar ──────────────────────────────────────────────────── */}
            <aside
                style={{
                    width: '220px',
                    backgroundColor: '#111111',
                    borderRight: '1px solid #1a1a1a',
                    display: 'flex',
                    flexDirection: 'column',
                    padding: '24px 16px',
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    bottom: 0,
                    zIndex: 30,
                }}
            >
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
                                onClick={(e) => !item.enabled && e.preventDefault()}
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

                    {/* Settings + Logout */}
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
            <div style={{ flex: 1, marginLeft: '220px', display: 'flex', flexDirection: 'column' }}>
                {/* Top bar */}
                <header
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        padding: '16px 32px',
                        borderBottom: '1px solid #1a1a1a',
                        backgroundColor: '#0a0a0a',
                        position: 'sticky',
                        top: 0,
                        zIndex: 20,
                    }}
                >
                    {/* Page title determined by pathname */}
                    <h2 style={{ fontSize: '18px', fontWeight: 700, color: '#ffffff', margin: 0 }}>
                        {pathname.includes('/employees/new')
                            ? 'Add New Employee'
                            : pathname.includes('/employees/')
                                ? 'Employee Profile'
                                : pathname.includes('/employees')
                                    ? 'Employee Management'
                                    : 'Dashboard'}
                    </h2>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        {/* System Online badge */}
                        <div
                            style={{
                                display: 'inline-flex', alignItems: 'center', gap: '6px',
                                padding: '6px 14px', borderRadius: '999px',
                                border: '1px solid rgba(34,197,94,0.25)',
                                fontSize: '12px', fontWeight: 500, color: '#22c55e',
                            }}
                        >
                            <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: '#22c55e' }} />
                            SYSTEM ONLINE
                        </div>

                        <NotificationBell />

                        {/* Add New Employee button (only on employees list page) */}
                        {pathname === '/admin/employees' && (
                            <Link
                                href="/admin/employees/new"
                                style={{
                                    display: 'flex', alignItems: 'center', gap: '8px',
                                    padding: '10px 20px', borderRadius: '10px', border: 'none',
                                    background: 'linear-gradient(135deg, #f97316 0%, #ea6c10 100%)',
                                    color: '#ffffff', fontSize: '14px', fontWeight: 600,
                                    textDecoration: 'none',
                                    boxShadow: '0 4px 16px rgba(249,115,22,0.3)',
                                    transition: 'opacity 0.2s',
                                }}
                                onMouseEnter={(e) => (e.currentTarget.style.opacity = '0.88')}
                                onMouseLeave={(e) => (e.currentTarget.style.opacity = '1')}
                            >
                                <UserPlus size={16} />
                                Add New Employee
                            </Link>
                        )}
                    </div>
                </header>

                {/* Page content */}
                <main style={{ flex: 1, padding: '28px 32px' }}>
                    {children}
                </main>
            </div>
        </div>
    );
}
