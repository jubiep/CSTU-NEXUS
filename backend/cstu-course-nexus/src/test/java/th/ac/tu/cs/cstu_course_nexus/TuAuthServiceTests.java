package th.ac.tu.cs.cstu_course_nexus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

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
                        {"UserName":"student@dome.tu.ac.th","PassWord":"test-password"}
                        """))
                .andRespond(withSuccess("""
                        {
                          "status": true,
                          "type": "student",
                          "username": "6800000000",
                          "displayname_th": "นักศึกษา ทดสอบ",
                          "email": "student@dome.tu.ac.th",
                          "faculty": "คณะทดสอบ",
                          "department": "วิทยาการคอมพิวเตอร์"
                        }
                        """, MediaType.APPLICATION_JSON));

        TuAuthService service = new TuAuthService(builder, VERIFY_URL, "test-application-key");
        AuthUser user = service.verify("student@dome.tu.ac.th", "test-password");

        assertEquals("นักศึกษา ทดสอบ", user.displayName());
        assertEquals("6800000000", user.studentId());
        assertEquals("คณะทดสอบ", user.faculty());
        assertEquals("วิทยาการคอมพิวเตอร์", user.department());
        assertEquals("student@dome.tu.ac.th", user.email());
        assertEquals(StudyYearCalculator.calculate("6800000000"), user.year());
        server.verify();
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
    void rejectsBlankOrCrlfKeyWith503() {
        RestClient.Builder builder = RestClient.builder();
        TuAuthService serviceWithCr = new TuAuthService(builder, VERIFY_URL, "\r\n");
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class,
                () -> serviceWithCr.verify("6800000000", "pass"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex1.getStatusCode());

        TuAuthService serviceWithQuotes = new TuAuthService(builder, VERIFY_URL, "\"\"");
        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class,
                () -> serviceWithQuotes.verify("6800000000", "pass"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex2.getStatusCode());
    }
}
