/**
 * ml.types.ts
 * ────────────
 * TypeScript types for all ML feature API contracts.
 * Phase 7.1: Task Priority Suggestion types only.
 */

// ── Shared ────────────────────────────────────────────────────────────────────

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

// ── Feature 1: Task Priority Suggestion ───────────────────────────────────────

/**
 * Request body sent from Next.js to Spring Boot.
 * POST /api/v1/ml/predict/task-priority
 */
export interface TaskPriorityRequestDto {
    taskTitle: string;
    taskDescription?: string;
    employeeId?: string;
    tags?: string[];
    estimatedHours?: number;
}

/**
 * Inner data object returned by Spring Boot's ApiResponse<TaskPriorityResponse>.
 */
export interface TaskPriorityPrediction {
    predictedPriority: TaskPriority;
    confidence: number;           // 0.0 – 1.0
    reasoning: string;
    fallbackUsed: boolean;
}

/**
 * Full Spring Boot ApiResponse<TaskPriorityResponse> shape.
 */
export interface TaskPriorityApiResponse {
    success: boolean;
    message: string;
    data: TaskPriorityPrediction;
    timestamp: string;
}
