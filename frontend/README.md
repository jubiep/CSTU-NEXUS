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

## Login, Profile และ Courses API contract

หน้า Login และ Profile เรียก backend จริงผ่าน session cookie แบบ HttpOnly ไม่มี mock login หรือ token ที่เปิดอ่านได้จาก JavaScript

- เมื่อเปิด Frontend บน `localhost` หรือ `127.0.0.1` ค่า API เริ่มต้นจะใช้ hostname เดียวกันที่พอร์ต `8080` (เช่น `http://127.0.0.1:8080/api`) ไม่ขึ้นกับพอร์ตของ Frontend เพื่อให้ session cookie ใช้ได้ และตั้งค่าใหม่ได้โดยกำหนด `window.CSTU_API_BASE_URL` **ก่อน** โหลด `app.js`
- `POST /auth/login` รับ `{ "username": "6800000000", "password": "..." }` โดย `username` คือเลขนักศึกษา 10 หลักจากฟอร์ม login แล้ว backend ส่งเลขนั้นเป็น `UserName` ไป TU AD Verify พร้อม `Application-Key`; สำเร็จแล้ว backend สร้าง session และตอบ `{ "user": { ... } }`
- `GET /auth/me` ใช้ session cookie และตอบ `{ "displayName": "...", "studentId": "...", "year": 2, "faculty": "...", "department": "...", "email": "..." }`
- หน้า Courses เรียก `/auth/me` เช่นกัน เพื่อแสดงชื่อและรหัสนักศึกษาจากบัญชีที่ยืนยันแล้ว; ถ้าโหลดไม่สำเร็จจะแสดงข้อผิดพลาดและปุ่มลองใหม่แทนข้อมูล mock
- `POST /auth/logout` ยกเลิก session ฝั่ง backend
- Frontend ใช้ `sessionStorage` เก็บเพียงสถานะนำทาง ไม่เก็บรหัสผ่าน, Application Key หรือ bearer token; โปรไฟล์จริงอ่านจาก `/auth/me`
- หน้าที่ต้องเข้าสู่ระบบตรวจ session จริงด้วย `GET /auth/me` ก่อนเริ่มแสดงเนื้อหา; หาก API ตอบ `401` จะล้างสถานะใน `sessionStorage` และกลับไปหน้า Login
- หากย้อนกลับไปยังหน้าที่เบราว์เซอร์เก็บไว้หลังออกจากระบบ ระบบจะพากลับไปหน้า Login เมื่อหน้าถูกกู้คืนจาก cache
- Backend จำกัด CORS ด้วย `APP_CORS_ALLOWED_ORIGINS` และเปิด credentials สำหรับ session cookie
- ค่า `year` คำนวณจากเลขสองหลักแรกของรหัสนักศึกษาเทียบกับปี พ.ศ. ปัจจุบัน (เช่น รหัสขึ้นต้น `68` ในปี พ.ศ. 2569 คือชั้นปี 2); หากอ่านรหัสไม่ได้หรือเป็นปีในอนาคต จะส่ง `null`

ตัวอย่างตั้ง API URL สำหรับ local development (วางก่อน `<script src="../app.js"></script>` ในหน้า):

```html
<script>window.CSTU_API_BASE_URL = "http://localhost:8080/api";</script>
```
ตั้ง `TU_API_APPLICATION_KEY` ในไฟล์ `.env` หรือ environment ของ **backend เท่านั้น** ก่อนรัน Spring Boot ดูขั้นตอนใน [คู่มือ Backend](../backend/README.md) ห้ามนำ Application Key ไปใส่ใน `app.js`, HTML หรือ commit ลง Git
