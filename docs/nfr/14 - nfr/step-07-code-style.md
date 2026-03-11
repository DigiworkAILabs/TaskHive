# Step 7 — NFR-MAIN-03: Code Style Enforcement

**NFR IDs:** NFR-MAIN-03
**Effort:** ~45 min | **Dependencies:** None
**Commit:** `feat(quality): add Checkstyle + ESLint configs (NFR-MAIN-03)`

---

## SRS Requirement

> Backend code style enforced via Checkstyle; frontend via ESLint.

## Current State

- **Backend:** No `checkstyle.xml`, no `maven-checkstyle-plugin`
- **Frontend:** Next.js default ESLint only, no custom rules

## Implementation

### Backend — Checkstyle

#### Modify: `pom.xml`

Add in `<build><plugins>`:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
    <configuration>
        <configLocation>checkstyle.xml</configLocation>
        <consoleOutput>true</consoleOutput>
        <failsOnError>false</failsOnError>
        <violationSeverity>warning</violationSeverity>
    </configuration>
</plugin>
```

#### New File: `checkstyle.xml`

**Path:** `taskhive-backend/checkstyle.xml`

Create a Checkstyle config based on Google Java Style with project-specific tweaks:
- Max line length: 120 chars (not Google's 100)
- Allow `*` imports for domain packages
- Enforce Javadoc on public service methods
- Require `@Override` annotation

### Frontend — ESLint

#### New File: `.eslintrc.json`

**Path:** `taskhive-frontend/.eslintrc.json`

```json
{
  "extends": [
    "next/core-web-vitals",
    "next/typescript"
  ],
  "rules": {
    "react/no-unescaped-entities": "warn",
    "@typescript-eslint/no-unused-vars": ["warn", { "argsIgnorePattern": "^_" }],
    "@typescript-eslint/no-explicit-any": "warn",
    "prefer-const": "error",
    "no-console": ["warn", { "allow": ["warn", "error"] }]
  }
}
```

#### Modify: `package.json`

Ensure lint script exists:

```json
{
  "scripts": {
    "lint": "next lint",
    "lint:fix": "next lint --fix"
  }
}
```

## Verification

```bash
# Backend — run Checkstyle
cd taskhive-backend
mvn checkstyle:check

# Frontend — run ESLint
cd taskhive-frontend
npm run lint
```
