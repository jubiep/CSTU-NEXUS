# CSTU-NEXUS — System Architecture Documentation

เอกสารอธิบายสถาปัตยกรรมระบบ **CSTU-NEXUS** (ระบบสนับสนุนการตัดสินใจเลือกเรียนและรีวิวรายวิชา CSTU) ครอบคลุมรูปแบบการออกแบบเชิงสถาปัตยกรรม (Architectural Pattern), การแบ่งเลเยอร์ (Layered Architecture), รูปแบบ Boundary-Control-Entity (BCE), ระบบรักษาความปลอดภัย, สถาปัตยกรรมข้อมูล และโครงสร้างการติดตั้งระบบ (Deployment Architecture)

---

## สารบัญ
1. [ภาพรวมสถาปัตยกรรมระบบ (System Architecture Overview)](#1-ภาพรวมสถาปัตยกรรมระบบ-system-architecture-overview)
2. [สถาปัตยกรรมแบบ 3-Tier Layered Architecture](#2-สถาปัตยกรรมแบบ-3-tier-layered-architecture)
3. [Boundary-Control-Entity (BCE) Pattern](#3-boundary-control-entity-bce-pattern)
4. [สถาปัตยกรรมการยืนยันตัวตนและความปลอดภัย (Security & Authentication Architecture)](#4-สถาปัตยกรรมการยืนยันตัวตนและความปลอดภัย-security--authentication-architecture)
5. [สถาปัตยกรรมข้อมูล (Data & Database Architecture)](#5-สถาปัตยกรรมข้อมูล-data--database-architecture)
6. [สถาปัตยกรรมการติดตั้งระบบ (Deployment & Container Architecture)](#6-สถาปัตยกรรมการติดตั้งระบบ-deployment--container-architecture)
7. [Tech Stack Specification](#7-tech-stack-specification)

---

## 1. ภาพรวมสถาปัตยกรรมระบบ (System Architecture Overview)

ระบบ CSTU-NEXUS ออกแบบโดยใช้หลักการ **Decoupled Architecture** แยกส่วนแสดงผล (Frontend) และส่วนประมวลผลเซิร์ฟเวอร์ (Backend) ออกจากกันอย่างเป็นอิสระ สื่อสารกันผ่าน **RESTful API (JSON over HTTP/HTTPS)** โดยมี Reverse Proxy (Nginx) คอยควบคุมทราฟฟิกและจัดการ Same-Origin Policy

```mermaid
flowchart TD
    subgraph Client["Client Tier (Browser)"]
        UI["Web Browser<br/>(HTML5 / CSS3 / Vanilla ES6+ JS)"]
    end

    subgraph Proxy["Proxy & Web Server"]
        Nginx["Nginx Reverse Proxy (Port 3000)<br/>- Static File Serving<br/>- API Gateway Routing (/api/*)"]
    end

    subgraph Application["Application Tier (Backend)"]
        SpringBoot["Spring Boot Application (Port 8080)<br/>- Spring Web MVC (Controllers)<br/>- Business Logic (Services)<br/>- Spring Data JPA / Spring JDBC<br/>- Session Management (HttpOnly Cookie)"]
    end

    subgraph Data["Data Tier"]
        Postgres[("PostgreSQL 17 Database (Port 5432)<br/>- courses & sections<br/>- mock_user & user_preferences<br/>- review")]
    end

    subgraph External["External Services"]
        TuAPI["TU Authentication API<br/>(https://restapi.tu.ac.th)"]
    end

    UI -->|"HTTP Request (Port 3000)"| Nginx
    Nginx -->|"Static Assets (.html, .css, .js)"| UI
    Nginx -->|"Proxy Pass /api/*"| SpringBoot
    SpringBoot -->|"REST API (AD Verify)"| TuAPI
    SpringBoot -->|"JDBC (SQL Queries)"| Postgres
```

---

## 2. สถาปัตยกรรมแบบ 3-Tier Layered Architecture

ระบบแบ่งออกเป็น 3 เลเยอร์หลักตามมาตรฐานระดับองค์กร:

### 2.1 Presentation Tier (Frontend)
- **โครงสร้าง:** Single Page / Multi-page Static Web pages พัฒนาด้วย Vanilla HTML5, CSS3 และ JavaScript (ES6+) โดยไม่พึ่งพา Frontend Framework ขนาดใหญ่ เพื่อประสิทธิภาพสูงสุด โหลดเร็ว และดูแลรักษาง่าย
- **หน้าที่:**
  - แสดงผล UI แบบ Mobile-first Responsive Design
  - รับข้อมูลจากผู้ใช้และตรวจสอบความถูกต้องเบื้องต้น (Client-side Validation)
  - เรียกใช้ REST API ผ่าน `fetch` API พร้อมแนบ Session Cookie
  - จัดการ State ของหน้าจอและการนำทาง (Navigation State)

### 2.2 Application Tier (Backend)
- **โครงสร้าง:** Spring Boot 4.1.x (Java 17) แบ่งเป็นเลเยอร์ย่อยภายใน:
  - **Controller Layer:** รับ HTTP Request, ตรวจสอบ Authentication Session, และส่ง Response แบบ JSON
  - **Service Layer:** ประมวลผล Business Logic เช่น การคำนวณชั้นปีจากรหัสนักศึกษา, การตรวจสอบสิทธิ์การรีวิว (Eligibility Rule: `currentYear > recommendedYear`), และอัลกอริทึมคำนวณ Match Score
  - **Data Access Layer (Repository / DAO):** เชื่อมโยงฐานข้อมูลผ่าน Spring Data JPA และ `JdbcTemplate`
- **หน้าที่:** ควบคุมกฎเกณฑ์ของระบบทั้งหมด (Business Integrity) โดยไม่พึ่งพาข้อมูลจาก Client เพียงอย่างเดียว

### 2.3 Data Tier (Database)
- **โครงสร้าง:** PostgreSQL 17 Relational Database รันบน Docker Container พร้อม persistent storage volume
- **หน้าที่:** จัดเก็บข้อมูลรายวิชา, เซกชัน, ข้อมูลผู้ใช้งานจำลอง, ความพึงพอใจในการเรียน (User Preferences), และข้อมูลการรีวิว

---

## 3. Boundary-Control-Entity (BCE) Pattern

ระบบประยุกต์ใช้แพตเทิร์น **BCE (Boundary, Control, Entity)** ตามมาตรฐานการวิเคราะห์และออกแบบระบบเชิงวัตถุ (Object-Oriented Analysis & Design):

| BCE Role | คอมโพเนนต์ในระบบ | รายละเอียดหน้าที่ |
|---|---|---|
| **Boundary** | `index.html`, `courses.html`, `course-detail.html`, `write-review.html`, `preference.html`, `profile.html`, `app.js` | ส่วนประสานงานระหว่างผู้ใช้กับระบบ รับอินพุต แสดงผล และแปลง Event ของผู้ใช้เป็นการเรียก API |
| **Control** | `AuthController`, `CourseController`, `ReviewController`, `PreferenceController`, `MatchScoreController`<br/>`TuAuthService`, `ReviewService`, `PreferenceService`, `MatchScoreService`, `StudyYearCalculator` | คลาสผู้ควบคุม ทำหน้าที่ประสานงานระหว่าง Boundary และ Entity ควบคุม Workflow, Business Rules และการคำนวณ |
| **Entity** | `AuthUser`, `CourseDetail`, `Section`, `Review`, `UserPreferences`, `MatchScoreResult` | ตัวแทนของข้อมูลเชิงธุรกิจ (Business Data) ที่คงอยู่ (Persisted) ในระบบหรือ Session |

---

## 4. สถาปัตยกรรมการยืนยันตัวตนและความปลอดภัย (Security & Authentication Architecture)

```mermaid
sequenceDiagram
    autonumber
    actor Student as นักศึกษา (Browser)
    participant Front as Frontend (app.js)
    participant Back as Backend (AuthController)
    participant TuSvc as TuAuthService
    participant TuAPI as TU AD Verify API
    participant Session as HttpSession

    Student->>Front: 1. กรอกรหัสนักศึกษา และรหัสผ่าน
    Front->>Back: 2. POST /api/auth/login
    Back->>TuSvc: 3. verify(username, password)
    TuSvc->>TuAPI: 4. HTTPS POST https://restapi.tu.ac.th/api/v1/auth/Ad/verify<br/>(แนบ TU_API_APPLICATION_KEY)
    TuAPI-->>TuSvc: 5. Response JSON (status, type, displayname, etc.)
    
    alt ตรวจสอบผ่าน (เป็นนักศึกษา CSTU)
        TuSvc-->>Back: 6. ข้อมูล AuthUser พร้อมคำนวณชั้นปี
        Back->>Session: 7. บันทึก AuthUser ลงใน Session
        Back-->>Front: 8. 200 OK (Set-Cookie: JSESSIONID, HttpOnly, SameSite=Lax)
        Front-->>Student: 9. นำทางเข้าสู่หน้า Courses
    else ตรวจสอบไม่ผ่าน / ไม่ใช่นักศึกษา CSTU
        Back-->>Front: 8. 401 Unauthorized หรือ 403 Forbidden
        Front-->>Student: 9. แจ้งเตือนข้อผิดพลาด
    end
```

### กลไกความปลอดภัยหลัก:
1. **Zero Secret on Frontend:** ตัวแปรลับ `TU_API_APPLICATION_KEY` จะถูกเก็บไว้ที่ Environment Variables ของเซิร์ฟเวอร์ Backend เท่านั้น ไม่มีการเปิดเผยสู่ Browser หรือโค้ดฝั่ง Client
2. **HttpOnly Session Cookies:** ไม่เก็บ JWT หรือ Token ใดๆ ลงใน `localStorage` เพื่อป้องกันการโจมตีประเภท Cross-Site Scripting (XSS)
3. **Role & Eligibility Enforcement:**
   - ตรวจสอบสิทธิ์ที่ฝั่ง Backend เสมอ (Server-side Validation)
   - สิทธิ์การเขียนรีวิว: ผู้ใช้ต้องล็อกอิน และต้องมีชั้นปีปัจจุบันสูงกว่าชั้นปีแนะนำของรายวิชานั้น (`currentYear > recommendedYear`) พร้อมป้องกันการส่งรีวิวซ้ำ
4. **Anonymous Review Privacy:** การรีวิวแบบปกปิดตัวตน (`isAnonymous = true`) ระบบจะ Mask รหัสนักศึกษาให้เหลือเฉพาะปีรุ่น (เช่น `68********`) เพื่อปกป้องความเป็นส่วนตัวของนักศึกษา

---

## 5. สถาปัตยกรรมข้อมูล (Data & Database Architecture)

ฐานข้อมูลได้รับการออกแบบในระดับ Third Normal Form (3NF) เพื่อลดความซ้ำซ้อนของข้อมูลและรักษาความถูกต้องสมบูรณ์ (Referential Integrity):

```mermaid
erDiagram
    MOCK_USER ||--o{ REVIEW : "writes"
    MOCK_USER ||--o| USER_PREFERENCES : "sets"
    COURSES ||--|{ SECTIONS : "contains"
    COURSES ||--o{ REVIEW : "receives"
    SECTIONS ||--o{ REVIEW : "has reviews"

    MOCK_USER {
        int user_id PK
        string student_id UK
        string display_name_th
        int current_year
        string faculty
        string department
        string email UK
    }

    COURSES {
        string course_id PK
        string course_name
        int recommended_year
    }

    SECTIONS {
        string section_id PK
        string course_id FK
        string section_number
        string instructor_name
    }

    REVIEW {
        int review_id PK
        string course_id FK
        string section_id FK
        int user_id FK
        int reviewer_year
        string review_text
        int teaching_method_score
        int evaluation_score
        int teaching_speed_score
        int assignment_style_score
        int workload_score
        boolean is_anonymous
        timestamp created_at
    }

    USER_PREFERENCES {
        int user_id PK
        int preferred_teaching_method
        int preferred_evaluation
        int preferred_teaching_speed
        int preferred_assignment_style
        int preferred_workload
    }
```

---

## 6. สถาปัตยกรรมการติดตั้งระบบ (Deployment & Container Architecture)

ระบบรองรับการรันผ่าน **Docker Compose** แบบ Multi-container Network:

```mermaid
flowchart LR
    subgraph Host["Host Machine"]
        Port3000["Port 3000"]
        Port8080["Port 8080 (Dev)"]
        Port5432["Port 5432 (Dev)"]
    end

    subgraph DockerNetwork["Docker Bridge Network (cstu-nexus-net)"]
        subgraph FrontContainer["Container: frontend"]
            NginxServ["Nginx Alpine"]
        end

        subgraph BackContainer["Container: backend"]
            SpringApp["Spring Boot (Java 17)"]
        end

        subgraph DBContainer["Container: db"]
            PostgresDB["PostgreSQL 17"]
            Volume[("Persistent Volume<br/>postgres_data")]
        end
    end

    Port3000 --> NginxServ
    Port8080 -.-> SpringApp
    Port5432 -.-> PostgresDB

    NginxServ -->|"proxy_pass http://backend:8080"| SpringApp
    SpringApp -->|"jdbc:postgresql://db:5432/cstu_nexus_db"| PostgresDB
    PostgresDB --- Volume
```

- **Reverse Proxy Advantage:** ผู้ใช้เข้าใช้งานผ่าน `http://localhost:3000` โดย Nginx จะ Forward คำขอ `/api/*` ไปยัง `http://backend:8080` อัตโนมัติ ทำให้หมดปัญหาเรื่อง Cross-Origin Resource Sharing (CORS) และ Cookies Domain
- **Data Persistence:** ใช้ Docker Named Volume `postgres_data` เพื่อให้ข้อมูลใน Database คงอยู่แม้สั่งปิดหรือ Restart Container

---

## 7. Tech Stack Specification

| ด้าน | เทคโนโลยี | เวอร์ชัน | เหตุผลความจำเป็นในการเลือกใช้ |
|---|---|---|---|
| **Frontend** | HTML5, CSS3, JavaScript ES6+ | Modern Standard | เบา โหลดเร็ว ไม่ต้อง build tool หรือ node_modules |
| **Reverse Proxy** | Nginx | Alpine Linux | เสิร์ฟ static assets และทำ reverse proxy เชื่อมต่อ Backend แบบ Same-Origin |
| **Backend** | Java, Spring Boot | Java 17, Spring Boot 4.1.x | Robust Enterprise Framework รองรับ MVC, JPA, Connection Pool |
| **Database** | PostgreSQL | 17 | Relational Database ประสิทธิภาพสูง รองรับ ACID และ Migration Scripts |
| **Containerization** | Docker, Docker Compose | v2+ | ควบคุมสภาพแวดล้อมการทำงานให้เหมือนกันทุกคนในทีม |
| **External API** | Thammasat AD Verify API | v1 | ตรวจสอบสถานะการเป็นนักศึกษาจริงของมหาวิทยาลัยธรรมศาสตร์ |
