'use client';

import React from 'react';
import { useCreateTask } from '@/features/task/hooks/useCreateTask';
import { TaskForm } from '@/features/task/components/TaskForm';
import { CreateTaskData } from '@/features/task/types/task.types';
import Link from 'next/link';
import { ArrowLeft } from 'lucide-react';

export default function NewTaskPage() {
    const { createTask, isLoading, error, success } = useCreateTask();

    const handleSubmit = async (data: CreateTaskData | any) => {
        await createTask(data as CreateTaskData);
    };

    return (
        <div>
            {/* Header */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '28px' }}>
                <Link
                    href="/admin/tasks"
                    style={{ color: '#71717a', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '6px', fontSize: '14px', transition: 'color 0.15s' }}
                    onMouseEnter={(e) => (e.currentTarget.style.color = '#f97316')}
                    onMouseLeave={(e) => (e.currentTarget.style.color = '#71717a')}
                >
                    <ArrowLeft size={16} />
                    Back to Tasks
                </Link>
                <span style={{ color: '#2a2a2a' }}>|</span>
                <h1 style={{ fontSize: '20px', fontWeight: 700, color: '#ffffff', margin: 0 }}>Create New Task</h1>
            </div>

            <TaskForm
                mode="create"
                onSubmit={handleSubmit}
                isLoading={isLoading}
                error={error}
                success={success}
                backHref="/admin/tasks"
            />
        </div>
    );
}
