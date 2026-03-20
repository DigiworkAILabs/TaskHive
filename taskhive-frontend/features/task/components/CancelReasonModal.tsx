'use client';

import React, { useState } from 'react';

interface CancelReasonModalProps {
    isOpen: boolean;
    taskTitle: string;
    onConfirm: (reason: string) => void;
    onClose: () => void;
    isLoading?: boolean;
}

/**
 * P1.3 — Modal that captures a mandatory reason when an admin cancels a task.
 * Cannot confirm without a non-blank reason.
 */
export const CancelReasonModal: React.FC<CancelReasonModalProps> = ({
    isOpen,
    taskTitle,
    onConfirm,
    onClose,
    isLoading = false,
}) => {
    const [reason, setReason] = useState('');
    const [error, setError] = useState<string | null>(null);

    if (!isOpen) return null;

    const handleConfirm = () => {
        if (!reason.trim()) {
            setError('Please provide a reason for cancellation.');
            return;
        }
        onConfirm(reason.trim());
    };

    const handleClose = () => {
        setReason('');
        setError(null);
        onClose();
    };

    return (
        <>
            {/* Backdrop */}
            <div
                style={{
                    position: 'fixed', inset: 0, zIndex: 100,
                    backgroundColor: 'rgba(0,0,0,0.6)',
                    backdropFilter: 'blur(2px)',
                }}
                onClick={handleClose}
            />

            {/* Modal */}
            <div
                style={{
                    position: 'fixed', top: '50%', left: '50%', zIndex: 101,
                    transform: 'translate(-50%, -50%)',
                    backgroundColor: '#161616', border: '1px solid #2a2a2a',
                    borderRadius: '14px', padding: '28px',
                    width: '440px', maxWidth: 'calc(100vw - 32px)',
                    boxShadow: '0 20px 60px rgba(0,0,0,0.7)',
                }}
            >
                <h3 style={{ margin: '0 0 6px', color: '#ffffff', fontSize: '16px', fontWeight: 700 }}>
                    Cancel Task
                </h3>
                <p style={{ margin: '0 0 18px', color: '#a1a1aa', fontSize: '13px' }}>
                    You are cancelling: <strong style={{ color: '#d4d4d8' }}>{taskTitle}</strong>
                </p>

                <label style={{ display: 'block', marginBottom: '6px', fontSize: '12px', color: '#71717a', fontWeight: 600 }}>
                    REASON FOR CANCELLATION *
                </label>
                <textarea
                    value={reason}
                    onChange={(e) => { setReason(e.target.value); setError(null); }}
                    placeholder="e.g. Requirements changed, task is no longer needed..."
                    rows={4}
                    style={{
                        width: '100%', boxSizing: 'border-box',
                        backgroundColor: '#0f0f0f', border: `1px solid ${error ? '#ef4444' : '#2a2a2a'}`,
                        borderRadius: '8px', color: '#ffffff', fontSize: '13px',
                        padding: '10px 12px', resize: 'vertical', outline: 'none',
                        transition: 'border-color 0.15s',
                    }}
                    onFocus={(e) => { e.currentTarget.style.borderColor = '#f59e0b'; }}
                    onBlur={(e) => { e.currentTarget.style.borderColor = error ? '#ef4444' : '#2a2a2a'; }}
                    autoFocus
                />
                {error && <p style={{ marginTop: '6px', fontSize: '12px', color: '#ef4444' }}>{error}</p>}

                <div style={{ display: 'flex', gap: '10px', marginTop: '20px', justifyContent: 'flex-end' }}>
                    <button
                        onClick={handleClose}
                        disabled={isLoading}
                        style={{
                            padding: '9px 18px', borderRadius: '8px',
                            border: '1px solid #2a2a2a', backgroundColor: 'transparent',
                            color: '#71717a', fontSize: '13px', cursor: 'pointer',
                        }}
                    >
                        Go Back
                    </button>
                    <button
                        onClick={handleConfirm}
                        disabled={isLoading}
                        style={{
                            padding: '9px 20px', borderRadius: '8px', border: 'none',
                            backgroundColor: '#ef4444', color: '#ffffff',
                            fontSize: '13px', fontWeight: 600, cursor: isLoading ? 'not-allowed' : 'pointer',
                            opacity: isLoading ? 0.7 : 1, transition: 'opacity 0.15s',
                        }}
                    >
                        {isLoading ? 'Cancelling...' : 'Confirm Cancellation'}
                    </button>
                </div>
            </div>
        </>
    );
};
