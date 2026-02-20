'use client';

import React, { useState } from 'react';
import { useForgotPassword } from '../hooks/useForgotPassword';
import Link from 'next/link';
import { ArrowLeft, Mail, CheckCircle2, AlertCircle } from 'lucide-react';

// ── Shared types ────────────────────────────────────────────────────────────
interface AuthInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    label: string;
}

function AuthInput({ label, id, ...rest }: AuthInputProps) {
    const [focused, setFocused] = useState(false);
    return (
        <div>
            <label
                htmlFor={id}
                style={{ display: 'block', fontSize: '13px', color: '#a1a1aa', marginBottom: '6px', fontWeight: 500 }}
            >
                {label}
            </label>
            <input
                id={id}
                {...rest}
                style={{
                    width: '100%',
                    backgroundColor: '#111111',
                    border: `1px solid ${focused ? '#f97316' : '#2a2a2a'}`,
                    borderRadius: '10px',
                    color: '#ffffff',
                    fontSize: '14px',
                    padding: '12px 16px',
                    outline: 'none',
                    transition: 'border-color 0.2s, box-shadow 0.2s',
                    boxShadow: focused ? '0 0 0 3px rgba(249,115,22,0.12)' : 'none',
                    boxSizing: 'border-box',
                }}
                onFocus={(e) => { setFocused(true); rest.onFocus?.(e); }}
                onBlur={(e) => { setFocused(false); rest.onBlur?.(e); }}
            />
        </div>
    );
}

export const ForgotPasswordForm = () => {
    const { forgotPassword, isLoading, error, success } = useForgotPassword();
    const [email, setEmail] = useState('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        forgotPassword(email);
    };

    const card: React.CSSProperties = {
        backgroundColor: '#161616',
        border: '1px solid #1f1f1f',
        borderRadius: '20px',
        padding: '40px 36px',
        boxShadow: '0 32px 64px rgba(0,0,0,0.5)',
    };

    if (success) {
        return (
            <div style={card}>
                <div style={{ textAlign: 'center', padding: '16px 0' }}>
                    <div
                        style={{
                            display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
                            width: 56, height: 56, borderRadius: '50%',
                            backgroundColor: 'rgba(34,197,94,0.12)', marginBottom: '20px',
                        }}
                    >
                        <CheckCircle2 size={28} color="#22c55e" />
                    </div>
                    <h2 style={{ fontSize: '20px', fontWeight: 700, color: '#ffffff', margin: '0 0 10px' }}>
                        Check your inbox
                    </h2>
                    <p style={{ fontSize: '13px', color: '#71717a', lineHeight: 1.6, marginBottom: '28px' }}>
                        We&apos;ve sent a password reset link to <strong style={{ color: '#a1a1aa' }}>{email}</strong>.
                        The link will expire in 1 hour.
                    </p>
                    <Link
                        href="/login"
                        style={{
                            display: 'inline-flex', alignItems: 'center', gap: '6px',
                            fontSize: '13px', color: '#71717a', textDecoration: 'none',
                        }}
                    >
                        <ArrowLeft size={13} /> Back to Login
                    </Link>
                </div>
            </div>
        );
    }

    return (
        <div style={card}>
            {/* Header */}
            <div style={{ marginBottom: '32px' }}>
                <div
                    style={{
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        width: 48, height: 48, borderRadius: '12px',
                        backgroundColor: 'rgba(249,115,22,0.1)', marginBottom: '20px', margin: '0 auto 20px',
                    }}
                >
                    <Mail size={22} color="#f97316" />
                </div>
                <h1 style={{ fontSize: '22px', fontWeight: 700, color: '#ffffff', margin: '0 0 8px', textAlign: 'center' }}>
                    Forgot Password?
                </h1>
                <p style={{ fontSize: '13px', color: '#71717a', textAlign: 'center', lineHeight: 1.6, margin: 0 }}>
                    No worries. Enter your email and we&apos;ll send you reset instructions.
                </p>
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

                <button
                    type="submit"
                    disabled={isLoading}
                    style={{
                        display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px',
                        width: '100%', padding: '13px 24px', borderRadius: '10px', border: 'none',
                        background: isLoading ? '#7c3e10' : 'linear-gradient(135deg, #f97316 0%, #ea6c10 100%)',
                        color: '#ffffff', fontSize: '15px', fontWeight: 600,
                        cursor: isLoading ? 'not-allowed' : 'pointer',
                        boxShadow: '0 4px 20px rgba(249,115,22,0.3)',
                    }}
                >
                    {isLoading ? 'Sending…' : 'Send Reset Link'}
                </button>
            </form>

            {/* Back */}
            <div style={{ textAlign: 'center', marginTop: '24px' }}>
                <Link
                    href="/login"
                    style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', fontSize: '13px', color: '#71717a', textDecoration: 'none' }}
                >
                    <ArrowLeft size={13} /> Back to Login
                </Link>
            </div>
        </div>
    );
};
