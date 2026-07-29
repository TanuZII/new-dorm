# Data Dictionary ฉบับเริ่มต้น

| กลุ่ม | ตาราง | หน้าที่ |
|---|---|---|
| Identity | `app_users` | บัญชี รหัสผ่านแบบ hash บทบาท และสถานะการล็อก |
| Identity | `roles`, `permissions`, `role_permissions`, `user_roles` | บทบาท 6 กลุ่ม สิทธิ์ราย resource และความสัมพันธ์บัญชี-บทบาท |
| Audit | `audit_logs` | ผู้ดำเนินการ การกระทำ เหตุผล IP trace ID และวันเวลา |
| Master Data | `master_data_items` | รหัส/ชื่อ ประเภท parent ช่วงวันที่ใช้งาน สถานะ และ optimistic-lock version |
| Import | `import_sessions`, `import_errors` | preview token, SHA-256, วันหมดอายุ สรุปจำนวนแถว และข้อผิดพลาดรายแถวก่อน atomic confirm |
| Tenant | `tenants` | นักศึกษา บุคลากร ศิษย์เก่า และบุคคลภายนอก |
| Tenant | `tenant_code_sequences`, `tenant_addresses`, `tenant_contacts` | ลำดับรหัสผู้เช่า ที่อยู่ และข้อมูลผู้ติดต่อ/ผู้ติดต่อฉุกเฉิน |
| Property | `rooms` | อาคาร ห้อง ชั้น ความจุ จำนวนผู้พัก และสถานะ |
| Property | `buildings`, `floors`, `beds`, `room_meters` | โครงสร้างอาคาร-ชั้น-ห้อง-เตียง และทะเบียนมิเตอร์ประจำห้อง |
| Reservation | `reservations`, `reservation_beds` | หัวรายการจองและเตียงจริงที่จัดสรรสำหรับการจองรายเตียงหรือทั้งห้อง |
| Occupancy | `occupancies` | การครอบครองเตียงและช่วงวันที่เข้าพัก |
| Occupancy | `occupancy_beds`, `occupancy_events` | การจัดสรรเตียงตามช่วงเวลาและประวัติเข้าพัก ย้ายห้อง ขอออก หรือยกเลิกแบบ append-only |
| Allocation | `bed_allocation_days` | ล็อกหนึ่งแถวต่อเตียงต่อวันด้วย primary key `(bed_id, allocation_date)` และอ้างถึง reservation หรือ occupancy อย่างใดอย่างหนึ่ง |
| Contract | `contracts` | สัญญา ช่วงเช่า การยืนยัน PDF และ SHA-256 |
| Billing | `charge_rates` | อัตราที่มีช่วงวันเริ่มและวันสิ้นสุด |
| Billing | `meter_readings` | มิเตอร์เดิม/ใหม่ อัตราต่อหน่วย และรอบเดือน |
| Billing | `invoices`, `invoice_items` | หัวใบแจ้งหนี้และรายละเอียดค่าใช้จ่าย |
| Payment | `payments`, `receipts` | หลักฐาน การตรวจรับ และใบเสร็จ |
| Deposit | `deposits` | เงินรับ หัก และคืนเงินประกัน |
| Maintenance | `maintenance_requests` | งานซ่อม ผู้รับผิดชอบ ผลงาน และสถานะ |
| Inventory | `inventory_items`, `stock_movements` | ยอดคงเหลือและประวัติรับเข้า/เบิกออก |
| Notification | `announcements` | ข่าว กลุ่มเป้าหมาย ช่วงแสดง และการเปิดความคิดเห็น |

จำนวนเงินใช้ `DECIMAL(12,2)` เสมอ วันใช้ `DATE` และเหตุการณ์ใช้ `TIMESTAMP`.
ข้อมูลธุรกรรมที่ออกเอกสารแล้วต้องเปลี่ยนสถานะหรือ void พร้อมเหตุผล ห้ามลบจริง
