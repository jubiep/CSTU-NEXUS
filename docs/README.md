# CSTU-NEXUS — Documentation Index

ศูนย์รวมเอกสารการวิเคราะห์ ออกแบบ สถาปัตยกรรม ข้อกำหนด และผลการทดสอบระบบ **CSTU-NEXUS** สำหรับรายวิชา **CS261 / CS264 Introduction to Software Engineering**

---

## โครงสร้างหมวดหมู่เอกสาร (Documentation Directory)

```text
docs/
├── requirements/              # ข้อกำหนดความต้องการ (User Stories & Detailed Use Cases)
│   ├── user-stories.md        # Agile User Stories แยกตาม Persona, Sprint และ Acceptance Criteria
│   └── use-cases.md           # ข้อกำหนด Use Case U1–U9 ละเอียด (Pre/Post-conditions, Basic/Alt Flows)
│
├── architecture/              # สถาปัตยกรรมระบบและแผนภาพ UML เชิงโครงสร้าง
│   ├── architecture.md        # สถาปัตยกรรมระบบ 3-Tier, BCE Pattern, Security, และ Deployment
│   ├── use-case-and-class-diagrams.md # แผนภาพ Use Case Diagram และ Design Class Diagram (BCE)
│   ├── sequence-diagrams.md   # Sequence Diagrams ทุก Use Case (U1–U9)
│   ├── entity-database-table-mapping.md # ER Diagram, Database Table Mapping & API-DAO
│   └── recommended-diagrams.md # แผนภาพแนะนำเพิ่มเติม (Component, Activity, และ State Machine)
│
├── api/                       # เอกสารข้อกำหนดและสัญญาของ REST APIs (API Specifications)
│   ├── preferences-api.md     # Learning Preferences API (GET / PUT)
│   ├── match-score-api.md     # Match Score API และเกณฑ์การคำนวณ
│   └── anonymous-review-api.md # Anonymous Review API และการพรางข้อมูลผู้รีวิว
│
├── testing/                   # เอกสารเคสทดสอบและการประกันคุณภาพ (QA & Test Cases)
│   ├── test-cases-authentication.md       # รายงานและ Test Cases ระบบยืนยันตัวตน (TU Auth)
│   ├── test-cases-course-section-search.md # Test Cases การค้นหารายวิชาและเซกชัน
│   ├── test-cases-learning-preferences.md # รายงานผลการทดสอบ Learning Preferences (U7)
│   ├── test-cases-review-and-study-year.md # Test Cases ตรวจสอบสิทธิ์ชั้นปีและระบบรีวิว
│   └── anonymous-review-qa-summary.md     # สรุปผลการทดสอบระบบรีวิวแบบปกปิดตัวตน
│
├── UXUI-Design/               # เอกสารการออกแบบ UI/UX จาก Figma
│   ├── Hifi-mobile-sprint1.pdf # ดีไซน์ Hi-Fi Mobile Sprint 1
│   └── Hifi-mobile-sprint2.pdf # ดีไซน์ Hi-Fi Mobile Sprint 2
│
└── group10_100001_CSTU_NEXUS.pdf # รายงานโครงงานฉบับสมบูรณ์
```

---

## สารบัญเอกสารแยกตามหมวดหมู่

### 1. ข้อกำหนดและความต้องการ (Requirements)
* [User Stories](requirements/user-stories.md) — ข้อกำหนดความต้องการของผู้ใช้งานในรูปแบบ Agile (Persona, Want, Value, Acceptance Criteria) ครอบคลุม Sprint 1 และ Sprint 2
* [Use Case Specifications](requirements/use-cases.md) — รายละเอียดข้อกำหนดการทำงานของ Use Cases ทั้งหมด (U1–U9) พร้อมตาราง Traceability Matrix เชื่อมโยง User Story ↔ API ↔ UI

### 2. สถาปัตยกรรมและการออกแบบระบบ (Architecture & Design)
* [System Architecture](architecture/architecture.md) — สถาปัตยกรรมระบบ 3-Tier, BCE Pattern, ระบบรักษาความปลอดภัย, สถาปัตยกรรมฐานข้อมูล และ Docker Multi-container Deployment
* [Use Case & Class Diagrams](architecture/use-case-and-class-diagrams.md) — แผนภาพ Use Case Diagram (U1–U9) และ Design Class Diagram ตามแพตเทิร์น BCE
* [Sequence Diagrams](architecture/sequence-diagrams.md) — แผนภาพลำดับการทำงานและ Interaction Flow U1 ถึง U9
* [Entity & Database Table Mapping](architecture/entity-database-table-mapping.md) — แผนภาพ ER Diagram, โครงสร้างตาราง PostgreSQL, และ Data Access Object (DAO) Mapping
* [Recommended UML Diagrams](architecture/recommended-diagrams.md) — แผนภาพแนะนำเพิ่มเติมสำหรับส่งอาจารย์ (Component Diagram, Activity Diagrams สำหรับ Match Score และ Eligibility Check, และ State Machine Diagrams)

### 3. ข้อกำหนด API (API Specifications)
* [Learning Preferences API](api/preferences-api.md) — ข้อกำหนดและ Schema ของ API จัดการความชอบในการเรียน 5 ด้าน
* [Match Score API](api/match-score-api.md) — ข้อกำหนดของ API คำนวณเปอร์เซ็นต์ความเข้ากันได้ระหว่างสไตล์ของนักศึกษากับเซกชัน
* [Anonymous Review API](api/anonymous-review-api.md) — ข้อกำหนดของ API สำหรับการเขียนและอ่านรีวิวแบบไม่เปิดเผยตัวตน

### 4. การทดสอบและการประกันคุณภาพ (Testing & QA)
* [Test Cases: Authentication](testing/test-cases-authentication.md) — ผลการทดสอบการเข้าสู่ระบบและตรวจสอบสิทธิ์นักศึกษา CSTU
* [Test Cases: Course & Section Search](testing/test-cases-course-section-search.md) — ผลการทดสอบค้นหาและคัดกรองรายวิชา
* [Test Cases: Learning Preferences](testing/test-cases-learning-preferences.md) — ผลการทดสอบโหลด ปรับ และบันทึก Preferences
* [Test Cases: Review & Study Year](testing/test-cases-review-and-study-year.md) — ผลการทดสอบการตรวจสอบสิทธิ์ชั้นปี และการส่งรีวิว
* [QA Summary: Anonymous Review](testing/anonymous-review-qa-summary.md) — สรุปผลการประกันคุณภาพฟีเจอร์รีวิวแบบปกปิดตัวตน
