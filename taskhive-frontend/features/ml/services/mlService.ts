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
    WorkloadRecommendationRequestDto,
    WorkloadRecommendation,
    WorkloadRecommendationApiResponse,
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
     * Feature 3 — Workload Balance Recommendation
     *
     * Sends task info + list of candidate employee IDs to Spring Boot.
     * Spring Boot enriches each candidate with performance stats and
     * forwards to FastAPI for ranking.
     *
     * Always resolves — returns null recommendation if ML is down.
     *
     * @param data   Request payload with task info and candidate IDs
     * @returns      Recommended employee ID and score breakdown
     */
    recommendWorkload: async (
        data: WorkloadRecommendationRequestDto
    ): Promise<WorkloadRecommendation> => {
        const response = await apiClient.post<WorkloadRecommendationApiResponse>(
            `${ML_BASE}/recommend/workload-balance`,
            data
        );
        return response.data.data;
    },
};
