package model;

import java.sql.Timestamp;

/**
 * 操作日志模型类
 */
public class OperationLog {
    private Integer logId;
    private Long accountId;
    private String username;
    private String operationType;
    private String operationDetail;
    private Timestamp operationTime;
    private String ipAddress;
    
    public OperationLog() {
    }
    
    public OperationLog(Long accountId, String username, String operationType, String operationDetail) {
        this.accountId = accountId;
        this.username = username;
        this.operationType = operationType;
        this.operationDetail = operationDetail;
        this.operationTime = new Timestamp(System.currentTimeMillis());
    }
    
    public Integer getLogId() {
        return logId;
    }
    
    public void setLogId(Integer logId) {
        this.logId = logId;
    }
    
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
    
    public String getOperationDetail() {
        return operationDetail;
    }
    
    public void setOperationDetail(String operationDetail) {
        this.operationDetail = operationDetail;
    }
    
    public Timestamp getOperationTime() {
        return operationTime;
    }
    
    public void setOperationTime(Timestamp operationTime) {
        this.operationTime = operationTime;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
