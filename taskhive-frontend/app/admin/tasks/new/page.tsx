'use client';

/**
 * app/admin/tasks/new/page.tsx
 * ─────────────────────────────
 * Create New Task page — Phase 7.1 ML integration added.
 *
 * Changes from original:
 *   - Imports usePriorityPrediction hook and PrioritySuggestionBadge
 *   - Adds "Suggest Priority" button next to the Priority dropdown
 *   - On button click → calls ML endpoint with current title + description
 *   - On Accept → auto-fills the priority dropdown
 *   - All ML interactions are non-blocking (form works normally without them)
 */

import React, { useState, useRef, useEffect } from 'react';
import Link from 'next/link';
import { ArrowLeft, Sparkles } from 'lucide-react';

import { useCreateTask } from '@/features/task/hooks/useCreateTask';
import { TaskForm } from '@/features/task/components/TaskForm';
import { CreateTaskData } from '@/features/task/types/task.types';
import { usePriorityPrediction } from '@/features/ml/hooks/usePriorityPrediction';
import { PrioritySuggestionBadge } from '@/features/ml/components/PrioritySuggestionBadge';
import type { TaskPriority } from '@/features/ml/types/ml.types';

export default function NewTaskPage() {
    const { createTask, isLoading, error, success } = useCreateTask();
    const { predict, prediction, isLoading: mlLoading, reset: resetML } = usePriorityPrediction();

    // Track form field values to pass to ML — lifted from TaskForm via callback
    const [mlFormData, setMLFormData] = useState({
        taskTitle: '',
        taskDescription: '',
        employeeId: '',
        tags: [] as string[],
        estimatedHours: undefined as number | undefined,
    });

    // Accepted priority — passed down to TaskForm to override its internal state
    const [acceptedPriority, setAcceptedPriority] = useState<TaskPriority | null>(null);

    const handleSubmit = async (data: CreateTaskData | Record<string, unknown>) => {
        await createTask(data as CreateTaskData);
        resetML();
        setAcceptedPriority(null);
    };

    /** Called by TaskForm whenever its fields change (lifted state for ML) */
    const handleFormChange = (fields: {
        title?: string;
        description?: string;
        assignedTo?: string;
        tags?: string;
        estimatedHours?: string;
    }) => {
        setMLFormData({
            taskTitle: fields.title ?? mlFormData.taskTitle,
            taskDescription: fields.description ?? mlFormData.taskDescription,
            employeeId: fields.assignedTo ?? mlFormData.employeeId,
            tags: fields.tags
                ? fields.tags.split(',').map(t => t.trim()).filter(Boolean)
                : mlFormData.tags,
            estimatedHours: fields.estimatedHours
                ? parseFloat(fields.estimatedHours)
                : mlFormData.estimatedHours,
        });
    };

    /** Fired when admin clicks "Suggest Priority" */
    const handleSuggestPriority = async () => {
        await predict({
            taskTitle: mlFormData.taskTitle,
            taskDescription: mlFormData.taskDescription,
            employeeId: mlFormData.employeeId || undefined,
            tags: mlFormData.tags,
            estimatedHours: mlFormData.estimatedHours,
        });
    };

    /** Fired when admin clicks "Accept" on the badge */
    const handleAcceptPriority = (priority: TaskPriority) => {
        setAcceptedPriority(priority);
    };

    const canSuggest = mlFormData.taskTitle.trim().length >= 2;

    return (
        <div>
            {/* Header */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '28px' }}>
                <Link
                    href="/admin/tasks"
                    style={{ color: '#71717a', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '14px', transition: 'color 0.15s' }}
                    onMouseEnter={(e) => (e.currentTarget.style.color = '#f97316')}
                    onMouseLeave={(e) => (e.currentTarget.style.color = '#71717a')}
                >
                    <ArrowLeft size={16} />
                    Back to Tasks
                </Link>
                <span style={{ color: '#2a2a2a' }}>|</span>
                <h1 style={{ fontSize: '20px', fontWeight: 700, color: '#ffffff', margin: 0 }}>
                    Create New Task
                </h1>

                {/* ML Suggest Priority button — shown in header for visibility */}
                <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <button
                        type="button"
                        id="ml-suggest-priority-btn"
                        onClick={handleSuggestPriority}
                        disabled={!canSuggest || mlLoading}
                        title={canSuggest ? 'Click to get AI priority suggestion' : 'Enter a task title first'}
                        style={{
                            display: 'flex', alignItems: 'center', gap: '6px',
                            padding: '8px 16px', borderRadius: '8px',
                            border: '1px solid rgba(249,115,22,0.35)',
                            background: canSuggest && !mlLoading
                                ? 'rgba(249,115,22,0.08)'
                                : 'transparent',
                            color: canSuggest ? '#f97316' : '#3f3f46',
                            fontSize: '13px', fontWeight: 500, cursor: canSuggest ? 'pointer' : 'not-allowed',
                            transition: 'all 0.15s',
                            opacity: canSuggest ? 1 : 0.5,
                        }}
                        onMouseEnter={(e) => canSuggest && !mlLoading && (e.currentTarget.style.background = 'rgba(249,115,22,0.15)')}
                        onMouseLeave={(e) => (e.currentTarget.style.background = canSuggest && !mlLoading ? 'rgba(249,115,22,0.08)' : 'transparent')}
                    >
                        <Sparkles size={14} />
                        {mlLoading ? 'Analysing…' : 'Suggest Priority'}
                    </button>
                </div>
            </div>

            {/* ML Suggestion Badge — rendered above the form */}
            <PrioritySuggestionBadge
                prediction={prediction}
                isLoading={mlLoading}
                onAccept={handleAcceptPriority}
            />

            {/* Spacer between badge and form */}
            {(prediction || mlLoading) && <div style={{ height: '12px' }} />}

            {/* Task Form */}
            <TaskForm
                mode="create"
                onSubmit={handleSubmit}
                isLoading={isLoading}
                error={error}
                success={success}
                backHref="/admin/tasks"
                // ML-accepted priority injected as override
                initialPriority={acceptedPriority ?? undefined}
                // Form change callback for ML context
                onFieldChange={handleFormChange}
            />
        </div>
    );
}
