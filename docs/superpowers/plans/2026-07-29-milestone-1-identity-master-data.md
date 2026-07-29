# Milestone 1 Identity, RBAC และ Master Data Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** ทำให้บัญชีภายใน สิทธิ์แบบ role/permission ข้อมูลตั้งต้น และ Excel import พร้อมใช้งานจริงและตรวจสอบย้อนหลังได้

**Architecture:** ต่อยอด modular monolith เดิมด้วยตาราง RBAC แบบ many-to-many, catalog service ที่แยกขอบเขตชัดเจน และ import session แบบ preview/confirm การเขียนหลายรายการอยู่ใน transaction เดียว ส่วน React ใช้ session profile เป็นแหล่ง route/menu authorization

**Tech Stack:** Java 21, Spring Boot 4.1, Spring Security, Spring Data JPA, Flyway, MySQL 8.4, Apache POI, React 19.2, TypeScript, Vitest

## Global Constraints

- REST API prefix `/api/v1`; pagination/filter/sort และขนาดหน้าสูงสุด 200
- Roles คงหกค่า: `ADMIN`, `DORM_STAFF`, `FINANCE`, `APPROVER`, `MAINTENANCE`, `TENANT`
- Password ใช้ BCrypt cost 12; secrets มาจาก environment variables
- Error envelope ต้องมี `code`, `message`, `fieldErrors`, `timestamp`, `traceId`
- การ deactivate และแก้สิทธิ์ต้องมีเหตุผลและ audit
- Excel import ต้อง validate ทั้งไฟล์และห้าม partial commit
- ทุก task ใช้ RED → GREEN → full verification → commit → push `origin/main`

---

### Task 1: Password self-service และ pagination cap

**Files:**
- Create: `backend/src/main/java/th/ac/dusit/dorm/identity/ChangePasswordRequest.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/identity/AuthController.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/identity/UserService.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/platform/WebPaginationConfiguration.java`
- Test: `backend/src/test/java/th/ac/dusit/dorm/identity/ChangePasswordIntegrationTest.java`
- Test: `backend/src/test/java/th/ac/dusit/dorm/platform/PaginationIntegrationTest.java`

**Interfaces:**
- Produces: `POST /api/v1/auth/change-password`
- Produces: `UserService.changeOwnPassword(String username, ChangePasswordRequest request, String ipAddress)`

- [ ] Write a failing integration test proving an authenticated user must supply the correct current password and the new hash allows the next login.
- [ ] Write a failing MVC test proving `size=500` is capped to `DormProperties.maxPageSize()`.
- [ ] Run `mvn -q "-Dtest=ChangePasswordIntegrationTest,PaginationIntegrationTest" test` and confirm failures are the missing behaviors.
- [ ] Implement `ChangePasswordRequest` with `currentPassword`, `newPassword` and confirmation validation; encode only after `PasswordEncoder.matches` succeeds.
- [ ] Add a `PageableHandlerMethodArgumentResolverCustomizer` bean using `properties.maxPageSize()`.
- [ ] Re-run targeted tests, then `mvn -q clean test`.
- [ ] Commit and push with message `feat: add password self-service and pagination limit`.

### Task 2: Database-backed RBAC

**Files:**
- Create: `backend/src/main/resources/db/migration/V2__identity_rbac_master_data.sql`
- Create: `backend/src/main/java/th/ac/dusit/dorm/identity/RoleEntity.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/identity/PermissionEntity.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/identity/RoleRepository.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/identity/RoleService.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/identity/RoleController.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/identity/AppUserEntity.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/identity/DatabaseUserDetailsService.java`
- Test: `backend/src/test/java/th/ac/dusit/dorm/identity/RolePermissionIntegrationTest.java`

**Interfaces:**
- Produces: `GET /api/v1/roles`
- Produces: `PUT /api/v1/roles/{roleCode}/permissions` with `{ "permissions": [...], "reason": "..." }`
- Produces authorities named `PERM_<RESOURCE>_<ACTION>` in addition to `ROLE_*`

- [ ] Write a failing MySQL integration test proving V2 seeds six roles, unique permission codes, and the admin assignment.
- [ ] Write a failing service test proving a user receives authorities from every assigned role.
- [ ] Run targeted tests and confirm schema/authority failures.
- [ ] Add V2 tables `roles`, `permissions`, `role_permissions`, `user_roles`, `master_data_items`, `import_sessions`, and `import_errors` with foreign keys and unique constraints.
- [ ] Seed the six fixed roles and permissions for users, master data, tenant, property, finance, approval, maintenance, reports, and self-service.
- [ ] Implement role list and atomic permission replacement with reason and audit.
- [ ] Preserve `app_users.role` as primary role during migration and backfill `user_roles` from it.
- [ ] Run MySQL migration and full backend tests.
- [ ] Commit and push with message `feat: add database backed role permissions`.

### Task 3: Audit history API

**Files:**
- Create: `backend/src/main/java/th/ac/dusit/dorm/audit/AuditLogEntity.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/audit/AuditLogRepository.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/audit/AuditLogResponse.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/audit/AuditLogService.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/audit/AuditLogController.java`
- Test: `backend/src/test/java/th/ac/dusit/dorm/audit/AuditLogControllerTest.java`

**Interfaces:**
- Produces: `GET /api/v1/audit-logs?actor=&action=&entityType=&from=&to=&page=&size=&sort=`

