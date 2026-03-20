'use client';

import React, { useRef, useState } from 'react';
import { AttachmentPurpose, TaskAttachment } from '../types/task.types';
import { taskService } from '../services/taskService';
import {
    Upload, CheckCircle, AlertCircle, FileText,
    ExternalLink, ShieldCheck, Lock,
} from 'lucide-react';

interface ProofUploadSectionProps {
    taskId: string;
    attachments: TaskAttachment[];      // full attachment list from parent
    onUploadSuccess: () => void;        // triggers parent re-fetch
    isAdmin?: boolean;                  // admin sees list only — no upload button
    taskDone?: boolean;                 // when DONE/CANCELLED hide upload
}

/** Format bytes to KB / MB */
function formatSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

/**
 * P1.1 — Proof section shown when a task has proofRequired=true.
 *
 * - EMPLOYEE: can upload proof + sees list of uploaded proofs with status badge
 * - ADMIN: sees proof list with download links only — NO upload button
 */
export const ProofUploadSection: React.FC<ProofUploadSectionProps> = ({
    taskId,
    attachments,
    onUploadSuccess,
    isAdmin = false,
    taskDone = false,
}) => {
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [isUploading, setIsUploading] = useState(false);
    const [uploadError, setUploadError] = useState<string | null>(null);
    const [uploadSuccess, setUploadSuccess] = useState(false);

    // Filter only PROOF attachments
    const proofAttachments = attachments.filter(
        (a) => a.attachmentPurpose === 'PROOF'
    );
    const hasProof = proofAttachments.length > 0;

    const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;

        setIsUploading(true);
        setUploadError(null);
        setUploadSuccess(false);

        try {
            await taskService.uploadAttachmentWithPurpose(taskId, file, 'PROOF');
            setUploadSuccess(true);
            onUploadSuccess();
            // Clear success message after 3s
            setTimeout(() => setUploadSuccess(false), 3000);
        } catch (err: any) {
            setUploadError(
                err.response?.data?.message || 'Upload failed. Please try again.'
            );
        } finally {
            setIsUploading(false);
            if (fileInputRef.current) fileInputRef.current.value = '';
        }
    };

    const sectionColor = hasProof ? '#22c55e' : '#f59e0b';
    const sectionBg = hasProof ? 'rgba(34,197,94,0.05)' : 'rgba(245,158,11,0.05)';
    const sectionBorder = hasProof
        ? '1px solid rgba(34,197,94,0.2)'
        : '1px solid rgba(245,158,11,0.2)';

    return (
        <div
            style={{
                marginTop: '20px',
                padding: '16px 18px',
                background: sectionBg,
                border: sectionBorder,
                borderRadius: '12px',
            }}
        >
            {/* ── Header ──────────────────────────────────────────────── */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    {hasProof
                        ? <ShieldCheck size={16} color="#22c55e" />
                        : <AlertCircle size={16} color="#f59e0b" />
                    }
                    <span style={{ fontSize: '13px', fontWeight: 600, color: sectionColor }}>
                        {isAdmin
                            ? hasProof ? 'Proof Submitted' : 'No Proof Uploaded Yet'
                            : hasProof ? 'Proof Uploaded ✓' : 'Proof Required to Submit'
                        }
                    </span>
                    {proofAttachments.length > 0 && (
                        <span style={{
                            padding: '1px 8px', borderRadius: '999px', fontSize: '11px',
                            backgroundColor: 'rgba(34,197,94,0.12)', color: '#22c55e',
                            fontWeight: 600,
                        }}>
                            {proofAttachments.length} file{proofAttachments.length > 1 ? 's' : ''}
                        </span>
                    )}
                </div>

                {/* Admin read-only label */}
                {isAdmin && (
                    <span style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: '#52525b' }}>
                        <Lock size={11} /> View only
                    </span>
                )}
            </div>

            {/* ── Proof file list ──────────────────────────────────────── */}
            {proofAttachments.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', marginBottom: isAdmin || taskDone ? '0' : '14px' }}>
                    {proofAttachments.map((att, idx) => (
                        <div
                            key={att.id}
                            style={{
                                display: 'flex', alignItems: 'center', gap: '10px',
                                padding: '8px 12px', borderRadius: '8px',
                                backgroundColor: 'rgba(34,197,94,0.06)',
                                border: '1px solid rgba(34,197,94,0.12)',
                            }}
                        >
                            <FileText size={14} color="#22c55e" style={{ flexShrink: 0 }} />
                            <div style={{ flex: 1, minWidth: 0 }}>
                                <p style={{ margin: 0, fontSize: '13px', color: '#e4e4e7', fontWeight: 500, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                    {att.fileName}
                                </p>
                                <p style={{ margin: '2px 0 0', fontSize: '11px', color: '#71717a' }}>
                                    Proof #{idx + 1} · {formatSize(att.fileSize)} · {new Date(att.createdAt).toLocaleString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}
                                </p>
                            </div>
                            <a
                                href={att.fileUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                title="View / Download"
                                style={{ color: '#71717a', flexShrink: 0, transition: 'color 0.15s' }}
                                onMouseEnter={(e) => (e.currentTarget.style.color = '#22c55e')}
                                onMouseLeave={(e) => (e.currentTarget.style.color = '#71717a')}
                            >
                                <ExternalLink size={14} />
                            </a>
                        </div>
                    ))}
                </div>
            ) : (
                !isAdmin && (
                    <p style={{ fontSize: '12px', color: '#71717a', margin: '0 0 12px' }}>
                        Upload a screenshot, photo, or document as proof of completion.
                    </p>
                )
            )}

            {/* ── Admin: empty state ───────────────────────────────────── */}
            {isAdmin && !hasProof && (
                <p style={{ fontSize: '12px', color: '#52525b', margin: 0 }}>
                    The employee has not uploaded any proof yet.
                </p>
            )}

            {/* ── Employee: upload control (hidden for admin and done tasks) ── */}
            {!isAdmin && !taskDone && (
                <div>
                    <input
                        ref={fileInputRef}
                        type="file"
                        style={{ display: 'none' }}
                        onChange={handleFileSelect}
                        accept="image/*,application/pdf,.doc,.docx,.xls,.xlsx,.zip"
                    />

                    <button
                        onClick={() => fileInputRef.current?.click()}
                        disabled={isUploading}
                        style={{
                            display: 'inline-flex', alignItems: 'center', gap: '6px',
                            padding: '7px 14px',
                            border: `1px dashed ${hasProof ? 'rgba(34,197,94,0.4)' : 'rgba(245,158,11,0.4)'}`,
                            borderRadius: '7px', background: 'transparent',
                            color: hasProof ? '#22c55e' : '#f59e0b',
                            fontSize: '12px', fontWeight: 500,
                            cursor: isUploading ? 'not-allowed' : 'pointer',
                            opacity: isUploading ? 0.7 : 1,
                            transition: 'border-color 0.15s, opacity 0.15s',
                        }}
                        onMouseEnter={(e) => {
                            if (!isUploading) e.currentTarget.style.borderColor = hasProof ? 'rgba(34,197,94,0.8)' : 'rgba(245,158,11,0.8)';
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.borderColor = hasProof ? 'rgba(34,197,94,0.4)' : 'rgba(245,158,11,0.4)';
                        }}
                    >
                        <Upload size={13} />
                        {isUploading ? 'Uploading...' : hasProof ? 'Upload Another Proof' : 'Upload Proof File'}
                    </button>

                    {/* Success feedback */}
                    {uploadSuccess && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '8px' }}>
                            <CheckCircle size={13} color="#22c55e" />
                            <span style={{ fontSize: '12px', color: '#22c55e', fontWeight: 500 }}>
                                Proof uploaded successfully!
                            </span>
                        </div>
                    )}

                    {/* Error feedback */}
                    {uploadError && (
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '8px' }}>
                            <AlertCircle size={13} color="#ef4444" />
                            <span style={{ fontSize: '12px', color: '#ef4444' }}>{uploadError}</span>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};
