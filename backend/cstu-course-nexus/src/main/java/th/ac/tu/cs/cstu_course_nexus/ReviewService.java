package th.ac.tu.cs.cstu_course_nexus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ตรงตาม U1: getReviews(courseId, sectionId)
    public List<Review> getReviews(String courseId, String sectionId) {
        if (sectionId == null || sectionId.trim().isEmpty()) {
            return reviewRepository.findByCourseIdOrderByCreatedAtDesc(courseId);
        }
        return reviewRepository.findByCourseIdAndSectionIdOrderByCreatedAtDesc(courseId, sectionId.trim());
    }

    public String getStudentId(Integer userId) {
        if (userId == null) return null;
        List<String> studentIds = jdbcTemplate.query(
                "SELECT student_id FROM mock_user WHERE user_id = ?",
                (rs, rowNum) -> rs.getString("student_id"),
                userId);
        return studentIds.isEmpty() ? null : studentIds.get(0);
    }

    public Integer upsertUser(AuthUser user) {
        Integer year = user.year() != null && user.year() >= 1 && user.year() <= 4
                ? user.year()
                : 1;
        return jdbcTemplate.queryForObject("""
                INSERT INTO mock_user (
                    user_id, student_id, display_name_th, current_year, faculty, department, email
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (student_id) DO UPDATE SET
                    display_name_th = EXCLUDED.display_name_th,
                    current_year = EXCLUDED.current_year,
                    faculty = EXCLUDED.faculty,
                    department = EXCLUDED.department,
                    email = EXCLUDED.email
                RETURNING user_id
                """,
                Integer.class,
                deriveUserId(user.studentId()),
                user.studentId(),
                user.displayName(),
                year,
                user.faculty(),
                user.department(),
                user.email());
    }

    private Integer findUserId(String studentId) {
        List<Integer> userIds = jdbcTemplate.query(
                "SELECT user_id FROM mock_user WHERE student_id = ?",
                (rs, rowNum) -> rs.getInt("user_id"),
                studentId);
        return userIds.isEmpty() ? null : userIds.get(0);
    }

    // ตรงตาม U3: checkReviewEligibility(courseId)
    // เงื่อนไข: ชั้นปีที่เรียนอยู่ (studentYear) ต้อง > recommended_year ในตาราง courses และยังไม่เคยรีวิว
    public boolean checkReviewEligibility(String studentId, String courseId) {
        if (studentId == null || studentId.isBlank() || courseId == null || courseId.isBlank()) {
            return false;
        }

        Integer studentYear = StudyYearCalculator.calculate(studentId);
        if (studentYear == null || studentYear < 1) {
            return false;
        }

        Integer userIdInt = findUserId(studentId);
        if (userIdInt == null) {
            return false;
        }
        boolean alreadyReviewed = reviewRepository.existsByCourseIdAndUserId(courseId, userIdInt);
        if (alreadyReviewed) {
            return false;
        }

        try {
            List<Integer> recommendedYears = jdbcTemplate.query(
                "SELECT recommended_year FROM courses WHERE UPPER(course_id) = UPPER(?)",
                (rs, rowNum) -> rs.getInt("recommended_year"),
                courseId.trim()
            );

            if (recommendedYears.isEmpty()) {
                return false;
            }

            int recommendedYear = recommendedYears.get(0);
            return studentYear > recommendedYear;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ดึงข้อมูลสถานะสิทธิ์รีวิวพร้อมเหตุผล เพื่อตอบกลับ Frontend ได้อย่างชัดเจน
     */
    public EligibilityStatus getEligibilityStatus(String studentId, String courseId) {
        if (studentId == null || studentId.isBlank()) {
            return new EligibilityStatus(false, null, null, "ไม่พบข้อมูลรหัสนักศึกษา");
        }

        Integer studentYear = StudyYearCalculator.calculate(studentId);
        if (studentYear == null || studentYear < 1) {
            return new EligibilityStatus(false, studentYear, null, "ไม่สามารถคำนวณชั้นปีจากรหัสนักศึกษาได้");
        }

        Integer userIdInt = findUserId(studentId);
        if (userIdInt == null) {
            return new EligibilityStatus(false, studentYear, null, "ไม่พบข้อมูลผู้ใช้ในระบบ กรุณาเข้าสู่ระบบอีกครั้ง");
        }
        boolean alreadyReviewed = reviewRepository.existsByCourseIdAndUserId(courseId, userIdInt);
        if (alreadyReviewed) {
            return new EligibilityStatus(false, studentYear, null, "คุณได้เคยส่งรีวิวรายวิชานี้ไปแล้ว");
        }

        try {
            List<Integer> recommendedYears = jdbcTemplate.query(
                "SELECT recommended_year FROM courses WHERE UPPER(course_id) = UPPER(?)",
                (rs, rowNum) -> rs.getInt("recommended_year"),
                courseId.trim()
            );

            if (recommendedYears.isEmpty()) {
                return new EligibilityStatus(false, studentYear, null, "ไม่พบข้อมูลรายวิชาในระบบ");
            }

            int recommendedYear = recommendedYears.get(0);
            boolean eligible = studentYear > recommendedYear;
            String message = eligible
                ? "คุณได้รับสิทธิ์แบ่งปันประสบการณ์"
                : "วิชานี้แนะนำสำหรับชั้นปีที่ " + recommendedYear + " (ต้องอยู่ชั้นปีที่สูงกว่าจึงจะรีวิวได้ ปัจจุบันคุณอยู่ชั้นปีที่ " + studentYear + ")";

            return new EligibilityStatus(eligible, studentYear, recommendedYear, message);
        } catch (Exception e) {
            return new EligibilityStatus(false, studentYear, null, "เกิดข้อผิดพลาดในการตรวจสอบข้อมูลวิชา");
        }
    }

    public record EligibilityStatus(boolean isEligible, Integer studentYear, Integer recommendedYear, String message) {}

    public boolean hasReviewed(String studentId, String courseId) {
        if (studentId == null || studentId.isBlank() || courseId == null || courseId.isBlank()) {
            return false;
        }
        Integer userId = findUserId(studentId);
        return userId != null && reviewRepository.existsByCourseIdAndUserId(courseId, userId);
    }

    /**
     * Map a student ID safely to a positive 32-bit integer for user_id storage
     */
    public static Integer deriveUserId(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            return 1;
        }
        int hash = Math.abs(studentId.hashCode() % 1_000_000_000);
        return hash == 0 ? 1 : hash;
    }

    // ตรงตาม U5: saveReview
    public Review saveReview(Review review) {
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }
        return reviewRepository.save(review);
    }
}
