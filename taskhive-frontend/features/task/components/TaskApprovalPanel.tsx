'use client';

import React, { useState } from 'react';
import { Task } from '../types/task.types';
import { useApproveTask } from '../hooks/useApproveTask';
import { useRejectTask } from '../hooks/useRejectTask';
import { CheckCircle, XCircle } from 'lucide-react';

interface TaskApprovalPanelProps {
    task: Task;
    onApproved: (updatedTask: Task) => void;
    onRejected: (updatedTask: Task) => void;
}

/**
 * P1.2 — Admin approval panel shown for tasks in PENDING_APPROVAL status.
 * Allows admin to approve (→ DONE) or reject with reason (→ IN_REVIEW).
 */
export const TaskApprovalPanel: React.FC<TaskApprovalPanelProps> = ({ task, onApproved, onRejected }) => {
    const { approveTask, isLoading: approving } = useApproveTask();
    const { rejectTask, isLoading: rejecting } = useRejectTask();
    const [rejectionReason, setRejectionReason] = useState('');
    const [showRejectForm, setShowRejectForm] = useState(false);
    const [formError, setFormError] = useState<string | null>(null);

    if (task.status !== 'PENDING_APPROVAL') return null;

    const handleApprove = async () => {
        const updated = await approveTask(task.id);
        if (updated) onApproved(updated);
    };

    const handleReject = async () => {
        if (!rejectionReason.trim()) {
            setFormError('Rejection reason is required.');
            return;
        }
        const updated = await rejectTask(task.id, rejectionReason.trim());
        if (updated) onRejected(updated);
    };

    return (
        <div
            style={{
                marginTop: '24px',
                padding: '18px',
                background: 'rgba(245,158,11,0.06)',
                border: '1px solid rgba(245,158,11,0.25)',
                borderRadius: '12px',
            }}
        >
            <p style={{ margin: '0 0 14px', fontSize: '13px', fontWeight: 600, color: '#f59e0b' }}>
                🕐 Awaiting Your Approval
            </p>

            {!showRejectForm ? (
                <div style={{ display: 'flex', gap: '10px' }}>
                    {/* Approve button */}
                    <button
                        onClick={handleApprove}
                        disabled={approving}
                        style={{
                            display: 'flex', alignItems: 'center', gap: '6px',
                            padding: '8px 16px', borderRadius: '8px', border: 'none',
                            backgroundColor: '#16a34a', color: '#ffffff',
                            fontSize: '13px', fontWeight: 600, cursor: approving ? 'not-allowed' : 'pointer',
                            opacity: approving ? 0.7 : 1, transition: 'opacity 0.15s',
                        }}
                    >
                        <CheckCircle size={14} />
                        {approving ? 'Approving...' : 'Approve'}
                    </button>

                    {/* Reject button */}
                    <button
                        onClick={() => setShowRejectForm(true)}
                        style={{
                            display: 'flex', alignItems: 'center', gap: '6px',
                            padding: '8px 16px', borderRadius: '8px',
                            border: '1px solid rgba(239,68,68,0.4)',
                            backgroundColor: 'transparent', color: '#ef4444',
                            fontSize: '13px', fontWeight: 600, cursor: 'pointer',
                        }}
                    >
                        <XCircle size={14} />
                        Request Revision
                    </button>
                </div>
            ) : (
                <div>
                    <textarea
                        value={rejectionReason}
                        onChange={(e) => { setRejectionReason(e.target.value); setFormError(null); }}
                        placeholder="Describe what needs to be revised..."
                        rows={3}
                        style={{
                            width: '100%', boxSizing: 'border-box',
                            backgroundColor: '#141414', border: '1px solid #2a2a2a',
                            borderRadius: '8px', color: '#ffffff', fontSize: '13px',
                            padding: '10px 12px', resize: 'vertical', outline: 'none',
                            marginBottom: '4px',
                        }}
                    />
                    {formError && (
                        <p style={{ marginBottom: '8px', fontSize: '12px', color: '#ef4444' }}>{formError}</p>
                    )}
                    <div style={{ display: 'flex', gap: '8px', marginTop: '8px' }}>
                        <button
                            onClick={handleReject}
                            disabled={rejecting}
                            style={{
                                padding: '8px 16px', borderRadius: '8px', border: 'none',
                                backgroundColor: '#dc2626', color: '#ffffff',
                                fontSize: '13px', fontWeight: 600, cursor: rejecting ? 'not-allowed' : 'pointer',
                                opacity: rejecting ? 0.7 : 1,
                            }}
                        >
                            {rejecting ? 'Sending...' : 'Send for Revision'}
                        </button>
                        <button
                            onClick={() => { setShowRejectForm(false); setRejectionReason(''); setFormError(null); }}
                            style={{
                                padding: '8px 14px', borderRadius: '8px',
                                border: '1px solid #2a2a2a', backgroundColor: 'transparent',
                                color: '#71717a', fontSize: '13px', cursor: 'pointer',
                            }}
                        >
                            Cancel
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};
