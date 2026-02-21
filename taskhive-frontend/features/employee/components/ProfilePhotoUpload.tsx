'use client';

import React, { useRef, useState } from 'react';
import { employeeService } from '../services/employeeService';
import { Camera, Loader2 } from 'lucide-react';

interface ProfilePhotoUploadProps {
    employeeId: string;
    currentPhotoUrl: string | null;
    firstName: string;
    lastName: string;
    onPhotoUploaded: (newUrl: string) => void;
}

const ACCEPTED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];
const MAX_SIZE_MB = 5;

export const ProfilePhotoUpload: React.FC<ProfilePhotoUploadProps> = ({
    employeeId,
    currentPhotoUrl,
    firstName,
    lastName,
    onPhotoUploaded,
}) => {
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [isUploading, setIsUploading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const initials = `${firstName.charAt(0)}${lastName.charAt(0)}`.toUpperCase();

    const handleClick = () => {
        fileInputRef.current?.click();
    };

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;

        // Validate type
        if (!ACCEPTED_TYPES.includes(file.type)) {
            setError('Only JPG, PNG, and WebP images are allowed');
            return;
        }

        // Validate size
        if (file.size > MAX_SIZE_MB * 1024 * 1024) {
            setError(`File size must be under ${MAX_SIZE_MB}MB`);
            return;
        }

        setIsUploading(true);
        setError(null);
        try {
            const photoUrl = await employeeService.uploadPhoto(employeeId, file);
            onPhotoUploaded(photoUrl);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Failed to upload photo');
        } finally {
            setIsUploading(false);
            // Reset input so same file can be re-selected
            if (fileInputRef.current) fileInputRef.current.value = '';
        }
    };

    return (
        <div style={{ position: 'relative', display: 'inline-block' }}>
            <div
                onClick={handleClick}
                style={{
                    width: '100px',
                    height: '100px',
                    borderRadius: '16px',
                    overflow: 'hidden',
                    cursor: 'pointer',
                    position: 'relative',
                    backgroundColor: '#1f1f1f',
                    border: '2px solid #2a2a2a',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    transition: 'border-color 0.2s',
                }}
                onMouseEnter={(e) => (e.currentTarget.style.borderColor = '#f97316')}
                onMouseLeave={(e) => (e.currentTarget.style.borderColor = '#2a2a2a')}
            >
                {currentPhotoUrl ? (
                    <img
                        src={currentPhotoUrl}
                        alt={`${firstName} ${lastName}`}
                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    />
                ) : (
                    <span style={{ fontSize: '32px', fontWeight: 700, color: '#f97316' }}>
                        {initials}
                    </span>
                )}

                {/* Hover overlay */}
                <div
                    style={{
                        position: 'absolute',
                        inset: 0,
                        backgroundColor: 'rgba(0,0,0,0.5)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        opacity: 0,
                        transition: 'opacity 0.2s',
                    }}
                    onMouseEnter={(e) => (e.currentTarget.style.opacity = '1')}
                    onMouseLeave={(e) => (e.currentTarget.style.opacity = '0')}
                >
                    {isUploading ? (
                        <Loader2 size={24} color="#ffffff" className="animate-spin" />
                    ) : (
                        <Camera size={24} color="#ffffff" />
                    )}
                </div>
            </div>

            {/* Verified badge */}
            <div
                style={{
                    position: 'absolute',
                    bottom: '-2px',
                    right: '-2px',
                    width: '24px',
                    height: '24px',
                    borderRadius: '50%',
                    backgroundColor: '#22c55e',
                    border: '2px solid #0a0a0a',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                }}
            >
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none">
                    <polyline points="20 6 9 17 4 12" stroke="white" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
            </div>

            <input
                ref={fileInputRef}
                type="file"
                accept=".jpg,.jpeg,.png,.webp"
                style={{ display: 'none' }}
                onChange={handleFileChange}
            />

            {error && (
                <p style={{ color: '#ef4444', fontSize: '11px', marginTop: '6px', maxWidth: '100px' }}>
                    {error}
                </p>
            )}
        </div>
    );
};
