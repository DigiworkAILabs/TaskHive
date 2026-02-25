'use client';

import React, { useState } from 'react';
import { Eye, EyeOff, Loader2 } from 'lucide-react';
import { useChangePassword } from '../hooks/useChangePassword';

function PasswordInput({
    label,
    value,
    onChange,
    placeholder,
}: {
    label: string;
    value: string;
    onChange: (v: string) => void;
    placeholder?: string;
}) {
    const [show, setShow] = useState(false);
    return (
        <div className="space-y-1.5">
            <label className="block text-sm font-medium text-zinc-300">{label}</label>
            <div className="relative">
                <input
                    type={show ? 'text' : 'password'}
                    value={value}
                    onChange={(e) => onChange(e.target.value)}
                    placeholder={placeholder ?? '••••••••'}
                    required
                    className="w-full bg-zinc-900 border border-zinc-700 text-white placeholder-zinc-600
                               rounded-lg px-3 py-2.5 pr-10 text-sm
                               focus:outline-none focus:ring-1 focus:ring-orange-500 focus:border-orange-500
                               transition-colors"
                />
                <button
                    type="button"
                    onClick={() => setShow((s) => !s)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-500 hover:text-zinc-300 transition-colors"
                    tabIndex={-1}
                    aria-label={show ? 'Hide password' : 'Show password'}
                >
                    {show ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
            </div>
        </div>
    );
}

export const ChangePasswordForm = () => {
    const { changePassword, isLoading, error, success } = useChangePassword();
    const [oldPassword, setOldPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [confirmError, setConfirmError] = useState('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setConfirmError('');
        if (newPassword !== confirmPassword) {
            setConfirmError("New passwords don't match.");
            return;
        }
        changePassword({ oldPassword, newPassword });
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4 max-w-sm">
            {error && (
                <div className="flex items-center gap-2 rounded-lg bg-red-500/10 border border-red-500/30
                                px-3 py-2.5 text-sm text-red-400">
                    {error}
                </div>
            )}
            {success && (
                <div className="flex items-center gap-2 rounded-lg bg-green-500/10 border border-green-500/30
                                px-3 py-2.5 text-sm text-green-400">
                    Password changed successfully!
                </div>
            )}

            <PasswordInput
                label="Current Password"
                value={oldPassword}
                onChange={setOldPassword}
                placeholder="Enter your current password"
            />
            <PasswordInput
                label="New Password"
                value={newPassword}
                onChange={setNewPassword}
                placeholder="Enter a new password"
            />
            <PasswordInput
                label="Confirm New Password"
                value={confirmPassword}
                onChange={setConfirmPassword}
                placeholder="Confirm your new password"
            />

            {confirmError && (
                <p className="text-xs text-red-400">{confirmError}</p>
            )}

            <div className="pt-1">
                <button
                    type="submit"
                    disabled={isLoading}
                    className="bg-orange-500 hover:bg-orange-600 disabled:opacity-60
                               text-white font-medium rounded-lg px-5 py-2.5 text-sm
                               flex items-center gap-2 transition-colors"
                >
                    {isLoading && <Loader2 className="w-4 h-4 animate-spin" />}
                    {isLoading ? 'Changing...' : 'Change Password'}
                </button>
            </div>
        </form>
    );
};
