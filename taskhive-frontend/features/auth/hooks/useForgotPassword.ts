import { useState } from 'react';
import { authService } from '../services/authService';

export const useForgotPassword = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);

    const forgotPassword = async (email: string) => {
        setIsLoading(true);
        setError(null);
        setSuccess(false);
        try {
            await authService.forgotPassword(email);
            setSuccess(true);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to send reset link');
        } finally {
            setIsLoading(false);
        }
    };

    return { forgotPassword, isLoading, error, success };
};
