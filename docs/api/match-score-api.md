# Match Score API

## Endpoint

`GET /api/courses/{courseId}/sections/{sectionId}/match-score`

Requires the authenticated `JSESSIONID` session cookie. The score is calculated for the selected section because reviews and teaching style can differ between sections.

## Calculation

The calculation has two rounds:

1. **Build the section profile.** For each of the five review factors, count the review votes for the `0`, `50`, and `100` categories. The category with the most votes represents that factor. If multiple categories tie for the most votes, use their mean score and return all tied labels. Legacy numeric review scores are assigned to the nearest category.
2. **Compare with the student's Preferences.** For teaching method, evaluation, teaching speed, and assignment style, similarity is `100 - abs(preference - sectionProfile)`. For workload, the preference is the amount of work the student can accept, so similarity is `100 - max(0, sectionWorkload - workloadTolerance)`. Each available factor has equal weight.

The total is the rounded arithmetic mean of available factor similarities. A factor is available only when both the student preference and section review profile exist. `coveragePercentage` reports the fraction of the five factors used in the result. The score is clamped to `0–100`.

## Ready response

```json
{
  "status": "READY",
  "score": 86,
  "level": "เข้ากันได้ดีมาก",
  "explanation": "รูปแบบการเรียนของ Section นี้ตรงกับ Preferences ของคุณหลายด้าน ใช้เป็นข้อมูลประกอบการเลือกเรียนได้ และควรพิจารณารายละเอียดแต่ละด้านร่วมด้วย",
  "sectionReviewCount": 12,
  "availableFactorCount": 5,
  "totalFactorCount": 5,
  "coveragePercentage": 100,
  "details": [
    {
      "key": "teachingMethod",
      "label": "แนวทางการสอน",
      "preferenceScore": 100,
      "preferenceLabel": "เน้นปฏิบัติ",
      "sectionProfileScore": 100.0,
      "sectionProfileLabels": ["เน้นปฏิบัติ"],
      "reviewCount": 12,
      "topVoteCount": 8,
      "consensusPercentage": 67,
      "similarityScore": 100,
      "weightPercentage": 20.0
    }
  ]
}
```

`details` contains all five factors. `similarityScore`, preference values, or section profile values can be `null` when that factor is unavailable.

## No data response

When no factor has both a saved preference and at least one valid section review, the endpoint returns HTTP `200` with `status: "NO_DATA"`, `score: null`, an explanation message, and the five factor details. The UI should show the no-data state rather than presenting a fabricated score.

## Errors

- `401`: missing or expired login session.
- `400`: blank course or section identifier.
- `404`: course or section does not exist.
- `503`: database is unavailable.

## Score bands

| Score | Label |
|---|---|
| 80–100 | เข้ากันได้ดีมาก |
| 60–79 | เข้ากันได้ดี |
| 40–59 | เข้ากันได้ปานกลาง |
| 0–39 | เข้ากันได้น้อย |
