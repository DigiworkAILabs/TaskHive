'use client';

import React, { useState, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { useTasks } from '../hooks/useTasks';
import { TaskCard } from './TaskCard';
import { taskService } from '../services/taskService';
import { TaskStatus, TaskPriority, TaskListItem } from '../types/task.types';
import {
    Search, Filter, ClipboardList, CheckCircle2, Clock, AlertTriangle, Loader2, AlertCircle, Plus, X,
} from 'lucide-react';
import Link from 'next/link';

// ── Shared stat card ──────────────────────────────────────────────────────────

function StatCard({ label, value, color, icon }: { label: string; value: number; color: string; icon: React.ReactNode }) {
    return (
        <div
            style={{
                backgroundColor: '#161616', border: '1px solid #1f1f1f',
                borderRadius: '14px', padding: '20px 24px', flex: '1 1 160px',
            }}
        >
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#71717a', fontSize: '12px', marginBottom: '10px' }}>
                {icon}
                {label}
            </div>
            <div style={{ fontSize: '26px', fontWeight: 700, color }}>{value}</div>
        </div>
    );
}

// ── Filter Select ─────────────────────────────────────────────────────────────

function FilterSelect({ value, onChange, options, placeholder }: {
    value: string;
    onChange: (v: string) => void;
    options: { value: string; label: string }[];
    placeholder: string;
}) {
    return (
        <select
            value={value}
            onChange={(e) => onChange(e.target.value)}
            style={{
                padding: '8px 12px', borderRadius: '8px', border: '1px solid #2a2a2a',
                backgroundColor: '#111111', color: value ? '#ffffff' : '#71717a', fontSize: '13px', cursor: 'pointer', outline: 'none',
            }}
        >
            <option value="">{placeholder}</option>
            {options.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
        </select>
    );
}

// ── Main Component ────────────────────────────────────────────────────────────

export const TaskList: React.FC = () => {
    const router = useRouter();
    const { tasks: listTasks, pagination, filters, isLoading: listLoading, error: listError, fetchTasks, updateFilters, goToPage } = useTasks();

    const [search, setSearch] = useState('');
    const [searchResults, setSearchResults] = useState<TaskListItem[] | null>(null);
    const [searchLoading, setSearchLoading] = useState(false);
    const [searchError, setSearchError] = useState<string | null>(null);
    const [searchTotal, setSearchTotal] = useState(0);
    const [selected, setSelected] = useState<Set<string>>(new Set());
    const [deletingId, setDeletingId] = useState<string | null>(null);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    // Determine active data source
    const isSearchActive = searchResults !== null;
    const tasks = isSearchActive ? searchResults : listTasks;
    const isLoading = isSearchActive ? searchLoading : listLoading;
    const error = isSearchActive ? searchError : listError;

    // Stats derived from current page results (simplified)
    const total = isSearchActive ? searchTotal : pagination.totalElements;
    const done = tasks.filter((t) => t.status === 'DONE').length;
    const inProgress = tasks.filter((t) => t.status === 'IN_PROGRESS').length;
    const overdue = tasks.filter((t) => !['DONE', 'CANCELLED'].includes(t.status) && new Date(t.dueDate) < new Date()).length;

    const handleSearchInput = (e: React.ChangeEvent<HTMLInputElement>) => {
        const val = e.target.value;
        setSearch(val);
        if (!val.trim()) {
            // Clear search — go back to list
            setSearchResults(null);
            setSearchError(null);
        }
    };

    // Use ref so handleSearchSubmit always reads the LATEST search value (avoids stale closure)
    const searchRef = useRef(search);
    searchRef.current = search;

    const handleSearchSubmit = async () => {
        const q = searchRef.current.trim();
        if (!q) return;
        setSearchLoading(true);
        setSearchError(null);
        try {
            const data = await taskService.search(q, 0, 50);
            setSearchResults(data.content);
            setSearchTotal(data.totalElements);
        } catch (err: any) {
            setSearchError(err.response?.data?.message || 'Search failed');
            setSearchResults([]);
        } finally {
            setSearchLoading(false);
        }
    };

    const handleSearchKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') handleSearchSubmit();
    };

    const clearSearch = () => {
        setSearch('');
        setSearchResults(null);
        setSearchError(null);
    };

    const handleSelect = (id: string) => {
        setSelected((prev) => {
            const next = new Set(prev);
            next.has(id) ? next.delete(id) : next.add(id);
            return next;
        });
    };

    const handleSelectAll = () => {
        if (selected.size === tasks.length) {
            setSelected(new Set());
        } else {
            setSelected(new Set(tasks.map((t) => t.id)));
        }
    };

    const handleDelete = async (id: string) => {
        setDeletingId(id);
        setShowDeleteConfirm(true);
    };

    const confirmDelete = async () => {
        if (!deletingId) return;
        try {
            await taskService.delete(deletingId);
            setShowDeleteConfirm(false);
            setDeletingId(null);
            fetchTasks();
        } catch { /* handled */ }
    };

    const taskToDelete = tasks.find((t) => t.id === deletingId);

    const statusOptions: { value: string; label: string }[] = [
        { value: 'TODO', label: 'To Do' },
        { value: 'IN_PROGRESS', label: 'In Progress' },
        { value: 'IN_REVIEW', label: 'In Review' },
        { value: 'DONE', label: 'Done' },
        { value: 'CANCELLED', label: 'Cancelled' },
    ];

    const priorityOptions: { value: string; label: string }[] = [
        { value: 'LOW', label: 'Low' },
        { value: 'MEDIUM', label: 'Medium' },
        { value: 'HIGH', label: 'High' },
        { value: 'CRITICAL', label: 'Critical' },
    ];

    const totalPages = pagination.totalPages;
    const currentPage = pagination.page;

    return (
        <div>
            {/* Header */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '24px', flexWrap: 'wrap', gap: '12px' }}>
                <h1 style={{ fontSize: '20px', fontWeight: 700, color: '#ffffff', margin: 0 }}>All Tasks</h1>
                <Link
                    href="/admin/tasks/new"
                    style={{
                        display: 'flex', alignItems: 'center', gap: '8px',
                        padding: '8px 16px', borderRadius: '10px', border: 'none',
                        background: 'linear-gradient(135deg, #f97316 0%, #ea6c10 100%)',
                        color: '#ffffff', fontSize: '13px', fontWeight: 600,
                        textDecoration: 'none', boxShadow: '0 4px 16px rgba(249,115,22,0.3)',
                        transition: 'opacity 0.2s', whiteSpace: 'nowrap',
                    }}
                    onMouseEnter={(e) => (e.currentTarget.style.opacity = '0.88')}
                    onMouseLeave={(e) => (e.currentTarget.style.opacity = '1')}
                >
                    <Plus size={16} />
                    Create Task
                </Link>
            </div>

            {/* Stat Cards */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: '12px', marginBottom: '20px' }}>
                <StatCard label="Total Tasks" value={total} color="#ffffff" icon={<ClipboardList size={14} />} />
                <StatCard label="In Progress" value={inProgress} color="#3b82f6" icon={<Clock size={14} />} />
                <StatCard label="Completed" value={done} color="#22c55e" icon={<CheckCircle2 size={14} />} />
                <StatCard label="Overdue" value={overdue} color="#ef4444" icon={<AlertTriangle size={14} />} />
            </div>

            {/* Search + Filters */}
            <div
                style={{
                    backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '14px',
                    padding: '12px 16px', display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '20px', flexWrap: 'wrap',
                }}
            >
                <Filter size={15} color="#71717a" style={{ flexShrink: 0 }} />
                <div style={{ position: 'relative', flex: '1', minWidth: '150px' }}>
                    <Search size={14} color="#71717a" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
                    <input
                        type="text"
                        placeholder="Search tasks… (press Enter)"
                        value={search}
                        onChange={handleSearchInput}
                        onKeyDown={handleSearchKeyDown}
                        style={{
                            width: '100%', padding: '8px 36px 8px 36px', borderRadius: '8px',
                            border: `1px solid ${isSearchActive ? 'rgba(249,115,22,0.4)' : '#2a2a2a'}`, backgroundColor: '#111111',
                            color: '#ffffff', fontSize: '13px', outline: 'none', boxSizing: 'border-box',
                        }}
                    />
                    {search && (
                        <button
                            onClick={clearSearch}
                            style={{ position: 'absolute', right: '10px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: '#71717a', padding: '2px', display: 'flex', alignItems: 'center' }}
                        >
                            <X size={12} />
                        </button>
                    )}
                </div>
                <FilterSelect
                    value={filters.status || ''}
                    onChange={(v) => updateFilters({ status: v as TaskStatus || undefined })}
                    options={statusOptions}
                    placeholder="All Statuses"
                />
                <FilterSelect
                    value={filters.priority || ''}
                    onChange={(v) => updateFilters({ priority: v as TaskPriority || undefined })}
                    options={priorityOptions}
                    placeholder="All Priorities"
                />
                {(filters.status || filters.priority || isSearchActive) && (
                    <button
                        onClick={() => { clearSearch(); updateFilters({ status: undefined, priority: undefined, search: undefined }); }}
                        style={{
                            padding: '8px 14px', borderRadius: '8px', border: '1px solid #2a2a2a',
                            backgroundColor: 'transparent', color: '#71717a', fontSize: '12px', cursor: 'pointer',
                        }}
                    >
                        Clear
                    </button>
                )}
            </div>

            {/* Error */}
            {error && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', backgroundColor: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.25)', borderRadius: '10px', padding: '12px 16px', marginBottom: '16px', color: '#f87171', fontSize: '13px' }}>
                    <AlertCircle size={15} />{error}
                </div>
            )}

            {/* Table */}
            <div style={{ backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '14px', overflow: 'hidden' }}>
                {isLoading ? (
                    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '60px' }}>
                        <Loader2 size={28} color="#f97316" className="animate-spin" />
                    </div>
                ) : tasks.length === 0 ? (
                    <div style={{ padding: '60px', textAlign: 'center', color: '#52525b' }}>
                        <ClipboardList size={40} style={{ margin: '0 auto 12px', opacity: 0.4 }} />
                        <p style={{ margin: 0, fontSize: '15px' }}>No tasks found</p>
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                            <thead>
                                <tr style={{ borderBottom: '1px solid #1f1f1f' }}>
                                    {['Title', 'Priority', 'Status', 'Assigned To', 'Due Date', ''].map((h) => (
                                        <th
                                            key={h}
                                            style={{ padding: '14px 16px', textAlign: 'left', fontSize: '11px', fontWeight: 600, letterSpacing: '0.5px', color: '#f97316', textTransform: 'uppercase' as const }}
                                        >
                                            {h}
                                        </th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody>
                                {tasks.map((task) => (
                                    <TaskCard
                                        key={task.id}
                                        task={task}
                                        selected={selected.has(task.id)}
                                        onSelect={handleSelect}
                                        onView={(id) => router.push(`/admin/tasks/${id}`)}
                                        onEdit={(id) => router.push(`/admin/tasks/${id}`)}
                                        onDelete={handleDelete}
                                    />
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '16px', flexWrap: 'wrap', gap: '8px' }}>
                    <span style={{ color: '#71717a', fontSize: '13px' }}>
                        Showing {tasks.length} of {pagination.totalElements} tasks
                    </span>
                    <div style={{ display: 'flex', gap: '6px' }}>
                        <PageBtn disabled={currentPage === 0} onClick={() => goToPage(currentPage - 1)} label="← Prev" />
                        {Array.from({ length: Math.min(totalPages, 5) }, (_, i) => i + Math.max(0, currentPage - 2)).filter(p => p < totalPages).map((p) => (
                            <PageBtn key={p} disabled={false} active={p === currentPage} onClick={() => goToPage(p)} label={String(p + 1)} />
                        ))}
                        <PageBtn disabled={pagination.last} onClick={() => goToPage(currentPage + 1)} label="Next →" />
                    </div>
                </div>
            )}

            {/* Delete Confirmation */}
            {showDeleteConfirm && (
                <>
                    <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.6)', zIndex: 100 }} onClick={() => setShowDeleteConfirm(false)} />
                    <div
                        style={{
                            position: 'fixed', top: '50%', left: '50%', transform: 'translate(-50%,-50%)',
                            backgroundColor: '#161616', border: '1px solid #2a2a2a', borderRadius: '16px',
                            padding: '32px', zIndex: 101, width: '420px', maxWidth: '90vw', boxShadow: '0 32px 64px rgba(0,0,0,0.5)',
                        }}
                    >
                        <h3 style={{ color: '#ffffff', fontSize: '18px', fontWeight: 600, margin: '0 0 12px' }}>Delete Task</h3>
                        <p style={{ color: '#a1a1aa', fontSize: '14px', margin: '0 0 24px' }}>
                            Are you sure you want to delete <strong style={{ color: '#fff' }}>{taskToDelete?.title}</strong>?
                        </p>
                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px' }}>
                            <button
                                onClick={() => setShowDeleteConfirm(false)}
                                style={{ padding: '10px 20px', borderRadius: '10px', border: '1px solid #2a2a2a', backgroundColor: 'transparent', color: '#a1a1aa', fontSize: '14px', cursor: 'pointer' }}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={confirmDelete}
                                style={{ padding: '10px 20px', borderRadius: '10px', border: 'none', backgroundColor: '#ef4444', color: '#fff', fontSize: '14px', fontWeight: 600, cursor: 'pointer' }}
                            >
                                Delete
                            </button>
                        </div>
                    </div>
                </>
            )}
        </div>
    );
};

// ── Pagination button ─────────────────────────────────────────────────────────

function PageBtn({ label, onClick, disabled, active }: { label: string; onClick: () => void; disabled: boolean; active?: boolean }) {
    return (
        <button
            onClick={onClick}
            disabled={disabled}
            style={{
                padding: '7px 14px', borderRadius: '8px', border: '1px solid #2a2a2a',
                backgroundColor: active ? 'rgba(249,115,22,0.12)' : 'transparent',
                color: active ? '#f97316' : disabled ? '#3f3f46' : '#a1a1aa',
                fontSize: '13px', cursor: disabled ? 'not-allowed' : 'pointer', transition: 'background-color 0.15s',
            }}
        >
            {label}
        </button>
    );
}
