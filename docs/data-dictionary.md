# Data Dictionary ฉบับเริ่มต้น

| กลุ่ม | ตาราง | หน้าที่ |
|---|---|---|
| Identity | `app_users` | บัญชี รหัสผ่านแบบ hash บทบาท และสถานะการล็อก |
| Audit | `audit_logs` | ผู้ดำเนินการ การกระทำ เหตุผล IP trace ID และวันเวลา |
| Tenant | `tenants` | นักศึกษา บุคลากร ศิษย์เก่า และบุคคลภายนอก |
| Property | `rooms` | อาคาร ห้อง ชั้น ความจุ จำนวนผู้พัก และสถานะ |
| Occupancy | `occupancies` | การครอบครองเตียงและช่วงวันที่เข้าพัก |
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

