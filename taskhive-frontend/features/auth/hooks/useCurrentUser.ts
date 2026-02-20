import { useAuthStore } from '../store/authStore';

export const useCurrentUser = () => {
    return useAuthStore((state) => state.user);
};
