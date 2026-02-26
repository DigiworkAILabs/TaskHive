import { AuditSearchParams } from "../types/audit.types";

/**
 * formatAuditDate
 * Formats ISO datetime to: "25 Feb 2026, 04:30 PM"
 */
export function formatAuditDate(isoString: string): string {
  if (!isoString) return "—";
  const date = new Date(isoString);

  const dateStr = date.toLocaleDateString("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  });

  const timeStr = date.toLocaleTimeString("en-IN", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: true,
  });

  return `${dateStr}, ${timeStr}`;
}

/**
 * getActionColor
 * Returns hex color based on action keywords
 */
export function getActionColor(action: string): string {
  const upperAction = action.toUpperCase();
  if (upperAction.includes("LOGIN") || upperAction.includes("LOGOUT")) {
    return "#3b82f6"; // Info blue
  }
  if (upperAction.includes("CREATED") || upperAction.includes("ACTIVATED")) {
    return "#22c55e"; // Success green
  }
  if (upperAction.includes("DELETED") || upperAction.includes("DEACTIVATED")) {
    return "#ef4444"; // Danger red
  }
  if (
    upperAction.includes("UPDATED") ||
    upperAction.includes("CHANGED") ||
    upperAction.includes("ASSIGNED") ||
    upperAction.includes("ADDED") ||
    upperAction.includes("RESET")
  ) {
    return "#f97316"; // Accent orange
  }
  return "#a3a3a3"; // Text muted
}

/**
 * getSecurityEventColor
 */
export function getSecurityEventColor(eventType: string): string {
  switch (eventType) {
    case "LOGIN_FAILED":
      return "#f97316";
    case "ACCOUNT_LOCKED":
      return "#ef4444";
    case "UNAUTHORIZED_ACCESS":
      return "#eab308";
    default:
      return "#a3a3a3";
  }
}

/**
 * truncateUuid
 */
export function truncateUuid(uuid: string, chars = 8): string {
  if (!uuid) return "—";
  if (uuid.length <= chars) return uuid;
  return uuid.substring(0, chars) + "...";
}

/**
 * truncateJson
 */
export function truncateJson(obj: Record<string, unknown> | null, maxChars = 80): string {
  if (!obj || Object.keys(obj).length === 0) return "—";
  try {
    const jsonStr = JSON.stringify(obj);
    if (jsonStr.length <= maxChars) return jsonStr;
    return jsonStr.substring(0, maxChars) + "...";
  } catch {
    return "—";
  }
}

/**
 * hasAnySearchParam
 */
export function hasAnySearchParam(params: AuditSearchParams): boolean {
  return Object.values(params).some((val) => !!val);
}

/**
 * COMPLIANCE_REPORT_LABELS
 */
export const COMPLIANCE_REPORT_LABELS = {
  USER_ACCESS: {
    label: "User Access Report",
    description: "Login events, role assignments, and account activations",
  },
  DATA_MODIFICATION: {
    label: "Data Modification Report",
    description: "All entity create, update, and delete events",
  },
  SECURITY_INCIDENT: {
    label: "Security Incident Report",
    description: "Failed logins, account lockouts, and unauthorized access attempts",
  },
} as const;
