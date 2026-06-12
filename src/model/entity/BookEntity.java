package model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "books")
public class BookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Integer bookId;

    @Column(name = "title", nullable = false, length = 200)
    @NotBlank(message = "书名不能为空")
    @Size(max = 200, message = "书名最长200字")
    private String title;

    @Column(name = "author", length = 100)
    private String author;

    @Column(name = "isbn", length = 50)
    @Pattern(regexp = "^(\\d{10}|\\d{13})?$", message = "ISBN格式错误")
    private String isbn;

    @Column(name = "category_id")
    private Integer categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private CategoryEntity category;

    @Column(name = "total_copies", nullable = false)
    @NotNull @Min(value = 1, message = "总册数至少1")
    private Integer totalCopies = 1;

    @Column(name = "available_copies", nullable = false)
    @NotNull @Min(value = 0, message = "可借册数不能为负")
    private Integer availableCopies = 1;

    @Column(name = "publisher", length = 100)
    private String publisher;

    @Column(name = "publish_date", length = 20)
    private String publishDate;

    @Column(name = "add_date", length = 20)
    private String addDate;

    public BookEntity() {}

    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public CategoryEntity getCategory() { return category; }
    public void setCategory(CategoryEntity category) { this.category = category; }

    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }

    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getPublishDate() { return publishDate; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }

    public String getAddDate() { return addDate; }
    public void setAddDate(String addDate) { this.addDate = addDate; }
}
