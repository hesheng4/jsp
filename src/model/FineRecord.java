package model;

import java.sql.Timestamp;

/**
 * 罚款记录模型类
 */
public class FineRecord {
    private Integer fineId;
    private Long accountId;
    private Integer recordId;
    private String username;
    private String bookTitle;
    private Double fineAmount;
    private String fineReason;
    private Timestamp createDate;
    private String payStatus;  // 未缴纳、已缴纳
    private Timestamp payDate;
    
    public FineRecord() {}
    
    public Integer getFineId() { return fineId; }
    public void setFineId(Integer fineId) { this.fineId = fineId; }
    
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    
    public Integer getRecordId() { return recordId; }
    public void setRecordId(Integer recordId) { this.recordId = recordId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    
    public Double getFineAmount() { return fineAmount; }
    public void setFineAmount(Double fineAmount) { this.fineAmount = fineAmount; }
    
    public String getFineReason() { return fineReason; }
    public void setFineReason(String fineReason) { this.fineReason = fineReason; }
    
    public Timestamp getCreateDate() { return createDate; }
    public void setCreateDate(Timestamp createDate) { this.createDate = createDate; }
    
    public String getPayStatus() { return payStatus; }
    public void setPayStatus(String payStatus) { this.payStatus = payStatus; }
    
    public Timestamp getPayDate() { return payDate; }
    public void setPayDate(Timestamp payDate) { this.payDate = payDate; }
}
