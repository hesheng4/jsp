package model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "book_reviews",
       uniqueConstraints = @UniqueConstraint(columnNames = {"account_id", "book_id"}))
public class BookReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(name = "rating", nullable = false)
    @NotNull @Min(1) @Max(5)
    private Integer rating;

    @Column(name = "review_content", columnDefinition = "TEXT")
    private String reviewContent;

    @Column(name = "review_date")
    private java.sql.Timestamp reviewDate;

    @Column(name = "is_visible")
    private Boolean isVisible = true;

    public BookReviewEntity() {}

    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getReviewContent() { return reviewContent; }
    public void setReviewContent(String reviewContent) { this.reviewContent = reviewContent; }

    public java.sql.Timestamp getReviewDate() { return reviewDate; }
    public void setReviewDate(java.sql.Timestamp reviewDate) { this.reviewDate = reviewDate; }

    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }
}
