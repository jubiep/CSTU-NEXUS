# CSTU-NEXUS — Use Case & Class Diagrams

เอกสารรวบรวม **Use Case Diagram** และ **Design Class Diagram** สำหรับระบบ **CSTU-NEXUS** อ้างอิงตามขอบเขตงาน U1–U5 และสถาปัตยกรรมแบบ **Boundary-Control-Entity (BCE) / Layered Pattern**

---

## สารบัญ
1. [Use Case Diagram & Actor Specifications](#1-use-case-diagram--actor-specifications)
2. [Design Class Diagram (BCE Pattern)](#2-design-class-diagram-bce-pattern)
3. [System Architecture & Deployment Overview](#3-system-architecture--deployment-overview)

---

## 1. Use Case Diagram & Actor Specifications

### 1.1 Use Case Diagram (CSTU-NEXUS)

```mermaid
flowchart LR
    %% Actors
    subgraph Actors [Actors]
        Student["👤 CSTU Student<br/>(นักศึกษา CSTU)"]
        TuAPI["🏢 TU Authentication System<br/>(TU AD Verify API)"]
    end

    %% System Boundary
    subgraph System ["🖥️ CSTU-NEXUS System"]
        U2(["U2: การยืนยันตัวตนและจัดการสิทธิ์<br/>(Login & Verify CSTU)"])
        U4(["U4: ค้นหารายวิชาและ Section"])
        U1(["U1: อ่าน Review รายวิชา/Section"])
        U3(["U3: ตรวจสอบชั้นปีก่อนเขียน Review"])
        U5(["U5: เขียน Review รายวิชาตาม Section"])
    end

    %% Interactions
    Student --> U2
    Student --> U4
    Student --> U1
    Student --> U5

    U2 <-->|REST API| TuAPI

    %% Relationships <<include>>
    U4 -.->|"<<include>> (ต้อง Login)"| U2
    U1 -.->|"<<include>> (ต้อง Login)"| U2
    U5 -.->|"<<include>> (ต้อง Login)"| U2
    U5 -.->|"<<include>> (ตรวจชั้นปี)"| U3
```

### 1.2 ตารางรายละเอียด Use Cases (U1–U5)

| Use Case ID | ชื่อ Use Case | Actor หลัก | ความสัมพันธ์ | วัตถุประสงค์โดยสรุป |
|:---:|---|:---:|:---:|---|
| **U1** | อ่าน Review รายวิชาและ Section จากรุ่นพี่ | CSTU Student | `<<include>> U2` | เปิดดูประวัติรีวิวและคำแนะนำของแต่ละเซกชันจากรุ่นพี่ในหลักสูตร |
| **U2** | การยืนยันตัวตนและการจัดการสิทธิ์ | CSTU Student,<br>TU AD Verify API | - | ตรวจสอบรหัสนักศึกษาและรหัสผ่านผ่านระบบของมหาวิทยาลัย และคัดกรองเฉพาะ นศ. CSTU |
| **U3** | ตรวจสอบชั้นปีก่อนเขียน Review | CSTU Student (System Rule) | `<<included by>> U5` | ตรวจสอบว่าผู้ใช้มีชั้นปี $\ge$ ชั้นปีที่แนะนำ (`recommended_year`) ของวิชานั้นๆ หรือไม่ |
| **U4** | ค้นหารายวิชาที่ต้องการ และ Section | CSTU Student | `<<include>> U2` | ค้นหารายวิชาจากรหัส/ชื่อวิชา คัดกรองตามชั้นปี และดูรายชื่อเซกชันพร้อมผู้สอน |
| **U5** | เขียน Review รายวิชาตาม Section | CSTU Student | `<<include>> U2, U3` | กรอกข้อความรีวิวประสบการณ์การเรียนและส่งบันทึกแยกตาม Section |

---

## 2. Design Class Diagram (BCE Pattern)

แผนภาพแสดงโครงสร้างคลาสเชิงสถิต (Static Structure) แบ่งตามรูปแบบ **Boundary-Control-Entity (BCE)** สอดคล้องกับ Sequence Diagrams และการพัฒนาจริง:

```mermaid
classDiagram
    %% =======================
    %% BOUNDARY LAYER (FRONTEND)
    %% =======================
    class LoginView {
        +submitLogin(username, password)
        +showErrorMessage(message)
        +redirectHome()
    }

    class CourseSearchView {
        -keyword: String
        -selectedYear: Integer
        +searchCourses(keyword, year)
        +renderCourseList(courses)
    }

    class CourseDetailView {
        -currentCourseId: String
        -selectedSectionId: String
        +loadCourseDetail(courseId)
        +displaySections(sections)
        +displayReviews(reviews)
    }

    class WriteReviewView {
        -courseId: String
        -sectionId: String
        -reviewText: String
        +checkEligibility()
        +submitReview()
        +showDeniedModal()
    }

    %% =======================
    %% CONTROL LAYER (BACKEND)
    %% =======================
    class AuthController {
        -tuAuthService: TuAuthService
        +login(request): LoginResponse
        +currentUser(request): AuthUser
        +logout(request): ResponseEntity
    }

    class TuAuthService {
        -restClient: RestClient
        -verifyUrl: String
        -applicationKey: String
        +verify(username, password): AuthUser
    }

    class StudyYearCalculator {
        +calculate(studentId): Integer
    }

    class CourseController {
        -jdbcTemplate: JdbcTemplate
        +searchCourses(keyword, year): List~CourseSummary~
        +getCourse(courseId): CourseDetail
    }

    class ReviewController {
        -jdbcTemplate: JdbcTemplate
        +getCourseReviews(courseId, sectionId): List~ReviewResponse~
        +checkEligibility(courseId, session): EligibilityResult
        +createReview(courseId, request, session): ReviewResponse
    }

    %% =======================
    %% ENTITY / MODEL LAYER
    %% =======================
    class AuthUser {
        +displayName: String
        +studentId: String
        +currentYear: Integer
        +faculty: String
        +department: String
        +email: String
    }

    class CourseDetail {
        +courseId: String
        +courseName: String
        +recommendedYear: Integer
        +sections: List~Section~
    }

    class Section {
        +sectionId: String
        +sectionNumber: String
        +instructorName: String
    }

    class Review {
        +reviewId: Integer
        +courseId: String
        +sectionId: String
        +userId: Integer
        +reviewerYear: Integer
        +reviewText: String
        +createdAt: Timestamp
    }

    %% =======================
    %% RELATIONSHIPS
    %% =======================
    LoginView ..> AuthController : HTTP Calls
    CourseSearchView ..> CourseController : HTTP Calls
    CourseDetailView ..> CourseController : HTTP Calls
    CourseDetailView ..> ReviewController : HTTP Calls
    WriteReviewView ..> ReviewController : HTTP Calls

    AuthController --> TuAuthService : uses
    AuthController ..> StudyYearCalculator : calls
    AuthController ..> AuthUser : returns

    CourseController ..> CourseDetail : produces
    CourseDetail "1" *-- "*" Section : contains

    ReviewController ..> Review : manages
    ReviewController ..> AuthUser : verifies session
```

---

## 3. System Architecture & Deployment Overview

ภาพรวมการวางระบบ (Deployment / Architecture View):

```mermaid
flowchart TD
    subgraph ClientTier ["🖥️ Client Tier (Frontend)"]
        Browser["Web Browser (Vanilla HTML5 / CSS3 / ES6 JavaScript)"]
    end

    subgraph AppTier ["⚙️ Application Tier (Backend)"]
        SpringBoot["Spring Boot 4.1.x (Java 17)<br/>- Spring Web MVC (REST APIs)<br/>- Spring JDBC (JdbcTemplate)<br/>- HttpOnly Session Security"]
    end

    subgraph DataTier ["🗄️ Data Tier (PostgreSQL)"]
        Postgres[("PostgreSQL 17 Database<br/>- courses, sections<br/>- mock_user, review")]
    end

    subgraph ExternalTier ["🌐 External Services"]
        TuAD["TU Authentication API<br/>(https://restapi.tu.ac.th)"]
    end

    Browser <-->|HTTP / JSON (REST APIs)<br/>Port 8080| SpringBoot
    SpringBoot <-->|JDBC (Port 5432)| Postgres
    SpringBoot <-->|HTTPS POST (Application-Key)| TuAD
```
