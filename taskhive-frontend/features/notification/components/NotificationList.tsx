'use client';

import { BellOff } from 'lucide-react';
import { useNotifications } from '../hooks/useNotifications';
import { NotificationItem } from './NotificationItem';

// Skeleton row for loading state
function NotificationSkeleton() {
    return (
        <div className="flex items-start gap-3 px-4 py-3 border-b border-zinc-700/30">
            <div className="w-9 h-9 rounded-full bg-zinc-700/50 animate-pulse shrink-0" />
            <div className="flex-1 space-y-2 pt-1">
                <div className="h-3 w-3/4 rounded bg-zinc-700/50 animate-pulse" />
                <div className="h-3 w-1/2 rounded bg-zinc-700/50 animate-pulse" />
            </div>
        </div>
    );
}

export function NotificationList() {
    const { data, isLoading } = useNotifications(0, 20);
    // Backend shape: { notifications: { content: [...], page, size, ... }, unreadCount: N }
    const notifications = data?.notifications?.content ?? [];

    if (isLoading) {
        return (
            <div>
                <NotificationSkeleton />
                <NotificationSkeleton />
                <NotificationSkeleton />
            </div>
        );
    }

    if (notifications.length === 0) {
        return (
            <div className="py-12 flex flex-col items-center justify-center gap-3">
                <BellOff className="w-10 h-10 text-zinc-600" />
                <p className="text-sm text-zinc-500 font-medium">No notifications yet</p>
                <p className="text-xs text-zinc-600">You're all caught up!</p>
            </div>
        );
    }

    return (
        <div
            className="overflow-y-auto"
            style={{
                maxHeight: '400px',
                scrollbarWidth: 'thin',
                scrollbarColor: '#3f3f46 transparent',
            }}
        >
            {notifications.map((notification) => (
                <NotificationItem key={notification.id} notification={notification} />
            ))}
        </div>
    );
}
