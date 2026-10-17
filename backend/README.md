# CSTU NEXUS — Backend & Database

ส่วนประมวลผลเซิร์ฟเวอร์ (REST API) และฐานข้อมูลสำหรับระบบ CSTU NEXUS

> 📖 สำหรับข้อมูลภาพรวมโปรเจกต์ สมาชิกกลุ่ม และคู่มือรวม สามารถดูได้ที่ [Root README.md](../README.md)

---

## เทคโนโลยีที่ใช้ (Backend & Database Tech Stack)

- **Java 17:** ภาษาหลักที่ใช้ในการพัฒนา
- **Spring Boot 4.1.x:** เฟรมเวิร์กหลัก (Spring Web MVC, Spring JDBC)
- **Lombok:** ลด Boilerplate Code สำหรับ Model/DTO
- **PostgreSQL 17:** ฐานข้อมูลเชิงสัมพันธ์ (Relational Database) รันบน Docker Container
- **Docker & Docker Compose:** ควบคุม Container สภาพแวดล้อมฐานข้อมูลให้อยู่ในรูปแบบเดียวกัน
- **Maven Wrapper (`mvnw`):** จัดการ Dependencies และ Build โปรเจกต์โดยไม่ต้องติดตั้ง Maven แยก

---

## โครงสร้างโฟลเดอร์ Backend

```text
backend/
├── cstu-course-nexus/             # Spring Boot Application
│   ├── src/main/java/             # Source Code หลักภาษา Java
│   │   └── th/ac/tu/cs/cstu_course_nexus/
│   │       ├── CstuCourseNexusApplication.java  # Main Application Entry Point
│   │       ├── HealthController.java            # Controller ตรวจสอบสถานะและทดสอบ DB
│   │       ├── AuthController.java              # Login, profile และ logout session
│   │       ├── AuthUser.java                    # ข้อมูลผู้ใช้ที่ยืนยันแล้วใน session
│   │       ├── StudyYearCalculator.java         # คำนวณชั้นปีจากรหัสนักศึกษา
│   │       ├── TuAuthService.java               # ยืนยันบัญชีกับ TU AD Verify API
│   │       └── WebConfig.java                   # CORS สำหรับ frontend
│   ├── src/main/resources/
│   │   └── application.properties # การตั้งค่า Spring Boot และการเชื่อมต่อ PostgreSQL
│   ├── src/test/                  # Unit / Integration Tests
│   ├── pom.xml                    # กำหนด Dependencies และ Plugins ของ Maven
│   └── mvnw / mvnw.cmd            # Maven Wrapper
│
├── database/                      # จัดการ PostgreSQL และ Seed Data
│   ├── compose.yaml               # Docker Compose สำหรับรัน PostgreSQL 17
│   ├── init.sql                   # Schema โครงสร้างตาราง (mock_user, courses, sections, review)
│   ├── seed.sql                   # SQL นำเข้าข้อมูลตั้งต้น (38 Courses, 63 Sections)
│   ├── courses.csv                # ข้อมูลรายวิชาต้นทาง
│   ├── sections.csv               # ข้อมูล Section ต้นทาง
│   ├── generate-seed.ps1          # PowerShell Script ตรวจสอบและแปลง CSV สู่ seed.sql
│   └── migrations/                # ประวัติไฟล์ SQL ปรับปรุง Schema แต่ละช่วง
│
└── README.md                      # เอกสารคู่มือ Backend & Database นี้
```

---

## สารบัญคู่มือการใช้งาน

