import apiClient from '@/shared/services/api/apiClient';
import { ENDPOINTS } from '@/shared/services/api/endpoints';
import { AdminDashboardResponse } from '../types/analytics.types';

export const analyticsService = {
    getAdminDashboard: async () => {
        const response = await apiClient.get<AdminDashboardResponse>(ENDPOINTS.ANALYTICS.DASHBOARD);
        return response.data;
    }
};
