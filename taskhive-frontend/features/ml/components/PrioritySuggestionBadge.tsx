'use client';

/**
 * features/ml/components/PrioritySuggestionBadge.tsx
 * ────────────────────────────────────────────────────
 * UI component that renders the ML priority suggestion inline with
 * the Priority dropdown in the Create Task form.
 *
 * States:
 *   loading   → spinning indicator
 *   suggestion → badge with label, confidence bar, [Accept] [Ignore] buttons
 *   ignored   → nothing (hidden)
 *   null      → nothing (not yet fetched)
 */

import React, { useState } from 'react';
import { Sparkles, Loader2, X } from 'lucide-react';
import type { TaskPriorityPrediction } from '../types/ml.types';
import type { TaskPriority } from '../types/ml.types';

interface PrioritySuggestionBadgeProps {
    /** The latest ML prediction, or null if unavailable */
    prediction: TaskPriorityPrediction | null;
    /** True while the ML request is in flight */
    isLoading: boolean;
    /** Called when admin clicks "Accept" — parent should update priority field */
    onAccept: (priority: TaskPriority) => void;
    /** The AI variant used to generate this suggestion (determines theming) */
    variant?: 'local' | 'gemini';
}

const PRIORITY_COLORS: Record<TaskPriority, { bg: string; text: string; border: string }> = {
    LOW: { bg: 'rgba(34,197,94,0.08)', text: '#22c55e', border: 'rgba(34,197,94,0.25)' },
    MEDIUM: { bg: 'rgba(245,158,11,0.08)', text: '#f59e0b', border: 'rgba(245,158,11,0.25)' },
    HIGH: { bg: 'rgba(249,115,22,0.08)', text: '#f97316', border: 'rgba(249,115,22,0.25)' },
    CRITICAL: { bg: 'rgba(239,68,68,0.08)', text: '#ef4444', border: 'rgba(239,68,68,0.25)' },
};

export const PrioritySuggestionBadge: React.FC<PrioritySuggestionBadgeProps> = ({
    prediction,
    isLoading,
    onAccept,
    variant = 'local',
}) => {
    const [dismissed, setDismissed] = useState(false);

    // Reset dismissed state when a new prediction arrives
    React.useEffect(() => {
        if (prediction) setDismissed(false);
    }, [prediction]);

    // ── Loading state ────────────────────────────────────────────────────────
    if (isLoading) {
        const loadingBg = variant === 'gemini' ? 'rgba(168,85,247,0.05)' : 'rgba(249,115,22,0.05)';
        const loadingBorder = variant === 'gemini' ? 'rgba(168,85,247,0.15)' : 'rgba(249,115,22,0.15)';
        const loadingText = variant === 'gemini' ? '#a855f7' : '#f97316';
        
        return (
            <div style={{
                display: 'flex', alignItems: 'center', gap: '6px',
                padding: '6px 10px', marginTop: '6px',
                backgroundColor: loadingBg,
                border: `1px solid ${loadingBorder}`,
                borderRadius: '8px', fontSize: '12px', color: '#a1a1aa',
            }}>
                <Loader2 size={12} style={{ animation: 'spin 1s linear infinite', color: loadingText }} />
                Analysing with AI…
            </div>
        );
    }

    // ── No prediction / dismissed ────────────────────────────────────────────
    if (!prediction || dismissed) return null;

    const priority = prediction.predictedPriority;
    const baseColors = PRIORITY_COLORS[priority];
    
    // Override colors if Gemini variant to match branding
    const colors = variant === 'gemini' 
        ? { bg: 'rgba(168,85,247,0.08)', text: '#a855f7', border: 'rgba(168,85,247,0.25)' }
        : baseColors;

    const pct = Math.round(prediction.confidence * 100);

    // ── Suggestion badge ─────────────────────────────────────────────────────
    return (
        <div style={{
            marginTop: '8px',
            padding: '10px 12px',
            backgroundColor: colors.bg,
            border: `1px solid ${colors.border}`,
            borderRadius: '10px',
            fontSize: '12px',
        }}>
            {/* Header row */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '6px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '5px', color: colors.text, fontWeight: 600 }}>
                    <Sparkles size={12} />
                    AI suggests: <strong style={{ marginLeft: '2px' }}>{priority}</strong>
                </div>
                <button
                    type="button"
                    onClick={() => setDismissed(true)}
                    title="Dismiss suggestion"
                    style={{
                        background: 'none', border: 'none', cursor: 'pointer',
                        color: '#52525b', padding: '2px', lineHeight: 0,
                    }}
                >
                    <X size={12} />
                </button>
            </div>

            {/* Confidence bar */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                <div style={{
                    flex: 1, height: '4px', backgroundColor: 'rgba(255,255,255,0.06)',
                    borderRadius: '2px', overflow: 'hidden',
                }}>
                    <div style={{
                        width: `${pct}%`, height: '100%',
                        backgroundColor: colors.text,
                        borderRadius: '2px',
                        transition: 'width 0.6s ease',
                    }} />
                </div>
                <span style={{ color: '#a1a1aa', whiteSpace: 'nowrap' }}>{pct}% confidence</span>
            </div>

            {/* Reasoning */}
            {prediction.reasoning && (
                <p style={{ color: '#71717a', margin: '0 0 8px 0', lineHeight: 1.4, fontSize: '11px' }}>
                    {prediction.reasoning}
                </p>
            )}

            {/* Action buttons */}
            <div style={{ display: 'flex', gap: '6px' }}>
                <button
                    type="button"
                    id="ml-priority-accept-btn"
                    onClick={() => { onAccept(priority); setDismissed(true); }}
                    style={{
                        padding: '5px 12px', borderRadius: '6px', border: 'none',
                        backgroundColor: colors.text, color: '#ffffff',
                        fontSize: '11px', fontWeight: 600, cursor: 'pointer',
                        transition: 'opacity 0.15s',
                    }}
                    onMouseEnter={(e) => (e.currentTarget.style.opacity = '0.85')}
                    onMouseLeave={(e) => (e.currentTarget.style.opacity = '1')}
                >
                    ✓ Accept
                </button>
                <button
                    type="button"
                    id="ml-priority-ignore-btn"
                    onClick={() => setDismissed(true)}
                    style={{
                        padding: '5px 12px', borderRadius: '6px',
                        border: `1px solid ${colors.border}`,
                        backgroundColor: 'transparent', color: '#71717a',
                        fontSize: '11px', cursor: 'pointer', transition: 'opacity 0.15s',
                    }}
                    onMouseEnter={(e) => (e.currentTarget.style.opacity = '0.7')}
                    onMouseLeave={(e) => (e.currentTarget.style.opacity = '1')}
                >
                    Ignore
                </button>

                {prediction.fallbackUsed && (
                    <span style={{ color: '#3f3f46', fontSize: '10px', alignSelf: 'center', marginLeft: '4px' }}>
                        (default)
                    </span>
                )}
            </div>
        </div>
    );
};
