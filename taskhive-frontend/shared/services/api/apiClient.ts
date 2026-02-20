import axios from 'axios';
import { useAuthStore } from '@/features/auth/store/authStore';

const apiClient = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_URL,
    headers: { 'Content-Type': 'application/json' },
    withCredentials: true,
});

let isRefreshing = false;
let failedQueue: Array<{ resolve: Function; reject: Function }> = [];

const processQueue = (error: unknown) => {
    failedQueue.forEach((p) => (error ? p.reject(error) : p.resolve()));
    failedQueue = [];
};

apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const original = error.config;

        if (error.response?.status === 401 && !original._retry) {
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                }).then(() => apiClient(original)).catch((e) => Promise.reject(e));
            }

            original._retry = true;
            isRefreshing = true;

            try {
                await apiClient.post('/auth/refresh');
                processQueue(null);
                return apiClient(original);
            } catch (refreshError) {
                processQueue(refreshError);
                useAuthStore.getState().clearAuth();
                // window.location.href = '/login'; // Let middleware or component handle redirect
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
);

export default apiClient;
