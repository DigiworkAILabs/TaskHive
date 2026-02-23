'use client';

import React, { useState, useRef, useEffect } from 'react';
import { Calendar as CalendarIcon, ChevronLeft, ChevronRight } from 'lucide-react';

interface DatePickerProps {
    value: string; // YYYY-MM-DD
    onChange: (date: string) => void;
    label: string;
    id: string;
    placeholder?: string;
    error?: string;
    required?: boolean;
}

export const DatePicker: React.FC<DatePickerProps> = ({
    value,
    onChange,
    label,
    id,
    placeholder = 'Select date',
    error,
    required,
}) => {
    const [isOpen, setIsOpen] = useState(false);

    // Parse value YYYY/MM/DD or YYYY-MM-DD to Date object safely
    const parseValue = (val: string) => {
        if (!val) return new Date();
        const separator = val.includes('-') ? '-' : '/';
        const parts = val.split(separator);
        if (parts.length === 3) {
            return new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]));
        }
        return new Date(val); // Fallback
    };

    const [viewDate, setViewDate] = useState(parseValue(value));
    const containerRef = useRef<HTMLDivElement>(null);

    // Close on click outside
    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    // Sync viewDate when value changes externally
    useEffect(() => {
        if (value) {
            setViewDate(parseValue(value));
        }
    }, [value]);

    const daysInMonth = (year: number, month: number) => new Date(year, month + 1, 0).getDate();
    const firstDayOfMonth = (year: number, month: number) => new Date(year, month, 1).getDay();

    const formatDate = (date: Date) => {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    };

    // Validation: FROM Today UNTIL end of next month
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const endOfNextMonth = new Date(today.getFullYear(), today.getMonth() + 2, 0);
    endOfNextMonth.setHours(23, 59, 59, 999);

    const isDateDisabled = (year: number, month: number, day: number) => {
        const date = new Date(year, month, day);
        return date < today || date > endOfNextMonth;
    };

    const handleDateClick = (day: number) => {
        if (isDateDisabled(viewDate.getFullYear(), viewDate.getMonth(), day)) return;

        const selectedDate = new Date(viewDate.getFullYear(), viewDate.getMonth(), day);
        onChange(formatDate(selectedDate));
        setIsOpen(false);
    };

    const nextMonth = () => {
        setViewDate(new Date(viewDate.getFullYear(), viewDate.getMonth() + 1, 1));
    };

    const prevMonth = () => {
        setViewDate(new Date(viewDate.getFullYear(), viewDate.getMonth() - 1, 1));
    };

    const handleMonthChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setViewDate(new Date(viewDate.getFullYear(), parseInt(e.target.value), 1));
    };

    const handleYearChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setViewDate(new Date(parseInt(e.target.value), viewDate.getMonth(), 1));
    };

    const renderCalendar = () => {
        const year = viewDate.getFullYear();
        const month = viewDate.getMonth();
        const totalDays = daysInMonth(year, month);
        const firstDay = firstDayOfMonth(year, month);
        const days = [];

        // Empty slots for previous month's days
        for (let i = 0; i < firstDay; i++) {
            days.push(<div key={`empty-${i}`} style={{ width: '32px', height: '32px' }} />);
        }

        // Days of current month
        for (let d = 1; d <= totalDays; d++) {
            const dateObj = new Date(year, month, d);
            const dateStr = formatDate(dateObj);
            const isSelected = value === dateStr;
            const isToday = formatDate(new Date()) === dateStr;
            const disabled = isDateDisabled(year, month, d);

            days.push(
                <div
                    key={d}
                    onClick={() => !disabled && handleDateClick(d)}
                    style={{
                        width: '32px',
                        height: '32px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '13px',
                        cursor: disabled ? 'not-allowed' : 'pointer',
                        borderRadius: '6px',
                        backgroundColor: isSelected ? '#f97316' : 'transparent',
                        color: isSelected ? '#ffffff' : disabled ? '#3f3f46' : isToday ? '#f97316' : '#d4d4d8',
                        transition: 'background-color 0.2s',
                        opacity: disabled ? 0.4 : 1,
                    }}
                    onMouseEnter={(e) => !isSelected && !disabled && (e.currentTarget.style.backgroundColor = '#2a2a2a')}
                    onMouseLeave={(e) => !isSelected && !disabled && (e.currentTarget.style.backgroundColor = 'transparent')}
                >
                    {d}
                </div>
            );
        }

        return days;
    };

    const monthNames = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];

    const currentYearNum = new Date().getFullYear();
    // Only current and future years (2026, 2027, 2028...)
    const years = Array.from({ length: 11 }, (_, i) => currentYearNum + i);

    const selectStyle: React.CSSProperties = {
        backgroundColor: '#111111',
        border: '1px solid #2a2a2a',
        borderRadius: '6px',
        color: '#ffffff',
        fontSize: '12px',
        padding: '2px 4px',
        outline: 'none',
        cursor: 'pointer',
    };

    return (
        <div style={{ position: 'relative', width: '100%' }} ref={containerRef}>
            <label
                htmlFor={id}
                style={{ display: 'block', fontSize: '13px', color: '#a1a1aa', marginBottom: '6px', fontWeight: 500 }}
            >
                {label} {required && <span style={{ color: '#f97316' }}>*</span>}
            </label>

            <div
                id={id}
                onClick={() => setIsOpen(!isOpen)}
                style={{
                    width: '100%',
                    backgroundColor: '#111111',
                    border: `1px solid ${error ? '#ef4444' : isOpen ? '#f97316' : '#2a2a2a'}`,
                    borderRadius: '10px',
                    color: value ? '#ffffff' : '#71717a',
                    fontSize: '14px',
                    padding: '12px 16px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    boxShadow: isOpen ? '0 0 0 3px rgba(249,115,22,0.12)' : 'none',
                    transition: 'border-color 0.2s, box-shadow 0.2s',
                }}
            >
                {value ? value : placeholder}
                <CalendarIcon size={16} color="#71717a" />
            </div>

            {error && (
                <p style={{ color: '#ef4444', fontSize: '12px', marginTop: '4px' }}>{error}</p>
            )}

            {isOpen && (
                <div
                    style={{
                        position: 'absolute',
                        top: 'calc(100% + 8px)',
                        left: 0,
                        zIndex: 50,
                        backgroundColor: '#161616',
                        border: '1px solid #2a2a2a',
                        borderRadius: '12px',
                        padding: '16px',
                        boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.4), 0 8px 10px -6px rgba(0, 0, 0, 0.4)',
                        width: '280px',
                    }}
                >
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px', gap: '4px' }}>
                        <button
                            type="button"
                            onClick={prevMonth}
                            style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#a1a1aa', display: 'flex', padding: 4 }}
                            onMouseEnter={(e) => (e.currentTarget.style.color = '#ffffff')}
                            onMouseLeave={(e) => (e.currentTarget.style.color = '#a1a1aa')}
                        >
                            <ChevronLeft size={16} />
                        </button>

                        <div style={{ display: 'flex', gap: '4px', flex: 1, justifyContent: 'center' }}>
                            <select
                                value={viewDate.getMonth()}
                                onChange={handleMonthChange}
                                style={selectStyle}
                            >
                                {monthNames.map((name, i) => (
                                    <option key={name} value={i}>{name}</option>
                                ))}
                            </select>
                            <select
                                value={viewDate.getFullYear()}
                                onChange={handleYearChange}
                                style={selectStyle}
                            >
                                {years.map(year => (
                                    <option key={year} value={year}>{year}</option>
                                ))}
                            </select>
                        </div>

                        <button
                            type="button"
                            onClick={nextMonth}
                            style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#a1a1aa', display: 'flex', padding: 4 }}
                            onMouseEnter={(e) => (e.currentTarget.style.color = '#ffffff')}
                            onMouseLeave={(e) => (e.currentTarget.style.color = '#a1a1aa')}
                        >
                            <ChevronRight size={16} />
                        </button>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '4px', textAlign: 'center', marginBottom: '8px' }}>
                        {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map(day => (
                            <span key={day} style={{ fontSize: '11px', fontWeight: 500, color: '#71717a' }}>{day}</span>
                        ))}
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '4px' }}>
                        {renderCalendar()}
                    </div>
                </div>
            )}
        </div>
    );
};
