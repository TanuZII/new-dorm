CREATE TABLE app_users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(80) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(160) NOT NULL,
    email VARCHAR(160),
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_app_users_username UNIQUE (username)
);

CREATE TABLE tenants (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_code VARCHAR(40) NOT NULL,
    tenant_type VARCHAR(30) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    citizen_id VARCHAR(20),
    student_code VARCHAR(30),
    email VARCHAR(160),
    phone VARCHAR(30),
    address TEXT,
    emergency_contact VARCHAR(200),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_tenants_code UNIQUE (tenant_code)
);

CREATE TABLE rooms (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    building_code VARCHAR(20) NOT NULL,
    number VARCHAR(20) NOT NULL,
    floor INT NOT NULL,
    capacity INT NOT NULL,
    occupied_beds INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_room_building_number UNIQUE (building_code, number),
    CONSTRAINT ck_room_capacity CHECK (capacity > 0),
    CONSTRAINT ck_room_occupancy CHECK (occupied_beds >= 0 AND occupied_beds <= capacity)
);

CREATE TABLE occupancies (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    bed_number INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_occupancy_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_occupancy_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT uk_active_room_bed UNIQUE (room_id, bed_number, status)
);

CREATE TABLE contracts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    contract_number VARCHAR(40) NOT NULL,
    tenant_id BIGINT NOT NULL,
    occupancy_id BIGINT NOT NULL,
    rental_period VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    confirmed_by VARCHAR(80),
    confirmed_at TIMESTAMP NULL,
    document_path VARCHAR(500),
    document_hash CHAR(64),
    cancellation_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_contract_number UNIQUE (contract_number),
    CONSTRAINT fk_contract_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_contract_occupancy FOREIGN KEY (occupancy_id) REFERENCES occupancies(id)
);

CREATE TABLE charge_rates (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    charge_type VARCHAR(30) NOT NULL,
    tenant_type VARCHAR(30),
    rental_period VARCHAR(20),
    amount DECIMAL(12,2) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_charge_rate_amount CHECK (amount >= 0)
);

CREATE TABLE meter_readings (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL,
    meter_type VARCHAR(20) NOT NULL,
    reading_month DATE NOT NULL,
    previous_value DECIMAL(12,2) NOT NULL,
    current_value DECIMAL(12,2) NOT NULL,
    rate_per_unit DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meter_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT uk_meter_room_month UNIQUE (room_id, meter_type, reading_month),
    CONSTRAINT ck_meter_values CHECK (current_value >= previous_value)
);

CREATE TABLE invoices (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(40) NOT NULL,
    tenant_id BIGINT NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    void_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_invoice_number UNIQUE (invoice_number),
    CONSTRAINT fk_invoice_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT ck_invoice_total CHECK (total_amount > 0),
    CONSTRAINT ck_invoice_paid CHECK (paid_amount >= 0 AND paid_amount <= total_amount)
);

CREATE TABLE invoice_items (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    charge_type VARCHAR(30) NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity DECIMAL(12,2) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_invoice_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    payment_number VARCHAR(40) NOT NULL,
    invoice_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    proof_path VARCHAR(500),
    proof_hash CHAR(64),
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP NULL,
    rejection_reason VARCHAR(500),
    paid_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_payment_number UNIQUE (payment_number),
    CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    CONSTRAINT fk_payment_reviewer FOREIGN KEY (reviewed_by) REFERENCES app_users(id),
    CONSTRAINT ck_payment_amount CHECK (amount > 0)
);

CREATE TABLE receipts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    receipt_number VARCHAR(40) NOT NULL,
    payment_id BIGINT NOT NULL,
    issued_by BIGINT NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    document_path VARCHAR(500),
    document_hash CHAR(64),
    void_reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'ISSUED',
    CONSTRAINT uk_receipt_number UNIQUE (receipt_number),
    CONSTRAINT fk_receipt_payment FOREIGN KEY (payment_id) REFERENCES payments(id),
    CONSTRAINT fk_receipt_issuer FOREIGN KEY (issued_by) REFERENCES app_users(id)
);

CREATE TABLE deposits (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    received_amount DECIMAL(12,2) NOT NULL,
    deducted_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    refunded_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    refund_proof_path VARCHAR(500),
    CONSTRAINT fk_deposit_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id)
);

CREATE TABLE maintenance_requests (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    request_number VARCHAR(40) NOT NULL,
    room_id BIGINT NOT NULL,
    tenant_id BIGINT,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    assigned_to BIGINT,
    result_note TEXT,
    opened_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP NULL,
    CONSTRAINT uk_maintenance_number UNIQUE (request_number),
    CONSTRAINT fk_maintenance_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_maintenance_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    CONSTRAINT fk_maintenance_assignee FOREIGN KEY (assigned_to) REFERENCES app_users(id)
);

CREATE TABLE inventory_items (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(40) NOT NULL,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(30) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    quantity DECIMAL(12,2) NOT NULL DEFAULT 0,
    reorder_level DECIMAL(12,2) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_inventory_sku UNIQUE (sku),
    CONSTRAINT ck_inventory_quantity CHECK (quantity >= 0)
);

CREATE TABLE stock_movements (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    maintenance_request_id BIGINT,
    movement_type VARCHAR(20) NOT NULL,
    quantity DECIMAL(12,2) NOT NULL,
    note VARCHAR(500),
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_item FOREIGN KEY (item_id) REFERENCES inventory_items(id),
    CONSTRAINT fk_stock_maintenance FOREIGN KEY (maintenance_request_id) REFERENCES maintenance_requests(id),
    CONSTRAINT fk_stock_creator FOREIGN KEY (created_by) REFERENCES app_users(id)
);

CREATE TABLE announcements (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    audience_type VARCHAR(20) NOT NULL,
    audience_reference VARCHAR(80),
    starts_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NULL,
    comments_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_announcement_creator FOREIGN KEY (created_by) REFERENCES app_users(id)
);

CREATE TABLE audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    actor VARCHAR(80) NOT NULL,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id VARCHAR(80),
    reason VARCHAR(500),
    ip_address VARCHAR(64),
    trace_id VARCHAR(64),
    details JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_created_at ON audit_logs(created_at);
CREATE INDEX idx_invoice_tenant_status ON invoices(tenant_id, status);
CREATE INDEX idx_contract_end_status ON contracts(end_date, status);
CREATE INDEX idx_maintenance_status ON maintenance_requests(status);
