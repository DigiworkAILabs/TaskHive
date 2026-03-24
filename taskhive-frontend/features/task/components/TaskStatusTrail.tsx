'use client';

import React from 'react';
import { TaskStatus } from '../types/task.types';

// Stages — colors removed here; fill color is unified per STATUS_FILL_COLOR below
const STAGES: { status: TaskStatus; label: string; blocks: number }[] = [
    { status: 'TODO',              label: 'To Do',           blocks: 2 },
    { status: 'IN_PROGRESS',      label: 'In Progress',     blocks: 3 },
    { status: 'IN_REVIEW',        label: 'In Review',       blocks: 3 },
    { status: 'PENDING_APPROVAL', label: 'Pending Approval',blocks: 3 },
    { status: 'DONE',             label: 'Done',            blocks: 3 },
];

// Single fill color — whole bar uses the color of the current status
const STATUS_FILL_COLOR: Partial<Record<TaskStatus, string>> = {
    TODO:              '#FF2200',   // traffic red
    IN_PROGRESS:       '#fcf005ff',   // bright traffic amber
    IN_REVIEW:         '#1E90FF',   // bright dodger blue
    PENDING_APPROVAL:  '#FF6600',   // deep orange
    DONE:              '#00DD00',   // traffic green
    CANCELLED:         '#3f3f46',
};

// Glow color per status
const STATUS_GLOW_COLOR: Partial<Record<TaskStatus, string>> = {
    TODO:              'rgba(255,34,0,0.55)',
    IN_PROGRESS:       'rgba(255,240,31,0.55)',
    IN_REVIEW:         'rgba(30,144,255,0.6)',
    PENDING_APPROVAL:  'rgba(255,102,0,0.5)',
    DONE:              'rgba(0,221,0,0.5)',
    CANCELLED:         'transparent',
};

const STATUS_INDEX: Partial<Record<TaskStatus, number>> = {
    TODO: 0, IN_PROGRESS: 1, IN_REVIEW: 2, PENDING_APPROVAL: 3, DONE: 4,
};

interface TaskStatusTrailProps {
    status: TaskStatus;
}

