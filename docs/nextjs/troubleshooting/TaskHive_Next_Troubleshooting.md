# TaskHive Next.js — Development Troubleshooting Reference

**Digiwork | `taskhive-frontend` | ML Integration & Core Features**  
Next.js 14+ · TailwindCSS · Zustand · Lucide Icons

> This document tracks real-world errors, state management pitfalls, and UI/UX bugs encountered during the development of the TaskHive Next.js frontend.

---

## How to Read This Document

Each issue contains three fields:

- **Symptom** — The error message, build failure, or broken UI behavior.
- **Root Cause** — The technical reason why the issue occurred.
- **Resolution** — The code change or logic update that fixed it.

---

# 1. ML Service & AI Integration

---

## 1.1 State Management & Hooks

### Issue 1.1.1 — Priority Dropdown "Locked" to AI Suggestion

**Symptom:** After clicking "Accept Suggestion" for an AI-suggested priority (e.g., HIGH), the user attempts to manually change it back to LOW, but the field instantly snaps back to HIGH.

**Root Cause:** A `useEffect` hook in `TaskForm.tsx` was synchronized with `initialPriority`. This effect included `formData.priority` in its dependency array. Every time the user manually changed the priority, the effect re-ran, saw that `initialPriority` was still set (and different from the new manual value), and overwrote the local state again.

**Resolution:** Removed `formData.priority` from the `useEffect` dependencies so the suggestion only applies once. Additionally, updated the parent page logic to clear the "accepted" flag whenever a manual field update is detected.

---

### Issue 1.1.2 — Stale AI Suggestion State

**Symptom:** After creating a task and resetting the form, the previous AI suggestion badge remained visible, or the "Accept" button didn't trigger correctly for a second attempt.

**Root Cause:** The `prediction` state from the ML hook was not being reset upon successful task submission or form clearing.

**Resolution:** Added a `resetML()` call to the form's success handler to clear out hooks and UI badges.

---

## 1.2 Build & Lint Errors

### Issue 1.2.1 — Duplicate Component Imports

**Symptom:** Build fails with `Duplicate identifier 'DatePicker'`.

**Root Cause:** During manual merge or rapid refactoring, the `DatePicker` component was imported twice at the top of `TaskForm.tsx`.

**Resolution:** Removed the redundant import line.

---

### Issue 1.2.2 — ReferenceError: useMlStore is not defined

**Symptom:** Page crashes on load or build fails with "cannot find name 'useMlStore'".

**Root Cause:** The component was updated to respect the ML Global Toggle, but the import for the Zustand store was missing from the file.

**Resolution:** Added `import { useMlStore } from '@/features/ml/store/mlStore';` to all affected pages and components.

---

## 1.3 UI & Role Visibility

### Issue 1.3.1 — AI Sections Visible When ML Feature is Toggled OFF

**Symptom:** Even if the administrator disables the ML feature, "AI Productivity Insights" and "Workload Recommendations" remain visible in the UI (showing empty or loading states).

**Root Cause:** Components were missing conditional rendering logic based on the `isMlEnabled` state from the global store.

**Resolution:** Wrapped all ML-powered components and buttons in conditional blocks:
```tsx
const { isMlEnabled } = useMlStore();
// ...
{isMlEnabled && <WorkloadRecommendationComponent ... />}
```

---

### Issue 1.3.2 — ML Toggle Visible to Employees

**Symptom:** Employees can see the ML Global Toggle in their sidebar or header, allowing them to disable features for the entire system (or their session).

**Root Cause:** The `MlFeatureToggle` component was included in the common `EmployeeLayout.tsx`.

**Resolution:** Removed the toggle from the employee layout. The ML feature flag management is restricted to the Admin layout only.

---

# Quick Reference: Common Fixes

| Symptom | Primary Solution |
|---------|------------------|
| Dropdown value keeps resetting | Check `useEffect` dependency arrays for aggressive state syncing. |
| Button doesn't appear after toggle | Verify `useMlStore` hook is initialized and `isMlEnabled` is checked. |
| "Module not found" for store | Ensure path uses the `@/` alias (e.g., `@/features/ml/store/mlStore`). |
| Duplicate key errors in lists | Ensure `key={item.id}` is unique, especially when dealing with AI-generated mock data. |

---

> 🚀 **Tip:** Always run `npm run build` locally after making state management changes to catch subtle hook dependency issues.
