'use client';

import React, { useState } from 'react';
import { useEmployees } from '../hooks/useEmployees';
import { useEmployeeSearch } from '../hooks/useEmployeeSearch';
import { employeeService } from '../services/employeeService';
import { EmployeeCard } from './EmployeeCard';
import { EmployeeListItem } from '../types/employee.types';
import { useRouter } from 'next/navigation';
import {
    Search, Users, UserCheck, ChevronLeft, ChevronRight,
    SlidersHorizontal, Loader2, AlertCircle, UserPlus,
} from 'lucide-react';

// ── Stat Card ───────────────────────────────────────────────────────────────

function StatCard({ icon, label, value, iconBg }: {
    icon: React.ReactNode; label: string; value: number | string; iconBg: string;
}) {
    return (
        <div
            style={{
                backgroundColor: '#161616',
                border: '1px solid #1f1f1f',
                borderRadius: '14px',
                padding: '16px',
                minWidth: 0,
            }}
        >
            <div
                style={{
                    width: '44px', height: '44px', borderRadius: '12px',
                    backgroundColor: iconBg, display: 'flex', alignItems: 'center', justifyContent: 'center',
                    marginBottom: '14px',
                }}
            >
                {icon}
            </div>
            <div style={{ color: '#71717a', fontSize: '13px', marginBottom: '4px' }}>{label}</div>
            <div style={{ color: '#ffffff', fontSize: '28px', fontWeight: 700 }}>{value}</div>
        </div>
    );
}

// ── Select Dropdown ─────────────────────────────────────────────────────────

function FilterSelect({ value, onChange, options, placeholder }: {
    value: string; onChange: (v: string) => void; options: string[]; placeholder: string;
}) {
    return (
        <select
            value={value}
            onChange={(e) => onChange(e.target.value)}
            style={{
                backgroundColor: '#111111',
                border: '1px solid #2a2a2a',
                borderRadius: '10px',
                color: '#d4d4d8',
                fontSize: '13px',
                padding: '10px 14px',
                outline: 'none',
                cursor: 'pointer',
                appearance: 'none',
                backgroundImage: `url("data:image/svg+xml,%3Csvg width='10' height='6' viewBox='0 0 10 6' fill='none' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 1L5 5L9 1' stroke='%2371717a' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E")`,
                backgroundRepeat: 'no-repeat',
                backgroundPosition: 'right 12px center',
                paddingRight: '32px',
                minWidth: '120px',
            }}
        >
            <option value="">{placeholder}</option>
            {options.map((opt) => (
                <option key={opt} value={opt}>{opt}</option>
            ))}
        </select>
    );
}

// ── Main EmployeeList Component ─────────────────────────────────────────────

