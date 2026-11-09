BEGIN;

-- Stop with a clear message instead of silently deleting existing reviews.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM review
        GROUP BY course_id, user_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot add review_course_user_unique: duplicate reviews exist for a course and user';
    END IF;
END $$;

CREATE UNIQUE INDEX review_course_user_unique
    ON review (course_id, user_id);

COMMIT;
