# CSTU NEXUS

ระบบสนับสนุนการตัดสินใจเลือกเรียนและรีวิวรายวิชาสำหรับนักศึกษาสาขาวิทยาการคอมพิวเตอร์ (CSTU) มหาวิทยาลัยธรรมศาสตร์ ช่วยให้นักศึกษาค้นหาข้อมูลเชิงลึกของรายวิชา วางแผนการเรียน และอ่าน/เขียนรีวิวประสบการณ์เพื่อประกอบการตัดสินใจลงทะเบียน

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

## เอกสารการออกแบบและข้อกำหนดของระบบ (Documentation)

เอกสารทางเทคนิคและสถาปัตยกรรมทั้งหมดจัดเก็บอยู่ในโฟลเดอร์ [`docs/`](docs/):

| เอกสาร | ลิงก์ | คำอธิบาย |
|---|---|---|
| **System Architecture** | [docs/architecture/architecture.md](docs/architecture/architecture.md) | สถาปัตยกรรมระบบ 3-Tier, BCE Pattern, ความปลอดภัย, และ Deployment |
| **User Stories** | [docs/requirements/user-stories.md](docs/requirements/user-stories.md) | ข้อกำหนดความต้องการแบบ Agile (Persona, Want, Value, Acceptance Criteria) |
| **Use Case Specifications** | [docs/requirements/use-cases.md](docs/requirements/use-cases.md) | รายละเอียดข้อกำหนด Use Case ทั้งหมด (U1–U9) พร้อม Pre/Post-conditions |
| **Use Case & Class Diagrams** | [docs/architecture/use-case-and-class-diagrams.md](docs/architecture/use-case-and-class-diagrams.md) | แผนภาพ Use Case Diagram (U1–U9) และ Design Class Diagram (BCE) |
| **Sequence Diagrams** | [docs/architecture/sequence-diagrams.md](docs/architecture/sequence-diagrams.md) | แผนภาพลำดับการทำงาน U1–U9 ทั้งหมด |
| **Database & ER Mapping** | [docs/architecture/entity-database-table-mapping.md](docs/architecture/entity-database-table-mapping.md) | แผนผัง ER Diagram, ตารางฐานข้อมูล, API-DAO Mapping, และสิทธิ์ |
| **API Specifications** | [docs/api/](docs/api/) | ข้อกำหนด API สำหรับการตั้งค่าความชอบ, Match Score และรีวิวแบบปิดบังตัวตน |
| **Recommended Diagrams** | [docs/architecture/recommended-diagrams.md](docs/architecture/recommended-diagrams.md) | แผนภาพแนะนำเพิ่มเติม (Component, Activity, และ State Machine Diagrams) |
| **Documentation Index** | [docs/README.md](docs/README.md) | สารบัญรวมเอกสารทั้งหมดจัดหมวดหมู่อย่างเป็นระเบียบ |

---

## ฟีเจอร์หลัก (Key Features)

1. **TU Authentication & Year Verification** — ยืนยันตัวตนผ่าน TU AD Verify API ตรวจสอบสิทธิ์เฉพาะนักศึกษา CSTU และคำนวณชั้นปีอัตโนมัติ
2. **Course & Section Search** — ค้นหารายวิชา กรองตามชั้นปีแนะนำ และดูข้อมูลรายชื่อเซกชันพร้อมอาจารย์ผู้สอน
3. **Structured Course Review & Privacy** — อ่านรีวิวจากรุ่นพี่ และเขียนรีวิวพร้อมประเมินเกณฑ์มาตรฐาน 5 ด้าน โดยเลือกเปิดเผยหรือปิดบังตัวตน (Anonymous) ได้
4. **Learning Preferences & Match Scoring** — กำหนดสไตล์การเรียนและ Workload ของตนเอง พร้อมแสดงเปอร์เซ็นต์ความเข้ากันได้ (Match Score %) กับแต่ละเซกชัน

---

## ขั้นตอนการติดตั้งและรันระบบ (Quick Start)

