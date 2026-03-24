'use client';

import React, { useState, useEffect, useMemo } from 'react';
import { TaskStatus } from '../types/task.types';

interface LcdCountdownTimerProps {
    dueDate: string;
    status: TaskStatus;
    noGlow?: boolean;
}

// ── Helpers ──────────────────────────────────────────────────────────────────

function computeRemaining(dueDate: string) {
    const now = Date.now();
    const due = new Date(dueDate).getTime();
    const diff = due - now;

    if (diff <= 0) return { days: 0, hours: 0, minutes: 0, seconds: 0, overdue: true, totalMs: diff };

    const totalSec = Math.floor(diff / 1000);
    const days    = Math.floor(totalSec / 86400);
    const hours   = Math.floor((totalSec % 86400) / 3600);
    const minutes = Math.floor((totalSec % 3600) / 60);
    const seconds = totalSec % 60;
    return { days, hours, minutes, seconds, overdue: false, totalMs: diff };
}

const pad = (n: number) => String(n).padStart(2, '0');

// ── Component ────────────────────────────────────────────────────────────────

export const LcdCountdownTimer: React.FC<LcdCountdownTimerProps> = ({ dueDate, status, noGlow = false }) => {
    const [remaining, setRemaining] = useState(() => computeRemaining(dueDate));

    useEffect(() => {
        setRemaining(computeRemaining(dueDate));
        const id = setInterval(() => setRemaining(computeRemaining(dueDate)), 1000);
        return () => clearInterval(id);
    }, [dueDate]);

    // Don't render for completed / cancelled tasks
    if (['DONE', 'CANCELLED'].includes(status)) return null;

    const { days, hours, minutes, seconds, overdue, totalMs } = remaining;

    // Colour scheme logic
    // 1. Overdue: Red
    // 2. Critical (< 1h): Red-Orange
    // 3. Warning (< 24h): Orange
    // 4. Safe (> 24h): Gold
    
    const isCritical = !overdue && totalMs <= 1000 * 60 * 60; // < 1 hour
    const isWarning  = !overdue && totalMs <= 1000 * 60 * 60 * 24; // < 24 hours

    let digitColor = '#fbbf24'; // Gold (Safe)
    let glowColor  = 'rgba(251,191,36,0.25)';
    let dimColor   = 'rgba(251,191,36,0.08)';
    let labelColor = '#fcd34d';
    let borderStroke = noGlow ? 'rgba(251,191,36,0.45)' : 'rgba(251,191,36,0.15)';

    if (overdue) {
        digitColor = '#ff4d4d';
        glowColor  = 'rgba(255,77,77,0.35)';
        dimColor   = 'rgba(255,77,77,0.1)';
        labelColor = '#ff8080';
        borderStroke = noGlow ? 'rgba(255,77,77,0.6)' : 'rgba(255,77,77,0.25)';
    } else if (isCritical) {
        digitColor = '#ef4444';
        glowColor  = 'rgba(239,68,68,0.35)';
        dimColor   = 'rgba(239,68,68,0.1)';
        labelColor = '#f87171';
        borderStroke = noGlow ? 'rgba(239,68,68,0.6)' : 'rgba(239,68,68,0.25)';
    } else if (isWarning) {
        digitColor = '#f97316';
        glowColor  = 'rgba(249,115,22,0.3)';
        dimColor   = 'rgba(249,115,22,0.08)';
        labelColor = '#fb923c';
        borderStroke = noGlow ? 'rgba(249,115,22,0.55)' : 'rgba(249,115,22,0.2)';
    }

    return (
        <>
            {/* Google Fonts – Orbitron for LCD feel */}
            <style>{`
                @import url('https://fonts.googleapis.com/css2?family=Orbitron:wght@400;700;900&display=swap');

                @keyframes lcdPulse {
                    0%, 100% { opacity: 1; transform: scale(1); }
                    50% { opacity: 0.95; transform: scale(1.01); }
                }
                @keyframes colonBlink {
                    0%, 49% { opacity: 1; filter: drop-shadow(0 0 8px ${glowColor}); }
                    50%, 100% { opacity: 0.3; filter: none; }
                }
                @keyframes overdueFlash {
                    0%, 100% { background-color: rgba(12, 12, 12, 0.85); border-color: ${borderStroke}; }
                    50% { background-color: rgba(60, 0, 0, 0.9); border-color: #ff4d4d; ${!noGlow ? `box-shadow: 0 0 20px rgba(255,77,77,0.4);` : ''} }
                }
                @keyframes warningPulse {
                    0%, 100% { border-color: ${borderStroke}; }
                    50% { border-color: ${digitColor}; ${!noGlow ? `box-shadow: 0 0 12px ${glowColor};` : ''} }
                }
                @keyframes shine {
                    from { transform: translateX(-100%) skewX(-15deg); }
                    to { transform: translateX(200%) skewX(-15deg); }
                }
            `}</style>

            <div
                style={{
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '4px',
                    padding: '10px 20px 8px 16px',
                    borderRadius: '14px',
                    backgroundColor: 'rgba(12, 12, 12, 0.85)',
                    backdropFilter: 'blur(12px)',
                    WebkitBackdropFilter: 'blur(12px)',
                    border: `1.5px solid ${borderStroke}`,
                    boxShadow: noGlow 
                        ? 'inset 0 2px 10px rgba(0,0,0,0.8), 0 5px 15px rgba(0,0,0,0.4)'
                        : `
                            inset 0 2px 10px rgba(0,0,0,0.8),
                            0 0 15px ${glowColor},
                            0 5px 15px rgba(0,0,0,0.4)
                        `,
                    position: 'relative',
                    overflow: 'hidden',
                    animation: overdue 
                        ? 'overdueFlash 1s ease-in-out infinite' 
                        : (isCritical || isWarning) 
                            ? 'warningPulse 2s ease-in-out infinite' 
                            : undefined,
                }}
            >
                {/* Glossy Reflection Overlay */}
                <div style={{
                    position: 'absolute',
                    top: 0, left: 0, width: '40%', height: '100%',
                    background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.05), transparent)',
                    animation: 'shine 4s linear infinite',
                    pointerEvents: 'none',
                    zIndex: 4,
                }} />

                {/* Scanline overlay for CRT / LCD feel */}
                <div style={{
                    position: 'absolute', inset: 0, zIndex: 2, pointerEvents: 'none',
                    backgroundImage: 'repeating-linear-gradient(transparent, transparent 1.5px, rgba(0,0,0,0.2) 1.5px, rgba(0,0,0,0.2) 3px)',
                    backgroundSize: '100% 3px',
                    opacity: 0.4,
                }} />

                {/* Overdue label */}
                {overdue && (
                    <span style={{
                        fontFamily: "'Orbitron', monospace",
                        fontSize: '11px',
                        fontWeight: 900,
                        letterSpacing: '3px',
                        color: '#ff4d4d',
                        textTransform: 'uppercase',
                        marginRight: '12px',
                        textShadow: '0 0 10px rgba(255,77,77,0.8)',
                        zIndex: 3,
                    }}>
                        OVERDUE
                    </span>
                )}

                {/* Timer icon */}
                {!overdue && (
                    <svg
                        width="20" height="20" viewBox="0 0 24 24" fill="none"
                        stroke={digitColor} strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"
                        style={{ marginRight: '10px', zIndex: 3, filter: `drop-shadow(0 0 6px ${glowColor})` }}
                    >
                        <circle cx="12" cy="12" r="10" />
                        <polyline points="12 6 12 12 16 14" />
                    </svg>
                )}

                {/* Days segment (only if > 0) */}
                {days > 0 && (
                    <>
                        <DigitGroup value={String(days)} color={digitColor} glow={glowColor} dim={dimColor} label="DAYS" labelColor={labelColor} />
                        <ColonSeparator color={digitColor} glow={glowColor} />
                    </>
                )}

                {/* Hours */}
                <DigitGroup value={pad(days > 0 ? hours : hours)} color={digitColor} glow={glowColor} dim={dimColor} label="HOURS" labelColor={labelColor} />
                <ColonSeparator color={digitColor} glow={glowColor} />

                {/* Minutes */}
                <DigitGroup value={pad(minutes)} color={digitColor} glow={glowColor} dim={dimColor} label="MINS" labelColor={labelColor} />
                <ColonSeparator color={digitColor} glow={glowColor} />

                {/* Seconds */}
                <DigitGroup value={pad(seconds)} color={digitColor} glow={glowColor} dim={dimColor} label="SECS" labelColor={labelColor} />
            </div>
        </>
    );
};

