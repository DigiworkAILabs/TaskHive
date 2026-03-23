import React, { useEffect, useState } from 'react';
import { Sparkles, Brain } from 'lucide-react';
import { useMlStore } from '../store/mlStore';

export const MlFeatureToggle = () => {
    const [mounted, setMounted] = useState(false);
    const { isMlEnabled, toggleMlEnabled, isGeminiEnabled, toggleGeminiEnabled } = useMlStore();

    useEffect(() => {
        setMounted(true);
    }, []);

    if (!mounted) {
        return <div style={{ width: '150px', height: '24px' }} />;
    }

    return (
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            {/* Local ML Toggle */}
            <div
                style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
                title={isMlEnabled ? 'Local ML Service: ON' : 'Local ML Service: OFF'}
            >
                <Brain
                    size={14}
                    style={{
                        color: isMlEnabled ? '#f97316' : '#52525b',
                        transition: 'color 0.2s',
                        flexShrink: 0,
                    }}
                />
                <div
                    onClick={toggleMlEnabled}
                    role="switch"
                    aria-checked={isMlEnabled}
                    aria-label="Toggle Local ML service"
                    style={{
                        width: '42px',
                        height: '24px',
                        borderRadius: '12px',
                        flexShrink: 0,
                        backgroundColor: isMlEnabled ? '#f97316' : '#3f3f46',
                        position: 'relative',
                        cursor: 'pointer',
                        transition: 'background-color 0.2s',
                    }}
                >
                    <div
                        style={{
                            width: '18px',
                            height: '18px',
                            borderRadius: '50%',
                            backgroundColor: '#ffffff',
                            position: 'absolute',
                            top: '3px',
                            transition: 'left 0.2s',
                            left: isMlEnabled ? '21px' : '3px',
                            boxShadow: '0 1px 3px rgba(0,0,0,0.4)',
                        }}
                    />
                </div>
            </div>

            {/* Gemini AI Toggle */}
            <div
                style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
                title={isGeminiEnabled ? 'Gemini AI Suggest: ON' : 'Gemini AI Suggest: OFF'}
            >
                <Sparkles
                    size={14}
                    style={{
                        color: isGeminiEnabled ? '#a855f7' : '#52525b', // Purple for Gemini
                        transition: 'color 0.2s',
                        flexShrink: 0,
                    }}
                />
                <div
                    onClick={toggleGeminiEnabled}
                    role="switch"
                    aria-checked={isGeminiEnabled}
                    aria-label="Toggle Gemini AI service"
                    style={{
                        width: '42px',
                        height: '24px',
                        borderRadius: '12px',
                        flexShrink: 0,
                        backgroundColor: isGeminiEnabled ? '#a855f7' : '#3f3f46',
                        position: 'relative',
                        cursor: 'pointer',
                        transition: 'background-color 0.2s',
                    }}
                >
                    <div
                        style={{
                            width: '18px',
                            height: '18px',
                            borderRadius: '50%',
                            backgroundColor: '#ffffff',
                            position: 'absolute',
                            top: '3px',
                            transition: 'left 0.2s',
                            left: isGeminiEnabled ? '21px' : '3px',
                            boxShadow: '0 1px 3px rgba(0,0,0,0.4)',
                        }}
                    />
                </div>
            </div>
        </div>
    );
};