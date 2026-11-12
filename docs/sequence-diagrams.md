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
- **คำอธิบาย:** นักศึกษา CSTU ให้คะแนนประเมินรายวิชาตามหัวข้อมาตรฐานที่กำหนด โดยเลือกประเมินบางหัวข้อหรือเลือก `"ไม่แน่ใจ"` ได้ (ต้องประเมินจริงอย่างน้อย 1 หัวข้อ) ระบบแปลงตัวเลือกเป็นค่าตัวเลข `0.0–1.0` เพื่อจัดเก็บและคำนวณสถิติ พร้อมป้องกันการส่งข้อมูลไปยังวิชาที่ไม่มีอยู่ การส่งซ้ำ และ DB Error

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

    Student->>View: 1. เลือกตัวเลือกประเมินตามหัวข้อ (3 ระดับ / 5 ระดับ / "ไม่แน่ใจ" / เว้นบางหัวข้อ)
    activate View
    Student->>View: 2. กดปุ่ม "ส่ง" (Submit)

    View->>View: 3. ล็อกปุ่ม "ส่ง" ทันที (ป้องกัน Double-click / Spam click - TC9)
    View->>View: 4. ตรวจเบื้องต้นว่ามีหัวข้อประเมินจริงอย่างน้อย 1 หัวข้อ

    alt ไม่มีหัวข้อที่ประเมินจริงเลย (ว่างทั้งหมด หรือเลือก "ไม่แน่ใจ" ทุกหัวข้อ - TC11)
        View->>View: 5. ปลดล็อกปุ่ม "ส่ง" และแสดงข้อความแจ้งเตือน
        View-->>Student: 6. แจ้งเตือนให้เลือกประเมินอย่างน้อย 1 หัวข้อ (ไม่ส่งคำขอและไม่นับสถิติ)
    else มีหัวข้อที่ประเมินจริงอย่างน้อย 1 หัวข้อ
        View->>Ctrl: 7. POST /api/courses/{id}/reviews { sectionId, reviewText, ratings }
        activate Ctrl

        Ctrl->>Session: 8. getAttribute("authenticatedUser")
        activate Session
        Session-->>Ctrl: 9. return AuthUser (studentId, currentYear) หรือ null
        deactivate Session

        alt ยังไม่ได้เข้าสู่ระบบ
            Ctrl-->>View: 10. 401 Unauthorized { error: "กรุณาเข้าสู่ระบบก่อนให้คะแนน" }
            View->>View: 11. ปลดล็อกปุ่มและแสดงข้อผิดพลาด / นำทางไป Login
            View-->>Student: 12. แจ้งเตือนให้เข้าสู่ระบบ
        else เข้าสู่ระบบแล้ว
            Ctrl->>Svc: 13. validateAndNormalizeRatings(ratings)
            activate Svc
            Svc->>Svc: 14. ตรวจสอบและแปลงตัวเลือกเป็นค่า 0.0–1.0 (3 ระดับ: 0.0/0.5/1.0, 5 ระดับ: 0.0–1.0, ไม่แน่ใจ/ไม่เลือก: null)
            Svc-->>Ctrl: 15. return ValidationResult (valid, normalizedScores)
            deactivate Svc

            alt ไม่มีหัวข้อประเมินจริงหลัง Validate ฝั่ง Server (TC11)
                Ctrl-->>View: 16. 400 Bad Request { error: "กรุณาเลือกประเมินอย่างน้อย 1 หัวข้อ" }
                View->>View: 17. ปลดล็อกปุ่มและแสดง validation error
                View-->>Student: 18. ปฏิเสธการส่งข้อมูล (ไม่บันทึกและไม่เพิ่มผู้ประเมินในสถิติ)
            else ข้อมูลคะแนนถูกต้อง (TC1–TC6)
                Ctrl->>Svc: 19. checkCourseAndEligibility(studentId, courseId)
                activate Svc
                Svc->>DB: 20. SELECT course_id, recommended_year FROM courses WHERE course_id = ?
                activate DB
                DB-->>Svc: 21. return Course หรือ empty
                deactivate DB

                alt ไม่พบรายวิชาในระบบ (TC7)
                    Svc-->>Ctrl: 22. return CourseNotFound
                    Ctrl-->>View: 23. 404 Not Found { error: "ไม่พบรายวิชาที่ระบุในระบบ" }
                    View->>View: 24. ปลดล็อกปุ่มและแสดงข้อความไม่พบรายวิชา
                    View-->>Student: 25. แจ้งเตือนไม่พบรายวิชา (ไม่มีการบันทึกข้อมูลลง DB)
                else พบรายวิชาในระบบ
                    Svc->>Repo: 26. existsByCourseIdAndUserId(courseId, userId)
                    activate Repo
                    Repo->>DB: 27. SELECT EXISTS(SELECT 1 FROM review WHERE course_id = ? AND user_id = ?)
                    activate DB
                    DB-->>Repo: 28. return alreadyReviewed (boolean)
                    deactivate DB
                    Repo-->>Svc: 29. return alreadyReviewed
                    deactivate Repo
                    Svc-->>Ctrl: 30. return EligibilityStatus(eligible, message)

                    alt เคยประเมินวิชานี้แล้ว หรือ ชั้นปีไม่ผ่านเกณฑ์ (TC8)
                        Ctrl-->>View: 31. 403 Forbidden { error: "คุณเคยส่งข้อมูลประเมินรายวิชานี้ไปแล้ว" }
                        View->>View: 32. แสดงข้อความแจ้งเตือนและคงสถานะปิดปุ่มส่ง
                        View-->>Student: 33. ปฏิเสธการส่งซ้ำ (ไม่สร้างข้อมูลซ้ำและไม่นับสถิติซ้ำซ้อน)
                    else ผ่านเกณฑ์และยังไม่เคยประเมิน
                        Ctrl->>Svc: 34. saveReviewWithRatings(courseId, userId, normalizedScores)
                        activate Svc
                        Svc->>Repo: 35. save(reviewWithScores)
                        activate Repo
                        Repo->>DB: 36. INSERT INTO review (..., teaching_method_score, evaluation_score, teaching_speed_score, assignment_style_score, workload_score)
                        activate DB
                        DB-->>Repo: 37. return saved row / UniqueViolation / DatabaseError
                        deactivate DB
                        Repo-->>Svc: 38. return saved Review / throw Exception
                        deactivate Repo

                        alt ส่งคำขอซ้ำพร้อมกันจนชน Unique Constraint (TC9)
                            Svc-->>Ctrl: 39. throw DataIntegrityViolationException
                            Ctrl-->>View: 40. 409 Conflict { error: "ข้อมูลถูกส่งไปแล้ว ไม่สามารถบันทึกซ้ำได้" }
                            View->>View: 41. คงข้อมูลเดิมเพียงชุดเดียว (ไม่นับคะแนนซ้ำ)
                            View-->>Student: 42. แจ้งเตือนสถานะการบันทึก (ป้องกันการบันทึกซ้ำซ้อน)
                        else ฐานข้อมูลขัดข้อง / ขาดการเชื่อมต่อ (TC10)
                            Svc-->>Ctrl: 43. throw DataAccessResourceFailureException
                            Ctrl-->>View: 44. 503 Service Unavailable { error: "ไม่สามารถบันทึกข้อมูลได้ในขณะนี้" }
                            View->>View: 45. ปลดล็อกปุ่มและแสดง error (ไม่แสดงว่าสำเร็จ)
                            View-->>Student: 46. แจ้งเตือนบันทึกไม่สำเร็จ (ไม่อัปเดตสถิติใดๆ)
                        else บันทึกลงฐานข้อมูลสำเร็จ (TC1–TC6)
                            Svc->>DB: 47. คำนวณสถิติเฉพาะหัวข้อที่ไม่เป็น null (AVG WHERE score IS NOT NULL)
                            activate DB
                            DB-->>Svc: 48. return updated course rating statistics
                            deactivate DB
                            Svc-->>Ctrl: 49. return ReviewResponse(reviewId, scores, stats)
                            Ctrl-->>View: 50. 201 Created { reviewId, scores, message: "บันทึกการให้คะแนนสำเร็จ" }
                            View->>View: 51. showSuccessModal()
                            View-->>Student: 52. แจ้งเตือนบันทึกสำเร็จ (หัวข้อ "ไม่แน่ใจ"/ไม่ได้เลือก เก็บเป็น null และไม่รวมในสถิติ)
                        end
                        deactivate Svc
                    end
                end
                deactivate Svc
            end
        end
        deactivate Ctrl
    end
    deactivate View
