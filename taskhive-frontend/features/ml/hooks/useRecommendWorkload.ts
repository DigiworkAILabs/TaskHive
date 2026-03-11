'use client';

/**
 * features/ml/hooks/useRecommendWorkload.ts
 * ──────────────────────────────────────────
 * React hook for workload balance recommendation.
 *
 * Usage in TaskForm:
 *   const { recommend, recommendation, isLoading, reset } = useRecommendWorkload();
 *   await recommend({ taskTitle, taskPriority, candidateEmployeeIds });
 *
 * The hook is non-blocking — it never throws to the UI.
 * On any error (ML down / network issue) it silently sets recommendation to null.
 */

import { useState, useCallback } from 'react';
import { mlService } from '../services/mlService';
import type {
    WorkloadRecommendation,
    WorkloadRecommendationRequestDto
} from '../types/ml.types';

interface UseRecommendWorkloadReturn {
    /** Trigger a workload recommendation request */
    recommend: (data: WorkloadRecommendationRequestDto) => Promise<void>;
    /** The latest recommendation result, or null if not yet fetched */
    recommendation: WorkloadRecommendation | null;
    /** True while the HTTP request is in-flight */
    isLoading: boolean;
    /** Non-critical error message (shown as a soft warning) */
    error: string | null;
    /** Reset state */
    reset: () => void;
}

export function useRecommendWorkload(): UseRecommendWorkloadReturn {
    const [recommendation, setRecommendation] = useState<WorkloadRecommendation | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const recommend = useCallback(async (data: WorkloadRecommendationRequestDto) => {
        // Guard: title and candidates are required
        if (!data.taskTitle?.trim() || !data.candidateEmployeeIds || data.candidateEmployeeIds.length === 0) {
            return;
        }

        setIsLoading(true);
        setError(null);

        try {
            const result = await mlService.recommendWorkload(data);
            setRecommendation(result);
        } catch (err: unknown) {
            // Non-blocking: log but don't surface as a hard error
            console.warn('[useRecommendWorkload] ML request failed:', err);
            setError('Workload recommendation unavailable');
            setRecommendation(null);
        } finally {
            setIsLoading(false);
        }
    }, []);

    const reset = useCallback(() => {
        setRecommendation(null);
        setError(null);
        setIsLoading(false);
    }, []);

    return { recommend, recommendation, isLoading, error, reset };
}
