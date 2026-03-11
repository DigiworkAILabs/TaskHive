import React from 'react';
import { Loader2, Zap, AlertTriangle, HelpCircle } from 'lucide-react';
import type { CompletionTimePrediction } from '../types/ml.types';

interface CompletionTimeEstimateProps {
    prediction: CompletionTimePrediction | null;
    isLoading: boolean;
    error: string | null;
}

export const CompletionTimeEstimate: React.FC<CompletionTimeEstimateProps> = ({
    prediction,
    isLoading,
    error,
}) => {
    if (isLoading) {
        return (
            <div className="flex items-center text-sm text-gray-500 mt-2">
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                <span>AI estimating completion time...</span>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex items-center text-sm text-red-500 mt-2">
                <AlertTriangle className="w-4 h-4 mr-2" />
                <span>AI suggestion failed. Enter estimate manually.</span>
            </div>
        );
    }

    if (!prediction) {
        return null;
    }

    // Determine color based on confidence bounds tightness
    const low = prediction.confidenceRange?.low ?? prediction.estimatedHours ?? 0;
    const high = prediction.confidenceRange?.high ?? prediction.estimatedHours ?? 0;
    const rangeSize = high - low;
    const isTightEstimation = rangeSize > 0 && rangeSize <= 2.0;

    return (
        <div style={{
            marginTop: '12px',
            fontSize: '13px',
            borderRadius: '10px',
            backgroundColor: 'rgba(168, 85, 247, 0.12)',
            padding: '12px 16px',
            border: '1px solid rgba(168, 85, 247, 0.3)',
            display: 'flex',
            alignItems: 'flex-start',
            gap: '12px',
            boxShadow: '0 4px 12px rgba(168, 85, 247, 0.1)'
        }}>
            <Zap
                size={16}
                style={{
                    marginTop: '2px',
                    flexShrink: 0,
                    color: prediction.fallbackUsed ? '#a1a1aa' : '#c084fc'
                }}
            />
            <div style={{ flex: 1 }}>
                <div style={{ fontWeight: 600, color: '#f3e8ff', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <span>
                        AI Suggests:{' '}
                        {prediction.fallbackUsed ? (
                            <span style={{ color: '#a1a1aa', fontStyle: 'italic', fontWeight: 400 }}>Manual Mode</span>
                        ) : (
                            <span style={{ color: isTightEstimation ? '#4ade80' : '#d8b4fe' }}>
                                ~{low.toFixed(1)} – {high.toFixed(1)} hours
                            </span>
                        )}
                    </span>

                    {/* Reasoning Tooltip Icon */}
                    {!prediction.fallbackUsed && (
                        <div title={prediction.reasoning} style={{ cursor: 'help' }}>
                            <HelpCircle size={15} style={{ color: '#c084fc', opacity: 0.8 }} />
                        </div>
                    )}
                </div>
                {prediction.fallbackUsed && (
                    <p style={{ color: '#a1a1aa', marginTop: '4px', fontSize: '12px', lineHeight: 1.4 }}>
                        {prediction.reasoning}
                    </p>
                )}
            </div>
        </div>
    );
};
