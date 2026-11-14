
-- 1. Users
CREATE TABLE mock_user (
    user_id INTEGER PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL UNIQUE,
    display_name_th VARCHAR(200) NOT NULL,
    current_year INTEGER NOT NULL
        CHECK (current_year BETWEEN 1 AND 4),
    faculty VARCHAR(150) NOT NULL,
    department VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

-- 2. Courses
CREATE TABLE courses (
    course_id VARCHAR(20) PRIMARY KEY,
    course_name VARCHAR(200) NOT NULL,
    recommended_year INTEGER NOT NULL
        CHECK (recommended_year BETWEEN 1 AND 4)
);

-- 3. Sections
CREATE TABLE sections (
    course_id VARCHAR(20) NOT NULL
        REFERENCES courses(course_id),
    section_id VARCHAR(30) PRIMARY KEY,
    section_number VARCHAR(10) NOT NULL,
    instructor_name TEXT,

    CONSTRAINT sections_course_section_unique
        UNIQUE (course_id, section_id)
);

-- 4. Reviews
CREATE TABLE review (
    review_id INTEGER GENERATED ALWAYS AS IDENTITY
        PRIMARY KEY,
    course_id VARCHAR(20) NOT NULL
        REFERENCES courses(course_id),
    section_id VARCHAR(30),
    user_id INTEGER NOT NULL
        REFERENCES mock_user(user_id),
    reviewer_year INTEGER NOT NULL
        CHECK (reviewer_year BETWEEN 1 AND 4),
    review_text TEXT NOT NULL,
    teaching_method_score DOUBLE PRECISION CHECK (teaching_method_score BETWEEN 0 AND 100),
    evaluation_score DOUBLE PRECISION CHECK (evaluation_score BETWEEN 0 AND 100),
    teaching_speed_score DOUBLE PRECISION CHECK (teaching_speed_score BETWEEN 0 AND 100),
    assignment_style_score DOUBLE PRECISION CHECK (assignment_style_score BETWEEN 0 AND 100),
    workload_score DOUBLE PRECISION CHECK (workload_score BETWEEN 0 AND 100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT review_section_course_match
        FOREIGN KEY (course_id, section_id)
        REFERENCES sections(course_id, section_id),

    -- Prevent concurrent duplicate reviews for the same course by one student.
    CONSTRAINT review_course_user_unique
        UNIQUE (course_id, user_id)
);
