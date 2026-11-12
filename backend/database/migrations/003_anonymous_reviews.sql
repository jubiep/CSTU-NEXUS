-- Persist anonymous visibility while retaining owner identity for authorization.
ALTER TABLE review
    ADD COLUMN is_anonymous BOOLEAN NOT NULL DEFAULT FALSE;

-- Support the course and section review feed ordering.
CREATE INDEX review_course_created_at_idx
    ON review (course_id, created_at DESC);
CREATE INDEX review_course_section_created_at_idx
    ON review (course_id, section_id, created_at DESC);
