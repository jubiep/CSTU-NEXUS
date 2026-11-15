package th.ac.tu.cs.cstu_course_nexus;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MatchScoreRepository {

    private final JdbcTemplate jdbcTemplate;

    public MatchScoreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean courseExists(String courseId) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM courses WHERE UPPER(course_id) = UPPER(?))",
                Boolean.class,
                courseId);
        return Boolean.TRUE.equals(exists);
    }

    public boolean sectionExists(String courseId, String sectionId) {
        Boolean exists = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM sections
                    WHERE UPPER(course_id) = UPPER(?) AND section_id = ?
                )
                """, Boolean.class, courseId, sectionId);
        return Boolean.TRUE.equals(exists);
    }

    public List<ReviewScores> findScores(String courseId, String sectionId) {
        return jdbcTemplate.query("""
                SELECT teaching_method_score,
                       evaluation_score,
                       teaching_speed_score,
                       assignment_style_score,
                       workload_score
                FROM review
                WHERE UPPER(course_id) = UPPER(?) AND section_id = ?
                ORDER BY created_at, review_id
                """, (row, rowNumber) -> new ReviewScores(
                nullableDouble(row.getObject("teaching_method_score")),
                nullableDouble(row.getObject("evaluation_score")),
                nullableDouble(row.getObject("teaching_speed_score")),
                nullableDouble(row.getObject("assignment_style_score")),
                nullableDouble(row.getObject("workload_score"))), courseId, sectionId);
    }

    private static Double nullableDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : null;
    }

    public record ReviewScores(
            Double teachingMethod,
            Double evaluation,
            Double teachingSpeed,
            Double assignmentStyle,
            Double workload) {
    }
}
