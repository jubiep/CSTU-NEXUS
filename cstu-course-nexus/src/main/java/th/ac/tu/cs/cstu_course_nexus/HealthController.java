package th.ac.tu.cs.cstu_course_nexus;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public String check() {
        return "CSTU Course Nexus is running!";
    }

    @PostMapping("/echo")
    public String echo(@RequestBody String message) {
        return "Received message: " + message;
    }

    @GetMapping("/db-test")
    public String testDatabaseConnection() {
        Integer courseCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM courses",
                Integer.class
        );

        return "PostgreSQL connection successful! Courses: " + courseCount;
    }
}
