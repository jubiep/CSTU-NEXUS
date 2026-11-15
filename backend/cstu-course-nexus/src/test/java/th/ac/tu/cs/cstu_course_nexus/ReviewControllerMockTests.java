package th.ac.tu.cs.cstu_course_nexus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ReviewControllerMockTests {

    private MockMvc mockMvc;
    private ReviewService reviewService;
    private AuthUser eligibleUser;

    @BeforeEach
    void setUp() {
        ReviewController controller = new ReviewController();
        reviewService = mock(ReviewService.class);
        ReflectionTestUtils.setField(controller, "reviewService", reviewService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        eligibleUser = new AuthUser("Test Student", "6700000000", 3,
                "Test Faculty", "Computer Science", "test@dome.tu.ac.th");
    }

    @Test
    void RV04_rejectsBlankReviewText() throws Exception {
        String[] blankReviewBodies = {
                "{\"reviewText\":null}",
                "{\"reviewText\":\"\"}",
                "{\"reviewText\":\"   \"}"
        };
        for (String body : blankReviewBodies) {
            mockMvc.perform(post("/api/courses/CS101/reviews")
                            .sessionAttr("authenticatedUser", eligibleUser)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("กรุณากรอกข้อความรีวิวก่อนส่ง"));
        }

        verify(reviewService, never()).saveReview(any(Review.class));
    }

    @Test
    void RV05_rejectsReviewWhenEligibilityCheckFails() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "CS101"))
                .thenReturn(new ReviewService.EligibilityStatus(false, 3, 3, "ไม่มีสิทธิ์"));

        mockMvc.perform(post("/api/courses/CS101/reviews")
                        .sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewText\":\"Valid review\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ไม่มีสิทธิ์"));

        verify(reviewService, never()).saveReview(any(Review.class));
    }

    @Test
    void RV06_rejectsSecondSubmissionWhenEligibilityHasChangedToAlreadyReviewed() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "CS101"))
                .thenReturn(new ReviewService.EligibilityStatus(true, 3, 1, "eligible"))
                .thenReturn(new ReviewService.EligibilityStatus(false, 3, null, "คุณได้เคยส่งรีวิวรายวิชานี้ไปแล้ว"));
        when(reviewService.saveReview(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(1L);
            return review;
        });

        String request = "{\"reviewText\":\"First review\"}";
        mockMvc.perform(post("/api/courses/CS101/reviews").sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/courses/CS101/reviews").sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isForbidden());

        verify(reviewService, org.mockito.Mockito.times(1)).saveReview(any(Review.class));
    }

    @Test
    void RV07AndRV08_returnBadRequestForInvalidSectionReferences() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "CS101"))
                .thenReturn(new ReviewService.EligibilityStatus(true, 3, 1, "eligible"));
        when(reviewService.saveReview(any(Review.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("invalid section reference"));

        for (String invalidSection : new String[] {"missing-section", "section-from-another-course"}) {
            mockMvc.perform(post("/api/courses/CS101/reviews")
                            .sessionAttr("authenticatedUser", eligibleUser)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"reviewText\":\"Review\",\"sectionId\":\"" + invalidSection + "\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }
    }

    @Test
    void RV09_normalizesBlankSectionToNull() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "CS101"))
                .thenReturn(new ReviewService.EligibilityStatus(true, 3, 1, "eligible"));
        when(reviewService.saveReview(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(2L);
            return review;
        });

        mockMvc.perform(post("/api/courses/CS101/reviews")
                        .sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewText\":\"Review\",\"sectionId\":\"   \"}"))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<Review> captor = org.mockito.ArgumentCaptor.forClass(Review.class);
        verify(reviewService).saveReview(captor.capture());
        org.junit.jupiter.api.Assertions.assertNull(captor.getValue().getSectionId());
    }

    @Test
    void RV02_acceptsCourseWideReviewWithNullSection() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "CS101"))
                .thenReturn(new ReviewService.EligibilityStatus(true, 3, 1, "eligible"));
        when(reviewService.saveReview(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(4L);
            return review;
        });

        mockMvc.perform(post("/api/courses/CS101/reviews")
                        .sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewText\":\"Course-wide review\",\"sectionId\":null}"))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<Review> captor = org.mockito.ArgumentCaptor.forClass(Review.class);
        verify(reviewService).saveReview(captor.capture());
        org.junit.jupiter.api.Assertions.assertNull(captor.getValue().getSectionId());
    }

    @Test
    void RV10_usesPathSessionAndCalculatedValuesInsteadOfClientSuppliedIdentity() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "CS102"))
                .thenReturn(new ReviewService.EligibilityStatus(true, 3, 1, "eligible"));
        when(reviewService.saveReview(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(3L);
            return review;
        });

        mockMvc.perform(post("/api/courses/CS102/reviews")
                        .sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":\"EVIL\",\"userId\":777,\"reviewerYear\":1,\"reviewText\":\"  Review  \"}"))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<Review> captor = org.mockito.ArgumentCaptor.forClass(Review.class);
        verify(reviewService).saveReview(captor.capture());
        Review saved = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals("CS102", saved.getCourseId());
        org.junit.jupiter.api.Assertions.assertEquals(99, saved.getUserId());
        org.junit.jupiter.api.Assertions.assertEquals(3, saved.getReviewerYear());
        org.junit.jupiter.api.Assertions.assertEquals("Review", saved.getReviewText());
    }

    @Test
    void RV12_databaseFailureReturnsServiceUnavailableWithoutSuccess() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "CS101"))
                .thenReturn(new ReviewService.EligibilityStatus(true, 3, 1, "eligible"));
        when(reviewService.saveReview(any(Review.class)))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("database unavailable"));

        mockMvc.perform(post("/api/courses/CS101/reviews")
                        .sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewText\":\"Review\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("ระบบฐานข้อมูลไม่พร้อมใช้งาน กรุณาลองใหม่ภายหลัง"));
    }

    @Test
    void U606_acceptsAndPreservesAllFiveNormalizedScores() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "CS261"))
                .thenReturn(new ReviewService.EligibilityStatus(true, 3, 2, "eligible"));
        when(reviewService.saveReview(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(61L);
            return review;
        });

        mockMvc.perform(post("/api/courses/CS261/reviews")
                        .sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reviewText":"U6 review","teachingMethodScore":0.0,
                                 "evaluationScore":50.0,"teachingSpeedScore":100.0,
                                 "assignmentStyleScore":50.0,"workloadScore":0.0}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reviewId").value(61));

        org.mockito.ArgumentCaptor<Review> captor = org.mockito.ArgumentCaptor.forClass(Review.class);
        verify(reviewService).saveReview(captor.capture());
        Review saved = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(0.0, saved.getTeachingMethodScore());
        org.junit.jupiter.api.Assertions.assertEquals(50.0, saved.getEvaluationScore());
        org.junit.jupiter.api.Assertions.assertEquals(100.0, saved.getTeachingSpeedScore());
        org.junit.jupiter.api.Assertions.assertEquals(50.0, saved.getAssignmentStyleScore());
        org.junit.jupiter.api.Assertions.assertEquals(0.0, saved.getWorkloadScore());
    }

    @Test
    void U601_acceptsPartialRatingsAndKeepsUnselectedScoresNull() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "CS261"))
                .thenReturn(new ReviewService.EligibilityStatus(true, 3, 2, "eligible"));
        when(reviewService.saveReview(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(62L);
            return review;
        });

        mockMvc.perform(post("/api/courses/CS261/reviews")
                        .sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reviewText":"Partial U6 review","teachingMethodScore":100.0,
                                 "evaluationScore":null,"teachingSpeedScore":null,
                                 "assignmentStyleScore":null,"workloadScore":null}
                                """))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<Review> captor = org.mockito.ArgumentCaptor.forClass(Review.class);
        verify(reviewService).saveReview(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(100.0, captor.getValue().getTeachingMethodScore());
        org.junit.jupiter.api.Assertions.assertNull(captor.getValue().getEvaluationScore());
        org.junit.jupiter.api.Assertions.assertNull(captor.getValue().getTeachingSpeedScore());
        org.junit.jupiter.api.Assertions.assertNull(captor.getValue().getAssignmentStyleScore());
        org.junit.jupiter.api.Assertions.assertNull(captor.getValue().getWorkloadScore());
    }

    @Test
    void U602_acceptsUnsureAsNullWhenAtLeastOneRealScoreExists() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "CS261"))
                .thenReturn(new ReviewService.EligibilityStatus(true, 3, 2, "eligible"));
        when(reviewService.saveReview(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(63L);
            return review;
        });

        mockMvc.perform(post("/api/courses/CS261/reviews")
                        .sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reviewText":"Unsure U6 review","teachingMethodScore":null,
                                 "evaluationScore":50.0,"teachingSpeedScore":null,
                                 "assignmentStyleScore":0.0,"workloadScore":null}
                                """))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<Review> captor = org.mockito.ArgumentCaptor.forClass(Review.class);
        verify(reviewService).saveReview(captor.capture());
        Review saved = captor.getValue();
        org.junit.jupiter.api.Assertions.assertNull(saved.getTeachingMethodScore());
        org.junit.jupiter.api.Assertions.assertEquals(50.0, saved.getEvaluationScore());
        org.junit.jupiter.api.Assertions.assertEquals(0.0, saved.getAssignmentStyleScore());
        org.junit.jupiter.api.Assertions.assertNull(saved.getWorkloadScore());
    }

    @Test
    void U605_preservesLowestAndHighestBoundaryScores() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "CS261"))
                .thenReturn(new ReviewService.EligibilityStatus(true, 3, 2, "eligible"));
        when(reviewService.saveReview(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(70L);
            return review;
        });

        String[] boundaryRequests = {
                "{\"reviewText\":\"low\",\"teachingMethodScore\":0,\"evaluationScore\":0,\"teachingSpeedScore\":0,\"assignmentStyleScore\":0,\"workloadScore\":0}",
                "{\"reviewText\":\"high\",\"teachingMethodScore\":100,\"evaluationScore\":100,\"teachingSpeedScore\":100,\"assignmentStyleScore\":100,\"workloadScore\":100}"
        };
        for (String request : boundaryRequests) {
            mockMvc.perform(post("/api/courses/CS261/reviews")
                            .sessionAttr("authenticatedUser", eligibleUser)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(request))
                    .andExpect(status().isCreated());
        }

        org.mockito.ArgumentCaptor<Review> captor = org.mockito.ArgumentCaptor.forClass(Review.class);
        verify(reviewService, org.mockito.Mockito.times(2)).saveReview(captor.capture());
        java.util.List<Review> saved = captor.getAllValues();
        for (int i = 0; i < 5; i++) {
            org.junit.jupiter.api.Assertions.assertEquals(0.0, scoreAt(saved.get(0), i));
            org.junit.jupiter.api.Assertions.assertEquals(100.0, scoreAt(saved.get(1), i));
        }
    }

    private static Double scoreAt(Review review, int index) {
        return switch (index) {
            case 0 -> review.getTeachingMethodScore();
            case 1 -> review.getEvaluationScore();
            case 2 -> review.getTeachingSpeedScore();
            case 3 -> review.getAssignmentStyleScore();
            case 4 -> review.getWorkloadScore();
            default -> throw new IllegalArgumentException("Unexpected score index: " + index);
        };
    }

    @Test
    void U607_returnsNotFoundWhenCourseDoesNotExist() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "NO999"))
                .thenReturn(new ReviewService.EligibilityStatus(false, 3, null, "ไม่พบข้อมูลรายวิชาในระบบ"));

        mockMvc.perform(post("/api/courses/NO999/reviews")
                        .sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewText\":\"U6 review\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("ไม่พบรายวิชา NO999"));

        verify(reviewService, never()).saveReview(any(Review.class));
    }

    @Test
    void U608_returnsConflictWhenStudentAlreadyReviewedCourse() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "CS261"))
                .thenReturn(new ReviewService.EligibilityStatus(false, 3, null, "คุณได้เคยส่งรีวิวรายวิชานี้ไปแล้ว"));
        when(reviewService.hasReviewed("6700000000", "CS261")).thenReturn(true);

        mockMvc.perform(post("/api/courses/CS261/reviews")
                        .sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewText\":\"Duplicate U6 review\"}"))
                .andExpect(status().isConflict());

        verify(reviewService, never()).saveReview(any(Review.class));
    }

    @Test
    void U612_rejectsScoresOutsideZeroToOneHundred() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "CS261"))
                .thenReturn(new ReviewService.EligibilityStatus(true, 3, 2, "eligible"));

        for (String invalidScore : new String[] {"-1.0", "100.1"}) {
            mockMvc.perform(post("/api/courses/CS261/reviews")
                            .sessionAttr("authenticatedUser", eligibleUser)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"reviewText\":\"U6 review\",\"teachingMethodScore\":" + invalidScore + "}"))
                    .andExpect(status().isBadRequest());
        }

        verify(reviewService, never()).saveReview(any(Review.class));
    }

    @Test
    void U613_returnsUnauthorizedWithoutSession() throws Exception {
        mockMvc.perform(post("/api/courses/CS261/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewText\":\"U6 review\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("กรุณาเข้าสู่ระบบก่อนเขียนรีวิว"));

        verify(reviewService, never()).saveReview(any(Review.class));
    }
}
