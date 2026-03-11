import React from 'react';
import { 
    TrendingUp, 
    TrendingDown, 
    Minus, 
    AlertCircle,
    CheckCircle2,
    Clock,
    MessageSquare,
    ChevronRight
} from 'lucide-react';
import { ProductivityScore } from '../types/ml.types';

interface ProductivityScoreCardProps {
    scoreData: ProductivityScore | null;
    isLoading: boolean;
    error: string | null;
}

export const ProductivityScoreCard: React.FC<ProductivityScoreCardProps> = ({
    scoreData,
    isLoading,
    error
}) => {
    if (isLoading) {
        return (
            <div className="bg-white dark:bg-slate-900 rounded-xl p-6 shadow-sm border border-slate-200 dark:border-slate-800 animate-pulse">
                <div className="h-4 w-32 bg-slate-200 dark:bg-slate-700 rounded mb-4" />
                <div className="flex items-center gap-4">
                    <div className="h-16 w-16 bg-slate-200 dark:bg-slate-700 rounded-full" />
                    <div className="space-y-2 flex-1">
                        <div className="h-4 bg-slate-200 dark:bg-slate-700 rounded w-3/4" />
                        <div className="h-3 bg-slate-200 dark:bg-slate-700 rounded w-1/2" />
                    </div>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="bg-red-50 dark:bg-red-900/10 rounded-xl p-4 border border-red-100 dark:border-red-900/20 text-red-600 dark:text-red-400 flex items-center gap-3">
                <AlertCircle size={20} />
                <p className="text-sm font-medium">{error}</p>
            </div>
        );
    }

    if (!scoreData) return null;

    const { score, grade, trend, reasoning, breakdown, fallbackUsed } = scoreData;

    const getGradeColor = (g: string) => {
        switch (g) {
            case 'A': return 'text-emerald-500 bg-emerald-500/10 border-emerald-500/20';
            case 'B': return 'text-blue-500 bg-blue-500/10 border-blue-500/20';
            case 'C': return 'text-amber-500 bg-amber-500/10 border-amber-500/20';
            case 'D': return 'text-orange-500 bg-orange-500/10 border-orange-500/20';
            default: return 'text-red-500 bg-red-500/10 border-red-500/20';
        }
    };

    const getTrendIcon = (t: string) => {
        switch (t) {
            case 'improving': return <TrendingUp size={16} className="text-emerald-500" />;
            case 'declining': return <TrendingDown size={16} className="text-red-500" />;
            default: return <Minus size={16} className="text-slate-400" />;
        }
    };

    return (
        <div className="bg-white dark:bg-slate-900 rounded-xl p-6 shadow-sm border border-slate-200 dark:border-slate-800 transition-all hover:shadow-md">
            <div className="flex justify-between items-start mb-6">
                <div>
                    <h3 className="text-sm font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider mb-1">
                        Productivity Score
                    </h3>
                    <div className="flex items-center gap-2">
                        <span className="text-3xl font-bold text-slate-900 dark:text-white">
                            {score.toFixed(1)}
                        </span>
                        <div className={`px-2 py-0.5 rounded border text-sm font-bold ${getGradeColor(grade)}`}>
                            Grade {grade}
                        </div>
                    </div>
                </div>
                <div className="flex items-center gap-2 bg-slate-50 dark:bg-slate-800/50 px-3 py-1.5 rounded-lg border border-slate-100 dark:border-slate-800">
                    <span className="text-xs font-medium text-slate-500 dark:text-slate-400 capitalize">
                        {trend}
                    </span>
                    {getTrendIcon(trend)}
                </div>
            </div>

            {/* Breakdown Grid */}
            <div className="grid grid-cols-2 gap-4 mb-6">
                <BreakdownItem 
                    label="Task Completion" 
                    value={breakdown.completion_rate_score} 
                    max={35} 
                    icon={<CheckCircle2 size={14} />}
                    color="emerald"
                />
                <BreakdownItem 
                    label="On-Time Rate" 
                    value={breakdown.on_time_score} 
                    max={30} 
                    icon={<Clock size={14} />}
                    color="blue"
                />
                <BreakdownItem 
                    label="Overdue Impact" 
                    value={breakdown.overdue_penalty} 
                    max={0} 
                    icon={<AlertCircle size={14} />}
                    color="red"
                />
                <BreakdownItem 
                    label="Engagement" 
                    value={breakdown.engagement_score} 
                    max={15} 
                    icon={<MessageSquare size={14} />}
                    color="indigo"
                />
            </div>

            <div className="p-4 bg-slate-50 dark:bg-slate-800/50 rounded-lg border border-dashed border-slate-200 dark:border-slate-700">
                <div className="flex gap-3">
                    <div className="mt-1">
                        <div className="w-1.5 h-1.5 rounded-full bg-blue-500" />
                    </div>
                    <div>
                        <p className="text-sm text-slate-600 dark:text-slate-300 leading-relaxed">
                            {reasoning}
                        </p>
                        {fallbackUsed && (
                          <p className="text-[10px] text-slate-400 mt-2 italic">
                            * Basic calculation used as ML model is initializing
                          </p>
                        )}
                    </div>
                </div>
            </div>
            
            <button className="w-full mt-6 py-2 flex items-center justify-center gap-2 text-sm font-medium text-blue-600 dark:text-blue-400 hover:text-blue-700 dark:hover:text-blue-300 transition-colors group">
                View detailed analytics
                <ChevronRight size={16} className="transition-transform group-hover:translate-x-0.5" />
            </button>
        </div>
    );
};

const BreakdownItem = ({ label, value, max, icon, color }: { label: string, value: number, max: number, icon: React.ReactNode, color: string }) => {
    // For penalty, we show it differently
    const isPenalty = max <= 0;
    const percentage = isPenalty ? Math.min(Math.abs(value) * 5, 100) : (value / max) * 100;
    
    return (
        <div className="space-y-1.5">
            <div className="flex justify-between items-center text-[11px] font-medium uppercase tracking-tight text-slate-500 dark:text-slate-400">
                <div className="flex items-center gap-1.5">
                    <span className={`text-${color}-500`}>{icon}</span>
                    {label}
                </div>
                <span>{value > 0 ? `+${value}` : value}</span>
            </div>
            <div className="h-1.5 w-full bg-slate-100 dark:bg-slate-800 rounded-full overflow-hidden">
                <div 
                    className={`h-full rounded-full transition-all duration-500 bg-${color}-500/80`}
                    style={{ width: `${percentage}%` }}
                />
            </div>
        </div>
    );
};
