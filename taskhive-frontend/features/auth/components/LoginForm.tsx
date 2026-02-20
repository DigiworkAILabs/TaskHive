'use client';

import React, { useState } from 'react';
import { useLogin } from '../hooks/useLogin';
import Link from 'next/link';
import { Eye, EyeOff, Lock, ArrowRight, AlertCircle } from 'lucide-react';

// ── TaskHive logo icon ──────────────────────────────────────────────────────
function TaskHiveIcon() {
    return (
        <div
            className="flex h-14 w-14 items-center justify-center rounded-2xl"
            style={{
                background: 'linear-gradient(135deg, #f97316 0%, #ea6c10 100%)',
                boxShadow: '0 8px 24px rgba(249,115,22,0.35)',
            }}
        >
            {/* document-check pictogram */}
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path
                    d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"
                    fill="rgba(255,255,255,0.25)"
                    stroke="white"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                />
                <polyline
                    points="14 2 14 8 20 8"
                    stroke="white"
                    strokeWidth="1.5"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                />
                <polyline
                    points="9 15 11 17 15 13"
                    stroke="white"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                />
            </svg>
        </div>
    );
}

// ── Shared input style ──────────────────────────────────────────────────────
const inputBase: React.CSSProperties = {
    width: '100%',
    backgroundColor: '#111111',
    border: '1px solid #2a2a2a',
    borderRadius: '10px',
    color: '#ffffff',
    fontSize: '14px',
    padding: '12px 16px',
    outline: 'none',
    transition: 'border-color 0.2s, box-shadow 0.2s',
};

interface AuthInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    label: string;
    right?: React.ReactNode;
}

function AuthInput({ label, right, id, ...rest }: AuthInputProps) {
    const [focused, setFocused] = useState(false);
    return (
        <div>
            <label
                htmlFor={id}
                style={{ display: 'block', fontSize: '13px', color: '#a1a1aa', marginBottom: '6px', fontWeight: 500 }}
            >
                {label}
            </label>
            <div style={{ position: 'relative' }}>
                <input
                    id={id}
                    {...rest}
                    style={{
                        ...inputBase,
                        paddingRight: right ? '44px' : '16px',
                        borderColor: focused ? '#f97316' : '#2a2a2a',
                        boxShadow: focused ? '0 0 0 3px rgba(249,115,22,0.12)' : 'none',
                    }}
                    onFocus={(e) => { setFocused(true); rest.onFocus?.(e); }}
                    onBlur={(e) => { setFocused(false); rest.onBlur?.(e); }}
                />
                {right && (
                    <div style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)' }}>
                        {right}
                    </div>
                )}
            </div>
        </div>
    );
}

// ── Main component ──────────────────────────────────────────────────────────
export const LoginForm = () => {
    const { login, isLoading, error } = useLogin();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        login({ email, password });
    };

    const card: React.CSSProperties = {
        backgroundColor: '#161616',
        border: '1px solid #1f1f1f',
        borderRadius: '20px',
        padding: '40px 36px',
        boxShadow: '0 32px 64px rgba(0,0,0,0.5)',
    };

    return (
        <div style={card}>
            {/* Header */}
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '32px', gap: '14px' }}>
                <TaskHiveIcon />
                <div style={{ textAlign: 'center' }}>
                    <h1 style={{ fontSize: '22px', fontWeight: 700, color: '#ffffff', margin: 0 }}>TaskHive</h1>
                    <p style={{ fontSize: '13px', color: '#71717a', marginTop: '6px' }}>
                        Enter your credentials to access the workspace.
                    </p>
                </div>
            </div>

            {/* Error */}
            {error && (
                <div
                    style={{
                        display: 'flex', alignItems: 'center', gap: '8px',
                        backgroundColor: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.25)',
                        borderRadius: '10px', padding: '10px 14px', marginBottom: '20px', color: '#f87171', fontSize: '13px',
                    }}
                >
                    <AlertCircle size={15} style={{ flexShrink: 0 }} />
                    {error}
                </div>
            )}

            {/* Form */}
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
                <AuthInput
                    id="email"
                    label="Email Address"
                    type="email"
                    placeholder="name@company.com"
                    value={email}
                    autoComplete="email"
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />

                <AuthInput
                    id="password"
                    label="Password"
                    type={showPassword ? 'text' : 'password'}
                    placeholder="Enter password"
                    value={password}
                    autoComplete="current-password"
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    right={
                        <button
                            type="button"
                            onClick={() => setShowPassword(!showPassword)}
                            style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#52525b', padding: 0 }}
                        >
                            {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                        </button>
                    }
                />

                {/* Sign In button */}
                <button
                    type="submit"
                    disabled={isLoading}
                    style={{
                        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px',
                        width: '100%', padding: '13px 24px', borderRadius: '10px', border: 'none',
                        background: isLoading ? '#7c3e10' : 'linear-gradient(135deg, #f97316 0%, #ea6c10 100%)',
                        color: '#ffffff', fontSize: '15px', fontWeight: 600,
                        cursor: isLoading ? 'not-allowed' : 'pointer',
                        transition: 'opacity 0.2s, transform 0.1s',
                        boxShadow: '0 4px 20px rgba(249,115,22,0.3)',
                        marginTop: '4px',
                    }}
                    onMouseEnter={(e) => !isLoading && ((e.currentTarget as HTMLButtonElement).style.opacity = '0.88')}
                    onMouseLeave={(e) => ((e.currentTarget as HTMLButtonElement).style.opacity = '1')}
                >
                    {isLoading ? 'Signing In…' : 'Sign In'}
                    {!isLoading && <ArrowRight size={16} />}
                </button>
            </form>

            {/* Forgot password */}
            <div style={{ textAlign: 'center', marginTop: '20px' }}>
                <Link
                    href="/forgot-password"
                    style={{ fontSize: '13px', color: '#71717a', textDecoration: 'none' }}
                    onMouseEnter={(e) => ((e.currentTarget as HTMLAnchorElement).style.color = '#f97316')}
                    onMouseLeave={(e) => ((e.currentTarget as HTMLAnchorElement).style.color = '#71717a')}
                >
                    Forgot Password?
                </Link>
            </div>

            {/* Security badge */}
            <div style={{ display: 'flex', justifyContent: 'center', marginTop: '24px' }}>
                <div
                    style={{
                        display: 'inline-flex', alignItems: 'center', gap: '6px',
                        backgroundColor: '#111111', border: '1px solid #1f1f1f',
                        borderRadius: '999px', padding: '6px 14px', fontSize: '11px', color: '#52525b',
                    }}
                >
                    <Lock size={11} color="#22c55e" />
                    Secure 256-bit Encrypted Connection
                </div>
            </div>
        </div>
    );
};
