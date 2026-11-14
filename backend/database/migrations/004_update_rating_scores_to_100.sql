BEGIN;

ALTER TABLE review
    DROP CONSTRAINT IF EXISTS review_rating_scores_range;

ALTER TABLE review
    ADD CONSTRAINT review_rating_scores_range CHECK (
        (teaching_method_score IS NULL OR teaching_method_score BETWEEN 0 AND 100)
        AND (evaluation_score IS NULL OR evaluation_score BETWEEN 0 AND 100)
        AND (teaching_speed_score IS NULL OR teaching_speed_score BETWEEN 0 AND 100)
        AND (assignment_style_score IS NULL OR assignment_style_score BETWEEN 0 AND 100)
        AND (workload_score IS NULL OR workload_score BETWEEN 0 AND 100)
    );

COMMIT;
