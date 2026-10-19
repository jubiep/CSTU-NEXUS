package th.ac.tu.cs.cstu_course_nexus;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final JdbcTemplate jdbcTemplate;

    public CourseController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Returns courses whose code or name contains keyword. An omitted or blank
     * keyword returns the initial course list.
     */
    @GetMapping
    public List<CourseSummary> searchCourses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer year) {
        if (year != null && (year < 1 || year > 4)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "year ต้องอยู่ระหว่าง 1 ถึง 4");
        }

        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String sql = """
                SELECT c.course_id, c.course_name, c.recommended_year, COUNT(s.section_id) AS section_count
                FROM courses c
                LEFT JOIN sections s ON s.course_id = c.course_id
                WHERE (? = '' OR c.course_id ILIKE ? ESCAPE '\\' OR c.course_name ILIKE ? ESCAPE '\\')
                  AND c.recommended_year = COALESCE(?, c.recommended_year)
                GROUP BY c.course_id, c.course_name, c.recommended_year
                ORDER BY c.course_id
                """;

        String searchPattern = "%" + escapeLike(normalizedKeyword) + "%";
        return jdbcTemplate.query(sql, (row, rowNumber) -> new CourseSummary(
                row.getString("course_id"),
                row.getString("course_name"),
                row.getInt("recommended_year"),
                row.getInt("section_count")
        ), normalizedKeyword, searchPattern, searchPattern, year);
    }

    @GetMapping("/{courseId}")
    public CourseDetail getCourse(@PathVariable String courseId) {
        String normalizedCourseId = courseId.trim().toUpperCase();
        List<CourseDetail> courses = jdbcTemplate.query("""
                SELECT course_id, course_name, recommended_year
                FROM courses
                WHERE UPPER(course_id) = ?
                """, (row, rowNumber) -> new CourseDetail(
                row.getString("course_id"),
                row.getString("course_name"),
                row.getInt("recommended_year"),
                List.of()
        ), normalizedCourseId);

        if (courses.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบรายวิชา " + courseId);
        }

        CourseDetail course = courses.get(0);
        List<Section> sections = jdbcTemplate.query("""
                SELECT section_id, section_number, instructor_name
                FROM sections
                WHERE course_id = ?
                ORDER BY section_number, section_id
                """, (row, rowNumber) -> new Section(
                row.getString("section_id"),
                row.getString("section_number"),
                row.getString("instructor_name")
        ), course.courseId());

        return new CourseDetail(course.courseId(), course.courseName(), course.recommendedYear(), sections);
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    public record CourseSummary(String courseId, String courseName, int recommendedYear, int sectionCount) {
    }

    public record CourseDetail(String courseId, String courseName, int recommendedYear, List<Section> sections) {
    }

    public record Section(String sectionId, String sectionNumber, String instructorName) {
    }
}
