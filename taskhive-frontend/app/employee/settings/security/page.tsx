'use client';

import { Bell, Loader2, ShieldCheck } from 'lucide-react';
import { useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ChangePasswordForm } from '@/features/auth/components/ChangePasswordForm';
import {
    useNotificationPreferences,
    useUpdateNotificationPreferences,
} from '@/features/notification/hooks/useNotificationPreferences';

const prefSchema = z.object({
    emailEnabled: z.boolean(),
    inAppEnabled: z.boolean(),
    taskAssigned: z.boolean(),
    taskOverdue: z.boolean(),
    dailyDigest: z.boolean(),
    digestTime: z.string().nullable().optional(),
});

type PrefFormValues = z.infer<typeof prefSchema>;

/* ─── Toggle switch component ─────────────────────────────────── */
function Toggle({
    checked,
    onChange,
    label,
}: {
    checked: boolean;
    onChange: (v: boolean) => void;
    label: string;
}) {
    return (
        <button
            type="button"
            role="switch"
            aria-checked={checked}
            aria-label={label}
            onClick={() => onChange(!checked)}
            className={[
                'relative inline-flex h-6 w-11 shrink-0 items-center rounded-full',
                'transition-colors duration-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-orange-500',
                checked ? 'bg-orange-500' : 'bg-zinc-700',
            ].join(' ')}
        >
            <span
                className={[
                    'inline-block h-4 w-4 transform rounded-full bg-white shadow-sm',
                    'transition-transform duration-200',
                    checked ? 'translate-x-6' : 'translate-x-1',
                ].join(' ')}
            />
        </button>
    );
}

