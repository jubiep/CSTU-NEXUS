# Test Cases: การเข้าสู่ระบบและการยืนยันตัวตน

วันที่จัดทำ: 12 พฤศจิกายน 2569  
ขอบเขต: การยืนยันตัวตนผ่าน TU Auth, การจำกัดสิทธิ์นักศึกษา CSTU, session, logout และการกลับหน้าก่อนหน้าหลัง logout  
แหล่งอ้างอิง: backend TuAuthService, AuthController, TuAuthServiceTests, AuthControllerTests; frontend pages/index.html, app.js; Playwright tests/auth.spec.js

## ขอบเขตฟังก์ชันที่เกี่ยวข้อง

| รหัส | ฟังก์ชัน | พฤติกรรมที่ตรวจ |
|---|---|---|
| F1 | เข้าสู่ระบบ | ตรวจข้อมูลกับ TU Auth และสร้าง session เมื่อผ่าน |
| F2 | จำกัดสิทธิ์ CSTU | อนุญาตรหัสนักศึกษาที่ขึ้นต้น 6809; ปฏิเสธรหัสที่ไม่ตรงก่อนสร้าง session |
| F3 | จัดการ session | อ่านข้อมูลผู้ใช้จาก session และยกเลิก session เมื่อ logout |
| F4 | แสดงผลการยืนยันตัวตน | แสดงข้อความกรณีข้อมูลผิด, ไม่มีสิทธิ์ หรือ TU Auth ใช้งานไม่ได้ |
| F5 | ป้องกันข้อมูลหลัง logout | เมื่อย้อนกลับไปหน้าที่ต้อง login ต้องตรวจ session ใหม่และไม่แสดงข้อมูลผู้ใช้ |

## ข้อมูลทดสอบและเงื่อนไขตั้งต้น

- ใช้ข้อมูลจำลองสำหรับ TU Auth และ API ใน automated tests; ไม่ส่งคำขอด้วยบัญชี TU จริง
- Prefix CSTU ที่ตั้งค่าในปัจจุบันคือ 6809; เลขตัวอย่าง 6809000000 ใช้ทดสอบกรณีผ่าน และ 6808000000 ใช้ทดสอบกรณีถูกปฏิเสธ
- Playwright ใช้ frontend ที่ http://localhost:5500 และ intercept คำขอ API; ผล Playwright ยืนยันพฤติกรรม frontend ตาม response จำลอง ไม่ใช่การเชื่อมกับ TU Auth จริง
- สถานะ “ผ่าน” หมายถึงมีผลรัน test ตามรายการนี้; “ครอบคลุมบางส่วน” หมายถึงผ่านเฉพาะ backend หรือ frontend mock แต่ยังขาดเงื่อนไข acceptance บางข้อ; “ยังไม่ทดสอบ” หมายถึงไม่มี automated test สำหรับพฤติกรรมนั้น

## Test Cases

| ID | ฟังก์ชัน | ประเภท | ขั้นตอน / ข้อมูลทดสอบ | ผลที่คาดหวัง | ผล/สถานะ |
|---|---|---|---|---|---|
| AUTH-01 | F1 | Happy | AuthControllerTests จำลอง TuAuthService ยืนยันเลขนักศึกษาและรหัสผ่านถูกต้อง แล้วเรียก POST /api/auth/login และ GET /api/auth/me ด้วย session เดิม | Login ตอบ 200, สร้าง session, /me คืนข้อมูลนักศึกษา | ครอบคลุมบางส่วน: ผ่าน MockMvc; ยังไม่มี Playwright ตรวจการนำทางหน้าเว็บหลัง login |
| AUTH-02 | F2, F4 | Negative | TuAuthServiceTests ให้ TU Auth ตอบบัญชี student ที่มีเลขขึ้นต้น 6808; Playwright จำลอง POST /auth/login ตอบ 403 โดยมี error เป็น Forbidden | Backend ปฏิเสธด้วย 403; หน้า login แสดงข้อความเฉพาะนักศึกษาสาขาวิทยาการคอมพิวเตอร์ | ผ่านแยกชั้น: backend unit test และ Playwright frontend test |
| AUTH-03 | F3 | Happy / Security | AuthControllerTests login แล้ว POST /api/auth/logout; จากนั้นเรียก /api/auth/me ด้วย session เดิม | Logout ตอบ 204 และ session เดิมถูกปฏิเสธด้วย 401 | ครอบคลุมบางส่วน: ผ่าน MockMvc; ข้อความ “Log Out สำเร็จ” ตามข้อกำหนดเดิมยังไม่มีในหน้าเว็บ |
| AUTH-04 | F1, F4 | Negative | TU Auth ตอบ 400/401 หรือ service คืน invalid credentials; ตรวจ response login และ session | Backend ตอบ 401, ไม่สร้าง session; หน้า login แสดงข้อความรหัสผ่าน/ข้อมูลยืนยันตัวตนไม่ถูกต้อง | ครอบคลุมบางส่วน: backend ผ่าน; ยังไม่มี Playwright ตรวจข้อความ 401 บนหน้าเว็บ |
| AUTH-05 | F1, F4 | Negative | POST login ด้วย username/password ว่าง และ username ที่ไม่ใช่ตัวเลข 10 หลัก; AuthControllerTests ตรวจว่าไม่เรียก TU Auth | Backend ตอบ 400; ใน browser ช่อง required/pattern ต้องหยุดการ submit และไม่ส่ง request | ครอบคลุมบางส่วน: backend validation ผ่าน; ยังไม่มี Playwright ตรวจ native form validation |
| AUTH-06 | F3, F5 | Security / Browser | Playwright เปิดหน้ารายวิชาในสถานะ login, ไปหน้า profile, logout, กด Back; API mock เปลี่ยน /auth/me เป็น 401 หลัง logout | กลับไปหน้า login และไม่แสดงรหัสนักศึกษาจากหน้าก่อนหน้า | ผ่านตาม flow Playwright ที่จำลอง API; ยังไม่ได้ตรวจการคืนหน้าจาก browser Back/Forward Cache จริง |
| AUTH-07 | F4 | Error | TuAuthServiceTests จำลอง TU Auth ตอบ 500 และ network timeout/connection error | Backend แปลงเป็น 502 พร้อม error ที่เหมาะสม; frontend ต้องแสดงข้อความและไม่ค้าง/ไม่เป็นหน้าขาว | ครอบคลุมบางส่วน: backend tests ผ่าน; ยังไม่มี Playwright ตรวจข้อความและสถานะหน้าเว็บ |

