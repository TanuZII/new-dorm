# Tenant, Property, Reservation and Occupancy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver a tested vertical milestone for tenant registration, normalized dormitory property, bed/room reservations, check-in, transfer, occupancy history, and checkout request.

**Architecture:** Flyway V3 migrates the legacy room/occupancy schema into explicit building, floor, bed, allocation, and event tables without deleting existing data. Transactional Spring services own state transitions and overlap protection; React renders four role-aware workspaces over typed `/api/v1` contracts.

**Tech Stack:** Java 21, Spring Boot 4.1, Spring Security, Spring Data JPA, Flyway, MySQL 8.4, React 19.2, TypeScript 5.9, Vite 7, Tailwind CSS 4, JUnit 5, Testcontainers, Vitest, Testing Library

## Global Constraints

- Preserve all existing data and primary keys; never reset `dbdorm`.
- Generate `tenantCode` as `TEN-%06d`; callers never submit it.
- Institutional identifiers are unique when present and required for `STUDENT`/`PERSONNEL`.
- REST prefix is `/api/v1`; lists support page/filter/sort and size remains capped at 200.
- Use constructor injection, request records with Jakarta validation, DTO responses, and transactional services.
- Use shared error envelope `code`, `message`, `fieldErrors`, `timestamp`, `traceId`.
- Every mutation writes audit data; cancellation, deactivate, transfer, and checkout require a reason.
- Every production behavior follows RED → verify failure → GREEN → full regression → commit → push.
- Secrets and environment-specific values never enter source control.

---

### Task 1: Flyway V3 normalized milestone schema

**Files:**
- Create: `backend/src/main/resources/db/migration/V3__tenant_property_occupancy.sql`
- Create: `backend/src/test/java/th/ac/dusit/dorm/platform/MilestoneTwoMigrationContractTest.java`
- Modify: `backend/src/test/java/th/ac/dusit/dorm/platform/MySqlMigrationIntegrationTest.java`
- Modify: `docs/data-dictionary.md`

**Interfaces:**
- Produces tables `tenant_code_sequences`, `tenant_addresses`, `tenant_contacts`, `buildings`, `floors`, `beds`, `room_meters`, `reservations`, `reservation_beds`, `occupancy_beds`, `occupancy_events`.
- Adds `app_users.tenant_id`, tenant detail/version columns, `rooms.floor_id`, and occupancy version/source/expected-end columns.
- Produces MySQL overlap triggers `trg_reservation_beds_no_overlap_i/u` and `trg_occupancy_beds_no_overlap_i/u`.

- [ ] **Step 1: Write the failing migration contract test**

```java
@Test
void v3DefinesNormalizedMilestoneAndOverlapGuards() throws Exception {
    String sql = Files.readString(Path.of("src/main/resources/db/migration/V3__tenant_property_occupancy.sql"));
    assertThat(sql).contains("CREATE TABLE buildings", "CREATE TABLE floors", "CREATE TABLE beds");
    assertThat(sql).contains("CREATE TABLE reservations", "CREATE TABLE reservation_beds");
    assertThat(sql).contains("CREATE TRIGGER trg_reservation_beds_no_overlap_i");
    assertThat(sql).contains("CREATE TRIGGER trg_occupancy_beds_no_overlap_i");
}
```

- [ ] **Step 2: Run RED**

Run: `mvn -q -Dtest=MilestoneTwoMigrationContractTest test`

Expected: FAIL because V3 does not exist.

- [ ] **Step 3: Add V3 migration**

Use explicit foreign keys, checks, unique constraints, indexes, and legacy backfill. Backfill one building per legacy `rooms.building_code`, one floor per `(building,floor)`, one bed per legacy capacity, and link legacy occupancy by `bed_number`. Trigger overlap predicate is inclusive:

```sql
existing.start_date <= NEW.end_date
AND existing.end_date >= NEW.start_date
```

Raise `SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'BED_NOT_AVAILABLE'`.

- [ ] **Step 4: Extend the MySQL 8.4 migration test**

Assert V3 migration success, migrated tables, `app_users.tenant_id`, and all four triggers through `information_schema.TRIGGERS`.

- [ ] **Step 5: Run GREEN and migration regression**

