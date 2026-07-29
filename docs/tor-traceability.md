# TOR Traceability Matrix

| TOR | ความสามารถ | จุดรองรับ |
|---|---|---|
| 3.2.3 | Responsive Web Application | React layout, mobile breakpoints และผลตรวจ desktop/mobile ใน `docs/uat/milestone-1.md` |
| 3.2.4–3.2.6 | Login, lock และสิทธิ์ | `identity`, Spring Security, session/CSRF, route/menu guard และหน้าจอ `/admin/users`, `/admin/roles` |
| 3.7 | Integrity และ security | database constraints, validation, RBAC |
| 3.10, 3.22 | Log ย้อนหลัง | `audit_logs`, retention config 365 วัน และหน้าค้นหา `/admin/audit` |
| 3.12 | ข้อมูลตั้งต้นและนำเข้าข้อมูล | effective-dated master data, `/admin/master-data`, atomic XLSX preview/confirm และ error workbook |
| 3.11 | งานการเงิน | invoice/payment/receipt schema และ billing domain |
| 3.13 | ข้อมูลผู้เช่า | tenant API และ `tenants` |
| 3.14 | ห้อง/เตียง/ห้องชำรุด | room API, optimistic lock และ constraints |
| 3.15 | แจ้งซ่อม | maintenance state machine และ schema |
| 3.16–3.17 | สต็อก | inventory domain และ stock movement schema |
| 4.5.2–4.5.3 | สัญญาอิเล็กทรอนิกส์ | contract state, document storage และ SHA-256 |
| 4.5.4–4.5.5 | ใบแจ้งหนี้/ใบเสร็จ | invoice API และ payment/receipt schema |
| 4.7 | รับและนำส่งเงิน | payment/receipt schema; workflow UI เป็น milestone ถัดไป |
| 4.8–4.10 | รายงานและ Excel | reporting module boundary; report templates เป็น milestone ถัดไป |
| 4.11 | ข่าวสาร | announcements schema; management UI เป็น milestone ถัดไป |

รายการที่ระบุว่า milestone ถัดไปมี schema และ boundary พร้อมแล้ว แต่ยังต้องเพิ่ม
workflow/API/UI เฉพาะงานก่อนนำไป UAT ตาม TOR เต็มรูปแบบ
