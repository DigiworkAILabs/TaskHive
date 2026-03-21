'use client';

import React, { useState, useEffect } from 'react';
import { CreateTaskData, UpdateTaskData, Task, TaskPriority } from '../types/task.types';
import { Loader2, AlertCircle, CheckCircle, Calendar, Tag, AlignLeft, User, Clock, ChevronDown } from 'lucide-react';
import Link from 'next/link';
import { employeeService } from '@/features/employee/services/employeeService';
import { EmployeeListItem } from '@/features/employee/types/employee.types';
import { DatePicker } from '@/features/employee/components/DatePicker';
import { useRecommendWorkload } from '@/features/ml/hooks/useRecommendWorkload';
import WorkloadRecommendationComponent from '@/features/ml/components/WorkloadRecommendation';
import { useMlStore } from '@/features/ml/store/mlStore';
import { Brain, Sparkles } from 'lucide-react';

interface TaskFormProps {
    mode: 'create' | 'edit';
    task?: Task | null;
    onSubmit: (data: CreateTaskData | UpdateTaskData) => Promise<void>;
    isLoading: boolean;
    error: string | null;
    success: boolean;
    backHref?: string;
    /** ML-accepted priority — overrides internal state when set */
    initialPriority?: TaskPriority;
    /** Notifies parent of field changes for ML context (non-blocking) */
    onFieldChange?: (fields: {
        title?: string;
        description?: string;
        priority?: string;
        assignedTo?: string;
        tags?: string;
        estimatedHours?: string;
    }) => void;
    /** ML Feature 2: Node to render below Estimated Hours */
    completionEstimateNode?: React.ReactNode;
}

// ── Shared input style ────────────────────────────────────────────────────────

const inputBase: React.CSSProperties = {
    width: '100%',
    backgroundColor: '#111111',
    border: '1px solid #2a2a2a',
    borderRadius: '10px',
    color: '#ffffff',
    fontSize: '14px',
    padding: '12px 16px',
    outline: 'none',
    transition: 'border-color 0.2s, box-shadow 0.2s',
    boxSizing: 'border-box',
};

function FormField({ label, required, children }: { label: string; required?: boolean; children: React.ReactNode }) {
    return (
        <div>
            <label style={{ display: 'block', fontSize: '13px', color: '#a1a1aa', marginBottom: '6px', fontWeight: 500 }}>
                {label} {required && <span style={{ color: '#f97316' }}>*</span>}
            </label>
            {children}
        </div>
    );
}

// ── Main Component ────────────────────────────────────────────────────────────

