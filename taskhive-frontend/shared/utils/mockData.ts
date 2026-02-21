import { AdminDashboardResponse } from '@/features/analytics/types/analytics.types';
import { EmployeeListResponse } from '@/features/employee/types/employee.types';

export const MOCK_DASHBOARD_DATA: AdminDashboardResponse = {
    totalEmployees: 48,
    employeesGrowth: 12,
    pendingTasks: 16,
    pendingTasksStatus: 'attention',
    inProgressTasks: 24,
    inProgressTasksStatus: 'on-track',
    completedTasks: 156,
    completedTasksGrowth: 8,
    recentTasks: [
        {
            id: '1',
            title: 'Redesign Dashboard UI',
            assigneeName: 'Alex Morgan',
            assigneeAvatar: '',
            dueDate: '2026-03-15',
            status: 'IN_PROGRESS',
            priority: 'HIGH'
        },
        {
            id: '2',
            title: 'Verify Employee API',
            assigneeName: 'Sarah Chen',
            assigneeAvatar: '',
            dueDate: '2026-03-10',
            status: 'TODO',
            priority: 'MEDIUM'
        },
        {
            id: '3',
            title: 'Database Migration',
            assigneeName: 'James Wilson',
            assigneeAvatar: '',
            dueDate: '2026-03-05',
            status: 'DONE',
            priority: 'CRITICAL'
        },
        {
            id: '4',
            title: 'Optimize Assets',
            assigneeName: 'Emma Davis',
            assigneeAvatar: '',
            dueDate: '2026-03-12',
            status: 'IN_REVIEW',
            priority: 'LOW'
        }
    ],
    taskDistribution: [
        { category: 'Marketing', percentage: 45 },
        { category: 'Development', percentage: 32 },
        { category: 'Design', percentage: 23 }
    ],
    upcomingDeadlines: [
        { id: '1', title: 'Lumina Revamp Launch', dueDate: 'March 24, 2026', color: 'bg-orange-500' },
        { id: '2', title: 'Q1 Performance Review', dueDate: 'March 28, 2026', color: 'bg-blue-500' },
        { id: '3', title: 'Security Audit', dueDate: 'April 02, 2026', color: 'bg-purple-500' }
    ]
};

export const MOCK_EMPLOYEES: EmployeeListResponse = {
    content: [
        {
            id: 'e1',
            firstName: 'Alex',
            lastName: 'Morgan',
            email: 'alex.m@taskhive.com',
            department: 'Design',
            designation: 'Lead Designer',
            joinDate: '2025-01-15',
            status: 'ACTIVE',
            version: 1,
            createdAt: '2025-01-15',
            updatedAt: '2025-01-15',
            isDeleted: false
        },
        {
            id: 'e2',
            firstName: 'Sarah',
            lastName: 'Chen',
            email: 's.chen@taskhive.com',
            department: 'Engineering',
            designation: 'Senior Developer',
            joinDate: '2024-11-20',
            status: 'ACTIVE',
            version: 1,
            createdAt: '2024-11-20',
            updatedAt: '2024-11-20',
            isDeleted: false
        },
        {
            id: 'e3',
            firstName: 'James',
            lastName: 'Wilson',
            email: 'j.wilson@taskhive.com',
            department: 'Marketing',
            designation: 'Growth Lead',
            joinDate: '2025-02-01',
            status: 'ACTIVE',
            version: 1,
            createdAt: '2025-02-01',
            updatedAt: '2025-02-01',
            isDeleted: false
        }
    ],
    totalElements: 3,
    totalPages: 1,
    size: 10,
    number: 0
};
