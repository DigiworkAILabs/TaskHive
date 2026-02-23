'use client';

import React from 'react';
import { TaskPriority } from '../types/task.types';

const priorityConfig: Record<TaskPriority, { label: string; color: string; bg: string }> = {
    LOW: { label: 'Low', color: '#22c55e', bg: 'rgba(34,197,94,0.12)' },
    MEDIUM: { label: 'Medium', color: '#f59e0b', bg: 'rgba(245,158,11,0.12)' },
    HIGH: { label: 'High', color: '#f97316', bg: 'rgba(249,115,22,0.12)' },
    CRITICAL: { label: 'Critical', color: '#ef4444', bg: 'rgba(239,68,68,0.12)' },
};

interface TaskPriorityBadgeProps {
    priority: TaskPriority;
    size?: 'sm' | 'md';
}

export const TaskPriorityBadge: React.FC<TaskPriorityBadgeProps> = ({ priority, size = 'md' }) => {
    const cfg = priorityConfig[priority] || priorityConfig.MEDIUM;
    const fontSize = size === 'sm' ? '11px' : '12px';
    const padding = size === 'sm' ? '3px 8px' : '4px 12px';

    return (
        <span
            style={{
                display: 'inline-flex',
                alignItems: 'center',
                gap: '5px',
                padding,
                borderRadius: '6px',
                fontSize,
                fontWeight: 600,
                backgroundColor: cfg.bg,
                color: cfg.color,
                letterSpacing: '0.3px',
                whiteSpace: 'nowrap',
            }}
        >
            {cfg.label}
        </span>
    );
};
