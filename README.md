# ระบบบริหารจัดการหอพักมหาวิทยาลัย

Web Application แบบ Modular Monolith สำหรับบริหารผู้เช่า ห้องพัก สัญญา การเงิน
งานซ่อม สต็อก และรายงานตาม TOR มหาวิทยาลัยสวนดุสิต

## เทคโนโลยี

- React 19.2, TypeScript, Vite และ Tailwind CSS 4
- Java 21, Spring Boot 4.1, Spring Security และ Spring Data JPA
- MySQL 8.4 LTS และ Flyway
- Session authentication, BCrypt, CSRF และ role-based access control

## โมดูลที่วางโครงสร้างแล้ว

- `identity`: session login, CSRF, บทบาท 6 กลุ่ม และการล็อกบัญชี
- `tenant`: ทะเบียนผู้เช่าและ REST API
- `property`: ห้องพัก จำนวนเตียง สถานะ และ REST API
- `contract`: state machine สำหรับการยืนยันสัญญา
- `billing`: สูตรมิเตอร์ ใบแจ้งหนี้ การชำระบางส่วน และการยกเลิก
- `maintenance`: state machine ของงานซ่อม
- `inventory`: กฎรับเข้า/เบิกออกและป้องกัน stock ติดลบ
- `documents`: file storage, generated filename และ SHA-256
- `reporting`: dashboard API

Flyway migration สร้างตารางสำหรับผู้ใช้ ผู้เช่า ห้อง การเข้าพัก สัญญา อัตรา
มิเตอร์ ใบแจ้งหนี้ การชำระ ใบเสร็จ เงินประกัน งานซ่อม สต็อก ข่าวสาร และ audit log

## เตรียมฐานข้อมูล

```sql
CREATE DATABASE dbdorm CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

กำหนด environment variables ก่อนเริ่มระบบ:

```powershell
$env:DORM_DB_URL='jdbc:mysql://127.0.0.1:3306/dbdorm?useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false'
$env:DORM_DB_USERNAME='dorm_app'
$env:DORM_DB_PASSWORD='your-database-password'
$env:DORM_BOOTSTRAP_PASSWORD='your-first-admin-password'
$env:DORM_STORAGE_PATH='D:\dorm-storage'
```

`DORM_BOOTSTRAP_PASSWORD` ใช้เฉพาะสร้างบัญชี `admin` ครั้งแรก ระบบจะไม่บันทึก
plain-text password ลงฐานข้อมูล หลังจากมีบัญชีแล้วสามารถไม่กำหนดค่านี้ได้

## พัฒนาและทดสอบ

Frontend:

```powershell
cd frontend
npm install
npm test
npm run dev
```

Backend:

```powershell
cd backend
mvn test
mvn spring-boot:run
```

Vite proxy จะส่ง `/api` และ `/actuator` ไปยัง `http://localhost:8080`

## สร้างไฟล์สำหรับติดตั้ง

```powershell
cd frontend
npm ci
npm test
npm run build

cd ..\backend
mvn clean test package
java -jar target\dorm-api-0.1.0-SNAPSHOT.jar
```

Maven จะรวม `frontend/dist` เข้าใน executable JAR โดยอัตโนมัติ เปิดระบบที่
`http://localhost:8080` และตรวจ health check ที่ `http://localhost:8080/actuator/health`

## Security defaults

- รหัสผ่านขั้นต่ำ 8 ตัวอักษร และเข้ารหัสด้วย BCrypt cost 12
- ล็อกบัญชี 15 นาทีเมื่อใส่รหัสผิดครบ 5 ครั้ง
- Session อายุ 30 นาที ใช้ HttpOnly/SameSite cookie
- Production ต้องตั้ง `DORM_COOKIE_SECURE=true` และให้บริการผ่าน HTTPS
- เอกสารเก็บนอก source tree พร้อม hash สำหรับตรวจการแก้ไข
- Audit retention เริ่มต้น 365 วัน