Run: `mvn -q -Dtest=MilestoneTwoMigrationContractTest,MySqlMigrationIntegrationTest test`

Run: `mvn -q clean test`

- [ ] **Step 6: Update data dictionary, verify, commit, and push**

Run: `git diff --check`

Commit: `feat: add tenant property occupancy schema`

Push: `git push origin main`

---

### Task 2: Complete tenant aggregate and secured API

**Files:**
- Create: `backend/src/main/java/th/ac/dusit/dorm/common/DomainConflictException.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/tenant/TenantType.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/tenant/TenantCodeGenerator.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/tenant/UpdateTenantRequest.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/tenant/ChangeTenantStatusRequest.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/tenant/TenantSpecifications.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/tenant/persistence/TenantAddressEntity.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/tenant/persistence/TenantContactEntity.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/common/GlobalExceptionHandler.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/tenant/CreateTenantRequest.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/tenant/TenantResponse.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/tenant/TenantController.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/tenant/TenantService.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/tenant/persistence/TenantEntity.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/tenant/persistence/TenantRepository.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/identity/AppUserEntity.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/identity/CreateUserRequest.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/identity/UserService.java`
- Test: `backend/src/test/java/th/ac/dusit/dorm/tenant/TenantServiceTest.java`
- Create: `backend/src/test/java/th/ac/dusit/dorm/tenant/TenantControllerTest.java`

**Interfaces:**
- `TenantCodeGenerator.nextCode(): String` returns `TEN-000001` style values from `tenant_code_sequences`.
- `TenantService.search(String query, TenantType type, Boolean active, Pageable pageable): Page<TenantResponse>`.
- Produces `GET/POST /api/v1/tenants`, `GET/PUT /api/v1/tenants/{id}`, `PATCH /api/v1/tenants/{id}/status`.
- Existing `POST /api/v1/users` accepts optional `tenantId`; only role `TENANT` may be linked, and one profile/user pair is unique.

- [ ] **Step 1: Replace legacy tenant tests with failing desired behavior**

Tests prove generated tenant code, required institutional ID for student/personnel, duplicate identifier mapping to `TENANT_IDENTIFIER_DUPLICATE`, nested address/contact persistence, update version, linked tenant account uniqueness, and deactivate reason audit.

```java
assertThat(service.create(studentRequest).tenantCode()).isEqualTo("TEN-000001");
assertThatThrownBy(() -> service.create(duplicateInstitutionalId))
    .isInstanceOf(DomainConflictException.class)
    .extracting("code").isEqualTo("TENANT_IDENTIFIER_DUPLICATE");
```

- [ ] **Step 2: Run RED**

Run: `mvn -q -Dtest=TenantServiceTest,TenantControllerTest test`

Expected: compilation/test failures for missing DTOs, generated code, filters, and endpoints.

- [ ] **Step 3: Implement the tenant aggregate minimally**

Use `@Version`, orphan-managed address/contact children, normalized identifiers, and a database-backed sequence insert. `DomainConflictException` carries an exact code and message; map it to HTTP 409 in `GlobalExceptionHandler`.

```java
public final class DomainConflictException extends RuntimeException {
    private final String code;
    public DomainConflictException(String code, String message) { super(message); this.code = code; }
    public String code() { return code; }
}

public String nextCode() {
    Number id = insert.executeAndReturnKey(Map.of());
    return "TEN-%06d".formatted(id.longValue());
}
```

- [ ] **Step 4: Implement role-secured controller contracts**

`ADMIN`/`DORM_STAFF` mutate, `ADMIN`/`DORM_STAFF`/`FINANCE` read. Every request body uses `@Valid`. Do not add DELETE.

- [ ] **Step 5: Run GREEN and full backend regression**

Run: `mvn -q -Dtest=TenantServiceTest,TenantControllerTest test`

Run: `mvn -q clean test`

- [ ] **Step 6: Verify, commit, and push**

Run: `git diff --check`

Commit: `feat: add complete tenant registry api`

Push: `git push origin main`

---

### Task 3: Property hierarchy, readiness, meters, and availability API

