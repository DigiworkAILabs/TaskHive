'use client';

import { ResetPasswordForm } from '@/features/auth/components/ResetPasswordForm';
import { Suspense } from 'react';

export default function ResetPasswordPage() {
    return (
        <Suspense fallback={<div className="text-white text-center">Loading...</div>}>
            <ResetPasswordForm />
        </Suspense>
    );
}
