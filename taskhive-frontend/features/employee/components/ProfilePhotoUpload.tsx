'use client';

import React, { useRef, useState } from 'react';
import { Camera, Upload, X, Check } from 'lucide-react';
import { employeeService } from '../services/employeeService';
import { cn } from '@/shared/utils/cn';

interface ProfilePhotoUploadProps {
    employeeId: string;
    currentPhotoUrl?: string;
    onUploadSuccess?: () => void;
}

export default function ProfilePhotoUpload({
    employeeId,
    currentPhotoUrl,
    onUploadSuccess
}: ProfilePhotoUploadProps) {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleFileUpload = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (!file) return;

        setLoading(true);
        setError(null);
        setSuccess(false);

        try {
            await employeeService.uploadPhoto(employeeId, file);
            setSuccess(true);
            if (onUploadSuccess) onUploadSuccess();
            setTimeout(() => setSuccess(false), 3000);
        } catch (err: any) {
            setError(err.message || 'Failed to upload photo');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="relative inline-block group">
            <div className="w-32 h-32 rounded-3xl bg-zinc-900 border-4 border-[#161616] overflow-hidden shadow-2xl relative">
                {currentPhotoUrl ? (
                    <img
                        src={currentPhotoUrl}
                        alt="Profile"
                        className="w-full h-full object-cover transition-transform group-hover:scale-110"
                    />
                ) : (
                    <div className="w-full h-full flex items-center justify-center bg-gradient-to-tr from-zinc-800 to-zinc-900">
                        <Camera className="w-10 h-10 text-zinc-600" />
                    </div>
                )}

                {/* Overlay on hover */}
                <div
                    onClick={() => fileInputRef.current?.click()}
                    className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer flex flex-col items-center justify-center gap-2"
                >
                    <Upload className="w-6 h-6 text-white" />
                    <span className="text-[10px] font-bold text-white uppercase tracking-tighter">Change Photo</span>
                </div>

                {/* Loading Spinner */}
                {loading && (
                    <div className="absolute inset-0 bg-black/40 backdrop-blur-[2px] flex items-center justify-center">
                        <div className="w-6 h-6 border-2 border-orange-500 border-t-transparent rounded-full animate-spin" />
                    </div>
                )}
            </div>

            {/* Success Indicator */}
            {success && (
                <div className="absolute -top-2 -right-2 w-8 h-8 bg-green-500 rounded-xl border-4 border-[#161616] flex items-center justify-center text-white animate-bounce shadow-lg">
                    <Check className="w-4 h-4" />
                </div>
            )}

            {/* Error Tooltip */}
            {error && (
                <div className="absolute top-full mt-2 left-1/2 -translate-x-1/2 w-48 bg-red-500 text-white text-[10px] font-bold py-2 px-3 rounded-lg shadow-xl z-10 text-center animate-in fade-in slide-in-from-top-2">
                    {error}
                </div>
            )}

            {/* Hidden Input */}
            <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileUpload}
                accept="image/*"
                className="hidden"
            />
        </div>
    );
}
