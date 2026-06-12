package model;

import java.sql.Date;

/**
 * 借阅记录模型类
 */
public class BorrowRecord {
    private Integer recordId;
    private Long accountId;
    private Integer bookId;
    private String bookTitle;
    private Date borrowDate;
    private Date dueDate; // 到期日期
    private Date returnDate;
    private String status; // "借阅中" 或 "已归还"
    private Integer renewCount; // 续借次数（最多1次）
    private Double fineAmount; // 罚款金额
    private String accountName; // 用户名（管理员查看时使用）
    
    public BorrowRecord() {
    }
    
    public BorrowRecord(Integer recordId, Long accountId, Integer bookId, 
                       Date borrowDate, Date returnDate, String status) {
        this.recordId = recordId;
        this.accountId = accountId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }
    
    public Integer getRecordId() {
        return recordId;
    }
    
    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }
    
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    public Integer getBookId() {
        return bookId;
    }
    
    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public Date getBorrowDate() {
        return borrowDate;
    }
    
    public void setBorrowDate(Date borrowDate) {
        this.borrowDate = borrowDate;
    }
    
    public Date getReturnDate() {
        return returnDate;
    }
    
    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
    
    public Integer getRenewCount() {
        return renewCount;
    }
    
    public void setRenewCount(Integer renewCount) {
        this.renewCount = renewCount;
    }
    
    public Double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(Double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}

