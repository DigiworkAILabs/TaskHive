'use client';

import React from 'react';
import {
    WorkloadRecommendation as WorkloadRecommendationType,
    EmployeeScoreBreakdown
} from '../types/ml.types';
import {
    Brain,
    CheckCircle2,
    Info,
    UserPlus,
    BarChart3,
    AlertCircle
} from 'lucide-react';

import { EmployeeListItem } from '@/features/employee/types/employee.types';

interface WorkloadRecommendationProps {
    recommendation: WorkloadRecommendationType;
    onAccept: (employeeId: string) => void;
    isLoading?: boolean;
    employees: EmployeeListItem[];
}

/**
 * WorkloadRecommendation Component
 * ────────────────────────────────
 * Displays the ML-recommended assignee with reasoning and score breakdown.
 * Allows the admin to "Apply" the recommendation to the form.
 */
const WorkloadRecommendation: React.FC<WorkloadRecommendationProps> = ({
    recommendation,
    onAccept,
    isLoading,
    employees
}) => {
    if (!recommendation && !isLoading) return null;

    const { recommendedEmployeeId, scoreBreakdown, reasoning, fallbackUsed } = recommendation || {};

    const getEmployeeName = (id: string) => {
        const emp = employees.find(e => e.id === id);
        return emp ? `${emp.firstName} ${emp.lastName}` : 'Unknown Employee';
    };

    const recommendedName = recommendedEmployeeId ? getEmployeeName(recommendedEmployeeId) : '';

    // Clean up reasoning: Replace the UUID at the start (or anywhere) with the name
    let sanitizedReasoning = reasoning || '';
    if (recommendedEmployeeId && sanitizedReasoning.includes(recommendedEmployeeId)) {
        sanitizedReasoning = sanitizedReasoning.replaceAll(recommendedEmployeeId, recommendedName);
    }

    // If fallback used or no recommendation, show a small hint
    if (fallbackUsed || !recommendedEmployeeId) {
        return (
            <div style={{
                marginTop: '12px',
                padding: '12px 16px',
                backgroundColor: 'rgba(255,165,0,0.05)',
                border: '1px dashed #3a3a3a',
                borderRadius: '10px',
                display: 'flex',
                alignItems: 'center',
                gap: '10px',
                color: '#a1a1aa',
                fontSize: '13px'
            }}>
                <Info size={14} color="#f59e0b" />
                <span>{reasoning || 'Recommendation unavailable. Please select manually.'}</span>
            </div>
        );
    }

    return (
        <div style={{
            marginTop: '16px',
            backgroundColor: '#1a1a1a',
            border: '1px solid #2a2a2a',
            borderRadius: '12px',
            overflow: 'hidden',
        }}>
            {/* Header */}
            <div style={{
                padding: '12px 16px',
                borderBottom: '1px solid #2a2a2a',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                backgroundColor: 'rgba(249,115,22,0.03)'
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Brain size={14} color="#f97316" />
                    <span style={{ fontSize: '13px', fontWeight: 600, color: '#f5f5f5' }}>
                        AI Recommendation
                    </span>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '11px', color: '#71717a' }}>
                    <BarChart3 size={11} />
                    Ranked by Workload & Performance
                </div>
            </div>

            {/* Body */}
            <div style={{ padding: '16px' }}>
                <p style={{ fontSize: '13px', color: '#d4d4d8', marginBottom: '14px', lineHeight: 1.5 }}>
                    {sanitizedReasoning}
                </p>

                {/* Score Breakdown (Small bars) */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginBottom: '16px' }}>
                    {scoreBreakdown.map((item: EmployeeScoreBreakdown) => {
                        const isRecommended = item.employeeId === recommendedEmployeeId;
                        const name = getEmployeeName(item.employeeId);
                        return (
                            <div key={item.employeeId}>
                                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '4px' }}>
                                    <span style={{ fontSize: '11px', color: isRecommended ? '#f97316' : '#a1a1aa' }}>
                                        {name}
                                    </span>
                                    <span style={{
                                        fontSize: '11px',
                                        color: isRecommended ? '#ffffff' : '#71717a',
                                        fontWeight: isRecommended ? 600 : 400
                                    }}>
                                        {Math.round(item.score)}%
                                    </span>
                                </div>
                                <div style={{
                                    height: '6px',
                                    backgroundColor: '#2a2a2a',
                                    borderRadius: '3px',
                                    position: 'relative',
                                    overflow: 'hidden'
                                }}>
                                    <div style={{
                                        position: 'absolute',
                                        left: 0,
                                        top: 0,
                                        bottom: 0,
                                        width: `${item.score}%`,
                                        backgroundColor: isRecommended ? '#f97316' : '#52525b',
                                        borderRadius: '3px',
                                        transition: 'width 0.5s ease-out'
                                    }} />
                                </div>
                            </div>
                        );
                    })}
                </div>

                {/* Action button */}
                <button
                    type="button"
                    onClick={() => onAccept(recommendedEmployeeId)}
                    style={{
                        width: '100%',
                        padding: '10px',
                        backgroundColor: '#2a2a2a',
                        border: '1px solid #3a3a3a',
                        borderRadius: '8px',
                        color: '#ffffff',
                        fontSize: '13px',
                        fontWeight: 500,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        gap: '8px',
                        cursor: 'pointer',
                        transition: 'background-color 0.2s, border-color 0.2s',
                    }}
                    onMouseEnter={(e) => {
                        e.currentTarget.style.backgroundColor = '#333333';
                        e.currentTarget.style.borderColor = '#f97316';
                    }}
                    onMouseLeave={(e) => {
                        e.currentTarget.style.backgroundColor = '#2a2a2a';
                        e.currentTarget.style.borderColor = '#3a3a3a';
                    }}
                >
                    <UserPlus size={14} color="#f97316" />
                    Assign to {recommendedName}
                </button>
            </div>
        </div>
    );
};

export default WorkloadRecommendation;
