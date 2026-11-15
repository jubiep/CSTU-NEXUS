package th.ac.tu.cs.cstu_course_nexus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MatchScoreServiceTests {

    private MatchScoreRepository repository;
    private UserPreferencesService preferencesService;
    private MatchScoreService service;
    private AuthUser user;

    @BeforeEach
    void setUp() {
        repository = mock(MatchScoreRepository.class);
        preferencesService = mock(UserPreferencesService.class);
        service = new MatchScoreService(repository, preferencesService);
        user = new AuthUser("Test Student", "6800000000", 2,
                "Test Faculty", "Computer Science", "test@dome.tu.ac.th");
        when(repository.courseExists("CS261")).thenReturn(true);
        when(repository.sectionExists("CS261", "cs261_100001")).thenReturn(true);
    }

    @Test
    void calculate_returnsCorrectScoreAndDetailsForAllFiveFactors() {
        when(preferencesService.getForUser(user)).thenReturn(new UserPreferences(0, 50, 100, 50, 50));
        when(repository.findScores("CS261", "cs261_100001")).thenReturn(List.of(
                new MatchScoreRepository.ReviewScores(0d, 50d, 100d, 50d, 50d)));

        MatchScoreService.MatchScoreResponse response = service.calculate(user, " CS261 ", " cs261_100001 ");

        assertEquals("READY", response.status());
        assertEquals(100, response.score());
        assertEquals("เข้ากันได้ดีมาก", response.level());
        assertTrue(response.explanation().contains("ตรงกับ Preferences"));
        assertEquals(5, response.availableFactorCount());
        assertEquals(100, response.coveragePercentage());
        assertEquals(5, response.details().size());
        assertEquals(20.0, response.details().get(0).weightPercentage());
        assertEquals(100, response.details().get(0).similarityScore());
        assertEquals("เน้นทฤษฎี", response.details().get(0).preferenceLabel());
    }

    @Test
    void calculate_clampsLowestAndHighestValidScoresToTheSupportedRange() {
        when(preferencesService.getForUser(user)).thenReturn(new UserPreferences(0, 0, 0, 0, 0));
        when(repository.findScores("CS261", "cs261_100001")).thenReturn(List.of(
                new MatchScoreRepository.ReviewScores(100d, 100d, 100d, 100d, 100d)));

        MatchScoreService.MatchScoreResponse lowest = service.calculate(user, "CS261", "cs261_100001");
        assertEquals(0, lowest.score());
        assertEquals(0, lowest.details().stream().mapToInt(MatchScoreService.FactorDetail::similarityScore).sum());

        when(preferencesService.getForUser(user)).thenReturn(new UserPreferences(100, 100, 100, 100, 100));
        when(repository.findScores("CS261", "cs261_100001")).thenReturn(List.of(
                new MatchScoreRepository.ReviewScores(100d, 100d, 100d, 100d, 100d)));
        MatchScoreService.MatchScoreResponse highest = service.calculate(user, "CS261", "cs261_100001");

        assertEquals(100, highest.score());
        assertEquals(100, highest.details().stream().mapToInt(MatchScoreService.FactorDetail::similarityScore).min().orElseThrow());
    }

    @Test
    void calculate_returnsNoDataWhenPreferencesAndReviewsAreMissing() {
        when(preferencesService.getForUser(user)).thenReturn(UserPreferences.empty());
        when(repository.findScores("CS261", "cs261_100001")).thenReturn(List.of());

        MatchScoreService.MatchScoreResponse response = service.calculate(user, "CS261", "cs261_100001");

        assertEquals("NO_DATA", response.status());
        assertNull(response.score());
        assertEquals(0, response.availableFactorCount());
        assertEquals(0, response.coveragePercentage());
        assertTrue(response.explanation().contains("ยังไม่มีข้อมูล"));
        assertEquals(5, response.details().size());
    }

    @Test
    void calculate_selectsTheCategoryWithTheMostVotes() {
        when(preferencesService.getForUser(user)).thenReturn(new UserPreferences(0, null, null, null, null));
        when(repository.findScores("CS261", "cs261_100001")).thenReturn(List.of(
                new MatchScoreRepository.ReviewScores(0d, null, null, null, null),
                new MatchScoreRepository.ReviewScores(0d, null, null, null, null),
                new MatchScoreRepository.ReviewScores(100d, null, null, null, null)));

        MatchScoreService.MatchScoreResponse response = service.calculate(user, "CS261", "cs261_100001");

        assertEquals(100, response.score());
        assertEquals(0.0, response.details().get(0).sectionProfileScore());
        assertEquals(List.of("เน้นทฤษฎี"), response.details().get(0).sectionProfileLabels());
        assertEquals(67, response.details().get(0).consensusPercentage());
    }

    @Test
    void calculate_workloadToleranceGivesFullSimilarityWhenSectionWorkloadIsLower() {
        when(preferencesService.getForUser(user)).thenReturn(new UserPreferences(null, null, null, null, 50));
        when(repository.findScores("CS261", "cs261_100001")).thenReturn(List.of(
                new MatchScoreRepository.ReviewScores(null, null, null, null, 0d)));

        MatchScoreService.MatchScoreResponse response = service.calculate(user, "CS261", "cs261_100001");

        assertEquals(100, response.score());
        assertEquals(100, response.details().get(4).similarityScore());
    }
    @Test
    void calculate_usesMajorityVoteAndAveragesTiedCategories() {
        when(preferencesService.getForUser(user)).thenReturn(new UserPreferences(50, null, null, null, null));
        when(repository.findScores("CS261", "cs261_100001")).thenReturn(List.of(
                new MatchScoreRepository.ReviewScores(0d, null, null, null, null),
                new MatchScoreRepository.ReviewScores(100d, null, null, null, null)));

        MatchScoreService.MatchScoreResponse response = service.calculate(user, "CS261", "cs261_100001");
        MatchScoreService.FactorDetail teaching = response.details().get(0);

        assertEquals("READY", response.status());
        assertEquals(100, response.score());
        assertEquals(50.0, teaching.sectionProfileScore());
        assertEquals(List.of("เน้นทฤษฎี", "เน้นปฏิบัติ"), teaching.sectionProfileLabels());
        assertEquals(100, teaching.similarityScore());
        assertEquals(20, response.coveragePercentage());
    }

    @Test
    void calculate_ignoresInvalidReviewValuesAndAllowsPartialCoverage() {
        when(preferencesService.getForUser(user)).thenReturn(new UserPreferences(0, 50, null, null, null));
        when(repository.findScores("CS261", "cs261_100001")).thenReturn(List.of(
                new MatchScoreRepository.ReviewScores(0d, 140d, null, null, null)));

        MatchScoreService.MatchScoreResponse response = service.calculate(user, "CS261", "cs261_100001");

        assertEquals("READY", response.status());
        assertEquals(100, response.score());
        assertEquals(1, response.availableFactorCount());
        assertEquals(20, response.coveragePercentage());
        assertNull(response.details().get(1).similarityScore());
    }
}