## Execution Log

| วันที่ | ชุดทดสอบ/คำสั่ง | ผล | หมายเหตุ |
|---|---|---|---|
| 12 พ.ย. 2569 | จาก backend/cstu-course-nexus: .\mvnw.cmd '-Dtest=AuthControllerTests,TuAuthServiceTests' test | ผ่าน: 14 tests, 0 failures, 0 errors | AuthControllerTests 5 รายการ และ TuAuthServiceTests 9 รายการ; ใช้ mock สำหรับ TU Auth |
| 12 พ.ย. 2569 | จากโฟลเดอร์หลัก: npx playwright test tests/auth.spec.js --project=chromium | ผ่าน: 2 tests | AUTH-02 และ AUTH-06; intercept/mock API ไม่ได้เรียก TU Auth จริง |

## ข้อจำกัดและรายการติดตาม

| ID | รายการ | สถานะ |
|---|---|---|
| A-01 | Prefix 6809 ถูกกำหนดตายตัว จึงอนุญาตเฉพาะเลขที่ขึ้นต้น 6809; ต้องปรับเมื่อกฎรหัสนักศึกษาของสาขาหรือปีรับเข้าเปลี่ยน | เปิด |
| A-02 | เพิ่ม Playwright success-login เพื่อยืนยันการนำทางและข้อมูลผู้ใช้บนหน้าหลัก | ยังไม่มี |
| A-03 | เพิ่ม Playwright logout เพื่อยืนยันข้อความ “Log Out สำเร็จ”; ปัจจุบัน logout นำทางกลับหน้า login แต่ไม่มี success alert | ยังไม่มี |
| A-04 | เพิ่ม Playwright สำหรับ 401, form validation ว่าง และ network/500 เพื่อยืนยัน error message และการใช้งานหน้า login ต่อได้ | ยังไม่มี |
| A-05 | Playwright page.goBack ตรวจ history navigation และ API revalidation; การกู้คืนจาก BFCache ควรตรวจด้วย browser จริงเพิ่มเติม | ตรวจเพิ่มด้วยมือ |

## สรุปผล

- Backend authentication tests ที่รัน: 14 ผ่าน, 0 ไม่ผ่าน
- Playwright authentication tests ที่รัน: 2 ผ่าน, 0 ไม่ผ่าน
- AUTH-02 มี test backend และ frontend แยกกัน; ไม่ใช่ end-to-end test ที่ต่อกับ TU Auth จริง
- AUTH-06 ผ่าน Playwright สำหรับ browser history flow ที่จำลอง API; BFCache ยังไม่ได้ตรวจด้วย browser จริง
- ข้อ 1, 3, 4, 5 และ 7 มี backend coverage บางส่วน แต่ยังขาด UI/E2E checks ตามรายการติดตามด้านบน
- ผลนี้เป็นผล focused auth tests ไม่ใช่ผลของ test suite ทั้งโปรเจกต์

เอกสารนี้ครอบคลุมเฉพาะ authentication test cases 1–7 ไม่ใช่รายการ test cases ของทุกฟีเจอร์ในระบบ