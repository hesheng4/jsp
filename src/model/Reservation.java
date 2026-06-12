package model;

import java.sql.Timestamp;

/**
 * 图书预约模型类
 */
public class Reservation {
    private Integer reservationId;
    private Long accountId;
    private Integer bookId;
    private String bookTitle;
    private String username;
    private Timestamp reservationDate;
    private String status;  // 等待中、已通知、已取消、已完成
    private Timestamp notifyDate;
    private Timestamp expireDate;
    
    public Reservation() {}
    
    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }
    
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    
    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }
    
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public Timestamp getReservationDate() { return reservationDate; }
    public void setReservationDate(Timestamp reservationDate) { this.reservationDate = reservationDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Timestamp getNotifyDate() { return notifyDate; }
    public void setNotifyDate(Timestamp notifyDate) { this.notifyDate = notifyDate; }
    
    public Timestamp getExpireDate() { return expireDate; }
    public void setExpireDate(Timestamp expireDate) { this.expireDate = expireDate; }
}
