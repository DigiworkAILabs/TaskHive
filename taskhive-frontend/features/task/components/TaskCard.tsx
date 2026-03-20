'use client';

import React, { useState } from 'react';
import { TaskListItem } from '../types/task.types';
import { TaskStatusBadge } from './TaskStatusBadge';
import { TaskPriorityBadge } from './TaskPriorityBadge';
import { MoreVertical, Eye, Pencil, Trash2, CheckSquare } from 'lucide-react';

interface TaskCardProps {
    task: TaskListItem;
    onView: (id: string) => void;
    onEdit: (id: string) => void;
    onDelete: (id: string) => void;
    selected: boolean;
    onSelect: (id: string) => void;
    hideAssignedTo?: boolean;
}

export const TaskCard: React.FC<TaskCardProps> = ({ task, onView, onEdit, onDelete, selected, onSelect, hideAssignedTo = false }) => {
    const [menuOpen, setMenuOpen] = useState(false);

    const isOverdue = !['DONE', 'CANCELLED'].includes(task.status) && new Date(task.dueDate) < new Date();
    const dueDateFormatted = task.dueDate
        ? new Date(task.dueDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
        : '—';

    const initials = task.assigneeName
        ? task.assigneeName.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2)
        : '?';

    return (
        <tr
            style={{ borderBottom: '1px solid #1f1f1f', transition: 'background-color 0.15s' }}
            onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.02)')}
            onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = 'transparent')}
        >

            {/* Title + Tags */}
            <td style={{ padding: '14px 16px', maxWidth: '300px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                    <span
                        style={{ fontWeight: 600, color: '#ffffff', fontSize: '14px', cursor: 'pointer' }}
                        onClick={() => onView(task.id)}
                    >
                        {task.title}
                    </span>
                    {task.isLate && (
                        <span
                            style={{
                                fontSize: '10px',
                                fontWeight: 700,
                                color: '#ffffff',
                                backgroundColor: '#ef4444',
                                borderRadius: '4px',
                                padding: '1px 6px',
                                letterSpacing: '0.5px',
                                flexShrink: 0,
                            }}
                        >
                            LATE
                        </span>
                    )}
                </div>
                {task.tags && task.tags.length > 0 && (
                    <div style={{ display: 'flex', gap: '4px', flexWrap: 'wrap' }}>
                        {task.tags.slice(0, 3).map((tag) => (
                            <span
                                key={tag}
                                style={{
                                    padding: '2px 8px',
                                    borderRadius: '4px',
                                    fontSize: '11px',
                                    backgroundColor: 'rgba(249,115,22,0.08)',
                                    color: '#f97316',
                                    border: '1px solid rgba(249,115,22,0.15)',
                                }}
                            >
                                {tag}
                            </span>
                        ))}
                    </div>
                )}
            </td>

            {/* Priority */}
            <td style={{ padding: '14px 16px' }}>
                <TaskPriorityBadge priority={task.priority} />
            </td>

            {/* Status */}
            <td style={{ padding: '14px 16px' }}>
                <TaskStatusBadge status={task.status} />
            </td>

            {/* Assigned To */}
            {!hideAssignedTo && (
                <td style={{ padding: '14px 16px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <div
                            style={{
                                width: '30px', height: '30px', borderRadius: '50%',
                                backgroundColor: '#1f1f1f', border: '2px solid #2a2a2a',
                                display: 'flex', alignItems: 'center', justifyContent: 'center',
                                fontSize: '11px', fontWeight: 700, color: '#f97316', flexShrink: 0,
                            }}
                        >
                            {initials}
                        </div>
                        <span style={{ color: '#a1a1aa', fontSize: '13px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '120px' }}>
                            {task.assigneeName || '—'}
                        </span>
                    </div>
                </td>
            )}

            {/* Due Date */}
            <td style={{ padding: '14px 16px' }}>
                <span style={{ fontSize: '13px', color: isOverdue ? '#ef4444' : '#a1a1aa', fontWeight: isOverdue ? 600 : 400 }}>
                    {dueDateFormatted}
                    {isOverdue && <span style={{ marginLeft: '4px', fontSize: '11px' }}>⚠</span>}
                </span>
            </td>

            {/* Actions */}
            <td style={{ padding: '14px 16px', position: 'relative' }}>
                <button
                    onClick={() => setMenuOpen(!menuOpen)}
                    style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#71717a', padding: '4px', borderRadius: '6px', transition: 'color 0.15s' }}
                    onMouseEnter={(e) => (e.currentTarget.style.color = '#ffffff')}
                    onMouseLeave={(e) => (e.currentTarget.style.color = '#71717a')}
                >
                    <MoreVertical size={18} />
                </button>

                {menuOpen && (
                    <>
                        <div style={{ position: 'fixed', inset: 0, zIndex: 40 }} onClick={() => setMenuOpen(false)} />
                        <div
                            style={{
                                position: 'absolute', right: '16px', top: '44px',
                                backgroundColor: '#1a1a1a', border: '1px solid #2a2a2a',
                                borderRadius: '10px', boxShadow: '0 8px 32px rgba(0,0,0,0.5)',
                                zIndex: 50, minWidth: '150px', overflow: 'hidden',
                            }}
                        >
                            <ActionItem icon={<Eye size={14} />} label="View" onClick={() => { setMenuOpen(false); onView(task.id); }} />
                            <div style={{ height: '1px', backgroundColor: '#2a2a2a', margin: '4px 0' }} />
                            <ActionItem icon={<Trash2 size={14} />} label="Delete" onClick={() => { setMenuOpen(false); onDelete(task.id); }} color="#ef4444" />
                        </div>
                    </>
                )}
            </td>
        </tr>
    );
};

function ActionItem({ icon, label, onClick, color }: { icon: React.ReactNode; label: string; onClick: () => void; color?: string }) {
    return (
        <button
            onClick={onClick}
            style={{
                display: 'flex', alignItems: 'center', gap: '10px', width: '100%',
                padding: '10px 14px', border: 'none', backgroundColor: 'transparent',
                color: color || '#d4d4d8', fontSize: '13px', cursor: 'pointer', transition: 'background-color 0.15s', textAlign: 'left',
            }}
            onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.05)')}
            onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = 'transparent')}
        >
            {icon}{label}
        </button>
    );
}
