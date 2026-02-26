import React from "react";
import { cn } from "@/shared/utils/cn";

// Badge
export const Badge = ({
  children,
  className,
  style,
}: {
  children: React.ReactNode;
  className?: string;
  style?: React.CSSProperties;
}) => (
  <span
    className={cn(
      "inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border",
      className
    )}
    style={style}
  >
    {children}
  </span>
);

// Skeleton
export const Skeleton = ({ className }: { className?: string }) => (
  <div className={cn("animate-pulse bg-[#2a2a2a] rounded", className)} />
);

// Table Components
export const Table = ({ children, className }: { children: React.ReactNode; className?: string }) => (
  <table className={cn("w-full border-collapse", className)}>{children}</table>
);

export const TableHeader = ({ children }: { children: React.ReactNode }) => (
  <thead className="border-bottom border-[#1f1f1f]">{children}</thead>
);

export const TableBody = ({ children }: { children: React.ReactNode }) => (
  <tbody className="divide-y divide-[#1f1f1f]">{children}</tbody>
);

export const TableRow = ({
  children,
  className,
  onClick,
  style,
}: {
  children: React.ReactNode;
  className?: string;
  onClick?: () => void;
  style?: React.CSSProperties;
}) => (
  <tr
    className={cn("border-b border-[#1f1f1f]", className)}
    onClick={onClick}
    style={style}
  >
    {children}
  </tr>
);

export const TableHead = ({ children, className }: { children: React.ReactNode; className?: string }) => (
  <th
    className={cn(
      "px-4 py-3 text-left text-xs font-semibold text-[#f97316] uppercase tracking-wider",
      className
    )}
  >
    {children}
  </th>
);

export const TableCell = ({
  children,
  className,
  colSpan,
}: {
  children: React.ReactNode;
  className?: string;
  colSpan?: number;
}) => (
  <td className={cn("px-4 py-4 text-sm whitespace-nowrap", className)} colSpan={colSpan}>
    {children}
  </td>
);

// Select (Simple version since proper Shadcn Select is complex)
export const SimpleSelect = ({
  value,
  onChange,
  options,
  placeholder,
  label,
}: {
  value: string;
  onChange: (val: string) => void;
  options: { value: string; label: string }[];
  placeholder: string;
  label?: string;
}) => (
  <div className="flex flex-col gap-1.5">
    {label && <label className="text-xs font-medium text-[#a3a3a3] ml-1">{label}</label>}
    <select
      value={value}
      onChange={(e) => onChange(e.target.value)}
      className="bg-[#111111] border border-[#2a2a2a] rounded-lg text-[#d4d4d8] text-sm p-2.5 outline-none cursor-pointer appearance-none bg-[url('data:image/svg+xml;charset=utf-8,%3Csvg%20width%3D%2210%22%20height%3D%226%22%20fill%3D%22none%22%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%3E%3Cpath%20d%3D%22M1%201l4%204%204-4%22%20stroke%3D%22%2371717a%22%20stroke-width%3D%221.5%22%20stroke-linecap%3D%22round%22%20stroke-linejoin%3D%22round%22%2F%3E%3C%2Fsvg%3E')] bg-no-repeat bg-[position:right_12px_center] pr-10"
    >
      <option value="">{placeholder}</option>
      {options.map((opt) => (
        <option key={opt.value} value={opt.value}>
          {opt.label}
        </option>
      ))}
    </select>
  </div>
);
