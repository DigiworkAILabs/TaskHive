'use client';

import { useEffect } from 'react';
import { Bell, Loader2, Settings } from 'lucide-react';
import Link from 'next/link';
import { useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from '@/components/ui/popover';
import { useAuthStore } from '@/features/auth/store/authStore';
import { webSocketService } from '../services/webSocketService';
import { useUnreadCount } from '../hooks/useNotifications';
import { useMarkAllAsRead } from '../hooks/useMarkAsRead';
import { NotificationList } from './NotificationList';
import type { WebSocketNotificationPayload } from '../types/notification.types';

export function NotificationBell() {
    const user = useAuthStore((s) => s.user);
    const queryClient = useQueryClient();
    const { data: unreadData } = useUnreadCount();
    const { mutate: markAll, isPending: isMarkingAll } = useMarkAllAsRead();

    // Backend returns a raw number (Long) for unread count, not an object
    const unreadCount = typeof unreadData === 'number' ? unreadData : 0;

    useEffect(() => {
        if (!user?.id) return;

        webSocketService.connect(user.id, (payload: WebSocketNotificationPayload) => {
            queryClient.invalidateQueries({ queryKey: ['notifications'] });
            queryClient.invalidateQueries({ queryKey: ['notifications', 'unread-count'] });

            toast(payload.title, {
                description: payload.message,
                duration: 5000,
                style: {
                    background: '#18181b',
                    border: '1px solid #3f3f46',
                    color: '#ffffff',
                },
            });
        });

        return () => {
            webSocketService.disconnect();
        };
    }, [user?.id, queryClient]);

    const settingsPath =
        user?.role === 'ADMIN' ? '/admin/dashboard' : '/employee/settings/security';

    return (
        <Popover>
            {/* ── Bell trigger ── */}
            <PopoverTrigger asChild>
                <button
                    className="relative w-9 h-9 flex items-center justify-center rounded-lg
                               bg-zinc-800/50 hover:bg-zinc-700/50
                               border border-zinc-700/50 hover:border-zinc-600
                               transition-all duration-150 outline-none focus-visible:ring-2
                               focus-visible:ring-orange-500"
                    aria-label="Notifications"
                >
                    <Bell className="w-[18px] h-[18px] text-zinc-400" />

                    {unreadCount > 0 && (
                        <>
                            {/* Ping animation */}
                            <span className="absolute -top-1.5 -right-1.5 w-[18px] h-[18px]
                                            rounded-full bg-red-500/40 animate-ping pointer-events-none" />
                            {/* Badge — fixed: h-[18px] not h={18} */}
                            <span className="absolute -top-1.5 -right-1.5 min-w-[18px] h-[18px] px-1
                                            rounded-full bg-red-500 text-white text-[10px] font-bold
                                            flex items-center justify-center leading-none pointer-events-none">
                                {unreadCount > 99 ? '99+' : unreadCount}
                            </span>
                        </>
                    )}
                </button>
            </PopoverTrigger>

            {/* ── Popover panel ── */}
            <PopoverContent
                align="end"
                sideOffset={8}
                className="w-[calc(100vw-2rem)] max-w-80 p-0 bg-zinc-900 border border-zinc-700/50
                           rounded-xl shadow-2xl shadow-black/60 overflow-hidden"
            >
                {/* Header */}
                <div className="flex items-center justify-between px-4 py-3
                                border-b border-zinc-700/50 bg-zinc-800/50">
                    <div className="flex items-center gap-2">
                        <span className="text-sm font-semibold text-white">Notifications</span>
                        {unreadCount > 0 && (
                            <span className="bg-orange-500/20 text-orange-400 text-xs
                                            px-1.5 py-0.5 rounded-md font-medium">
                                {unreadCount}
                            </span>
                        )}
                    </div>

                    {unreadCount > 0 && (
                        <button
                            onClick={() => markAll()}
                            disabled={isMarkingAll}
                            className="text-xs text-zinc-400 hover:text-orange-400
                                       transition-colors flex items-center gap-1 disabled:opacity-50"
                        >
                            {isMarkingAll ? (
                                <Loader2 className="w-3 h-3 animate-spin" />
                            ) : (
                                'Mark all read'
                            )}
                        </button>
                    )}
                </div>

                {/* Notification list */}
                <NotificationList />

                {/* Footer */}
                <div className="px-4 py-2.5 border-t border-zinc-700/50 bg-zinc-800/30">
                    <Link
                        href={settingsPath}
                        className="flex items-center justify-center gap-1.5
                                   text-xs text-zinc-500 hover:text-orange-400 transition-colors"
                    >
                        <Settings className="w-3 h-3" />
                        Notification settings
                    </Link>
                </div>
            </PopoverContent>
        </Popover>
    );
}
