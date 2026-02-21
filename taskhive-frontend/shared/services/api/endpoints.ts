export const ENDPOINTS = {
    AUTH: {
        LOGIN: '/auth/login',
        LOGOUT: '/auth/logout',
        REFRESH: '/auth/refresh',
        ME: '/auth/me',
        FORGOT_PASSWORD: '/auth/forgot-password',
        RESET_PASSWORD: '/auth/reset-password',
        ACTIVATE_ACCOUNT: '/auth/activate-account',
        CHANGE_PASSWORD: '/auth/change-password',
    },
    EMPLOYEE: {
        LIST: '/employees',
        CREATE: '/employees',
        DETAIL: (id: string) => `/employees/${id}`,
        UPDATE: (id: string) => `/employees/${id}`,
        DELETE: (id: string) => `/employees/${id}`,
        ACTIVATE: (id: string) => `/employees/${id}/activate`,
        DEACTIVATE: (id: string) => `/employees/${id}/deactivate`,
        PHOTO: (id: string) => `/employees/${id}/photo`,
        SEARCH: '/employees/search',
    },
    ANALYTICS: {
        DASHBOARD: '/analytics/admin-dashboard',
        TASK_DISTRIBUTION: '/analytics/task-distribution',
        COMPLETION_TREND: '/analytics/task-completion-trend',
        PERFORMANCE: '/analytics/employee-performance',
    },
};
