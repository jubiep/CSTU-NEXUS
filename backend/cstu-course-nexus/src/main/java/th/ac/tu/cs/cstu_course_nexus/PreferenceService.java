package th.ac.tu.cs.cstu_course_nexus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class PreferenceService {

    private final JdbcTemplate jdbcTemplate;

    public PreferenceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Preferences get(String studentId) {
        List<Preferences> rows = jdbcTemplate.query("""
                SELECT teaching_style, assessment_style, teaching_speed,
                       work_format, workload_tolerance
                FROM learning_preference
                WHERE student_id = ?
                """, this::mapPreferences, studentId);

        return rows.isEmpty() ? Preferences.empty() : rows.get(0);
    }

    public Preferences save(String studentId, Preferences preferences) {
        jdbcTemplate.update("""
                INSERT INTO learning_preference (
                    student_id, teaching_style, assessment_style, teaching_speed,
                    work_format, workload_tolerance, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, NOW())
                ON CONFLICT (student_id) DO UPDATE SET
                    teaching_style = EXCLUDED.teaching_style,
                    assessment_style = EXCLUDED.assessment_style,
                    teaching_speed = EXCLUDED.teaching_speed,
                    work_format = EXCLUDED.work_format,
                    workload_tolerance = EXCLUDED.workload_tolerance,
                    updated_at = NOW()
                """,
                studentId,
                preferences.teachingStyle(),
                preferences.assessmentStyle(),
                preferences.teachingSpeed(),
                preferences.workFormat(),
                preferences.workloadTolerance());

        return preferences;
    }

    private Preferences mapPreferences(ResultSet rs, int rowNum) throws SQLException {
        return new Preferences(
                nullableInteger(rs, "teaching_style"),
                nullableInteger(rs, "assessment_style"),
                nullableInteger(rs, "teaching_speed"),
                nullableInteger(rs, "work_format"),
                nullableInteger(rs, "workload_tolerance"));
    }

    private static Integer nullableInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    public record Preferences(
            Integer teachingStyle,
            Integer assessmentStyle,
            Integer teachingSpeed,
            Integer workFormat,
            Integer workloadTolerance) {

        public static Preferences empty() {
            return new Preferences(null, null, null, null, null);
        }
    }
}
