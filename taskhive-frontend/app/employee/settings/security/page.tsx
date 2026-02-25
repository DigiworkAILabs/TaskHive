'use client';

import { ChangePasswordForm } from '@/features/auth/components/ChangePasswordForm';

export default function SecuritySettingsPage() {
    return (
        <div className="space-y-6">
            <div>
                <h3 className="text-lg font-medium leading-6 text-gray-900">Security Settings</h3>
                <p className="mt-1 text-sm text-gray-500">Update your password and security preferences.</p>
            </div>
            <div className="bg-white shadow sm:rounded-lg">
                <div className="px-4 py-5 sm:p-6">
                    <h3 className="text-lg font-medium leading-6 text-gray-900">Change Password</h3>
                    <div className="mt-2 max-w-xl text-sm text-gray-500">
                        <p>Ensure your account is using a long, random password to stay secure.</p>
                    </div>
                    <div className="mt-5">
                        <ChangePasswordForm />
                    </div>
                </div>
            </div>
        </div>
    );
}
