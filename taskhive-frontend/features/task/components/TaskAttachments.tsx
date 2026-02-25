'use client';

import React, { useState, useEffect, useRef } from 'react';
import { taskService } from '../services/taskService';
import { TaskAttachment } from '../types/task.types';
import { Paperclip, Upload, Loader2, AlertCircle, FileText, Image, Archive } from 'lucide-react';

interface TaskAttachmentsProps {
    taskId: string;
}

function fileSizeLabel(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

function FileIcon({ mime }: { mime: string }) {
    if (mime.startsWith('image/')) return <Image size={16} color="#3b82f6" />;
    if (mime.includes('pdf') || mime.includes('document')) return <FileText size={16} color="#f97316" />;
    return <Archive size={16} color="#a1a1aa" />;
}

export const TaskAttachments: React.FC<TaskAttachmentsProps> = ({ taskId }) => {
    const [attachments, setAttachments] = useState<TaskAttachment[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [isUploading, setIsUploading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const fileRef = useRef<HTMLInputElement>(null);

    const fetchAttachments = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await taskService.getAttachments(taskId);
            setAttachments(data);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load attachments');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => { if (taskId) fetchAttachments(); }, [taskId]);

    const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        setIsUploading(true);
        setError(null);
        try {
            const att = await taskService.uploadAttachment(taskId, file);
            setAttachments((prev) => [...prev, att]);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Upload failed');
        } finally {
            setIsUploading(false);
            if (fileRef.current) fileRef.current.value = '';
        }
    };

    return (
        <div style={{ backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '14px', padding: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Paperclip size={16} color="#f97316" />
                    <h3 style={{ fontSize: '15px', fontWeight: 600, color: '#ffffff', margin: 0 }}>
                        Attachments <span style={{ color: '#52525b', fontSize: '13px', fontWeight: 400 }}>({attachments.length})</span>
                    </h3>
                </div>
                <button
                    onClick={() => fileRef.current?.click()}
                    disabled={isUploading}
                    style={{
                        display: 'flex', alignItems: 'center', gap: '6px',
                        padding: '8px 14px', borderRadius: '8px', border: '1px solid rgba(249,115,22,0.3)',
                        backgroundColor: 'rgba(249,115,22,0.08)', color: '#f97316',
                        fontSize: '13px', fontWeight: 500, cursor: isUploading ? 'not-allowed' : 'pointer',
                    }}
                >
                    {isUploading ? <Loader2 size={14} className="animate-spin" /> : <Upload size={14} />}
                    {isUploading ? 'Uploading…' : 'Upload File'}
                </button>
                <input ref={fileRef} type="file" style={{ display: 'none' }} onChange={handleUpload} />
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
            ) : attachments.length === 0 ? (
                <p style={{ color: '#52525b', fontSize: '13px', textAlign: 'center', padding: '16px 0' }}>No attachments yet.</p>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                    {attachments.map((att) => (
                        <a
                            key={att.id}
                            href={att.fileUrl}
                            target="_blank"
                            rel="noopener noreferrer"
                            style={{
                                display: 'flex', alignItems: 'center', gap: '12px',
                                padding: '12px 14px', borderRadius: '10px',
                                backgroundColor: '#111111', border: '1px solid #2a2a2a',
                                textDecoration: 'none', transition: 'border-color 0.15s',
                            }}
                            onMouseEnter={(e) => (e.currentTarget.style.borderColor = '#f97316')}
                            onMouseLeave={(e) => (e.currentTarget.style.borderColor = '#2a2a2a')}
                        >
                            <FileIcon mime={att.mimeType} />
                            <div style={{ flex: 1, minWidth: 0 }}>
                                <div style={{ color: '#ffffff', fontSize: '13px', fontWeight: 500, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                                    {att.fileName}
                                </div>
                                <div style={{ color: '#52525b', fontSize: '11px', marginTop: '2px' }}>
                                    {fileSizeLabel(att.fileSize)} · {att.uploadedByName}
                                </div>
                            </div>
                        </a>
                    ))}
                </div>
            )}
        </div>
    );
};
