package model.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "authorization_logs")
public class AuthorizationLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @Column(name = "action_detail", length = 255)
    private String actionDetail;

    @Column(name = "action_time")
    private java.sql.Timestamp actionTime;

    public AuthorizationLogEntity() {}

    public Integer getLogId() { return logId; }
    public void setLogId(Integer logId) { this.logId = logId; }

    public Long getOperatorId() { return operatorId; }
    public void setOperatorId(Long operatorId) { this.operatorId = operatorId; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getActionDetail() { return actionDetail; }
    public void setActionDetail(String actionDetail) { this.actionDetail = actionDetail; }

    public java.sql.Timestamp getActionTime() { return actionTime; }
    public void setActionTime(java.sql.Timestamp actionTime) { this.actionTime = actionTime; }
}
