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

    // Determine color based on confidence bounds tightness (optional UX enhancement)
    const rangeSize = prediction.confidenceRange.high - prediction.confidenceRange.low;
    const isTightEstimation = rangeSize <= 2.0;

    return (
        <div className="mt-2 text-sm rounded-md bg-purple-50 p-3 border border-purple-100 flex items-start group relative">
            <Zap className={`w-4 h-4 mr-2 mt-0.5 flex-shrink-0 ${prediction.fallbackUsed ? 'text-gray-400' : 'text-purple-600'}`} />
            <div className="flex-1">
                <div className="font-medium text-purple-900 flex items-center justify-between">
                    <span>
                        AI Suggests:{' '}
                        {prediction.fallbackUsed ? (
                            <span className="text-gray-600 italic">Manual Mode</span>
                        ) : (
                            <span className={isTightEstimation ? 'text-green-700' : 'text-purple-700'}>
                                ~{prediction.confidenceRange.low.toFixed(1)} – {prediction.confidenceRange.high.toFixed(1)} hours
                            </span>
                        )}
                    </span>

                    {/* Tooltip for reasoning */}
                    {!prediction.fallbackUsed && (
                        <div className="relative flex items-center">
                            <HelpCircle className="w-4 h-4 text-purple-400 hover:text-purple-600 cursor-help transition-colors" />
                            <div className="absolute right-0 bottom-full mb-2 hidden group-hover:block w-64 bg-gray-900 text-white text-xs rounded shadow-lg p-2 z-10 transition-opacity whitespace-normal leading-relaxed before:content-[''] before:absolute before:border-4 before:border-transparent before:border-t-gray-900 before:top-full before:right-1.5">
                                {prediction.reasoning}
                            </div>
                        </div>
                    )}
                </div>
                {prediction.fallbackUsed && (
                    <p className="text-gray-500 mt-1 text-xs">
                        {prediction.reasoning}
                    </p>
                )}
            </div>
        </div>
    );
};
