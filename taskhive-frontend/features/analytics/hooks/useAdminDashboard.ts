import { useState, useEffect, useCallback } from 'react';
import { analyticsService } from '../services/analyticsService';
import { AdminDashboardResponse } from '../types/analytics.types';
import { MOCK_DASHBOARD_DATA } from '@/shared/utils/mockData';

export function useAdminDashboard() {
    const [data, setData] = useState<AdminDashboardResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const fetchDashboardData = useCallback(async () => {
        setLoading(true);
        try {
            const result = await analyticsService.getAdminDashboard();
            setData(result);
            setError(null);
        } catch (err: any) {
            console.warn('Dashboard API failed, using mock data:', err.message);
            setData(MOCK_DASHBOARD_DATA);
            setError(null);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchDashboardData();
    }, [fetchDashboardData]);

    return { data, loading, error, refetch: fetchDashboardData };
}
