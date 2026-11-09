package th.ac.tu.cs.cstu_course_nexus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:55432/cstu_rv11_test",
        "spring.datasource.username=postgres",
        "spring.datasource.password=rv11_test_password",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "tu.auth.application-key=rv11-test-only"
})
class ReviewConcurrentSubmissionIntegrationTests {

    private static final String STUDENT_ID = "6700000000";
    private static final int USER_ID = ReviewService.deriveUserId(STUDENT_ID);
    private static final String COURSE_ID = "RV11_TEST";
    private static final String SECTION_ID = "RV11_TEST_SEC01";

    @Autowired
    private ReviewController reviewController;

    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ReviewService reviewService;

    private MockHttpSession session;

    @BeforeEach
    void prepareIsolatedTestData() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
        jdbcTemplate.update("DELETE FROM review WHERE course_id = ?", COURSE_ID);
        jdbcTemplate.update("DELETE FROM sections WHERE course_id = ?", COURSE_ID);
        jdbcTemplate.update("DELETE FROM courses WHERE course_id = ?", COURSE_ID);
        jdbcTemplate.update("DELETE FROM mock_user WHERE student_id = ?", STUDENT_ID);

        jdbcTemplate.update("INSERT INTO courses (course_id, course_name, recommended_year) VALUES (?, ?, ?)",
                COURSE_ID, "RV11 concurrency fixture", 1);
        jdbcTemplate.update("INSERT INTO sections (course_id, section_id, section_number, instructor_name) VALUES (?, ?, ?, ?)",
                COURSE_ID, SECTION_ID, "000001", "Test Instructor");
        jdbcTemplate.update("INSERT INTO mock_user (user_id, student_id, display_name_th, current_year, faculty, department, email) VALUES (?, ?, ?, ?, ?, ?, ?)",
                USER_ID, STUDENT_ID, "นักศึกษาทดสอบ", 3, "คณะทดสอบ", "สาขาทดสอบ", "rv11-test@dome.tu.ac.th");

        AuthUser user = new AuthUser("นักศึกษาทดสอบ", STUDENT_ID, 3,
                "คณะทดสอบ", "สาขาทดสอบ", "rv11-test@dome.tu.ac.th");
        session = new MockHttpSession();
        session.setAttribute("authenticatedUser", user);

        doReturn(USER_ID).when(reviewService).upsertUser(user);
        doReturn(new ReviewService.EligibilityStatus(true, 3, 1, "คุณได้รับสิทธิ์แบ่งปันประสบการณ์"))
                .when(reviewService).getEligibilityStatus(STUDENT_ID, COURSE_ID);
    }

    @Test
    void concurrentDuplicateSubmissionsPersistAtMostOneReview() throws Exception {
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> first = executor.submit(() -> submitTogether(ready, start));
            Future<Integer> second = executor.submit(() -> submitTogether(ready, start));

            ready.await();
            start.countDown();

            List<Integer> statuses = List.of(first.get(), second.get()).stream().sorted().toList();
            assertEquals(List.of(201, 400), statuses,
                    "One request should be created and the concurrent duplicate rejected");
            assertEquals(1, jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM review WHERE course_id = ? AND user_id = ?",
                    Integer.class, COURSE_ID, USER_ID));
        } finally {
            executor.shutdownNow();
        }
    }

    private int submitTogether(CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        start.await();
        MvcResult result = mockMvc.perform(post("/api/courses/{id}/reviews", COURSE_ID)
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sectionId\":\"" + SECTION_ID + "\",\"reviewText\":\"Concurrent review\"}"))
                .andReturn();
        return result.getResponse().getStatus();
    }
}
