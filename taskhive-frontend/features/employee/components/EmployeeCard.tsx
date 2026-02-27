'use client';

import React, { useState } from 'react';
import { EmployeeListItem, EmployeeStatus } from '../types/employee.types';
import { MoreVertical, Eye, Pencil, UserCheck, UserX, Trash2 } from 'lucide-react';

interface EmployeeCardProps {
    employee: EmployeeListItem;
    selected: boolean;
    onSelect: (id: string) => void;
    onView: (id: string) => void;
    onEdit: (id: string) => void;
    onActivate: (id: string) => void;
    onDeactivate: (id: string) => void;
    onDelete: (id: string) => void;
}

const statusConfig: Record<EmployeeStatus, { label: string; color: string; bg: string }> = {
    ACTIVE: { label: 'Active', color: '#22c55e', bg: 'rgba(34,197,94,0.12)' },
    INACTIVE: { label: 'Inactive', color: '#ef4444', bg: 'rgba(239,68,68,0.12)' },
    PENDING: { label: 'Pending', color: '#eab308', bg: 'rgba(234,179,8,0.12)' },
};

export const EmployeeCard: React.FC<EmployeeCardProps> = ({
    employee,
    selected,
    onSelect,
    onView,
    onEdit,
    onActivate,
    onDeactivate,
    onDelete,
}) => {
    const [menuOpen, setMenuOpen] = useState(false);
    const initials = `${employee.firstName.charAt(0)}${employee.lastName.charAt(0)}`.toUpperCase();
    const status = statusConfig[employee.status] || statusConfig.PENDING;

    return (
        <tr
            style={{
                borderBottom: '1px solid #1f1f1f',
                transition: 'background-color 0.15s',
            }}
            onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.02)')}
            onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = 'transparent')}
        >
            {/* Employee - Avatar + Name + Email */}
            <td style={{ padding: '16px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer' }}
                    onClick={() => onView(employee.id)}
                >
                    {employee.photoUrl ? (
                        <img
                            src={employee.photoUrl}
                            alt={`${employee.firstName} ${employee.lastName}`}
                            style={{
                                width: '40px',
                                height: '40px',
                                borderRadius: '50%',
                                objectFit: 'cover',
                                border: '2px solid #2a2a2a',
                            }}
                        />
                    ) : (
                        <div
                            style={{
                                width: '40px',
                                height: '40px',
                                borderRadius: '50%',
                                backgroundColor: '#1f1f1f',
                                border: '2px solid #2a2a2a',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                fontSize: '14px',
                                fontWeight: 600,
                                color: '#f97316',
                                flexShrink: 0,
                            }}
                        >
                            {initials}
                        </div>
                    )}
                    <div>
                        <div style={{ fontWeight: 600, color: '#ffffff', fontSize: '14px' }}>
                            {employee.firstName} {employee.lastName}
                        </div>
                        <div style={{ color: '#71717a', fontSize: '12px', marginTop: '2px' }}>
                            {employee.email}
                        </div>
                    </div>
                </div>
            </td>

            {/* Role (Designation) */}
            <td style={{ padding: '16px', color: '#a1a1aa', fontSize: '14px' }}>
                {employee.designation || '—'}
            </td>

            {/* Department */}
            <td style={{ padding: '16px', color: '#a1a1aa', fontSize: '14px' }}>
                {employee.department || '—'}
            </td>

            {/* Status Badge */}
            <td style={{ padding: '16px' }}>
                <span
                    style={{
                        display: 'inline-flex',
                        alignItems: 'center',
                        gap: '6px',
                        padding: '4px 12px',
                        borderRadius: '999px',
                        fontSize: '12px',
                        fontWeight: 500,
                        backgroundColor: status.bg,
                        color: status.color,
                    }}
                >
                    <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: status.color }} />
                    {status.label}
                </span>
            </td>

            {/* Actions */}
            <td style={{ padding: '16px', position: 'relative' }}>
                <button
                    onClick={() => setMenuOpen(!menuOpen)}
                    style={{
                        background: 'none',
                        border: 'none',
                        cursor: 'pointer',
                        color: '#71717a',
                        padding: '4px',
                        borderRadius: '6px',
                        transition: 'color 0.15s',
                    }}
                    onMouseEnter={(e) => (e.currentTarget.style.color = '#ffffff')}
                    onMouseLeave={(e) => (e.currentTarget.style.color = '#71717a')}
                >
                    <MoreVertical size={18} />
                </button>

                {menuOpen && (
                    <>
                        {/* Backdrop to close */}
                        <div
                            style={{ position: 'fixed', inset: 0, zIndex: 40 }}
                            onClick={() => setMenuOpen(false)}
                        />
                        <div
                            style={{
                                position: 'absolute',
                                right: '16px',
                                top: '44px',
                                backgroundColor: '#1a1a1a',
                                border: '1px solid #2a2a2a',
                                borderRadius: '10px',
                                boxShadow: '0 8px 32px rgba(0,0,0,0.5)',
                                zIndex: 50,
                                minWidth: '160px',
                                overflow: 'hidden',
                            }}
                        >
                            <ActionItem icon={<Eye size={14} />} label="View" onClick={() => { setMenuOpen(false); onView(employee.id); }} />
                            {employee.status !== 'ACTIVE' && (
                                <ActionItem icon={<UserCheck size={14} />} label="Activate" onClick={() => { setMenuOpen(false); onActivate(employee.id); }} color="#22c55e" />
                            )}
                            {employee.status === 'ACTIVE' && (
                                <ActionItem icon={<UserX size={14} />} label="Deactivate" onClick={() => { setMenuOpen(false); onDeactivate(employee.id); }} color="#eab308" />
                            )}
                            <div style={{ height: '1px', backgroundColor: '#2a2a2a', margin: '4px 0' }} />
                            <ActionItem icon={<Trash2 size={14} />} label="Delete" onClick={() => { setMenuOpen(false); onDelete(employee.id); }} color="#ef4444" />
                        </div>
                    </>
                )}
            </td>
        </tr>
    );
};

// ── Action menu item ────────────────────────────────────────────────────────

function ActionItem({ icon, label, onClick, color }: { icon: React.ReactNode; label: string; onClick: () => void; color?: string }) {
    return (
        <button
            onClick={onClick}
            style={{
                display: 'flex',
                alignItems: 'center',
                gap: '10px',
                width: '100%',
                padding: '10px 14px',
                border: 'none',
                backgroundColor: 'transparent',
                color: color || '#d4d4d8',
                fontSize: '13px',
                cursor: 'pointer',
                transition: 'background-color 0.15s',
                textAlign: 'left',
            }}
            onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.05)')}
            onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = 'transparent')}
        >
            {icon}
            {label}
        </button>
    );
}
