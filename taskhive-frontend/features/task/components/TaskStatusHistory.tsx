'use client';

import React, { useState, useEffect } from 'react';
import { taskService } from '../services/taskService';
import { TaskStatusHistory as HistoryEntry } from '../types/task.types';
import { TaskStatusBadge } from './TaskStatusBadge';
import { History, Loader2, AlertCircle, ArrowRight } from 'lucide-react';

interface TaskStatusHistoryProps {
    taskId: string;
}

export const TaskStatusHistory: React.FC<TaskStatusHistoryProps> = ({ taskId }) => {
    const [history, setHistory] = useState<HistoryEntry[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!taskId) return;
        setIsLoading(true);
        setError(null);
        taskService.getHistory(taskId)
            .then(setHistory)
            .catch((err: any) => setError(err.response?.data?.message || 'Failed to load history'))
            .finally(() => setIsLoading(false));
    }, [taskId]);

    return (
        <div style={{ backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '14px', padding: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '20px' }}>
                <History size={16} color="#f97316" />
                <h3 style={{ fontSize: '15px', fontWeight: 600, color: '#ffffff', margin: 0 }}>Status History</h3>
            </div>

            {error && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', backgroundColor: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.25)', borderRadius: '8px', padding: '10px 14px', marginBottom: '16px', color: '#f87171', fontSize: '13px' }}>
                    <AlertCircle size={14} />{error}
                </div>
            )}

            {isLoading ? (
                <div style={{ display: 'flex', justifyContent: 'center', padding: '24px' }}>
                    <Loader2 size={24} color="#f97316" className="animate-spin" />
                </div>
            ) : history.length === 0 ? (
                <p style={{ color: '#52525b', fontSize: '13px', textAlign: 'center', padding: '16px 0' }}>No status changes recorded.</p>
            ) : (
                <div style={{ position: 'relative' }}>
                    {/* Vertical timeline line */}
                    <div style={{ position: 'absolute', left: '15px', top: '8px', bottom: '8px', width: '2px', backgroundColor: '#1f1f1f' }} />
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                        {history.map((entry) => {
                            const isRejection = entry.oldStatus === 'PENDING_APPROVAL' && entry.newStatus === 'IN_REVIEW';
                            return (
                                <div key={entry.id} style={{ display: 'flex', gap: '16px', alignItems: 'flex-start' }}>
                                    {/* Dot */}
                                    <div
                                        style={{
                                            width: '30px', height: '30px', borderRadius: '50%', flexShrink: 0,
                                            backgroundColor: isRejection ? 'rgba(249,115,22,0.1)' : '#111111',
                                            border: `2px solid ${isRejection ? 'rgba(249,115,22,0.4)' : '#2a2a2a'}`,
                                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                                            zIndex: 1,
                                        }}
                                    >
                                        <div style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: isRejection ? '#f97316' : '#f97316', opacity: isRejection ? 1 : 0.5 }} />
                                    </div>
                                    {/* Content */}
                                    <div style={{ flex: 1, paddingBottom: '4px' }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap', marginBottom: '4px' }}>
                                            {entry.oldStatus && <TaskStatusBadge status={entry.oldStatus} size="sm" />}
                                            {entry.oldStatus && <ArrowRight size={12} color="#52525b" />}
                                            <TaskStatusBadge status={entry.newStatus} size="sm" />
                                            {isRejection && (
                                                <span style={{ fontSize: '11px', fontWeight: 600, color: '#f97316', backgroundColor: 'rgba(249,115,22,0.1)', padding: '1px 8px', borderRadius: '999px', border: '1px solid rgba(249,115,22,0.2)' }}>
                                                    Revision Requested
                                                </span>
                                            )}
                                        </div>
                                        <div style={{ fontSize: '12px', color: '#71717a' }}>
                                            by <span style={{ color: '#a1a1aa' }}>{entry.changedByName}</span>
                                            {' · '}
                                            {new Date(entry.changedAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' })}
                                        </div>
                                        {entry.comment && (
                                            <div style={{
                                                marginTop: '6px', padding: '8px 12px', borderRadius: '8px',
                                                backgroundColor: isRejection ? 'rgba(249,115,22,0.06)' : '#111111',
                                                border: `1px solid ${isRejection ? 'rgba(249,115,22,0.2)' : '#2a2a2a'}`,
                                                color: isRejection ? '#e4e4e7' : '#a1a1aa',
                                                fontSize: '13px', fontStyle: 'italic',
                                            }}>
                                                "{entry.comment}"
                                            </div>
                                        )}
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                </div>
            )}
        </div>
    );
};
