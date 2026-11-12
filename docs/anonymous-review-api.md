# Anonymous Review API

Authentication uses the existing session cookie (`JSESSIONID`) established by TU login. Review ownership comes from the authenticated session; client supplied `userId` is ignored.

## Create

`POST /api/courses/{courseId}/reviews`

Request: `{"sectionId":"cs261_100001","reviewText":"Useful feedback","isAnonymous":true}`. `isAnonymous` is optional and defaults to `false`. Blank `reviewText` returns HTTP 400. Success returns HTTP 201 with `reviewId` and a message.

## Edit own review

`PUT /api/courses/{courseId}/reviews/{reviewId}` accepts `reviewText` and optional `isAnonymous`. Identity and course are not accepted from the request. Only the owner can edit; other users receive HTTP 404.

## List and privacy

`GET /api/courses/{courseId}/reviews?sectionId={sectionId}` returns `isAnonymous`. Anonymous items contain `author:"Anonymous"` and omit `userId` and `studentId`. Non-anonymous items retain existing identity fields. Owner ID remains stored privately for authorization and duplicate-review enforcement.

Apply `backend/database/migrations/003_anonymous_reviews.sql` once to existing PostgreSQL databases. Fresh databases get the column and feed indexes from `backend/database/init.sql`.
