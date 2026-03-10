# Step 10 — NFR-USE-04 + NFR-USE-02: Accessibility

**NFR IDs:** NFR-USE-04 (Touch Targets), NFR-USE-02 (WCAG 2.1 AA)
**Effort:** ~2 hrs | **Dependencies:** None
**Commit:** `feat(frontend): add touch targets + accessibility (NFR-USE-04, NFR-USE-02)`

---

## SRS Requirements

> **NFR-USE-04:** Minimum touch target: 44×44px.
> **NFR-USE-02:** WCAG 2.1 Level AA accessibility compliance.

## Current State

No CSS rules for minimum touch targets. No accessibility audit performed. No `aria-*` attributes beyond basic button labels in layouts.

## Implementation

### Modify: `globals.css`

Add global touch target rules and accessibility utilities:

```css
/* ── Touch Targets (NFR-USE-04) ────────────────── */
button,
a,
[role="button"],
input[type="checkbox"],
input[type="radio"],
select {
    min-height: 44px;
    min-width: 44px;
}

/* Exceptions for inline text links */
p a, span a, li a {
    min-height: auto;
    min-width: auto;
}

/* ── Focus Visibility (WCAG 2.4.7) ────────────── */
:focus-visible {
    outline: 2px solid #f97316;
    outline-offset: 2px;
}

/* ── Skip to Content (WCAG 2.4.1) ──────────────── */
.skip-to-content {
    position: absolute;
    top: -100%;
    left: 0;
    z-index: 9999;
    padding: 12px 24px;
    background: #f97316;
    color: white;
    font-weight: 600;
    text-decoration: none;
}
.skip-to-content:focus {
    top: 0;
}

/* ── Reduced Motion Preference ─────────────────── */
@media (prefers-reduced-motion: reduce) {
    *, *::before, *::after {
        animation-duration: 0.01ms !important;
        transition-duration: 0.01ms !important;
    }
}
```

### Modify: Layout files (admin + employee)

Add "Skip to Content" link and `main` landmark:

```tsx
{/* Add at top of layout, before sidebar */}
<a href="#main-content" className="skip-to-content">
    Skip to content
</a>

{/* Add id to main tag */}
<main id="main-content" className="flex-1 p-4 md:p-7">
    {children}
</main>
```

### Accessibility Checklist for Components

| Area | Check | Action |
|------|-------|--------|
| Sidebar nav | `role="navigation"` | Add `<nav aria-label="Main navigation">` |
| Notification bell | `aria-label` | Add `aria-label="Notifications"` |
| Close buttons | `aria-label` | Already present ✅ |
| Form inputs | `<label>` association | Ensure all inputs have `id` + `<label htmlFor>` |
| Images | `alt` text | Ensure all `<img>` have descriptive `alt` |
| Color contrast | 4.5:1 ratio | Verify text colors against dark background |
| Modal dialogs | Focus trap | Ensure focus stays within modal |
| Toast messages | `aria-live="polite"` | Sonner toasts handle this automatically ✅ |

## Verification

```bash
# Install accessibility testing tool
cd taskhive-frontend
npm install -D axe-core @axe-core/react

# Or use browser extensions:
# - Chrome: axe DevTools, Lighthouse Accessibility audit
# - Run Lighthouse audit → Accessibility score should be ≥ 90
```
