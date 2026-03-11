'use client';

import React, { useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useEmployee } from '../hooks/useEmployee';
import { useUpdateEmployee } from '../hooks/useUpdateEmployee';
import { useDeleteEmployee } from '../hooks/useDeleteEmployee';
import { employeeService } from '../services/employeeService';
import { ProfilePhotoUpload } from './ProfilePhotoUpload';
import { EmployeeForm } from './EmployeeForm';
import { UpdateEmployeeData, EmployeeStatus } from '../types/employee.types';
import {
    ArrowLeft, Pencil, Trash2, UserCheck, UserX,
    Mail, Phone, Building2, Calendar, Loader2, AlertCircle, BrainCircuit
} from 'lucide-react';
import { useProductivityScore } from '../../ml/hooks/useProductivityScore';
import { ProductivityScoreCard } from '../../ml/components/ProductivityScoreCard';
import Link from 'next/link';

const statusConfig: Record<EmployeeStatus, { label: string; color: string; bg: string }> = {
    ACTIVE: { label: 'Active', color: '#22c55e', bg: 'rgba(34,197,94,0.12)' },
    INACTIVE: { label: 'Inactive', color: '#ef4444', bg: 'rgba(239,68,68,0.12)' },
    PENDING: { label: 'Pending', color: '#eab308', bg: 'rgba(234,179,8,0.12)' },
};