### 1. สิ่งที่ต้องมีในเครื่อง (Prerequisites)
- [Git](https://git-scm.com/)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Java JDK 17](https://adoptium.net/temurin/releases/?version=17) *(กรณีต้องการรัน Backend แบบ Local)*

> 💡 ดูคู่มือเตรียมเครื่องและติดตั้งโปรแกรมแบบละเอียดได้ที่ [REQUIREMENTS.md](REQUIREMENTS.md)

---

### 2. ตั้งค่า Environment Variables (`.env`)

คัดลอกไฟล์ `.env.example` เป็น `.env` ที่ root ของโปรเจกต์:

```bash
# บน Windows PowerShell
copy .env.example .env

# บน macOS / Linux
cp .env.example .env
```

เปิดไฟล์ `.env` แล้วใส่ `TU_API_APPLICATION_KEY` สำหรับเรียกใช้ TU Authentication API:

```env
TU_API_APPLICATION_KEY=your_actual_key_here
```

---

### 3. รันทั้งระบบด้วย Docker Compose (แนะนำ)

สั่ง build และรันทั้ง Frontend (Nginx), Backend (Spring Boot) และ Database (PostgreSQL 17) ด้วยคำสั่งเดียว:

```bash
docker compose --env-file .env up --build
```

- เปิด Browser ไปที่: **`http://localhost:3000`**
- ตรวจสอบสถานะการทำงาน: `docker compose ps`
- ดู Log การทำงาน: `docker compose logs -f`
- หยุดระบบ: `docker compose down`

---

### 4. ทางเลือกรันแบบ Local Development (แยกส่วน)

#### 4.1 เริ่มต้น Database (PostgreSQL 17)
```bash
docker compose -f backend/database/compose.yaml up -d db
```
*(ตัวเลือก) นำเข้า Seed Data ครั้งแรก:*
```bash
docker exec -i cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db -v ON_ERROR_STOP=1 < backend/database/seed.sql
```

#### 4.2 รัน Spring Boot Backend
```bash
cd backend/cstu-course-nexus
# บน Windows
.\mvnw.cmd spring-boot:run

# บน macOS / Linux
./mvnw spring-boot:run
```
Backend พร้อมทำงานที่ `http://localhost:8080`

#### 4.3 รัน Frontend
เปิดไฟล์ `frontend/pages/index.html` ผ่าน **VS Code Live Server** (พอร์ต 5500) หรือรันด้วย Python:
```bash
cd frontend
python -m http.server 5500
```
เปิด Browser ที่ `http://localhost:5500/pages/index.html`

---

## โครงสร้างโปรเจกต์โดยสังเขป (Project Structure)

```text
CSTU-NEXUS/
├── docs/                      # เอกสารสถาปัตยกรรม, Use Cases, User Stories และ UML Diagrams
├── frontend/                  # ฝั่งหน้าเว็บผู้ใช้งาน (HTML5, CSS3, Vanilla JS, Nginx Reverse Proxy)
│   ├── pages/                 # หน้าเว็บ (index, courses, course-detail, write-review, preference, profile)
│   ├── css/                   # สไตล์ชีทแยกตามหน้า
│   └── app.js                 # Logic ส่วนกลางและการเชื่อมต่อ API
├── backend/                   # ฝั่งเซิร์ฟเวอร์และฐานข้อมูล
│   ├── cstu-course-nexus/     # Spring Boot Source Code (Java 17, Controllers, Services, Repositories)
│   └── database/              # Schema (init.sql), Seed Data (seed.sql), และ Migrations
├── docker-compose.yml         # การติดตั้งระบบรวมทุก Service (Frontend, Backend, Database)
├── REQUIREMENTS.md            # คู่มือเตรียมเครื่องและติดตั้ง Tech Stack
└── README.md                  # เอกสารแนะนำและคู่มือการเริ่มต้น
```

---

## สิทธิ์การใช้งาน (License)

Academic project for **CS261 / CS264 Introduction to Software Engineering** — Thammasat University, Faculty of Science and Technology, Department of Computer Science.
