# Milestone 1 UAT — Identity, RBAC และ Master Data

วันที่ตรวจ: 29 กรกฎาคม 2026

## ขอบเขต

- Login/session และ route/menu authorization ตามบทบาท
- จัดการผู้ใช้และสิทธิ์
- ค้นหา audit log
- จัดการ master data แบบ effective-dated
- นำเข้า `.xlsx` แบบ preview/confirm โดยไม่เกิด partial commit

## เส้นทางสำหรับทดสอบ

| URL | ผู้มีสิทธิ์ | เกณฑ์รับมอบ |
|---|---|---|
| `/admin/users` | `ADMIN` | ค้นหา แบ่งหน้า เพิ่มบัญชี เปลี่ยนสถานะ และตั้งรหัสผ่านใหม่ได้ |
| `/admin/roles` | `ADMIN` | เห็นบทบาท 6 กลุ่มและบันทึกชุดสิทธิ์พร้อมเหตุผลได้ |
| `/admin/audit` | `ADMIN` | กรอง actor/action/entity/date ดู trace ID และรายละเอียดได้โดยไม่มี mutation control |
| `/admin/master-data` | `ADMIN` | สลับ 13 ประเภท เพิ่ม/แก้ไข/เปลี่ยนสถานะ และแสดงช่วงวันที่ใช้งานได้ |
| `/admin/imports` | `ADMIN` | ตรวจ `.xlsx` ก่อนบันทึก ดาวน์โหลด error workbook และยืนยันได้ครั้งเดียวเมื่อทุกแถวถูกต้อง |

บัญชีที่ไม่ใช่ `ADMIN` ต้องไม่เห็นเมนูด้านบน และการเปิด URL โดยตรงต้องกลับหน้า `/`

## รูปแบบ Excel

- ขนาดสูงสุด 10 MB และต้องเป็นไฟล์ `.xlsx`
- หัวตาราง: `type`, `code`, `nameTh`, `nameEn`, `parentId`, `effectiveFrom`, `effectiveTo`
- Preview มีอายุ 1 ชั่วโมง แสดง SHA-256, จำนวนแถวทั้งหมด/ถูกต้อง/ผิด และข้อผิดพลาดรายแถว
- ถ้ามีแถวผิด ปุ่มยืนยันต้องไม่ปรากฏ และ confirm ต้องไม่เพิ่มข้อมูลแม้แต่บางส่วน

## หลักฐานการตรวจ

| รายการ | ผล |
|---|---|
| Frontend component tests | ผ่าน 19 tests, 8 test files |
| Frontend production build | ผ่าน `npm run build` |
| Backend automated tests | ผ่าน 78 tests, 0 failures/errors |
| Desktop browser | Login, dashboard, admin navigation, user dialog และ empty/error states ไม่มี horizontal overflow |
| Mobile 390 × 844 | users, roles, audit, master data และ import ไม่มี horizontal overflow; sidebar และ action controls แสดงครบ |
| Accessibility contract | มี semantic heading/region/dialog, ชื่อ control, keyboard focus style และ reduced-motion rule |

คำสั่งตรวจซ้ำ:

```powershell
cd frontend
npm test
npm run build

cd ..\backend
mvn -q clean test
```

การตรวจ end-to-end บนฐานข้อมูลของหน่วยงานต้องทำซ้ำใน staging โดยใช้บัญชีทดสอบแยก และตรวจ audit record หลัง mutation ทุกประเภทก่อนลงนาม UAT ขั้นสุดท้าย
