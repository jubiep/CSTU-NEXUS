# CSTU NEXUS — Frontend

ส่วนติดต่อผู้ใช้งาน (Client-side Web Application) สำหรับระบบสนับสนุนการตัดสินใจเลือกเรียนและรีวิวรายวิชา CSTU NEXUS

> 📖 สำหรับข้อมูลภาพรวมโปรเจกต์ และการรันฐานข้อมูล สามารถดูได้ที่ [Root README.md](../README.md)

---

## เทคโนโลยีที่ใช้ (Frontend Tech Stack)

- **HTML5:** Semantic Elements รองรับโครงสร้างหน้าเว็บที่เข้าถึงง่าย
- **CSS3:** Flexbox, CSS Grid, CSS Variables (`--tu-primary: #C3002F`, `--bg-color: #F8F9FA` ฯลฯ) และ Mobile-first Media Queries
- **JavaScript (Vanilla ES6+):** ควบคุม Dynamic DOM, Mock Authentication, Modal Dialogs และ Navigation State
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
├── app.js                    # ฟังก์ชัน Client-side รวม (renderTopbar, doLogin, doLogout ฯลฯ)
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

## ข้อมูลจำลองสำหรับการทดสอบ (Mock Data & Login)

- **Login Bypass:** ในช่วงการพัฒนายังไม่ได้ต่อ TU Authentication API จริง สามารถกรอกอีเมลและรหัสผ่านใดๆ แล้วกด **Log in** เพื่อเข้าใช้งานระบบได้ทันที
- **Session State:** จัดเก็บสถานะการล็อกอินผ่าน `sessionStorage` (`cstu_logged_in`)
- **API Integration Point:** มีการใส่คอมเมนต์ `// TODO:` กำกับไว้ใน `app.js` และหน้าต่างๆ สำหรับจุดที่รอเชื่อมต่อ REST API กับ Spring Boot Backend
