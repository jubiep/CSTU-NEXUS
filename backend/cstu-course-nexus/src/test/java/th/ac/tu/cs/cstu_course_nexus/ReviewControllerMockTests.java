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
        mockMvc.perform(post("/api/courses/CS101/reviews")
                        .sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewText\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("กรุณากรอกข้อความรีวิวก่อนส่ง"));

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
    void RV13_persistsAnonymousChoiceFromRequest() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.getEligibilityStatus("6700000000", "CS101"))
                .thenReturn(new ReviewService.EligibilityStatus(true, 3, 1, "eligible"));
        when(reviewService.saveReview(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/courses/CS101/reviews").sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewText\":\"Private review\",\"isAnonymous\":true}"))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<Review> captor = org.mockito.ArgumentCaptor.forClass(Review.class);
        verify(reviewService).saveReview(captor.capture());
        org.junit.jupiter.api.Assertions.assertTrue(captor.getValue().isAnonymous());
    }

    @Test
    void RV14_anonymousFeedResponseOmitsUserIdentity() throws Exception {
        Review anonymous = new Review();
        anonymous.setId(8L);
        anonymous.setCourseId("CS101");
        anonymous.setUserId(99);
        anonymous.setReviewerYear(3);
        anonymous.setReviewText("Hidden author");
        anonymous.setAnonymous(true);
        when(reviewService.getReviews("CS101", null)).thenReturn(java.util.List.of(anonymous));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/courses/CS101/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author").value("67********"))
                .andExpect(jsonPath("$[0].isAnonymous").value(true))
                .andExpect(jsonPath("$[0].userId").doesNotExist())
                .andExpect(jsonPath("$[0].studentId").doesNotExist());
    }

    @Test
    void RV15_cannotEditAnotherUsersReview() throws Exception {
        when(reviewService.upsertUser(eligibleUser)).thenReturn(99);
        when(reviewService.findOwnedReview(42L, 99)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/courses/CS101/reviews/42")
                        .sessionAttr("authenticatedUser", eligibleUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reviewText\":\"Changed\"}"))
                .andExpect(status().isNotFound());
        verify(reviewService, never()).saveReview(any(Review.class));
    }

    @Test
    void RV16_masksAnonymousAuthorPrefixFromReviewerYear() throws Exception {
        java.util.List<Review> reviews = new java.util.ArrayList<>();
        for (int year = 1; year <= 4; year++) {
            Review review = new Review();
            review.setId((long) year);
            review.setCourseId("CS101");
            review.setUserId(100 + year);
            review.setReviewerYear(year);
            review.setReviewText("Review " + year);
            review.setAnonymous(true);
            reviews.add(review);
        }
        when(reviewService.getReviews("CS101", null)).thenReturn(reviews);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/courses/CS101/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].author").value("69********"))
                .andExpect(jsonPath("$[1].author").value("68********"))
                .andExpect(jsonPath("$[2].author").value("67********"))
                .andExpect(jsonPath("$[3].author").value("66********"))
                .andExpect(jsonPath("$[0].studentId").doesNotExist())
                .andExpect(jsonPath("$[0].userId").doesNotExist());
    }

}
