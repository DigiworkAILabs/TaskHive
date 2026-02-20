import apiClient from '@/shared/services/api/apiClient';
import { ENDPOINTS } from '@/shared/services/api/endpoints';
import { LoginResponse, User } from '../types/auth.types';

export const authService = {
    login: async (credentials: any) => {
        const response = await apiClient.post<LoginResponse>(ENDPOINTS.AUTH.LOGIN, credentials);
        return response.data;
    },
    logout: async () => {
        return apiClient.post(ENDPOINTS.AUTH.LOGOUT);
    },
    me: async () => {
        const response = await apiClient.get<User>(ENDPOINTS.AUTH.ME);
        return response.data;
    },
    forgotPassword: async (email: string) => {
        return apiClient.post(ENDPOINTS.AUTH.FORGOT_PASSWORD, { email });
    },
    resetPassword: async (data: any) => {
        return apiClient.post(ENDPOINTS.AUTH.RESET_PASSWORD, data);
    },
    activateAccount: async (data: any) => {
        return apiClient.post(ENDPOINTS.AUTH.ACTIVATE_ACCOUNT, data);
    },
    changePassword: async (data: any) => {
        return apiClient.post(ENDPOINTS.AUTH.CHANGE_PASSWORD, data);
    }
};
