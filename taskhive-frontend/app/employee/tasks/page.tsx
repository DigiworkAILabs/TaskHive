'use client';

import React, { useState, useRef } from 'react';
import { useMyTasks } from '@/features/task/hooks/useMyTasks';
import { TaskCard } from '@/features/task/components/TaskCard';
import { taskService } from '@/features/task/services/taskService';
import { useRouter } from 'next/navigation';
import { Loader2, ClipboardList, AlertCircle, Filter, Search, X, ChevronUp, ChevronDown, ChevronsUpDown } from 'lucide-react';
import { TaskStatus, TaskPriority, TaskListItem } from '@/features/task/types/task.types';

// ── Sort column header ─────────────────────────────────────────────────────────

interface SortHeaderProps {
    label: string;
    field: string;
    currentSortBy: string;
    currentSortDir: 'asc' | 'desc';
    onSort: (field: string) => void;
}

function SortHeader({ label, field, currentSortBy, currentSortDir, onSort }: SortHeaderProps) {
    const isActive = currentSortBy === field;
    return (
        <th
            onClick={() => onSort(field)}
            style={{
                padding: '14px 16px', textAlign: 'left', fontSize: '11px', fontWeight: 600,
                color: isActive ? '#f97316' : '#71717a', textTransform: 'uppercase',
                cursor: 'pointer', userSelect: 'none', whiteSpace: 'nowrap',
                transition: 'color 0.15s',
            }}
            onMouseEnter={(e) => !isActive && (e.currentTarget.style.color = '#d4d4d8')}
            onMouseLeave={(e) => !isActive && (e.currentTarget.style.color = '#71717a')}
        >
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                {label}
                {isActive
                    ? currentSortDir === 'asc'
                        ? <ChevronUp size={12} style={{ color: '#f97316' }} />
                        : <ChevronDown size={12} style={{ color: '#f97316' }} />
                    : <ChevronsUpDown size={12} style={{ opacity: 0.4 }} />
                }
            </span>
        </th>
    );
}

// ── Page ──────────────────────────────────────────────────────────────────────

