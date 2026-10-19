# CSTU NEXUS

ระบบสนับสนุนการตัดสินใจเลือกเรียนและรีวิวรายวิชาสำหรับนักศึกษาสาขาวิทยาการคอมพิวเตอร์ (CSTU) มหาวิทยาลัยธรรมศาสตร์
ช่วยให้นักศึกษาค้นหาข้อมูลเชิงลึกของรายวิชา วางแผนการเรียน และรีวิวประสบการณ์เพื่อประกอบการตัดสินใจลงทะเบียนได้อย่างมีประสิทธิภาพ

> **หมายเหตุ:** โปรเจกต์นี้เป็นส่วนหนึ่งของรายวิชา **CS261 / CS264 Introduction to Software Engineering**  
> ภาควิชาวิทยาการคอมพิวเตอร์ คณะวิทยาศาสตร์และเทคโนโลยี มหาวิทยาลัยธรรมศาสตร์

---

## สมาชิกกลุ่ม 10 — Section 100001

| ลำดับ | ชื่อ-นามสกุล | รหัสนักศึกษา |
|:---:|---|:---:|
| 1 | นางสาววิรมณ มาเกิด | 6809617407 |
| 2 | นายชิษณุพงศ์ คำเขื่อน | 6809617050 |
| 3 | นายชีวเทพ พากเพียร | 6809617068 |
| 4 | นายเธียรกุล จันทร์รุ่งเรือง | 6809540153 |
| 5 | นายธีรณัฏฐ์ ม่วงคราม | 6809617191 |
| 6 | นายธนภูมิ ธเนศนินาท | 6809681437 |

---

## Tech Stack ปัจจุบัน

| Layer | Technology | รายละเอียด |
|---|---|---|
| **Frontend** | HTML5, CSS3, Vanilla JavaScript (ES6+) | Mobile-first Responsive Design, TU Red (`#C3002F`) Theme |
| **Backend** | Java 17, Spring Boot 4.1.x | Spring Web MVC, Spring JDBC (`JdbcTemplate`), Lombok |
| **Database** | PostgreSQL 17 | รันบน Docker Container (Docker Compose) |
| **Build & Tooling** | Maven Wrapper (`./mvnw`), Docker Desktop | รองรับการรันโดยไม่ต้องติดตั้ง Maven แยก |
| **Authentication** | TU Authentication API | ตรวจสอบสถานะนักศึกษาและชั้นปี (1–4) |
| **Version Control** | Git / GitHub (Monorepo) | จัดการโค้ด Frontend และ Backend ร่วมกันในคลังเดียว |

---

## โครงสร้างโปรเจกต์ (Monorepo Structure)

