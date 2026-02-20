import { useState } from 'react';
import { authService } from '../services/authService';
import { useRouter } from 'next/navigation';

export const useActivateAccount = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const router = useRouter();

    const activateAccount = async (data: any) => {
        setIsLoading(true);
        setError(null);
        try {
            await authService.activateAccount(data);
            router.push('/login');
        } catch (err: any) {
            setError(err.response?.data?.message || 'Account activation failed');
        } finally {
            setIsLoading(false);
        }
    };

    return { activateAccount, isLoading, error };
};
