package th.ac.tu.cs.cstu_course_nexus;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TuAuthService {

    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() { };

    private final RestClient restClient;
    private final String verifyUrl;
    private final String applicationKey;

    public TuAuthService(
            RestClient.Builder restClientBuilder,
            @Value("${tu.auth.verify-url:https://restapi.tu.ac.th/api/v1/auth/Ad/verify}") String verifyUrl,
            @Value("${tu.auth.application-key:}") String applicationKey
    ) {
        this.restClient = restClientBuilder.build();
        this.verifyUrl = verifyUrl;
        this.applicationKey = applicationKey;
    }

    public AuthUser verify(String username, String password) {
        if (applicationKey.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "ยังไม่ได้ตั้งค่า TU API Application Key บน backend"
            );
        }

        Map<String, Object> response;
        try {
            response = restClient.post()
                    .uri(verifyUrl)
                    .header("Application-Key", applicationKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("UserName", username, "PassWord", password))
                    .retrieve()
                    .body(RESPONSE_TYPE);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()) {
                throw invalidCredentials();
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "TU Authentication ตอบกลับด้วยข้อผิดพลาด กรุณาลองใหม่ภายหลัง"
            );
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "ไม่สามารถเชื่อมต่อ TU Authentication ได้ กรุณาลองใหม่ภายหลัง"
            );
        }

        if (response == null || !isSuccessful(response.get("status"))) {
            throw invalidCredentials();
        }

        String accountType = value(response, "type", "accountType");
        if (!"student".equalsIgnoreCase(accountType)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "อนุญาตเฉพาะบัญชีนักศึกษา TU");
        }

        String email = value(response, "email");
        if (!email.toLowerCase().endsWith("@dome.tu.ac.th")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "อนุญาตเฉพาะบัญชีนักศึกษา TU");
        }

        String studentId = value(response, "username", "student_id", "studentId");
        String displayName = value(response, "displayname_th", "displayName", "displayname_en");
        if (studentId.isBlank() || displayName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "ข้อมูลบัญชีที่ได้รับจาก TU Authentication ไม่ครบถ้วน"
            );
        }

        return new AuthUser(
                displayName,
                studentId,
                StudyYearCalculator.calculate(studentId),
                value(response, "faculty"),
                value(response, "department"),
                email
        );
    }

    private static boolean isSuccessful(Object status) {
        return Boolean.TRUE.equals(status) || "true".equalsIgnoreCase(String.valueOf(status));
    }

    private static String value(Map<String, Object> response, String... keys) {
        for (String key : keys) {
            Object value = response.get(key);
            if (value instanceof String text && !text.isBlank()) {
                return text.trim();
            }
        }
        return "";
    }

    private static ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง");
    }
}
