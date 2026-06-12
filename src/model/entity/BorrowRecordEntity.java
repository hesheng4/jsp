package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "borrow_records")
public class BorrowRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Integer recordId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(name = "borrow_date", nullable = false)
    private java.sql.Date borrowDate;

    @Column(name = "due_date")
    private java.sql.Date dueDate;

    @Column(name = "return_date")
    private java.sql.Date returnDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "借阅中";

    @Column(name = "renew_count", nullable = false)
    private Integer renewCount = 0;

    @Column(name = "fine_amount", nullable = false)
    private Double fineAmount = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", insertable = false, updatable = false)
    private BookEntity book;

    public BorrowRecordEntity() {}

    public Integer getRecordId() { return recordId; }
    public void setRecordId(Integer recordId) { this.recordId = recordId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public java.sql.Date getBorrowDate() { return borrowDate; }
    public void setBorrowDate(java.sql.Date borrowDate) { this.borrowDate = borrowDate; }

    public java.sql.Date getDueDate() { return dueDate; }
    public void setDueDate(java.sql.Date dueDate) { this.dueDate = dueDate; }

    public java.sql.Date getReturnDate() { return returnDate; }
    public void setReturnDate(java.sql.Date returnDate) { this.returnDate = returnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getRenewCount() { return renewCount; }
    public void setRenewCount(Integer renewCount) { this.renewCount = renewCount; }

    public Double getFineAmount() { return fineAmount; }
    public void setFineAmount(Double fineAmount) { this.fineAmount = fineAmount; }

    public UserEntity getUser() { return user; }
    public BookEntity getBook() { return book; }
}
