import React from 'react';

export default function AuthLayout({ children }: { children: React.ReactNode }) {
    return (
        <div
            style={{ backgroundColor: '#0a0a0a', minHeight: '100vh' }}
            className="flex min-h-screen flex-col items-center justify-center px-4 py-12 relative overflow-hidden"
        >
            {/* Subtle ambient glow */}
            <div
                className="absolute inset-0 pointer-events-none"
                style={{
                    background:
                        'radial-gradient(ellipse 60% 50% at 50% -10%, rgba(249,115,22,0.08) 0%, transparent 70%)',
                }}
            />

            {/* Card */}
            <div className="relative z-10 w-full max-w-[420px]">
                {children}
            </div>

            {/* Footer */}
            <p className="relative z-10 mt-8 text-center text-xs text-zinc-600">
                &copy; 2026 TaskHive by Digiwork. All rights reserved.
            </p>
        </div>
    );
}
