'use client';

import React, { useState, useEffect } from 'react';
import { CreateEmployeeData, UpdateEmployeeData, Employee } from '../types/employee.types';
import { ArrowLeft, Loader2, AlertCircle, CheckCircle } from 'lucide-react';
import Link from 'next/link';
import { DatePicker } from './DatePicker';

interface EmployeeFormProps {
    mode: 'create' | 'edit';
    employee?: Employee | null;
    onSubmit: (data: CreateEmployeeData | UpdateEmployeeData) => Promise<void>;
    isLoading: boolean;
    error: string | null;
    success: boolean;
}

// ── Shared input style ──────────────────────────────────────────────────────

const inputBase: React.CSSProperties = {
    width: '100%',
    backgroundColor: '#111111',
    border: '1px solid #2a2a2a',
    borderRadius: '10px',
    color: '#ffffff',
    fontSize: '14px',
    padding: '12px 16px',
    outline: 'none',
    transition: 'border-color 0.2s, box-shadow 0.2s',
};

function FormInput({
    label,
    id,
    required,
    error,
    ...rest
}: React.InputHTMLAttributes<HTMLInputElement> & { label: string; error?: string }) {
    const [focused, setFocused] = useState(false);
    return (
        <div>
            <label
                htmlFor={id}
                style={{ display: 'block', fontSize: '13px', color: '#a1a1aa', marginBottom: '6px', fontWeight: 500 }}
            >
                {label} {required && <span style={{ color: '#f97316' }}>*</span>}
            </label>
            <input
                id={id}
                {...rest}
                style={{
                    ...inputBase,
                    borderColor: error ? '#ef4444' : focused ? '#f97316' : '#2a2a2a',
                    boxShadow: focused ? '0 0 0 3px rgba(249,115,22,0.12)' : 'none',
                }}
                onFocus={(e) => { setFocused(true); rest.onFocus?.(e); }}
                onBlur={(e) => { setFocused(false); rest.onBlur?.(e); }}
            />
            {error && (
                <p style={{ color: '#ef4444', fontSize: '12px', marginTop: '4px' }}>{error}</p>
            )}
        </div>
    );
}

// ── Main Component ──────────────────────────────────────────────────────────

