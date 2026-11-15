package th.ac.tu.cs.cstu_course_nexus;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class PreferenceServiceTests {

    private static final String STUDENT_ID = "6809000000";

    private JdbcTemplate jdbcTemplate;
    private PreferenceService preferenceService;

    @BeforeEach
    void setUp() {
        jdbcTemplate = mock(JdbcTemplate.class);
        preferenceService = new PreferenceService(jdbcTemplate);
    }

    @Test
    void get_returnsEmptyPreferencesWhenStudentHasNotSavedAny() {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(List.of());

        PreferenceService.Preferences result = preferenceService.get(STUDENT_ID);

        assertEquals(PreferenceService.Preferences.empty(), result);
    }

    @Test
    void get_returnsStoredPreferences() {
        PreferenceService.Preferences stored =
                new PreferenceService.Preferences(0, 50, 100, null, 50);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(List.of(stored));

        PreferenceService.Preferences result = preferenceService.get(STUDENT_ID);

        assertEquals(stored, result);
    }

    @Test
    void save_upsertsAllValuesAndReturnsSavedPreferences() {
        PreferenceService.Preferences preferences =
                new PreferenceService.Preferences(0, 50, 100, null, 50);
        when(jdbcTemplate.update(anyString(), any(Object[].class))).thenReturn(1);

        PreferenceService.Preferences result = preferenceService.save(STUDENT_ID, preferences);

        assertEquals(preferences, result);
        ArgumentCaptor<Object[]> arguments = ArgumentCaptor.forClass(Object[].class);
        verify(jdbcTemplate).update(anyString(), arguments.capture());
        assertArrayEquals(new Object[] {STUDENT_ID, 0, 50, 100, null, 50}, arguments.getValue());
    }
}
