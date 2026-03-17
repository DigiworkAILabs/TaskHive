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
        return <div className="w-16 h-8" />;
    }

    return (
        <div className="flex items-center gap-2" title="Enable Machine Learning Insights">
            <Sparkles
                size={20}
                className={`transition-colors duration-300 ${isMlEnabled ? 'text-[#ff4b4b]' : 'text-zinc-400'}`}
            />
            <button
                type="button"
                role="switch"
                aria-checked={isMlEnabled}
                onClick={toggleMlEnabled}
                className={`
                    relative inline-flex h-8 w-16 flex-shrink-0 cursor-pointer 
                    rounded-full transition-colors duration-300 ease-in-out 
                    focus:outline-none shadow-[inset_0_2px_4px_rgba(0,0,0,0.12)] items-center
                    ${isMlEnabled ? 'bg-[#ff4b4b]' : 'bg-zinc-200'}
                `}
            >
                <span className="sr-only">Use ML Insights</span>
                <span
                    aria-hidden="true"
                    className={`
                        pointer-events-none inline-block h-6 w-6 transform rounded-full 
                        bg-white shadow-[0_3px_8px_rgba(0,0,0,0.2)] ring-0 transition duration-300 ease-in-out
                        ${isMlEnabled ? 'translate-x-9' : 'translate-x-1'}
                    `}
                />
            </button>
        </div>
    );
};