1. [สิ่งที่ต้องติดตั้งก่อนเริ่มต้น](#1-สิ่งที่ต้องติดตั้งก่อนเริ่มต้น)
2. [รู้จักโครงสร้างโปรเจกต์](#2-รู้จักโครงสร้างโปรเจกต์)
3. [ตั้งค่าและเริ่มต้น Database](#3-ตั้งค่าและเริ่มต้น-database)
4. [ทำความเข้าใจ Schema และ Seed Data](#4-ทำความเข้าใจ-schema-และ-seed-data)
5. [รัน Spring Boot Backend](#5-รัน-spring-boot-backend)
6. [ตั้งค่า TU Authentication](#6-ตั้งค่า-tu-authentication)
7. [ทดสอบ Backend และ Database](#7-ทดสอบ-backend-และ-database)
8. [คำสั่งที่ใช้บ่อย](#8-คำสั่งที่ใช้บ่อย)
9. [การแก้ไขปัญหาเบื้องต้น](#9-การแก้ไขปัญหาเบื้องต้น)
10. [ข้อควรระวังเกี่ยวกับ Database](#10-ข้อควรระวังเกี่ยวกับ-database)

---

## 1. สิ่งที่ต้องติดตั้งก่อนเริ่มต้น

ก่อนเริ่มต้น ให้ตรวจสอบว่ามีโปรแกรมต่อไปนี้แล้ว

| โปรแกรม | ใช้ทำอะไร |
|---|---|
| Git | ดาวน์โหลด Source Code และจัดการ Version ของโปรเจกต์ |
| Docker Desktop | รัน PostgreSQL ใน Container |
| Java JDK 17 | ใช้รันและพัฒนา Spring Boot Backend |
| Visual Studio Code หรือ IDE อื่น | ใช้เปิดและแก้ไข Source Code |

ลิงก์สำหรับติดตั้ง:

- [Git](https://git-scm.com/downloads)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Eclipse Temurin JDK 17](https://adoptium.net/temurin/releases/?version=17)
- [Visual Studio Code](https://code.visualstudio.com/)

**หมายเหตุ:** โปรเจกต์มี Maven Wrapper (`mvnw`) อยู่แล้ว จึงไม่จำเป็นต้องติดตั้ง Maven แยก

### 1.1 ตรวจสอบโปรแกรม

เปิด Terminal หรือ Git Bash แล้วรัน:

```bash
git --version
java -version
docker --version
docker compose version
```

ตรวจสอบว่า Java เป็นเวอร์ชัน 17 และ Docker สามารถใช้งานได้

หากติดตั้ง Java 17 แล้ว แต่ Terminal ยังแสดง Java เวอร์ชันอื่น ให้ตรวจสอบ `JAVA_HOME` และ `PATH` ให้ชี้ไปยัง JDK 17 ก่อนดำเนินการต่อ

### 1.2 เข้าสู่โฟลเดอร์ Backend

เนื่องจากโปรเจกต์เป็น Monorepo ก่อนรันคำสั่งทั้งหมดในคู่มือนี้ ให้เปิด Terminal แล้วเข้าไปที่โฟลเดอร์ `backend/`:

```bash
cd backend
```

คำสั่งทั้งหมดในเอกสารนี้จะอ้างอิงจากโฟลเดอร์ `backend/` เป็นหลัก

---

## 2. รู้จักโครงสร้างโปรเจกต์

โปรเจกต์แบ่งออกเป็น 2 ส่วนหลัก ได้แก่ Database และ Spring Boot Backend

### 2.1 ไฟล์สำคัญในโฟลเดอร์หลัก

| ไฟล์หรือโฟลเดอร์ | หน้าที่ |
|---|---|
| `README.md` | คู่มือการตั้งค่าและใช้งานโปรเจกต์ |
| `database/` | ไฟล์สำหรับตั้งค่า PostgreSQL, Schema และ Seed Data |
| `cstu-course-nexus/` | Source Code ของ Spring Boot Backend |
| `.gitignore` | กำหนดไฟล์ที่ Git ไม่ควรติดตาม |

### 2.2 ไฟล์สำคัญใน `database/`

| ไฟล์ | หน้าที่ |
|---|---|
| `compose.yaml` | กำหนด PostgreSQL Container, Port, Database, User และ Persistent Volume |
| `init.sql` | กำหนดโครงสร้างตารางที่ใช้ตอน Initialize Database ใหม่ |
| `courses.csv` | ข้อมูลรายวิชาต้นทาง |
| `sections.csv` | ข้อมูล Section ต้นทาง |
| `generate-seed.ps1` | สคริปต์ PowerShell สำหรับตรวจสอบข้อมูลและสร้าง SQL จากไฟล์ CSV |
| `seed.sql` | SQL สำหรับนำเข้าข้อมูล Courses และ Sections |
| `migrations/` | เก็บ SQL สำหรับการเปลี่ยนแปลง Schema ในแต่ละช่วงของการพัฒนา |

ไฟล์เหล่านี้มีหน้าที่ต่างกัน ไม่ควรนำมาใช้แทนกัน

ตัวอย่างเช่น การแก้ `init.sql` ไม่ได้หมายความว่า Database ที่สร้างไปแล้วจะเปลี่ยนแปลงตาม และการแก้ CSV ก็ไม่ได้ทำให้ข้อมูลใน Database อัปเดตเอง

### 2.3 ไฟล์สำคัญใน `cstu-course-nexus/`

| ไฟล์หรือโฟลเดอร์ | หน้าที่ |
|---|---|
| `pom.xml` | กำหนด Dependencies และการตั้งค่าการ Build ของ Maven |
| `mvnw` | Maven Wrapper สำหรับเรียก Maven โดยไม่ต้องติดตั้ง Maven แยก |
| `src/main/java/` | เก็บ Java Source Code ของ Backend |
| `src/main/resources/application.properties` | ตั้งค่าการทำงานของ Spring Boot และการเชื่อมต่อ Database |
| `src/test/` | ตำแหน่งสำหรับ Test Code |

ไฟล์สำคัญที่เกี่ยวข้องกับการเชื่อมต่อ Database ได้แก่:

- `pom.xml` มี PostgreSQL JDBC Driver และ Spring JDBC
- `application.properties` กำหนด Database URL, Username และ Password
- `HealthController.java` มี Endpoint สำหรับตรวจสอบการทำงานของแอปและการเชื่อมต่อ Database

ปัจจุบันการทดสอบ Database ใช้ `JdbcTemplate` ผ่าน Spring JDBC ส่วน JPA ยังไม่ได้เปิดใช้งาน

---

## 3. ตั้งค่าและเริ่มต้น Database

ในโปรเจกต์นี้ PostgreSQL 17 ทำงานผ่าน Docker Compose เพื่อให้สมาชิกในทีมใช้ Database Environment ที่สอดคล้องกัน

### ขั้นตอนที่ 1: เปิด Docker Desktop

เปิด Docker Desktop และรอจนกว่า Docker จะพร้อมใช้งาน

### ขั้นตอนที่ 2: เข้าสู่โฟลเดอร์ Backend

เปิด Terminal แล้วเข้าไปที่โฟลเดอร์ `backend/`:

```bash
cd backend
```

### ขั้นตอนที่ 3: เริ่มต้น PostgreSQL

รันคำสั่ง:

```bash
docker compose -f database/compose.yaml up -d
```

คำสั่งนี้จะสร้างหรือเริ่ม Container ตามการตั้งค่าใน `compose.yaml` โดยทำงานอยู่เบื้องหลัง

### ขั้นตอนที่ 4: ตรวจสอบสถานะ Container

```bash
docker compose -f database/compose.yaml ps
```

ตรวจสอบว่า Container `cstu-nexus-postgres` อยู่ในสถานะกำลังทำงาน

สามารถตรวจสอบ Log เพิ่มเติมได้ด้วย:

```bash
docker logs cstu-nexus-postgres
```

### ขั้นตอนที่ 5: เชื่อมต่อ PostgreSQL

```bash
docker exec -it cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db
```

เมื่อเชื่อมต่อสำเร็จ จะเข้าสู่ PostgreSQL Command Line ซึ่งเรียกว่า `psql`

ลองแสดงตารางใน Database:

```sql
\dt
```

หากต้องการออกจาก `psql`:

```text
\q
```

เมื่อเริ่มต้น Database สำเร็จแล้ว สามารถดำเนินการขั้นตอนถัดไปได้

---

## 4. ทำความเข้าใจ Schema และ Seed Data

ก่อนรัน Backend ควรเข้าใจความแตกต่างระหว่าง Schema และ Seed Data ก่อน

- **Schema** คือโครงสร้างของ Database เช่น ตาราง คอลัมน์ และข้อกำหนดของข้อมูล
- **Seed Data** คือข้อมูลตั้งต้นที่นำเข้าไปเก็บในตาราง เพื่อใช้ในการพัฒนาและทดสอบระบบ

### 4.1 ตารางใน Database

ไฟล์ `database/init.sql` ใช้กำหนดโครงสร้างตารางของโปรเจกต์ ซึ่งประกอบด้วย:

| ตาราง | หน้าที่โดยสรุป |
|---|---|
| `mock_user` | เก็บข้อมูลผู้ใช้จำลอง |
| `courses` | เก็บข้อมูลรายวิชา |
| `sections` | เก็บข้อมูล Section ของรายวิชา |
| `review` | เก็บข้อมูลรีวิว |

### 4.2 นำเข้า Seed Data

ไฟล์ `database/seed.sql` เตรียมข้อมูลตั้งต้นสำหรับ:

- `courses` จำนวน 38 รายวิชา
- `sections` จำนวน 63 Sections

ข้อมูลต้นทางมาจาก `courses.csv` และ `sections.csv` โดยไม่รวม Placeholder Sections ที่มี `section_number` เป็น `000000`

**สำคัญ:** การเริ่ม PostgreSQL ด้วย Docker Compose ไม่ได้หมายความว่า `seed.sql` จะถูกรันโดยอัตโนมัติ

หากเป็น Database ใหม่ที่สร้างตารางแล้ว แต่ยังไม่มีข้อมูล Courses และ Sections ให้ตรวจสอบก่อนว่าจำเป็นต้องนำเข้า Seed Data หรือไม่

คำสั่งนำเข้าข้อมูล:

```bash
docker exec -i cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db -v ON_ERROR_STOP=1 < database/seed.sql
```

หลังนำเข้า ให้ตรวจสอบจำนวนข้อมูล:

```bash
docker exec -it cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db
```

จากนั้นรันภายใน `psql`:

```sql
SELECT COUNT(*) FROM courses;
SELECT COUNT(*) FROM sections;
```

ออกจาก `psql` ด้วย `\q`

จำนวนข้อมูลที่คาดหวังสำหรับ Seed Data ชุดปัจจุบันคือ 38 Courses และ 63 Sections

**อย่านำเข้า Seed Data ซ้ำโดยไม่ตรวจสอบก่อน** เพราะ `seed.sql` ไม่ได้ออกแบบมาให้รันซ้ำบน Database ที่มีข้อมูลอยู่แล้ว และอาจเกิด Primary Key ซ้ำได้

### 4.3 เมื่อข้อมูล CSV เปลี่ยน

หากมีการแก้ไข `courses.csv` หรือ `sections.csv` ให้ใช้ Workflow ต่อไปนี้

**ขั้นตอนที่ 1: สร้าง SQL สำหรับทดสอบ**

จากโฟลเดอร์หลักของ Repository:

```bash
powershell.exe -NoProfile -ExecutionPolicy Bypass -File database/generate-seed.ps1 -OutputPath database/seed.generated.test.sql
```

คำสั่งนี้จะสร้างไฟล์ทดสอบแยกต่างหาก โดยไม่เขียนทับ `seed.sql` ตัวจริง

**ขั้นตอนที่ 2: ตรวจสอบผลลัพธ์**

ตรวจสอบว่า Script ทำงานสำเร็จ ไม่มี Error และจำนวนข้อมูลตรงกับ CSV ที่แก้ไข

เปิดไฟล์ `database/seed.generated.test.sql` เพื่อตรวจสอบ SQL ที่สร้างขึ้นก่อนดำเนินการต่อ

**ขั้นตอนที่ 3: สร้าง `seed.sql` ตัวจริง**

เมื่อผลการทดสอบถูกต้องแล้ว ให้รัน:

```bash
powershell.exe -NoProfile -ExecutionPolicy Bypass -File database/generate-seed.ps1
```

คำสั่งนี้จะสร้าง `database/seed.sql` ตาม CSV ปัจจุบัน

**ขั้นตอนที่ 4: ตรวจสอบการเปลี่ยนแปลง**

```bash
git diff --check
git diff -- database/seed.sql
git status
```

ตรวจสอบว่าการเปลี่ยนแปลงใน `seed.sql` สอดคล้องกับข้อมูลที่ตั้งใจแก้ไข

**ข้อควรจำ:** การสร้าง `seed.sql` ใหม่ไม่ได้ทำให้ข้อมูลใน PostgreSQL เปลี่ยนตามโดยอัตโนมัติ การนำข้อมูลชุดใหม่เข้า Database ที่มีข้อมูลอยู่แล้วต้องพิจารณา Primary Key, Foreign Key และข้อมูลเดิมก่อนเสมอ

---

## 5. รัน Backend และ Database ผ่าน Docker

ต้องติดตั้งและเปิด Docker Desktop ก่อน จากนั้นเปิด PowerShell ที่ root ของ repository และตั้ง TU API key ใน terminal เดียวกันกับที่จะรัน Compose:

```powershell
$secureKey = Read-Host "TU API Application Key" -AsSecureString
$env:TU_API_APPLICATION_KEY = [System.Net.NetworkCredential]::new("", $secureKey).Password
docker compose -f backend/database/compose.yaml up -d --build
```

Compose จะ build Spring Boot backend, เริ่ม PostgreSQL และเริ่ม backend หลังจาก database พร้อมใช้งาน โดย backend เปิดที่ `http://localhost:8080` และข้อมูล PostgreSQL เก็บใน named volume เดิม

ตรวจสถานะและ log:

```powershell
docker compose -f backend/database/compose.yaml ps
docker compose -f backend/database/compose.yaml logs -f backend
```

หยุด app/database โดยเก็บข้อมูลฐานข้อมูลไว้:

```powershell
docker compose -f backend/database/compose.yaml down
```

ห้ามใช้ `down -v` หากยังต้องการเก็บข้อมูลใน database volume

---

## 6. ตั้งค่า TU Authentication

Backend เชื่อมต่อ `https://restapi.tu.ac.th/api/v1/auth/Ad/verify` จากฝั่ง server เท่านั้น โดยส่งชื่อผู้ใช้/รหัสผ่านไปตรวจสอบ และเก็บผลยืนยันใน HttpOnly session cookie ของแอป ไม่ส่ง TU Application Key ให้ browser

ขอ Application Key จากผู้ดูแล TU API แล้วตั้งค่าใน PowerShell **ก่อนเริ่ม Compose** (ใช้ค่าจริงเฉพาะในเครื่อง/secret manager ห้าม commit ลง Git):

```powershell
$secureKey = Read-Host "TU API Application Key" -AsSecureString
$env:TU_API_APPLICATION_KEY = [System.Net.NetworkCredential]::new("", $secureKey).Password
$env:APP_CORS_ALLOWED_ORIGINS = "http://localhost:5500"
docker compose -f backend/database/compose.yaml up -d --build
```

ถ้าใช้ Live Server ที่พอร์ตอื่น ให้แก้ `APP_CORS_ALLOWED_ORIGINS` ให้ตรง origin ที่แสดงใน address bar เช่น `http://127.0.0.1:5500` ไม่ใช่ URL ของ backend

เรียกใช้ได้ที่:

- `POST http://localhost:8080/api/auth/login` รับ JSON `{ "username": "6800000000", "password": "..." }`; ในฟอร์มให้กรอกเลขนักศึกษา 10 หลัก ระบบส่งเลขนั้นไป TU Authentication
- `GET http://localhost:8080/api/auth/me` คืนข้อมูลบัญชีเมื่อส่ง session cookie ที่ได้จาก login
- `POST http://localhost:8080/api/auth/logout` ยกเลิก session

Backend อนุญาตเฉพาะ account type `student` ที่มีอีเมล `@dome.tu.ac.th` ตามข้อมูลจาก TU Verify; ขณะนี้ยังไม่ได้จำกัดสาขา CSTU เนื่องจากต้องใช้ค่าคณะ/สาขาจริงจาก TU API ชั้นปีคำนวณจากเลขสองหลักแรกของรหัสนักศึกษาเทียบกับปี พ.ศ. ปัจจุบัน เช่น `68` ในปี พ.ศ. 2569 ได้ชั้นปี 2; หากรหัสอ่านไม่ได้หรือเป็นปีในอนาคตจะส่ง `null`

ค่า session หมดอายุใน 30 นาที Cookie ตั้ง `HttpOnly` และ `SameSite=Lax`; เมื่อนำ frontend/backend ไปอยู่คนละ site ใน production ต้องใช้ HTTPS และตั้ง `SESSION_COOKIE_SECURE=true`, `SESSION_COOKIE_SAME_SITE=none` พร้อมระบุ origin จริงใน `APP_CORS_ALLOWED_ORIGINS` การเก็บ session เริ่มต้นอยู่ในหน่วยความจำของ Spring Boot เหมาะกับการพัฒนา/instance เดียว; ระบบหลาย instance ควรย้าย session store ไป Redis หรือระบบ shared session

---

## 7. ทดสอบ Backend และ Database

หลังจาก Spring Boot เริ่มทำงาน ให้เปิด Terminal อีกหน้าต่างหนึ่ง

### 7.1 ตรวจสอบว่า Backend ทำงานหรือไม่

```bash
curl -i http://localhost:8080/health
```

Endpoint นี้ใช้ตรวจสอบว่าแอปตอบสนองได้หรือไม่

ผลลัพธ์ที่คาดหวังใน Body:

```text
CSTU Course Nexus is running!
```

### 7.2 ตรวจสอบการเชื่อมต่อ Database

```bash
curl -i http://localhost:8080/db-test
```

Endpoint นี้ใช้ `JdbcTemplate` รัน SQL เพื่อนับจำนวนแถวในตาราง `courses`

หาก Database เชื่อมต่อสำเร็จและมี Seed Data ครบตามชุดปัจจุบัน จะได้ข้อความลักษณะนี้:

```text
PostgreSQL connection successful! Courses: 38
```

หากได้ผลลัพธ์ดังกล่าว แสดงว่า Spring Boot สามารถเชื่อมต่อ PostgreSQL และอ่านข้อมูลจากตาราง `courses` ได้

### 7.3 Endpoint ที่มีในปัจจุบัน

| Method | Endpoint | หน้าที่ |
|---|---|---|
| `GET` | `/health` | ตรวจสอบว่า Backend ทำงานอยู่ |
| `POST` | `/echo` | รับข้อความแล้วส่งข้อความตอบกลับ |
| `GET` | `/db-test` | ตรวจสอบการเชื่อมต่อ PostgreSQL และนับ Courses |
| `POST` | `/api/auth/login` | ยืนยันบัญชีกับ TU AD และสร้าง session |
| `GET` | `/api/auth/me` | อ่านโปรไฟล์จาก session |
| `POST` | `/api/auth/logout` | ยกเลิก session |

Endpoints สำหรับ Authentication ต้องตั้ง `TU_API_APPLICATION_KEY` ก่อนใช้งาน

### 7.4 หยุด Spring Boot

กลับไปที่ Terminal ที่กำลังรัน Spring Boot แล้วกด:

```text
Ctrl + C
```

เมื่อต้องการเริ่มแอปใหม่ ให้รัน `./mvnw spring-boot:run` อีกครั้งจากโฟลเดอร์ `cstu-course-nexus`

---

## 8. คำสั่งที่ใช้บ่อย

### Docker และ PostgreSQL

รันคำสั่งเหล่านี้จากโฟลเดอร์หลักของ Repository

| สิ่งที่ต้องการทำ | คำสั่ง |
|---|---|
| เริ่ม Database | `docker compose -f database/compose.yaml up -d` |
| ตรวจสอบ Container | `docker compose -f database/compose.yaml ps` |
| ดู PostgreSQL Log | `docker logs cstu-nexus-postgres` |
| หยุด Database | `docker compose -f database/compose.yaml stop` |
| เริ่ม Database ที่หยุดไว้ | `docker compose -f database/compose.yaml start` |

### Spring Boot และ Maven

รันคำสั่งเหล่านี้จาก root ของ repository:

| สิ่งที่ต้องการทำ | คำสั่ง |
|---|---|
| เริ่ม Database และ Backend | `docker compose -f backend/database/compose.yaml up -d --build` |
| ดูสถานะบริการ | `docker compose -f backend/database/compose.yaml ps` |
| ดู log ของ Backend | `docker compose -f backend/database/compose.yaml logs -f backend` |
| หยุดบริการโดยเก็บข้อมูล | `docker compose -f backend/database/compose.yaml down` |

รัน Test โดยไม่ผ่าน Docker จาก `backend/cstu-course-nexus` ด้วย `.\mvnw.cmd test`

---

## 9. การแก้ไขปัญหาเบื้องต้น

### ปัญหา: Docker ไม่ทำงาน

- ตรวจสอบว่า Docker Desktop เปิดอยู่
- รอจน Docker พร้อมใช้งาน
- ลองตรวจสอบด้วย `docker --version` และ `docker compose version`

### ปัญหา: Port 5432 ถูกใช้งานอยู่

อาจมี PostgreSQL หรือ Container อื่นใช้ Port นี้อยู่ ให้ตรวจสอบ Container และโปรแกรมที่ใช้งาน Port ก่อนเปลี่ยนการตั้งค่า

### ปัญหา: Spring Boot เชื่อมต่อ Database ไม่ได้

ตรวจสอบตามลำดับ:

1. PostgreSQL Container กำลังทำงานหรือไม่
2. Database ชื่อ `cstu_nexus_db` มีอยู่หรือไม่
3. Username และ Password ตรงกับค่าที่กำหนดใน PostgreSQL หรือไม่
4. `spring.datasource.url` ใน `application.properties` ถูกต้องหรือไม่
5. Port `5432` เปิดให้แอปเชื่อมต่อได้หรือไม่

### ปัญหา: ไม่พบตาราง `courses`

- ตรวจสอบว่า Database ถูก Initialize แล้ว
- ตรวจสอบว่ากำลังเชื่อมต่อ Database ที่ถูกต้อง
- ตรวจสอบว่า Schema มีตาราง `courses`
- หากตารางมีอยู่แต่ไม่มีข้อมูล ให้ตรวจสอบ Seed Data แทนการสร้างตารางซ้ำ

### ปัญหา: แก้ `init.sql` แล้ว Database ไม่เปลี่ยนแปลง

PostgreSQL จะรัน Initialization Script เมื่อ Data Directory ยังว่างเท่านั้น การแก้ไฟล์ภายหลังจึงไม่เปลี่ยนตารางที่มีอยู่โดยอัตโนมัติ

ควรใช้วิธีจัดการ Schema Change หรือ Migration ที่ทีมตกลงร่วมกัน ไม่ควรลบ Database เพื่อให้ Initialization Script ทำงานใหม่โดยไม่ตรวจสอบผลกระทบก่อน

### ปัญหา: `java -version` แสดง Java เวอร์ชันผิด

ตรวจสอบ `JAVA_HOME` และ `PATH` ใน Terminal ที่ใช้งาน ให้ชี้ไปยัง JDK 17 จากนั้นเปิด Terminal ใหม่และตรวจสอบอีกครั้ง

### ปัญหา: PowerShell แจ้ง ParserError

ตรวจสอบว่า `generate-seed.ps1` ถูกบันทึกด้วย Encoding ที่รองรับข้อความในไฟล์ หากใช้ Windows PowerShell 5.1 และพบปัญหาการอ่านอักขระ ให้บันทึกไฟล์เป็น UTF-8 with BOM แล้วทดลองใหม่

---

## 10. ข้อควรระวังเกี่ยวกับ Database

### 10.1 Persistent Volume

Docker Compose ใช้ Persistent Volume เพื่อเก็บข้อมูล Database ให้คงอยู่แม้หยุดหรือสร้าง Container ใหม่

คำสั่ง `stop` ใช้หยุด Container โดยไม่ลบข้อมูลใน Volume

**อย่าใช้คำสั่งต่อไปนี้หากยังต้องการเก็บข้อมูล Database ไว้:**

```bash
docker compose -f database/compose.yaml down -v
```

คำสั่งนี้สามารถลบ Volume ที่ Compose จัดการอยู่ได้ และทำให้ข้อมูล Database ที่เก็บใน Volume สูญหาย

### 10.2 การเปลี่ยนแปลง Schema

ก่อนแก้ไข Schema ให้ตรวจสอบผลกระทบกับข้อมูลเดิมและสมาชิกคนอื่นในทีมก่อน

ไฟล์ใน `database/migrations/` เป็นสคริปต์สำหรับการเปลี่ยนแปลง Schema ในแต่ละช่วงของการพัฒนา ไม่ควรรัน Migration เดิมซ้ำโดยไม่ตรวจสอบว่าเคยนำไปใช้กับ Database แล้วหรือไม่

### 10.3 Password และ Environment Variables

Spring Boot อ่านการตั้งค่าการเชื่อมต่อ Database จาก `cstu-course-nexus/src/main/resources/application.properties`

ค่าที่ใช้ในเครื่องสำหรับการพัฒนา ได้แก่:

- Database URL ใน Docker Compose: `jdbc:postgresql://db:5432/cstu_nexus_db`
- Username: `cstu_nexus_user`
- Password: กำหนดผ่าน `SPRING_DATASOURCE_PASSWORD` ให้ตรงกับ PostgreSQL Container

หากรัน Spring Boot จากเครื่องโดยตรง ให้ใช้ Database URL ที่มี host เป็น `localhost`; ใน Docker Compose backend ใช้ service host `db`

รหัสผ่านเริ่มต้นมีไว้สำหรับ Local Development เท่านั้น ไม่ควรนำไปใช้ในระบบ Production และไม่ควร Commit รหัสผ่านจริงของระบบเข้าสู่ Repository

---

**หมายเหตุ:** คู่มือนี้อ้างอิงโครงสร้างและการตั้งค่าของ Repository ในปัจจุบัน หากมีการเปลี่ยนแปลงไฟล์ คำสั่ง หรือการตั้งค่า Database ควรปรับปรุง README ให้ตรงกับระบบจริงด้วย