export const TaskForm: React.FC<TaskFormProps> = ({ mode, task, onSubmit, isLoading, error, success, backHref = '/admin/tasks', initialPriority, onFieldChange, completionEstimateNode }) => {
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        priority: (initialPriority ?? 'MEDIUM') as TaskPriority,
        assignedTo: '',
        dueDate: '',
        dueTime: '17:00',
        estimatedHours: '',
        tags: '',
        proofRequired: false,
        approvalRequired: false,
    });

    const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
    const [focusedField, setFocusedField] = useState<string | null>(null);
    const [employees, setEmployees] = useState<EmployeeListItem[]>([]);
    const [isFetchingEmployees, setIsFetchingEmployees] = useState(false);

    // AI Workload Recommendation Hook
    const {
        recommend,
        recommendation,
        isLoading: isRecommending,
        error: recommendError,
        reset: resetRecommendation
    } = useRecommendWorkload();

    const { isMlEnabled } = useMlStore();

    useEffect(() => {
        if (mode === 'edit' && task) {
            let editTime = '17:00';
            if (task.dueDate && task.dueDate.includes('T')) {
                editTime = task.dueDate.slice(11, 16); // Extract HH:mm from ISO string
            }
            setFormData({
                title: task.title || '',
                description: task.description || '',
                priority: task.priority || 'MEDIUM',
                assignedTo: task.assignedTo || '',
                dueDate: task.dueDate ? task.dueDate.slice(0, 10) : '',
                dueTime: editTime,
                estimatedHours: task.estimatedHours?.toString() || '',
                tags: task.tags?.join(', ') || '',
                proofRequired: task.proofRequired ?? false,
                approvalRequired: task.approvalRequired ?? false,
            });
        }
    }, [mode, task]);

    useEffect(() => {
        const fetchEmployees = async () => {
            setIsFetchingEmployees(true);
            try {
                // Fetch first 100 active employees
                const response = await employeeService.list({ page: 0, size: 100, status: 'ACTIVE' });
                setEmployees(response.content);
            } catch (err) {
                console.error('Failed to fetch employees:', err);
            } finally {
                setIsFetchingEmployees(false);
            }
        };
        fetchEmployees();
    }, []);

    const validate = (): boolean => {
        const errors: Record<string, string> = {};
        if (!formData.title.trim()) errors.title = 'Title is required';
        if (mode === 'create' && !formData.assignedTo.trim()) errors.assignedTo = 'Assignee is required';
        if (mode === 'create' && !formData.dueDate) errors.dueDate = 'Due date is required';
        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    };

    // Sync ML-accepted priority into form state
    useEffect(() => {
        if (initialPriority) {
            setFormData(prev => ({ ...prev, priority: initialPriority }));
            // Notify parent to sync ML context (essential for Completion Time prediction)
            if (onFieldChange) {
                onFieldChange({ priority: initialPriority });
            }
        }
    }, [initialPriority, onFieldChange]);

    const handleChange = (field: string) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        const value = e.target.value;
        console.log(`[TaskForm] Change ${field}:`, value);
        setFormData((prev) => ({ ...prev, [field]: value }));
        if (fieldErrors[field]) setFieldErrors((prev) => { const n = { ...prev }; delete n[field]; return n; });
        // Notify parent for ML context
        if (onFieldChange) {
            console.log(`[TaskForm] Notifying parent of ${field}`);
            onFieldChange({ [field]: value });
        }
    };

    const handleRecommendAssignee = async () => {
        if (!formData.title.trim() || employees.length === 0) return;

        const candidateIds = employees.map(emp => emp.id);

        await recommend({
            taskTitle: formData.title,
            taskPriority: formData.priority,
            taskEstimatedHours: formData.estimatedHours ? parseFloat(formData.estimatedHours) : undefined,
            candidateEmployeeIds: candidateIds
        });
    };

    const handleApplyRecommendation = (empId: string) => {
        setFormData(prev => ({ ...prev, assignedTo: empId }));
        // Sync parent ML state so Completion Time can trigger
        if (onFieldChange) {
            onFieldChange({ assignedTo: empId });
        }
        resetRecommendation(); // Clear the recommendation UI after applying
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!validate()) return;

        const tags = formData.tags.split(',').map((t) => t.trim()).filter(Boolean);

        if (mode === 'create') {
            const payload: CreateTaskData = {
                title: formData.title.trim(),
                priority: formData.priority,
                assignedTo: formData.assignedTo.trim(),
                dueDate: `${formData.dueDate}T${formData.dueTime}:00`,
            };
            if (formData.description.trim()) payload.description = formData.description.trim();
            if (formData.estimatedHours) payload.estimatedHours = parseFloat(formData.estimatedHours);
            if (tags.length) payload.tags = tags;
            payload.proofRequired = formData.proofRequired;
            payload.approvalRequired = formData.approvalRequired;
            await onSubmit(payload);
        } else {
            const payload: UpdateTaskData = {};
            if (formData.title.trim()) payload.title = formData.title.trim();
            if (formData.description.trim()) payload.description = formData.description.trim();
            payload.priority = formData.priority;
            if (formData.assignedTo.trim()) payload.assignedTo = formData.assignedTo.trim();
            if (formData.dueDate) {
                payload.dueDate = formData.dueDate.includes('T') ? formData.dueDate : `${formData.dueDate}T${formData.dueTime}:00`;
            }
            if (formData.estimatedHours) payload.estimatedHours = parseFloat(formData.estimatedHours);
            payload.tags = tags;
            payload.proofRequired = formData.proofRequired;
            payload.approvalRequired = formData.approvalRequired;
            await onSubmit(payload);
        }
    };

    const priorityOptions: { value: TaskPriority; label: string; color: string }[] = [
        { value: 'LOW', label: 'Low', color: '#22c55e' },
        { value: 'MEDIUM', label: 'Medium', color: '#f59e0b' },
        { value: 'HIGH', label: 'High', color: '#f97316' },
        { value: 'CRITICAL', label: 'Critical', color: '#ef4444' },
    ];

    const inputStyle = (field: string): React.CSSProperties => ({
        ...inputBase,
        borderColor: fieldErrors[field] ? '#ef4444' : focusedField === field ? '#f97316' : '#2a2a2a',
        boxShadow: focusedField === field ? '0 0 0 3px rgba(249,115,22,0.12)' : 'none',
    });

    return (
        <div>
            {/* Error */}
            {error && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', backgroundColor: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.25)', borderRadius: '10px', padding: '12px 16px', marginBottom: '20px', color: '#f87171', fontSize: '13px' }}>
                    <AlertCircle size={15} style={{ flexShrink: 0 }} />{error}
                </div>
            )}

            {/* Success */}
            {success && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', backgroundColor: 'rgba(34,197,94,0.08)', border: '1px solid rgba(34,197,94,0.25)', borderRadius: '10px', padding: '12px 16px', marginBottom: '20px', color: '#22c55e', fontSize: '13px' }}>
                    <CheckCircle size={15} style={{ flexShrink: 0 }} />
                    {mode === 'create' ? 'Task created successfully!' : 'Task updated successfully!'}
                </div>
            )}

            <div style={{ backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '16px', padding: '32px' }}>
                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    {/* Title */}
                    <FormField label="Task Title" required>
                        <input
                            id="title"
                            placeholder="Enter a clear, descriptive task title…"
                            value={formData.title}
                            onChange={handleChange('title')}
                            onFocus={() => setFocusedField('title')}
                            onBlur={() => setFocusedField(null)}
                            style={inputStyle('title')}
                        />
                        {fieldErrors.title && <p style={{ color: '#ef4444', fontSize: '12px', marginTop: '4px' }}>{fieldErrors.title}</p>}
                    </FormField>

                    {/* Description */}
                    <FormField label="Description">
                        <div style={{ display: 'flex', alignItems: 'flex-start', gap: '10px' }}>
                            <AlignLeft size={15} color="#52525b" style={{ marginTop: '14px', flexShrink: 0 }} />
                            <textarea
                                placeholder="Describe the task, steps, and acceptance criteria…"
                                value={formData.description}
                                onChange={handleChange('description')}
                                onFocus={() => setFocusedField('description')}
                                onBlur={() => setFocusedField(null)}
                                rows={4}
                                style={{
                                    ...inputStyle('description'),
                                    resize: 'vertical',
                                    fontFamily: 'inherit',
                                    lineHeight: 1.6,
                                }}
                            />
                        </div>
                    </FormField>

                    {/* Priority + Assigned To */}
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                        <FormField label="Priority" required>
                            <select
                                value={formData.priority}
                                onChange={handleChange('priority')}
                                onFocus={() => setFocusedField('priority')}
                                onBlur={() => setFocusedField(null)}
                                style={{ ...inputStyle('priority'), cursor: 'pointer' }}
                            >
                                {priorityOptions.map((p) => (
                                    <option key={p.value} value={p.value}>{p.label}</option>
                                ))}
                            </select>
                        </FormField>

                        <FormField label="Assigned To" required={mode === 'create'}>
                            <div style={{ position: 'relative' }}>
                                {/* AI Recommend Toggle */}
                                {isMlEnabled && (
                                    <button
                                        type="button"
                                        onClick={handleRecommendAssignee}
                                        disabled={isRecommending || !formData.title.trim() || employees.length === 0}
                                        style={{
                                            position: 'absolute',
                                            right: '0',
                                            top: '-26px',
                                            backgroundColor: 'transparent',
                                            border: 'none',
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '4px',
                                            color: isRecommending ? '#f97316' : '#a1a1aa',
                                            fontSize: '11px',
                                            fontWeight: 600,
                                            cursor: (isRecommending || !formData.title.trim()) ? 'not-allowed' : 'pointer',
                                            transition: 'color 0.2s',
                                        }}
                                        onMouseEnter={(e) => !isRecommending && (e.currentTarget.style.color = '#f97316')}
                                        onMouseLeave={(e) => !isRecommending && (e.currentTarget.style.color = '#a1a1aa')}
                                    >
                                        {isRecommending ? <Loader2 size={11} className="animate-spin" /> : <Brain size={11} />}
                                        {isRecommending ? 'Analyzing...' : 'Recommend Best Fit'}
                                    </button>
                                )}

                                <User size={14} color="#52525b" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', zIndex: 1, pointerEvents: 'none' }} />
                                <select
                                    value={formData.assignedTo}
                                    onChange={handleChange('assignedTo')}
                                    onFocus={() => setFocusedField('assignedTo')}
                                    onBlur={() => setFocusedField(null)}
                                    style={{ ...inputStyle('assignedTo'), paddingLeft: '36px', cursor: 'pointer', appearance: 'none' }}
                                    disabled={isFetchingEmployees}
                                >
                                    <option value="" disabled>{isFetchingEmployees ? 'Loading names…' : 'Select an employee…'}</option>
                                    {employees.map((emp) => (
                                        <option key={emp.id} value={emp.id}>
                                            {emp.firstName} {emp.lastName} ({emp.department})
                                        </option>
                                    ))}
                                </select>
                                <ChevronDown size={14} color="#52525b" style={{ position: 'absolute', right: '14px', top: '50%', transform: 'translateY(-50%)', pointerEvents: 'none' }} />
                            </div>

                            {/* AI Recommendation Result UI */}
                            {isMlEnabled && recommendation && (
                                <WorkloadRecommendationComponent
                                    recommendation={recommendation}
                                    onAccept={handleApplyRecommendation}
                                    isLoading={isRecommending}
                                    employees={employees}
                                />
                            )}

                            {fieldErrors.assignedTo && <p style={{ color: '#ef4444', fontSize: '12px', marginTop: '4px' }}>{fieldErrors.assignedTo}</p>}
                        </FormField>
                    </div>

                    {/* Due Date + Time + Estimated Hours */}
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                        <div>
                            <DatePicker
                                id="dueDate"
                                label="Due Date & Time"
                                value={formData.dueDate}
                                onChange={(date) => setFormData(prev => ({ ...prev, dueDate: date }))}
                                required={mode === 'create'}
                                error={fieldErrors.dueDate}
                                minDate={new Date()}
                                maxDate={new Date(new Date().getFullYear(), new Date().getMonth() + 2, 0)}
                            />
                            {/* Time input — appears once a date is selected */}
                            {formData.dueDate && (
                                <div style={{ marginTop: '8px' }}>
                                    <label
                                        htmlFor="dueTime"
                                        style={{ display: 'block', fontSize: '12px', color: '#71717a', marginBottom: '4px', fontWeight: 500 }}
                                    >
                                        Due Time (24-hr)
                                    </label>
                                    <div style={{ position: 'relative' }}>
                                        <Clock size={14} color="#52525b" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
                                        <input
                                            id="dueTime"
                                            type="time"
                                            value={formData.dueTime}
                                            onChange={handleChange('dueTime')}
                                            onFocus={() => setFocusedField('dueTime')}
                                            onBlur={() => setFocusedField(null)}
                                            style={{
                                                ...inputStyle('dueTime'),
                                                paddingLeft: '34px',
                                                colorScheme: 'dark',
                                            }}
                                        />
                                    </div>
                                </div>
                            )}
                        </div>

                        <FormField label="Estimated Hours">
                            <div style={{ position: 'relative' }}>
                                <Clock size={14} color="#52525b" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }} />
                                <input
                                    type="number"
                                    min="0"
                                    step="0.5"
                                    placeholder="e.g. 4.5"
                                    value={formData.estimatedHours}
                                    onChange={handleChange('estimatedHours')}
                                    onFocus={() => setFocusedField('estimatedHours')}
                                    onBlur={() => setFocusedField(null)}
                                    style={{ ...inputStyle('estimatedHours'), paddingLeft: '36px' }}
                                />
                            </div>
                            {completionEstimateNode}
                        </FormField>
                    </div>

                    {/* Tags */}
                    <FormField label="Tags (comma-separated)">
                        <div style={{ position: 'relative' }}>
                            <Tag size={14} color="#52525b" style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)' }} />
                            <input
                                placeholder="e.g. frontend, urgent, bug-fix"
                                value={formData.tags}
                                onChange={handleChange('tags')}
                                onFocus={() => setFocusedField('tags')}
                                onBlur={() => setFocusedField(null)}
                                style={{ ...inputStyle('tags'), paddingLeft: '36px' }}
                            />
                        </div>
                    </FormField>

                    {/* ── Task Requirements (P1.1 / P1.2) ─────────────────── */}
                    <div style={{
                        backgroundColor: '#111111',
                        border: '1px solid #2a2a2a',
                        borderRadius: '12px',
                        padding: '18px 20px',
                    }}>
                        <p style={{ fontSize: '12px', fontWeight: 600, color: '#71717a', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: '14px' }}>
                            Task Requirements
                        </p>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                            {/* Proof Required toggle */}
                            <label style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', cursor: 'pointer' }}>
                                <div>
                                    <p style={{ fontSize: '14px', fontWeight: 500, color: '#e4e4e7', margin: 0 }}>Require Proof of Completion</p>
                                    <p style={{ fontSize: '12px', color: '#71717a', margin: '2px 0 0' }}>Assignee must upload a proof file before submitting</p>
                                </div>
                                <div
                                    onClick={() => setFormData(prev => ({ ...prev, proofRequired: !prev.proofRequired }))}
                                    style={{
                                        width: '42px', height: '24px', borderRadius: '12px', flexShrink: 0,
                                        backgroundColor: formData.proofRequired ? '#f97316' : '#3f3f46',
                                        position: 'relative', cursor: 'pointer', transition: 'background-color 0.2s',
                                    }}
                                >
                                    <div style={{
                                        width: '18px', height: '18px', borderRadius: '50%', backgroundColor: '#ffffff',
                                        position: 'absolute', top: '3px', transition: 'left 0.2s',
                                        left: formData.proofRequired ? '21px' : '3px',
                                        boxShadow: '0 1px 3px rgba(0,0,0,0.4)',
                                    }} />
                                </div>
                            </label>

                            {/* Approval Required toggle */}
                            <label style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', cursor: 'pointer' }}>
                                <div>
                                    <p style={{ fontSize: '14px', fontWeight: 500, color: '#e4e4e7', margin: 0 }}>Require Admin Approval</p>
                                    <p style={{ fontSize: '12px', color: '#71717a', margin: '2px 0 0' }}>Task goes to Pending Approval instead of Done</p>
                                </div>
                                <div
                                    onClick={() => setFormData(prev => ({ ...prev, approvalRequired: !prev.approvalRequired }))}
                                    style={{
                                        width: '42px', height: '24px', borderRadius: '12px', flexShrink: 0,
                                        backgroundColor: formData.approvalRequired ? '#f97316' : '#3f3f46',
                                        position: 'relative', cursor: 'pointer', transition: 'background-color 0.2s',
                                    }}
                                >
                                    <div style={{
                                        width: '18px', height: '18px', borderRadius: '50%', backgroundColor: '#ffffff',
                                        position: 'absolute', top: '3px', transition: 'left 0.2s',
                                        left: formData.approvalRequired ? '21px' : '3px',
                                        boxShadow: '0 1px 3px rgba(0,0,0,0.4)',
                                    }} />
                                </div>
                            </label>
                        </div>
                    </div>

                    {/* Actions */}
                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '8px' }}>
                        <Link
                            href={backHref}
                            style={{
                                padding: '12px 24px', borderRadius: '10px', border: '1px solid #2a2a2a',
                                backgroundColor: 'transparent', color: '#a1a1aa', fontSize: '14px', fontWeight: 500,
                                textDecoration: 'none', transition: 'border-color 0.2s',
                            }}
                            onMouseEnter={(e) => (e.currentTarget.style.borderColor = '#52525b')}
                            onMouseLeave={(e) => (e.currentTarget.style.borderColor = '#2a2a2a')}
                        >
                            Cancel
                        </Link>
                        <button
                            type="submit"
                            disabled={isLoading}
                            style={{
                                display: 'flex', alignItems: 'center', gap: '8px', padding: '12px 28px',
                                borderRadius: '10px', border: 'none',
                                background: isLoading ? '#7c3e10' : 'linear-gradient(135deg, #f97316 0%, #ea6c10 100%)',
                                color: '#ffffff', fontSize: '14px', fontWeight: 600,
                                cursor: isLoading ? 'not-allowed' : 'pointer',
                                boxShadow: '0 4px 20px rgba(249,115,22,0.3)', transition: 'opacity 0.2s',
                            }}
                            onMouseEnter={(e) => !isLoading && ((e.currentTarget as HTMLButtonElement).style.opacity = '0.88')}
                            onMouseLeave={(e) => ((e.currentTarget as HTMLButtonElement).style.opacity = '1')}
                        >
                            {isLoading && <Loader2 size={16} className="animate-spin" />}
                            {isLoading ? (mode === 'create' ? 'Creating…' : 'Saving…') : (mode === 'create' ? 'Create Task' : 'Save Changes')}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};
