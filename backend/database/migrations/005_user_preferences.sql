BEGIN;

CREATE TABLE user_preferences (
    user_id INTEGER PRIMARY KEY
        REFERENCES mock_user(user_id) ON DELETE CASCADE,
    preferred_teaching_method INTEGER
        CHECK (preferred_teaching_method BETWEEN 0 AND 100),
    preferred_evaluation INTEGER
        CHECK (preferred_evaluation BETWEEN 0 AND 100),
    preferred_teaching_speed INTEGER
        CHECK (preferred_teaching_speed BETWEEN 0 AND 100),
    preferred_assignment_style INTEGER
        CHECK (preferred_assignment_style BETWEEN 0 AND 100),
    preferred_workload INTEGER
        CHECK (preferred_workload BETWEEN 0 AND 100)
);

COMMIT;
