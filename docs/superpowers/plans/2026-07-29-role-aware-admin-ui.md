# Role-aware Administration UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver responsive, role-protected React administration pages for users, roles, audit history, effective-dated master data, and atomic Excel import.

**Architecture:** React Router renders feature pages inside the existing application shell. A session context and shared route metadata drive both authorization guards and visible navigation, while one API client standardizes cookie, CSRF, JSON-error, and download behavior.

**Tech Stack:** React 19.2, TypeScript 5.9, React Router, Vite 7, Tailwind CSS 4, Vitest, Testing Library

## Global Constraints

- Preserve the current navy/mint visual system, Noto Sans Thai body face, Chakra Petch display/data face, and responsive sidebar.
- Use `/api/v1` endpoints and `credentials: 'include'` for every request.
- Hide unauthorized menus and redirect unauthorized direct routes; do not render disabled admin controls.
- Thai is the primary UI language; API codes and hashes remain unchanged.
- Every task uses red-green-refactor and ends with tests, build verification, commit, and push.

---

### Task 1: Session context, API client, routing, and guarded shell

**Files:**
- Modify: `frontend/package.json`
- Modify: `frontend/src/main.tsx`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/auth/SessionGate.tsx`
- Create: `frontend/src/auth/SessionContext.tsx`
- Create: `frontend/src/auth/RoleRoute.tsx`
- Create: `frontend/src/api/client.ts`
- Create: `frontend/src/layout/AppShell.tsx`
- Test: `frontend/src/auth/RoleRoute.test.tsx`
- Test: `frontend/src/App.test.tsx`

**Interfaces:**
- Produces: `SessionProfile`, `useSession()`, `hasAnyRole(roles)`, `api.get/post/patch/put`, and guarded routes.
- Consumes: `GET /api/v1/auth/me`, `GET /api/v1/auth/csrf`, `POST /api/v1/auth/login`.

- [x] Write failing tests proving ADMIN sees administration navigation and DORM_STAFF does not.
- [x] Write a failing test proving direct `/admin/users` access redirects a non-admin to `/`.
- [x] Install `react-router-dom`, implement typed session context and the centralized API client.
- [x] Extract the existing shell/dashboard, define route metadata once, and implement `RoleRoute`.
- [x] Run `npm test` and `npm run build`; commit and push `feat: add role aware admin routing`.

### Task 2: Users and roles administration

**Files:**
- Create: `frontend/src/features/users/UsersPage.tsx`
- Create: `frontend/src/features/users/UsersPage.test.tsx`
- Create: `frontend/src/features/roles/RolesPage.tsx`
- Create: `frontend/src/features/roles/RolesPage.test.tsx`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/index.css`

**Interfaces:**
- Consumes: paged `GET /api/v1/users`, `POST /api/v1/users`, `PATCH /api/v1/users/{id}/status`, `POST /api/v1/users/{id}/reset-password`, `GET /api/v1/roles`, and role permission mutations.
- Produces: `/admin/users` and `/admin/roles` screens with search, paging, creation, status change, password reset, and permission editing.

- [x] Write failing component tests for results, empty/error states, and successful CSRF-backed mutations.
- [x] Implement typed user/role models and accessible forms with explicit confirmation for destructive status changes.
- [x] Implement desktop tables and mobile record cards using shared status styling.
- [x] Run targeted tests, full frontend tests, and production build; commit and push `feat: add user and role administration`.

### Task 3: Audit and master-data administration

**Files:**
- Create: `frontend/src/features/audit/AuditPage.tsx`
- Create: `frontend/src/features/audit/AuditPage.test.tsx`
- Create: `frontend/src/features/master-data/MasterDataPage.tsx`
- Create: `frontend/src/features/master-data/MasterDataPage.test.tsx`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/index.css`

**Interfaces:**
- Consumes: filtered/paged `GET /api/v1/audit-logs`, `GET/POST /api/v1/master-data/{type}`, `PUT /api/v1/master-data/{type}/{id}`, and `PATCH /api/v1/master-data/{type}/{id}/status`.
- Produces: `/admin/audit` read-only history and `/admin/master-data` effective-dated CRUD.

- [x] Write failing tests for audit filters and read-only presentation.
- [x] Write failing tests for master-data type switching, create/edit, deactivate reason, and `409 CONCURRENT_MODIFICATION` recovery.
- [x] Implement the pages with URL-backed filters and visible effective-date/status fields.
- [x] Run targeted tests, full frontend tests, and production build; commit and push `feat: add audit and master data administration`.

### Task 4: Atomic Excel import wizard

**Files:**
- Create: `frontend/src/features/imports/ImportWizard.tsx`
- Create: `frontend/src/features/imports/ImportWizard.test.tsx`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/index.css`

**Interfaces:**
- Consumes: multipart `POST /api/v1/imports/master-data/preview`, `POST /api/v1/imports/{token}/confirm`, and `GET /api/v1/imports/{token}/errors.xlsx`.
- Produces: `/admin/imports` select-review-confirm workflow.

- [x] Write a failing test proving XLSX selection previews totals and row errors.
- [x] Write failing tests proving invalid previews hide confirm, error workbook downloads, and valid previews confirm once.
- [x] Implement file constraints, progress/busy states, SHA-256 display, expiry display, error table, and reset action.
- [x] Run targeted tests, full frontend tests, production build, and backend regression tests; commit and push `feat: add master data import wizard`.

### Task 5: Responsive and acceptance verification

**Files:**
- Modify: `frontend/src/index.css`
- Modify: `README.md`
- Modify: `docs/tor-traceability-matrix.md`

**Interfaces:**
- Consumes: all Task 1–4 routes.
- Produces: responsive acceptance evidence and operator instructions.

- [x] Exercise every admin route at desktop and mobile widths and fix clipping, focus, overflow, and empty/error layouts.
- [x] Verify Chrome/Edge-compatible navigation, refresh-safe routes, keyboard focus, and reduced motion.
- [x] Document administration URLs, XLSX columns, 10 MB limit, preview expiry, and authorization behavior.
- [x] Run `npm test`, `npm run build`, `mvn -q clean test`, and `git diff --check`.
- [x] Commit and push `docs: add milestone 1 administration acceptance evidence`.
