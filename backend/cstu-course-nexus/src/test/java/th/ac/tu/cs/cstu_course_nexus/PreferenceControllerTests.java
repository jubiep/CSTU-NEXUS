package th.ac.tu.cs.cstu_course_nexus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class PreferenceControllerTests {

    private static final String STUDENT_ID = "6809000000";

    private MockMvc mockMvc;
    private PreferenceService preferenceService;
    private AuthUser user;

    @BeforeEach
    void setUp() {
        preferenceService = mock(PreferenceService.class);
        PreferenceController controller = new PreferenceController(preferenceService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        user = new AuthUser("Test Student", STUDENT_ID, 2,
                "Test Faculty", "Computer Science", "test@dome.tu.ac.th");
    }

    @Test
    void GET_returnsSavedPreferencesForSessionStudent() throws Exception {
        when(preferenceService.get(STUDENT_ID))
                .thenReturn(new PreferenceService.Preferences(0, 50, 100, null, 50));

        mockMvc.perform(get("/api/preferences").sessionAttr("authenticatedUser", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teachingStyle").value(0))
                .andExpect(jsonPath("$.assessmentStyle").value(50))
                .andExpect(jsonPath("$.teachingSpeed").value(100))
                .andExpect(jsonPath("$.workFormat").doesNotExist())
                .andExpect(jsonPath("$.workloadTolerance").value(50));
    }

    @Test
    void GET_withoutSession_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/preferences"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());

        verify(preferenceService, never()).get(any());
    }

    @Test
    void PUT_savesAllowedValuesForSessionStudent() throws Exception {
        PreferenceService.Preferences saved =
                new PreferenceService.Preferences(0, 50, 100, 0, 50);
        when(preferenceService.save(eq(STUDENT_ID), any(PreferenceService.Preferences.class)))
                .thenReturn(saved);

        mockMvc.perform(put("/api/preferences")
                        .sessionAttr("authenticatedUser", user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "teachingStyle": 0,
                                  "assessmentStyle": 50,
                                  "teachingSpeed": 100,
                                  "workFormat": 0,
                                  "workloadTolerance": 50
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teachingStyle").value(0))
                .andExpect(jsonPath("$.assessmentStyle").value(50))
                .andExpect(jsonPath("$.teachingSpeed").value(100))
                .andExpect(jsonPath("$.workFormat").value(0))
                .andExpect(jsonPath("$.workloadTolerance").value(50));

        verify(preferenceService).save(eq(STUDENT_ID), eq(saved));
    }

    @Test
    void PUT_rejectsValuesOutsideThreePointScale() throws Exception {
        mockMvc.perform(put("/api/preferences")
                        .sessionAttr("authenticatedUser", user)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "teachingStyle": 25,
                                  "assessmentStyle": 50,
                                  "teachingSpeed": 100,
                                  "workFormat": 0,
                                  "workloadTolerance": 50
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(preferenceService, never()).save(any(), any());
    }

    @Test
    void PUT_withoutSession_returnsUnauthorized() throws Exception {
        mockMvc.perform(put("/api/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "teachingStyle": 0,
                                  "assessmentStyle": 50,
                                  "teachingSpeed": 100,
                                  "workFormat": 0,
                                  "workloadTolerance": 50
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verify(preferenceService, never()).save(any(), any());
    }
}
