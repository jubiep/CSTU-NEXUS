package th.ac.tu.cs.cstu_course_nexus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserPreferencesRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserPreferencesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserPreferences> findByUserId(Integer userId) {
        List<UserPreferences> preferences = jdbcTemplate.query("""
                SELECT preferred_teaching_method,
                       preferred_evaluation,
                       preferred_teaching_speed,
                       preferred_assignment_style,
                       preferred_workload
                FROM user_preferences
                WHERE user_id = ?
                """, (resultSet, rowNumber) -> mapPreferences(resultSet), userId);
        return preferences.stream().findFirst();
    }

    public void save(Integer userId, UserPreferences preferences) {
        jdbcTemplate.update("""
                INSERT INTO user_preferences (
                    user_id,
                    preferred_teaching_method,
                    preferred_evaluation,
                    preferred_teaching_speed,
                    preferred_assignment_style,
                    preferred_workload
                )
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (user_id) DO UPDATE SET
                    preferred_teaching_method = EXCLUDED.preferred_teaching_method,
                    preferred_evaluation = EXCLUDED.preferred_evaluation,
                    preferred_teaching_speed = EXCLUDED.preferred_teaching_speed,
                    preferred_assignment_style = EXCLUDED.preferred_assignment_style,
                    preferred_workload = EXCLUDED.preferred_workload
                """,
                userId,
                preferences.preferredTeachingMethod(),
                preferences.preferredEvaluation(),
                preferences.preferredTeachingSpeed(),
                preferences.preferredAssignmentStyle(),
                preferences.preferredWorkload());
    }

    private static UserPreferences mapPreferences(ResultSet resultSet) throws SQLException {
        return new UserPreferences(
                nullableInteger(resultSet, "preferred_teaching_method"),
                nullableInteger(resultSet, "preferred_evaluation"),
                nullableInteger(resultSet, "preferred_teaching_speed"),
                nullableInteger(resultSet, "preferred_assignment_style"),
                nullableInteger(resultSet, "preferred_workload"));
    }

    private static Integer nullableInteger(ResultSet resultSet, String columnName) throws SQLException {
        int value = resultSet.getInt(columnName);
        return resultSet.wasNull() ? null : value;
    }
}
