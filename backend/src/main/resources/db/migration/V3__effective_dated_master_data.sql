ALTER TABLE master_data_items DROP INDEX uk_master_data_type_code;

CREATE UNIQUE INDEX uk_master_data_type_code_from
    ON master_data_items(data_type, item_code, effective_from);

INSERT INTO master_data_items
    (data_type, item_code, name_th, name_en, effective_from)
VALUES
    ('TITLE', 'MR', 'นาย', 'Mr.', '2000-01-01'),
    ('TITLE', 'MRS', 'นาง', 'Mrs.', '2000-01-01'),
    ('TITLE', 'MS', 'นางสาว', 'Ms.', '2000-01-01'),
    ('COUNTRY', 'TH', 'ประเทศไทย', 'Thailand', '2000-01-01'),
    ('TENANT_TYPE', 'STUDENT', 'นักศึกษา', 'Student', '2000-01-01'),
    ('TENANT_TYPE', 'STAFF', 'บุคลากร', 'Staff', '2000-01-01'),
    ('TENANT_TYPE', 'EXTERNAL', 'บุคคลภายนอก', 'External', '2000-01-01'),
    ('RENTAL_PERIOD', 'SEMESTER', 'รายภาคการศึกษา', 'Semester', '2000-01-01'),
    ('RENTAL_PERIOD', 'YEARLY', 'รายปี', 'Yearly', '2000-01-01'),
    ('CONTRACT_TYPE', 'SEMESTER', 'สัญญารายภาคการศึกษา', 'Semester contract', '2000-01-01'),
    ('CONTRACT_TYPE', 'YEARLY', 'สัญญารายปี', 'Yearly contract', '2000-01-01'),
    ('CONTRACT_TYPE', 'CUSTOM', 'สัญญากำหนดเอง', 'Custom contract', '2000-01-01'),
    ('FEE_TYPE', 'RENT', 'ค่าเช่า', 'Rent', '2000-01-01'),
    ('FEE_TYPE', 'WATER', 'ค่าน้ำ', 'Water', '2000-01-01'),
    ('FEE_TYPE', 'ELECTRICITY', 'ค่าไฟฟ้า', 'Electricity', '2000-01-01'),
    ('FEE_TYPE', 'DEPOSIT', 'เงินประกัน', 'Deposit', '2000-01-01'),
    ('FEE_TYPE', 'PENALTY', 'ค่าปรับ', 'Penalty', '2000-01-01'),
    ('FEE_TYPE', 'OTHER', 'ค่าบริการอื่น', 'Other', '2000-01-01');
