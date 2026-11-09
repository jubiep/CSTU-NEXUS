package th.ac.tu.cs.cstu_course_nexus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.util.ReflectionTestUtils;

class ReviewServiceMockTests {

    private ReviewService service;
    private ReviewRepository reviewRepository;
    private JdbcTemplate jdbcTemplate;
    private final AtomicReference<String> courseLookupSql = new AtomicReference<>();
    private final AtomicReference<Object> courseLookupParameter = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        service = new ReviewService();
        reviewRepository = mock(ReviewRepository.class);
        jdbcTemplate = mock(JdbcTemplate.class);
        ReflectionTestUtils.setField(service, "reviewRepository", reviewRepository);
        ReflectionTestUtils.setField(service, "jdbcTemplate", jdbcTemplate);
    }

    @Test
    void EL05_returnsMessageWhenStudentHasNoUserRecord() {
        stubQueries(List.of(), List.of(1));

        ReviewService.EligibilityStatus result = service.getEligibilityStatus("6700000000", "CS101");

        assertFalse(result.isEligible());
        assertTrue(result.message().contains("ไม่พบข้อมูลผู้ใช้"));
    }

    @Test
    void EL06_rejectsStudentIdThatCannotProduceStudyYear() {
        ReviewService.EligibilityStatus result = service.getEligibilityStatus("invalid", "CS101");

        assertFalse(result.isEligible());
        assertTrue(result.message().contains("ไม่สามารถคำนวณชั้นปี"));
    }

    @Test
    void EL07_returnsMessageWhenCourseDoesNotExist() {
        stubQueries(List.of(42), List.of());

        ReviewService.EligibilityStatus result = service.getEligibilityStatus("6700000000", "NO-SUCH-COURSE");

        assertFalse(result.isEligible());
        assertTrue(result.message().contains("ไม่พบข้อมูลรายวิชา"));
    }

    @Test
    void EL08_rejectsStudentWhoAlreadyReviewedTheCourse() {
        stubQueries(List.of(42), List.of(1));
        when(reviewRepository.existsByCourseIdAndUserId("CS101", 42)).thenReturn(true);

        ReviewService.EligibilityStatus result = service.getEligibilityStatus("6700000000", "CS101");

        assertFalse(result.isEligible());
        assertTrue(result.message().contains("เคยส่งรีวิว"));
    }

    @Test
    void EL09_reviewForAnotherCourseDoesNotBlockEligibility() {
        stubQueries(List.of(42), List.of(1));
        when(reviewRepository.existsByCourseIdAndUserId("CS101", 42)).thenReturn(true);
        when(reviewRepository.existsByCourseIdAndUserId("CS102", 42)).thenReturn(false);

        ReviewService.EligibilityStatus result = service.getEligibilityStatus("6700000000", "CS102");

        assertTrue(result.isEligible());
        assertEquals(3, result.studentYear());
    }

    @Test
    void EL10_courseIdLookupIsCaseInsensitiveAndTrimsWhitespace() {
        stubQueries(List.of(42), List.of(1));

        ReviewService.EligibilityStatus result = service.getEligibilityStatus("6700000000", " cs101 ");

        assertTrue(result.isEligible());
        assertEquals(1, result.recommendedYear());
        org.junit.jupiter.api.Assertions.assertTrue(courseLookupSql.get().contains("UPPER(course_id)"));
        assertEquals("cs101", courseLookupParameter.get());
    }

    private void stubQueries(List<Integer> userIds, List<Integer> recommendedYears) {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any(Object[].class)))
                .thenAnswer(invocation -> {
                    String sql = invocation.getArgument(0);
                    if (sql.contains("SELECT user_id")) {
                        return userIds;
                    }
                    if (sql.contains("SELECT recommended_year")) {
                        courseLookupSql.set(sql);
                        courseLookupParameter.set(invocation.getArgument(2));
                        return recommendedYears;
                    }
                    throw new AssertionError("Unexpected SQL in mock: " + sql);
                });
    }
}
