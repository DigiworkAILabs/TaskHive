import React, { useState } from 'react';
import { useChangePassword } from '../hooks/useChangePassword';

export const ChangePasswordForm = () => {
    const { changePassword, isLoading, error, success } = useChangePassword();
    const [oldPassword, setOldPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (newPassword !== confirmPassword) {
            alert("New passwords don't match");
            return;
        }
        changePassword({ oldPassword, newPassword });
    };

    return (
        <form onSubmit={handleSubmit}>
            {error && <div className="error">{error}</div>}
            {success && <div className="success">Password changed successfully!</div>}
            <div>
                <label>Current Password</label>
                <input type="password" value={oldPassword} onChange={(e) => setOldPassword(e.target.value)} required />
            </div>
            <div>
                <label>New Password</label>
                <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required />
            </div>
            <div>
                <label>Confirm New Password</label>
                <input type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />
            </div>
            <button type="submit" disabled={isLoading}>
                {isLoading ? 'Changing...' : 'Change Password'}
            </button>
        </form>
    );
};
