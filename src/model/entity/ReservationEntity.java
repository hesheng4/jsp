package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reservations")
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Integer reservationId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @Column(name = "reservation_date")
    private java.sql.Timestamp reservationDate;

    @Column(name = "status", length = 20)
    private String status = "等待中";

    @Column(name = "notify_date")
    private java.sql.Timestamp notifyDate;

    @Column(name = "expire_date")
    private java.sql.Timestamp expireDate;

    public ReservationEntity() {}

    public Integer getReservationId() { return reservationId; }
    public void setReservationId(Integer reservationId) { this.reservationId = reservationId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Integer getBookId() { return bookId; }
    public void setBookId(Integer bookId) { this.bookId = bookId; }

    public java.sql.Timestamp getReservationDate() { return reservationDate; }
    public void setReservationDate(java.sql.Timestamp reservationDate) { this.reservationDate = reservationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public java.sql.Timestamp getNotifyDate() { return notifyDate; }
    public void setNotifyDate(java.sql.Timestamp notifyDate) { this.notifyDate = notifyDate; }

    public java.sql.Timestamp getExpireDate() { return expireDate; }
    public void setExpireDate(java.sql.Timestamp expireDate) { this.expireDate = expireDate; }
}
