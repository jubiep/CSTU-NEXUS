package th.ac.tu.cs.cstu_course_nexus;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/preferences")
public class PreferenceController {

    private static final String AUTH_USER_SESSION_KEY = "authenticatedUser";
    private static final String INVALID_VALUE_MESSAGE = "ค่า Preference ต้องเป็น 0, 50 หรือ 100";

    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping
    public ResponseEntity<?> getPreferences(HttpServletRequest request) {
        AuthUser user = authenticatedUser(request);
        if (user == null) {
            return unauthorized();
        }
        return ResponseEntity.ok(preferenceService.get(user.studentId()));
    }

    @PutMapping
    public ResponseEntity<?> savePreferences(
            @RequestBody PreferenceService.Preferences preferences,
            HttpServletRequest request) {
        AuthUser user = authenticatedUser(request);
        if (user == null) {
            return unauthorized();
        }
        if (preferences == null || !valid(preferences.teachingStyle())
                || !valid(preferences.assessmentStyle())
                || !valid(preferences.teachingSpeed())
                || !valid(preferences.workFormat())
                || !valid(preferences.workloadTolerance())) {
            return ResponseEntity.badRequest().body(Map.of("error", INVALID_VALUE_MESSAGE));
        }
        return ResponseEntity.ok(preferenceService.save(user.studentId(), preferences));
    }

    private static AuthUser authenticatedUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute(AUTH_USER_SESSION_KEY);
        return user instanceof AuthUser authUser ? authUser : null;
    }

    private static boolean valid(Integer value) {
        return value == null || value == 0 || value == 50 || value == 100;
    }

    private static ResponseEntity<Map<String, String>> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "กรุณาเข้าสู่ระบบก่อนจัดการ Preference"));
    }
}
