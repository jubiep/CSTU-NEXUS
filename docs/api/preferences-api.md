# Learning Preferences API

The Preferences page stores the signed-in student's five learning preferences. Each preference is an integer from `0` to `100`, or `null` when the student has not selected a value. The server identifies the user from the authenticated session; clients must not submit a user or student ID.

These values are read by the Match Score calculation (`GET /api/courses/{courseId}/sections/{sectionId}/match-score`).

## Endpoints

### `GET /api/users/preferences`

Requires the existing `JSESSIONID` session cookie. Returns the saved preferences, or five `null` values when the student has not saved preferences yet.

```json
{
  "preferredTeachingMethod": 0,
  "preferredEvaluation": 50,
  "preferredTeachingSpeed": 0,
  "preferredAssignmentStyle": 100,
  "preferredWorkload": 50
}
```

### `PUT /api/users/preferences`

Requires the existing `JSESSIONID` session cookie. Saves all five values with an upsert. Each value must be an integer from `0` to `100`, or `null`.

```json
{
  "preferredTeachingMethod": 0,
  "preferredEvaluation": 50,
  "preferredTeachingSpeed": 0,
  "preferredAssignmentStyle": 100,
  "preferredWorkload": 50
}
```

Returns the saved object.

| Status | When |
|---|---|
| `400` | A value is outside `0`–`100`, is not an integer, or the body is malformed |
| `401` | The session is missing or expired |
| `503` | The database is unavailable |

## Preference scale

The Preferences page sends `0`, `50`, or `100`; the API also accepts any integer in between.

| Field | `0` | `50` | `100` |
|---|---|---|---|
| `preferredTeachingMethod` | เน้นทฤษฎี | สมดุล | เน้นปฏิบัติ |
| `preferredEvaluation` | เน้นคะแนนสอบ | สมดุล | เน้นโครงงาน |
| `preferredTeachingSpeed` | สอนช้า | ปานกลาง | สอนเร็ว |
| `preferredAssignmentStyle` | เน้นงานเดี่ยว | สมดุล | เน้นงานกลุ่ม |
| `preferredWorkload` | รับงานได้น้อย | รับงานได้ปานกลาง | รับงานได้มาก |

The `preferredWorkload` field represents how much work a student can accept, not the amount they prefer. Match scoring awards full similarity when a section's workload is within that tolerance and reduces it when workload exceeds it.

## Database setup

New databases receive the `user_preferences` table from `backend/database/init.sql`. For an existing database, apply `005_user_preferences.sql` once, then `006_drop_learning_preference.sql` to remove the legacy `learning_preference` table:

```powershell
Get-Content -Raw backend/database/migrations/005_user_preferences.sql | docker exec -i cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db -v ON_ERROR_STOP=1
Get-Content -Raw backend/database/migrations/006_drop_learning_preference.sql | docker exec -i cstu-nexus-postgres psql -U cstu_nexus_user -d cstu_nexus_db -v ON_ERROR_STOP=1
```
