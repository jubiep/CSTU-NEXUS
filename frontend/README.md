# CSTU NEXUS — Frontend

ส่วนติดต่อผู้ใช้งาน (Client-side Web Application) สำหรับระบบสนับสนุนการตัดสินใจเลือกเรียนและรีวิวรายวิชา CSTU NEXUS

> 📖 สำหรับข้อมูลภาพรวมโปรเจกต์ และการรันฐานข้อมูล สามารถดูได้ที่ [Root README.md](../README.md)

---

## เทคโนโลยีที่ใช้ (Frontend Tech Stack)

- **HTML5:** Semantic Elements รองรับโครงสร้างหน้าเว็บที่เข้าถึงง่าย
- **CSS3:** Flexbox, CSS Grid, CSS Variables (`--tu-primary: #C3002F`, `--bg-color: #F8F9FA` ฯลฯ) และ Mobile-first Media Queries
- **JavaScript (Vanilla ES6+):** ควบคุม Dynamic DOM, การเรียก REST API, Modal Dialogs และ Navigation State
- **Icons & Assets:** Vector SVGs และ Image Assets จากดีไซน์ Figma

---

## โครงสร้างโฟลเดอร์ Frontend

```text
frontend/
├── pages/                    # ไฟล์หน้า HTML แต่ละหน้า
│   ├── index.html            # หน้าเข้าสู่ระบบ (Login)
│   ├── courses.html          # หน้ารายการวิชาทั้งหมด (Course Catalog)
│   ├── course-detail.html    # หน้ารายละเอียดวิชา รีวิว และ Section Matching
│   ├── write-review.html     # หน้าฟอร์มเขียนรีวิวรายวิชา
│   └── profile.html          # หน้าโปรไฟล์ผู้ใช้และตั้งค่า Preferences
│
├── css/                      # สไตล์ชีทแยกตามการใช้งาน
│   ├── main.css              # Global tokens, reset, typography และ Topbar component
│   ├── login.css             # สไตล์เฉพาะหน้า Login
│   ├── courses.css           # สไตล์หน้ารายการวิชาและการค้นหา/ตัวกรอง
│   ├── course-detail.css     # สไตล์หน้าคะแนนรีวิว สถิติ และ Accordion sections
│   ├── write-review.css      # สไตล์ฟอร์ม กรอกคะแนน และ Tag inputs
│   └── profile.css           # สไตล์หน้าโปรไฟล์ผู้ใช้
│
├── src/                      # รูปภาพ โลโก้ และไอคอนต่างๆ
├── app.js                    # API client และฟังก์ชันส่วนกลาง (apiRequest, requireAuth, logout ฯลฯ)
└── README.md                 # เอกสารแนะนำ Frontend เบื้องต้น
```

---

## วิธีการรัน Frontend สำหรับพัฒนา (Local Development)

Frontend ตัวนี้สร้างด้วย Vanilla HTML/CSS/JS บริสุทธิ์ จึงสามารถรันได้ทันทีโดยไม่ต้องติดตั้ง Node.js หรือ `npm install`

