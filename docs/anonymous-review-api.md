# API รีวิวแบบปกปิดเลขนักศึกษา

Authentication uses the existing session cookie (`JSESSIONID`) established by TU login. Review ownership comes from the authenticated session; client supplied `userId` is ignored.

## Create

`POST /api/courses/{courseId}/reviews`

Request: `{"sectionId":"cs261_100001","reviewText":"Useful feedback","isAnonymous":true}`. `isAnonymous` is optional and defaults to `false`. Blank `reviewText` returns HTTP 400. Success returns HTTP 201 with `reviewId` and a message.

## Edit own review

`PUT /api/courses/{courseId}/reviews/{reviewId}` accepts `reviewText` and optional `isAnonymous`. Identity and course are not accepted from the request. Only the owner can edit; other users receive HTTP 404.

## List and privacy

`GET /api/courses/{courseId}/reviews?sectionId={sectionId}` returns `isAnonymous`. รายการที่เปิดโหมดปกปิดจะแสดง `author` ตามชั้นปี (`69********`, `68********`, `67********` หรือ `66********`) และไม่ส่ง `userId` หรือ `studentId` กลับมา ส่วนรายการปกติยังส่งข้อมูลผู้รีวิวตามรูปแบบเดิม โดยเก็บ owner ID ไว้ใช้ตรวจสิทธิ์และป้องกันรีวิวซ้ำ

Apply `backend/database/migrations/003_anonymous_reviews.sql` once to existing PostgreSQL databases. Fresh databases get the column and feed indexes from `backend/database/init.sql`.

## Existing Docker installation / troubleshooting

The Docker entrypoint only runs `init.sql` when the database volume is first created. For an existing volume, apply the migration explicitly, then rebuild the services so the running backend/frontend use the updated source:

```powershell
Get-Content -Raw backend/database/migrations/003_anonymous_reviews.sql | docker compose exec -T db psql -U cstu_nexus_user -d cstu_nexus_db
docker compose up -d --build backend frontend
```

Check `GET /api/courses/{courseId}/reviews` in the browser Network panel. A newly anonymous review must include `"isAnonymous": true` and the masked `author` for its year, with no `studentId` or `userId`. If the flag is false/missing, the running backend is stale or the review was saved before this feature was active. Existing reviews receive `false` during migration; update their `is_anonymous` value explicitly if they were meant to be anonymous. Do not infer anonymity from the author's identity.
