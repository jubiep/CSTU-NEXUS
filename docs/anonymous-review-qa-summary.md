# สรุป QA: รีวิวแบบปกปิดเลขนักศึกษา

| Case | Automated coverage | Status |
| --- | --- | --- |
| TC1 โพสต์แบบปกปิดตัวตน | RV13 accepts and persists `isAnonymous=true` | Added; execution pending |
| TC2 โพสต์แบบปกติ | Entity default is false | Execution pending |
| TC3 แก้รีวิวที่ปกปิดตัวตน | PUT preserves status when omitted | Execution pending |
| TC4 เนื้อหาว่าง | RV04 rejects blank review | Existing test; execution pending |
| TC5 ซ่อนข้อมูลผู้โพสต์ | RV14 asserts `userId`/`studentId` absent and masked prefix returned | Added; execution pending |
| Authorization | RV15 rejects edits without owner match | Added; execution pending |

Browser integration, migration execution, performance/load testing, monitoring alert delivery, and QA/Product Owner sign-off require the configured runtime and remain pending. No claim of passed execution is made until the test suite can run in the project environment.
