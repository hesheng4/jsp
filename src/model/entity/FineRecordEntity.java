package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "fine_records")
public class FineRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fine_id")
    private Integer fineId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "record_id", nullable = false)
    private Integer recordId;

    @Column(name = "fine_amount", nullable = false)
    private Double fineAmount;

    @Column(name = "fine_reason", length = 255)
    private String fineReason;

    @Column(name = "create_date")
    private java.sql.Timestamp createDate;

    @Column(name = "pay_status", length = 20)
    private String payStatus = "未缴纳";

    @Column(name = "pay_date")
    private java.sql.Timestamp payDate;

    public FineRecordEntity() {}

    public Integer getFineId() { return fineId; }
    public void setFineId(Integer fineId) { this.fineId = fineId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Integer getRecordId() { return recordId; }
    public void setRecordId(Integer recordId) { this.recordId = recordId; }

    public Double getFineAmount() { return fineAmount; }
    public void setFineAmount(Double fineAmount) { this.fineAmount = fineAmount; }

    public String getFineReason() { return fineReason; }
    public void setFineReason(String fineReason) { this.fineReason = fineReason; }

    public java.sql.Timestamp getCreateDate() { return createDate; }
    public void setCreateDate(java.sql.Timestamp createDate) { this.createDate = createDate; }

    public String getPayStatus() { return payStatus; }
    public void setPayStatus(String payStatus) { this.payStatus = payStatus; }

    public java.sql.Timestamp getPayDate() { return payDate; }
    public void setPayDate(java.sql.Timestamp payDate) { this.payDate = payDate; }
}
