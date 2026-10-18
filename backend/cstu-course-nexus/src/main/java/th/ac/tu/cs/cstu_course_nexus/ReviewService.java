package th.ac.tu.cs.cstu_course_nexus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public Page<Review> getReviewsByCourseAndSection(String courseId, String sectionId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findByCourseIdAndSectionIdOrderByCreatedAtDesc(courseId, sectionId, pageable);
    }

    public boolean checkEligibility(String studentId, String courseId) {
        if (studentId == null || studentId.length() < 2) {
            return false;
        }

        try {
            int entryYear = Integer.parseInt(studentId.substring(0, 2));
            int currentYearBE = 69; // พ.ศ. 2569
            int studentYear = (currentYearBE - entryYear) + 1;

            Integer userIdInt;
            try {
                userIdInt = Integer.parseInt(studentId);
            } catch (NumberFormatException e) {
                userIdInt = 1;
            }

            boolean alreadyReviewed = reviewRepository.existsByCourseIdAndUserId(courseId, userIdInt);

            return studentYear >= 1 && !alreadyReviewed;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public Review saveReview(Review review) {
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }
        return reviewRepository.save(review);
    }
}