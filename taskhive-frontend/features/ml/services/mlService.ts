/**
 * features/ml/services/mlService.ts
 * ────────────────────────────────────
 * API service layer for all ML feature HTTP calls.
 * Phase 7.1: predictTaskPriority only.
 *
 * Uses the shared apiClient (axios with cookie auth) — same as taskService.ts.
 * Spring Boot endpoint is ADMIN-only; auth cookie is attached automatically.
 */

import apiClient from '@/shared/services/api/apiClient';
import type {
    TaskPriorityRequestDto,
    TaskPriorityPrediction,
    TaskPriorityApiResponse,
    CompletionTimeRequestDto,
    CompletionTimePrediction,
    CompletionTimeApiResponse,
} from '../types/ml.types';

const ML_BASE = '/ml';

export const mlService = {
    /**
     * Feature 1 — Task Priority Suggestion
     *
     * Sends task title + description + metadata to Spring Boot,
     * which enriches with employee data and forwards to FastAPI.
     *
     * Always resolves — Spring Boot returns MEDIUM fallback if ML is down.
     *
     * @param data   Request payload from the Create Task form
     * @returns      Prediction with confidence score and reasoning
     */
    predictTaskPriority: async (
        data: TaskPriorityRequestDto
    ): Promise<TaskPriorityPrediction> => {
        const response = await apiClient.post<TaskPriorityApiResponse>(
            `${ML_BASE}/predict/task-priority`,
            data
        );
        return response.data.data;
    },

    /**
     * Feature 2 — Task Completion Time Estimation
     *
     * Sends task title, description, selected priority, and employee ID.
     * Spring Boot enriches it and fetches from FastAPI.
     *
     * @param data Request payload
     * @returns Prediction with confidence range and reasoning
     */
    predictCompletionTime: async (
        data: CompletionTimeRequestDto
    ): Promise<CompletionTimePrediction> => {
        const response = await apiClient.post<CompletionTimeApiResponse>(
            `${ML_BASE}/predict/completion-time`,
            data
        );
        return response.data.data;
    },
};
