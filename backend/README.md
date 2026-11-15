# CSTU NEXUS — Backend & Database

ส่วนประมวลผลเซิร์ฟเวอร์ (REST API) และฐานข้อมูลสำหรับระบบ CSTU NEXUS

> 📖 **ภาพรวมระบบและสถาปัตยกรรม:** ดูได้ที่ [docs/architecture/architecture.md](../docs/architecture/architecture.md) และ [Root README.md](../README.md)

---

## Tech Stack
- **Language & Framework:** Java 17, Spring Boot 4.1.x (Spring Web MVC, Spring Data JPA, Spring JDBC)
- **Database:** PostgreSQL 17 (Docker Container)
- **Build & Dependency Tool:** Maven Wrapper (`mvnw` / `mvnw.cmd`)
- **External Integration:** TU Authentication API (AD Verify)

---

## โครงสร้างโฟลเดอร์ Backend

```text
backend/
├── cstu-course-nexus/             # Spring Boot Application
│   ├── src/main/java/             # Source Code ภาษา Java (Controllers, Services, Models, Repositories)
│   ├── src/main/resources/        # application.properties
│   ├── src/test/                  # Unit & Integration Tests
│   ├── pom.xml                    # Maven Configuration
│   └── mvnw / mvnw.cmd            # Maven Wrapper
└── database/                      # จัดการ PostgreSQL และ Seed Data
    ├── compose.yaml               # Docker Compose สำหรับรันเฉพาะ Database และ Backend
    ├── init.sql                   # โครงสร้างตารางหลักทั้งหมด
    ├── seed.sql                   # ข้อมูลตั้งต้น 38 รายวิชา และ 63 เซกชัน
    └── migrations/                # Migration scripts สำหรับอัปเดต Schema
```

---

## ขั้นตอนการติดตั้งและรันระบบ

### 1. ตั้งค่า `.env`
ตรวจสอบว่ามีไฟล์ `.env` ที่ root ของ repository พร้อมระบุคีย์:
```env
TU_API_APPLICATION_KEY=your_actual_key_here
```

### 2. เริ่มต้น Database (PostgreSQL 17)
เปิด Terminal จาก root หรือโฟลเดอร์ `backend/`:
```bash
# รัน PostgreSQL Container
docker compose -f backend/database/compose.yaml up -d db

# (เฉพาะสร้าง DB ครั้งแรก) นำเข้า Seed Data รายวิชาและเซกชัน
docker exec -i cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db -v ON_ERROR_STOP=1 < backend/database/seed.sql
```

ตรวจสอบตารางใน Database:
```bash
docker exec -it cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db -c "\dt"
```

### 3. รัน Spring Boot Application
เข้าไปที่โฟลเดอร์ `backend/cstu-course-nexus`:
```bash
cd backend/cstu-course-nexus

# บน Windows
.\mvnw.cmd spring-boot:run

# บน macOS / Linux
./mvnw spring-boot:run
```
แอปพลิเคชันจะเริ่มทำงานที่ `http://localhost:8080` (โหลดค่าจาก `.env` ที่ root อัตโนมัติ)

*(ทางเลือก)* รัน Backend + Database พร้อมกันผ่าน Docker:
```bash
docker compose --env-file .env -f backend/database/compose.yaml up -d --build
```

---

## การทดสอบระบบ (Running Tests)

### รัน Unit & Integration Tests ทั้งหมด:
```bash
cd backend/cstu-course-nexus

# บน Windows
.\mvnw.cmd test

# บน macOS / Linux
./mvnw test
```

### รัน Integration Tests แบบใช้ Test Database แยก:
```powershell
docker compose -f backend/database/test-compose.yaml up -d --wait
Push-Location backend/cstu-course-nexus
.\mvnw.cmd test
Pop-Location
docker compose -f backend/database/test-compose.yaml down -v
```

---

## สรุป REST API Endpoints

| Method | Endpoint | Use Case | คำอธิบาย |
|---|---|:---:|---|
| `GET` | `/health` | - | ตรวจสอบสถานะการทำงานของเซิร์ฟเวอร์ |
| `GET` | `/db-test` | - | ทดสอบการเชื่อมต่อ PostgreSQL |
| `POST` | `/api/auth/login` | U2 | ยืนยันตัวตนผ่าน TU AD Verify และสร้าง Session Cookie |
| `GET` | `/api/auth/me` | U2, U3 | คืนข้อมูลโปรไฟล์นักศึกษาและชั้นปีปัจจุบันจาก Session |
| `POST` | `/api/auth/logout` | U2 | ยกเลิก Session ผู้ใช้ |
| `GET` | `/api/courses?keyword=&year=` | U4 | ค้นหารายวิชา กรองตามชั้นปีแนะนำ |
| `GET` | `/api/courses/{id}` | U4 | ดึงรายละเอียดวิชาและรายชื่อเซกชันพร้อมผู้สอน |
| `GET` | `/api/courses/{id}/reviews?sectionId=` | U1 | ดึงประวัติรีวิวของวิชา (รองรับกรองเซกชัน และ Anonymous Masking) |
| `GET` | `/api/courses/{id}/reviews/eligibility` | U3 | ตรวจสอบสิทธิ์ชั้นปีก่อนส่งรีวิว (`currentYear > recommendedYear`) |
| `POST` | `/api/courses/{id}/reviews` | U5, U6 | ส่งรีวิวใหม่ พร้อมคะแนนเกณฑ์ 5 ด้าน และโหมดปกปิดตัวตน |
| `PUT` | `/api/courses/{id}/reviews/{reviewId}` | U5, U6 | แก้ไขข้อความรีวิวของตนเอง |
| `GET` | `/api/users/preferences` | U7 | อ่านค่าความชอบในการเรียน 5 ด้านของผู้ใช้ |
| `PUT` | `/api/users/preferences` | U7 | บันทึก/อัปเดตความชอบในการเรียนของผู้ใช้ |
| `GET` | `/api/courses/{id}/sections/{secId}/match-score` | U7 | คำนวณความเข้ากันได้ (%) ระหว่าง Preference กับคะแนนเซกชัน |

> 📖 **ดูรายละเอียดการทำงานและ Data Flow:**  
> - [Sequence Diagrams (U1–U7)](../docs/architecture/sequence-diagrams.md)  
> - [Entity & Database Table Mapping](../docs/architecture/entity-database-table-mapping.md)