// ── Sub-components ───────────────────────────────────────────────────────────

function DigitGroup({
    value, color, glow, dim, label, labelColor,
}: {
    value: string; color: string; glow: string; dim: string; label: string; labelColor: string;
}) {
    return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '4px', zIndex: 3 }}>
            <div style={{ display: 'flex', gap: '2px', position: 'relative' }}>
                {/* Dim "ghost" digits behind — LCD effect */}
                <span style={{
                    fontFamily: "'Orbitron', monospace",
                    fontSize: '28px',
                    fontWeight: 900,
                    color: dim,
                    letterSpacing: '2.5px',
                    lineHeight: 1,
                    position: 'absolute',
                    top: 0, left: 0,
                    userSelect: 'none',
                    pointerEvents: 'none',
                    opacity: 0.5,
                }}>
                    {value.replace(/./g, '8')}
                </span>
                {/* Actual digits */}
                <span style={{
                    fontFamily: "'Orbitron', monospace",
                    fontSize: '28px',
                    fontWeight: 900,
                    color: color,
                    letterSpacing: '2.5px',
                    lineHeight: 1,
                    textShadow: `
                        0 0 10px ${glow},
                        0 0 2px rgba(255,255,255,0.3)
                    `,
                    position: 'relative',
                }}>
                    {value}
                </span>
            </div>
            <span style={{
                fontFamily: "'Orbitron', monospace",
                fontSize: '9px',
                fontWeight: 800,
                color: labelColor,
                letterSpacing: '1.5px',
                opacity: 0.8,
                textTransform: 'uppercase',
                textShadow: `0 0 2px ${glow}`,
            }}>
                {label}
            </span>
        </div>
    );
}

function ColonSeparator({ color, glow }: { color: string; glow: string }) {
    return (
        <span style={{
            fontFamily: "'Orbitron', monospace",
            fontSize: '24px',
            fontWeight: 900,
            color: color,
            textShadow: `0 0 8px ${glow}`,
            lineHeight: 1,
            padding: '0 4px',
            zIndex: 3,
            animation: 'colonBlink 1s step-start infinite',
            alignSelf: 'center',
            marginBottom: '10px',
        }}>
            :
        </span>
    );
}
