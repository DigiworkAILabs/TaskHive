'use client';

import { TaskDetail } from '@/features/task/components/TaskDetail';

export default function AdminTaskDetailPage() {
    return <TaskDetail isAdmin={true} />;
}
