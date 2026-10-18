package th.ac.tu.cs.cstu_course_nexus;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/courses")
public class ReviewController {

    private static final String AUTH_USER_SESSION_KEY = "authenticatedUser";

    @Autowired
    private ReviewService reviewService;

    // GET /api/courses/{courseId}/reviews
    @GetMapping("/{courseId}/reviews")
    public ResponseEntity<Page<Review>> getReviews(
            @PathVariable String courseId,
            @RequestParam String section,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        Page<Review> reviews = reviewService.getReviewsByCourseAndSection(courseId, section, page, size);
        return ResponseEntity.ok(reviews);
    }

    // GET /api/courses/{courseId}/reviews/eligibility
    @GetMapping("/{courseId}/reviews/eligibility")
    public ResponseEntity<?> checkEligibility(
            @PathVariable String courseId,
            HttpSession session) {
        
        Object sessionUser = session.getAttribute(AUTH_USER_SESSION_KEY);
        if (!(sessionUser instanceof AuthUser authUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("canReview", false, "message", "กรุณาเข้าสู่ระบบก่อนเช็กสิทธิ์"));
        }

        String studentId = authUser.studentId(); 
        boolean isEligible = reviewService.checkEligibility(studentId, courseId);
        return ResponseEntity.ok(Map.of("canReview", isEligible));
    }

    // POST /api/courses/{courseId}/reviews
    @PostMapping("/{courseId}/reviews")
    public ResponseEntity<?> createReview(
            @PathVariable String courseId,
            @RequestBody Review review,
            HttpSession session) {

        // 1. ตรวจสอบ Authentication จาก Session
        Object sessionUser = session.getAttribute(AUTH_USER_SESSION_KEY);
        if (!(sessionUser instanceof AuthUser authUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("message", "กรุณาเข้าสู่ระบบก่อนเขียนรีวิว"));
        }

        // 2. ดึง/แปลง userId จาก AuthUser
        Integer userId;
        try {
            userId = Integer.parseInt(authUser.studentId());
        } catch (NumberFormatException e) {
            userId = 1; // Fallback กรณีรหัสนักศึกษาแปลงเป็น Integer โดยตรงไม่ได้
        }

        // 3. Validate ข้อความรีวิว
        if (review.getReviewText() == null || review.getReviewText().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "กรุณากรอกข้อความรีวิวก่อนทำการส่ง"));
        }

        if (review.getSectionId() == null || review.getSectionId().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "กรุณาระบุ Section"));
        }

        // 4. บันทึกรีวิว
        review.setCourseId(courseId);
        review.setUserId(userId);
        
        if (authUser.year() != null) {
            review.setReviewerYear(authUser.year());
        } else if (review.getReviewerYear() == null) {
            review.setReviewerYear(1);
        }

        Review savedReview = reviewService.saveReview(review);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
    }
}