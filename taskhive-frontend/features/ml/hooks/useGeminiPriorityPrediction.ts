'use client';

/**
 * features/ml/hooks/useGeminiPriorityPrediction.ts
 * ────────────────────────────────────────────
 * React hook for task priority suggestion via Gemini.
 *
 * The hook is non-blocking — it never throws to the UI.
 * On any error (Gemini down / network issue) it silently sets prediction to null.
 */

import { useState, useCallback } from 'react';
import { mlService } from '../services/mlService';
import type { TaskPriorityPrediction, TaskPriorityRequestDto } from '../types/ml.types';

interface UseGeminiPriorityPredictionReturn {
    predict: (data: TaskPriorityRequestDto) => Promise<void>;
    prediction: TaskPriorityPrediction | null;
    isLoading: boolean;
    error: string | null;
    reset: () => void;
}

export function useGeminiPriorityPrediction(): UseGeminiPriorityPredictionReturn {
    const [prediction, setPrediction] = useState<TaskPriorityPrediction | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const predict = useCallback(async (data: TaskPriorityRequestDto) => {
        if (!data.taskTitle?.trim() || data.taskTitle.trim().length < 2) {
            return;
        }

        setIsLoading(true);
        setError(null);

        try {
            const result = await mlService.predictGeminiTaskPriority(data);
            setPrediction(result);
        } catch (err: unknown) {
            console.warn('[useGeminiPriorityPrediction] Gemini request failed:', err);
            setError('Gemini Suggestion unavailable');
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
