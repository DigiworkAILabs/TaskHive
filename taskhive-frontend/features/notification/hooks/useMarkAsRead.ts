import { useMutation, useQueryClient } from '@tanstack/react-query';
import { markAsRead, markAllAsRead } from '../services/notificationService';

// Invalidate these 3 keys after every mark-read operation
const INVALIDATE_KEYS = [
    ['notifications'],
    ['notifications', 'unread'],
    ['notifications', 'unread-count'],
] as const;

export function useMarkAsRead() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: string) => markAsRead(id),
        onSuccess: () => {
            INVALIDATE_KEYS.forEach((key) =>
                queryClient.invalidateQueries({ queryKey: key })
            );
        },
    });
}

export function useMarkAllAsRead() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: markAllAsRead,
        onSuccess: () => {
            INVALIDATE_KEYS.forEach((key) =>
                queryClient.invalidateQueries({ queryKey: key })
            );
        },
    });
}