export const EmployeeDetail: React.FC = () => {
    const params = useParams();
    const router = useRouter();
    const id = params.id as string;

    const { employee, isLoading, error, refetch } = useEmployee(id);
    const { updateEmployee, isLoading: isUpdating, error: updateError, success: updateSuccess } = useUpdateEmployee();
    const { deleteEmployee, isLoading: isDeleting } = useDeleteEmployee();
    const { 
        scoreData, 
        isLoading: isLoadingScore, 
        error: scoreError 
    } = useProductivityScore(id);

    const [isEditing, setIsEditing] = useState(false);
    const [statusLoading, setStatusLoading] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    // ── Status change handlers (PATCH activate/deactivate) ──────────────────
    const handleActivate = async () => {
        setStatusLoading(true);
        try {
            await employeeService.activate(id);
            refetch();
        } catch { /* error handled by refetch */ }
        finally { setStatusLoading(false); }
    };

    const handleDeactivate = async () => {
        setStatusLoading(true);
        try {
            await employeeService.deactivate(id);
            refetch();
        } catch { /* error handled by refetch */ }
        finally { setStatusLoading(false); }
    };

    // ── Delete handler ──────────────────────────────────────────────────────
    const handleDelete = async () => {
        await deleteEmployee(id);
        router.push('/admin/employees');
    };

    // ── Edit submit handler ─────────────────────────────────────────────────
    const handleEditSubmit = async (data: any) => {
        const result = await updateEmployee(id, data as UpdateEmployeeData);
        if (result) {
            setIsEditing(false);
            refetch();
        }
    };

    // ── Loading state ───────────────────────────────────────────────────────
    if (isLoading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '400px' }}>
                <Loader2 size={32} color="#f97316" className="animate-spin" />
            </div>
        );
    }

    if (error || !employee) {
        return (
            <div style={{ padding: '24px' }}>
                <div
                    style={{
                        display: 'flex', alignItems: 'center', gap: '8px',
                        backgroundColor: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.25)',
                        borderRadius: '10px', padding: '16px', color: '#f87171', fontSize: '14px',
                    }}
                >
                    <AlertCircle size={18} />
                    {error || 'Employee not found'}
                </div>
            </div>
        );
    }

    const status = statusConfig[employee.status] || statusConfig.PENDING;

    // ── Edit mode ───────────────────────────────────────────────────────────
    if (isEditing) {
        return (
            <div>
                <button
                    onClick={() => setIsEditing(false)}
                    style={{
                        display: 'flex', alignItems: 'center', gap: '6px',
                        background: 'none', border: 'none', color: '#71717a',
                        fontSize: '14px', cursor: 'pointer', marginBottom: '16px',
                    }}
                >
                    <ArrowLeft size={16} /> Cancel Edit
                </button>
                <EmployeeForm
                    mode="edit"
                    employee={employee}
                    onSubmit={handleEditSubmit}
                    isLoading={isUpdating}
                    error={updateError}
                    success={updateSuccess}
                />
            </div>
        );
    }

    // ── View mode ───────────────────────────────────────────────────────────
    return (
        <div>
            {/* Top bar */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '24px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    <Link
                        href="/admin/employees"
                        style={{
                            color: '#71717a', textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '6px',
                            fontSize: '14px', transition: 'color 0.15s',
                        }}
                        onMouseEnter={(e) => (e.currentTarget.style.color = '#f97316')}
                        onMouseLeave={(e) => (e.currentTarget.style.color = '#71717a')}
                    >
                        <ArrowLeft size={16} />
                        Back to Employees
                    </Link>
                    <span style={{ color: '#2a2a2a' }}>|</span>
                    <h1 style={{ fontSize: '20px', fontWeight: 700, color: '#ffffff', margin: 0 }}>
                        Employee Profile
                    </h1>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    {/* Edit button */}
                    <button
                        onClick={() => setIsEditing(true)}
                        style={{
                            display: 'flex', alignItems: 'center', gap: '6px',
                            padding: '10px 16px', borderRadius: '10px',
                            border: '1px solid #2a2a2a', backgroundColor: 'transparent',
                            color: '#d4d4d8', fontSize: '13px', cursor: 'pointer',
                            transition: 'border-color 0.15s',
                        }}
                        onMouseEnter={(e) => (e.currentTarget.style.borderColor = '#52525b')}
                        onMouseLeave={(e) => (e.currentTarget.style.borderColor = '#2a2a2a')}
                    >
                        <Pencil size={14} />
                        Edit
                    </button>

                    {/* More actions (three-dot is already in EmployeeCard, here we use explicit buttons) */}
                </div>
            </div>

            {/* Profile Banner */}
            <div
                style={{
                    backgroundColor: '#161616',
                    border: '1px solid #1f1f1f',
                    borderRadius: '16px',
                    padding: '32px',
                    marginBottom: '20px',
                }}
            >
                <div style={{ display: 'flex', alignItems: 'flex-start', gap: '24px', flexWrap: 'wrap' }}>
                    {/* Photo */}
                    <ProfilePhotoUpload
                        employeeId={employee.id}
                        currentPhotoUrl={employee.photoUrl}
                        firstName={employee.firstName}
                        lastName={employee.lastName}
                        onPhotoUploaded={() => refetch()}
                    />

                    {/* Info */}
                    <div style={{ flex: 1, minWidth: '240px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
                            <h2 style={{ fontSize: '24px', fontWeight: 700, color: '#ffffff', margin: 0 }}>
                                {employee.firstName} {employee.lastName}
                            </h2>
                            {employee.designation && (
                                <span
                                    style={{
                                        padding: '4px 12px', borderRadius: '999px', fontSize: '12px', fontWeight: 500,
                                        backgroundColor: 'rgba(249,115,22,0.12)', color: '#f97316',
                                    }}
                                >
                                    {employee.designation}
                                </span>
                            )}
                        </div>

                        <div
                            style={{
                                display: 'flex', alignItems: 'center', gap: '20px',
                                marginTop: '12px', flexWrap: 'wrap', color: '#a1a1aa', fontSize: '13px',
                            }}
                        >
                            <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                <Mail size={14} /> {employee.email}
                            </span>
                            {employee.phone && (
                                <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                    <Phone size={14} /> {employee.phone}
                                </span>
                            )}
                        </div>
                    </div>

                    {/* Status badge */}
                    <span
                        style={{
                            display: 'inline-flex', alignItems: 'center', gap: '6px',
                            padding: '6px 14px', borderRadius: '999px', fontSize: '13px', fontWeight: 500,
                            backgroundColor: status.bg, color: status.color,
                        }}
                    >
                        <span style={{ width: '7px', height: '7px', borderRadius: '50%', backgroundColor: status.color }} />
                        {status.label}
                    </span>
                </div>
            </div>

            {/* Info Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '24px' }}>
                <InfoCard icon={<Building2 size={18} />} label="Department" value={employee.department || '—'} />
                <InfoCard icon={<Calendar size={18} />} label="Join Date" value={employee.joinDate ? new Date(employee.joinDate).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' }) : '—'} />
            </div>

            {/* AI Productivity Section */}
            <div style={{ marginBottom: '24px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
                    <BrainCircuit size={18} color="#6366f1" />
                    <h2 style={{ color: "#ffffff", fontSize: "16px", fontWeight: 600, margin: 0 }}>
                        AI Productivity Insights
                    </h2>
                </div>
                <div style={{ maxWidth: '600px' }}>
                    <ProductivityScoreCard 
                        scoreData={scoreData}
                        isLoading={isLoadingScore}
                        error={scoreError}
                    />
                </div>
            </div>

            {/* Action Bar */}
            <div
                style={{
                    backgroundColor: '#161616',
                    border: '1px solid #1f1f1f',
                    borderRadius: '16px',
                    padding: '20px 24px',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '12px',
                    flexWrap: 'wrap',
                }}
            >
                <span style={{ color: '#71717a', fontSize: '13px', fontWeight: 500, marginRight: '8px' }}>Actions:</span>

                {employee.status !== 'ACTIVE' && (
                    <ActionButton
                        icon={<UserCheck size={14} />}
                        label="Activate"
                        onClick={handleActivate}
                        loading={statusLoading}
                        color="#22c55e"
                    />
                )}
                {employee.status === 'ACTIVE' && (
                    <ActionButton
                        icon={<UserX size={14} />}
                        label="Deactivate"
                        onClick={handleDeactivate}
                        loading={statusLoading}
                        color="#eab308"
                    />
                )}

                <ActionButton
                    icon={<Trash2 size={14} />}
                    label="Delete"
                    onClick={() => setShowDeleteConfirm(true)}
                    loading={isDeleting}
                    color="#ef4444"
                />
            </div>

            {/* Delete Confirmation Modal */}
            {showDeleteConfirm && (
                <>
                    <div
                        style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.6)', zIndex: 100 }}
                        onClick={() => setShowDeleteConfirm(false)}
                    />
                    <div
                        style={{
                            position: 'fixed', top: '50%', left: '50%', transform: 'translate(-50%,-50%)',
                            backgroundColor: '#161616', border: '1px solid #2a2a2a', borderRadius: '16px',
                            padding: '32px', zIndex: 101, width: '420px', maxWidth: '90vw',
                            boxShadow: '0 32px 64px rgba(0,0,0,0.5)',
                        }}
                    >
                        <h3 style={{ color: '#ffffff', fontSize: '18px', fontWeight: 600, margin: '0 0 12px 0' }}>
                            Delete Employee
                        </h3>
                        <p style={{ color: '#a1a1aa', fontSize: '14px', margin: '0 0 24px 0' }}>
                            Are you sure you want to delete <strong style={{ color: '#ffffff' }}>{employee.firstName} {employee.lastName}</strong>?
                            This action will soft-delete the employee and prevent them from logging in.
                        </p>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                            <button
                                onClick={() => setShowDeleteConfirm(false)}
                                style={{
                                    padding: '10px 20px', borderRadius: '10px', border: '1px solid #2a2a2a',
                                    backgroundColor: 'transparent', color: '#a1a1aa', fontSize: '14px', cursor: 'pointer',
                                }}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleDelete}
                                disabled={isDeleting}
                                style={{
                                    display: 'flex', alignItems: 'center', gap: '6px',
                                    padding: '10px 20px', borderRadius: '10px', border: 'none',
                                    backgroundColor: '#ef4444', color: '#ffffff', fontSize: '14px', fontWeight: 600,
                                    cursor: isDeleting ? 'not-allowed' : 'pointer', opacity: isDeleting ? 0.6 : 1,
                                }}
                            >
                                {isDeleting && <Loader2 size={14} className="animate-spin" />}
                                {isDeleting ? 'Deleting…' : 'Delete'}
                            </button>
                        </div>
                    </div>
                </>
            )}
        </div>
    );
};

// ── Info Card ────────────────────────────────────────────────────────────────

function InfoCard({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
    return (
        <div
            style={{
                backgroundColor: '#161616',
                border: '1px solid #1f1f1f',
                borderRadius: '12px',
                padding: '20px',
            }}
        >
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#71717a', fontSize: '12px', marginBottom: '8px' }}>
                {icon}
                {label}
            </div>
            <div style={{ color: '#ffffff', fontSize: '15px', fontWeight: 600 }}>{value}</div>
        </div>
    );
}

// ── Action Button ────────────────────────────────────────────────────────────

function ActionButton({ icon, label, onClick, loading, color }: {
    icon: React.ReactNode; label: string; onClick: () => void; loading: boolean; color: string;
}) {
    return (
        <button
            onClick={onClick}
            disabled={loading}
            style={{
                display: 'flex', alignItems: 'center', gap: '6px',
                padding: '8px 16px', borderRadius: '8px',
                border: `1px solid ${color}30`,
                backgroundColor: `${color}10`,
                color: color,
                fontSize: '13px', fontWeight: 500,
                cursor: loading ? 'not-allowed' : 'pointer',
                opacity: loading ? 0.6 : 1,
                transition: 'background-color 0.15s',
            }}
        >
            {loading ? <Loader2 size={14} className="animate-spin" /> : icon}
            {label}
        </button>
    );
}
