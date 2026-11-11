BEGIN;

ALTER TABLE review
    ADD COLUMN teaching_method_score DOUBLE PRECISION,
    ADD COLUMN evaluation_score DOUBLE PRECISION,
    ADD COLUMN teaching_speed_score DOUBLE PRECISION,
    ADD COLUMN assignment_style_score DOUBLE PRECISION,
    ADD COLUMN workload_score DOUBLE PRECISION;

ALTER TABLE review
    ADD CONSTRAINT review_rating_scores_range CHECK (
        (teaching_method_score IS NULL OR teaching_method_score BETWEEN 0 AND 1)
        AND (evaluation_score IS NULL OR evaluation_score BETWEEN 0 AND 1)
        AND (teaching_speed_score IS NULL OR teaching_speed_score BETWEEN 0 AND 1)
        AND (assignment_style_score IS NULL OR assignment_style_score BETWEEN 0 AND 1)
        AND (workload_score IS NULL OR workload_score BETWEEN 0 AND 1)
    );

COMMIT;
