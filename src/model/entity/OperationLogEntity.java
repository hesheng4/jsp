package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "operation_logs")
public class OperationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;

    @Column(name = "operation_detail", length = 500)
    private String operationDetail;

    @Column(name = "operation_time", nullable = false)
    private java.sql.Timestamp operationTime;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    public OperationLogEntity() {}

    public Integer getLogId() { return logId; }
    public void setLogId(Integer logId) { this.logId = logId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public String getOperationDetail() { return operationDetail; }
    public void setOperationDetail(String operationDetail) { this.operationDetail = operationDetail; }

    public java.sql.Timestamp getOperationTime() { return operationTime; }
    public void setOperationTime(java.sql.Timestamp operationTime) { this.operationTime = operationTime; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
}
