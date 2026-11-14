package th.ac.tu.cs.cstu_course_nexus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserPreferencesPersistenceIntegrationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserPreferencesService userPreferencesService;

    private MockMvc mockMvc;
    private AuthUser authenticatedUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new UserPreferencesController(userPreferencesService)).build();
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        authenticatedUser = new AuthUser(
                "U7 Test Student",
                "U7T" + suffix,
                2,
                "Test Faculty",
                "Computer Science",
                "u7." + suffix + "@example.invalid");
    }

    @Test
    void PUT_persistsPreferencesAndGET_returnsTheSavedValues() throws Exception {
        mockMvc.perform(put("/api/users/preferences")
                        .sessionAttr("authenticatedUser", authenticatedUser)
                        .contentType("application/json")
                        .content("""
                                {
                                  "preferredTeachingMethod": 50,
                                  "preferredEvaluation": 100,
                                  "preferredTeachingSpeed": 75,
                                  "preferredAssignmentStyle": 0,
                                  "preferredWorkload": 100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredWorkload").value(100));

        Map<String, Object> saved = jdbcTemplate.queryForMap("""
                SELECT p.preferred_teaching_method,
                       p.preferred_evaluation,
                       p.preferred_teaching_speed,
                       p.preferred_assignment_style,
                       p.preferred_workload
                FROM user_preferences p
                JOIN mock_user u ON u.user_id = p.user_id
                WHERE u.student_id = ?
                """, authenticatedUser.studentId());

        assertEquals(50, ((Number) saved.get("preferred_teaching_method")).intValue());
        assertEquals(100, ((Number) saved.get("preferred_evaluation")).intValue());
        assertEquals(75, ((Number) saved.get("preferred_teaching_speed")).intValue());
        assertEquals(0, ((Number) saved.get("preferred_assignment_style")).intValue());
        assertEquals(100, ((Number) saved.get("preferred_workload")).intValue());

        mockMvc.perform(get("/api/users/preferences")
                        .sessionAttr("authenticatedUser", authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredTeachingMethod").value(50))
                .andExpect(jsonPath("$.preferredEvaluation").value(100))
                .andExpect(jsonPath("$.preferredTeachingSpeed").value(75))
                .andExpect(jsonPath("$.preferredAssignmentStyle").value(0))
                .andExpect(jsonPath("$.preferredWorkload").value(100));
    }
}
