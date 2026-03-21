'use client';

import { useEffect, useState } from 'react';
import { TodayOverview, AnomalyAlert } from '../../task/types/task.types';
import { fetchTodayOverview, fetchAnomalies } from '../services/analyticsService';

interface TodayOverviewState {
    overview: TodayOverview | null;
    anomalies: AnomalyAlert[];
    isLoading: boolean;
    error: string | null;
    refresh: () => void;
}

export const useTodayOverview = (): TodayOverviewState => {
    const [overview, setOverview] = useState<TodayOverview | null>(null);
    const [anomalies, setAnomalies] = useState<AnomalyAlert[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const load = async () => {
        setIsLoading(true);
        setError(null);
        try {
            const [ov, al] = await Promise.all([
                fetchTodayOverview(),
                fetchAnomalies(),
            ]);
            setOverview(ov);
            setAnomalies(al);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to load today\'s overview');
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        load();
        // Auto-refresh every 5 minutes
        const timer = setInterval(load, 5 * 60 * 1000);
        return () => clearInterval(timer);
    }, []);

    return { overview, anomalies, isLoading, error, refresh: load };
};