export const EmployeeForm: React.FC<EmployeeFormProps> = ({
    mode,
    employee,
    onSubmit,
    isLoading,
    error,
    success,
}) => {
    const [formData, setFormData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        department: '',
        designation: '',
        joinDate: '',
    });

    const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

    // Pre-fill form data for edit mode
    useEffect(() => {
        if (mode === 'edit' && employee) {
            setFormData({
                firstName: employee.firstName || '',
                lastName: employee.lastName || '',
                email: employee.email || '',
                phone: employee.phone || '',
                department: employee.department || '',
                designation: employee.designation || '',
                joinDate: employee.joinDate || '',
            });
        }
    }, [mode, employee]);



    const validate = (): boolean => {
        const errors: Record<string, string> = {};

        if (!formData.firstName.trim()) errors.firstName = 'First name is required';
        if (!formData.lastName.trim()) errors.lastName = 'Last name is required';

        if (mode === 'create') {
            if (!formData.email.trim()) {
                errors.email = 'Email is required';
            } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
                errors.email = 'Invalid email format';
            }
        }

        setFieldErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const handleChange = (field: string) => (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData((prev) => ({ ...prev, [field]: e.target.value }));
        // Clear field error on change
        if (fieldErrors[field]) {
            setFieldErrors((prev) => {
                const next = { ...prev };
                delete next[field];
                return next;
            });
        }
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!validate()) return;

        if (mode === 'create') {
            const payload: CreateEmployeeData = {
                firstName: formData.firstName.trim(),
                lastName: formData.lastName.trim(),
                email: formData.email.trim(),
            };
            if (formData.phone.trim()) payload.phone = formData.phone.trim();
            if (formData.department.trim()) payload.department = formData.department.trim();
            if (formData.designation.trim()) payload.designation = formData.designation.trim();
            if (formData.joinDate) {
                payload.joinDate = formData.joinDate.includes('T') ? formData.joinDate : `${formData.joinDate}T23:59:59`;
            }
            onSubmit(payload);
        } else {
            const payload: UpdateEmployeeData = {};
            if (formData.firstName.trim()) payload.firstName = formData.firstName.trim();
            if (formData.lastName.trim()) payload.lastName = formData.lastName.trim();
            if (formData.phone.trim()) payload.phone = formData.phone.trim();
            if (formData.department.trim()) payload.department = formData.department.trim();
            if (formData.designation.trim()) payload.designation = formData.designation.trim();
            if (formData.joinDate) {
                payload.joinDate = formData.joinDate.includes('T') ? formData.joinDate : `${formData.joinDate}T23:59:59`;
            }
            onSubmit(payload);
        }
    };

    const cardStyle: React.CSSProperties = {
        backgroundColor: '#161616',
        border: '1px solid #1f1f1f',
        borderRadius: '16px',
    };

    return (
        <div>
            {/* Header */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '28px', flexWrap: 'wrap' }}>
                <Link
                    href="/admin/employees"
                    style={{
                        color: '#71717a',
                        textDecoration: 'none',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px',
                        fontSize: '14px',
                        transition: 'color 0.15s',
                    }}
                    onMouseEnter={(e) => (e.currentTarget.style.color = '#f97316')}
                    onMouseLeave={(e) => (e.currentTarget.style.color = '#71717a')}
                >
                    <ArrowLeft size={16} />
                    Back to Employees
                </Link>
                <span style={{ color: '#2a2a2a' }}>|</span>
                <h1 style={{ fontSize: '20px', fontWeight: 700, color: '#ffffff', margin: 0 }}>
                    {mode === 'create' ? 'Add New Employee' : 'Edit Employee'}
                </h1>
            </div>

            {/* Error Alert */}
            {error && (
                <div
                    style={{
                        display: 'flex', alignItems: 'center', gap: '8px',
                        backgroundColor: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.25)',
                        borderRadius: '10px', padding: '12px 16px', marginBottom: '20px', color: '#f87171', fontSize: '13px',
                    }}
                >
                    <AlertCircle size={15} style={{ flexShrink: 0 }} />
                    {error}
                </div>
            )}

            {/* Success Alert */}
            {success && (
                <div
                    style={{
                        display: 'flex', alignItems: 'center', gap: '8px',
                        backgroundColor: 'rgba(34,197,94,0.08)', border: '1px solid rgba(34,197,94,0.25)',
                        borderRadius: '10px', padding: '12px 16px', marginBottom: '20px', color: '#22c55e', fontSize: '13px',
                    }}
                >
                    <CheckCircle size={15} style={{ flexShrink: 0 }} />
                    {mode === 'create' ? 'Employee created successfully!' : 'Employee updated successfully!'}
                </div>
            )}

            {/* Form */}
            <div style={cardStyle}>
                <form onSubmit={handleSubmit} className="flex flex-col gap-5 p-4 md:p-8">
                    {/* Row: First Name + Last Name */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <FormInput
                            id="firstName"
                            label="First Name"
                            placeholder="Enter first name"
                            value={formData.firstName}
                            onChange={handleChange('firstName')}
                            required
                            error={fieldErrors.firstName}
                        />
                        <FormInput
                            id="lastName"
                            label="Last Name"
                            placeholder="Enter last name"
                            value={formData.lastName}
                            onChange={handleChange('lastName')}
                            required
                            error={fieldErrors.lastName}
                        />
                    </div>

                    {/* Row: Email + Phone */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <FormInput
                            id="email"
                            label="Email"
                            type="email"
                            placeholder="name@company.com"
                            value={formData.email}
                            onChange={handleChange('email')}
                            required={mode === 'create'}
                            disabled={mode === 'edit'}
                            error={fieldErrors.email}
                        />
                        <FormInput
                            id="phone"
                            label="Phone"
                            type="tel"
                            placeholder="+1 (555) 123-4567"
                            value={formData.phone}
                            onChange={handleChange('phone')}
                        />
                    </div>

                    {/* Row: Department + Designation */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <FormInput
                            id="department"
                            label="Department"
                            placeholder="e.g. Engineering"
                            value={formData.department}
                            onChange={handleChange('department')}
                        />
                        <FormInput
                            id="designation"
                            label="Designation"
                            placeholder="e.g. Senior Developer"
                            value={formData.designation}
                            onChange={handleChange('designation')}
                        />
                    </div>

                    {/* Row: Join Date */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <DatePicker
                            id="joinDate"
                            label="Join Date"
                            value={formData.joinDate}
                            onChange={(date) => setFormData(prev => ({ ...prev, joinDate: date }))}
                            minDate={new Date()}
                            maxDate={(() => { const d = new Date(); d.setDate(d.getDate() + 30); return d; })()}
                        />
                    </div>

                    {/* Actions */}
                    <div className="flex flex-col-reverse sm:flex-row justify-end gap-3 mt-2">
                        <Link
                            href="/admin/employees"
                            style={{
                                padding: '12px 24px',
                                borderRadius: '10px',
                                border: '1px solid #2a2a2a',
                                backgroundColor: 'transparent',
                                color: '#a1a1aa',
                                fontSize: '14px',
                                fontWeight: 500,
                                textDecoration: 'none',
                                transition: 'border-color 0.2s',
                            }}
                            onMouseEnter={(e) => (e.currentTarget.style.borderColor = '#52525b')}
                            onMouseLeave={(e) => (e.currentTarget.style.borderColor = '#2a2a2a')}
                        >
                            Cancel
                        </Link>
                        <button
                            type="submit"
                            disabled={isLoading}
                            style={{
                                display: 'flex', alignItems: 'center', gap: '8px',
                                padding: '12px 28px',
                                borderRadius: '10px',
                                border: 'none',
                                background: isLoading ? '#7c3e10' : 'linear-gradient(135deg, #f97316 0%, #ea6c10 100%)',
                                color: '#ffffff',
                                fontSize: '14px',
                                fontWeight: 600,
                                cursor: isLoading ? 'not-allowed' : 'pointer',
                                boxShadow: '0 4px 20px rgba(249,115,22,0.3)',
                                transition: 'opacity 0.2s',
                            }}
                            onMouseEnter={(e) => !isLoading && ((e.currentTarget as HTMLButtonElement).style.opacity = '0.88')}
                            onMouseLeave={(e) => ((e.currentTarget as HTMLButtonElement).style.opacity = '1')}
                        >
                            {isLoading && <Loader2 size={16} className="animate-spin" />}
                            {isLoading
                                ? (mode === 'create' ? 'Creating…' : 'Saving…')
                                : (mode === 'create' ? 'Create Employee' : 'Save Changes')}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};
