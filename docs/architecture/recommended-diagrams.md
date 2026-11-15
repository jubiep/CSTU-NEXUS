# CSTU-NEXUS — Recommended UML Diagrams

เอกสารรวบรวมคำแนะนำและโค้ดแผนภาพ (Mermaid Diagrams) เพิ่มเติมที่ควรจัดทำสำหรับโครงงาน **CS261 / CS264 Introduction to Software Engineering** เพื่อให้รายงานและเอกสารส่งอาจารย์มีความสมบูรณ์ตามหลักการวิเคราะห์และออกแบบระบบเชิงวัตถุ (OOAD)

---

## สารบัญแผนภาพแนะนำ
1. [Component Diagram (แผนภาพส่วนประกอบระบบ)](#1-component-diagram-แผนภาพส่วนประกอบระบบ)
2. [Activity Diagram: Flow การคำนวณ Match Score Algorithm](#2-activity-diagram-flow-การคำนวณ-match-score-algorithm)
3. [Activity Diagram: Flow การตรวจสอบสิทธิ์เขียนรีวิว (Eligibility Rule)](#3-activity-diagram-flow-การตรวจสอบสิทธิ์เขียนรีวิว-eligibility-rule)
4. [State Machine Diagram: สถานะการรีวิวและโหมดปกปิดตัวตน (Review Lifecycle)](#4-state-machine-diagram-สถานะการรีวิวและโหมดปกปิดตัวตน-review-lifecycle)
5. [State Machine Diagram: วงจรชีวิตของ Session ผู้ใช้งาน (User Authentication Lifecycle)](#5-state-machine-diagram-วงจรชีวิตของ-session-ผู้ใช้งาน-user-authentication-lifecycle)

---

## 1. Component Diagram (แผนภาพส่วนประกอบระบบ)

> 💡 **เหตุผลที่ควรมี:** อาจารย์มักให้ความสำคัญกับการแสดงการแยกส่วน (Modularization) ระหว่าง Frontend Components, Backend Layers และ External Services เพื่อแสดงให้เห็นการเชื่อมต่อผ่าน REST Protocols และ Database Interfaces

```mermaid
flowchart TD
    subgraph ClientComponent ["🖥️ Client Component (Frontend)"]
        UI_Courses["Courses & Search Module"]
        UI_Review["Review & Rating Module"]
        UI_Pref["Preferences Module"]
        UI_Auth["Authentication Module"]
        ApiClient["HTTP API Client (app.js)"]
        
        UI_Courses --> ApiClient
        UI_Review --> ApiClient
        UI_Pref --> ApiClient
        UI_Auth --> ApiClient
    end

    subgraph GatewayComponent ["🌐 Reverse Proxy & Web Server"]
        Nginx["Nginx Reverse Proxy<br/>- Serves Static Web Files<br/>- Routes /api/*"]
    end

    subgraph BackendComponent ["⚙️ Backend Component (Spring Boot)"]
        subgraph Controllers ["Controller Layer"]
            C_Auth["AuthController"]
            C_Course["CourseController"]
            C_Review["ReviewController"]
            C_Pref["PreferenceController"]
            C_Match["MatchScoreController"]
        end

        subgraph Services ["Service / Business Logic Layer"]
            S_Auth["TuAuthService"]
            S_Year["StudyYearCalculator"]
            S_Review["ReviewService"]
            S_Pref["PreferenceService"]
            S_Match["MatchScoreService"]
        end

        subgraph Repositories ["Data Access Layer (DAO / JPA)"]
            R_Review["ReviewRepository"]
            R_Pref["UserPreferencesRepository"]
            R_Match["MatchScoreRepository"]
            R_Jdbc["JdbcTemplate (Courses/Sections)"]
        end

        C_Auth --> S_Auth
        C_Auth --> S_Year
        C_Course --> R_Jdbc
        C_Review --> S_Review
        C_Review --> S_Year
        C_Pref --> S_Pref
        C_Match --> S_Match

        S_Review --> R_Review
        S_Pref --> R_Pref
        S_Match --> R_Match
        S_Match --> R_Pref
    end

    subgraph ExternalComponent ["🏢 External Services & Data"]
        TuAPI["TU AD Verify API<br/>(https://restapi.tu.ac.th)"]
        PostgresDB[("PostgreSQL 17 Database")]
    end

    ApiClient -->|"HTTP / Same-Origin"| Nginx
    Nginx -->|"Proxy Pass /api/*"| Controllers
    S_Auth -->|"HTTPS POST (TU Key)"| TuAPI
    Repositories -->|"JDBC / SQL"| PostgresDB
```

---

## 2. Activity Diagram: Flow การคำนวณ Match Score Algorithm

> 💡 **เหตุผลที่ควรมี:** ฟีเจอร์ Match Score เป็นจุดเด่น (Key Innovation) ของระบบ CSTU-NEXUS การมี Activity Diagram จะช่วยอธิบายวิธีการคำนวณ 2 รอบ (รอบ Section Profile Consensus และรอบ Student Similarity) ได้อย่างชัดเจนในการนำเสนอ

```mermaid
flowchart TD
    Start([เริ่มต้น: เรียกดู Match Score ของเซกชัน]) --> CheckPref{ผู้ใช้มีบันทึก<br/>Preferences หรือไม่?}
    
    CheckPref -- ไม่พบ --> ReturnNoData["ส่งสถานะ NO_DATA<br/>(ยังไม่มีข้อมูลเพียงพอ)"]
    CheckPref -- มีข้อมูล --> QueryReviews["ดึงประวัติรีวิวและคะแนนเกณฑ์ 5 ด้านของ Section"]
    
    QueryReviews --> CheckReviews{มีรีวิวใน Section<br/>อย่างน้อย 1 รายการหรือไม่?}
    CheckReviews -- ไม่มี --> ReturnNoData
    
    CheckReviews -- มีรีวิว --> Round1["รอบที่ 1: สร้าง Section Profile<br/>- นับคะแนนโหวตในระดับ 0, 50, 100 ของแต่ละด้าน<br/>- เลือกระดับคะแนนที่ได้โหวตสูงสุดเป็นตัวแทนด้านนั้น"]
    
    Round1 --> Round2["รอบที่ 2: เปรียบเทียบกับ Preference ของนักศึกษา<br/>สำหรับแต่ละด้านที่มีข้อมูลทั้งคู่:"]
    
    Round2 --> CalcStandard["4 ด้านแรก (แนวสอน, สัดส่วนคะแนน, ความเร็ว, รูปแบบงาน):<br/>Similarity = 100 - |Preference - SectionProfile|"]
    
    CalcStandard --> CalcWorkload["ด้าน Workload Tolerance (ความทนทานต่องาน):<br/>Similarity = 100 - max(0, SectionWorkload - WorkloadTolerance)"]
    
    CalcWorkload --> CalcTotal["คำนวณ Match Percentage เฉลี่ยรวม:<br/>Total Score = ผลรวม Similarity ของด้านที่พร้อม / จำนวนด้านที่พร้อม"]
    
    CalcTotal --> Clamp["จำกัดคะแนนให้อยู่ในช่วง 0 - 100%"]
    Clamp --> ClassifyLevel["ประเมินระดับความเข้ากันได้:<br/>- 80-100%: เข้ากันได้ดีมาก<br/>- 60-79%: เข้ากันได้ดี<br/>- 40-59%: ปานกลาง<br/>- ต่ำกว่า 40%: อาจไม่ตรงสไตล์"]
    
    ClassifyLevel --> ReturnReady["ส่งผลลัพธ์สถานะ READY พร้อมคะแนนและรายละเอียดแต่ละด้าน"]
    ReturnReady --> End([สิ้นสุด])
    ReturnNoData --> End
```

---

## 3. Activity Diagram: Flow การตรวจสอบสิทธิ์เขียนรีวิว (Eligibility Rule)

> 💡 **เหตุผลที่ควรมี:** แสดงการควบคุม Business Rule อย่างเข้มงวดของระบบ (U3 & U5)

```mermaid
flowchart TD
    Start([ผู้ใช้เปิดหน้ารายละเอียดรายวิชา]) --> CheckSession{มี Session การ Login<br/>ที่ถูกต้องหรือไม่?}
    
    CheckSession -- ไม่มี --> DisableButton["ปิดปุ่มเขียนรีวิว<br/>(Disabled: ต้องเข้าสู่ระบบก่อน)"]
    
    CheckSession -- มี Session --> GetInfo["ดึงรหัสนักศึกษาและชั้นปีปัจจุบัน (Current Year)<br/>และดึงชั้นปีแนะนำของวิชา (Recommended Year)"]
    
    GetInfo --> CheckYear{Current Year ><br/>Recommended Year ?}
    
    CheckYear -- ไม่ใช่ (ชั้นปียังไม่ถึง) --> ShowYearDenied["ปิดปุ่มเขียนรีวิว<br/>(Disabled: เปิดให้เฉพาะรุ่นพี่ที่เคยผ่านวิชานี้)"]
    
    CheckYear -- ใช่ (เป็นรุ่นพี่) --> CheckHistory{เคยส่งรีวิววิชานี้<br/>ไปแล้วหรือไม่?}
    
    CheckHistory -- เคยส่งแล้ว --> ShowAlreadyReviewed["เปลี่ยนปุ่มเป็น 'แก้ไขรีวิวของฉัน'<br/>(ป้องกันการปั๊มรีวิวซ้ำ)"]
    
    CheckHistory -- ยังไม่เคยส่ง --> EnableButton["เปิดปุ่ม 'เขียนรีวิว'<br/>(Active / Enabled)"]
    
    EnableButton --> End([สิ้นสุด])
    DisableButton --> End
    ShowYearDenied --> End
    ShowAlreadyReviewed --> End
```

---

## 4. State Machine Diagram: สถานะการรีวิวและโหมดปกปิดตัวตน (Review Lifecycle)

> 💡 **เหตุผลที่ควรมี:** แสดงวงจรชีวิตของรีวิวตั้งแต่ยังไม่มีสิทธิ์ จนถึงการบันทึกแบบเปิดเผยตัวตน หรือแบบ Anonymous Masking

```mermaid
stateDiagram-v2
    [*] --> Ineligible : นักศึกษาชั้นปียังไม่ถึงเกณฑ์ / ยังไม่ Login
    
    Ineligible --> Eligible : เลื่อนชั้นปี / เข้าสู่ระบบสำเร็จ (Current Year > Recommended Year)
    
    Eligible --> Drafting : กดปุ่ม "เขียนรีวิว" เข้าสู่หน้าฟอร์ม
    
    Drafting --> PublicReview : บันทึกรีวิวโดยไม่เปิดสวิตช์ Anonymous
    Drafting --> AnonymousReview : บันทึกรีวิวโดยเปิดสวิตช์ "ไม่เปิดเผยตัวตน"
    
    state PublicReview {
        [*] --> DisplayWithAuthor : แสดงชื่อ/รหัสนักศึกษาแบบเต็ม
    }
    
    state AnonymousReview {
        [*] --> DisplayMasked : แสดงรหัสพรางตัวตามรุ่น (เช่น 68********)
        DisplayMasked --> HideStudentId : ไม่ส่ง Student ID / User ID ผ่าน API
    }
    
    PublicReview --> Editing : เจ้าของรีวิวกดแก้ไข
    AnonymousReview --> Editing : เจ้าของรีวิวกดแก้ไข
    
    Editing --> PublicReview : บันทึกการแก้ไข (แบบเปิดเผย)
    Editing --> AnonymousReview : บันทึกการแก้ไข (แบบ Anonymous)
```

---

## 5. State Machine Diagram: วงจรชีวิตของ Session ผู้ใช้งาน (User Authentication Lifecycle)

> 💡 **เหตุผลที่ควรมี:** แสดงการจัดการ Session และความปลอดภัย (Security Architecture)

```mermaid
stateDiagram-v2
    [*] --> Guest : เปิดเข้าสู่ระบบครั้งแรก
    
    Guest --> Authenticating : ส่งรหัสนักศึกษาและรหัสผ่าน (POST /api/auth/login)
    
    Authenticating --> Guest : ตรวจสอบไม่ผ่าน / รหัสผิด / ไม่ใช่ CSTU
    Authenticating --> Authenticated : ยืนยันสำเร็จ (สร้าง HttpOnly JSESSIONID)
    
    state Authenticated {
        [*] --> ActiveSession
        ActiveSession --> ActiveSession : ร้องขอ API ต่างๆ (ต่ออายุ Session อัตโนมัติ)
    }
    
    Authenticated --> Guest : ผู้ใช้กด Logout (POST /api/auth/logout)
    Authenticated --> Expired : ไม่มีความเคลื่อนไหวเกิน 30 นาที (Timeout)
    Expired --> Guest : ระบบบังคับให้ล็อกอินใหม่
```
