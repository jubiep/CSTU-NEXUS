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

        Integer userIdInt = deriveUserId(studentId);
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

        Integer userIdInt = deriveUserId(studentId);
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