package th.ac.tu.cs.cstu_course_nexus;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MatchScoreControllerTests {

    private MatchScoreService service;
    private MockMvc mockMvc;
    private AuthUser user;

    @BeforeEach
    void setUp() {
        service = mock(MatchScoreService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new MatchScoreController(service)).build();
        user = new AuthUser("Test Student", "6800000000", 2,
                "Test Faculty", "Computer Science", "test@dome.tu.ac.th");
    }

    @Test
    void GET_withoutSession_returnsUnauthorizedAndDoesNotCalculate() throws Exception {
        mockMvc.perform(get("/api/courses/CS261/sections/cs261_100001/match-score"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("กรุณาเข้าสู่ระบบก่อนดู Match Score"));

        verify(service, never()).calculate(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void GET_withSession_returnsScoreDetailsAndExplanation() throws Exception {
        MatchScoreService.MatchScoreResponse response = new MatchScoreService.MatchScoreResponse(
                "READY", 86, "เข้ากันได้ดีมาก", "ผลการวิเคราะห์ตัวอย่าง",
                12, 5, 5, 100,
                List.of(new MatchScoreService.FactorDetail("teachingMethod", "แนวทางการสอน",
                        100, "เน้นปฏิบัติ", 100.0, List.of("เน้นปฏิบัติ"),
                        12, 8, 67, 100, 20.0)));
        when(service.calculate(user, "CS261", "cs261_100001")).thenReturn(response);

        mockMvc.perform(get("/api/courses/CS261/sections/cs261_100001/match-score")
                        .sessionAttr("authenticatedUser", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.score").value(86))
                .andExpect(jsonPath("$.level").value("เข้ากันได้ดีมาก"))
                .andExpect(jsonPath("$.explanation").value("ผลการวิเคราะห์ตัวอย่าง"))
                .andExpect(jsonPath("$.details[0].label").value("แนวทางการสอน"))
                .andExpect(jsonPath("$.details[0].similarityScore").value(100));
    }
}