'use client';

/**
 * features/ml/hooks/usePriorityPrediction.ts
 * ────────────────────────────────────────────
 * React hook for task priority suggestion.
 *
 * Usage in TaskForm/page.tsx:
 *   const { predict, prediction, isLoading, reset } = usePriorityPrediction();
 *   await predict({ taskTitle, taskDescription, tags, estimatedHours, employeeId });
 *
 * The hook is non-blocking — it never throws to the UI.
 * On any error (ML down / network issue) it silently sets prediction to null.
 */

import { useState, useCallback } from 'react';
import { mlService } from '../services/mlService';
import type { TaskPriorityPrediction, TaskPriorityRequestDto } from '../types/ml.types';

interface UsePriorityPredictionReturn {
    /** Trigger a priority prediction request */
    predict: (data: TaskPriorityRequestDto) => Promise<void>;
    /** The latest prediction result, or null if not yet fetched */
    prediction: TaskPriorityPrediction | null;
    /** True while the HTTP request is in-flight */
    isLoading: boolean;
    /** Non-critical error message (shown as a soft warning, not a form error) */
    error: string | null;
    /** Reset state — called when form is cleared */
    reset: () => void;
}

export function usePriorityPrediction(): UsePriorityPredictionReturn {
    const [prediction, setPrediction] = useState<TaskPriorityPrediction | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const predict = useCallback(async (data: TaskPriorityRequestDto) => {
        // Guard: title is required
        if (!data.taskTitle?.trim() || data.taskTitle.trim().length < 2) {
            return;
        }

        setIsLoading(true);
        setError(null);

        try {
            const result = await mlService.predictTaskPriority(data);
            setPrediction(result);
        } catch (err: unknown) {
            // Non-blocking: log but don't surface as a hard error
            console.warn('[usePriorityPrediction] ML request failed:', err);
            setError('Priority suggestion unavailable');
            setPrediction(null);
        } finally {
            setIsLoading(false);
        }
    }, []);

    const reset = useCallback(() => {
        setPrediction(null);
        setError(null);
        setIsLoading(false);
    }, []);

    return { predict, prediction, isLoading, error, reset };
}
