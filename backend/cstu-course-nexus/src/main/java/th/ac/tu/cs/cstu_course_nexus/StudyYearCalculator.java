package th.ac.tu.cs.cstu_course_nexus;

import java.time.Year;
import java.time.ZoneId;

public final class StudyYearCalculator {

    private static final int BUDDHIST_ERA_OFFSET = 543;
    private static final int STUDENT_ID_CENTURY = 2500;
    private static final ZoneId THAILAND_ZONE = ZoneId.of("Asia/Bangkok");

    private StudyYearCalculator() {
    }

    public static Integer calculate(String studentId) {
        return calculate(studentId, Year.now(THAILAND_ZONE).getValue());
    }

    static Integer calculate(String studentId, int gregorianYear) {
        if (studentId == null || !studentId.matches("\\d{2}.*")) {
            return null;
        }

        int admissionYear = STUDENT_ID_CENTURY + Integer.parseInt(studentId.substring(0, 2));
        int currentBuddhistYear = gregorianYear + BUDDHIST_ERA_OFFSET;
        int studyYear = currentBuddhistYear - admissionYear + 1;
        return studyYear > 0 ? studyYear : null;
    }
}
