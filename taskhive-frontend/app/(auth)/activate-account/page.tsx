'use client';

import { ActivateAccountForm } from '@/features/auth/components/ActivateAccountForm';
import { Suspense } from 'react';

export default function ActivateAccountPage() {
    return (
        <Suspense fallback={<div className="text-white text-center">Loading...</div>}>
            <ActivateAccountForm />
        </Suspense>
    );
}
