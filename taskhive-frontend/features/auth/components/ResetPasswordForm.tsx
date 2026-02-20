'use client';

import React, { useState } from 'react';
import { useResetPassword } from '../hooks/useResetPassword';
import { useSearchParams } from 'next/navigation';
import { Eye, EyeOff, KeyRound, AlertCircle } from 'lucide-react';
import Link from 'next/link';
import { ArrowLeft } from 'lucide-react';

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

export const ResetPasswordForm = () => {
    const { resetPassword, isLoading, error } = useResetPassword();
    const searchParams = useSearchParams();
    const token = searchParams.get('token');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirm, setShowConfirm] = useState(false);
    const [matchError, setMatchError] = useState('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (password !== confirmPassword) {
            setMatchError("Passwords don't match.");
            return;
        }
        setMatchError('');
        resetPassword({ token, newPassword: password });
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
                    <p style={{ color: '#f87171', fontSize: '14px' }}>Invalid or expired reset link.</p>
                    <Link href="/forgot-password" style={{ color: '#f97316', fontSize: '13px', marginTop: '12px', display: 'inline-block' }}>
                        Request a new link
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
                        backgroundColor: 'rgba(249,115,22,0.1)', margin: '0 auto 20px',
                    }}
                >
                    <KeyRound size={22} color="#f97316" />
                </div>
                <h1 style={{ fontSize: '22px', fontWeight: 700, color: '#ffffff', margin: '0 0 8px', textAlign: 'center' }}>
                    Set New Password
                </h1>
                <p style={{ fontSize: '13px', color: '#71717a', textAlign: 'center', lineHeight: 1.6, margin: 0 }}>
                    Your new password must be different from previously used passwords.
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

            {/* Form */}
            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
                <PasswordInput
                    id="new-password"
                    label="New Password"
                    placeholder="Enter new password"
                    value={password}
                    show={showPassword}
                    onToggle={() => setShowPassword(!showPassword)}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                    minLength={8}
                />

                <PasswordInput
                    id="confirm-password"
                    label="Confirm New Password"
                    placeholder="Confirm your password"
                    value={confirmPassword}
                    show={showConfirm}
                    onToggle={() => setShowConfirm(!showConfirm)}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                />

                {/* Password hint */}
                <p style={{ fontSize: '12px', color: '#52525b', margin: 0 }}>
                    Minimum 8 characters. Cannot match your last 3 passwords.
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
                    {isLoading ? 'Resetting…' : 'Reset Password'}
                </button>
            </form>

            <div style={{ textAlign: 'center', marginTop: '24px' }}>
                <Link href="/login" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', fontSize: '13px', color: '#71717a', textDecoration: 'none' }}>
                    <ArrowLeft size={13} /> Back to Login
                </Link>
            </div>
        </div>
    );
};
