import { useState } from 'react';
import { authService } from '../services/authService';
import { useAuthStore } from '../store/authStore';
import { useRouter } from 'next/navigation';

export const useLogin = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const setUser = useAuthStore((state) => state.setUser);
    const router = useRouter();

    const login = async (credentials: any) => {
        setIsLoading(true);
        setError(null);
        try {
            const response = await authService.login(credentials);
            setUser(response.user);
            router.push(response.user.role === 'ADMIN' ? '/admin/dashboard' : '/employee/dashboard');
        } catch (err: any) {
            setError(err.response?.data?.message || 'Login failed');
        } finally {
            setIsLoading(false);
        }
    };

    return { login, isLoading, error };
};