```text
CSTU-NEXUS/
├── frontend/                     # ฝั่งหน้าเว็บผู้ใช้งาน
│   ├── pages/                    # ไฟล์หน้าเว็บ HTML แต่ละหน้า
│   │   ├── index.html            # หน้าเข้าสู่ระบบ (Login)
│   │   ├── courses.html          # หน้ารายการวิชาทั้งหมด (Course List)
│   │   ├── course-detail.html    # หน้ารายละเอียดวิชาและรีวิว (Course Detail)
│   │   ├── write-review.html     # หน้าเขียนรีวิววิชา (Write Review)
│   │   └── profile.html          # หน้าโปรไฟล์ผู้ใช้และตั้งค่า (Profile / Preferences)
│   ├── css/                      # สไตล์ชีทแยกตามโมดูลและคอมโพเนนต์
│   │   ├── main.css              # Global styles, variables, typography, topbar
│   │   ├── login.css             # สไตล์หน้า Login
│   │   ├── courses.css           # สไตล์หน้ารายการวิชา
│   │   ├── course-detail.css     # สไตล์หน้ารายละเอียดและ Section Matching
│   │   ├── write-review.css      # สไตล์หน้าฟอร์มเขียนรีวิว
│   │   └── profile.css           # สไตล์หน้าโปรไฟล์ผู้ใช้งาน
│   ├── src/                      # รูปภาพ โลโก้ และ Asset มีเดีย
│   ├── app.js                    # ฟังก์ชันหลักฝั่ง Client (API integration, Auth state, Topbar)
│   └── README.md                 # คู่มือการใช้งาน Frontend
│
├── backend/                      # ฝั่งเซิร์ฟเวอร์และฐานข้อมูล
│   ├── cstu-course-nexus/        # Spring Boot Application
│   │   ├── src/main/java/        # Java Source Code
│   │   │   └── th/ac/tu/cs/cstu_course_nexus/
│   │   │       ├── CstuCourseNexusApplication.java  # Main Application + .env Auto-loader
│   │   │       ├── HealthController.java            # ตรวจสอบสถานะและเชื่อมต่อ DB
│   │   │       ├── AuthController.java              # Login, Profile, Logout session
│   │   │       ├── AuthUser.java                    # Model ข้อมูลผู้ใช้ใน session
│   │   │       ├── StudyYearCalculator.java         # คำนวณชั้นปีจากรหัสนักศึกษา
│   │   │       ├── TuAuthService.java               # ยืนยันตัวตนกับ TU AD Verify API
│   │   │       └── WebConfig.java                   # CORS และ Timeout ของ RestClient
│   │   ├── src/main/resources/
│   │   │   └── application.properties # การตั้งค่าเชื่อมต่อ Database และ Spring Boot
│   │   ├── pom.xml               # รายการ Dependencies และการตั้งค่า Maven
│   │   └── mvnw / mvnw.cmd       # Maven Wrapper
│   ├── database/                 # การตั้งค่าฐานข้อมูล PostgreSQL และ Seed Data
│   │   ├── compose.yaml          # Docker Compose สำหรับรัน PostgreSQL 17 และ Backend
│   │   ├── init.sql              # Schema โครงสร้างตาราง (mock_user, courses, sections, review)
│   │   ├── seed.sql              # ชุดข้อมูลตั้งต้น (38 รายวิชา, 63 เซกชัน)
│   │   ├── courses.csv           # ไฟล์ข้อมูลวิชาตั้งต้น
│   │   ├── sections.csv          # ไฟล์ข้อมูลเซกชันตั้งต้น
│   │   ├── generate-seed.ps1     # สคริปต์แปลง CSV สู่ seed.sql
│   │   └── migrations/           # ประวัติการปรับปรุง Database Schema
│   └── README.md                 # คู่มือการตั้งค่า Backend & Database ฉบับสมบูรณ์
│
├── .env.example                  # แม่แบบการตั้งค่า Environment Variables
├── .gitignore                    # ละเว้นไฟล์ที่ไม่ควรขึ้น Git (.env, target/, build artifacts)
└── README.md                     # เอกสารแนะนำและคู่มือโปรเจกต์ฉบับหลัก
```

---

## Features หลัก

1. **Structured Course Review** — รีวิววิชาตามหัวข้อเกณฑ์มาตรฐาน (แนวการสอน, สัดส่วนคะแนน, ปริมาณงาน, สื่อการเรียน) และเลือกเปิดเผยหรือปิดบังตัวตนได้
2. **TU Auth & Year Verification** — ยืนยันสิทธิ์นักศึกษาผ่าน TU Authentication API และคำนวณชั้นปีอัตโนมัติจากรหัสนักศึกษา พร้อมเก็บ Session Cookie แบบ HttpOnly ปลอดภัย
3. **Section Matching Algorithm** — คำนวณความเข้ากันได้ระหว่างความชอบของนักศึกษา (User Preferences) กับคะแนนรีวิวของแต่ละเซกชัน แสดงเป็น Match Percentage (%)
4. **Interactive Course Exploration** — ค้นหา คัดกรองตามชั้นปีที่แนะนำ และดูสถิติรีวิวของรายวิชาต่าง ๆ ในหลักสูตร CSTU

---

## ขั้นตอนการติดตั้งและรันระบบ (Quick Start)

