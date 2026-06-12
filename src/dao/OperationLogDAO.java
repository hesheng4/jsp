package dao;

import database.DatabaseConnection;
import model.OperationLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 操作日志数据访问对象
 */
public class OperationLogDAO {
    private static final Logger LOGGER = AppLogger.getLogger(OperationLogDAO.class);

    
    /**
     * 记录操作日志
     */
    public void logOperation(Long accountId, String username, String operationType, String operationDetail) {
        String sql = "INSERT INTO operation_logs (account_id, username, operation_type, operation_detail, operation_time) " +
                     "VALUES (?, ?, ?, ?, NOW())";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, accountId);
            pstmt.setString(2, username);
            pstmt.setString(3, operationType);
            pstmt.setString(4, operationDetail);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            LOGGER.warning("记录操作日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有操作日志（管理员查看）
     */
    public List<OperationLog> getAllLogs() {
        List<OperationLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM operation_logs ORDER BY operation_time DESC LIMIT 1000";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                OperationLog log = new OperationLog();
                log.setLogId(rs.getInt("log_id"));
                log.setAccountId(rs.getLong("account_id"));
                log.setUsername(rs.getString("username"));
                log.setOperationType(rs.getString("operation_type"));
                log.setOperationDetail(rs.getString("operation_detail"));
                log.setOperationTime(rs.getTimestamp("operation_time"));
                log.setIpAddress(rs.getString("ip_address"));
                logs.add(log);
            }
            
        } catch (SQLException e) {
            LOGGER.warning("查询操作日志失败: " + e.getMessage());
        }
        
        return logs;
    }
}
