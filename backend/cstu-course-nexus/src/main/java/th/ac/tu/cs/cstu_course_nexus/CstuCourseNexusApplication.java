package th.ac.tu.cs.cstu_course_nexus;

import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CstuCourseNexusApplication {

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(CstuCourseNexusApplication.class, args);
    }

    static void loadDotenv() {
        Path[] candidates = {
                Path.of(".env"),
                Path.of("../../.env"),
                Path.of("../.env")
        };
        for (Path path : candidates) {
            if (Files.isRegularFile(path)) {
                try {
                    for (String line : Files.readAllLines(path)) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        int eq = line.indexOf('=');
                        if (eq > 0) {
                            String key = line.substring(0, eq).trim();
                            String val = line.substring(eq + 1).trim();
                            if ((val.startsWith("\"") && val.endsWith("\"")) ||
                                    (val.startsWith("'") && val.endsWith("'"))) {
                                if (val.length() >= 2) {
                                    val = val.substring(1, val.length() - 1);
                                }
                            }
                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, val);
                            }
                        }
                    }
                    break;
                } catch (Exception ignored) {
                }
            }
        }
    }
}
