'use client';

import React from 'react';
import { TaskStatus } from '../types/task.types';

const statusConfig: Record<TaskStatus, { label: string; color: string; bg: string; dot: string }> = {
    TODO: { label: 'To Do', color: '#a1a1aa', bg: 'rgba(161,161,170,0.12)', dot: '#a1a1aa' },
    IN_PROGRESS: { label: 'In Progress', color: '#3b82f6', bg: 'rgba(59,130,246,0.12)', dot: '#3b82f6' },
    IN_REVIEW: { label: 'In Review', color: '#a855f7', bg: 'rgba(168,85,247,0.12)', dot: '#a855f7' },
    PENDING_APPROVAL: { label: 'Pending Approval', color: '#f59e0b', bg: 'rgba(245,158,11,0.12)', dot: '#f59e0b' },
    DONE: { label: 'Done', color: '#22c55e', bg: 'rgba(34,197,94,0.12)', dot: '#22c55e' },
    CANCELLED: { label: 'Cancelled', color: '#ef4444', bg: 'rgba(239,68,68,0.12)', dot: '#ef4444' },
};

interface TaskStatusBadgeProps {
    status: TaskStatus;
    size?: 'sm' | 'md';
}

export const TaskStatusBadge: React.FC<TaskStatusBadgeProps> = ({ status, size = 'md' }) => {
    const cfg = statusConfig[status] || statusConfig.TODO;
    const fontSize = size === 'sm' ? '11px' : '12px';
    const padding = size === 'sm' ? '3px 8px' : '4px 12px';

    return (
        <span
            style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '5px',
                padding,
                borderRadius: '999px',
                fontSize,
                fontWeight: 500,
                backgroundColor: cfg.bg,
                color: cfg.color,
                whiteSpace: 'nowrap',
            }}
        >
            <span style={{ width: '6px', height: '6px', borderRadius: '50%', backgroundColor: cfg.dot, flexShrink: 0 }} />
            {cfg.label}
        </span>
    );
};
