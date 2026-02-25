import type {
    NotificationListResponse,
    NotificationPreference,
    NotificationPreferenceRequest,
} from '../types/notification.types';

const BASE = process.env.NEXT_PUBLIC_API_URL;

// ────────────────────────────────────────────────────────────────────────────
// GET /api/v1/notifications?page={page}&size={size}
//   Backend returns: ApiResponse<NotificationListResponse>
//   Where  NotificationListResponse = { notifications: PageResponse<Notification>, unreadCount: long }
// ────────────────────────────────────────────────────────────────────────────
export async function getNotifications(
    page = 0,
    size = 20
): Promise<NotificationListResponse> {
    const res = await fetch(`${BASE}/notifications?page=${page}&size=${size}`, {
        credentials: 'include',
    });
    if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || 'Failed to fetch notifications');
    }
    const body = await res.json();
    // body.data is the NotificationListResponse wrapper
    return body.data;
}

// ────────────────────────────────────────────────────────────────────────────
// GET /api/v1/notifications/unread?page={page}&size={size}
//   Backend returns: ApiResponse<PageResponse<NotificationResponse>>
// ────────────────────────────────────────────────────────────────────────────
export async function getUnreadNotifications(
    page = 0,
    size = 20
): Promise<NotificationListResponse> {
    const res = await fetch(`${BASE}/notifications/unread?page=${page}&size=${size}`, {
        credentials: 'include',
    });
    if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || 'Failed to fetch unread notifications');
    }
    const body = await res.json();
    return body.data;
}

// ────────────────────────────────────────────────────────────────────────────
// GET /api/v1/notifications/unread-count
//   Backend returns: ApiResponse<Long>
//   body.data is a RAW NUMBER (e.g. 3), NOT an object like { count: 3 }
// ────────────────────────────────────────────────────────────────────────────
export async function getUnreadCount(): Promise<number> {
    const res = await fetch(`${BASE}/notifications/unread-count`, {
        credentials: 'include',
    });
    if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || 'Failed to fetch unread count');
    }
    const body = await res.json();
    // body.data is a raw number (Long), e.g. 5
    return body.data as number;
}

// ────────────────────────────────────────────────────────────────────────────
// PATCH /api/v1/notifications/{id}/read
// ────────────────────────────────────────────────────────────────────────────
export async function markAsRead(id: string): Promise<void> {
    const res = await fetch(`${BASE}/notifications/${id}/read`, {
        method: 'PATCH',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
    });
    if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || 'Failed to mark notification as read');
    }
}

// ────────────────────────────────────────────────────────────────────────────
// PATCH /api/v1/notifications/mark-all-read
// ────────────────────────────────────────────────────────────────────────────
export async function markAllAsRead(): Promise<void> {
    const res = await fetch(`${BASE}/notifications/mark-all-read`, {
        method: 'PATCH',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
    });
    if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || 'Failed to mark all notifications as read');
    }
}

// ────────────────────────────────────────────────────────────────────────────
// GET /api/v1/notifications/preferences
//   Backend returns: ApiResponse<NotificationPreference>
// ────────────────────────────────────────────────────────────────────────────
export async function getNotificationPreferences(): Promise<NotificationPreference> {
    const res = await fetch(`${BASE}/notifications/preferences`, {
        credentials: 'include',
    });
    if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || 'Failed to fetch notification preferences');
    }
    const body = await res.json();
    return body.data;
}

// ────────────────────────────────────────────────────────────────────────────
// PUT /api/v1/notifications/preferences
//   Backend returns: ApiResponse<NotificationPreference>
// ────────────────────────────────────────────────────────────────────────────
export async function updateNotificationPreferences(
    data: NotificationPreferenceRequest
): Promise<NotificationPreference> {
    const res = await fetch(`${BASE}/notifications/preferences`, {
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
    });
    if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || 'Failed to update notification preferences');
    }
    const body = await res.json();
    return body.data;
}
