package dao;

import database.DatabaseConnection;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 授权管理数据访问对象
 * 仅root用户可以执行授权操作
 */
public class AuthorizationDAO {
    private static final Logger LOGGER = AppLogger.getLogger(AuthorizationDAO.class);

    
    /**
     * 检查是否为root用户
     */
    public boolean isRoot(Long accountId) {
        String sql = "SELECT is_root FROM users WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("is_root") == 1;
            }
        } catch (SQLException e) {
            LOGGER.warning("检查root权限失败: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 授权账号（启用账号）
     */
    public boolean authorizeUser(Long rootId, Long targetId) {
        if (!isRoot(rootId)) {
            LOGGER.warning("非root用户无权执行授权操作");
            return false;
        }
        
        String sql = "UPDATE users SET is_enabled = 1, authorized_by = ?, authorized_date = NOW() WHERE account_id = ? AND is_root = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, rootId);
            pstmt.setLong(2, targetId);
            if (pstmt.executeUpdate() > 0) {
                logAuthorization(rootId, targetId, "授权", "启用账号");
                return true;
            }
        } catch (SQLException e) {
            LOGGER.warning("授权失败: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 回收授权（禁用账号）
     */
    public boolean revokeAuthorization(Long rootId, Long targetId) {
        if (!isRoot(rootId)) {
            LOGGER.warning("非root用户无权执行回收操作");
            return false;
        }
        
        String sql = "UPDATE users SET is_enabled = 0, authorized_by = NULL, authorized_date = NULL WHERE account_id = ? AND is_root = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, targetId);
            if (pstmt.executeUpdate() > 0) {
                logAuthorization(rootId, targetId, "回收", "禁用账号");
                return true;
            }
        } catch (SQLException e) {
            LOGGER.warning("回收授权失败: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 批量授权
     */
    public int batchAuthorize(Long rootId, List<Long> targetIds) {
        if (!isRoot(rootId)) return 0;
        int count = 0;
        for (Long targetId : targetIds) {
            if (authorizeUser(rootId, targetId)) count++;
        }
        return count;
    }
    
    /**
     * 批量回收授权
     */
    public int batchRevoke(Long rootId, List<Long> targetIds) {
        if (!isRoot(rootId)) return 0;
        int count = 0;
        for (Long targetId : targetIds) {
            if (revokeAuthorization(rootId, targetId)) count++;
        }
        return count;
    }
    
    /**
     * 授予管理员权限
     */
    public boolean grantAdmin(Long rootId, Long targetId) {
        if (!isRoot(rootId)) {
            LOGGER.warning("非root用户无权执行此操作");
            return false;
        }
        
        String sql = "UPDATE users SET is_admin = 1 WHERE account_id = ? AND is_root = 0 AND is_enabled = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, targetId);
            if (pstmt.executeUpdate() > 0) {
                logAuthorization(rootId, targetId, "设为管理员", "授予管理员权限");
                return true;
            }
        } catch (SQLException e) {
            LOGGER.warning("授予管理员权限失败: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 撤销管理员权限
     */
    public boolean revokeAdmin(Long rootId, Long targetId) {
        if (!isRoot(rootId)) {
            LOGGER.warning("非root用户无权执行此操作");
            return false;
        }
        
        String sql = "UPDATE users SET is_admin = 0 WHERE account_id = ? AND is_root = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, targetId);
            if (pstmt.executeUpdate() > 0) {
                logAuthorization(rootId, targetId, "取消管理员", "撤销管理员权限");
                return true;
            }
        } catch (SQLException e) {
            LOGGER.warning("撤销管理员权限失败: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 获取待授权用户列表
     */
    public List<User> getPendingUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_enabled = 0 AND is_root = 0 ORDER BY account_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("查询待授权用户失败: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * 获取已授权用户列表
     */
    public List<User> getAuthorizedUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_enabled = 1 AND is_root = 0 ORDER BY authorized_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("查询已授权用户失败: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * 获取所有管理员列表
     */
    public List<User> getAdminUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_admin = 1 AND is_root = 0 ORDER BY account_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("查询管理员列表失败: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * 获取授权记录
     */
    public List<Map<String, Object>> getAuthorizationLogs(int limit) {
        List<Map<String, Object>> logs = new ArrayList<>();
        String sql = "SELECT al.*, u1.username as operator_name, u2.username as target_name " +
                     "FROM authorization_logs al " +
                     "LEFT JOIN users u1 ON al.operator_id = u1.account_id " +
                     "LEFT JOIN users u2 ON al.target_id = u2.account_id " +
                     "ORDER BY al.action_time DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> log = new HashMap<>();
                log.put("logId", rs.getInt("log_id"));
                log.put("operatorId", rs.getLong("operator_id"));
                log.put("operatorName", rs.getString("operator_name"));
                log.put("targetId", rs.getLong("target_id"));
                log.put("targetName", rs.getString("target_name"));
                log.put("actionType", rs.getString("action_type"));
                log.put("actionDetail", rs.getString("action_detail"));
                log.put("actionTime", rs.getTimestamp("action_time"));
                logs.add(log);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询授权记录失败: " + e.getMessage());
        }
        return logs;
    }
    
    /**
     * 记录授权操作日志
     */
    private void logAuthorization(Long operatorId, Long targetId, String actionType, String detail) {
        String sql = "INSERT INTO authorization_logs (operator_id, target_id, action_type, action_detail) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, operatorId);
            pstmt.setLong(2, targetId);
            pstmt.setString(3, actionType);
            pstmt.setString(4, detail);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.warning("记录授权日志失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查账号是否已启用
     */
    public boolean isEnabled(Long accountId) {
        String sql = "SELECT is_enabled, is_root, is_admin FROM users WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // root用户和管理员始终启用
                return rs.getInt("is_root") == 1 || rs.getInt("is_admin") == 1 || rs.getInt("is_enabled") == 1;
            }
        } catch (SQLException e) {
            LOGGER.warning("检查账号状态失败: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 获取授权统计
     */
    public Map<String, Integer> getAuthorizationStats() {
        Map<String, Integer> stats = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 待授权数量
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE is_enabled = 0 AND is_root = 0");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("pending", rs.getInt(1));
            }
            // 已授权数量
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE is_enabled = 1 AND is_root = 0");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("authorized", rs.getInt(1));
            }
            // 管理员数量
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE is_admin = 1 AND is_root = 0");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("admins", rs.getInt(1));
            }
        } catch (SQLException e) {
            LOGGER.warning("获取授权统计失败: " + e.getMessage());
        }
        return stats;
    }
    
    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setAccountId(rs.getLong("account_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setAdmin(rs.getInt("is_admin") == 1);
        try {
            user.setEmail(rs.getString("email"));
            user.setAddress(rs.getString("address"));
            user.setBorrowLimit(rs.getInt("borrow_limit"));
            user.setBlacklisted(rs.getInt("is_blacklisted") == 1);
            user.setBlacklistReason(rs.getString("blacklist_reason"));
            user.setBlacklistDate(rs.getTimestamp("blacklist_date"));
            user.setRoot(rs.getInt("is_root") == 1);
            user.setEnabled(rs.getInt("is_enabled") == 1);
            user.setAuthorizedBy(rs.getLong("authorized_by"));
            if (rs.wasNull()) user.setAuthorizedBy(null);
            user.setAuthorizedDate(rs.getTimestamp("authorized_date"));
        } catch (SQLException ignored) {}
        return user;
    }
}
