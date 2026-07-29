# Identity, RBAC และ Master Data Design

## ขอบเขต

Milestone นี้ต่อยอดจาก platform infrastructure และ audited user administration ที่มีอยู่แล้ว เพื่อทำให้การยืนยันตัวตน สิทธิ์ และข้อมูลตั้งต้นพร้อมรองรับโมดูลธุรกิจถัดไป โดยไม่รวม tenant workflow, occupancy, contract หรือ billing

## สถาปัตยกรรม

- คง session authentication และ `AppUserEntity` เป็นแหล่งบัญชีภายใน
- เพิ่ม role/permission matrix แบบฐานข้อมูล โดย seed บทบาท `ADMIN`, `DORM_STAFF`, `FINANCE`, `APPROVER`, `MAINTENANCE`, `TENANT`
- สิทธิ์ใช้รหัส `RESOURCE:ACTION` และถูกแปลงเป็น Spring Security authorities ตอน login
- ผู้ใช้หนึ่งรายรองรับหลายบทบาท แต่ระหว่าง migration คอลัมน์ `app_users.role` ยังคงเป็น primary role เพื่อ backward compatibility
- Master data ใช้ resource แยกตามชนิดที่มีโครงสร้างเฉพาะ และใช้ catalog กลางเฉพาะรายการ code/name/effective dates ที่มีรูปแบบเดียวกัน
- การแก้ไขและยกเลิก user, role, permission และ master data ต้องสร้าง append-only audit event

## Interfaces

- `/api/v1/users`: list/search/create/change status/reset password/change password
- `/api/v1/roles`: list role พร้อม permissions และแก้ permission assignment
- `/api/v1/audit-logs`: filter actor/action/entity/date และ pagination
- `/api/v1/master-data/{type}`: list/create/update/deactivate รายการ catalog
- `/api/v1/imports`: upload, preview, confirm และดาวน์โหลด error workbook
- ขนาดหน้า API สูงสุด 200 รายการ

## Excel Import

การ upload จะเก็บไฟล์ชั่วคราวพร้อม SHA-256 และสร้าง preview session ที่มีวันหมดอายุ การ preview อ่านทุกแถวและคืนผล validation โดยไม่เขียน business tables การ confirm ต้องใช้ preview token และ hash เดิม จากนั้นตรวจซ้ำทั้งไฟล์ใน transaction เดียว หากมีข้อผิดพลาดแม้หนึ่งแถว transaction ต้อง rollback ทั้งหมดและสร้าง error workbook

## Security และ Error Handling

- Endpoint ใน milestone นี้เป็น `ADMIN` เท่านั้น ยกเว้น change-password ของบัญชีตนเอง
- รหัสผ่านใหม่ต้องผ่าน policy เดียวกันทุกช่องทางและเข้ารหัส BCrypt cost 12
- ไม่คืน password hash หรือข้อมูลลับผ่าน API
- optimistic locking และข้อมูลชนกันตอบ `409`; validation ตอบ error envelope กลางพร้อม trace ID
- ไฟล์ import จำกัด 10 MB และตรวจ extension, MIME และ magic bytes ก่อน parse

## Testing

- Domain/service tests สำหรับ permission resolution, password policy, deactivate และ atomic import
- MVC authorization tests ครบ anonymous, wrong role และ admin
- Repository/integration tests บน MySQL 8.4 สำหรับ migration, unique constraints และ rollback ทั้งไฟล์
- Frontend component tests สำหรับ role-aware menu, user/master forms และ import wizard
- เกณฑ์จบ milestone: ไม่มีเมนู dead-end, API pagination ไม่เกิน 200, import ผิดหนึ่งแถวไม่บันทึกข้อมูล และ audit ค้นย้อนหลังได้

## การตัดสินใจที่ล็อก

- ใช้บัญชีภายใน ไม่มี LDAP/AD, SMTP หรือ OTP
- notification เป็นภายในระบบ
- ไม่ลบ user/role/master data ที่ถูกใช้งาน แต่เปลี่ยนสถานะพร้อมเหตุผล
- audit retention ค่าเริ่มต้น 365 วันและไม่ต่ำกว่า 90 วัน
- executable JAR เป็น packaging เดียว
