package th.ac.tu.cs.cstu_course_nexus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CourseControllerMockTests {

    private MockMvc mockMvc;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        CourseController controller;
        jdbcTemplate = mock(JdbcTemplate.class);
        controller = new CourseController(jdbcTemplate);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void U4_01_searchesCoursesByKeywordAndRecommendedYear() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(List.of(new CourseController.CourseSummary(
                        "CS101", "Introduction to Computer Science", 1, 2)));

        mockMvc.perform(get("/api/courses")
                        .param("keyword", " CS101 ")
                        .param("year", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseId").value("CS101"))
                .andExpect(jsonPath("$[0].courseName").value("Introduction to Computer Science"))
                .andExpect(jsonPath("$[0].recommendedYear").value(1))
                .andExpect(jsonPath("$[0].sectionCount").value(2));

        verify(jdbcTemplate).query(anyString(), any(RowMapper.class),
                eq("CS101"), eq("%CS101%"), eq("%CS101%"), eq(1));
    }

    @Test
    void U4_02_returnsInitialCourseListWhenKeywordIsMissingOrBlank() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(get("/api/courses").param("keyword", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(jdbcTemplate, org.mockito.Mockito.times(2)).query(anyString(), any(RowMapper.class),
                eq(""), eq("%%"), eq("%%"), isNull());
    }

    @Test
    void U4_03_rejectsRecommendedYearOutsideOneThroughFour() throws Exception {
        mockMvc.perform(get("/api/courses").param("year", "5"))
                .andExpect(status().isBadRequest());

        verify(jdbcTemplate, never()).query(anyString(), any(RowMapper.class), any(Object[].class));
    }

    @Test
    void U4_04_returnsCourseDetailsAndItsSections() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(List.of(new CourseController.CourseDetail(
                                "CS101", "Introduction to Computer Science", 1, List.of())))
                .thenReturn(List.of(
                        new CourseController.Section("cs101_010001", "010001", "Instructor One"),
                        new CourseController.Section("cs101_010002", "010002", "Instructor Two")));

        mockMvc.perform(get("/api/courses/cs101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value("CS101"))
                .andExpect(jsonPath("$.courseName").value("Introduction to Computer Science"))
                .andExpect(jsonPath("$.sections.length()").value(2))
                .andExpect(jsonPath("$.sections[0].sectionId").value("cs101_010001"))
                .andExpect(jsonPath("$.sections[0].sectionNumber").value("010001"))
                .andExpect(jsonPath("$.sections[0].instructorName").value("Instructor One"))
                .andExpect(jsonPath("$.sections[1].sectionNumber").value("010002"));

        verify(jdbcTemplate, org.mockito.Mockito.times(2)).query(
                anyString(), any(RowMapper.class), any(Object[].class));
    }

    @Test
    void U4_05_returnsNotFoundForUnknownCourse() throws Exception {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/courses/UNKNOWN"))
                .andExpect(status().isNotFound());

        verify(jdbcTemplate, org.mockito.Mockito.times(1)).query(
                anyString(), any(RowMapper.class), any(Object[].class));
    }
}
