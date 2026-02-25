import { useQuery } from '@tanstack/react-query';
import {
    getNotifications,
    getUnreadNotifications,
    getUnreadCount,
} from '../services/notificationService';

// All notifications (paginated)
// Returns NotificationListResponse = { notifications: PageResponse<Notification>, unreadCount }
export function useNotifications(page = 0, size = 10) {
    return useQuery({
        queryKey: ['notifications', { page, size }],
        queryFn: () => getNotifications(page, size),
        staleTime: 30_000,
    });
}

// Unread notifications only (paginated)
export function useUnreadNotifications(page = 0, size = 10) {
    return useQuery({
        queryKey: ['notifications', 'unread', { page, size }],
        queryFn: () => getUnreadNotifications(page, size),
        staleTime: 30_000,
    });
}

// Unread count for the bell badge
// getUnreadCount() returns a raw number (e.g. 5), NOT { count: 5 }
// refetchInterval = 30s as polling fallback in case WebSocket misses an event
export function useUnreadCount() {
    return useQuery({
        queryKey: ['notifications', 'unread-count'],
        queryFn: getUnreadCount,
        staleTime: 10_000,
        refetchInterval: 30_000,
    });
}
