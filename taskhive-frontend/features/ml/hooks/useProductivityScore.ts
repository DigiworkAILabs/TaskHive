import { useState, useEffect, useCallback } from 'react';
import { mlService } from '../services/mlService';
import { ProductivityScore } from '../types/ml.types';

/**
 * useProductivityScore
 * ───────────────────
 * Hook to fetch and manage the ML productivity score for an employee.
 * 
 * @param employeeId   UUID of the employee to score
 * @param periodDays   Review period (default 30)
 */
export function useProductivityScore(employeeId?: string, periodDays: number = 30) {
    const [scoreData, setScoreData] = useState<ProductivityScore | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const fetchScore = useCallback(async () => {
        if (!employeeId) return;

        setIsLoading(true);
        setError(null);
        try {
            const data = await mlService.getProductivityScore(employeeId, periodDays);
            setScoreData(data);
        } catch (err: any) {
            console.error('[useProductivityScore] Error:', err);
            setError(err.response?.data?.message || 'Failed to calculate productivity score');
        } finally {
            setIsLoading(false);
        }
    }, [employeeId, periodDays]);

    useEffect(() => {
        fetchScore();
    }, [fetchScore]);

    return {
        scoreData,
        isLoading,
        error,
        refetch: fetchScore
    };
}
