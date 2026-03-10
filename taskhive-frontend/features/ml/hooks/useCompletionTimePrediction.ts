import { useState, useCallback } from 'react';
import { mlService } from '../services/mlService';
import { CompletionTimeRequestDto, CompletionTimePrediction } from '../types/ml.types';

export const useCompletionTimePrediction = () => {
    const [prediction, setPrediction] = useState<CompletionTimePrediction | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const checkCompletionTime = useCallback(async (data: CompletionTimeRequestDto) => {
        // Only trigger if we have all necessary fields
        if (!data.taskTitle || !data.priority || !data.employeeId || data.taskTitle.length < 2) {
            setPrediction(null);
            return;
        }

        setIsLoading(true);
        setError(null);

        try {
            const result = await mlService.predictCompletionTime(data);
            setPrediction(result);
        } catch (err: any) {
            console.error('Failed to get completion time prediction:', err);
            // Don't throw, just set error so UI can fail gracefully
            setError(err.message || 'Failed to fetch prediction');
            setPrediction(null);
        } finally {
            setIsLoading(false);
        }
    }, []);

    const clearPrediction = useCallback(() => {
        setPrediction(null);
        setError(null);
    }, []);

    return {
        prediction,
        isLoading,
        error,
        checkCompletionTime,
        clearPrediction,
    };
};
