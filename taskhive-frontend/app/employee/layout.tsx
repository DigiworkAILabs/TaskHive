'use client';

import React, { useState, useEffect, useRef, useCallback } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/features/auth/store/authStore';
import { authService } from '@/features/auth/services/authService';
import {
    LayoutDashboard, ClipboardList, Settings, LogOut, Menu, X,
} from 'lucide-react';
import { NotificationBell } from '@/features/notification/components/NotificationBell';

// ── Constants ────────────────────────────────────────────────────────────────

const SIDEBAR_EXPANDED_W = 270;
const SIDEBAR_COLLAPSED_W = 72;

// ── Sidebar nav items ───────────────────────────────────────────────────────

const navItems = [
    { label: 'Dashboard', icon: LayoutDashboard, href: '/employee/dashboard', enabled: true },
    { label: 'My Tasks', icon: ClipboardList, href: '/employee/tasks', enabled: true },
    { label: 'Security', icon: Settings, href: '/employee/settings/security', enabled: true },
];

// ── TaskHive Logo ───────────────────────────────────────────────────────────

function Logo({ collapsed }: { collapsed: boolean }) {
    return (
        <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            padding: collapsed ? '0' : '0 8px',
            justifyContent: collapsed ? 'center' : 'flex-start',
            transition: 'all 0.25s ease',
        }}>
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
            <span style={{
                fontSize: '18px',
                fontWeight: 700,
                color: '#ffffff',
                opacity: collapsed ? 0 : 1,
                maxWidth: collapsed ? 0 : '150px',
                overflow: 'hidden',
                whiteSpace: 'nowrap',
                transition: 'opacity 0.3s ease, max-width 0.3s ease',
            }}>
                TaskHive
            </span>
        </div>
    );
}

// ── Tooltip wrapper for collapsed state ─────────────────────────────────────

function NavTooltip({ label, show, children }: { label: string; show: boolean; children: React.ReactNode }) {
    const [hovered, setHovered] = useState(false);

    return (
        <div
            style={{ position: 'relative' }}
            onMouseEnter={() => setHovered(true)}
            onMouseLeave={() => setHovered(false)}
        >
            {children}
            {show && hovered && (
                <div style={{
                    position: 'absolute',
                    left: '100%',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    marginLeft: '12px',
                    padding: '6px 12px',
                    borderRadius: '8px',
                    backgroundColor: '#1f1f1f',
                    border: '1px solid #2a2a2a',
                    color: '#ffffff',
                    fontSize: '13px',
                    fontWeight: 500,
                    whiteSpace: 'nowrap',
                    zIndex: 100,
                    boxShadow: '0 4px 12px rgba(0,0,0,0.4)',
                    pointerEvents: 'none',
                    animation: 'tooltipFadeIn 0.15s ease',
                }}>
                    {label}
                </div>
            )}
        </div>
    );
}

// ── Main Layout ─────────────────────────────────────────────────────────────

