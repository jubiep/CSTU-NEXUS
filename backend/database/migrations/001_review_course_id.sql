BEGIN;

-- 1. Allow PostgreSQL to validate course + section pairs.
ALTER TABLE sections
    ADD CONSTRAINT sections_course_section_unique
    UNIQUE (course_id, section_id);

-- 2. Add course_id to existing reviews.
ALTER TABLE review
    ADD COLUMN course_id VARCHAR(20);

-- 3. Backfill course_id from each review's existing section.
UPDATE review AS r
SET course_id = s.course_id
FROM sections AS s
WHERE s.section_id = r.section_id;

-- 4. Every review must belong to a course.
ALTER TABLE review
    ALTER COLUMN course_id SET NOT NULL;

ALTER TABLE review
    ADD CONSTRAINT review_course_id_fkey
    FOREIGN KEY (course_id)
    REFERENCES courses(course_id);

-- 5. Replace the old section-only foreign key.
ALTER TABLE review
    DROP CONSTRAINT review_section_id_fkey;

-- 6. A course-level review does not need a section.
ALTER TABLE review
    ALTER COLUMN section_id DROP NOT NULL;

-- 7. If a section is specified, it must belong to the same course.
ALTER TABLE review
    ADD CONSTRAINT review_section_course_match
    FOREIGN KEY (course_id, section_id)
    REFERENCES sections(course_id, section_id);

COMMIT;
