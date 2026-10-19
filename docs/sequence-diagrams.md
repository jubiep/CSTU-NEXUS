# CSTU-NEXUS - Sequence Diagrams

เอกสารรวบรวม Sequence Diagrams สำหรับระบบ **CSTU-NEXUS** ในรูปแบบ **Boundary-Control-Entity (BCE)** อ้างอิงตาม REST API และกระบวนการทำงานจริงของระบบ (GitHub รองรับการเรนเดอร์ Mermaid Diagrams ด้านล่างโดยอัตโนมัติ)

---

## สารบัญ Use Cases
- [U1: อ่าน Review ของรายวิชาและ Section จากรุ่นพี่](#u1-อ่าน-review-ของรายวิชาและ-section-จากรุ่นพี่)
- [U2: การยืนยันตัวตนและการจัดการสิทธิ์ก่อนใช้งานระบบ (Authentication & Authorization)](#u2-การยืนยันตัวตนและการจัดการสิทธิ์ก่อนใช้งานระบบ-authentication--authorization)
- [U3: ตรวจสอบชั้นปีของนักศึกษาก่อนเขียน Review](#u3-ตรวจสอบชั้นปีของนักศึกษาก่อนเขียน-review)
- [U4: ค้นหารายวิชาที่ต้องการ และ Section](#u4-ค้นหารายวิชาที่ต้องการ-และ-section)
- [U5: เขียน Review รายวิชาตาม Section](#u5-เขียน-review-รายวิชาตาม-section)

---

## U1: อ่าน Review ของรายวิชาและ Section จากรุ่นพี่

- **Endpoint:** `GET /api/courses/{id}/reviews?sectionId=...`
- **คำอธิบาย:** นักศึกษาเปิดหน้ารายละเอียดรายวิชาเพื่ออ่านรีวิวทั้งหมด หรือเลือกกรองเฉพาะ Section ที่ต้องการศึกษา

```mermaid
sequenceDiagram
    autonumber
    actor Student as TU Student
    participant View as CourseDetailView<br/>(Boundary)
    participant Ctrl as ReviewController<br/>(Control)
    participant DB as ReviewDatabase<br/>(Entity)

    Student->>View: 1. เลือกวิชา หรือเลือกฟิลเตอร์ Section
    activate View
    View->>Ctrl: 2. getReviews(courseId, sectionId)
    activate Ctrl
    
    Ctrl->>DB: 3. SELECT * FROM review WHERE course_id = ? AND section_id = ?
    activate DB
    DB-->>Ctrl: 4. return reviewList (reviewId, sectionNumber, reviewerYear, reviewText, createdAt)
    deactivate DB
    
    Ctrl-->>View: 5. return JSON [ReviewResponse]
    deactivate Ctrl
    
    View->>View: 6. renderReviews(reviewList)
    View-->>Student: 7. แสดงรายการรีวิวและชั้นปีของรุ่นพี่ผู้รีวิวบนหน้าจอ
    deactivate View
```

---

## U2: การยืนยันตัวตนและการจัดการสิทธิ์ก่อนใช้งานระบบ (Authentication & Authorization)

- **Endpoint:** `POST /api/auth/login`
- **คำอธิบาย:** นักศึกษาเข้าสู่ระบบด้วยเลขทะเบียนนักศึกษาและรหัสผ่าน โดยระบบจะทำการยืนยันตัวตนผ่าน TU AD Verify API คำนวณชั้นปี และสร้าง Session

```mermaid
sequenceDiagram
    autonumber
    actor Student as TU Student
    participant View as LoginView<br/>(Boundary)
    participant Ctrl as LoginController<br/>(Control)
    participant TuService as TuAuthService<br/>(Control)
    participant TuAPI as TU AD Verify API<br/>(External Service)
    participant Session as HttpSession / DB<br/>(Entity)

    Student->>View: 1. กรอก UserName, Password
    activate View
    Student->>View: 2. กด submit
    View->>Ctrl: 3. checkLogin(userName, password)
    activate Ctrl

    Ctrl->>TuService: 4. verify(userName, password)
    activate TuService
    TuService->>TuAPI: 5. call REST API https://restapi.tu.ac.th/api/v1/auth/Ad/verify
    activate TuAPI
    TuAPI-->>TuService: 6. return LoginResponse in JSON format
    deactivate TuAPI
    TuService-->>Ctrl: 7. return AuthUser (studentId, name, faculty)
    deactivate TuService

    alt เข้าสู่ระบบสำเร็จ (Credential ถูกต้อง)
        Ctrl->>Ctrl: 8. StudyYearCalculator.calculate(studentId)
        Ctrl->>Session: 9. saveSession(user, currentYear)
        activate Session
        Session-->>Ctrl: 10. sessionCreated (Set-Cookie)
        deactivate Session
        Ctrl-->>View: 11. return LoginResponse (Success + User Data)
        View->>View: 12. show(LoginResponse)
        View-->>Student: 13. เข้าสู่ระบบสำเร็จ และนำทางไปยังหน้าหลัก
    else ข้อมูลไม่ถูกต้อง หรือ ไม่ใช่ นศ.
        Ctrl-->>View: 14. return LoginResponse (401 Unauthorized / Error)
        View->>View: 15. showErrorMessage()
        View-->>Student: 16. แสดงข้อความแจ้งเตือนความผิดพลาด
    end
    deactivate Ctrl
    deactivate View
```

---

## U3: ตรวจสอบชั้นปีของนักศึกษาก่อนเขียน Review

- **Endpoint:** `GET /api/auth/me`
- **คำอธิบาย:** ตรวจสอบว่าผู้ใช้ล็อกอินอยู่หรือไม่ และมีชั้นปีที่มีสิทธิ์เขียนรีวิวหรือไม่ก่อนเปิดฟอร์มเขียนรีวิว

```mermaid
sequenceDiagram
    autonumber
    actor Student as TU Student
    participant View as CourseDetailView / WriteReviewView<br/>(Boundary)
    participant Ctrl as ReviewController<br/>(Control)
    participant Session as HttpSession<br/>(Entity)

    Student->>View: 1. กดปุ่ม "เขียนรีวิว" (Click Write Review)
    activate View
    View->>Ctrl: 2. checkReviewEligibility(courseId)
    activate Ctrl

    Ctrl->>Session: 3. getAttribute("authenticatedUser")
    activate Session
    Session-->>Ctrl: 4. return AuthUser (studentId, currentYear)
    deactivate Session

    alt ตรวจสอบผ่าน (ล็อกอินแล้ว และมีชั้นปีที่มีสิทธิ์เขียนรีวิว)
        Ctrl-->>View: 5. return 200 OK (eligible: true, currentYear)
        View->>View: 6. displayReviewForm(courseId, sections)
        View-->>Student: 7. แสดงฟอร์มสำหรับเขียนรีวิวและเลือก Section
    else ไม่ผ่าน (ยังไม่ได้เข้าสู่ระบบ หรือ ชั้นปีไม่ตรงตามเกณฑ์)
        Ctrl-->>View: 8. return 403 Forbidden / 401 Unauthorized (eligible: false)
        deactivate Ctrl
        View->>View: 9. showAccessDeniedModal("กรุณาเข้าสู่ระบบ หรือ นศ. ยังไม่มีสิทธิ์รีวิววิชานี้")
        View-->>Student: 10. แสดงแจ้งเตือนปฏิเสธการเข้าถึงฟอร์ม
    end
    deactivate View
```

---

## U4: ค้นหารายวิชาที่ต้องการ และ Section

- **Endpoint:** `GET /api/courses?keyword=...&year=...` และ `GET /api/courses/{id}`
- **คำอธิบาย:** ค้นหารายวิชาตามคีย์เวิร์ดหรือชั้นปีที่แนะนำ และกดเข้าไปดูข้อมูลเซกชันและอาจารย์ผู้สอน

```mermaid
sequenceDiagram
    autonumber
    actor Student as TU Student
    participant View as CourseSearchView<br/>(Boundary)
    participant Ctrl as CourseController<br/>(Control)
    participant DB as CourseDatabase<br/>(Entity)

    Student->>View: 1. พิมพ์คำค้นหา (keyword) / เลือกชั้นปี (year)
    activate View
    Student->>View: 2. กดปุ่ม "ค้นหา"
    View->>Ctrl: 3. searchCourses(keyword, year)
    activate Ctrl

    Ctrl->>DB: 4. SELECT c.*, COUNT(s.section_id) FROM courses c LEFT JOIN sections s ...
    activate DB
    DB-->>Ctrl: 5. return courseSummaryList
    deactivate DB
    Ctrl-->>View: 6. return JSON [CourseSummary]
    deactivate Ctrl

    View->>View: 7. renderCourseList(courses)
    View-->>Student: 8. แสดงรายการวิชาที่ตรงกับเงื่อนไขค้นหา

    opt นักศึกษาคลิกดูรายละเอียดและเซกชันของวิชานั้น
        Student->>View: 9. คลิกเลือกรายวิชา (courseId)
        View->>Ctrl: 10. getCourse(courseId)
        activate Ctrl
        Ctrl->>DB: 11. SELECT * FROM courses JOIN sections WHERE course_id = ?
        activate DB
        DB-->>Ctrl: 12. return courseDetail with sections
        deactivate DB
        Ctrl-->>View: 13. return JSON CourseDetail
        deactivate Ctrl
        View->>View: 14. showCourseDetailAndSections()
        View-->>Student: 15. แสดงข้อมูลวิชาและรายการ Section (อาจารย์ผู้สอน, รหัสเซกชัน)
    end
    deactivate View
```

---

## U5: เขียน Review รายวิชาตาม Section

- **Endpoint:** `POST /api/courses/{id}/reviews`
- **คำอธิบาย:** นักศึกษากรอกเนื้อหารีวิว เลือกเซกชัน และบันทึกข้อมูลลงฐานข้อมูล

```mermaid
sequenceDiagram
    autonumber
    actor Student as TU Student
    participant View as WriteReviewView<br/>(Boundary)
    participant Ctrl as ReviewController<br/>(Control)
    participant Session as HttpSession<br/>(Entity)
    participant DB as ReviewDatabase<br/>(Entity)

    Student->>View: 1. เลือก Section และพิมพ์ข้อความรีวิว
    activate View
    Student->>View: 2. กดปุ่ม "ส่งรีวิว" (Submit Review)
    
    View->>Ctrl: 3. submitReview(courseId, sectionId, reviewText)
    activate Ctrl

    Ctrl->>Session: 4. getAuthenticatedUser()
    activate Session
    Session-->>Ctrl: 5. return AuthUser (userId, currentYear)
    deactivate Session

    alt ตรวจสอบข้อมูลถูกต้อง (Valid Review)
        Ctrl->>DB: 6. INSERT INTO review (course_id, section_id, user_id, reviewer_year, review_text) VALUES (...)
        activate DB
        DB-->>Ctrl: 7. return generated reviewId
        deactivate DB

        Ctrl-->>View: 8. return 201 Created { reviewId, message: "รีวิวสำเร็จ" }
        View->>View: 9. showSuccessPopup()
        View-->>Student: 10. แจ้งเตือนส่งรีวิวสำเร็จ และนำกลับไปหน้ารายละเอียดวิชา
    else ข้อมูลไม่ครบถ้วน (เช่น ไม่ได้เลือก Section หรือข้อความว่างเปล่า)
        Ctrl-->>View: 11. return 400 Bad Request { error: "กรุณากรอกข้อมูลให้ครบถ้วน" }
        deactivate Ctrl
        View->>View: 12. showValidationError()
        View-->>Student: 13. แสดงข้อความแจ้งเตือนให้กรอกข้อมูลให้ครบ
    end
    deactivate View
```
