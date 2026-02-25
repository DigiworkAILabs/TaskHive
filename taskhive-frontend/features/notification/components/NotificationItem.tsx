import { useRouter } from 'next/navigation';
import { ClipboardList, RefreshCw, MessageSquare, AlertTriangle, CheckCircle2 } from 'lucide-react';
import { useMarkAsRead } from '../hooks/useMarkAsRead';
import type { Notification, NotificationType } from '../types/notification.types';
import { useAuthStore } from '@/features/auth/store/authStore';

function timeAgo(dateString: string): string {
    const now = new Date();
    const date = new Date(dateString);
    const diff = Math.floor((now.getTime() - date.getTime()) / 1000);
    if (diff < 60) return 'Just now';
    if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
    if (diff < 604800) return `${Math.floor(diff / 86400)}d ago`;
    return date.toLocaleDateString();
}

const NOTIFICATION_ICON_MAP: Record<
    NotificationType,
    { icon: React.ElementType; bgClass: string; iconClass: string }
> = {
    TASK_ASSIGNED: { icon: ClipboardList, bgClass: 'bg-blue-500/15', iconClass: 'text-blue-400' },
    TASK_STATUS_CHANGED: { icon: RefreshCw, bgClass: 'bg-green-500/15', iconClass: 'text-green-400' },
    TASK_COMMENT_ADDED: { icon: MessageSquare, bgClass: 'bg-purple-500/15', iconClass: 'text-purple-400' },
    TASK_OVERDUE: { icon: AlertTriangle, bgClass: 'bg-red-500/15', iconClass: 'text-red-400' },
    TASK_COMPLETED: { icon: CheckCircle2, bgClass: 'bg-emerald-500/15', iconClass: 'text-emerald-400' },
};

export function NotificationItem({ notification }: { notification: Notification }) {
    const router = useRouter();
    const { mutate: markRead } = useMarkAsRead();
    const user = useAuthStore((s) => s.user);

    function handleClick() {
        // 1. Mark as read if not already read
        if (!notification.read) {
            markRead(notification.id);
        }
        // 2. Navigate to task if entityType is TASK
        if (notification.entityType === 'TASK' && notification.entityId) {
            const basePath = user?.role === 'ADMIN' ? '/admin' : '/employee';
            router.push(`${basePath}/tasks/${notification.entityId}`);
        }
    }

    const { icon: Icon, bgClass, iconClass } =
        NOTIFICATION_ICON_MAP[notification.type] ?? {
            icon: ClipboardList,
            bgClass: 'bg-zinc-700/50',
            iconClass: 'text-zinc-400',
        };

    return (
        <div
            onClick={handleClick}
            className={[
                'flex items-start gap-3 px-4 py-3 cursor-pointer transition-colors duration-150',
                'border-b border-zinc-700/30 last:border-b-0',
                notification.read
                    ? 'hover:bg-zinc-800/40'
                    : 'bg-zinc-800/80 border-l-2 border-orange-500 hover:bg-zinc-700/50',
            ].join(' ')}
        >
            {/* Icon bubble */}
            <div className={`w-9 h-9 rounded-full flex items-center justify-center shrink-0 ${bgClass}`}>
                <Icon className={`w-4 h-4 ${iconClass}`} />
            </div>

            {/* Text content */}
            <div className="flex-1 min-w-0">
                <p className={`text-sm leading-tight ${notification.read ? 'text-zinc-300 font-normal' : 'text-white font-medium'}`}>
                    {notification.title}
                </p>
                <p className="text-xs text-zinc-400 mt-0.5 line-clamp-2 leading-relaxed">
                    {notification.message}
                </p>
                <p className="text-xs text-zinc-500 mt-1">{timeAgo(notification.createdAt)}</p>
            </div>

            {/* Unread indicator dot */}
            {!notification.read && (
                <span className="w-2 h-2 rounded-full bg-orange-500 shrink-0 mt-1.5" />
            )}
        </div>
    );
}
