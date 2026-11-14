package th.ac.tu.cs.cstu_course_nexus;

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
import org.mockito.ArgumentCaptor;

class UserPreferencesControllerTests {

    private MockMvc mockMvc;
    private UserPreferencesService userPreferencesService;
    private AuthUser authenticatedUser;

    @BeforeEach
    void setUp() {
        userPreferencesService = org.mockito.Mockito.mock(UserPreferencesService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new UserPreferencesController(userPreferencesService)).build();
        authenticatedUser = new AuthUser("Test Student", "6700000000", 3,
                "Test Faculty", "Computer Science", "test@dome.tu.ac.th");
    }

    @Test
    void GET_returnsEmptyPreferencesWhenUserHasNoSavedValues() throws Exception {
        when(userPreferencesService.getForUser(authenticatedUser))
                .thenReturn(UserPreferences.empty());

        mockMvc.perform(get("/api/users/preferences")
                        .sessionAttr("authenticatedUser", authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredTeachingMethod").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.preferredEvaluation").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.preferredTeachingSpeed").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.preferredAssignmentStyle").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.preferredWorkload").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void GET_returnsSavedPreferencesForTheAuthenticatedUser() throws Exception {
        when(userPreferencesService.getForUser(authenticatedUser))
                .thenReturn(new UserPreferences(0, 50, 25, 100, 75));

        mockMvc.perform(get("/api/users/preferences")
                        .sessionAttr("authenticatedUser", authenticatedUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredTeachingMethod").value(0))
                .andExpect(jsonPath("$.preferredEvaluation").value(50))
                .andExpect(jsonPath("$.preferredTeachingSpeed").value(25))
                .andExpect(jsonPath("$.preferredAssignmentStyle").value(100))
                .andExpect(jsonPath("$.preferredWorkload").value(75));
    }

    @Test
    void GET_requiresAnAuthenticatedSession() throws Exception {
        mockMvc.perform(get("/api/users/preferences"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());

        verify(userPreferencesService, never()).getForUser(authenticatedUser);
    }

    @Test
    void PUT_savesValuesForTheAuthenticatedSessionUser() throws Exception {
        UserPreferences preferences = new UserPreferences(0, 50, 25, 100, 75);
        when(userPreferencesService.saveForUser(authenticatedUser, preferences)).thenReturn(preferences);

        mockMvc.perform(put("/api/users/preferences")
                        .sessionAttr("authenticatedUser", authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "preferredTeachingMethod": 0,
                                  "preferredEvaluation": 50,
                                  "preferredTeachingSpeed": 25,
                                  "preferredAssignmentStyle": 100,
                                  "preferredWorkload": 75
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preferredTeachingMethod").value(0))
                .andExpect(jsonPath("$.preferredEvaluation").value(50))
                .andExpect(jsonPath("$.preferredWorkload").value(75));

        ArgumentCaptor<UserPreferences> captor = ArgumentCaptor.forClass(UserPreferences.class);
        verify(userPreferencesService).saveForUser(org.mockito.ArgumentMatchers.eq(authenticatedUser), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(preferences, captor.getValue());
    }

    @Test
    void PUT_rejectsScoresOutsideTheSupportedRange() throws Exception {
        mockMvc.perform(put("/api/users/preferences")
                        .sessionAttr("authenticatedUser", authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preferredTeachingMethod\":101}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        verify(userPreferencesService, never()).saveForUser(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void PUT_rejectsFractionalScores() throws Exception {
        mockMvc.perform(put("/api/users/preferences")
                        .sessionAttr("authenticatedUser", authenticatedUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preferredTeachingMethod\":50.5}"))
                .andExpect(status().isBadRequest());

        verify(userPreferencesService, never()).saveForUser(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
