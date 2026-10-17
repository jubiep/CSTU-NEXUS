package th.ac.tu.cs.cstu_course_nexus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String AUTH_USER_SESSION_KEY = "authenticatedUser";

    private final TuAuthService tuAuthService;

    public AuthController(TuAuthService tuAuthService) {
        this.tuAuthService = tuAuthService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        if (request == null || request.username() == null || !request.username().matches("\\d{10}")
                || request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "กรุณากรอกเลขนักศึกษา 10 หลักและรหัสผ่าน");
        }

        AuthUser user = tuAuthService.verify(request.username(), request.password());
        HttpSession session = servletRequest.getSession(true);
        servletRequest.changeSessionId();
        session.setAttribute(AUTH_USER_SESSION_KEY, user);
        return new LoginResponse(user);
    }

    @GetMapping("/me")
    public AuthUser currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "กรุณาเข้าสู่ระบบอีกครั้ง");
        }

        Object user = session.getAttribute(AUTH_USER_SESSION_KEY);
        if (!(user instanceof AuthUser authUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "กรุณาเข้าสู่ระบบอีกครั้ง");
        }
        return authUser;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.noContent().build();
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(AuthUser user) {
    }
}