/* ─── Notification Preferences Section ───────────────────────── */
function NotificationPreferencesSection() {
    const { data: prefs, isLoading } = useNotificationPreferences();
    const { mutate, isPending } = useUpdateNotificationPreferences();

    const { register, handleSubmit, watch, setValue, reset } =
        useForm<PrefFormValues>({ resolver: zodResolver(prefSchema) });

    useEffect(() => {
        if (prefs) {
            reset({
                emailEnabled: prefs.emailEnabled,
                inAppEnabled: prefs.inAppEnabled,
                taskAssigned: prefs.taskAssigned,
                taskOverdue: prefs.taskOverdue,
                dailyDigest: prefs.dailyDigest,
                digestTime: prefs.digestTime,
            });
        }
    }, [prefs, reset]);

    const dailyDigestValue = watch('dailyDigest');

    function onSubmit(values: PrefFormValues) {
        mutate({
            ...values,
            digestTime: values.dailyDigest ? (values.digestTime ?? null) : null,
        });
    }

    const prefRows: { field: keyof PrefFormValues; label: string; description: string }[] = [
        {
            field: 'emailEnabled',
            label: 'Email Notifications',
            description: 'Receive notifications via email',
        },
        {
            field: 'inAppEnabled',
            label: 'In-App Notifications',
            description: 'Show notifications inside the app',
        },
        {
            field: 'taskAssigned',
            label: 'Task Assignment Alerts',
            description: 'Get notified when a task is assigned to you',
        },
        {
            field: 'taskOverdue',
            label: 'Overdue Task Alerts',
            description: 'Get notified when your tasks become overdue',
        },
        {
            field: 'dailyDigest',
            label: 'Daily Digest Email',
            description: 'Receive a daily summary of your tasks',
        },
    ];

    /* Loading skeleton */
    if (isLoading) {
        return (
            <div className="rounded-xl border border-zinc-700/50 bg-zinc-800/50 p-6">
                <div className="flex items-center gap-3 mb-6">
                    <div className="w-5 h-5 rounded bg-zinc-700/50 animate-pulse" />
                    <div className="h-5 w-48 rounded bg-zinc-700/50 animate-pulse" />
                </div>
                <div className="space-y-1">
                    {[1, 2, 3, 4, 5].map((i) => (
                        <div key={i} className="flex justify-between items-center py-3 border-b border-zinc-700/30 last:border-0">
                            <div className="space-y-1.5">
                                <div className="h-4 w-40 rounded bg-zinc-700/50 animate-pulse" />
                                <div className="h-3 w-56 rounded bg-zinc-700/50 animate-pulse" />
                            </div>
                            <div className="h-6 w-11 rounded-full bg-zinc-700/50 animate-pulse" />
                        </div>
                    ))}
                </div>
            </div>
        );
    }

    return (
        <div className="rounded-xl border border-zinc-700/50 bg-zinc-800/50 p-6">
            {/* Card header */}
            <div className="flex items-center gap-3 mb-1">
                <div className="w-8 h-8 rounded-lg bg-orange-500/15 flex items-center justify-center">
                    <Bell className="w-4 h-4 text-orange-500" />
                </div>
                <div>
                    <h2 className="text-base font-semibold text-white">Notification Preferences</h2>
                    <p className="text-xs text-zinc-500">Control how and when you receive notifications</p>
                </div>
            </div>

            <div className="border-t border-zinc-700/50 mt-4" />

            <form onSubmit={handleSubmit(onSubmit)} className="mt-1">
                {prefRows.map((row, index) => (
                    <div
                        key={row.field}
                        className={[
                            'flex items-center justify-between py-3.5',
                            index < prefRows.length - 1 ? 'border-b border-zinc-700/30' : '',
                        ].join(' ')}
                    >
                        <div>
                            <p className="text-sm font-medium text-zinc-200">{row.label}</p>
                            <p className="text-xs text-zinc-500 mt-0.5">{row.description}</p>
                        </div>
                        <Toggle
                            checked={!!(watch(row.field) as boolean)}
                            onChange={(v) => setValue(row.field, v)}
                            label={row.label}
                        />
                    </div>
                ))}

                {/* Conditional digest time input */}
                {dailyDigestValue && (
                    <div className="py-4 border-t border-zinc-700/30 space-y-2">
                        <p className="text-sm font-medium text-zinc-200">Digest Delivery Time</p>
                        <p className="text-xs text-zinc-500">What time should we send your daily digest?</p>
                        <input
                            type="time"
                            {...register('digestTime')}
                            className="bg-zinc-900 border border-zinc-700 text-white
                                       rounded-lg px-3 py-2 text-sm focus:outline-none
                                       focus:ring-1 focus:ring-orange-500 focus:border-orange-500"
                        />
                    </div>
                )}

                <div className="mt-5 flex justify-end">
                    <button
                        type="submit"
                        disabled={isPending}
                        className="bg-orange-500 hover:bg-orange-600 disabled:opacity-60
                                   text-white font-medium rounded-lg px-5 py-2.5 text-sm
                                   flex items-center gap-2 transition-colors"
                    >
                        {isPending && <Loader2 className="w-4 h-4 animate-spin" />}
                        {isPending ? 'Saving…' : 'Save Preferences'}
                    </button>
                </div>
            </form>
        </div>
    );
}

/* ─── Page ────────────────────────────────────────────────────── */
export default function SecuritySettingsPage() {
    return (
        <div className="max-w-3xl space-y-6 p-6">
            {/* Page heading */}
            <div className="pb-2 border-b border-zinc-800">
                <h1 className="text-xl font-bold text-white">Security &amp; Notifications</h1>
                <p className="mt-1 text-sm text-zinc-500">
                    Manage your password and notification preferences.
                </p>
            </div>

            {/* Change password card */}
            <div className="rounded-xl border border-zinc-700/50 bg-zinc-800/50 p-6">
                <div className="flex items-center gap-3 mb-1">
                    <div className="w-8 h-8 rounded-lg bg-orange-500/15 flex items-center justify-center">
                        <ShieldCheck className="w-4 h-4 text-orange-500" />
                    </div>
                    <div>
                        <h2 className="text-base font-semibold text-white">Change Password</h2>
                        <p className="text-xs text-zinc-500">Use a long, random password to stay secure.</p>
                    </div>
                </div>
                <div className="border-t border-zinc-700/50 mt-4 pt-5">
                    <ChangePasswordForm />
                </div>
            </div>

            {/* Notification preferences card */}
            <NotificationPreferencesSection />
        </div>
    );
}
