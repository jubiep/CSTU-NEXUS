# CSTU NEXUS — Frontend

ส่วนติดต่อผู้ใช้งาน (Client-side Web Application) สำหรับระบบสนับสนุนการตัดสินใจเลือกเรียนและรีวิวรายวิชา CSTU NEXUS

> 📖 **ภาพรวมระบบและสถาปัตยกรรม:** ดูได้ที่ [docs/architecture.md](../docs/architecture.md) และ [Root README.md](../README.md)

---

## Tech Stack
- **HTML5:** Semantic Elements สำหรับโครงสร้างเว็บ
- **CSS3:** Flexbox, CSS Grid, CSS Variables (`--tu-primary: #C3002F`) และ Mobile-first Responsive Design
- **JavaScript (Vanilla ES6+):** ควบคุม DOM, Event Handling, และเรียกใช้ REST API ผ่าน `fetch`
- **Reverse Proxy:** Nginx (เสิร์ฟ static files และ reverse proxy คำขอ `/api/*` ไปยัง Backend)

---

## โครงสร้างโฟลเดอร์ Frontend

```text
frontend/
├── pages/                    # หน้าเว็บ HTML
│   ├── index.html            # หน้าเข้าสู่ระบบ (Login)
│   ├── courses.html          # หน้ารายการวิชาทั้งหมดและการค้นหา (Course Catalog)
│   ├── course-detail.html    # หน้ารายละเอียดวิชา รีวิว และ Section Match Score
│   ├── write-review.html     # หน้าฟอร์มเขียนรีวิว ให้คะแนน 5 ด้าน และโหมด Anonymous
│   ├── preference.html       # หน้าตั้งค่าความชอบในการเรียน (Learning Preferences)
│   └── profile.html          # หน้าโปรไฟล์ผู้ใช้และออกจากระบบ
├── css/                      # สไตล์ชีทแยกตามแต่ละหน้าเว็บ
├── src/                      # รูปภาพ โลโก้ และไอคอน
├── app.js                    # API client และฟังก์ชันส่วนกลาง (apiRequest, requireAuth, Topbar)
└── nginx.conf.template       # การตั้งค่า Nginx สำหรับ Reverse Proxy บน Docker
```

---

## วิธีการรัน Frontend

### วิธีที่ 1: รันทั้งระบบผ่าน Docker Compose (แนะนำ)
จาก root ของ repository:
```bash
docker compose --env-file .env up --build
```
เปิด Browser ที่ **`http://localhost:3000`** (Nginx จะ proxy `/api` ไปยัง Backend อัตโนมัติ จึงไม่ต้องตั้งค่า CORS)

### วิธีที่ 2: ใช้ VS Code Live Server (สำหรับ Local Dev)
1. ติดตั้ง Extension **Live Server** ใน VS Code
2. คลิกขวาที่ไฟล์ `frontend/pages/index.html` → เลือก **"Open with Live Server"**
3. หน้าเว็บจะเปิดที่ `http://localhost:5500/pages/index.html`

### วิธีที่ 3: ใช้ Python Simple HTTP Server
```bash
cd frontend
python -m http.server 5500
```
เปิดเบราว์เซอร์ไปที่: `http://localhost:5500/pages/index.html`

---

## ความสัมพันธ์ระหว่างหน้าเว็บและ Use Cases

| หน้าเว็บ | Use Case | REST API ที่เชื่อมต่อ | บทบาทหน้าที่ |
|---|:---:|---|---|
| `pages/index.html` | **U2** | `POST /api/auth/login` | เข้าสู่ระบบด้วย TU Authentication และรับ Session Cookie |
| `pages/courses.html` | **U4** | `GET /api/courses?keyword=&year=` | ค้นหาและคัดกรองรายวิชาตามชั้นปี |
| `pages/course-detail.html` | **U1, U3, U4, U7** | `GET /api/courses/{id}`<br>`GET /api/courses/{id}/reviews`<br>`GET /api/courses/{id}/reviews/eligibility`<br>`GET /api/courses/{id}/sections/{secId}/match-score` | แสดงรายละเอียดวิชา, รายชื่อเซกชัน, สถิติรีวิว, Match Score %, และตรวจสอบสิทธิ์เขียนรีวิว |
| `pages/write-review.html` | **U3, U5, U6** | `GET /api/courses/{id}/reviews/eligibility`<br>`POST /api/courses/{id}/reviews` | ตรวจสอบสิทธิ์ชั้นปี และส่งฟอร์มรีวิวพร้อมเกณฑ์ 5 ด้าน และตัวเลือกปิดบังตัวตน |
| `pages/preference.html` | **U7** | `GET /api/users/preferences`<br>`PUT /api/users/preferences` | โหลดและบันทึกความชอบในการเรียน 5 ด้านของผู้ใช้ |
| `pages/profile.html` | **U2** | `GET /api/auth/me`<br>`POST /api/auth/logout` | แสดงข้อมูลโปรไฟล์นักศึกษา และออกจากระบบ |

---

## การตั้งค่า API Base URL สำหรับ Local Development

กรณีเปิดหน้าเว็บผ่าน Live Server (พอร์ต 5500) สามารถระบุ URL ชี้ไปยัง Backend Spring Boot (พอร์ต 8080) ได้โดยใส่ Script ไว้ก่อนโหลด `app.js`:

```html
<script>window.CSTU_API_BASE_URL = "http://localhost:8080/api";</script>
<script src="../app.js"></script>
```
*(หากรันผ่าน Docker Compose บนพอร์ต 3000 ระบบจะใช้ same-origin `/api` อัตโนมัติ)*
