# Role-aware administration UI design

## Scope

Build the first complete administration workspace over the existing identity, audit, master-data, and Excel-import APIs. The UI keeps the current navy/mint dorm-operations identity and responsive sidebar. It adds real routes, explicit loading/empty/error states, and hides unauthorized navigation and routes rather than merely disabling them.

## Chosen approach

Use React Router with a session context. This gives refresh-safe deep links and one route guard shared by all administration pages. A custom History API router would avoid a dependency but would duplicate matching, navigation, and test behavior; a tab-only screen would not satisfy the required URLs.

## Architecture

- `SessionProvider` owns the authenticated profile and login state. `useSession` exposes `profile`, `hasRole`, and session refresh behavior.
- `RoleRoute` checks roles before rendering protected content and redirects authenticated but unauthorized users to `/`.
- `AppShell` owns the existing responsive sidebar/topbar and renders route content through an outlet.
- `api/client.ts` centralizes JSON errors, cookie credentials, CSRF acquisition for mutations, and file downloads.
- Feature pages own their query state and API-specific forms. Shared table/status/page components remain presentation-only.

## Routes and authorization

| Route | Page | Allowed roles |
|---|---|---|
| `/` | Operations dashboard | authenticated staff roles |
| `/admin/users` | Users | `ADMIN` |
| `/admin/roles` | Roles and permissions | `ADMIN` |
| `/admin/audit` | Audit history | `ADMIN` |
| `/admin/master-data` | Effective-dated master data | `ADMIN` |
| `/admin/imports` | Excel import wizard | `ADMIN` |

The sidebar derives its visible entries from the same route metadata used by guards, preventing menu/route authorization drift.

## Interaction design

Administration pages use a dense but calm operations-desk layout: page title and primary action, compact filter strip, responsive data table, status chips, and contextual empty/error guidance. The signature element is an “operations rail” above each table that visibly joins filters, result count, and current system state like a dormitory floor plan. On mobile, tables become labelled record cards and the sidebar remains an overlay.

The import wizard has three explicit stages: select XLSX, review preview totals and row errors, then confirm. Invalid previews never expose the confirm action. Error workbooks download from the preview token.

## Error handling and accessibility

- API errors use the backend `message` and field errors when available; generic errors state the recovery action.
- Every async screen has loading, empty, error, and retry states.
- Dialogs/forms use labels, status messages use `role="status"` or `role="alert"`, keyboard focus is visible, and reduced motion remains respected.
- Thai is the primary interface language; codes, hashes, and document identifiers use the utility typeface.

## Testing

- Session tests prove roles from `/auth/me` reach children.
- Route tests prove non-admin menus are absent and direct admin URLs redirect.
- Feature tests mock fetch and verify table rendering, filters, mutations with CSRF, import preview, invalid confirm suppression, confirmation, and error-workbook download.
- Completion requires `npm test`, `npm run build`, and backend regression tests because the production artifact embeds `frontend/dist`.
