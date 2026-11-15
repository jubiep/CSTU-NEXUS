CREATE TABLE IF NOT EXISTS learning_preference (
    student_id VARCHAR(20) PRIMARY KEY,
    teaching_style SMALLINT CHECK (teaching_style IN (0, 50, 100)),
    assessment_style SMALLINT CHECK (assessment_style IN (0, 50, 100)),
    teaching_speed SMALLINT CHECK (teaching_speed IN (0, 50, 100)),
    work_format SMALLINT CHECK (work_format IN (0, 50, 100)),
    workload_tolerance SMALLINT CHECK (workload_tolerance IN (0, 50, 100)),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