**Files:**
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/persistence/BuildingEntity.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/persistence/FloorEntity.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/persistence/BedEntity.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/persistence/RoomMeterEntity.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/persistence/BuildingRepository.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/persistence/FloorRepository.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/persistence/BedRepository.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/persistence/RoomMeterRepository.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/PropertyRequests.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/PropertyResponses.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/PropertyHierarchyService.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/AvailabilityQuery.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/AvailabilityResponse.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/AvailabilityService.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/BedAvailabilityPolicy.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/property/persistence/RoomEntity.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/property/RoomController.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/property/RoomService.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/BuildingController.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/FloorController.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/BedController.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/property/RoomMeterController.java`
- Create: `backend/src/test/java/th/ac/dusit/dorm/property/PropertyHierarchyServiceTest.java`
- Create: `backend/src/test/java/th/ac/dusit/dorm/property/PropertyControllerTest.java`
- Create: `backend/src/test/java/th/ac/dusit/dorm/property/AvailabilityRepositoryTest.java`

**Interfaces:**
- `AvailabilityService.search(AvailabilityQuery query, Pageable pageable): Page<AvailabilityResponse>`.
- Produces CRUD/status endpoints documented in the design and `GET /api/v1/beds/availability`.
- Capacity is derived from active beds; room status cannot be directly set to conflict with allocation state.

- [ ] **Step 1: Write failing hierarchy and availability tests**

```java
assertThat(service.createRoom(requestWithTwoBeds).capacity()).isEqualTo(2);
assertThat(availability.search(queryForAvailableBeds, pageable).getContent())
    .extracting(AvailabilityResponse::bedCode).containsExactly("P1-201-B1");
