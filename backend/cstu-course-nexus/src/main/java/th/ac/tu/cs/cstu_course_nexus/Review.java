package th.ac.tu.cs.cstu_course_nexus;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Column(name = "course_id", nullable = false)
    private String courseId;

    @Column(name = "section_id")
    private String sectionId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "reviewer_year", nullable = false)
    private Integer reviewerYear;

    @Column(name = "review_text", nullable = false, columnDefinition = "TEXT")
    private String reviewText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Review() {}

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.reviewerYear == null) {
            this.reviewerYear = 1;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getSectionId() { return sectionId; }
    public void setSectionId(String sectionId) { this.sectionId = sectionId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getReviewerYear() { return reviewerYear; }
    public void setReviewerYear(Integer reviewerYear) { this.reviewerYear = reviewerYear; }

    public String getReviewText() { return reviewText; }
    public void setReviewText(String reviewText) { this.reviewText = reviewText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Helper compatibility methods for frontend
    public String getText() { return reviewText; }
    public void setText(String text) {
        if (this.reviewText == null || this.reviewText.isBlank()) {
            this.reviewText = text;
        }
    }

    public String getComment() { return reviewText; }
    public void setComment(String comment) {
        if (this.reviewText == null || this.reviewText.isBlank()) {
            this.reviewText = comment;
        }
    }

    public Integer getYear() { return reviewerYear; }

    public String getDate() {
        if (createdAt == null) return "";
        return createdAt.toLocalDate().toString();
    }
}