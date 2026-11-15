package th.ac.tu.cs.cstu_course_nexus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:55432/cstu_rv11_test",
        "spring.datasource.username=postgres",
        "spring.datasource.password=rv11_test_password",
        "spring.datasource.driver-class-name=org.postgresql.Driver"
})
class CstuCourseNexusApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void loadDotenvDoesNotThrow() {
        assertDoesNotThrow(CstuCourseNexusApplication::loadDotenv);
    }
}
