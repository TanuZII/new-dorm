CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL,
    name_th VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_roles_code UNIQUE (code)
);

CREATE TABLE permissions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(80) NOT NULL,
    resource_name VARCHAR(40) NOT NULL,
    action_name VARCHAR(30) NOT NULL,
    description VARCHAR(500),
    CONSTRAINT uk_permissions_code UNIQUE (code)
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    granted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id),
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id)
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES app_users(id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE master_data_items (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    data_type VARCHAR(40) NOT NULL,
    item_code VARCHAR(40) NOT NULL,
    name_th VARCHAR(200) NOT NULL,
    name_en VARCHAR(200),
    parent_id BIGINT,
    effective_from DATE NOT NULL,
    effective_to DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deactivation_reason VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_master_data_type_code UNIQUE (data_type, item_code),
    CONSTRAINT fk_master_data_parent FOREIGN KEY (parent_id) REFERENCES master_data_items(id),
    CONSTRAINT ck_master_data_dates CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE TABLE import_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    token CHAR(36) NOT NULL,
    import_type VARCHAR(40) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(500) NOT NULL,
    sha256 CHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_rows INT NOT NULL DEFAULT 0,
    valid_rows INT NOT NULL DEFAULT 0,
    invalid_rows INT NOT NULL DEFAULT 0,
    created_by VARCHAR(80) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    confirmed_at TIMESTAMP NULL,
    CONSTRAINT uk_import_sessions_token UNIQUE (token),
    CONSTRAINT ck_import_row_counts CHECK (
        total_rows >= 0 AND valid_rows >= 0 AND invalid_rows >= 0
        AND valid_rows + invalid_rows = total_rows)
);

CREATE TABLE import_errors (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    import_session_id BIGINT NOT NULL,
    source_row_number INT NOT NULL,
    field_name VARCHAR(80) NOT NULL,
    rejected_value VARCHAR(500),
    error_code VARCHAR(80) NOT NULL,
    message VARCHAR(500) NOT NULL,
    CONSTRAINT fk_import_errors_session FOREIGN KEY (import_session_id) REFERENCES import_sessions(id)
);

INSERT INTO roles (code, name_th) VALUES
    ('ADMIN', 'ผู้ดูแลระบบ'),
    ('DORM_STAFF', 'เจ้าหน้าที่หอพัก'),
    ('FINANCE', 'เจ้าหน้าที่การเงิน'),
    ('APPROVER', 'ผู้อนุมัติ'),
    ('MAINTENANCE', 'เจ้าหน้าที่ซ่อมและพัสดุ'),
    ('TENANT', 'ผู้เช่า');

INSERT INTO permissions (code, resource_name, action_name, description) VALUES
    ('USERS:READ', 'USERS', 'READ', 'ค้นหาและดูบัญชีผู้ใช้'),
    ('USERS:WRITE', 'USERS', 'WRITE', 'สร้างและแก้ไขบัญชีผู้ใช้'),
    ('ROLES:READ', 'ROLES', 'READ', 'ดูบทบาทและสิทธิ์'),
    ('ROLES:WRITE', 'ROLES', 'WRITE', 'กำหนดสิทธิ์บทบาท'),
    ('AUDIT:READ', 'AUDIT', 'READ', 'ค้นหาประวัติการใช้งาน'),
    ('MASTER_DATA:READ', 'MASTER_DATA', 'READ', 'ดูข้อมูลตั้งต้น'),
    ('MASTER_DATA:WRITE', 'MASTER_DATA', 'WRITE', 'แก้ไขข้อมูลตั้งต้น'),
    ('TENANTS:READ', 'TENANTS', 'READ', 'ดูทะเบียนผู้เช่า'),
    ('TENANTS:WRITE', 'TENANTS', 'WRITE', 'แก้ไขทะเบียนผู้เช่า'),
    ('PROPERTY:READ', 'PROPERTY', 'READ', 'ดูอาคาร ห้อง และเตียง'),
    ('PROPERTY:WRITE', 'PROPERTY', 'WRITE', 'แก้ไขอาคาร ห้อง และเตียง'),
    ('FINANCE:READ', 'FINANCE', 'READ', 'ดูข้อมูลการเงิน'),
    ('FINANCE:WRITE', 'FINANCE', 'WRITE', 'จัดทำรายการการเงิน'),
    ('APPROVAL:WRITE', 'APPROVAL', 'WRITE', 'อนุมัติรายการ'),
    ('MAINTENANCE:WRITE', 'MAINTENANCE', 'WRITE', 'ดำเนินงานซ่อมและสต็อก'),
    ('REPORTS:READ', 'REPORTS', 'READ', 'ดูและส่งออกรายงาน'),
    ('SELF_SERVICE:READ', 'SELF_SERVICE', 'READ', 'ดูข้อมูลของตนเอง'),
    ('SELF_SERVICE:WRITE', 'SELF_SERVICE', 'WRITE', 'ทำรายการของตนเอง');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r CROSS JOIN permissions p WHERE r.code = 'ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p
    ON p.code IN ('MASTER_DATA:READ', 'TENANTS:READ', 'TENANTS:WRITE',
                  'PROPERTY:READ', 'PROPERTY:WRITE', 'REPORTS:READ')
WHERE r.code = 'DORM_STAFF';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p
    ON p.code IN ('TENANTS:READ', 'FINANCE:READ', 'FINANCE:WRITE', 'REPORTS:READ')
WHERE r.code = 'FINANCE';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p
    ON p.code IN ('FINANCE:READ', 'APPROVAL:WRITE', 'REPORTS:READ')
WHERE r.code = 'APPROVER';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p
    ON p.code IN ('PROPERTY:READ', 'MAINTENANCE:WRITE')
WHERE r.code = 'MAINTENANCE';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r JOIN permissions p
    ON p.code IN ('SELF_SERVICE:READ', 'SELF_SERVICE:WRITE')
WHERE r.code = 'TENANT';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM app_users u
JOIN roles r ON r.code = u.role;

CREATE INDEX idx_master_data_type_active ON master_data_items(data_type, active);
CREATE INDEX idx_import_sessions_status_expires ON import_sessions(status, expires_at);
