# WAR Packaging Design

## Goal

เปลี่ยน backend จาก JAR เป็น WAR โดยไฟล์เดียวต้องรองรับทั้งการ deploy บน external Tomcat และการรันแบบ standalone ด้วย `java -jar` ขณะที่ React production build ยังคงถูกรวมและให้บริการจาก Spring Boot

## Build Design

- กำหนด Maven packaging เป็น `war`
- ใช้ชื่อ artifact เดิม `dorm-api-0.1.0-SNAPSHOT` จึงได้ไฟล์ `target/dorm-api-0.1.0-SNAPSHOT.war`
- กำหนด embedded Tomcat dependency เป็น `provided` เพื่อไม่ให้ servlet container ซ้ำเมื่อ deploy บน external Tomcat
- คง `spring-boot-maven-plugin` เพื่อ repackage WAR ให้รันด้วย `java -jar` ได้
- คง resource mapping จาก `../frontend/dist` ไปยัง `static` ภายใน WAR

## Application Bootstrap

ให้ `DormApplication` สืบทอด `SpringBootServletInitializer` และ override `configure` เพื่อรองรับ lifecycle ของ external servlet container ส่วน `main` method เดิมยังคงอยู่สำหรับ executable WAR และ `mvn spring-boot:run`

## Runtime and Configuration

- การเชื่อม MySQL, Flyway, document storage และ credentials ยังรับค่าจาก environment variables เหมือนเดิม
- เมื่อ deploy เป็น `dorm-api.war` บน external Tomcat ค่า context path โดยทั่วไปเป็น `/dorm-api`; ผู้ดูแลสามารถ rename เป็น `ROOT.war` หากต้องการให้ระบบอยู่ที่ `/`
- External Tomcat ต้องรองรับ Jakarta Servlet รุ่นที่ Spring Boot 4.1 ใช้ และต้องรันด้วย Java 21

## Verification

- รัน backend tests ทั้งหมด
- รัน `mvn clean package` และยืนยันว่ามีไฟล์ `.war` โดยไม่มี executable `.jar` เป็น artifact หลัก
- ตรวจโครงสร้าง WAR ว่ามี `WEB-INF/classes`, frontend static assets และ Spring Boot loader
- รัน `java -jar target/dorm-api-0.1.0-SNAPSHOT.war` บนพอร์ตทดสอบ แล้วตรวจ `/actuator/health` ว่าตอบ `UP`
- อัปเดต README ให้ใช้คำสั่ง build/run และแนวทาง deploy WAR ที่ถูกต้อง

## Out of Scope

- ไม่ติดตั้งหรือปรับ configuration ของ external Tomcat บนเครื่องปลายทาง
- ไม่เปลี่ยน API, database schema, authentication หรือ frontend behavior
