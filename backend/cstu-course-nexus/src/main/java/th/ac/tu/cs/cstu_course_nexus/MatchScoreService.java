package th.ac.tu.cs.cstu_course_nexus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MatchScoreService {

    private static final int TOTAL_FACTORS = 5;

    private static final List<Dimension> DIMENSIONS = List.of(
            new Dimension("teachingMethod", "แนวทางการสอน",
                    MatchScoreRepository.ReviewScores::teachingMethod,
                    UserPreferences::preferredTeachingMethod,
                    Map.of(0, "เน้นทฤษฎี", 50, "สมดุล", 100, "เน้นปฏิบัติ"),
                    Map.of(0, "เน้นทฤษฎี", 50, "สมดุล", 100, "เน้นปฏิบัติ"), false),
            new Dimension("evaluation", "สัดส่วนการวัดผลและตัดเกรด",
                    MatchScoreRepository.ReviewScores::evaluation,
                    UserPreferences::preferredEvaluation,
                    Map.of(0, "เน้นคะแนนสอบ", 50, "สมดุล", 100, "เน้นโครงงาน"),
                    Map.of(0, "เน้นคะแนนสอบ", 50, "สมดุล", 100, "เน้นโครงงาน"), false),
            new Dimension("teachingSpeed", "ความเร็วในการสอน",
                    MatchScoreRepository.ReviewScores::teachingSpeed,
                    UserPreferences::preferredTeachingSpeed,
                    Map.of(0, "สอนช้า", 50, "ปานกลาง", 100, "สอนเร็ว"),
                    Map.of(0, "สอนช้า", 50, "ปานกลาง", 100, "สอนเร็ว"), false),
            new Dimension("assignmentStyle", "รูปแบบงาน",
                    MatchScoreRepository.ReviewScores::assignmentStyle,
                    UserPreferences::preferredAssignmentStyle,
                    Map.of(0, "เน้นงานเดี่ยว", 50, "สมดุล", 100, "เน้นงานกลุ่ม"),
                    Map.of(0, "เน้นงานเดี่ยว", 50, "สมดุล", 100, "เน้นงานกลุ่ม"), false),
            new Dimension("workload", "ปริมาณงาน",
                    MatchScoreRepository.ReviewScores::workload,
                    UserPreferences::preferredWorkload,
                    Map.of(0, "งานน้อย", 50, "ปริมาณงานสมดุล", 100, "งานเยอะ"),
                    Map.of(0, "รับงานได้น้อย", 50, "รับงานได้ปานกลาง", 100, "รับงานได้มาก"), true));

    private final MatchScoreRepository matchScoreRepository;
    private final UserPreferencesService userPreferencesService;

    public MatchScoreService(
            MatchScoreRepository matchScoreRepository,
            UserPreferencesService userPreferencesService) {
        this.matchScoreRepository = matchScoreRepository;
        this.userPreferencesService = userPreferencesService;
    }

    public MatchScoreResponse calculate(AuthUser user, String courseId, String sectionId) {
        String normalizedCourseId = normalize(courseId, "ไม่พบรหัสรายวิชา");
        String normalizedSectionId = normalize(sectionId, "กรุณาเลือก Section ก่อนดู Match Score");

        if (!matchScoreRepository.courseExists(normalizedCourseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบรายวิชา " + normalizedCourseId);
        }
        if (!matchScoreRepository.sectionExists(normalizedCourseId, normalizedSectionId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ไม่พบ Section ของรายวิชานี้");
        }

        UserPreferences preferences = userPreferencesService.getForUser(user);
        List<MatchScoreRepository.ReviewScores> reviews =
                matchScoreRepository.findScores(normalizedCourseId, normalizedSectionId);

        List<FactorDetail> details = new ArrayList<>(TOTAL_FACTORS);
        double similarityTotal = 0;
        int availableFactors = 0;

        for (Dimension dimension : DIMENSIONS) {
            Integer preferenceScore = dimension.preferenceValue().apply(preferences);
            Profile profile = aggregate(reviews, dimension);

            Integer similarity = null;
            double weight = 0;
            if (preferenceScore != null && profile.score() != null) {
                similarity = calculateSimilarity(preferenceScore, profile.score(), dimension.isTolerance());
                similarityTotal += similarity;
                availableFactors++;
            }

            details.add(new FactorDetail(
                    dimension.key(),
                    dimension.label(),
                    preferenceScore,
                    preferenceScore == null ? null : labelFor(dimension, preferenceScore),
                    profile.score(),
                    profile.labels(),
                    profile.voteCount(),
                    profile.topVotes(),
                    profile.consensusPercentage(),
                    similarity,
                    weight));
        }

        int score = availableFactors == 0 ? 0 : clamp((int) Math.round(similarityTotal / availableFactors));
        double factorWeight = availableFactors == 0 ? 0 : round(100.0 / availableFactors, 1);
        details = details.stream().map(detail -> detail.similarityScore() == null
                ? detail
                : new FactorDetail(
                        detail.key(), detail.label(), detail.preferenceScore(), detail.preferenceLabel(),
                        detail.sectionProfileScore(), detail.sectionProfileLabels(), detail.reviewCount(),
                        detail.topVoteCount(), detail.consensusPercentage(), detail.similarityScore(), factorWeight))
                .toList();

        int coveragePercentage = (int) Math.round(availableFactors * 100.0 / TOTAL_FACTORS);
        boolean hasScore = availableFactors > 0;
        return new MatchScoreResponse(
                hasScore ? "READY" : "NO_DATA",
                hasScore ? score : null,
                hasScore ? scoreLevel(score) : null,
                hasScore ? explanation(score) : "ยังไม่มีข้อมูล Preferences หรือรีวิวของ Section นี้เพียงพอสำหรับคำนวณ Match Score",
                reviews.size(),
                availableFactors,
                TOTAL_FACTORS,
                coveragePercentage,
                details);
    }

    private static Profile aggregate(
            List<MatchScoreRepository.ReviewScores> reviews,
            Dimension dimension) {
        Map<Integer, Integer> votes = new HashMap<>();
        int validVoteCount = 0;

        for (MatchScoreRepository.ReviewScores review : reviews) {
            Double rawScore = dimension.reviewValue().apply(review);
            if (rawScore == null || !Double.isFinite(rawScore) || rawScore < 0 || rawScore > 100) {
                continue;
            }
            int categoryScore = clamp((int) Math.round(rawScore / 50.0) * 50);
            votes.merge(categoryScore, 1, Integer::sum);
            validVoteCount++;
        }

        if (validVoteCount == 0) {
            return Profile.empty();
        }

        int topVotes = votes.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        List<Integer> winners = votes.entrySet().stream()
                .filter(entry -> entry.getValue() == topVotes)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
        double profileScore = winners.stream().mapToInt(Integer::intValue).average().orElse(0);
        List<String> labels = winners.stream()
                .map(score -> dimension.sectionLabels().getOrDefault(score, ""))
                .toList();
        return new Profile(profileScore, labels, validVoteCount, topVotes,
                (int) Math.round(topVotes * 100.0 / validVoteCount));
    }

    private static Integer calculateSimilarity(int preferenceScore, double sectionScore, boolean tolerance) {
        if (preferenceScore < 0 || preferenceScore > 100) {
            return null;
        }
        double difference = tolerance
                ? Math.max(0, sectionScore - preferenceScore)
                : Math.abs(preferenceScore - sectionScore);
        return clamp((int) Math.round(100 - difference));
    }

    private static String labelFor(Dimension dimension, int score) {
        int nearestCategory = clamp((int) Math.round(score / 50.0) * 50);
        return dimension.preferenceLabels().getOrDefault(nearestCategory, "");
    }

    private static String scoreLevel(int score) {
        if (score >= 80) return "เข้ากันได้ดีมาก";
        if (score >= 60) return "เข้ากันได้ดี";
        if (score >= 40) return "เข้ากันได้ปานกลาง";
        return "เข้ากันได้น้อย";
    }

    private static String explanation(int score) {
        if (score >= 80) {
            return "รูปแบบการเรียนของ Section นี้ตรงกับ Preferences ของคุณหลายด้าน ใช้เป็นข้อมูลประกอบการเลือกเรียนได้ และควรพิจารณารายละเอียดแต่ละด้านร่วมด้วย";
        }
        if (score >= 60) {
            return "มีหลายด้านที่ค่อนข้างตรงกับ Preferences ของคุณ ลองดูรายละเอียดรายด้านเพื่อพิจารณาว่าด้านที่ต่างกันสำคัญกับคุณแค่ไหน";
        }
        if (score >= 40) {
            return "รูปแบบการเรียนตรงกับ Preferences ของคุณบางด้าน ควรดูรายละเอียดรายด้านและข้อมูลรายวิชาเพิ่มเติมก่อนตัดสินใจ";
        }
        return "รูปแบบการเรียนของ Section นี้ต่างจาก Preferences ของคุณหลายด้าน คะแนนนี้เป็นข้อมูลประกอบการตัดสินใจ ไม่ได้ตัดสินว่าคุณจะเรียนได้หรือไม่ได้";
    }

    private static String normalize(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static double round(double value, int digits) {
        double multiplier = Math.pow(10, digits);
        return Math.round(value * multiplier) / multiplier;
    }

    private record Dimension(
            String key,
            String label,
            Function<MatchScoreRepository.ReviewScores, Double> reviewValue,
            Function<UserPreferences, Integer> preferenceValue,
            Map<Integer, String> sectionLabels,
            Map<Integer, String> preferenceLabels,
            boolean isTolerance) {
    }

    private record Profile(
            Double score,
            List<String> labels,
            int voteCount,
            int topVotes,
            int consensusPercentage) {
        private static Profile empty() {
            return new Profile(null, List.of(), 0, 0, 0);
        }
    }

    public record FactorDetail(
            String key,
            String label,
            Integer preferenceScore,
            String preferenceLabel,
            Double sectionProfileScore,
            List<String> sectionProfileLabels,
            int reviewCount,
            int topVoteCount,
            int consensusPercentage,
            Integer similarityScore,
            double weightPercentage) {
    }

    public record MatchScoreResponse(
            String status,
            Integer score,
            String level,
            String explanation,
            int sectionReviewCount,
            int availableFactorCount,
            int totalFactorCount,
            int coveragePercentage,
            List<FactorDetail> details) {
    }
}
