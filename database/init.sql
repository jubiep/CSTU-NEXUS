
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
    instructor_name TEXT
);

-- 4. Reviews
CREATE TABLE review (
    review_id INTEGER GENERATED ALWAYS AS IDENTITY
        PRIMARY KEY,
    section_id VARCHAR(30) NOT NULL
        REFERENCES sections(section_id),
    user_id INTEGER NOT NULL
        REFERENCES mock_user(user_id),
    reviewer_year INTEGER NOT NULL
        CHECK (reviewer_year BETWEEN 1 AND 4),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
