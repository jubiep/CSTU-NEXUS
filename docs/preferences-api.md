# Learning Preferences API

The Preferences page stores the signed-in student's five learning preferences. Each preference accepts `0`, `50`, `100`, or `null` when the student has not selected a value. The server derives `studentId` from the authenticated session; clients must not submit a student ID.

## Endpoints

### `GET /api/preferences`

Requires the existing `JSESSIONID` session cookie. Returns the saved preferences, or five `null` values when the student has not saved preferences yet.

```json
{
  "teachingStyle": 0,
  "assessmentStyle": 50,
  "teachingSpeed": 0,
  "workFormat": 100,
  "workloadTolerance": 50
}
```

### `PUT /api/preferences`

Requires the existing `JSESSIONID` session cookie. Saves all five values with an upsert. Values must be `0`, `50`, `100`, or `null`.

```json
{
  "teachingStyle": 0,
  "assessmentStyle": 50,
  "teachingSpeed": 0,
  "workFormat": 100,
  "workloadTolerance": 50
}
```

Returns the saved object. Invalid values return HTTP `400`; a missing or expired session returns HTTP `401`.

## Preference scale

| Field | `0` | `50` | `100` |
|---|---|---|---|
| `teachingStyle` | เน้นทฤษฎี | สมดุล | เน้นปฏิบัติ |
| `assessmentStyle` | เน้นคะแนนสอบ | สมดุล | เน้นโครงงาน |
| `teachingSpeed` | สอนช้า | ปานกลาง | สอนเร็ว |
| `workFormat` | เน้นงานเดี่ยว | สมดุล | เน้นงานกลุ่ม |
| `workloadTolerance` | รับงานได้น้อย | รับงานได้ปานกลาง | รับงานได้มาก |

The `workloadTolerance` field represents how much work a student can accept, not the amount they prefer. Match scoring should award full similarity when a section's workload is within that tolerance and reduce it when workload exceeds it.

## Database setup

New databases receive the `learning_preference` table from `backend/database/init.sql`. Apply `backend/database/migrations/004_learning_preferences.sql` to an existing database before using the page.

```powershell
Get-Content -Raw backend/database/migrations/004_learning_preferences.sql | docker exec -i cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db -v ON_ERROR_STOP=1
```
