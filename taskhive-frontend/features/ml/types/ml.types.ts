/**
 * ml.types.ts
 * ────────────
 * TypeScript types for all ML feature API contracts.
 * Phase 7.1: Task Priority Suggestion
 * Phase 7.2: Task Completion Time Estimation
 * Phase 7.3: Workload Balance Recommendation
 Phase 7.4: Employee Productivity Scoring
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

// ── Feature 2: Task Completion Time Estimation ───────────────────────────────

/**
 * Request body sent from Next.js to Spring Boot.
 * POST /api/v1/ml/predict/completion-time
 */
export interface CompletionTimeRequestDto {
    taskTitle: string;
    taskDescription?: string;
    priority: TaskPriority;
    employeeId: string;
    estimatedHours?: number; // admin manual input
}

export interface ConfidenceRange {
    low: number;
    high: number;
}

/**
 * Inner data object returned by Spring Boot's ApiResponse<CompletionTimeResponse>.
 */
export interface CompletionTimePrediction {
    estimatedHours: number;
    confidenceRange: ConfidenceRange;
    reasoning: string;
    fallbackUsed: boolean;
}

/**
 * Full Spring Boot ApiResponse<CompletionTimeResponse> shape.
 */
export interface CompletionTimeApiResponse {
    success: boolean;
    message: string;
    data: CompletionTimePrediction;
    timestamp: string;
}

// ── Feature 3: Workload Balance Recommendation ──────────────────────────

/**
 * Request body sent from Next.js to Spring Boot.
 * POST /api/v1/ml/recommend/workload-balance
 */
export interface WorkloadRecommendationRequestDto {
    taskTitle: string;
    taskPriority: TaskPriority;
    taskEstimatedHours?: number;
    candidateEmployeeIds: string[];
}

/**
 * Individual score breakdown for a candidate.
 */
export interface EmployeeScoreBreakdown {
    employeeId: string;
    score: number;
}

/**
 * Inner data object returned by Spring Boot's ApiResponse<WorkloadRecommendationResponse>.
 */
export interface WorkloadRecommendation {
    recommendedEmployeeId: string | null;
    scoreBreakdown: EmployeeScoreBreakdown[];
    reasoning: string;
    fallbackUsed: boolean;
}

/**
 * Full Spring Boot ApiResponse<WorkloadRecommendationResponse> shape.
 */
export interface WorkloadRecommendationApiResponse {
    success: boolean;
    message: string;
    data: WorkloadRecommendation;
    timestamp: string;
}
// ── Feature 4: Employee Productivity Scoring ──────────────────────────

export interface ProductivityBreakdown {
    completion_rate_score: number;
    on_time_score: number;
    overdue_penalty: number;
    engagement_score: number;
}

export type PerformanceTrend = 'improving' | 'stable' | 'declining';

/**
 * Inner data object for Productivity Score.
 */
export interface ProductivityScore {
    score: number;
    grade: 'A' | 'B' | 'C' | 'D' | 'F';
    breakdown: ProductivityBreakdown;
    trend: PerformanceTrend;
    reasoning: string;
    fallbackUsed: boolean;
}

/**
 * Full Spring Boot ApiResponse<ProductivityScoreResponse> shape.
 */
export interface ProductivityScoreApiResponse {
    success: boolean;
    message: string;
    data: ProductivityScore;
    timestamp: string;
}