### 1. ความต้องการของระบบ (Prerequisites)
- [Git](https://git-scm.com/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (สำหรับรัน PostgreSQL)
- [Java JDK 17](https://adoptium.net/temurin/releases/?version=17) ขึ้นไป

---

### 2. ตั้งค่าไฟล์สภาพแวดล้อม (`.env`)

คัดลอกไฟล์แม่แบบ `.env.example` เป็น `.env` ที่ root ของโปรเจกต์:

```bash
# บน Windows PowerShell
copy .env.example .env

# บน Linux / macOS / Git Bash
cp .env.example .env
```

เปิดไฟล์ `.env` แล้วใส่ `TU_API_APPLICATION_KEY` ที่ได้รับจากผู้ดูแลระบบ TU API:

```env
TU_API_APPLICATION_KEY=your_actual_key_here
APP_CORS_ALLOWED_ORIGINS=http://localhost:5500,http://127.0.0.1:5500,http://localhost:5501,http://127.0.0.1:5501
```

*(ไฟล์ `.env` นี้จะไม่ถูกนำขึ้น Git เนื่องจากมี `.gitignore` ดักไว้แล้ว)*

---

### 3. เริ่มต้น Database (PostgreSQL 17 บน Docker)

1. เปิด **Docker Desktop**
2. เปิด Terminal ที่โฟลเดอร์หลักของโปรเจกต์ แล้วสั่งเริ่ม PostgreSQL Container:

```bash
docker compose -f backend/database/compose.yaml up -d db
```

3. ตรวจสอบสถานะ Container:
```bash
docker compose -f backend/database/compose.yaml ps
```
*(ฐานข้อมูลจะเปิดพอร์ต `5432` สำหรับ Database `cstu_nexus_db`)*

4. (ตัวเลือก) นำเข้า Seed Data ตั้งต้น (หากเพิ่งสร้าง Database ครั้งแรก):
```bash
docker exec -i cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db -v ON_ERROR_STOP=1 < backend/database/seed.sql
```

---

### 4. รัน Spring Boot Backend

#### ทางเลือกที่ A: รันผ่าน Maven Wrapper โดยตรง (แนะนำสำหรับ Local Dev)
1. ไปที่โฟลเดอร์ Backend:
```bash
cd backend/cstu-course-nexus
```

2. รันแอปพลิเคชัน (แอปจะโหลดค่า Key จากไฟล์ `.env` ที่ root อัตโนมัติ):
```bash
# บน Windows (PowerShell / Command Prompt)
.\mvnw.cmd spring-boot:run

# บน Linux / macOS / Git Bash
./mvnw spring-boot:run
```
Backend จะพร้อมทำงานที่ `http://localhost:8080`

#### ทางเลือกที่ B: รันทั้ง Backend และ Database ผ่าน Docker Compose
```bash
docker compose --env-file .env -f backend/database/compose.yaml up -d --build
```

---

### 5. รัน Frontend

Frontend ถูกพัฒนาด้วย Vanilla HTML/CSS/JS บริสุทธิ์ สามารถเปิดได้ทันที:

#### วิธีที่ 1: ใช้ Live Server (VS Code Extension)
- คลิกขวาที่ไฟล์ `frontend/pages/index.html` → เลือก **Open with Live Server**

#### วิธีที่ 2: ใช้ Python Simple HTTP Server
```bash
cd frontend/
python -m http.server 5500
```
เปิดเบราว์เซอร์ไปที่: `http://localhost:5500/pages/index.html` (หรือ `http://127.0.0.1:5500/pages/index.html`)

---

## API Endpoints (Spring Boot)

| Method | Endpoint | คำอธิบาย | สถานะ |
|---|---|---|:---:|
| `GET` | `/health` | ตรวจสอบสถานะการทำงานของ Backend | พร้อมใช้งาน |
| `GET` | `/db-test` | ทดสอบ Query ตาราง courses จาก PostgreSQL | พร้อมใช้งาน |
| `POST` | `/echo` | ทดสอบรับ-ส่ง String Request Body | พร้อมใช้งาน |
| `POST` | `/api/auth/login` | ยืนยันบัญชีนักศึกษา TU และสร้าง HttpOnly Session | พร้อมใช้งาน |
| `GET` | `/api/auth/me` | ดึงข้อมูลโปรไฟล์ผู้ใช้ปัจจุบันจาก Session | พร้อมใช้งาน |
| `POST` | `/api/auth/logout` | ยกเลิก Session ผู้ใช้ | พร้อมใช้งาน |
| `GET` | `/api/courses` | ดึงรายชื่อและข้อมูลวิชาทั้งหมด | อยู่ระหว่างพัฒนา |
| `GET` | `/api/courses/{code}/reviews` | ดึงประวัติรีวิวของรายวิชานั้น ๆ | อยู่ระหว่างพัฒนา |
| `POST` | `/api/reviews` | บันทึกรีวิวรายวิชาใหม่ลงฐานข้อมูล | อยู่ระหว่างพัฒนา |
| `GET` | `/api/users/me` | ดึงข้อมูลโปรไฟล์ผู้ใช้ปัจจุบัน | อยู่ระหว่างพัฒนา |
| `POST` | `/api/users/preferences` | บันทึกการตั้งค่า Preferences การเรียนของผู้ใช้ | อยู่ระหว่างพัฒนา |

---

## Database Schema เบื้องต้น

- `mock_user` : ข้อมูลผู้ใช้งาน, รหัสนักศึกษา, ชื่อ, ชั้นปี, สาขา, อีเมล
- `courses` : รหัสวิชา (`course_id`), ชื่อวิชา (`course_name`), ชั้นปีที่แนะนำ (`recommended_year`)
- `sections` : ข้อมูลเซกชัน, อาจารย์ผู้สอน เชื่อมโยงกับ `courses`
- `review` : รีวิวรายวิชา คะแนน และข้อมูลผู้เขียนรีวิว

---

## สิทธิ์การใช้งาน (License)

Academic project for **CS261 / CS264 Introduction to Software Engineering** — Thammasat University, Faculty of Science and Technology, Department of Computer Science.