```

#### เกณฑ์การแปลงคะแนนมาตรฐาน (0.0 – 1.0) และเงื่อนไขสำคัญ (TC1–TC11)
- **หัวข้อแบบ 3 ระดับ** *(แนวทางการสอน, สัดส่วนการวัดผลและตัดเกรด, รูปแบบงาน)*: แปลงเป็น `0.0`, `0.5`, `1.0` ตามลำดับ (TC3, TC5)
- **หัวข้อแบบ 5 ระดับ** *(ความเร็วในการสอน, ปริมาณงาน)*: แปลงเป็น `0.0`, `0.25`, `0.5`, `0.75`, `1.0` ตามลำดับ (TC4, TC5)
- **ตัวเลือก `"ไม่แน่ใจ"` หรือหัวข้อที่ไม่ได้เลือก**: จัดเก็บเป็น `null` และไม่นำไปคำนวณค่าเฉลี่ย/สถิติของหัวข้อนั้น (TC1, TC2)
- **เงื่อนไขป้องกันข้อผิดพลาด (Validation & Reliability)**:
  - ต้องมีหัวข้อที่ประเมินจริงอย่างน้อย 1 หัวข้อ หากว่างทั้งหมดหรือเป็น `"ไม่แน่ใจ"` ทั้งหมดระบบจะปฏิเสธการส่ง (TC11)
  - ตรวจสอบรายวิชาในฐานข้อมูลก่อนบันทึก หากไม่มีรายวิชาจะปฏิเสธและแจ้งเตือน (TC7)
  - ป้องกันการประเมินซ้ำในวิชาเดิม และการกดปุ่มส่งรัวๆ (Double-click / Spam click) ทั้งฝั่ง UI Lock และ Unique Constraint `(course_id, user_id)` (TC8, TC9)
  - กรณีฐานข้อมูลขัดข้อง ระบบจะคืนข้อผิดพลาดโดยไม่แสดงว่าสำเร็จและไม่อัปเดตสถิติใดๆ (TC10)

