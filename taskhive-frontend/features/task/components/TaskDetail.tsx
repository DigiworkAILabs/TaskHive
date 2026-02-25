'use client';

import React, { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useTask } from '../hooks/useTask';
import { useUpdateTask } from '../hooks/useUpdateTask';
import { useUpdateTaskStatus } from '../hooks/useUpdateTaskStatus';
import { taskService } from '../services/taskService';
import { TaskForm } from './TaskForm';
import { TaskStatusBadge } from './TaskStatusBadge';
import { TaskPriorityBadge } from './TaskPriorityBadge';
import { TaskComments } from './TaskComments';
import { TaskAttachments } from './TaskAttachments';
import { TaskStatusHistory } from './TaskStatusHistory';
import { TaskStatus, UpdateTaskData } from '../types/task.types';
import {
    ArrowLeft, Pencil, Trash2, Calendar, Clock, Tag, User,
    Loader2, AlertCircle, CheckCircle2,
} from 'lucide-react';
import Link from 'next/link';

interface TaskDetailProps {
    isAdmin?: boolean;
}

// Must match backend: com.digiwork.taskhive.module.task.enums.TaskStatus.VALID_TRANSITIONS
const STATUS_TRANSITIONS: Record<TaskStatus, TaskStatus[]> = {
    TODO: ['IN_PROGRESS', 'CANCELLED'],
    IN_PROGRESS: ['IN_REVIEW', 'CANCELLED'],             // NOT 'TODO' — backend forbids it
    IN_REVIEW: ['DONE', 'IN_PROGRESS', 'CANCELLED'],
    DONE: ['CANCELLED'],                                   // backend allows DONE→CANCELLED
    CANCELLED: [],                                         // no outgoing transitions
};

const STATUS_LABELS: Record<TaskStatus, string> = {
    TODO: 'To Do',
    IN_PROGRESS: 'In Progress',
    IN_REVIEW: 'In Review',
    DONE: 'Done',
    CANCELLED: 'Cancelled',
};

