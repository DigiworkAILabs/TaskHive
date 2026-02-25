import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import {
    getNotificationPreferences,
    updateNotificationPreferences,
} from '../services/notificationService';
import type { NotificationPreferenceRequest } from '../types/notification.types';

export function useNotificationPreferences() {
    return useQuery({
        queryKey: ['notification-preferences'],
        queryFn: getNotificationPreferences,
        staleTime: 60_000,
    });
}

export function useUpdateNotificationPreferences() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (data: NotificationPreferenceRequest) =>
            updateNotificationPreferences(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['notification-preferences'] });
            toast.success('Preferences saved successfully');
        },
        onError: (error: Error) => {
            toast.error(error.message || 'Failed to save preferences');
        },
    });
}