export const TaskStatusTrail: React.FC<TaskStatusTrailProps> = ({ status }) => {
    const isCancelled = status === 'CANCELLED';
    const isDone      = status === 'DONE';
    const currentIndex = isCancelled ? -1 : (STATUS_INDEX[status] ?? 0);

    // Single fill color for ALL filled/current blocks
    const fillColor  = STATUS_FILL_COLOR[status] ?? '#3f3f46';
    const borderColor = fillColor;
    const glowColor  = STATUS_GLOW_COLOR[status] ?? 'transparent';

    return (
        <>
            <style>{`
                @keyframes blockBlink {
                    0%, 49% { opacity: 1; }
                    50%, 100% { opacity: 0.25; }
                }
                @keyframes blockFillIn {
                    from { transform: scaleX(0); }
                    to   { transform: scaleX(1); }
                }
            `}</style>

            <div style={{ marginBottom: '20px' }}>
                {/* Header row */}
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '8px' }}>
                    <span style={{
                        fontSize: '9px', letterSpacing: '2px', textTransform: 'uppercase',
                        color: '#3f3f46', fontFamily: 'monospace', fontWeight: 700,
                    }}>
                        PROGRESS
                    </span>
                    {isCancelled && (
                        <span style={{
                            fontSize: '9px', letterSpacing: '2px', fontFamily: 'monospace',
                            fontWeight: 700, color: '#ef4444', textTransform: 'uppercase',
                            padding: '2px 8px',
                            border: '1px solid rgba(239,68,68,0.4)',
                            backgroundColor: 'rgba(239,68,68,0.08)',
                        }}>
                            ✕ CANCELLED
                        </span>
                    )}
                </div>

                {/* Battery shell */}
                <div style={{ display: 'flex', alignItems: 'stretch' }}>

                    {/* Left nub — takes unified fill color */}
                    <div style={{
                        width: '6px', alignSelf: 'center', height: '14px', flexShrink: 0,
                        backgroundColor: isCancelled ? '#2a2a2a' : fillColor,
                        transition: 'background-color 0.3s',
                    }} />

                    {/* Outer casing */}
                    <div style={{
                        flex: 1, display: 'flex', gap: '4px',
                        border: `2px solid ${isCancelled ? '#2a2a2a' : '#3f3f46'}`,
                        padding: '3px',
                        backgroundColor: '#0a0a0a',
                        position: 'relative',
                        overflow: 'hidden',
                        transition: 'border-color 0.3s',
                    }}>
                        {/* Scanline texture */}
                        <div style={{
                            position: 'absolute', inset: 0, zIndex: 3, pointerEvents: 'none',
                            backgroundImage: 'repeating-linear-gradient(transparent, transparent 1px, rgba(0,0,0,0.22) 1px, rgba(0,0,0,0.22) 2px)',
                            backgroundSize: '100% 2px',
                        }} />

                        {STAGES.map((stage, stageIdx) => {
                            const isCompleted = !isCancelled && stageIdx < currentIndex;
                            const isCurrent   = !isCancelled && stageIdx === currentIndex;
                            const isFuture    = isCancelled || stageIdx > currentIndex;

                            return (
                                <React.Fragment key={stage.status}>
                                    {/* Gap divider between stage groups (except before first) */}
                                    {stageIdx > 0 && (
                                        <div style={{ width: '2px', flexShrink: 0, backgroundColor: '#1a1a1a', alignSelf: 'stretch' }} />
                                    )}

                                    {/* Stage group: N blocks */}
                                    <div style={{ flex: stage.blocks, display: 'flex', gap: '2px' }}>
                                        {Array.from({ length: stage.blocks }).map((_, blockIdx) => {
                                            const delay = stageIdx * 0.06 + blockIdx * 0.04;

                                            return (
                                                <div
                                                    key={blockIdx}
                                                    style={{
                                                        flex: 1, height: '14px',
                                                        position: 'relative', overflow: 'hidden',
                                                        border: isFuture
                                                            ? '1px solid #1f1f1f'
                                                            : `1px solid ${borderColor}`,
                                                        backgroundColor: isFuture ? 'transparent' : fillColor,
                                                        boxShadow: !isFuture ? `0 0 6px 1px ${glowColor}` : undefined,
                                                        animation: undefined,
                                                        opacity: (isCurrent && blockIdx === stage.blocks - 1) ? 0.55 : 1,
                                                        transition: 'border-color 0.2s, background-color 0.2s',
                                                    }}
                                                >
                                                    {/* Fill-in sweep for completed blocks */}
                                                    {isCompleted && (
                                                        <div style={{
                                                            position: 'absolute', inset: 0,
                                                            backgroundColor: fillColor,
                                                            transformOrigin: 'left',
                                                            animation: `blockFillIn 0.25s ease ${delay}s both`,
                                                        }} />
                                                    )}

                                                    {/* Inner highlight line (top edge) */}
                                                    {!isFuture && (
                                                        <div style={{
                                                            position: 'absolute', top: 0, left: 0, right: 0,
                                                            height: '2px',
                                                            backgroundColor: 'rgba(255,255,255,0.15)',
                                                            zIndex: 1,
                                                        }} />
                                                    )}
                                                </div>
                                            );
                                        })}
                                    </div>
                                </React.Fragment>
                            );
                        })}
                    </div>

                    {/* Right nub (positive terminal) */}
                    <div style={{
                        width: '4px', alignSelf: 'center', height: '10px', flexShrink: 0,
                        backgroundColor: '#1f1f1f',
                        border: '1px solid #2a2a2a',
                        borderLeft: 'none',
                    }} />
                </div>

                {/* Stage labels */}
                <div style={{ display: 'flex', marginTop: '5px', paddingLeft: '7px', paddingRight: '5px' }}>
                    {STAGES.map((stage, idx) => {
                        const isCompleted = !isCancelled && idx < currentIndex;
                        const isCurrent   = !isCancelled && idx === currentIndex;
                        // Proportional flex matching block count
                        const flexVal = stage.blocks + (idx > 0 ? 0.15 : 0); // account for divider gap

                        return (
                            <div key={stage.status} style={{ flex: flexVal, textAlign: 'center' }}>
                                <span style={{
                                    fontSize: '11px',
                                    fontFamily: 'monospace',
                                    letterSpacing: '0.2px',
                                    textTransform: 'uppercase',
                                    fontWeight: isCurrent ? 700 : 400,
                                    color: isCurrent
                                        ? fillColor
                                        : isCompleted
                                            ? '#3f3f46'
                                            : '#1f1f1f',
                                }}>
                                    {stage.label}
                                </span>
                            </div>
                        );
                    })}
                </div>
            </div>
        </>
    );
};
