import { useState } from 'react';
import { authService } from '../services/authService';

export const useChangePassword = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);

    const changePassword = async (data: any) => {
        setIsLoading(true);
        setError(null);
        setSuccess(false);
        try {
            await authService.changePassword(data);
            setSuccess(true);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to change password');
        } finally {
            setIsLoading(false);
        }
    };

    return { changePassword, isLoading, error, success };
};
