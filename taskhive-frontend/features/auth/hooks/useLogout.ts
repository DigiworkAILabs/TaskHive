import { useRouter } from 'next/navigation';
import { authService } from '../services/authService';
import { useAuthStore } from '../store/authStore';

export const useLogout = () => {
    const clearAuth = useAuthStore((state) => state.clearAuth);
    const router = useRouter();

    const logout = async () => {
        try {
            await authService.logout();
        } catch (error) {
            console.error('Logout failed', error);
        } finally {
            clearAuth();
            router.push('/login');
        }
    };

    return { logout };
};
