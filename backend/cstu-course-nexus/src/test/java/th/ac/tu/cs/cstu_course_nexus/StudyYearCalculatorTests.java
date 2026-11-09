package th.ac.tu.cs.cstu_course_nexus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class StudyYearCalculatorTests {

    private static final int TEST_YEAR = 2026; // พ.ศ. 2569

    @Test
    void SY01_calculatesYearTwoForStudentIdStartingWith68() {
        assertEquals(2, StudyYearCalculator.calculate("6800000000", TEST_YEAR));
    }

    @Test
    void SY02_calculatesYearThreeForStudentIdStartingWith67() {
        assertEquals(3, StudyYearCalculator.calculate("6700000000", TEST_YEAR));
    }

    @Test
    void SY03_calculatesYearFourForStudentIdStartingWith66() {
        assertEquals(4, StudyYearCalculator.calculate("6600000000", TEST_YEAR));
    }

    @Test
    void SY04_capsStoredReviewerYearAtFourWhenCalculatedYearIsFive() {
        Integer calculatedYear = StudyYearCalculator.calculate("6500000000", TEST_YEAR);
        assertEquals(5, calculatedYear);
        assertEquals(4, ReviewService.normalizeStoredYear(calculatedYear));
    }

    @Test
    void SY05_returnsNullForBlankOrMalformedIds() {
        assertNull(StudyYearCalculator.calculate(null, TEST_YEAR));
        assertNull(StudyYearCalculator.calculate("", TEST_YEAR));
        assertNull(StudyYearCalculator.calculate("student", TEST_YEAR));
        assertNull(StudyYearCalculator.calculate("68A0000000", TEST_YEAR));
    }

    @Test
    void SY06_rejectsIdsThatDoNotContainExactlyTenDigits() {
        assertNull(StudyYearCalculator.calculate("68", TEST_YEAR));
        assertNull(StudyYearCalculator.calculate("680000000", TEST_YEAR));
        assertNull(StudyYearCalculator.calculate("68000000000", TEST_YEAR));
    }

    @Test
    void SY07_returnsNullForAdmissionYearInTheFuture() {
        assertNull(StudyYearCalculator.calculate("7000000000", TEST_YEAR));
    }
}
