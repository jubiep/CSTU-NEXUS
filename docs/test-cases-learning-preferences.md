# รายงานทดสอบระบบ U7: Preferences

วันที่ทดสอบ: 14 พฤศจิกายน 2569  
ขอบเขต: โหลด ปรับ และบันทึก Preferences รวมถึงตรวจสอบการบันทึกลงฐานข้อมูล ตาม DoD ของ Task7  
API ที่ใช้: `GET/PUT /api/users/preferences` ตาม API contract จาก Task3 และ Issue #30

## ขอบเขตและสภาพแวดล้อม

- Frontend: หน้า Preferences ที่ `http://localhost:3000/pages/preference.html`
- Backend integration test: Spring Boot, Java 17.0.20.1, PostgreSQL 17.11 ที่ `localhost:5432/cstu_nexus_db`
- Browser checks ใช้ Playwright session และ mock API เพื่อควบคุม response; ไม่ได้ใช้บัญชีจริงหรือเขียนข้อมูลทดสอบจาก browser ลงฐานข้อมูล
- Database persistence ตรวจแยกด้วย MockMvc และ PostgreSQL จริงใน `UserPreferencesPersistenceIntegrationTests`; test ใช้ transaction rollback
- ใช้ `PUT` สำหรับบันทึกตาม contract จาก Task3/Issue #30 แม้ตัวอย่างใน Trello Task6 จะระบุ `POST`

## Test Cases

| ID | DoD | ขั้นตอน / ข้อมูลทดสอบ | ผลที่คาดหวัง | ผลจริง / สถานะ |
|---|---|---|---|---|
| U7-SYS-01 | โหลด Preference เดิมเป็นค่าเริ่มต้น | Mock `GET` ให้คะแนน `100, 50, 75, null, 25` สำหรับ Teaching, Evaluation, Speed, Assignment และ Workload ตามลำดับ | UI เลือกค่าระดับ `3, 2, 4, ไม่มีค่า, 2` | Browser check ได้ผลตรงกันทุกช่อง และ Assignment ไม่มี radio ที่ถูกเลือก — **ผ่าน** |
| U7-SYS-02 | เลือก/เปลี่ยน Preference และ Workload | เลือก Teaching ระดับ 2 แล้วคลิกซ้ำเพื่อล้าง; เลือก Workload ระดับ 3 และบันทึก | คลิกซ้ำยกเลิกหัวใจได้; ค่าที่ล้างส่งเป็น `null`; Workload ระดับ 3 จาก 5 ส่งเป็น 50 | หลังคลิกครั้งแรกมี radio ที่เลือก 1 รายการ; หลังคลิกซ้ำเหลือ 0 และไม่มีหัวใจที่เติมสี; payload มี Teaching `null` และ Workload `50` — **ผ่าน** |
| U7-SYS-03 | Validate เมื่อไม่ระบุ Workload | เลือก Preference อื่นและกดบันทึกโดยไม่เลือก Workload; จากนั้นเลือก Workload ระดับ 3 และบันทึกใหม่ | ครั้งแรกแจ้งเตือนและไม่ส่ง PUT; เมื่อเลือก Workload แล้วบันทึกได้ | แสดงข้อความให้เลือกระดับ Workload, PUT ก่อนเลือกมี 0 ครั้ง; หลังเลือก Workload มี PUT 1 ครั้งและ success dialog ปรากฏ — **ผ่าน** |
| U7-SYS-04 | ตรวจว่าฐานข้อมูลอัปเดตจริง | Integration test ส่ง PUT ด้วยคะแนน `50, 100, 75, 0, 100`; ตรวจแถวใน `user_preferences` แล้วเรียก GET | ค่าทั้งห้าถูก persist และ GET คืนค่าเดียวกับที่บันทึก | ตรวจค่าทั้งห้าจาก PostgreSQL จริงและ GET คืนค่าตรงกัน — **ผ่าน** |
| U7-SYS-05 | แสดงสถานะการบันทึก | บันทึกผ่าน mock API ที่ตอบสำเร็จ | แสดงสถานะ/หน้าต่างยืนยันการบันทึกสำเร็จ | Browser check พบ success dialog — **ผ่าน (UI ใช้ mock API)** |

## Execution Log

| ชุดทดสอบ / คำสั่ง | ผล | หมายเหตุ |
|---|---|---|
| `.\mvnw.cmd '-Dtest=UserPreferencesControllerTests,UserPreferencesServiceTests,UserPreferencesPersistenceIntegrationTests' test` จาก `backend\cstu-course-nexus` | ผ่าน: 13 tests, 0 failures, 0 errors, 0 skipped | รวม controller 10, service 2 และ PostgreSQL integration 1 |
| Browser checks ใน Playwright session พร้อม mock `GET/PUT` | ผ่านตาม U7-SYS-01, U7-SYS-02, U7-SYS-03 และ U7-SYS-05 | ตรวจผล UI, request count และ payload; ไม่ได้เชื่อม browser เข้ากับ backend จริง |
| Standalone runtime validation ตาม `.tsupgrader\runtime-validation\eval-plan.json` | Process exit code 0 | ตัวตรวจไม่สามารถ parse จำนวน tests จาก Maven output (`framework=unknown`); จำนวน 13 tests ยืนยันจาก Maven output โดยตรง |

## ข้อจำกัด

- ยังไม่ได้รัน repository Playwright suite `tests/preferences.spec.js` ด้วย CLI เพราะ environment นี้ไม่มี `node`/`npm`
- ไม่ได้ทดสอบ browser-to-live-backend แบบ end-to-end ใน session เดียว; UI และ backend/database ตรวจแยกกัน โดย UI ใช้ mock API และ backend integration test ใช้ PostgreSQL จริง
- ไม่ได้ทดสอบการบังคับ Workload ที่ backend โดยตรง; ตามขอบเขต Task7 ที่ตรวจครั้งนี้ validation ทำที่ UI ก่อนส่ง PUT

## สรุปผลตาม DoD

- โหลดค่าเดิมและแสดงค่าที่ถูกต้อง: **ผ่าน**
- เลือก/เปลี่ยน Preference และ Workload รวมถึงยกเลิกหัวใจ: **ผ่าน**
- แจ้งเตือนและป้องกันการบันทึกเมื่อไม่เลือก Workload: **ผ่าน**
- ตรวจการ persist และอ่านค่ากลับจาก PostgreSQL จริง: **ผ่าน**
- แสดงสถานะบันทึกสำเร็จ: **ผ่านด้วย mock API**

ผลทดสอบผ่านตาม DoD ที่ตรวจไว้ โดยมีข้อจำกัดว่า Playwright CLI suite และ full browser-to-live-backend E2E ยังไม่ได้รัน
