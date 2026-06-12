package model;

/**
 * 图书模型类
 */
public class Book {
    private Integer bookId;
    private String title;
    private String author;
    private String isbn;
    private Integer categoryId;
    private String categoryName;
    private Integer totalCopies; // 总册数
    private Integer availableCopies; // 可借册数
    private String publisher;
    private String publishDate;
    private String addDate; // 添加时间
    
    public Book() {
    }
    
    public Book(Integer bookId, String title, String author, String isbn, 
                Integer categoryId, Integer totalCopies, Integer availableCopies,
                String publisher, String publishDate) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.categoryId = categoryId;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.publisher = publisher;
        this.publishDate = publishDate;
    }
    
    public Integer getBookId() {
        return bookId;
    }
    
    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public Integer getTotalCopies() {
        return totalCopies;
    }
    
    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }
    
    public Integer getAvailableCopies() {
        return availableCopies;
    }
    
    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public String getPublishDate() {
        return publishDate;
    }
    
    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }
    
    public String getAddDate() {
        return addDate;
    }
    
    public void setAddDate(String addDate) {
        this.addDate = addDate;
    }
}

