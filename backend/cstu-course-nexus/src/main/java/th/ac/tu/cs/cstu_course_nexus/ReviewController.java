package th.ac.tu.cs.cstu_course_nexus;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class ReviewController {

    private static final String AUTH_USER_SESSION_KEY = "authenticatedUser";

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<Map<String, Object>>> getReviews(
            @PathVariable("id") String courseId,
            @RequestParam(value = "sectionId", required = false) String sectionId,
            HttpSession session) {
        
        List<Review> reviewList = reviewService.getReviews(courseId, sectionId);
        List<Map<String, Object>> response = reviewList.stream().map(review -> {
            Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("id", review.getId());
            item.put("courseId", review.getCourseId());
            item.put("sectionId", review.getSectionId());
            item.put("year", review.getReviewerYear());
            item.put("reviewerYear", review.getReviewerYear());
            item.put("text", review.getReviewText());
            item.put("comment", review.getReviewText());
            item.put("teachingMethodScore", review.getTeachingMethodScore());
            item.put("evaluationScore", review.getEvaluationScore());
            item.put("teachingSpeedScore", review.getTeachingSpeedScore());
            item.put("assignmentStyleScore", review.getAssignmentStyleScore());
            item.put("workloadScore", review.getWorkloadScore());
            item.put("date", review.getDate());
            boolean anonymous = Boolean.TRUE.equals(review.isAnonymous());
            item.put("isAnonymous", anonymous);
            if (anonymous) {
                item.put("author", maskedStudentIdForYear(review.getReviewerYear()));
            } else {
                item.put("userId", review.getUserId());
                item.put("studentId", reviewService.getStudentId(review.getUserId()));
            }
            return item;
        }).toList();

        return ResponseEntity.ok(response);
    }

    private String maskedStudentIdForYear(Integer reviewerYear) {
        return switch (reviewerYear == null ? 0 : reviewerYear) {
            case 1 -> "69********";
            case 2 -> "68********";
            case 3 -> "67********";
            case 4 -> "66********";
            default -> "**********";
        };
    }

    @GetMapping("/{id}/reviews/eligibility")
    public ResponseEntity<?> checkReviewEligibility(
            @PathVariable("id") String courseId,
            HttpSession session) {
        
        Object sessionUser = session.getAttribute(AUTH_USER_SESSION_KEY);
        if (!(sessionUser instanceof AuthUser authUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("eligible", false, "message", "กรุณาเข้าสู่ระบบก่อนเช็กสิทธิ์"));
        }

        String studentId = authUser.studentId(); 
        reviewService.upsertUser(authUser);
        ReviewService.EligibilityStatus status = reviewService.getEligibilityStatus(studentId, courseId);

        return ResponseEntity.ok(Map.of(
            "eligible", status.isEligible(),
            "alreadyReviewed", reviewService.hasReviewed(studentId, courseId),
            "studentYear", status.studentYear() != null ? status.studentYear() : 1,
            "recommendedYear", status.recommendedYear() != null ? status.recommendedYear() : 0,
            "message", status.message()
        ));
    }

    @PostMapping("/{id}/reviews")
    public ResponseEntity<?> submitReview(
            @PathVariable("id") String courseId,
            @RequestBody Review review,
            HttpSession session) {

        Object sessionUser = session.getAttribute(AUTH_USER_SESSION_KEY);
        if (!(sessionUser instanceof AuthUser authUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("error", "กรุณาเข้าสู่ระบบก่อนเขียนรีวิว"));
        }

        String text = review.getReviewText();
        if (text == null || text.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                                 .body(Map.of("error", "กรุณากรอกข้อความรีวิวก่อนส่ง"));
        }
        String studentId = authUser.studentId();
        Integer userId = reviewService.upsertUser(authUser);
        ReviewService.EligibilityStatus status = reviewService.getEligibilityStatus(studentId, courseId);
        if ("ไม่พบข้อมูลรายวิชาในระบบ".equals(status.message())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "ไม่พบรายวิชา " + courseId));
        }
        if (!status.isEligible()) {
            HttpStatus statusCode = reviewService.hasReviewed(studentId, courseId)
                    ? HttpStatus.CONFLICT : HttpStatus.FORBIDDEN;
            return ResponseEntity.status(statusCode)
                                 .body(Map.of("error", status.message()));
        }

        if (!validScore(review.getTeachingMethodScore())
                || !validScore(review.getEvaluationScore())
                || !validScore(review.getTeachingSpeedScore())
                || !validScore(review.getAssignmentStyleScore())
                || !validScore(review.getWorkloadScore())) {
            return ResponseEntity.badRequest().body(Map.of("error", "คะแนนแต่ละหัวข้อต้องอยู่ระหว่าง 0 ถึง 100 หรือเป็น null"));
        }

        review.setCourseId(courseId);
        review.setUserId(userId);
        review.setReviewText(text.trim());
        
        // Normalize empty string sectionId to null so foreign key matches
        if (review.getSectionId() != null && review.getSectionId().trim().isEmpty()) {
            review.setSectionId(null);
        }
        
        if (authUser.year() != null) {
            review.setReviewerYear(ReviewService.normalizeStoredYear(authUser.year()));
        } else if (review.getReviewerYear() == null) {
            Integer year = StudyYearCalculator.calculate(studentId);
            review.setReviewerYear(ReviewService.normalizeStoredYear(year));
        }

        Review savedReview = reviewService.saveReview(review);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "reviewId", savedReview.getId(),
            "message", "รีวิวสำเร็จ"
        ));
    }

    @PutMapping("/{courseId}/reviews/{reviewId}")
    public ResponseEntity<?> editReview(@PathVariable String courseId, @PathVariable Long reviewId,
            @RequestBody Review update, HttpSession session) {
        Object sessionUser = session.getAttribute(AUTH_USER_SESSION_KEY);
        if (!(sessionUser instanceof AuthUser authUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "กรุณาเข้าสู่ระบบก่อนแก้ไขรีวิว"));
        }
        Integer userId = reviewService.upsertUser(authUser);
        Review existing = reviewService.findOwnedReview(reviewId, userId).orElse(null);
        if (existing == null || !existing.getCourseId().equalsIgnoreCase(courseId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "ไม่พบรีวิวที่คุณมีสิทธิ์แก้ไข"));
        }
        if (update.getReviewText() == null || update.getReviewText().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "กรุณากรอกข้อความรีวิวก่อนส่ง"));
        }
        existing.setReviewText(update.getReviewText().trim());
        if (update.isAnonymous() != null) {
            existing.setAnonymous(update.isAnonymous());
        }
        reviewService.saveReview(existing);
        return ResponseEntity.ok(Map.of("reviewId", existing.getId(), "message", "แก้ไขรีวิวสำเร็จ"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleReviewConstraintError(DataIntegrityViolationException exception) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "ข้อมูลรายวิชาหรือ Section ไม่ถูกต้อง กรุณาตรวจสอบแล้วลองอีกครั้ง"
        ));
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<?> handleReviewDatabaseUnavailable(DataAccessResourceFailureException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "ระบบฐานข้อมูลไม่พร้อมใช้งาน กรุณาลองใหม่ภายหลัง"
        ));
    }
}
