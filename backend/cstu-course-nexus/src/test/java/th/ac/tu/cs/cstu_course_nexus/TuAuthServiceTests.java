package th.ac.tu.cs.cstu_course_nexus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

class TuAuthServiceTests {

    private static final String VERIFY_URL = "https://restapi.tu.ac.th/api/v1/auth/Ad/verify";

    @Test
    void verifiesStudentAndMapsProfile() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(VERIFY_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Application-Key", "test-application-key"))
                .andExpect(content().json("""
                        {"UserName":"6809000000","PassWord":"test-password"}
                        """))
                .andRespond(withSuccess("""
                        {
                          "status": true,
                          "type": "student",
                          "username": "6809000000",
                          "displayname_th": "นักศึกษา ทดสอบ",
                          "email": "student@tu.ac.th",
                          "faculty": "คณะทดสอบ",
                          "department": "วิทยาการคอมพิวเตอร์"
                        }
                        """, MediaType.APPLICATION_JSON));

        TuAuthService service = new TuAuthService(builder, VERIFY_URL, "test-application-key");
        AuthUser user = service.verify("6809000000", "test-password");

        assertEquals("นักศึกษา ทดสอบ", user.displayName());
        assertEquals("6809000000", user.studentId());
        assertEquals("คณะทดสอบ", user.faculty());
        assertEquals("วิทยาการคอมพิวเตอร์", user.department());
        assertEquals("student@tu.ac.th", user.email());
        assertEquals(StudyYearCalculator.calculate("6809000000"), user.year());
        server.verify();
    }

    @Test
    void acceptsAllConfiguredCstuStudentIdPrefixes() {
        for (String studentId : new String[] {
                "6609000000", "6709000000", "6809000000", "6909000000"
        }) {
            RestClient.Builder builder = RestClient.builder();
            MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
            server.expect(requestTo(VERIFY_URL))
                    .andRespond(withSuccess("""
                            {
                              "status": true,
                              "type": "student",
                              "username": "%s",
                              "displayname_th": "นักศึกษา CSTU ทดสอบ",
                              "email": "student@tu.ac.th",
                              "faculty": "คณะทดสอบ",
                              "department": "วิทยาการคอมพิวเตอร์"
                            }
                            """.formatted(studentId), MediaType.APPLICATION_JSON));

            TuAuthService service = new TuAuthService(builder, VERIFY_URL, "test-application-key");
            AuthUser user = service.verify(studentId, "test-password");

            assertEquals(studentId, user.studentId());
            server.verify();
        }
    }

    @Test
    void rejectsNonStudentAccounts() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(VERIFY_URL))
                .andRespond(withSuccess("""
                        {
                          "status": true,
                          "type": "employee",
                          "username": "employee",
                          "displayname_th": "เจ้าหน้าที่",
                          "email": "employee@tu.ac.th"
                        }
                        """, MediaType.APPLICATION_JSON));

        TuAuthService service = new TuAuthService(builder, VERIFY_URL, "test-application-key");
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.verify("employee", "test-password")
        );

        assertEquals(403, exception.getStatusCode().value());
        server.verify();
    }

    @Test
    void rejectsStudentOutsideConfiguredCstuIdPrefix() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(VERIFY_URL))
                .andRespond(withSuccess("""
                        {
                          "status": true,
                          "type": "student",
                          "username": "6808000000",
                          "displayname_th": "นักศึกษาต่างสาขา",
                          "email": "student@tu.ac.th",
                          "faculty": "คณะทดสอบ",
                          "department": "สาขาอื่น"
                        }
                        """, MediaType.APPLICATION_JSON));

        TuAuthService service = new TuAuthService(builder, VERIFY_URL, "test-application-key");
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.verify("6808000000", "test-password")
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals(
                "ระบบนี้อนุญาตให้ใช้งานเฉพาะนักศึกษาสาขาวิชาวิทยาการคอมพิวเตอร์ เท่านั้น",
                exception.getReason()
        );
        server.verify();
    }

    @Test
    void usesLoginStudentIdWhenTuResponseOmitsStudentMetadata() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(VERIFY_URL))
                .andRespond(withSuccess("""
                        {
                          "status": true,
                          "displayname_th": "นักศึกษา ทดสอบ",
                          "email": "student@tu.ac.th"
                        }
                        """, MediaType.APPLICATION_JSON));

        TuAuthService service = new TuAuthService(builder, VERIFY_URL, "test-application-key");
        AuthUser user = service.verify("6809000000", "test-password");

        assertEquals("6809000000", user.studentId());
        server.verify();
    }

    @Test
    void mapsBadRequestFromTuApiToInvalidCredentials() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(VERIFY_URL))
                .andRespond(withBadRequest());

        TuAuthService service = new TuAuthService(builder, VERIFY_URL, "test-application-key");
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.verify("6809000000", "wrong-password")
        );

        assertEquals(401, exception.getStatusCode().value());
        assertEquals("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง", exception.getReason());
        server.verify();
    }

    @Test
    void rejectsBlankOrCrlfKeyWith503() {
        RestClient.Builder builder = RestClient.builder();
        TuAuthService serviceWithCr = new TuAuthService(builder, VERIFY_URL, "\r\n");
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class,
                () -> serviceWithCr.verify("6809000000", "pass"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex1.getStatusCode());

        TuAuthService serviceWithQuotes = new TuAuthService(builder, VERIFY_URL, "\"\"");
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class,
                () -> serviceWithQuotes.verify("6809000000", "pass"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex2.getStatusCode());
    }

    @Test
    void mapsUnauthorizedTuResponseToInvalidCredentials() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(VERIFY_URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        TuAuthService service = new TuAuthService(builder, VERIFY_URL, "test-application-key");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.verify("6800000000", "wrong-password"));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง", exception.getReason());
        server.verify();
    }

    @Test
    void mapsTuServerFailureToBadGateway() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(VERIFY_URL))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        TuAuthService service = new TuAuthService(builder, VERIFY_URL, "test-application-key");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.verify("6800000000", "password"));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals(true, exception.getReason().contains("500"));
        server.verify();
    }

    @Test
    void mapsNetworkTimeoutOrConnectionFailureToBadGateway() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(VERIFY_URL))
                .andRespond(withException(new java.io.IOException("connection timed out")));

        TuAuthService service = new TuAuthService(builder, VERIFY_URL, "test-application-key");
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.verify("6800000000", "password"));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals(true, exception.getReason().contains("ไม่สามารถเชื่อมต่อ TU Authentication ได้"));
        server.verify();
    }
}
