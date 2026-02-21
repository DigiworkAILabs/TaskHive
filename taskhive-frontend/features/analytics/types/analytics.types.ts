// Analytics and Dashboard Types

export interface AdminDashboardResponse {
    totalEmployees: number;
    employeesGrowth: number; // e.g. 12
    pendingTasks: number;
    pendingTasksStatus: 'attention' | 'on-track' | 'normal';
    inProgressTasks: number;
    inProgressTasksStatus: 'on-track' | 'at-risk' | 'normal';
    completedTasks: number;
    completedTasksGrowth: number; // e.g. 8

    recentTasks: DashboardTask[];
    taskDistribution: TaskDistribution[];
    upcomingDeadlines: UpcomingDeadline[];
}

export interface DashboardTask {
    id: string;
    title: string;
    assigneeName: string;
    assigneeAvatar?: string;
    dueDate: string;
    status: 'TODO' | 'IN_PROGRESS' | 'IN_REVIEW' | 'DONE';
    priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
}

export interface TaskDistribution {
    category: string;
    percentage: number;
}

export interface UpcomingDeadline {
    id: string;
    title: string;
    dueDate: string;
    color: string; // hex or color name
}
