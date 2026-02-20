import { create } from 'zustand';
import { User } from '../types/auth.types';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  setUser: (user: User) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  setUser: (user) => set({ user, isAuthenticated: true }),
  clearAuth: () => set({ user: null, isAuthenticated: false }),
}));

export const useIsAdmin = () => useAuthStore((s) => s.user?.role === 'ADMIN');
export const useIsEmployee = () => useAuthStore((s) => s.user?.role === 'EMPLOYEE');
export const useCurrentUser = () => useAuthStore((s) => s.user);
