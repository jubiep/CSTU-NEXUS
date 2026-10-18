package th.ac.tu.cs.cstu_course_nexus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CstuCourseNexusApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void loadDotenvDoesNotThrow() {
        assertDoesNotThrow(CstuCourseNexusApplication::loadDotenv);
    }
}
