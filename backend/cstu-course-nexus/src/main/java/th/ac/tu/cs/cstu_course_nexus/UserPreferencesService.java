package th.ac.tu.cs.cstu_course_nexus;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserPreferencesService {

    private final ReviewService reviewService;
    private final UserPreferencesRepository userPreferencesRepository;

    public UserPreferencesService(
            ReviewService reviewService,
            UserPreferencesRepository userPreferencesRepository) {
        this.reviewService = reviewService;
        this.userPreferencesRepository = userPreferencesRepository;
    }

    public UserPreferences getForUser(AuthUser user) {
        Integer userId = reviewService.upsertUser(user);
        return userPreferencesRepository.findByUserId(userId).orElseGet(UserPreferences::empty);
    }

    public UserPreferences saveForUser(AuthUser user, UserPreferences preferences) {
        Integer userId = reviewService.upsertUser(user);
        userPreferencesRepository.save(userId, preferences);
        return preferences;
    }
}
