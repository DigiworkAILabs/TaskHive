import { useState } from 'react';
import { authService } from '../services/authService';
import { useRouter } from 'next/navigation';

export const useResetPassword = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const router = useRouter();

    const resetPassword = async (data: any) => {
        setIsLoading(true);
        setError(null);
        try {
            await authService.resetPassword(data);
            router.push('/login');
        } catch (err: any) {
            setError(err.response?.data?.message || 'Password reset failed');
        } finally {
            setIsLoading(false);
        }
    };

    return { resetPassword, isLoading, error };
};
