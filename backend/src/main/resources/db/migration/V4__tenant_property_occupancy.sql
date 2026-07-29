CREATE TABLE tenant_code_sequences (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO tenant_code_sequences (id)
SELECT id FROM tenants;

ALTER TABLE tenants
    ADD COLUMN institutional_id VARCHAR(40),
    ADD COLUMN title_code VARCHAR(40),
    ADD COLUMN first_name_en VARCHAR(100),
    ADD COLUMN last_name_en VARCHAR(100),
    ADD COLUMN faculty_code VARCHAR(40),
    ADD COLUMN major_code VARCHAR(40),
    ADD COLUMN company_name VARCHAR(200),
    ADD COLUMN company_tax_id VARCHAR(40),
    ADD COLUMN company_document_path VARCHAR(500),
    ADD COLUMN company_document_hash CHAR(64),
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

UPDATE tenants
SET institutional_id = tenant_code
WHERE tenant_type IN ('STUDENT', 'STAFF', 'PERSONNEL');

UPDATE tenants SET tenant_type = 'PERSONNEL' WHERE tenant_type = 'STAFF';
UPDATE tenants SET tenant_code = CONCAT('TEN-', LPAD(id, 6, '0'));

CREATE UNIQUE INDEX uk_tenants_institutional_id ON tenants(institutional_id);

ALTER TABLE app_users ADD COLUMN tenant_id BIGINT;
ALTER TABLE app_users
    ADD CONSTRAINT uk_app_users_tenant UNIQUE (tenant_id),
    ADD CONSTRAINT fk_app_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id);

CREATE TABLE tenant_addresses (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    address_type VARCHAR(20) NOT NULL,
    address_line VARCHAR(500) NOT NULL,
    subdistrict_code VARCHAR(40),
    district_code VARCHAR(40),
    province_code VARCHAR(40),
    postal_code VARCHAR(10),
    country_code VARCHAR(40) NOT NULL DEFAULT 'TH',
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_tenant_addresses_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT uk_tenant_addresses_type UNIQUE (tenant_id, address_type),
    CONSTRAINT ck_tenant_addresses_type CHECK (address_type IN ('CURRENT', 'REGISTERED'))
);

CREATE TABLE tenant_contacts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    contact_type VARCHAR(20) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    relationship_name VARCHAR(100),
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(160),
    primary_contact BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_tenant_contacts_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT ck_tenant_contacts_type CHECK (contact_type IN ('GUARDIAN', 'EMERGENCY'))
);

CREATE INDEX idx_tenant_contacts_tenant_type ON tenant_contacts(tenant_id, contact_type);

CREATE TABLE buildings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    building_code VARCHAR(20) NOT NULL,
    name_th VARCHAR(200) NOT NULL,
    name_en VARCHAR(200),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_buildings_code UNIQUE (building_code)
);

INSERT INTO buildings (building_code, name_th)
SELECT DISTINCT building_code, building_code FROM rooms;

CREATE TABLE floors (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    building_id BIGINT NOT NULL,
    floor_number INT NOT NULL,
    floor_code VARCHAR(20) NOT NULL,
    name_th VARCHAR(200),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_floors_building FOREIGN KEY (building_id) REFERENCES buildings(id),
    CONSTRAINT uk_floors_building_number UNIQUE (building_id, floor_number),
    CONSTRAINT uk_floors_building_code UNIQUE (building_id, floor_code)
);

INSERT INTO floors (building_id, floor_number, floor_code, name_th)
SELECT b.id, r.floor, CONCAT('F', r.floor), CONCAT('ชั้น ', r.floor)
FROM rooms r
JOIN buildings b ON b.building_code = r.building_code
GROUP BY b.id, r.floor;

ALTER TABLE rooms
    ADD COLUMN floor_id BIGINT,
    ADD COLUMN name_th VARCHAR(200),
    ADD COLUMN name_en VARCHAR(200),
    ADD COLUMN readiness_reason VARCHAR(500),
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

UPDATE rooms r
JOIN buildings b ON b.building_code = r.building_code
JOIN floors f ON f.building_id = b.id AND f.floor_number = r.floor
SET r.floor_id = f.id,
    r.name_th = CONCAT('ห้อง ', r.number);

ALTER TABLE rooms
    ADD CONSTRAINT fk_rooms_floor FOREIGN KEY (floor_id) REFERENCES floors(id);

CREATE TABLE beds (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    bed_code VARCHAR(40) NOT NULL,
    bed_number INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    status_reason VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_beds_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT uk_beds_room_number UNIQUE (room_id, bed_number),
    CONSTRAINT uk_beds_code UNIQUE (bed_code),
    CONSTRAINT ck_beds_number CHECK (bed_number > 0),
    CONSTRAINT ck_beds_status CHECK (status IN ('AVAILABLE', 'RESERVED', 'OCCUPIED', 'DAMAGED', 'INACTIVE'))
);

INSERT INTO beds (room_id, bed_code, bed_number)
WITH RECURSIVE numbers (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM numbers WHERE n < 100
)
SELECT r.id, CONCAT(r.building_code, '-', r.number, '-B', numbers.n), numbers.n
FROM rooms r
JOIN numbers ON numbers.n <= r.capacity;

CREATE TABLE room_meters (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    meter_type VARCHAR(20) NOT NULL,
    serial_number VARCHAR(80) NOT NULL,
    installed_on DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_meters_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT uk_room_meters_serial UNIQUE (serial_number),
    CONSTRAINT uk_room_meters_type_active UNIQUE (room_id, meter_type, active),
    CONSTRAINT ck_room_meters_type CHECK (meter_type IN ('WATER', 'ELECTRIC'))
);

