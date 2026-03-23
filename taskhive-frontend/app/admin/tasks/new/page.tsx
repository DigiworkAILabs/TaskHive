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

import React, { useState, useRef, useEffect, useCallback } from 'react';
import Link from 'next/link';
import { ArrowLeft, Sparkles } from 'lucide-react';

import { useCreateTask } from '@/features/task/hooks/useCreateTask';
import { TaskForm } from '@/features/task/components/TaskForm';
import { CreateTaskData } from '@/features/task/types/task.types';
import { usePriorityPrediction } from '@/features/ml/hooks/usePriorityPrediction';
import { PrioritySuggestionBadge } from '@/features/ml/components/PrioritySuggestionBadge';
import { useGeminiPriorityPrediction } from '@/features/ml/hooks/useGeminiPriorityPrediction';
import { useCompletionTimePrediction } from '@/features/ml/hooks/useCompletionTimePrediction';
import { CompletionTimeEstimate } from '@/features/ml/components/CompletionTimeEstimate';
import { useMlStore } from '@/features/ml/store/mlStore';
import type { TaskPriority } from '@/features/ml/types/ml.types';

export default function NewTaskPage() {
    const { createTask, isLoading, error, success } = useCreateTask();
    const { predict, prediction, isLoading: mlLoading, reset: resetML } = usePriorityPrediction();
    const { predict: geminiPredict, prediction: geminiPrediction, isLoading: geminiLoading, reset: resetGemini } = useGeminiPriorityPrediction();
    const { checkCompletionTime, prediction: compPrediction, isLoading: compLoading, error: compError, clearPrediction: clearComp } = useCompletionTimePrediction();
    const { isMlEnabled, isGeminiEnabled } = useMlStore();

    // Track form field values to pass to ML — lifted from TaskForm via callback
    const [mlFormData, setMLFormData] = useState({
        taskTitle: '',
        taskDescription: '',
        priority: 'MEDIUM' as TaskPriority,
        employeeId: '',
        tags: [] as string[],
        estimatedHours: undefined as number | undefined,
    });

    // Accepted priority — passed down to TaskForm to override its internal state
    const [acceptedPriority, setAcceptedPriority] = useState<TaskPriority | null>(null);

    const handleSubmit = useCallback(async (data: any) => {
        await createTask(data as CreateTaskData);
        resetML();
        resetGemini();
        setAcceptedPriority(null);
    }, [createTask, resetML, resetGemini]);

    /** Called by TaskForm whenever its fields change (lifted state for ML) */
    const handleFormChange = useCallback((fields: {
        title?: string;
        description?: string;
        priority?: string;
        assignedTo?: string;
        tags?: string;
        estimatedHours?: string;
    }) => {
        console.log('[NewTaskPage] Field change:', fields);
        
        // If the user manually changes priority, clear the 'accepted' state
        if (fields.priority) {
            setAcceptedPriority(null);
        }

        setMLFormData(prev => ({
            ...prev,
            taskTitle: fields.title ?? prev.taskTitle,
            taskDescription: fields.description ?? prev.taskDescription,
            priority: (fields.priority as TaskPriority) ?? prev.priority,
            employeeId: fields.assignedTo ?? prev.employeeId,
            tags: fields.tags !== undefined
                ? fields.tags.split(',').map(t => t.trim()).filter(Boolean)
                : prev.tags,
            estimatedHours: fields.estimatedHours !== undefined
                ? (fields.estimatedHours ? parseFloat(fields.estimatedHours) : undefined)
                : prev.estimatedHours,
        }));
    }, []);

    /** Auto-trigger Completion Time Prediction */
    useEffect(() => {
        console.log('[NewTaskPage] ML State check:', {
            priority: mlFormData.priority,
            employeeId: mlFormData.employeeId,
            titleLength: mlFormData.taskTitle.trim().length
        });
        if (mlFormData.priority && mlFormData.employeeId && mlFormData.taskTitle.trim().length >= 2) {
            console.log('[NewTaskPage] Triggering completion time prediction');
            const timer = setTimeout(() => {
                checkCompletionTime({
                    taskTitle: mlFormData.taskTitle,
                    taskDescription: mlFormData.taskDescription,
                    priority: mlFormData.priority,
                    employeeId: mlFormData.employeeId,
                    estimatedHours: mlFormData.estimatedHours,
                });
            }, 600); // 600ms debounce
            return () => clearTimeout(timer);
        } else {
            clearComp();
        }
    }, [mlFormData.priority, mlFormData.employeeId, mlFormData.taskTitle, mlFormData.taskDescription, mlFormData.estimatedHours, checkCompletionTime, clearComp]);

    /** Fired when admin clicks "Suggest Priority" */
    const handleSuggestPriority = useCallback(async () => {
        await predict({
            taskTitle: mlFormData.taskTitle,
            taskDescription: mlFormData.taskDescription,
            employeeId: mlFormData.employeeId || undefined,
            tags: mlFormData.tags,
            estimatedHours: mlFormData.estimatedHours,
        });
    }, [predict, mlFormData]);

    /** Fired when admin clicks "Gemini Suggest Priority" */
    const handleGeminiSuggestPriority = useCallback(async () => {
        await geminiPredict({
            taskTitle: mlFormData.taskTitle,
            taskDescription: mlFormData.taskDescription,
            employeeId: mlFormData.employeeId || undefined,
            tags: mlFormData.tags,
            estimatedHours: mlFormData.estimatedHours,
        });
    }, [geminiPredict, mlFormData]);

    /** Fired when admin clicks "Accept" on the badge */
    const handleAcceptPriority = useCallback((priority: TaskPriority) => {
        setAcceptedPriority(priority);
    }, []);

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
                    {isMlEnabled && (
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
                    )}

                    {isGeminiEnabled && (
                        <button
                            type="button"
                            id="gemini-suggest-priority-btn"
                            onClick={handleGeminiSuggestPriority}
                            disabled={!canSuggest || geminiLoading}
                            title={canSuggest ? 'Click to get Gemini AI priority suggestion' : 'Enter a task title first'}
                            style={{
                                display: 'flex', alignItems: 'center', gap: '6px',
                                padding: '8px 16px', borderRadius: '8px',
                                border: '1px solid rgba(168,85,247,0.35)', // Purple border
                                background: canSuggest && !geminiLoading
                                    ? 'rgba(168,85,247,0.08)'
                                    : 'transparent',
                                color: canSuggest ? '#a855f7' : '#3f3f46', // Purple text
                                fontSize: '13px', fontWeight: 500, cursor: canSuggest ? 'pointer' : 'not-allowed',
                                transition: 'all 0.15s',
                                opacity: canSuggest ? 1 : 0.5,
                            }}
                            onMouseEnter={(e) => canSuggest && !geminiLoading && (e.currentTarget.style.background = 'rgba(168,85,247,0.15)')}
                            onMouseLeave={(e) => (e.currentTarget.style.background = canSuggest && !geminiLoading ? 'rgba(168,85,247,0.08)' : 'transparent')}
                        >
                            <Sparkles size={14} />
                            {geminiLoading ? 'Gemini Analysing…' : 'Gemini Suggest Priority'}
                        </button>
                    )}
                </div>
            </div>

            {/* ML Suggestion Badge — rendered above the form */}
            {isMlEnabled && (
                <PrioritySuggestionBadge
                    prediction={prediction}
                    isLoading={mlLoading}
                    onAccept={handleAcceptPriority}
                    variant="local"
                />
            )}

            {/* Gemini Suggestion Badge */}
            {isGeminiEnabled && (
                <PrioritySuggestionBadge
                    prediction={geminiPrediction}
                    isLoading={geminiLoading}
                    onAccept={handleAcceptPriority}
                    variant="gemini"
                />
            )}

            {/* Spacer between badge and form */}
            {(prediction || mlLoading || geminiPrediction || geminiLoading) && <div style={{ height: '12px' }} />}

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
                completionEstimateNode={
                    isMlEnabled ? (
                        <CompletionTimeEstimate
                            prediction={compPrediction}
                            isLoading={compLoading}
                            error={compError}
                        />
                    ) : null
                }
            />
        </div>
    );
}