export default function EmployeeTasksPage() {
    const { tasks: myTasks, pagination, filters, isLoading: myTasksLoading, error: myTasksError, updateFilters } = useMyTasks();
    const router = useRouter();

    // ── Search state ──────────────────────────────────────────────────────────
    const [search, setSearch] = useState('');
    const [searchResults, setSearchResults] = useState<TaskListItem[] | null>(null);
    const [searchLoading, setSearchLoading] = useState(false);
    const [searchError, setSearchError] = useState<string | null>(null);
    const searchRef = useRef(search);
    searchRef.current = search;

    const isSearchActive = searchResults !== null;
    const tasks = isSearchActive ? searchResults : myTasks;
    const isLoading = isSearchActive ? searchLoading : myTasksLoading;
    const error = isSearchActive ? searchError : myTasksError;

    // Active sort state (read from filters for accuracy)
    const sortBy = (filters.sortBy as string) || 'dueDate';
    const sortDir = (filters.sortDir as 'asc' | 'desc') || 'asc';

    const handleSort = (field: string) => {
        if (isSearchActive) return; // sorting not applicable to local search results
        const newDir: 'asc' | 'desc' = sortBy === field && sortDir === 'asc' ? 'desc' : 'asc';
        updateFilters({ sortBy: field, sortDir: newDir });
    };

    const handleSearchInput = (e: React.ChangeEvent<HTMLInputElement>) => {
        const val = e.target.value;
        setSearch(val);
        if (!val.trim()) { setSearchResults(null); setSearchError(null); }
    };

    const handleSearchSubmit = async () => {
        const q = searchRef.current.trim();
        if (!q) return;
        setSearchLoading(true);
        setSearchError(null);
        try {
            const data = await taskService.search(q, 0, 50);
            setSearchResults(data.content);
        } catch (err: any) {
            setSearchError(err.response?.data?.message || 'Search failed');
            setSearchResults([]);
        } finally {
            setSearchLoading(false);
        }
    };

    const clearSearch = () => { setSearch(''); setSearchResults(null); setSearchError(null); };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') handleSearchSubmit();
    };

    // ── Stats ─────────────────────────────────────────────────────────────────
    const stats = {
        total: isSearchActive ? searchResults!.length : pagination.totalElements,
        todo: tasks.filter(t => t.status === 'TODO').length,
        inProgress: tasks.filter(t => t.status === 'IN_PROGRESS').length,
        done: tasks.filter(t => t.status === 'DONE').length
    };

    return (
        <div>
            <div style={{ marginBottom: '24px' }}>
                <h1 style={{ fontSize: '22px', fontWeight: 700, color: '#ffffff', margin: 0 }}>My Tasks</h1>
                <p style={{ color: '#71717a', fontSize: '14px', marginTop: '4px' }}>Track and manage your assigned tasks</p>
            </div>

            {/* Simple Stats */}
            <div style={{ display: 'flex', gap: '16px', marginBottom: '24px', flexWrap: 'wrap' }}>
                <QuickStat label="Total" value={stats.total} color="#ffffff" />
                <QuickStat label="To Do" value={stats.todo} color="#a1a1aa" />
                <QuickStat label="In Progress" value={stats.inProgress} color="#3b82f6" />
                <QuickStat label="Completed" value={stats.done} color="#22c55e" />
            </div>

            {/* Filter + Search Bar */}
            <div
                style={{
                    backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '14px',
                    padding: '16px 20px', display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '20px', flexWrap: 'wrap',
                }}
            >
                <Filter size={15} color="#71717a" />

                {/* Search Input */}
                <div style={{ position: 'relative', flex: '1', minWidth: '180px' }}>
                    <Search size={14} color="#71717a" style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)' }} />
                    <input
                        type="text"
                        placeholder="Search tasks… (press Enter)"
                        value={search}
                        onChange={handleSearchInput}
                        onKeyDown={handleKeyDown}
                        style={{
                            width: '100%', padding: '8px 36px 8px 36px', borderRadius: '8px', boxSizing: 'border-box',
                            border: `1px solid ${isSearchActive ? 'rgba(249,115,22,0.4)' : '#2a2a2a'}`,
                            backgroundColor: '#111111', color: '#ffffff', fontSize: '13px', outline: 'none',
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

                {/* Status + Priority Filters — hidden when search is active */}
                {!isSearchActive && (
                    <>
                        <select
                            value={filters.status || ''}
                            onChange={(e) => updateFilters({ status: e.target.value as TaskStatus || undefined })}
                            style={{
                                padding: '8px 12px', borderRadius: '8px', border: '1px solid #2a2a2a',
                                backgroundColor: '#111111', color: '#ffffff', fontSize: '13px', cursor: 'pointer', outline: 'none',
                            }}
                        >
                            <option value="">All Statuses</option>
                            <option value="TODO">To Do</option>
                            <option value="IN_PROGRESS">In Progress</option>
                            <option value="IN_REVIEW">In Review</option>
                            <option value="DONE">Done</option>
                        </select>

                        <select
                            value={filters.priority || ''}
                            onChange={(e) => updateFilters({ priority: e.target.value as TaskPriority || undefined })}
                            style={{
                                padding: '8px 12px', borderRadius: '8px', border: '1px solid #2a2a2a',
                                backgroundColor: '#111111', color: '#ffffff', fontSize: '13px', cursor: 'pointer', outline: 'none',
                            }}
                        >
                            <option value="">All Priorities</option>
                            <option value="LOW">Low</option>
                            <option value="MEDIUM">Medium</option>
                            <option value="HIGH">High</option>
                            <option value="CRITICAL">Critical</option>
                        </select>
                    </>
                )}

                {isSearchActive && (
                    <button
                        onClick={clearSearch}
                        style={{
                            padding: '8px 14px', borderRadius: '8px', border: '1px solid rgba(249,115,22,0.3)',
                            backgroundColor: 'rgba(249,115,22,0.08)', color: '#f97316', fontSize: '12px', cursor: 'pointer',
                        }}
                    >
                        Clear Search
                    </button>
                )}
            </div>

            {/* Error State */}
            {error && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', backgroundColor: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.25)', borderRadius: '10px', padding: '12px 16px', marginBottom: '20px', color: '#f87171', fontSize: '13px' }}>
                    <AlertCircle size={15} /> {error}
                </div>
            )}

            {/* Task Table */}
            <div style={{ backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '14px', overflow: 'hidden' }}>
                {isLoading ? (
                    <div style={{ display: 'flex', justifyContent: 'center', padding: '60px' }}>
                        <Loader2 size={24} color="#f97316" className="animate-spin" />
                    </div>
                ) : tasks.length === 0 ? (
                    <div style={{ padding: '60px', textAlign: 'center', color: '#52525b' }}>
                        <ClipboardList size={40} style={{ margin: '0 auto 12px', opacity: 0.4 }} />
                        <p style={{ margin: 0 }}>{isSearchActive ? `No tasks matched "${search}"` : 'No tasks found'}</p>
                    </div>
                ) : (
                    <div style={{ overflowX: 'auto' }}>
                        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                            <thead>
                                <tr style={{ borderBottom: '1px solid #1f1f1f' }}>
                                    {/* Non-sortable */}
                                    <th style={{ padding: '14px 16px', textAlign: 'left', fontSize: '11px', fontWeight: 600, color: '#71717a', textTransform: 'uppercase' }}>
                                        Title
                                    </th>
                                    <SortHeader label="Priority" field="priority" currentSortBy={sortBy} currentSortDir={sortDir} onSort={handleSort} />
                                    <SortHeader label="Status" field="status" currentSortBy={sortBy} currentSortDir={sortDir} onSort={handleSort} />
                                    <SortHeader label="Due Date" field="dueDate" currentSortBy={sortBy} currentSortDir={sortDir} onSort={handleSort} />
                                    {/* Actions — no sort */}
                                    <th style={{ padding: '14px 16px' }} />
                                </tr>
                            </thead>
                            <tbody>
                                {tasks.map(task => (
                                    <TaskCard
                                        key={task.id}
                                        task={task}
                                        selected={false}
                                        onSelect={() => { }}
                                        onView={(id) => router.push(`/employee/tasks/${id}`)}
                                        onEdit={() => { }}
                                        onDelete={() => { }}
                                        hideAssignedTo
                                    />
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>

            {/* Sort hint when search is active */}
            {isSearchActive && (
                <p style={{ marginTop: '10px', fontSize: '12px', color: '#52525b', textAlign: 'right' }}>
                    Sorting is disabled while search is active.
                </p>
            )}
        </div>
    );
}

function QuickStat({ label, value, color }: { label: string, value: number, color: string }) {
    return (
        <div style={{ backgroundColor: '#161616', border: '1px solid #1f1f1f', borderRadius: '12px', padding: '16px 20px', flex: '1 1 120px' }}>
            <div style={{ fontSize: '12px', color: '#71717a', marginBottom: '4px' }}>{label}</div>
            <div style={{ fontSize: '20px', fontWeight: 700, color }}>{value}</div>
        </div>
    );
}
