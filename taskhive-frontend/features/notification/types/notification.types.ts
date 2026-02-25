export type NotificationType =
    | 'TASK_ASSIGNED'
    | 'TASK_STATUS_CHANGED'
    | 'TASK_COMMENT_ADDED'
    | 'TASK_OVERDUE'
    | 'TASK_COMPLETED';

export type EmailStatus = 'PENDING' | 'SENT' | 'FAILED';

export interface Notification {
    id: string;
    type: NotificationType;
    title: string;
    message: string;
    read: boolean;
    readAt: string | null;
    entityType: string | null;
    entityId: string | null;
    createdAt: string;
}

// ────────────────────────────────────────────────────────────────────────────
// Backend shape: GET /notifications  →  ApiResponse<NotificationListResponse>
//   where NotificationListResponse = { notifications: PageResponse<NotificationResponse>, unreadCount: long }
// ────────────────────────────────────────────────────────────────────────────

/** Mirrors com.digiwork.taskhive.common.dto.PageResponse<NotificationResponse> */
export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
}

/** Mirrors com.digiwork.taskhive.module.notification.dto.NotificationListResponse */
export interface NotificationListResponse {
    notifications: PageResponse<Notification>;
    unreadCount: number;
}

// ────────────────────────────────────────────────────────────────────────────
// Backend shape: GET /notifications/unread-count  →  ApiResponse<Long>
//   data field is a raw number, NOT an object
// ────────────────────────────────────────────────────────────────────────────

// (We handle this as `number` directly in the service, no wrapper type needed)

// ────────────────────────────────────────────────────────────────────────────
// Preferences
// ────────────────────────────────────────────────────────────────────────────

export interface NotificationPreference {
    id: string;
    userId: string;
    emailEnabled: boolean;
    inAppEnabled: boolean;
    taskAssigned: boolean;
    taskOverdue: boolean;
    dailyDigest: boolean;
    digestTime: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface NotificationPreferenceRequest {
    emailEnabled: boolean;
    inAppEnabled: boolean;
    taskAssigned: boolean;
    taskOverdue: boolean;
    dailyDigest: boolean;
    digestTime: string | null;
}

// ────────────────────────────────────────────────────────────────────────────
// WebSocket payload  (sent via STOMP /topic/notifications/{userId})
// ────────────────────────────────────────────────────────────────────────────

export interface WebSocketNotificationPayload {
    id: string;
    type: NotificationType;
    title: string;
    message: string;
    entityType: string | null;
    entityId: string | null;
    createdAt: string;
}
