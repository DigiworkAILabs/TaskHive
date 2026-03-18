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
        return <div className="w-6 h-6" />;
    }

    return (
        <div className="flex items-center gap-2" title="Machine Learning Status">
            <Sparkles
                size={14}
                className={`transition-colors duration-300 ${isMlEnabled ? 'text-[#ff4b4b] animate-pulse' : 'text-zinc-600'}`}
            />
            <button
                type="button"
                role="radio"
                aria-checked={isMlEnabled}
                onClick={toggleMlEnabled}
                className={`
                    group relative flex h-6 w-6 items-center justify-center rounded-full 
                    transition-all duration-300 ease-in-out focus:outline-none
                    ${isMlEnabled 
                        ? 'bg-[#ff4b4b]/10 ring-1 ring-[#ff4b4b]/30 shadow-[0_0_12px_rgba(255,75,75,0.2)]' 
                        : 'bg-zinc-800/50 ring-1 ring-white/5 shadow-inner'
                    }
                `}
            >
                {/* Outer Ring / Track */}
                <div className={`
                    absolute inset-0 rounded-full border transition-all duration-300
                    ${isMlEnabled ? 'border-[#ff4b4b]/40 scale-100' : 'border-white/5 scale-90'}
                `} />

                {/* Inner Indicator (The Radio Dot) */}
                <div
                    className={`
                        h-2.5 w-2.5 rounded-full transition-all duration-500 ease-[cubic-bezier(0.34,1.56,0.64,1)]
                        ${isMlEnabled 
                            ? 'bg-[#ff4b4b] shadow-[0_0_8px_#ff4b4b] scale-100 opacity-100' 
                            : 'bg-zinc-700 scale-50 opacity-40'
                        }
                    `}
                />
            </button>
        </div>
    );
};