export const TaskDetail: React.FC<TaskDetailProps> = ({ isAdmin = false }) => {
    const params = useParams();
    const router = useRouter();
    const id = params.id as string;

    const { task, isLoading, error, refetch } = useTask(id);
    const { updateTask, isLoading: isUpdating, error: updateError, success: updateSuccess } = useUpdateTask();
    const { updateStatus, isLoading: isChangingStatus } = useUpdateTaskStatus();

    const [isEditing, setIsEditing] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);
    const [statusComment, setStatusComment] = useState('');
    const [showStatusModal, setShowStatusModal] = useState(false);
    const [targetStatus, setTargetStatus] = useState<TaskStatus | null>(null);

    const backHref = isAdmin ? '/admin/tasks' : '/employee/tasks';

    const handleDelete = async () => {
        setIsDeleting(true);
        try {
            await taskService.delete(id);
            router.push(backHref);
        } catch { /* handled */ } finally {
            setIsDeleting(false);
        }
    };

    const handleEditSubmit = async (data: any) => {
        const result = await updateTask(id, data as UpdateTaskData);
        if (result) { setIsEditing(false); refetch(); }
    };

    const openStatusModal = (status: TaskStatus) => {
        setTargetStatus(status);
        setShowStatusModal(true);
    };

    const handleStatusChange = async () => {
        if (!targetStatus) return;
        const result = await updateStatus(id, { status: targetStatus, comment: statusComment.trim() || undefined });
        if (result) { refetch(); setShowStatusModal(false); setStatusComment(''); setTargetStatus(null); }
    };

    // ── Loading ──────────────────────────────────────────────────────────────
    if (isLoading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
                <Loader2 size={32} color="#f97316" className="animate-spin" />
            </div>
        );
    }

    if (error || !task) {
        return (
            <div style={{ padding: '24px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', backgroundColor: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.25)', borderRadius: '10px', padding: '16px', color: '#f87171', fontSize: '14px' }}>
                    <AlertCircle size={18} />
                    {error || 'Task not found'}
                </div>
            </div>
        );
    }

    // ── Edit Mode ────────────────────────────────────────────────────────────
    if (isEditing) {
        return (
            <div>
                <button
                    onClick={() => setIsEditing(false)}
                    style={{ display: 'flex', alignItems: 'center', gap: '6px', background: 'none', border: 'none', color: '#71717a', fontSize: '14px', cursor: 'pointer', marginBottom: '16px' }}
                >
                    <ArrowLeft size={16} /> Cancel Edit
                </button>
                <TaskForm
                    mode="edit"
                    task={task}
                    onSubmit={handleEditSubmit}
                    isLoading={isUpdating}
                    error={updateError}
                    success={updateSuccess}
                    backHref={backHref}
                />
            </div>
        );
    }

    const isOverdue = !['DONE', 'CANCELLED'].includes(task.status) && new Date(task.dueDate) < new Date();
    const availableTransitions = STATUS_TRANSITIONS[task.status] || [];

    // ── View Mode ────────────────────────────────────────────────────────────
    return (
        <div>
            {/* Top Bar */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '24px', flexWrap: 'wrap', gap: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    <Link
                        href={backHref}
                        style={{ color: '#71717a', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '14px', transition: 'color 0.15s' }}
                        onMouseEnter={(e) => (e.currentTarget.style.color = '#f97316')}
                        onMouseLeave={(e) => (e.currentTarget.style.color = '#71717a')}
                    >
                        <ArrowLeft size={16} />
                        {isAdmin ? 'Back to Tasks' : 'My Tasks'}
                    </Link>
                    <span style={{ color: '#2a2a2a' }}>|</span>
                    <h1 style={{ fontSize: '20px', fontWeight: 700, color: '#ffffff', margin: 0, maxWidth: '500px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {task.title}
                    </h1>
                </div>
                {isAdmin && (
                    <div style={{ display: 'flex', gap: '8px' }}>
                        <button
                            onClick={() => setIsEditing(true)}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px', padding: '10px 16px', borderRadius: '10px', border: '1px solid #2a2a2a', backgroundColor: 'transparent', color: '#d4d4d8', fontSize: '13px', cursor: 'pointer', transition: 'border-color 0.15s' }}
                            onMouseEnter={(e) => (e.currentTarget.style.borderColor = '#52525b')}
                            onMouseLeave={(e) => (e.currentTarget.style.borderColor = '#2a2a2a')}
                        >
                            <Pencil size={14} /> Edit
                        </button>
                        <button
                            onClick={() => setShowDeleteConfirm(true)}
                            style={{ display: 'flex', alignItems: 'center', gap: '6px', padding: '10px 16px', borderRadius: '10px', border: '1px solid rgba(239,68,68,0.3)', backgroundColor: 'rgba(239,68,68,0.08)', color: '#ef4444', fontSize: '13px', cursor: 'pointer' }}
                        >
                            <Trash2 size={14} /> Delete
                        </button>
                    </div>
                )}
            </div>

            {/* Header Card */}
            <div style={{ backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '16px', padding: '28px', marginBottom: '20px' }}>
                <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px', marginBottom: '16px' }}>
                    <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                        <TaskStatusBadge status={task.status} />
                        <TaskPriorityBadge priority={task.priority} />
                        {isOverdue && (
                            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '5px', padding: '4px 10px', borderRadius: '999px', fontSize: '12px', backgroundColor: 'rgba(239,68,68,0.12)', color: '#ef4444' }}>
                                ⚠ Overdue
                            </span>
                        )}
                    </div>
                </div>

                {task.description && (
                    <p style={{ color: '#a1a1aa', fontSize: '14px', lineHeight: 1.7, margin: '0 0 20px' }}>{task.description}</p>
                )}

                {/* Meta Grid */}
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: '16px' }}>
                    <MetaItem icon={<User size={14} />} label="Assigned To" value={task.assigneeName || task.assignedTo || '—'} />
                    <MetaItem icon={<Calendar size={14} />} label="Due Date" value={task.dueDate ? new Date(task.dueDate).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' }) : '—'} valueColor={isOverdue ? '#ef4444' : undefined} />
                    <MetaItem icon={<Clock size={14} />} label="Est. Hours" value={task.estimatedHours != null ? `${task.estimatedHours}h` : '—'} />
                    <MetaItem icon={<Calendar size={14} />} label="Created" value={new Date(task.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })} />
                </div>

                {/* Tags */}
                {task.tags && task.tags.length > 0 && (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '16px', flexWrap: 'wrap' }}>
                        <Tag size={13} color="#71717a" />
                        {task.tags.map((tag) => (
                            <span
                                key={tag}
                                style={{ padding: '3px 10px', borderRadius: '6px', fontSize: '12px', backgroundColor: 'rgba(249,115,22,0.08)', color: '#f97316', border: '1px solid rgba(249,115,22,0.15)' }}
                            >
                                {tag}
                            </span>
                        ))}
                    </div>
                )}
            </div>

            {/* Status Actions */}
            {availableTransitions.length > 0 && (
                <div style={{ backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '14px', padding: '16px 20px', display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap', marginBottom: '20px' }}>
                    <span style={{ color: '#71717a', fontSize: '13px', fontWeight: 500 }}>Move to:</span>
                    {availableTransitions.map((s) => (
                        <button
                            key={s}
                            onClick={() => openStatusModal(s)}
                            disabled={isChangingStatus}
                            style={{
                                display: 'flex', alignItems: 'center', gap: '6px',
                                padding: '8px 16px', borderRadius: '8px',
                                border: '1px solid #2a2a2a', backgroundColor: 'transparent',
                                color: '#d4d4d8', fontSize: '13px', fontWeight: 500,
                                cursor: 'pointer', transition: 'all 0.15s',
                            }}
                            onMouseEnter={(e) => { e.currentTarget.style.borderColor = '#f97316'; e.currentTarget.style.color = '#f97316'; }}
                            onMouseLeave={(e) => { e.currentTarget.style.borderColor = '#2a2a2a'; e.currentTarget.style.color = '#d4d4d8'; }}
                        >
                            <CheckCircle2 size={14} />
                            {STATUS_LABELS[s]}
                        </button>
                    ))}
                </div>
            )}

            {/* Comments + Attachments + History */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '20px' }}>
                <TaskComments taskId={id} />
                <TaskAttachments taskId={id} />
                <TaskStatusHistory taskId={id} />
            </div>

            {/* Status Change Modal */}
            {showStatusModal && targetStatus && (
                <>
                    <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.6)', zIndex: 100 }} onClick={() => setShowStatusModal(false)} />
                    <div
                        style={{
                            position: 'fixed', top: '50%', left: '50%', transform: 'translate(-50%,-50%)',
                            backgroundColor: '#161616', border: '1px solid #2a2a2a', borderRadius: '16px',
                            padding: '32px', zIndex: 101, width: '420px', maxWidth: '90vw', boxShadow: '0 32px 64px rgba(0,0,0,0.5)',
                        }}
                    >
                        <h3 style={{ color: '#ffffff', fontSize: '18px', fontWeight: 600, margin: '0 0 12px' }}>
                            Move to {STATUS_LABELS[targetStatus]}
                        </h3>
                        <p style={{ color: '#a1a1aa', fontSize: '14px', margin: '0 0 16px' }}>
                            Add an optional comment for this status change.
                        </p>
                        <textarea
                            value={statusComment}
                            onChange={(e) => setStatusComment(e.target.value)}
                            placeholder="Comment (optional)…"
                            rows={3}
                            style={{
                                width: '100%', padding: '12px 14px', borderRadius: '10px', border: '1px solid #2a2a2a',
                                backgroundColor: '#111111', color: '#ffffff', fontSize: '14px',
                                outline: 'none', resize: 'none', fontFamily: 'inherit', boxSizing: 'border-box',
                            }}
                            onFocus={(e) => (e.currentTarget.style.borderColor = '#f97316')}
                            onBlur={(e) => (e.currentTarget.style.borderColor = '#2a2a2a')}
                        />
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '20px' }}>
                            <button
                                onClick={() => { setShowStatusModal(false); setStatusComment(''); }}
                                style={{ padding: '10px 20px', borderRadius: '10px', border: '1px solid #2a2a2a', backgroundColor: 'transparent', color: '#a1a1aa', fontSize: '14px', cursor: 'pointer' }}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleStatusChange}
                                disabled={isChangingStatus}
                                style={{
                                    display: 'flex', alignItems: 'center', gap: '6px', padding: '10px 20px',
                                    borderRadius: '10px', border: 'none',
                                    background: 'linear-gradient(135deg, #f97316 0%, #ea6c10 100%)',
                                    color: '#fff', fontSize: '14px', fontWeight: 600,
                                    cursor: isChangingStatus ? 'not-allowed' : 'pointer', opacity: isChangingStatus ? 0.7 : 1,
                                }}
                            >
                                {isChangingStatus && <Loader2 size={14} className="animate-spin" />}
                                Confirm
                            </button>
                        </div>
                    </div>
                </>
            )}

            {/* Delete Confirm Modal */}
            {showDeleteConfirm && (
                <>
                    <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.6)', zIndex: 100 }} onClick={() => setShowDeleteConfirm(false)} />
                    <div
                        style={{
                            position: 'fixed', top: '50%', left: '50%', transform: 'translate(-50%,-50%)',
                            backgroundColor: '#161616', border: '1px solid #2a2a2a', borderRadius: '16px',
                            padding: '32px', zIndex: 101, width: '420px', maxWidth: '90vw', boxShadow: '0 32px 64px rgba(0,0,0,0.5)',
                        }}
                    >
                        <h3 style={{ color: '#ffffff', fontSize: '18px', fontWeight: 600, margin: '0 0 12px' }}>Delete Task</h3>
                        <p style={{ color: '#a1a1aa', fontSize: '14px', margin: '0 0 24px' }}>
                            Are you sure you want to delete <strong style={{ color: '#fff' }}>{task.title}</strong>? This action cannot be undone.
                        </p>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                            <button onClick={() => setShowDeleteConfirm(false)} style={{ padding: '10px 20px', borderRadius: '10px', border: '1px solid #2a2a2a', backgroundColor: 'transparent', color: '#a1a1aa', fontSize: '14px', cursor: 'pointer' }}>
                                Cancel
                            </button>
                            <button
                                onClick={handleDelete}
                                disabled={isDeleting}
                                style={{ display: 'flex', alignItems: 'center', gap: '6px', padding: '10px 20px', borderRadius: '10px', border: 'none', backgroundColor: '#ef4444', color: '#fff', fontSize: '14px', fontWeight: 600, cursor: isDeleting ? 'not-allowed' : 'pointer', opacity: isDeleting ? 0.6 : 1 }}
                            >
                                {isDeleting && <Loader2 size={14} className="animate-spin" />}
                                {isDeleting ? 'Deleting…' : 'Delete'}
                            </button>
                        </div>
                    </div>
                </>
            )}
        </div>
    );
};

// ── Meta Item ─────────────────────────────────────────────────────────────────

function MetaItem({ icon, label, value, valueColor }: { icon: React.ReactNode; label: string; value: string; valueColor?: string }) {
    return (
        <div style={{ backgroundColor: '#111111', border: '1px solid #1f1f1f', borderRadius: '10px', padding: '14px 16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#71717a', fontSize: '11px', marginBottom: '6px', textTransform: 'uppercase', letterSpacing: '0.4px' }}>
                {icon}{label}
            </div>
            <div style={{ color: valueColor || '#ffffff', fontSize: '14px', fontWeight: 600 }}>{value}</div>
        </div>
    );
}