### วิธีที่ 1: ใช้ VS Code Live Server (แนะนำ)
1. ติดตั้งส่วนขยาย **Live Server** ใน VS Code
2. คลิกขวาที่ไฟล์ [`pages/index.html`](file:///C:/uni/CSTU-NEXUS/frontend/pages/index.html)
3. เลือก **"Open with Live Server"**

### วิธีที่ 2: ใช้ Python Simple HTTP Server
เปิด Terminal เข้ามาที่โฟลเดอร์ `frontend/`:
```bash
cd frontend
python -m http.server 5500
```
เปิดเบราว์เซอร์ไปที่: `http://localhost:5500/pages/index.html`

---

## ความสัมพันธ์ระหว่างหน้าเว็บและ Use Cases (U1–U5)

| หน้าเว็บ | Use Case ที่เกี่ยวข้อง | REST API ที่เชื่อมต่อ | บทบาทหน้าที่ |
|---|:---:|---|---|
| [`index.html`](file:///c:/uni/CSTU-NEXUS/frontend/pages/index.html) | **U2** | `POST /api/auth/login` | เข้าสู่ระบบด้วย TU Authentication และรับ Session Cookie |
| [`courses.html`](file:///c:/uni/CSTU-NEXUS/frontend/pages/courses.html) | **U4** | `GET /api/courses?keyword=&year=` | ค้นหาและคัดกรองรายวิชาตามชั้นปี |
| [`course-detail.html`](file:///c:/uni/CSTU-NEXUS/frontend/pages/course-detail.html) | **U1, U3, U4** | `GET /api/courses/{id}`<br>`GET /api/courses/{id}/reviews`<br>`GET /api/courses/{id}/reviews/eligibility` | แสดงข้อมูลเซกชัน อาจารย์ผู้สอน ประวัติรีวิวจากรุ่นพี่ และเช็คสิทธิ์ปุ่มเขียนรีวิว |
| [`write-review.html`](file:///c:/uni/CSTU-NEXUS/frontend/pages/write-review.html) | **U3, U5** | `GET /api/courses/{id}/reviews/eligibility`<br>`POST /api/courses/{id}/reviews` | ตรวจสอบสิทธิ์ชั้นปี และส่งฟอร์มรีวิววิชาแยกตาม Section |
| [`profile.html`](file:///c:/uni/CSTU-NEXUS/frontend/pages/profile.html) | **U2** | `GET /api/auth/me`<br>`POST /api/auth/logout` | แสดงข้อมูลโปรไฟล์นักศึกษา และออกจากระบบ |

> 📖 **ลำดับการทำงานและ Interaction:** ดูแผนผัง [Sequence Diagrams ฉบับสมบูรณ์ (U1–U5)](../docs/sequence-diagrams.md)

---

## Login, Profile, Courses และ Reviews API Contract

หน้าเว็บเรียก Backend จริงผ่าน Session Cookie แบบ HttpOnly (ปลอดภัย ไม่มีเก็บ token ลง local storage):

- เมื่อเปิด Frontend บน `localhost` หรือ `127.0.0.1` ค่า API เริ่มต้นจะใช้ hostname เดียวกันที่พอร์ต `8080` (เช่น `http://127.0.0.1:8080/api`) ไม่ขึ้นกับพอร์ตของ Frontend เพื่อให้ session cookie ใช้ได้ และตั้งค่าใหม่ได้โดยกำหนด `window.CSTU_API_BASE_URL` **ก่อน** โหลด `app.js`
- `POST /api/auth/login` (U2): รับ `{ "username": "6800000000", "password": "..." }` ยืนยันผ่าน TU AD Verify และสร้าง HttpOnly Session
- `GET /api/auth/me` (U2, U3): คืนข้อมูลผู้ใช้และชั้นปีปัจจุบัน (`currentYear`) เพื่อตรวจสิทธิ์ก่อนเปิดฟอร์มรีวิว
- `POST /api/auth/logout` (U2): ยกเลิก Session ฝั่ง Backend
- `GET /api/courses` (U4): ดึงรายการวิชา รองรับ Query Parameters `keyword` และ `year`
- `GET /api/courses/{id}` (U4): ดึงข้อมูลรายละเอียดวิชาและรายการ Section พร้อมอาจารย์ผู้สอน
- `GET /api/courses/{id}/reviews` (U1): อ่านประวัติรีวิว รองรับ Query Parameter `sectionId` (คืนค่า `reviewText`, `reviewerYear`, `createdAt`)
- `GET /api/courses/{id}/reviews/eligibility` (U3): ตรวจสอบว่าผู้ใช้มีสิทธิ์รีวิววิชานี้หรือไม่ (`studentYear > recommendedYear` และยังไม่เคยรีวิว)
- `POST /api/courses/{id}/reviews` (U5): ส่งข้อมูลรีวิวใหม่ `{ "sectionId": "...", "reviewText": "..." }`
- Frontend ใช้ `sessionStorage` เก็บเพียงสถานะนำทาง ไม่เก็บรหัสผ่านหรือ Application Key
- ค่า `year` คำนวณจากเลขสองหลักแรกของรหัสนักศึกษาเทียบกับปี พ.ศ. ปัจจุบัน (เช่น รหัสขึ้นต้น `68` ในปี พ.ศ. 2569 คือชั้นปี 2)

ตัวอย่างตั้ง API URL สำหรับ local development (วางก่อน `<script src="../app.js"></script>` ในหน้า):

```html
<script>window.CSTU_API_BASE_URL = "http://localhost:8080/api";</script>
```
ตั้ง `TU_API_APPLICATION_KEY` ในไฟล์ `.env` หรือ environment ของ **backend เท่านั้น** ก่อนรัน Spring Boot ดูขั้นตอนใน [คู่มือ Backend](../backend/README.md) ห้ามนำ Application Key ไปใส่ใน `app.js`, HTML หรือ commit ลง Git