export const EmployeeList: React.FC = () => {
    const router = useRouter();
    const { employees, pagination, isLoading, error, updateFilters, goToPage, fetchEmployees } = useEmployees();
    const { query: searchQuery, setQuery: setSearchQuery } = useEmployeeSearch();

    const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
    const [departmentFilter, setDepartmentFilter] = useState('');
    const [statusFilter, setStatusFilter] = useState('');
    const [searchInput, setSearchInput] = useState('');
    const [actionLoading, setActionLoading] = useState<string | null>(null);

    // ── Filter departments from the current dataset ─────────────────────────
    const departments = [...new Set(employees.map((e) => e.department).filter(Boolean))];

    // ── Selection handlers ──────────────────────────────────────────────────
    const toggleSelect = (id: string) => {
        setSelectedIds((prev) => {
            const next = new Set(prev);
            if (next.has(id)) next.delete(id); else next.add(id);
            return next;
        });
    };

    const toggleSelectAll = () => {
        if (selectedIds.size === employees.length) {
            setSelectedIds(new Set());
        } else {
            setSelectedIds(new Set(employees.map((e) => e.id)));
        }
    };

    // ── Search handler ──────────────────────────────────────────────────────
    const handleSearch = (value: string) => {
        setSearchInput(value);
        setSearchQuery(value);
        // Use the list endpoint filter for integrated search
        updateFilters({ name: value || undefined, page: 0 });
    };

    // ── Department filter ───────────────────────────────────────────────────
    const handleDepartmentFilter = (value: string) => {
        setDepartmentFilter(value);
        updateFilters({ department: value || undefined, page: 0 });
    };

    // ── Status filter ───────────────────────────────────────────────────────
    const handleStatusFilter = (value: string) => {
        setStatusFilter(value);
        updateFilters({ status: value || undefined, page: 0 });
    };

    // ── Action handlers ─────────────────────────────────────────────────────
    const handleView = (id: string) => router.push(`/admin/employees/${id}`);
    const handleEdit = (id: string) => router.push(`/admin/employees/${id}`);

    const handleActivate = async (id: string) => {
        setActionLoading(id);
        try { await employeeService.activate(id); fetchEmployees(); }
        catch { /* swallow — list will show stale until refetch */ }
        finally { setActionLoading(null); }
    };

    const handleDeactivate = async (id: string) => {
        setActionLoading(id);
        try { await employeeService.deactivate(id); fetchEmployees(); }
        catch { /* swallow */ }
        finally { setActionLoading(null); }
    };

    const handleDelete = async (id: string) => {
        if (!confirm('Are you sure you want to delete this employee?')) return;
        setActionLoading(id);
        try { await employeeService.delete(id); fetchEmployees(); }
        catch { /* swallow */ }
        finally { setActionLoading(null); }
    };

    // ── Computed values ─────────────────────────────────────────────────────
    const activeCount = employees.filter((e) => e.status === 'ACTIVE').length;
    const startItem = pagination.page * pagination.size + 1;
    const endItem = Math.min(startItem + employees.length - 1, pagination.totalElements);

    // ── Pagination helpers ──────────────────────────────────────────────────
    const renderPageNumbers = () => {
        const pages: React.ReactNode[] = [];
        const total = pagination.totalPages;
        const current = pagination.page;

        for (let i = 0; i < total && i < 5; i++) {
            const pageNum = i;
            if (total > 5 && i === 4 && current < total - 1) {
                pages.push(
                    <span key="dots" style={{ color: '#52525b', padding: '0 4px' }}>…</span>
                );
                pages.push(
                    <PageButton key={total - 1} page={total - 1} current={current} onClick={() => goToPage(total - 1)} />
                );
                break;
            }
            pages.push(
                <PageButton key={pageNum} page={pageNum} current={current} onClick={() => goToPage(pageNum)} />
            );
        }
        return pages;
    };

    return (
        <div>
            {/* Stat Cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '12px', marginBottom: '24px' }}>
                <StatCard
                    icon={<Users size={22} color="#6366f1" />}
                    label="Total Employees"
                    value={pagination.totalElements || employees.length}
                    iconBg="rgba(99,102,241,0.12)"
                />
                <StatCard
                    icon={<UserCheck size={22} color="#22c55e" />}
                    label="Active Status"
                    value={activeCount}
                    iconBg="rgba(34,197,94,0.12)"
                />
                <StatCard
                    icon={<UserPlus size={22} color="#f97316" />}
                    label="Pending"
                    value={employees.filter((e) => e.status === 'PENDING').length}
                    iconBg="rgba(249,115,22,0.12)"
                />
            </div>

            {/* Search & Filter Bar */}
            <div
                style={{
                    backgroundColor: '#161616',
                    border: '1px solid #1f1f1f',
                    borderRadius: '14px',
                    padding: '12px 16px',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '10px',
                    marginBottom: '20px',
                    flexWrap: 'wrap',
                }}
            >
                {/* Search */}
                <div style={{ position: 'relative', flex: '1 1 200px', minWidth: 0 }}>
                    <Search size={16} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: '#52525b' }} />
                    <input
                        type="text"
                        placeholder="Search by name, role, or ID..."
                        value={searchInput}
                        onChange={(e) => handleSearch(e.target.value)}
                        style={{
                            width: '100%',
                            backgroundColor: '#111111',
                            border: '1px solid #2a2a2a',
                            borderRadius: '10px',
                            color: '#ffffff',
                            fontSize: '13px',
                            padding: '10px 14px 10px 40px',
                            outline: 'none',
                            transition: 'border-color 0.2s',
                        }}
                        onFocus={(e) => (e.currentTarget.style.borderColor = '#f97316')}
                        onBlur={(e) => (e.currentTarget.style.borderColor = '#2a2a2a')}
                    />
                </div>

                {/* Filter dropdowns */}
                <FilterSelect
                    value={departmentFilter}
                    onChange={handleDepartmentFilter}
                    options={departments}
                    placeholder="All Departments"
                />
                <FilterSelect
                    value={statusFilter}
                    onChange={handleStatusFilter}
                    options={['ACTIVE', 'INACTIVE', 'PENDING']}
                    placeholder="All Statuses"
                />

                {/* Filter icon */}
                <button
                    onClick={() => { setSearchInput(''); setDepartmentFilter(''); setStatusFilter(''); updateFilters({ name: undefined, department: undefined, status: undefined, page: 0 }); }}
                    style={{
                        width: '40px', height: '40px', borderRadius: '10px',
                        border: '1px solid #2a2a2a', backgroundColor: 'transparent',
                        display: 'flex', alignItems: 'center', justifyContent: 'center',
                        cursor: 'pointer', color: '#71717a', transition: 'color 0.15s',
                    }}
                    onMouseEnter={(e) => (e.currentTarget.style.color = '#ffffff')}
                    onMouseLeave={(e) => (e.currentTarget.style.color = '#71717a')}
                    title="Clear filters"
                >
                    <SlidersHorizontal size={16} />
                </button>
            </div>

            {/* Error */}
            {error && (
                <div
                    style={{
                        display: 'flex', alignItems: 'center', gap: '8px',
                        backgroundColor: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.25)',
                        borderRadius: '10px', padding: '12px 16px', marginBottom: '16px', color: '#f87171', fontSize: '13px',
                    }}
                >
                    <AlertCircle size={15} style={{ flexShrink: 0 }} />
                    {error}
                </div>
            )}

            {/* Table */}
            <div
                style={{
                    backgroundColor: '#161616',
                    border: '1px solid #1f1f1f',
                    borderRadius: '14px',
                    overflow: 'hidden',
                }}
            >
                {isLoading ? (
                    <div style={{ display: 'flex', justifyContent: 'center', padding: '60px 0' }}>
                        <Loader2 size={28} color="#f97316" className="animate-spin" />
                    </div>
                ) : employees.length === 0 ? (
                    <div style={{ textAlign: 'center', padding: '60px 24px', color: '#52525b', fontSize: '14px' }}>
                        No employees found.
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '600px' }}>
                            <thead>
                                <tr style={{ borderBottom: '1px solid #1f1f1f' }}>
                                    <HeaderCell>EMPLOYEE</HeaderCell>
                                    <HeaderCell>ROLE</HeaderCell>
                                    <HeaderCell>DEPARTMENT</HeaderCell>
                                    <HeaderCell>STATUS</HeaderCell>
                                    <HeaderCell>ACTIONS</HeaderCell>
                                </tr>
                            </thead>
                            <tbody>
                                {employees.map((emp) => (
                                    <EmployeeCard
                                        key={emp.id}
                                        employee={emp}
                                        selected={selectedIds.has(emp.id)}
                                        onSelect={toggleSelect}
                                        onView={handleView}
                                        onEdit={handleEdit}
                                        onActivate={handleActivate}
                                        onDeactivate={handleDeactivate}
                                        onDelete={handleDelete}
                                    />
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}

                {/* Pagination */}
                {!isLoading && employees.length > 0 && (
                    <div
                        style={{
                            display: 'flex', alignItems: 'center', justifyContent: 'space-between',
                            padding: '12px 16px', borderTop: '1px solid #1f1f1f', flexWrap: 'wrap', gap: '8px',
                        }}
                    >
                        <span style={{ color: '#71717a', fontSize: '13px' }}>
                            Showing <strong style={{ color: '#d4d4d8' }}>{startItem}-{endItem}</strong> of{' '}
                            <strong style={{ color: '#d4d4d8' }}>{pagination.totalElements}</strong> employees
                        </span>

                        <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <NavButton
                                disabled={pagination.page === 0}
                                onClick={() => goToPage(pagination.page - 1)}
                            >
                                <ChevronLeft size={16} />
                            </NavButton>

                            {renderPageNumbers()}

                            <NavButton
                                disabled={pagination.last}
                                onClick={() => goToPage(pagination.page + 1)}
                            >
                                <ChevronRight size={16} />
                            </NavButton>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
};

// ── Helper components ───────────────────────────────────────────────────────

function HeaderCell({ children }: { children: React.ReactNode }) {
    return (
        <th
            style={{
                padding: '14px 16px',
                textAlign: 'left',
                fontSize: '11px',
                fontWeight: 600,
                letterSpacing: '0.5px',
                color: '#f97316',
                textTransform: 'uppercase' as const,
            }}
        >
            {children}
        </th>
    );
}

function PageButton({ page, current, onClick }: { page: number; current: number; onClick: () => void }) {
    const isActive = page === current;
    return (
        <button
            onClick={onClick}
            style={{
                width: '32px', height: '32px', borderRadius: '8px',
                border: isActive ? 'none' : '1px solid transparent',
                backgroundColor: isActive ? '#f97316' : 'transparent',
                color: isActive ? '#ffffff' : '#71717a',
                fontSize: '13px', fontWeight: isActive ? 600 : 400,
                cursor: 'pointer', transition: 'all 0.15s',
            }}
            onMouseEnter={(e) => !isActive && (e.currentTarget.style.backgroundColor = 'rgba(255,255,255,0.05)')}
            onMouseLeave={(e) => !isActive && (e.currentTarget.style.backgroundColor = 'transparent')}
        >
            {page + 1}
        </button>
    );
}

function NavButton({ children, disabled, onClick }: { children: React.ReactNode; disabled: boolean; onClick: () => void }) {
    return (
        <button
            onClick={onClick}
            disabled={disabled}
            style={{
                width: '32px', height: '32px', borderRadius: '8px',
                border: '1px solid #2a2a2a',
                backgroundColor: 'transparent',
                color: disabled ? '#2a2a2a' : '#71717a',
                cursor: disabled ? 'not-allowed' : 'pointer',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                transition: 'color 0.15s',
            }}
        >
            {children}
        </button>
    );
}