export default function EmployeeLayout({ children }: { children: React.ReactNode }) {
    const pathname = usePathname();
    const router = useRouter();
    const user = useAuthStore((s) => s.user);
    const clearAuth = useAuthStore((s) => s.clearAuth);

    const [collapsed, setCollapsed] = useState(false);
    const [mobileOpen, setMobileOpen] = useState(false);
    const [hoverExpanded, setHoverExpanded] = useState(false);
    const [isDesktop, setIsDesktop] = useState(true);
    const sidebarRef = useRef<HTMLElement>(null);
    const hoverTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    const isExpanded = !collapsed || hoverExpanded;
    const sidebarWidth = isExpanded ? SIDEBAR_EXPANDED_W : SIDEBAR_COLLAPSED_W;

    // SSR-safe: detect desktop vs mobile
    useEffect(() => {
        const check = () => setIsDesktop(window.innerWidth >= 768);
        check();
        window.addEventListener('resize', check);
        return () => window.removeEventListener('resize', check);
    }, []);

    // Close mobile sidebar on route change
    useEffect(() => {
        if (!isDesktop) setMobileOpen(false);
    }, [pathname, isDesktop]);

    // Hover expand/collapse
    const handleMouseEnter = useCallback(() => {
        if (!collapsed || !isDesktop) return;
        hoverTimerRef.current = setTimeout(() => setHoverExpanded(true), 200);
    }, [collapsed, isDesktop]);

    const handleMouseLeave = useCallback(() => {
        if (hoverTimerRef.current) clearTimeout(hoverTimerRef.current);
        setHoverExpanded(false);
    }, []);

    const handleLogout = async () => {
        try {
            await authService.logout();
        } catch { /* ignore */ }
        clearAuth();
        router.push('/login');
    };

    const toggleCollapsed = () => {
        setCollapsed((prev) => !prev);
        setHoverExpanded(false);
    };

    return (
        <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: '#0a0a0a' }}>
            {/* ── Global styles ─────────────────────────────────────────── */}
            <style>{`
                @keyframes tooltipFadeIn {
                    from { opacity: 0; transform: translateY(-50%) translateX(-4px); }
                    to   { opacity: 1; transform: translateY(-50%) translateX(0); }
                }
            `}</style>

            {/* ── Skip to Content (WCAG 2.4.1) ─────────────────────────── */}
            <a href="#main-content" className="skip-to-content">
                Skip to content
            </a>

            {/* ── Mobile Overlay ──────────────────────────────────────────── */}
            {mobileOpen && (
                <div
                    onClick={() => setMobileOpen(false)}
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
                ref={sidebarRef}
                onMouseEnter={handleMouseEnter}
                onMouseLeave={handleMouseLeave}
                style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    bottom: 0,
                    zIndex: 50,
                    display: 'flex',
                    flexDirection: 'column',
                    backgroundColor: '#111111',
                    borderRight: '1px solid #1a1a1a',
                    width: isDesktop ? `${sidebarWidth}px` : '270px',
                    padding: isExpanded ? '24px 16px' : '24px 14px',
                    transition: 'width 0.3s cubic-bezier(0.4, 0, 0.2, 1), padding 0.3s cubic-bezier(0.4, 0, 0.2, 1), transform 0.3s ease',
                    transform: !isDesktop && !mobileOpen ? 'translateX(-100%)' : 'translateX(0)',
                    overflowX: 'hidden',
                    willChange: 'width',
                }}
            >
                {/* Close button on mobile */}
                {!isDesktop && (
                    <button
                        onClick={() => setMobileOpen(false)}
                        style={{
                            position: 'absolute',
                            top: '16px',
                            right: '16px',
                            background: 'none',
                            border: 'none',
                            color: '#a1a1aa',
                            cursor: 'pointer',
                        }}
                        aria-label="Close sidebar"
                    >
                        <X size={20} />
                    </button>
                )}

                {/* Logo */}
                <div style={{ marginBottom: '32px' }}>
                    <Logo collapsed={!isExpanded} />
                </div>



                {/* Nav */}
                <nav aria-label="Main navigation" style={{ display: 'flex', flexDirection: 'column', gap: '4px', flex: 1 }}>
                    {navItems.map((item) => {
                        const isActive = pathname === item.href || (item.href !== '/dashboard' && pathname.startsWith(item.href));
                        const Icon = item.icon;

                        const link = (
                            <Link
                                key={item.label}
                                href={item.href}
                                style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '12px',
                                    padding: isExpanded ? '11px 14px' : '11px 0',
                                    borderRadius: '10px',
                                    textDecoration: 'none',
                                    fontSize: '14px',
                                    fontWeight: isActive ? 600 : 400,
                                    color: isActive ? '#ffffff' : '#a1a1aa',
                                    backgroundColor: isActive ? 'rgba(249,115,22,0.12)' : 'transparent',
                                    borderLeft: isExpanded ? (isActive ? '3px solid #f97316' : '3px solid transparent') : 'none',
                                    transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                                    cursor: 'pointer',
                                    justifyContent: isExpanded ? 'flex-start' : 'center',
                                    position: 'relative',
                                    overflow: 'hidden',
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
                                <Icon size={20} color={isActive ? '#f97316' : undefined} style={{ flexShrink: 0 }} />
                                {!isExpanded && isActive && (
                                    <div style={{
                                        position: 'absolute',
                                        left: '4px',
                                        top: '50%',
                                        transform: 'translateY(-50%)',
                                        width: '3px',
                                        height: '16px',
                                        borderRadius: '2px',
                                        backgroundColor: '#f97316',
                                    }} />
                                )}
                                <span style={{
                                    opacity: isExpanded ? 1 : 0,
                                    maxWidth: isExpanded ? '150px' : 0,
                                    overflow: 'hidden',
                                    whiteSpace: 'nowrap',
                                    transition: 'opacity 0.3s ease, max-width 0.3s ease',
                                }}>
                                    {item.label}
                                </span>
                            </Link>
                        );

                        return (
                            <NavTooltip key={item.label} label={item.label} show={!isExpanded}>
                                {link}
                            </NavTooltip>
                        );
                    })}
                </nav>

                {/* User info at bottom */}
                <div
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        gap: isExpanded ? '10px' : '0',
                        padding: isExpanded ? '14px' : '10px',
                        borderRadius: '12px',
                        backgroundColor: 'rgba(255,255,255,0.03)',
                        marginTop: '12px',
                        justifyContent: isExpanded ? 'flex-start' : 'center',
                        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                    }}
                >
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

                    <div style={{
                        flex: 1,
                        minWidth: 0,
                        opacity: isExpanded ? 1 : 0,
                        maxWidth: isExpanded ? '150px' : 0,
                        overflow: 'hidden',
                        transition: 'opacity 0.3s ease, max-width 0.3s ease',
                    }}>
                        <div style={{ fontSize: '13px', fontWeight: 600, color: '#ffffff', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                            {user ? `${user.firstName} ${user.lastName}` : 'Employee'}
                        </div>
                        <div style={{ fontSize: '11px', color: '#52525b' }}>
                            Employee
                        </div>
                    </div>

                    {isExpanded && (
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
                    )}
                </div>
            </aside>

            {/* ── Main Content ─────────────────────────────────────────────── */}
            <div
                style={{
                    flex: 1,
                    display: 'flex',
                    flexDirection: 'column',
                    minWidth: 0,
                    marginLeft: isDesktop ? `${sidebarWidth}px` : 0,
                    transition: 'margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                }}
            >
                {/* Top bar */}
                <header
                    className="flex items-center justify-between px-4 py-3 md:px-8 md:py-4 border-b border-[#1a1a1a] bg-[#0a0a0a] sticky top-0 z-20"
                >
                    <div className="flex items-center gap-3">
                        <button
                            onClick={() => {
                                if (!isDesktop) {
                                    setMobileOpen(!mobileOpen);
                                } else {
                                    toggleCollapsed();
                                }
                            }}
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
