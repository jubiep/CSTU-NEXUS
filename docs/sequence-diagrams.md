# CSTU-NEXUS - Sequence Diagrams

เอกสารรวบรวม Sequence Diagrams สำหรับระบบ **CSTU-NEXUS** ในรูปแบบ **Boundary-Control-Entity (BCE)** อ้างอิงตาม REST API และกระบวนการทำงานจริงของระบบ (GitHub รองรับการเรนเดอร์ Mermaid Diagrams ด้านล่างโดยอัตโนมัติ)

---

## สารบัญ Use Cases

### Sprint 1
- [U1: อ่าน Review ของรายวิชาและ Section จากรุ่นพี่](#u1-อ่าน-review-ของรายวิชาและ-section-จากรุ่นพี่)
- [U2: การยืนยันตัวตนและการจัดการสิทธิ์ก่อนใช้งานระบบ (Authentication & Authorization)](#u2-การยืนยันตัวตนและการจัดการสิทธิ์ก่อนใช้งานระบบ-authentication--authorization)
- [U3: ตรวจสอบชั้นปีของนักศึกษาก่อนเขียน Review](#u3-ตรวจสอบชั้นปีของนักศึกษาก่อนเขียน-review)
- [U4: ค้นหารายวิชาที่ต้องการ และ Section](#u4-ค้นหารายวิชาที่ต้องการ-และ-section)
- [U5: เขียน Review รายวิชาตาม Section](#u5-เขียน-review-รายวิชาตาม-section)

### Sprint 2
- [U6: ให้คะแนนรายวิชาตามหัวข้อที่กำหนด](#u6-ให้คะแนนรายวิชาตามหัวข้อที่กำหนด)

---

## Sprint 1 (U1–U5)

### U1: อ่าน Review ของรายวิชาและ Section จากรุ่นพี่

- **Endpoint:** `GET /api/courses/{id}/reviews?sectionId=...`
- **คำอธิบาย:** นักศึกษาเปิดหน้ารายละเอียดรายวิชาเพื่ออ่านรีวิวทั้งหมด หรือเลือกกรองเฉพาะ Section ที่ต้องการศึกษา

```mermaid
sequenceDiagram
    autonumber
    actor Student as TU Student
    participant View as CourseDetailView<br/>(Boundary)
    participant Ctrl as ReviewController<br/>(Control)
    participant Svc as ReviewService<br/>(Control)
    participant Repo as ReviewRepository<br/>(Entity)
    participant DB as PostgreSQL Database<br/>(Entity)

    Student->>View: 1. เลือกวิชา หรือเลือกฟิลเตอร์ Section
    activate View
    View->>Ctrl: 2. GET /api/courses/{id}/reviews?sectionId=...
    activate Ctrl
    
    Ctrl->>Svc: 3. getReviews(courseId, sectionId)
    activate Svc
    
    alt มีการระบุ sectionId
        Svc->>Repo: 4. findByCourseIdAndSectionIdOrderByCreatedAtDesc(...)
    else ดูรีวิวทั้งหมดของวิชา
        Svc->>Repo: 4. findByCourseIdOrderByCreatedAtDesc(...)
    end
    activate Repo
    Repo->>DB: 5. SQL SELECT review JOIN sections ...
    activate DB
    DB-->>Repo: 6. return review rows
    deactivate DB
    Repo-->>Svc: 7. return List~Review~
    deactivate Repo
    Svc-->>Ctrl: 8. return List~Review~
    deactivate Svc
    Ctrl-->>View: 9. 200 OK [JSON Review List]
    deactivate Ctrl
    
    View->>View: 10. renderReviews(reviewList)
    View-->>Student: 11. แสดงรายการรีวิวและชั้นปีของรุ่นพี่ผู้รีวิวบนหน้าจอ
    deactivate View
```

---

### U2: การยืนยันตัวตนและการจัดการสิทธิ์ก่อนใช้งานระบบ (Authentication & Authorization)

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

### U3: ตรวจสอบชั้นปีของนักศึกษาก่อนเขียน Review

- **Endpoint:** `GET /api/courses/{id}/reviews/eligibility`
- **คำอธิบาย:** ระบบตรวจสอบว่าผู้ใช้ได้เข้าสู่ระบบ มีชั้นปีที่ผ่านเกณฑ์ (`studentYear > recommendedYear`) และยังไม่เคยส่งรีวิวในรายวิชานี้มาก่อน

```mermaid
sequenceDiagram
    autonumber
    actor Student as TU Student
    participant View as CourseDetailView / WriteReviewView<br/>(Boundary)
    participant Ctrl as ReviewController<br/>(Control)
    participant Session as HttpSession<br/>(Entity)
    participant Svc as ReviewService<br/>(Control)
    participant Calc as StudyYearCalculator<br/>(Control)
    participant Repo as ReviewRepository<br/>(Entity)
    participant DB as PostgreSQL Database<br/>(Entity)

    Student->>View: 1. เปิดหน้ารายละเอียดวิชา / หน้าเขียนรีวิว
    activate View
    View->>Ctrl: 2. GET /api/courses/{id}/reviews/eligibility
    activate Ctrl

    Ctrl->>Session: 3. getAttribute("authenticatedUser")
    activate Session
    Session-->>Ctrl: 4. return AuthUser (studentId, currentYear)
    deactivate Session

    alt ยังไม่ได้เข้าสู่ระบบ
        Ctrl-->>View: 5. 401 Unauthorized { eligible: false, message: "กรุณาเข้าสู่ระบบก่อนเช็กสิทธิ์" }
        View->>View: 6. ซ่อนปุ่มเขียนรีวิว / นำทางไปหน้า Login
    else ล็อกอินแล้ว
        Ctrl->>Svc: 7. getEligibilityStatus(studentId, courseId)
        activate Svc
        Svc->>Calc: 8. calculate(studentId)
        Calc-->>Svc: 9. return studentYear
        Svc->>Repo: 10. existsByCourseIdAndUserId(courseId, userId)
        Repo-->>Svc: 11. return alreadyReviewed (boolean)
        Svc->>DB: 12. SELECT recommended_year FROM courses WHERE course_id = ?
        DB-->>Svc: 13. return recommendedYear

        alt เคยรีวิวแล้ว หรือ ชั้นปีไม่ผ่านเกณฑ์ (studentYear <= recommendedYear)
            Svc-->>Ctrl: 14. return EligibilityStatus(eligible: false, message)
            Ctrl-->>View: 15. 200 OK { eligible: false, message }
            View->>View: 16. ซ่อนปุ่มรีวิว หรือ ปิดปุ่ม Submit และแสดงข้อความแจ้งเตือน
            View-->>Student: 17. แสดงเหตุผลที่ไม่สามารถรีวิวได้
        else ผ่านเกณฑ์ (studentYear > recommendedYear และยังไม่เคยรีวิว)
            Svc-->>Ctrl: 18. return EligibilityStatus(eligible: true, message: "คุณได้รับสิทธิ์แบ่งปันประสบการณ์")
            deactivate Svc
            Ctrl-->>View: 19. 200 OK { eligible: true, studentYear, recommendedYear, message }
            deactivate Ctrl
            View->>View: 20. แสดงปุ่มเขียนรีวิว / เปิดให้กรอกแบบฟอร์มรีวิวได้
            View-->>Student: 21. อนุญาตให้กรอกเนื้อหารีวิว
        end
    end
    deactivate View
```

---

### U4: ค้นหารายวิชาที่ต้องการ และ Section

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

### U5: เขียน Review รายวิชาตาม Section

- **Endpoint:** `POST /api/courses/{id}/reviews`
- **คำอธิบาย:** นักศึกษากรอกเนื้อหารีวิว เลือกเซกชัน และบันทึกข้อมูลลงฐานข้อมูล

```mermaid
sequenceDiagram
    autonumber
    actor Student as TU Student
    participant View as WriteReviewView<br/>(Boundary)
    participant Ctrl as ReviewController<br/>(Control)
    participant Session as HttpSession<br/>(Entity)
    participant Svc as ReviewService<br/>(Control)
    participant Repo as ReviewRepository<br/>(Entity)
    participant DB as PostgreSQL Database<br/>(Entity)

    Student->>View: 1. เลือก Section และพิมพ์ข้อความรีวิว
    activate View
    Student->>View: 2. กดปุ่ม "ส่งรีวิว" (Submit Review)
    
    View->>Ctrl: 3. POST /api/courses/{id}/reviews { sectionId, reviewText }
    activate Ctrl

    Ctrl->>Session: 4. getAttribute("authenticatedUser")
    activate Session
    Session-->>Ctrl: 5. return AuthUser (studentId, currentYear)
    deactivate Session

    alt ยังไม่เข้าสู่ระบบ หรือ ข้อความรีวิวว่างเปล่า
        Ctrl-->>View: 6. 401 / 400 Bad Request { error: "..." }
        View->>View: 7. showValidationError(error)
        View-->>Student: 8. แจ้งเตือนข้อผิดพลาดบนหน้าจอ
    else ข้อมูลเบื้องต้นถูกต้อง
        Ctrl->>Svc: 9. getEligibilityStatus(studentId, courseId)
        activate Svc
        Svc-->>Ctrl: 10. return status (eligible: boolean)
        deactivate Svc

        alt ชั้นปีไม่ผ่านเกณฑ์ หรือ เคยรีวิววิชานี้แล้ว
            Ctrl-->>View: 11. 403 Forbidden { error: status.message }
            View->>View: 12. showAccessDeniedModal(message)
            View-->>Student: 13. แสดงข้อความปฏิเสธการส่งรีวิว
        else มีสิทธิ์รีวิว (Eligible)
            Ctrl->>Svc: 14. saveReview(review)
            activate Svc
            Svc->>Repo: 15. save(review)
            activate Repo
            Repo->>DB: 16. INSERT INTO review (course_id, section_id, user_id, reviewer_year, review_text, created_at) ...
            activate DB
            DB-->>Repo: 17. return saved review row with ID
            deactivate DB
            Repo-->>Svc: 18. return saved Review entity
            deactivate Repo
            Svc-->>Ctrl: 19. return Review
            deactivate Svc

            Ctrl-->>View: 20. 201 Created { reviewId, message: "รีวิวสำเร็จ" }
            deactivate Ctrl
            View->>View: 21. showSuccessModal()
            View-->>Student: 22. แจ้งเตือนส่งรีวิวสำเร็จ และนำกลับไปหน้ารายละเอียดวิชา
        end
    end
    deactivate View
```

---

## Sprint 2 (U6)

### U6: ให้คะแนนรายวิชาตามหัวข้อที่กำหนด

- **Endpoint:** `POST /api/courses/{id}/reviews`
- **คำอธิบาย:** นักศึกษา CSTU ให้คะแนนประเมินรายวิชาตามหัวข้อมาตรฐานที่กำหนด (ปรับปรุงสเกลเป็น 3 ระดับ 0–100 เท่ากันทุกหัวข้อ: `0.0`, `50.0`, `100.0` หรือเลือก `"ไม่แน่ใจ"` / เว้นว่างเป็น `null`) ระบบแปลงค่าตัวเลือกและบันทึกคะแนนลงฐานข้อมูล พร้อมรองรับการตรวจสอบสิทธิ์ความปลอดภัย ป้องกันการส่งวิชาที่ไม่มีอยู่ การประเมินซ้ำ (409 Conflict) การสแปมปุ่มส่ง และข้อผิดพลาด Database Error

```mermaid
sequenceDiagram
    autonumber
    actor Student as TU Student (CSTU)
    participant View as WriteReviewView<br/>(Boundary)
    participant Ctrl as ReviewController<br/>(Control)
    participant Session as HttpSession<br/>(Entity)
    participant Svc as ReviewService<br/>(Control)
    participant Repo as ReviewRepository<br/>(Entity)
    participant DB as PostgreSQL Database<br/>(Entity)

    Student->>View: 1. เลือกตัวเลือกประเมิน 5 หัวข้อ (3 ระดับ สเกล 0–100 หรือ "ไม่แน่ใจ") และพิมพ์ข้อความรีวิว
    activate View
    Student->>View: 2. กดปุ่ม "ส่ง" (Submit)

    View->>View: 3. ล็อกปุ่ม "ส่ง" (disabled = true, "กำลังส่ง...") ป้องกัน Double-click / Spam click (TC9)
    View->>View: 4. normalizeRatings(ratings) แปลงตัวเลือกเป็นสเกล 0.0–100.0 (3 ระดับ: 0.0, 50.0, 100.0 / ไม่แน่ใจ: null)

    alt ตรวจสอบฟอร์มเบื้องต้นไม่ผ่าน (เลือกไม่ครบ หรือไม่มีหัวข้อที่ประเมินจริง - TC11)
        View->>View: 5. ปลดล็อกปุ่ม "ส่ง" และแสดงข้อความแจ้งเตือน form-error
        View-->>Student: 6. แจ้งเตือนให้เลือกการประเมินให้ครบถ้วน (ไม่ส่งคำขอไปยัง Server)
    else ฟอร์มผ่านการตรวจสอบ
        View->>Ctrl: 7. POST /api/courses/{id}/reviews { sectionId, reviewText, ...Scores (0-100 หรือ null) }
        activate Ctrl

        Ctrl->>Session: 8. getAttribute("authenticatedUser")
        activate Session
        Session-->>Ctrl: 9. return AuthUser (studentId, currentYear) หรือ null
        deactivate Session

        alt ยังไม่ได้เข้าสู่ระบบ (Session ว่าง / null)
            Ctrl-->>View: 10. 401 Unauthorized { error: "กรุณาเข้าสู่ระบบก่อนเขียนรีวิว" }
            View->>View: 11. ปลดล็อกปุ่มและแสดงข้อความแจ้งเตือน / นำทางไปหน้า Login
            View-->>Student: 12. แจ้งเตือนให้เข้าสู่ระบบก่อนดำเนินการ
        else เข้าสู่ระบบแล้ว
            alt ข้อความรีวิวว่างเปล่า (text == null || empty)
                Ctrl-->>View: 13. 400 Bad Request { error: "กรุณากรอกข้อความรีวิวก่อนส่ง" }
                View->>View: 14. ปลดล็อกปุ่มและแสดง validation error
                View-->>Student: 15. แจ้งเตือนให้กรอกข้อความรีวิว
            else ข้อมูลข้อความรีวิวถูกต้อง
                Ctrl->>Svc: 16. getEligibilityStatus(studentId, courseId)
                activate Svc
                Svc->>DB: 17. SELECT recommended_year FROM courses WHERE UPPER(course_id) = ?
                activate DB
                DB-->>Svc: 18. return recommendedYear หรือ empty
                deactivate DB

                alt ไม่พบรายวิชาในระบบ (TC7)
                    Svc-->>Ctrl: 19. return EligibilityStatus(false, message: "ไม่พบข้อมูลรายวิชาในระบบ")
                    Ctrl-->>View: 20. 404 Not Found { error: "ไม่พบรายวิชา " + courseId }
                    View->>View: 21. ปลดล็อกปุ่มและแสดงข้อความไม่พบรายวิชา
                    View-->>Student: 22. แจ้งเตือนไม่พบรายวิชา (ไม่มีการบันทึกข้อมูลลง DB)
                else พบรายวิชาในระบบ
                    Svc->>Repo: 23. existsByCourseIdAndUserId(courseId, userId)
                    activate Repo
                    Repo->>DB: 24. SELECT EXISTS(SELECT 1 FROM review WHERE course_id = ? AND user_id = ?)
                    activate DB
                    DB-->>Repo: 25. return alreadyReviewed (boolean)
                    deactivate DB
                    Repo-->>Svc: 26. return alreadyReviewed
                    deactivate Repo
                    Svc-->>Ctrl: 27. return EligibilityStatus(eligible, studentYear, recommendedYear, message)

                    alt เคยประเมินวิชานี้ไปแล้ว (alreadyReviewed = true - TC8)
                        Ctrl-->>View: 28. 409 Conflict { error: "คุณได้เคยส่งรีวิวรายวิชานี้ไปแล้ว" }
                        View->>View: 29. ปิดการส่งฟอร์มและแสดงข้อความเตือน
                        View-->>Student: 30. ปฏิเสธการส่งซ้ำ (ไม่สร้างข้อมูลซ้ำและไม่นับคะแนนซ้ำซ้อน)
                    else ชั้นปีไม่ผ่านเกณฑ์ (studentYear <= recommendedYear)
                        Ctrl-->>View: 31. 403 Forbidden { error: "วิชานี้แนะนำสำหรับชั้นปีที่..." }
                        View->>View: 32. ปิดปุ่มส่งและแสดงข้อความจำกัดสิทธิ์ชั้นปี
                        View-->>Student: 33. ปฏิเสธสิทธิ์การรีวิว
                    else มีสิทธิ์รีวิวและยังไม่เคยประเมิน
                        alt คะแนนไม่อยู่ในช่วง 0–100 หรือไม่ใช่ null (validScore check)
                            Ctrl-->>View: 34. 400 Bad Request { error: "คะแนนแต่ละหัวข้อต้องอยู่ระหว่าง 0 ถึง 100 หรือเป็น null" }
                            View->>View: 35. ปลดล็อกปุ่มและแสดง error
                            View-->>Student: 36. แจ้งเตือนช่วงคะแนนไม่ถูกต้อง
                        else คะแนนและข้อมูลถูกต้องทั้งหมด (TC1–TC6)
                            Ctrl->>Svc: 37. saveReview(reviewWithScores)
                            activate Svc
                            Svc->>Repo: 38. save(review)
                            activate Repo
                            Repo->>DB: 39. INSERT INTO review (course_id, section_id, user_id, reviewer_year, review_text, teaching_method_score, evaluation_score, teaching_speed_score, assignment_style_score, workload_score)
                            activate DB
                            DB-->>Repo: 40. return saved review row / DataIntegrityViolation / DB Error
                            deactivate DB
                            Repo-->>Svc: 41. return saved Review / throw Exception
                            deactivate Repo

                            alt ส่งคำขอซ้ำพร้อมกันจนชน Unique Constraint (TC9)
                                Svc-->>Ctrl: 42. throw DataIntegrityViolationException
                                Ctrl-->>View: 43. 400 Bad Request { error: "ข้อมูลรายวิชาหรือ Section ไม่ถูกต้อง กรุณาตรวจสอบแล้วลองอีกครั้ง" }
                                View->>View: 44. คงข้อมูลเดิมเพียงชุดเดียว (ไม่นับคะแนนซ้ำ)
                                View-->>Student: 45. แจ้งเตือนสถานะการบันทึก (ป้องกันการบันทึกซ้ำซ้อน)
                            else ฐานข้อมูลขัดข้อง / ตัดการเชื่อมต่อ (TC10)
                                Svc-->>Ctrl: 46. throw DataAccessResourceFailureException
                                Ctrl-->>View: 47. 503 Service Unavailable { error: "ระบบฐานข้อมูลไม่พร้อมใช้งาน กรุณาลองใหม่ภายหลัง" }
                                View->>View: 48. ปลดล็อกปุ่มและแสดงข้อความขัดข้อง (ไม่แสดงว่าสำเร็จ)
                                View-->>Student: 49. แจ้งเตือนไม่สามารถบันทึกได้ (ไม่อัปเดตสถิติใดๆ)
                            else บันทึกลงฐานข้อมูลสำเร็จ (TC1–TC6)
                                Svc-->>Ctrl: 50. return saved Review
                                Ctrl-->>View: 51. 201 Created { reviewId, message: "รีวิวสำเร็จ" }
                                View->>View: 52. showSuccessModal() (แสดงป๊อปอัป "รีวิวเสร็จสิ้น")
                                View-->>Student: 53. แสดงผลบันทึกสำเร็จ (หัวข้อที่เลือก "ไม่แน่ใจ" เก็บเป็น null ใน DB ไม่นำไปคำนวณสถิติ)
                            end
                            deactivate Svc
                        end
                    end
                end
                deactivate Svc
            end
        end
        deactivate Ctrl
    end
    deactivate View
```

#### เกณฑ์การแปลงคะแนนมาตรฐาน (0.0 – 100.0) และเงื่อนไขการทำงานในปัจจุบัน
- **โครงสร้างหัวข้อการประเมิน 3 ระดับ (สเกล 0–100 เท่ากันทุกหัวข้อ)**:
  - **แนวทางการสอน (`teaching_method`)**: เน้นทฤษฎี (`0.0`), สมดุล (`50.0`), เน้นปฏิบัติ (`100.0`)
  - **สัดส่วนการวัดผลและตัดเกรด (`evaluation`)**: เน้นคะแนนสอบ (`0.0`), สมดุล (`50.0`), เน้นโครงงาน (`100.0`)
  - **ความเร็วในการสอน (`teaching_speed`)**: สอนช้า (`0.0`), ปานกลาง (`50.0`), สอนเร็ว (`100.0`)
  - **รูปแบบงาน (`assignment_style`)**: เน้นงานเดี่ยว (`0.0`), สมดุล (`50.0`), เน้นงานกลุ่ม (`100.0`)
  - **ปริมาณงาน (`workload`)**: งานน้อย (`0.0`), ปริมาณงานสมดุล (`50.0`), งานเยอะ (`100.0`)
- **ตัวเลือก `"ไม่แน่ใจ"` หรือหัวข้อที่ไม่ได้เลือก**: จัดเก็บค่าคะแนนในฐานข้อมูลเป็น `null` และไม่ถูกนำไปคำนวณค่าเฉลี่ยสถิติของหัวข้อนั้น (TC1, TC2)
- **เงื่อนไขและการตรวจสอบความถูกต้องตามโค้ดจริง (Validation & Error Handling)**:
  - **Client-side UI Lock**: เมื่อกดปุ่ม "ส่ง" ปุ่มจะถูก disable ทันที ป้องกันการส่งซ้ำซ้อน (Double-click / Spam click) (TC9)
  - **Course Validation**: ตรวจสอบรหัสรายวิชาในฐานข้อมูล หากไม่มีรายวิชาจะคืนค่า `404 Not Found` (TC7)
  - **Duplicate Check**: ตรวจสอบการรีวิวซ้ำผ่าน `ReviewService.hasReviewed(...)` หากเคยรีวิวแล้วจะคืนค่า `409 Conflict` (TC8) พร้อม `review_course_user_unique` บน PostgreSQL ป้องกันคำขอคู่ขนาน (TC9)
  - **Score Range Validation**: ตรวจสอบคะแนนทุกหัวข้อต้องอยู่ระหว่าง `0.0` ถึง `100.0` หรือเป็น `null` หากไม่ถูกต้องจะคืนค่า `400 Bad Request`
  - **Database Error Handling**: จัดการ `DataAccessResourceFailureException` ด้วยการตอบกลับ `503 Service Unavailable` และไม่แสดงว่าบันทึกสำเร็จ (TC10)

