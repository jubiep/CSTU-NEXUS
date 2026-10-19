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
    public ResponseEntity<List<Review>> getReviews(
            @PathVariable("id") String courseId,
            @RequestParam("sectionId") String sectionId) {
        
        List<Review> reviewList = reviewService.getReviews(courseId, sectionId);
        
        return ResponseEntity.ok(reviewList);
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
        boolean isEligible = reviewService.checkReviewEligibility(studentId, courseId);
        
        if (!isEligible) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body(Map.of("eligible", false, "message", "ยังไม่มีสิทธิ์รีวิววิชานี้"));
        }

        return ResponseEntity.ok(Map.of(
            "eligible", true,
            "currentYear", authUser.year() != null ? authUser.year() : 1
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

        if (review.getReviewText() == null || review.getReviewText().trim().isEmpty() ||
            review.getSectionId() == null || review.getSectionId().trim().isEmpty()) {
            
            return ResponseEntity.badRequest()
                                 .body(Map.of("error", "กรุณากรอกข้อมูลให้ครบถ้วน"));
        }

        Integer userId;
        try {
            userId = Integer.parseInt(authUser.studentId());
        } catch (NumberFormatException e) {
            userId = 1;
        }

        review.setCourseId(courseId);
        review.setUserId(userId);
        
        if (authUser.year() != null) {
            review.setReviewerYear(authUser.year());
        } else if (review.getReviewerYear() == null) {
            review.setReviewerYear(1);
        }

        Review savedReview = reviewService.saveReview(review);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "reviewId", savedReview.getId(),
            "message", "รีวิวสำเร็จ"
        ));
    }
}