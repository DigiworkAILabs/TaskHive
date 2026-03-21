import React, { useEffect, useState } from 'react';
import { Sparkles } from 'lucide-react';
import { useMlStore } from '../store/mlStore';

export const MlFeatureToggle = () => {
    const [mounted, setMounted] = useState(false);
    const { isMlEnabled, toggleMlEnabled } = useMlStore();

    useEffect(() => {
        setMounted(true);
    }, []);

    if (!mounted) {
        return <div style={{ width: '70px', height: '24px' }} />;
    }

    return (
        <div
            style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
            title={isMlEnabled ? 'ML Service: ON' : 'ML Service: OFF'}
        >
            {/* Sparkles icon — matches the SVG beside the toggle in TaskForm */}
            <Sparkles
                size={14}
                style={{
                    color: isMlEnabled ? '#f97316' : '#52525b',
                    transition: 'color 0.2s',
                    flexShrink: 0,
                }}
            />

            {/* Pill toggle — identical dimensions & style to TaskForm Task Requirements toggle */}
            <div
                onClick={toggleMlEnabled}
                role="switch"
                aria-checked={isMlEnabled}
                aria-label="Toggle ML service"
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
    );
};