CREATE TABLE reservations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    allocation_scope VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    cancellation_reason VARCHAR(500),
    created_by VARCHAR(80) NOT NULL,
    confirmed_by VARCHAR(80),
    confirmed_at TIMESTAMP NULL,
    cancelled_by VARCHAR(80),
    cancelled_at TIMESTAMP NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservations_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_reservations_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT ck_reservations_scope CHECK (allocation_scope IN ('BED', 'ROOM')),
    CONSTRAINT ck_reservations_dates CHECK (end_date >= start_date),
    CONSTRAINT ck_reservations_status CHECK (status IN ('DRAFT', 'CONFIRMED', 'CHECKED_IN', 'CANCELLED', 'EXPIRED'))
);

CREATE TABLE reservation_beds (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    bed_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservation_beds_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id),
    CONSTRAINT fk_reservation_beds_bed FOREIGN KEY (bed_id) REFERENCES beds(id),
    CONSTRAINT uk_reservation_beds UNIQUE (reservation_id, bed_id),
    CONSTRAINT ck_reservation_beds_dates CHECK (end_date >= start_date)
);

ALTER TABLE occupancies
    ADD COLUMN reservation_id BIGINT,
    ADD COLUMN expected_end_date DATE,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    ADD CONSTRAINT fk_occupancies_reservation FOREIGN KEY (reservation_id) REFERENCES reservations(id);

UPDATE occupancies o
LEFT JOIN contracts c ON c.occupancy_id = o.id
SET o.expected_end_date = COALESCE(o.end_date, c.end_date, DATE_ADD(o.start_date, INTERVAL 1 YEAR));

CREATE TABLE occupancy_beds (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    occupancy_id BIGINT NOT NULL,
    bed_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    released_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_occupancy_beds_occupancy FOREIGN KEY (occupancy_id) REFERENCES occupancies(id),
    CONSTRAINT fk_occupancy_beds_bed FOREIGN KEY (bed_id) REFERENCES beds(id),
    CONSTRAINT uk_occupancy_beds UNIQUE (occupancy_id, bed_id, start_date),
    CONSTRAINT ck_occupancy_beds_dates CHECK (end_date >= start_date)
);

INSERT INTO occupancy_beds (occupancy_id, bed_id, start_date, end_date, active)
SELECT o.id, b.id, o.start_date, o.expected_end_date,
       CASE WHEN o.status IN ('CHECKED_OUT', 'CANCELLED') THEN FALSE ELSE TRUE END
FROM occupancies o
JOIN beds b ON b.room_id = o.room_id AND b.bed_number = o.bed_number;

CREATE TABLE bed_allocation_days (
    bed_id BIGINT NOT NULL,
    allocation_date DATE NOT NULL,
    reservation_bed_id BIGINT,
    occupancy_bed_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (bed_id, allocation_date),
    CONSTRAINT fk_bed_allocation_days_bed
        FOREIGN KEY (bed_id) REFERENCES beds(id),
    CONSTRAINT fk_bed_allocation_days_reservation_bed
        FOREIGN KEY (reservation_bed_id) REFERENCES reservation_beds(id) ON DELETE CASCADE,
    CONSTRAINT fk_bed_allocation_days_occupancy_bed
        FOREIGN KEY (occupancy_bed_id) REFERENCES occupancy_beds(id) ON DELETE CASCADE,
    CONSTRAINT ck_bed_allocation_days_source CHECK (
        (reservation_bed_id IS NOT NULL) + (occupancy_bed_id IS NOT NULL) = 1
    )
);

INSERT INTO bed_allocation_days (bed_id, allocation_date, occupancy_bed_id)
WITH RECURSIVE allocation_dates AS (
    SELECT id AS occupancy_bed_id, bed_id, start_date AS allocation_date, end_date
    FROM occupancy_beds
    WHERE active = TRUE
    UNION ALL
    SELECT occupancy_bed_id, bed_id, DATE_ADD(allocation_date, INTERVAL 1 DAY), end_date
    FROM allocation_dates
    WHERE allocation_date < end_date
)
SELECT bed_id, allocation_date, occupancy_bed_id
FROM allocation_dates;

CREATE TABLE occupancy_events (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    occupancy_id BIGINT NOT NULL,
    event_type VARCHAR(30) NOT NULL,
    previous_room_id BIGINT,
    new_room_id BIGINT,
    reason VARCHAR(500),
    actor VARCHAR(80) NOT NULL,
    trace_id VARCHAR(64),
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_occupancy_events_occupancy FOREIGN KEY (occupancy_id) REFERENCES occupancies(id),
    CONSTRAINT fk_occupancy_events_previous_room FOREIGN KEY (previous_room_id) REFERENCES rooms(id),
    CONSTRAINT fk_occupancy_events_new_room FOREIGN KEY (new_room_id) REFERENCES rooms(id),
    CONSTRAINT ck_occupancy_events_type CHECK (event_type IN ('CHECKED_IN', 'TRANSFERRED', 'CHECKOUT_REQUESTED', 'CHECKED_OUT', 'CANCELLED'))
);

CREATE INDEX idx_tenants_type_active ON tenants(tenant_type, active);
CREATE INDEX idx_rooms_floor_status ON rooms(floor_id, status, active);
CREATE INDEX idx_beds_room_status ON beds(room_id, status, active);
CREATE INDEX idx_reservations_tenant_status ON reservations(tenant_id, status);
CREATE INDEX idx_reservation_beds_overlap ON reservation_beds(bed_id, active, start_date, end_date);
CREATE INDEX idx_occupancy_beds_overlap ON occupancy_beds(bed_id, active, start_date, end_date);
CREATE INDEX idx_occupancy_events_stay_time ON occupancy_events(occupancy_id, occurred_at);
