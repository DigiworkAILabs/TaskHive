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
    EMPLOYEES: {
        BASE: '/employees',
        DETAIL: (id: string) => `/employees/${id}`,
        ACTIVATE: (id: string) => `/employees/${id}/activate`,
        DEACTIVATE: (id: string) => `/employees/${id}/deactivate`,
        UPLOAD_PHOTO: (id: string) => `/employees/${id}/photo`,
        GET_PHOTO: (id: string) => `/employees/${id}/photo`,
        SEARCH: '/employees/search',
    },
};
