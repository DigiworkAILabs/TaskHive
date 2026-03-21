// ── Enums ────────────────────────────────────────────────────────────────────
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'PENDING_APPROVAL' | 'DONE' | 'CANCELLED';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type AttachmentPurpose = 'GENERAL' | 'PROOF';

// ── Full Task (GET /tasks/{id}) ───────────────────────────────────────────────
export interface Task {
    id: string;
    title: string;
    description: string;
    status: TaskStatus;
    priority: TaskPriority;
    assignedTo: string;         // employee id
    assigneeName?: string;    // populated by backend (matches JSON key from backend)
    dueDate: string;            // ISO date string
    completedAt: string | null;
    estimatedHours: number | null;
    tags: string[];
    isDeleted: boolean;
    version: number;
    createdBy: string;
    updatedBy: string;
    createdAt: string;
    updatedAt: string;
    // Phase 1 v2.5
    isLate?: boolean;
    lateByMinutes?: number | null;
    submittedAt?: string | null;
    proofRequired?: boolean;
    approvalRequired?: boolean;
}

// ── Task List Item (GET /tasks) ───────────────────────────────────────────────
export interface TaskListItem {
    id: string;
    title: string;
    status: TaskStatus;
    priority: TaskPriority;
    assignedTo: string;
    assigneeName: string;
    dueDate: string;
    tags: string[];
    createdAt: string;
    // Phase 1 v2.5
    isLate?: boolean;
}

// ── Create Task DTO ───────────────────────────────────────────────────────────
export interface CreateTaskData {
    title: string;
    description?: string;
    priority: TaskPriority;
    assignedTo: string;
    dueDate: string;
    estimatedHours?: number;
    tags?: string[];
    proofRequired?: boolean;     // P1.1
    approvalRequired?: boolean;  // P1.2
}

// ── Update Task DTO ───────────────────────────────────────────────────────────
export interface UpdateTaskData {
    title?: string;
    description?: string;
    priority?: TaskPriority;
    assignedTo?: string;
    dueDate?: string;
    estimatedHours?: number;
    tags?: string[];
    proofRequired?: boolean;     // P1.1
    approvalRequired?: boolean;  // P1.2
}

// ── Update Status DTO ─────────────────────────────────────────────────────────
export interface UpdateTaskStatusData {
    status: TaskStatus;
    comment?: string;
    reason?: string;  // mandatory for CANCELLED
}

// ── Task Comment ──────────────────────────────────────────────────────────────
export interface TaskComment {
    id: string;
    taskId: string;
    authorId: string;
    authorName: string;
    content: string;
    createdAt: string;
}

export interface AddCommentData {
    content: string;
}

// ── Task Attachment ───────────────────────────────────────────────────────────
export interface TaskAttachment {
    id: string;
    taskId: string;
    uploadedBy: string;
    uploaderName: string;       // matches backend TaskAttachmentResponse.uploaderName
    fileName: string;
    fileUrl: string;
    fileSize: number;
    mimeType: string;
    attachmentPurpose?: AttachmentPurpose;  // 'PROOF' | 'GENERAL' — sent by backend since P1.1 fix
    createdAt: string;
}

// ── Task Status History ───────────────────────────────────────────────────────
export interface TaskStatusHistory {
    id: string;
    taskId: string;
    oldStatus: TaskStatus | null;
    newStatus: TaskStatus;
    changedBy: string;
    changedByName: string;
    comment: string | null;
    ipAddress: string | null;
    changedAt: string;
}

// ── Filters ───────────────────────────────────────────────────────────────────
export interface TaskFilters {
    status?: TaskStatus;
    priority?: TaskPriority;
    assignedTo?: string;
    search?: string;
    page: number;
    size: number;
    sortBy?: string;
    sortDir?: 'asc' | 'desc';
}

// ── Pagination wrapper ────────────────────────────────────────────────────────
export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

// ── Analytics ─────────────────────────────────────────────────────────────────
export interface TodayOverview {
    totalAssignedToday: number;
    completedToday: number;
    inProgress: number;
    pendingApproval: number;
    overdueToday: number;
    lateSubmissionsToday: number;
    activeAnomalies: number;
}

export interface AnomalyAlert {
    ruleId: string;
    description: string;
    severity: 'HIGH' | 'MEDIUM' | 'LOW';
    detectedAt: string;
}
