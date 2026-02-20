'use client';

import React, { useState } from 'react';
import { useActivateAccount } from '../hooks/useActivateAccount';
import { useSearchParams } from 'next/navigation';
import { Eye, EyeOff, ShieldCheck, AlertCircle, CheckCircle2 } from 'lucide-react';

interface PasswordInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
    label: string;
    show: boolean;
    onToggle: () => void;
}

function PasswordInput({ label, id, show, onToggle, ...rest }: PasswordInputProps) {
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
                    type={show ? 'text' : 'password'}
                    {...rest}
                    style={{
                        width: '100%',
                        backgroundColor: '#111111',
                        border: `1px solid ${focused ? '#f97316' : '#2a2a2a'}`,
                        borderRadius: '10px',
                        color: '#ffffff',
                        fontSize: '14px',
                        padding: '12px 44px 12px 16px',
                        outline: 'none',
                        boxShadow: focused ? '0 0 0 3px rgba(249,115,22,0.12)' : 'none',
                        boxSizing: 'border-box',
                    }}
                    onFocus={(e) => { setFocused(true); rest.onFocus?.(e); }}
                    onBlur={(e) => { setFocused(false); rest.onBlur?.(e); }}
                />
                <button
                    type="button"
                    onClick={onToggle}
                    style={{
                        position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)',
                        background: 'none', border: 'none', cursor: 'pointer', color: '#52525b', padding: 0,
                    }}
                >
                    {show ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
            </div>
        </div>
    );
}

// Password strength helper
function getStrength(pwd: string): { level: number; label: string; color: string } {
    if (pwd.length === 0) return { level: 0, label: '', color: '#27272a' };
    let score = 0;
    if (pwd.length >= 8) score++;
    if (/[A-Z]/.test(pwd)) score++;
    if (/[0-9]/.test(pwd)) score++;
    if (/[^A-Za-z0-9]/.test(pwd)) score++;
    const map: { label: string; color: string }[] = [
        { label: 'Weak', color: '#ef4444' },
        { label: 'Fair', color: '#f97316' },
        { label: 'Good', color: '#eab308' },
        { label: 'Strong', color: '#22c55e' },
        { label: 'Strong', color: '#22c55e' },
    ];
    return { level: score, ...map[score] };
}

export const ActivateAccountForm = () => {
    const { activateAccount, isLoading, error } = useActivateAccount();
    const searchParams = useSearchParams();
    const token = searchParams.get('token');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [matchError, setMatchError] = useState('');
    const [activated, setActivated] = useState(false);

    const strength = getStrength(password);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (password !== confirmPassword) {
            setMatchError("Passwords don't match.");
            return;
        }
        setMatchError('');
        await activateAccount({ token, newPassword: password });
        setActivated(true);
    };

    const card: React.CSSProperties = {
        backgroundColor: '#161616',
        border: '1px solid #1f1f1f',
        borderRadius: '20px',
        padding: '40px 36px',
        boxShadow: '0 32px 64px rgba(0,0,0,0.5)',
    };

    if (!token) {
        return (
            <div style={card}>
                <div style={{ textAlign: 'center' }}>
                    <p style={{ color: '#f87171', fontSize: '14px' }}>Invalid or expired activation link.</p>
                    <p style={{ color: '#71717a', fontSize: '12px', marginTop: '8px' }}>Please contact your administrator for a new invitation.</p>
                </div>
            </div>
        );
    }

    if (activated && !error) {
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
                        Account Activated!
                    </h2>
                    <p style={{ fontSize: '13px', color: '#71717a', marginBottom: '24px', lineHeight: 1.6 }}>
                        Your account is now active. You can sign in to access the workspace.
                    </p>
                    <a
                        href="/login"
                        style={{
                            display: 'inline-flex', alignItems: 'center', gap: '8px',
                            padding: '11px 28px', borderRadius: '10px', border: 'none',
                            background: 'linear-gradient(135deg, #f97316 0%, #ea6c10 100%)',
                            color: '#ffffff', fontSize: '14px', fontWeight: 600, textDecoration: 'none',
                        }}
                    >
                        Go to Login
                    </a>
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
                        backgroundColor: 'rgba(249,115,22,0.1)', margin: '0 auto 20px',
                    }}
                >
                    <ShieldCheck size={22} color="#f97316" />
                </div>
                <h1 style={{ fontSize: '22px', fontWeight: 700, color: '#ffffff', margin: '0 0 8px', textAlign: 'center' }}>
                    Activate Your Account
                </h1>
                <p style={{ fontSize: '13px', color: '#71717a', textAlign: 'center', lineHeight: 1.6, margin: 0 }}>
                    Set a strong password to secure your TaskHive account.
                </p>
            </div>

            {/* Errors */}
            {(error || matchError) && (
                <div
                    style={{
                        display: 'flex', alignItems: 'center', gap: '8px',
                        backgroundColor: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.25)',
                        borderRadius: '10px', padding: '10px 14px', marginBottom: '20px', color: '#f87171', fontSize: '13px',
                    }}
                >
                    <AlertCircle size={15} style={{ flexShrink: 0 }} />
                    {matchError || error}
                </div>
            )}

            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
                <div>
                    <PasswordInput
                        id="password"
                        label="Set Password"
                        placeholder="Create a strong password"
                        value={password}
                        show={showPassword}
                        onToggle={() => setShowPassword(!showPassword)}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                        minLength={8}
                    />
                    {/* Strength bar */}
                    {password.length > 0 && (
                        <div style={{ marginTop: '8px' }}>
                            <div style={{ display: 'flex', gap: '4px', marginBottom: '4px' }}>
                                {[1, 2, 3, 4].map((i) => (
                                    <div
                                        key={i}
                                        style={{
                                            flex: 1, height: '3px', borderRadius: '2px',
                                            backgroundColor: i <= strength.level ? strength.color : '#27272a',
                                            transition: 'background-color 0.3s',
                                        }}
                                    />
                                ))}
                            </div>
                            <p style={{ fontSize: '11px', color: strength.color, margin: 0 }}>{strength.label}</p>
                        </div>
                    )}
                </div>

                <PasswordInput
                    id="confirm-password"
                    label="Confirm Password"
                    placeholder="Repeat your password"
                    value={confirmPassword}
                    show={showConfirm}
                    onToggle={() => setShowConfirm(!showConfirm)}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                />

                <p style={{ fontSize: '12px', color: '#52525b', margin: 0 }}>
                    Must be at least 8 characters with uppercase, numbers, and symbols.
                </p>

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
                    {isLoading ? 'Activating…' : 'Activate Account'}
                </button>
            </form>
        </div>
    );
};
