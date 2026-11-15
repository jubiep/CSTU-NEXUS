package th.ac.tu.cs.cstu_course_nexus;

import java.math.BigDecimal;

public record UserPreferencesRequest(
        BigDecimal preferredTeachingMethod,
        BigDecimal preferredEvaluation,
        BigDecimal preferredTeachingSpeed,
        BigDecimal preferredAssignmentStyle,
        BigDecimal preferredWorkload
) {
    public boolean hasValidScores() {
        return valid(preferredTeachingMethod)
                && valid(preferredEvaluation)
                && valid(preferredTeachingSpeed)
                && valid(preferredAssignmentStyle)
                && valid(preferredWorkload);
    }

    public UserPreferences toPreferences() {
        return new UserPreferences(
                toInteger(preferredTeachingMethod),
                toInteger(preferredEvaluation),
                toInteger(preferredTeachingSpeed),
                toInteger(preferredAssignmentStyle),
                toInteger(preferredWorkload));
    }

    private static boolean valid(BigDecimal score) {
        return score == null || (isInteger(score)
                && score.compareTo(BigDecimal.ZERO) >= 0
                && score.compareTo(BigDecimal.valueOf(100)) <= 0);
    }

    private static boolean isInteger(BigDecimal value) {
        return value.stripTrailingZeros().scale() <= 0;
    }

    private static Integer toInteger(BigDecimal score) {
        return score == null ? null : score.intValueExact();
    }
}