- [ ] Write failing MVC tests for ADMIN access, non-admin denial, filters, and page response.
- [ ] Write a failing repository test for inclusive `from` and exclusive `to` boundaries.
- [ ] Implement read-only entity/repository/specification/service/controller; expose no mutation endpoint.
- [ ] Run targeted and full backend tests.
- [ ] Commit and push with message `feat: expose secured audit history`.

### Task 4: Effective-dated master data API

**Files:**
- Create package: `backend/src/main/java/th/ac/dusit/dorm/masterdata/`
- Create: `backend/src/test/java/th/ac/dusit/dorm/masterdata/MasterDataServiceTest.java`
- Create: `backend/src/test/java/th/ac/dusit/dorm/masterdata/MasterDataControllerTest.java`

**Interfaces:**
- Produces: `GET/POST /api/v1/master-data/{type}`
- Produces: `PUT /api/v1/master-data/{type}/{id}`
- Produces: `PATCH /api/v1/master-data/{type}/{id}/status`
- Supported types: `TITLE`, `COUNTRY`, `PROVINCE`, `DISTRICT`, `SUBDISTRICT`, `POSTAL_CODE`, `TENANT_TYPE`, `ACADEMIC_YEAR`, `RENTAL_PERIOD`, `CONTRACT_TYPE`, `FACULTY`, `MAJOR`, `FEE_TYPE`

- [ ] Write failing service tests for normalized unique codes, overlapping effective dates, parent-child geography, and deactivate reason.
- [ ] Write failing MVC tests for pagination/filter/sort and ADMIN-only mutations.
- [ ] Implement entity with `@Version`, effective dates, optional `parentId`, active flag and deactivate reason.
- [ ] Map optimistic lock exceptions to `409 CONCURRENT_MODIFICATION` in the common error envelope.
- [ ] Seed TOR-required types/codes in V2 without hard-coding rates in Java.
- [ ] Run targeted tests and MySQL-backed full suite.
- [ ] Commit and push with message `feat: add effective dated master data`.

### Task 5: Atomic Excel import backend

**Files:**
- Modify: `backend/pom.xml` to add `org.apache.poi:poi-ooxml`
- Create package: `backend/src/main/java/th/ac/dusit/dorm/imports/`
- Test: `backend/src/test/java/th/ac/dusit/dorm/imports/MasterDataImportIntegrationTest.java`

**Interfaces:**
- Produces: `POST /api/v1/imports/master-data/preview` multipart XLSX
- Produces: `POST /api/v1/imports/{token}/confirm`
- Produces: `GET /api/v1/imports/{token}/errors.xlsx`
- Preview response: token, SHA-256, totalRows, validRows, invalidRows, row errors, expiresAt

- [ ] Build XLSX fixtures in test memory and write a failing test proving preview performs zero business-table inserts.
- [ ] Write a failing integration test with one valid and one invalid row proving confirm inserts zero rows.
- [ ] Write a failing test proving a changed file hash or expired token cannot confirm.
- [ ] Implement magic-byte/MIME/10 MB checks, streaming row parsing, normalization and validators.
- [ ] Persist preview metadata/errors; confirm revalidates hash and all rows inside one transaction.
- [ ] Generate an XLSX error workbook with row number, field, rejected value and message.
- [ ] Run targeted, MySQL integration and full backend tests.
- [ ] Commit and push with message `feat: add atomic master data excel import`.

### Task 6: Role-aware React administration UI

**Files:**
- Modify: `frontend/src/auth/SessionGate.tsx`
- Modify: `frontend/src/App.tsx`
- Create: `frontend/src/auth/RoleRoute.tsx`
- Create: `frontend/src/features/users/UsersPage.tsx`
- Create: `frontend/src/features/master-data/MasterDataPage.tsx`
- Create: `frontend/src/features/imports/ImportWizard.tsx`
- Create tests beside each component

**Interfaces:**
- Consumes: auth profile, users, roles, audit logs, master data and import endpoints from Tasks 1–5
- Produces: routes `/admin/users`, `/admin/roles`, `/admin/audit`, `/admin/master-data`, `/admin/imports`

- [ ] Write failing component tests proving unauthorized menus/routes are absent, not merely disabled.
- [ ] Write failing tests for user status/reset forms requiring reason and import preview blocking confirm when errors exist.
- [ ] Implement route metadata with allowed roles and render navigation from the same metadata.
- [ ] Implement accessible loading, empty, error, pagination and confirmation states with no dead-end controls.
- [ ] Run `npm test` and `npm run build`.
- [ ] Run backend tests because the executable JAR embeds `frontend/dist`.
- [ ] Commit and push with message `feat: add role aware administration workspace`.

### Task 7: Milestone 1 acceptance and documentation

**Files:**
- Modify: `README.md`
- Modify: `docs/data-dictionary.md`
- Modify: `docs/tor-traceability.md`
- Create: `docs/uat/milestone-1.md`

**Interfaces:**
- Produces reproducible runbook and UAT evidence for Milestone 1

- [ ] Run `mvn -q clean test` with Docker available and record total tests/zero failures.
- [ ] Run `npm test` and `npm run build`; package with `mvn -q -DskipTests package`.
- [ ] Smoke test health, login, user CRUD, permission enforcement, master CRUD, import rollback and audit search.
- [ ] Update data dictionary and traceability links to API/test/UAT evidence.
- [ ] Confirm `git diff --check` and no credentials occur outside tests/examples.
- [ ] Commit and push with message `docs: complete milestone one acceptance evidence`.
