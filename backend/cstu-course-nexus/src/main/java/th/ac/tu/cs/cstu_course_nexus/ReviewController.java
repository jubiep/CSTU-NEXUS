package th.ac.tu.cs.cstu_course_nexus;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
            item.put("userId", review.getUserId());
            item.put("year", review.getReviewerYear());
            item.put("reviewerYear", review.getReviewerYear());
            item.put("text", review.getReviewText());
            item.put("comment", review.getReviewText());
            item.put("date", review.getDate());
            item.put("studentId", reviewService.getStudentId(review.getUserId()));
            return item;
        }).toList();

        return ResponseEntity.ok(response);
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
        ReviewService.EligibilityStatus status = reviewService.getEligibilityStatus(studentId, courseId);
        if (!status.isEligible()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body(Map.of("error", status.message()));
        }

        Integer userId = ReviewService.deriveUserId(studentId);

        review.setCourseId(courseId);
        review.setUserId(userId);
        review.setReviewText(text.trim());
        
        // Normalize empty string sectionId to null so foreign key matches
        if (review.getSectionId() != null && review.getSectionId().trim().isEmpty()) {
            review.setSectionId(null);
        }
        
        if (authUser.year() != null) {
            review.setReviewerYear(authUser.year());
        } else if (review.getReviewerYear() == null) {
            Integer year = StudyYearCalculator.calculate(studentId);
            review.setReviewerYear(year != null ? year : 1);
        }

        Review savedReview = reviewService.saveReview(review);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "reviewId", savedReview.getId(),
            "message", "รีวิวสำเร็จ"
        ));
    }
}
