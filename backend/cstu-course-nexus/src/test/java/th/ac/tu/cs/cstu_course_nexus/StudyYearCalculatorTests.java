package th.ac.tu.cs.cstu_course_nexus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class StudyYearCalculatorTests {

    @Test
    void calculatesYearTwoForStudentIdStartingWith68In2026() {
        assertEquals(2, StudyYearCalculator.calculate("6800000000", 2026));
    }

    @Test
    void returnsNullForInvalidOrFutureStudentIds() {
        assertNull(StudyYearCalculator.calculate("student", 2026));
        assertNull(StudyYearCalculator.calculate("7000000000", 2026));
    }

    @Test
    void doesNotCapYearsForStudentsTakingLongerToGraduate() {
        assertEquals(5, StudyYearCalculator.calculate("6800000000", 2029));
    }
}
