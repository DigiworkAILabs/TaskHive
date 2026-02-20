import { User } from '../types/auth.types';

export const isAdmin = (user: User | null): boolean => {
    return user?.role === 'ADMIN';
};

export const isEmployee = (user: User | null): boolean => {
    return user?.role === 'EMPLOYEE';
};

export const isLoggedIn = (user: User | null): boolean => {
    return !!user;
};
