# CSTU NEXUS

ระบบรีวิวรายวิชาสำหรับนักศึกษาสาขาวิทยาการคอมพิวเตอร์ (CSTU) มหาวิทยาลัยธรรมศาสตร์
ช่วยให้นักศึกษาค้นหาข้อมูลเชิงลึกของรายวิชา เพื่อประกอบการตัดสินใจลงทะเบียนได้อย่างมีประสิทธิภาพ

---

## สมาชิกกลุ่ม 10 — Section 100001

| ลำดับ | ชื่อ-นามสกุล | รหัสนักศึกษา |
|---|---|---|
| 1 | นางสาววิรมณ มาเกิด | 6809617407 |
| 2 | นายชิษณุพงศ์ คำเขื่อน | 6809617050 |
| 3 | นายชีวเทพ พากเพียร | 6809617068 |
| 4 | นายเธียรกุล จันทร์รุ่งเรือง | 6809540153 |
| 5 | นายธีรณัฏฐ์ ม่วงคราม | 6809617191 |
| 6 | นายธนภูมิ ธเนศนินาท | 6809681437 |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Backend | Java + Spring Boot |
| Database | Microsoft SQL Server |
| Authentication | TU Authentication API |
| Version Control | Git / GitLab |

---

## Features หลัก

1. **Structured Review** — รีวิวตามหัวข้อที่กำหนด (แนวการสอน, ปริมาณงาน, สื่อการเรียน ฯลฯ) พร้อมเลือกปิด/เปิดตัวตนได้
2. **TU Auth & Year Verification** — ยืนยันสิทธิ์ผ่าน TU Authentication; สิทธิ์การรีวิวกำหนดตามชั้นปี
3. **Section Matching** — ประมวลผล User Preferences คู่กับคะแนนรีวิว แสดงผลเป็น Match Percentage

---

## วิธีรัน Frontend (Development)

ยังไม่มี backend — เปิดไฟล์ผ่าน Live Server ได้เลย

```bash
# ใช้ VS Code + Live Server extension
# คลิกขวาที่ index.html → Open with Live Server

# หรือใช้ Python simple server
cd cstu-nexus/
python -m http.server 5500
# เปิด http://localhost:5500
```

**Mock credentials:** กรอก username/password อะไรก็ได้ (ยังไม่เชื่อม TU Auth)

---

## API Endpoints (TODO — Spring Boot)

จุดที่ frontend รอเชื่อมต่อ มี comment `// TODO:` ไว้ในโค้ดทุกจุด

| Method | Endpoint | คำอธิบาย |
|---|---|---|
| `POST` | `/api/auth/login` | TU Auth login |
| `GET` | `/api/courses` | ดึงรายวิชาทั้งหมด |
| `GET` | `/api/courses/{code}/reviews` | ดึงรีวิวของรายวิชา |
| `POST` | `/api/reviews` | ส่งรีวิวใหม่ |
| `GET` | `/api/users/me` | ดึงข้อมูล user ปัจจุบัน |
| `POST` | `/api/users/preferences` | บันทึก Preferences |

---

## License

Academic project — Thammasat University, Faculty of Science and Technology
