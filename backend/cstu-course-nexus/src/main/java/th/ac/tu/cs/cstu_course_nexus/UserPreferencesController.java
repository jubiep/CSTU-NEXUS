package th.ac.tu.cs.cstu_course_nexus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/preferences")
public class UserPreferencesController {

    private static final String AUTH_USER_SESSION_KEY = "authenticatedUser";

    private final UserPreferencesService userPreferencesService;

    public UserPreferencesController(UserPreferencesService userPreferencesService) {
        this.userPreferencesService = userPreferencesService;
    }

    @GetMapping
    public ResponseEntity<?> getPreferences(HttpServletRequest request) {
        AuthUser user = authenticatedUser(request);
        if (user == null) {
            return unauthorized();
        }
        return ResponseEntity.ok(userPreferencesService.getForUser(user));
    }

    @PutMapping
    public ResponseEntity<?> savePreferences(
            @RequestBody UserPreferencesRequest preferences,
            HttpServletRequest request) {
        AuthUser user = authenticatedUser(request);
        if (user == null) {
            return unauthorized();
        }
        if (preferences == null || !preferences.hasValidScores()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "คะแนน Preferences แต่ละด้านต้องอยู่ระหว่าง 0 ถึง 100 หรือเป็น null"));
        }
        return ResponseEntity.ok(userPreferencesService.saveForUser(user, preferences.toPreferences()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(
            DataIntegrityViolationException exception) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "บันทึก Preferences ไม่สำเร็จ กรุณาตรวจสอบข้อมูลแล้วลองอีกครั้ง"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableRequestBody(
            HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "รูปแบบ Request ไม่ถูกต้อง กรุณาส่งคะแนนเป็นจำนวนเต็ม 0 ถึง 100 หรือ null"));
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<Map<String, String>> handleDatabaseUnavailable(
            DataAccessResourceFailureException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "ระบบฐานข้อมูลไม่พร้อมใช้งาน กรุณาลองใหม่ภายหลัง"));
    }

    private static AuthUser authenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute(AUTH_USER_SESSION_KEY);
        return user instanceof AuthUser authUser ? authUser : null;
    }

    private static ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "กรุณาเข้าสู่ระบบก่อนจัดการ Preferences"));
    }
}
