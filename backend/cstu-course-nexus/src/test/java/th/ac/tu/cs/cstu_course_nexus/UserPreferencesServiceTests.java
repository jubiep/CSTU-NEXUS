package th.ac.tu.cs.cstu_course_nexus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class UserPreferencesServiceTests {

    private ReviewService reviewService;
    private UserPreferencesRepository userPreferencesRepository;
    private UserPreferencesService userPreferencesService;
    private AuthUser authenticatedUser;

    @BeforeEach
    void setUp() {
        reviewService = Mockito.mock(ReviewService.class);
        userPreferencesRepository = Mockito.mock(UserPreferencesRepository.class);
        userPreferencesService = new UserPreferencesService(reviewService, userPreferencesRepository);
        authenticatedUser = new AuthUser("Test Student", "6700000000", 3,
                "Test Faculty", "Computer Science", "test@dome.tu.ac.th");
    }

    @Test
    void getForUserReturnsEmptyPreferencesAndUsesTheResolvedDatabaseUserId() {
        when(reviewService.upsertUser(authenticatedUser)).thenReturn(42);
        when(userPreferencesRepository.findByUserId(42)).thenReturn(Optional.empty());

        assertEquals(UserPreferences.empty(), userPreferencesService.getForUser(authenticatedUser));
        verify(userPreferencesRepository).findByUserId(42);
    }

    @Test
    void saveForUserPersistsPreferencesForTheResolvedDatabaseUserId() {
        UserPreferences preferences = new UserPreferences(0, 50, 25, 100, 75);
        when(reviewService.upsertUser(authenticatedUser)).thenReturn(42);

        assertEquals(preferences, userPreferencesService.saveForUser(authenticatedUser, preferences));
        verify(userPreferencesRepository).save(42, preferences);
    }
}
