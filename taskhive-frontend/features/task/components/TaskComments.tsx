'use client';

import React, { useState, useEffect } from 'react';
import { taskService } from '../services/taskService';
import { TaskComment } from '../types/task.types';
import { MessageSquare, Send, Loader2, AlertCircle } from 'lucide-react';

interface TaskCommentsProps {
    taskId: string;
}

export const TaskComments: React.FC<TaskCommentsProps> = ({ taskId }) => {
    const [comments, setComments] = useState<TaskComment[]>([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [newComment, setNewComment] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const fetchComments = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const data = await taskService.getComments(taskId);
            setComments(Array.isArray(data) ? data : []);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load comments');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => { if (taskId) fetchComments(); }, [taskId]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!newComment.trim()) return;
        setIsSubmitting(true);
        try {
            const comment = await taskService.addComment(taskId, { content: newComment.trim() });
            setComments((prev) => [...prev, comment]);
            setNewComment('');
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to add comment');
        } finally {
            setIsSubmitting(false);
        }
    };

    const initials = (name: string) => name.split(' ').map((n) => n[0]).join('').toUpperCase().slice(0, 2);

    return (
        <div style={{ backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '14px', padding: '24px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '20px' }}>
                <MessageSquare size={16} color="#f97316" />
                <h3 style={{ fontSize: '15px', fontWeight: 600, color: '#ffffff', margin: 0 }}>
                    Comments <span style={{ color: '#52525b', fontSize: '13px', fontWeight: 400 }}>({comments.length})</span>
                </h3>
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
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', marginBottom: '20px' }}>
                    {comments.length === 0 ? (
                        <p style={{ color: '#52525b', fontSize: '13px', textAlign: 'center', padding: '16px 0' }}>No comments yet. Be the first!</p>
                    ) : comments.map((c) => (
                        <div key={c.id} style={{ display: 'flex', gap: '12px' }}>
                            <div
                                style={{
                                    width: '32px', height: '32px', borderRadius: '50%', flexShrink: 0,
                                    backgroundColor: '#1f1f1f', border: '2px solid #2a2a2a',
                                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                                    fontSize: '11px', fontWeight: 700, color: '#f97316',
                                }}
                            >
                                {initials(c.authorName)}
                            </div>
                            <div style={{ flex: 1 }}>
                                <div style={{ display: 'flex', alignItems: 'baseline', gap: '8px', marginBottom: '4px' }}>
                                    <span style={{ fontSize: '13px', fontWeight: 600, color: '#ffffff' }}>{c.authorName}</span>
                                    <span style={{ fontSize: '11px', color: '#52525b' }}>
                                        {new Date(c.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' })}
                                    </span>
                                </div>
                                <div
                                    style={{
                                        backgroundColor: '#111111', border: '1px solid #2a2a2a',
                                        borderRadius: '10px', padding: '12px 14px',
                                        fontSize: '14px', color: '#d4d4d8', lineHeight: 1.6,
                                    }}
                                >
                                    {c.content}
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Add Comment */}
            <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '10px', alignItems: 'flex-end' }}>
                <textarea
                    value={newComment}
                    onChange={(e) => setNewComment(e.target.value)}
                    placeholder="Write a comment…"
                    rows={2}
                    style={{
                        flex: 1, padding: '12px 14px', borderRadius: '10px',
                        border: '1px solid #2a2a2a', backgroundColor: '#111111',
                        color: '#ffffff', fontSize: '14px', outline: 'none',
                        resize: 'none', fontFamily: 'inherit', lineHeight: 1.5,
                    }}
                    onFocus={(e) => (e.currentTarget.style.borderColor = '#f97316')}
                    onBlur={(e) => (e.currentTarget.style.borderColor = '#2a2a2a')}
                />
                <button
                    type="submit"
                    disabled={isSubmitting || !newComment.trim()}
                    style={{
                        display: 'flex', alignItems: 'center', gap: '6px',
                        padding: '12px 18px', borderRadius: '10px', border: 'none',
                        background: newComment.trim() ? 'linear-gradient(135deg, #f97316 0%, #ea6c10 100%)' : '#1f1f1f',
                        color: newComment.trim() ? '#ffffff' : '#52525b',
                        fontSize: '13px', fontWeight: 600, cursor: newComment.trim() ? 'pointer' : 'not-allowed',
                        transition: 'all 0.2s',
                    }}
                >
                    {isSubmitting ? <Loader2 size={14} className="animate-spin" /> : <Send size={14} />}
                    Post
                </button>
            </form>
        </div>
    );
};
