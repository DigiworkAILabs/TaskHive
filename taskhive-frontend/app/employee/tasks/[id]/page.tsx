'use client';

import React from 'react';
import { TaskDetail } from '@/features/task/components/TaskDetail';

export default function EmployeeTaskDetailPage() {
    return (
        <TaskDetail isAdmin={false} />
    );
}