```

Also prove damaged/inactive rooms and beds are excluded, duplicate codes are rejected, meter type/serial is unique, and role access matches the design.

- [ ] **Step 2: Run RED**

Run: `mvn -q -Dtest=PropertyHierarchyServiceTest,PropertyControllerTest,AvailabilityRepositoryTest test`

- [ ] **Step 3: Implement focused entities/services/controllers**

Keep one public service per aggregate. Lock status mutation rules inside services. Use specifications/projections for availability instead of loading full graphs.

```java
@Service
@Transactional(readOnly = true)
public class AvailabilityService {
    public Page<AvailabilityResponse> search(AvailabilityQuery query, Pageable pageable) {
        return bedRepository.findAvailable(query, pageable);
    }
}
```

- [ ] **Step 4: Run GREEN and full regression**

Run targeted tests, then `mvn -q clean test`.

- [ ] **Step 5: Verify, commit, and push**

Commit: `feat: add property hierarchy and availability`

Push after `git diff --check`.

---

### Task 4: Transactional reservation workflow and overlap protection

**Files:**
- Create: `backend/src/main/java/th/ac/dusit/dorm/reservation/persistence/ReservationEntity.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/reservation/persistence/ReservationBedEntity.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/reservation/persistence/ReservationRepository.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/reservation/persistence/ReservationBedRepository.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/reservation/CreateReservationRequest.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/reservation/ReservationActionRequest.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/reservation/ReservationResponse.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/reservation/ReservationStatus.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/reservation/AllocationScope.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/reservation/ReservationService.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/reservation/ReservationController.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/reservation/ReservationExpiryJob.java`
- Create: `backend/src/test/java/th/ac/dusit/dorm/reservation/ReservationStateTest.java`
- Create: `backend/src/test/java/th/ac/dusit/dorm/reservation/ReservationServiceTest.java`
- Create: `backend/src/test/java/th/ac/dusit/dorm/reservation/ReservationControllerTest.java`
- Create: `backend/src/test/java/th/ac/dusit/dorm/reservation/ConcurrentReservationIntegrationTest.java`
- Create: `backend/src/test/java/th/ac/dusit/dorm/reservation/ReservationExpiryJobTest.java`

**Interfaces:**
- `ReservationService.create(CreateReservationRequest, String actor): ReservationResponse` creates `DRAFT`.
- `confirm(long id, long version, String actor): ReservationResponse` locks beds in ascending ID order and creates allocations.
- `cancel(long id, long version, String reason, String actor): ReservationResponse` requires a reason.
- Produces list/detail/create/confirm/cancel endpoints.

- [ ] **Step 1: Write failing state tests**

Prove only `DRAFT → CONFIRMED → CHECKED_IN`, cancellation from draft/confirmed, and expiry from confirmed. The scheduled expiry job uses the configured check-in deadline and is idempotent. Any other transition throws `INVALID_STATE_TRANSITION`.

- [ ] **Step 2: Write failing service/controller tests**

Prove bed scope allocates one bed, room scope allocates every active room bed, room readiness is rechecked, duplicate confirm is idempotent only for the same version/state, and cancellation writes the reason.

- [ ] **Step 3: Run RED**

Run: `mvn -q -Dtest=ReservationStateTest,ReservationServiceTest,ReservationControllerTest test`

- [ ] **Step 4: Implement minimal workflow**

Map SQLSTATE/message `BED_NOT_AVAILABLE` to `DomainConflictException("BED_NOT_AVAILABLE", ...)`. Keep availability display advisory; confirm is authoritative.

```java
@Transactional
public ReservationResponse confirm(long id, long version, String actor) {
    ReservationEntity reservation = requireVersion(repository.findByIdForUpdate(id), version);
    List<BedEntity> beds = bedRepository.lockAllByIdOrder(resolveBedIds(reservation));
    availabilityPolicy.requireAvailable(beds, reservation.getStartDate(), reservation.getEndDate());
    reservation.confirm(actor, clock.instant());
    allocationRepository.saveAll(toAllocations(reservation, beds));
    auditService.record(actor, "RESERVATION_CONFIRMED", "RESERVATION",
            String.valueOf(id), null, null, Map.of("version", reservation.getVersion()));
    return ReservationResponse.from(reservation);
}
```

- [ ] **Step 5: Prove concurrency on MySQL 8.4**

Start two transactions with a barrier and assert exactly one confirm succeeds for the same bed/date range. Also prove non-overlapping ranges both succeed.

- [ ] **Step 6: Run full regression, commit, and push**

Commit: `feat: add transactional reservation workflow`

---

### Task 5: Check-in, transfer, checkout request, history, and `/my/*`

**Files:**
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/persistence/OccupancyEntity.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/persistence/OccupancyBedEntity.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/persistence/OccupancyEventEntity.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/persistence/OccupancyRepository.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/persistence/OccupancyBedRepository.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/persistence/OccupancyEventRepository.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/CheckInRequest.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/TransferOccupancyRequest.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/CheckoutRequest.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/OccupancyResponse.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/OccupancyEventResponse.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/OccupancyService.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/OccupancyController.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/MyTenantController.java`
- Create: `backend/src/main/java/th/ac/dusit/dorm/occupancy/AuthenticatedTenantResolver.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/identity/AppUserEntity.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/reservation/ReservationController.java`
- Modify: `backend/src/main/java/th/ac/dusit/dorm/reservation/ReservationService.java`
- Create: `backend/src/test/java/th/ac/dusit/dorm/occupancy/OccupancyServiceTest.java`
- Create: `backend/src/test/java/th/ac/dusit/dorm/occupancy/OccupancyControllerTest.java`
- Create: `backend/src/test/java/th/ac/dusit/dorm/occupancy/AtomicTransferIntegrationTest.java`
- Create: `backend/src/test/java/th/ac/dusit/dorm/occupancy/MyTenantIdorTest.java`

**Interfaces:**
- Check-in is `POST /api/v1/reservations/{id}/check-in` and converts reservation allocations to occupancy allocations atomically.
- `OccupancyService.transfer(long id, TransferOccupancyRequest, String actor)` closes old allocations, opens new allocations, appends `TRANSFERRED`, and leaves current state `CHECKED_IN`.
- `requestCheckout` changes state to `CHECKOUT_PENDING` and appends reason.
- `/api/v1/my/*` resolves tenant only from the authenticated username relation.

- [ ] **Step 1: Write failing check-in/transfer/state tests**

Prove confirmed-only check-in, no duplicate active stay, atomic transfer rollback when any destination bed conflicts, append-only history, and checkout reason.

- [ ] **Step 2: Write failing IDOR tests**

Create two tenant users and assert tenant A receives only A's profile/reservation/occupancy; no `/my/*` endpoint accepts `tenantId`.

- [ ] **Step 3: Run RED, implement minimally, then run GREEN**

Run targeted tests before and after implementation. Use `@Transactional` on every multi-table mutation and pessimistic bed locking in sorted order.

```java
@Transactional
public OccupancyResponse transfer(long id, TransferOccupancyRequest request, String actor) {
    OccupancyEntity stay = requireVersion(repository.findByIdForUpdate(id), request.version());
    List<BedEntity> destination = bedRepository.lockAllByIdOrder(request.bedIds());
    availabilityPolicy.requireAvailable(destination, request.transferDate(), stay.getExpectedEndDate());
    allocationRepository.closeCurrent(stay.getId(), request.transferDate().minusDays(1));
    allocationRepository.saveAll(toDestinationAllocations(stay, destination, request.transferDate()));
    eventRepository.save(OccupancyEventEntity.transferred(stay, request.reason(), actor));
    return OccupancyResponse.from(stay);
}
```

- [ ] **Step 4: Run MySQL atomic-transfer test and full backend suite**

Run: `mvn -q clean test`

- [ ] **Step 5: Verify, commit, and push**

Commit: `feat: add occupancy lifecycle and tenant self service`

---

### Task 6: Tenant Registry React workspace

**Files:**
- Create: `frontend/src/features/tenants/tenantTypes.ts`
- Create: `frontend/src/features/tenants/TenantsPage.tsx`
- Create: `frontend/src/features/tenants/TenantFormDialog.tsx`
- Create: `frontend/src/features/tenants/TenantDetailPanel.tsx`
- Create tests beside each component.
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/layout/AppShell.tsx`
- Modify: `frontend/src/index.css`

**Interfaces:**
- Produces `/tenants`, allowed for `ADMIN`, `DORM_STAFF`, `FINANCE`; mutation controls only for `ADMIN`, `DORM_STAFF`.
- Consumes tenant APIs from Task 2 through the shared `api` client.

- [ ] **Step 1: Write failing route/list/form/conflict tests**

Prove role navigation, search parameters, generated code display, nested address/contact fields, no tenant-code input, mobile-card class, error/empty states, and 409 refresh behavior.

- [ ] **Step 2: Run RED**

Run: `npm test -- --run src/features/tenants`

- [ ] **Step 3: Implement the workspace**

Keep API types separate from form state. Use semantic dialog/regions and explicit labels. Preserve entered values after validation/server failures.

```tsx
<Route element={<RoleRoute roles={['ADMIN', 'DORM_STAFF', 'FINANCE']} />}>
  <Route path="/tenants" element={<TenantsPage />} />
</Route>
```

- [ ] **Step 4: Run GREEN, full tests, and build**

Run: `npm test`

Run: `npm run build`

- [ ] **Step 5: Run backend regression because WAR embeds frontend**

Run: `mvn -q clean test`

- [ ] **Step 6: Verify, commit, and push**

Commit: `feat: add tenant registry workspace`

---

### Task 7: Property Map React workspace

**Files:**
- Create: `frontend/src/features/properties/propertyTypes.ts`
- Create: `frontend/src/features/properties/PropertiesPage.tsx`
- Create: `frontend/src/features/properties/PropertyFilters.tsx`
- Create: `frontend/src/features/properties/RoomDetailPanel.tsx`
- Create: `frontend/src/features/properties/PropertyDialog.tsx`
- Create tests beside each component.
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/layout/AppShell.tsx`
- Modify: `frontend/src/index.css`

**Interfaces:**
- Produces `/properties` for `ADMIN`, `DORM_STAFF`, `MAINTENANCE`.
- Consumes hierarchy, status, meter, and availability APIs from Task 3.

- [ ] **Step 1: Write failing hierarchy/filter/status/mobile tests**

Prove building/floor/date filters, room detail bed/meter rendering, mutation controls hidden for maintenance, readiness reason display, and no horizontal overflow contract.

- [ ] **Step 2: Run RED, implement, and run GREEN**

Run targeted tests after each RED/GREEN cycle.

```tsx
<Route element={<RoleRoute roles={['ADMIN', 'DORM_STAFF', 'MAINTENANCE']} />}>
  <Route path="/properties" element={<PropertiesPage />} />
</Route>
```

- [ ] **Step 3: Run full tests/build/backend regression**

Run `npm test`, `npm run build`, and `mvn -q clean test`.

- [ ] **Step 4: Verify, commit, and push**

Commit: `feat: add property availability workspace`

---

### Task 8: Reservation and Occupancy React workspaces

**Files:**
- Create: `frontend/src/features/reservations/reservationTypes.ts`
- Create: `frontend/src/features/reservations/ReservationsPage.tsx`
- Create: `frontend/src/features/reservations/ReservationWizard.tsx`
- Create: `frontend/src/features/reservations/ReservationReview.tsx`
- Create: `frontend/src/features/reservations/ReservationsPage.test.tsx`
- Create: `frontend/src/features/reservations/ReservationWizard.test.tsx`
- Create: `frontend/src/features/occupancies/occupancyTypes.ts`
- Create: `frontend/src/features/occupancies/OccupanciesPage.tsx`
- Create: `frontend/src/features/occupancies/TransferDialog.tsx`
- Create: `frontend/src/features/occupancies/CheckoutRequestDialog.tsx`
- Create: `frontend/src/features/occupancies/OccupancyTimeline.tsx`
- Create: `frontend/src/features/occupancies/OccupanciesPage.test.tsx`
- Create: `frontend/src/features/occupancies/OccupancyActions.test.tsx`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/layout/AppShell.tsx`
- Modify: `frontend/src/index.css`

**Interfaces:**
- Produces `/reservations` and `/occupancies` for `ADMIN`, `DORM_STAFF`; finance may read occupancy without actions.
- Consumes Tasks 4–5 APIs.

- [ ] **Step 1: Write failing reservation wizard tests**

Prove steps cannot advance without tenant/dates/scope/allocation, invalid room readiness blocks confirmation, confirm re-fetches availability on `BED_NOT_AVAILABLE`, and success cannot submit twice.

- [ ] **Step 2: Write failing occupancy tests**

Prove only legal actions are shown, transfer requires destination/reason/version, checkout request requires reason, and timeline is read-only.

- [ ] **Step 3: Run RED, implement, and run GREEN**

Use shared page states and status chips; do not duplicate the API client.

```tsx
<Route element={<RoleRoute roles={['ADMIN', 'DORM_STAFF']} />}>
  <Route path="/reservations" element={<ReservationsPage />} />
</Route>
<Route element={<RoleRoute roles={['ADMIN', 'DORM_STAFF', 'FINANCE']} />}>
  <Route path="/occupancies" element={<OccupanciesPage />} />
</Route>
```

- [ ] **Step 4: Run full verification**

Run `npm test`, `npm run build`, `mvn -q clean test`, and `git diff --check`.

- [ ] **Step 5: Commit and push**

Commit: `feat: add reservation and occupancy workspaces`

---

### Task 9: Milestone 2 browser acceptance and documentation

**Files:**
- Create: `docs/uat/milestone-2.md`
- Modify: `README.md`
- Modify: `docs/data-dictionary.md`
- Modify: `docs/tor-traceability.md`
- Modify: this plan to mark completed evidence.

**Interfaces:**
- Produces a reproducible runbook and TOR/UAT evidence.

- [ ] **Step 1: Run automated verification fresh**

Run `npm test`, `npm run build`, `npm audit --omit=dev`, `mvn -q clean test`, and `mvn -q -DskipTests package`.

- [ ] **Step 2: Browser acceptance**

Test `/tenants`, `/properties`, `/reservations`, and `/occupancies` on desktop and 390 × 844. Verify navigation, dialogs, empty/error/conflict states, focus, and zero horizontal overflow.

- [ ] **Step 3: Staging business smoke**

Create one tenant, property hierarchy, bed reservation, check-in, transfer, and checkout request. Verify overlap rejection, audit events, and `/my/*` isolation using two tenant accounts.

- [ ] **Step 4: Document evidence and traceability**

Record exact test counts, zero failures, browser widths, API paths, migration version, and any infrastructure prerequisite. Do not record real credentials or personal data.

- [ ] **Step 5: Final verification, commit, and push**

Run `git diff --check` and a credential scan.

Commit: `docs: complete milestone two acceptance evidence`

Push: `git push origin main`

## Execution Choice

The user requested completion in the current task, so use **Inline Execution** with a verification and commit/push checkpoint after every task. Do not dispatch subagents.
