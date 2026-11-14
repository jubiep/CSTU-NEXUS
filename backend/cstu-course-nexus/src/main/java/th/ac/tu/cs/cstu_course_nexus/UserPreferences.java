package th.ac.tu.cs.cstu_course_nexus;

public record UserPreferences(
        Integer preferredTeachingMethod,
        Integer preferredEvaluation,
        Integer preferredTeachingSpeed,
        Integer preferredAssignmentStyle,
        Integer preferredWorkload
) {
    public static UserPreferences empty() {
        return new UserPreferences(null, null, null, null, null);
    }
}
