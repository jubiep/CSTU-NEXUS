package th.ac.tu.cs.cstu_course_nexus;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class MatchScoreController {

    private static final String AUTH_USER_SESSION_KEY = "authenticatedUser";

    private final MatchScoreService matchScoreService;

    public MatchScoreController(MatchScoreService matchScoreService) {
        this.matchScoreService = matchScoreService;
    }

    @GetMapping("/{courseId}/sections/{sectionId}/match-score")
    public ResponseEntity<?> getMatchScore(
            @PathVariable String courseId,
            @PathVariable String sectionId,
            HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionUser = session == null ? null : session.getAttribute(AUTH_USER_SESSION_KEY);
        if (!(sessionUser instanceof AuthUser user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "กรุณาเข้าสู่ระบบก่อนดู Match Score"));
        }
        return ResponseEntity.ok(matchScoreService.calculate(user, courseId, sectionId));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<Map<String, String>> handleDatabaseUnavailable(
            DataAccessResourceFailureException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "ระบบฐานข้อมูลไม่พร้อมใช้งาน กรุณาลองใหม่ภายหลัง"));
    }
}
