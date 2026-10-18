package th.ac.tu.cs.cstu_course_nexus;

import java.io.Serializable;

public record AuthUser(
        String displayName,
        String studentId,
        Integer year,
        String faculty,
        String department,
        String email
) implements Serializable {
}
