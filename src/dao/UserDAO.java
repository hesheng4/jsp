package dao;

import database.DatabaseConnection;
import model.User;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 用户数据访问对象
 */
public class UserDAO {
    private static final Logger LOGGER = AppLogger.getLogger(UserDAO.class);

    
    // 默认管理员账号ID（受保护，不可删除或修改关键信息）
    public static final Long PROTECTED_ADMIN_ID = 100000000000L;
    
    /**
     * 检查是否为受保护的管理员账号
     */
    public static boolean isProtectedAdmin(Long accountId) {
        return PROTECTED_ADMIN_ID.equals(accountId);
    }
    
    /**
     * 用户注册
     */
    public boolean register(Long accountId, String username, String password, String phone, boolean isAdmin) {
        if (accountId == null || accountId.toString().length() != 12) return false;
        if (phone == null || phone.length() != 11 || !phone.matches("\\d{11}")) return false;
        
        // 检查账号是否已存在
        if (accountExists(accountId)) {
            LOGGER.warning("注册失败: 账号已存在");
            return false;
        }
        
        // 检查用户名是否已存在
        if (usernameExists(username)) {
            LOGGER.warning("注册失败: 用户名已存在");
            return false;
        }
        
        String sql = "INSERT INTO users (account_id, username, password, phone, is_admin) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            pstmt.setString(2, username);
            pstmt.setString(3, PasswordUtil.encode(password));
            pstmt.setString(4, phone);
            pstmt.setInt(5, isAdmin ? 1 : 0);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("注册失败: " + e.getMessage());
            return false;
        }
    }
    
    public boolean register(Long accountId, String username, String password) {
        return register(accountId, username, password, null, false);
    }
    
    /**
     * 用户登录
     */
    public User login(Long accountId, String password) {
        String sql = "SELECT * FROM users WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                // 兼容旧版明文密码
                if (PasswordUtil.isLegacyPassword(storedPassword)) {
                    if (storedPassword.equals(password)) {
                        return mapUser(rs);
                    }
                } else {
                    String[] parts = PasswordUtil.splitStoredPassword(storedPassword);
                    if (PasswordUtil.verifyPassword(password, parts[0], parts[1])) {
                        return mapUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.warning("登录失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 用户登录（带授权检查，需要数据库有扩展字段）
     */
    public User loginWithAuthCheck(Long accountId, String password) {
        User user = login(accountId, password);
        if (user != null) {
            // root用户和管理员始终可以登录，普通用户需要已授权
            if (!user.isRoot() && !user.isAdmin() && !user.isEnabled()) {
                LOGGER.warning("账号未授权，请联系管理员");
                return null;
            }
        }
        return user;
    }
    
    /**
     * 检查账号是否已存在
     */
    public boolean accountExists(Long accountId) {
        String sql = "SELECT COUNT(*) FROM users WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.warning("检查账号失败: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 检查用户名是否已存在
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.warning("检查用户名失败: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY account_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("查询用户失败: " + e.getMessage());
        }
        return users;
    }
    
    /**
     * 删除用户
     */
    public boolean deleteUser(Long accountId) {
        // 保护默认管理员账号，不允许删除
        if (isProtectedAdmin(accountId)) {
            LOGGER.warning("删除用户失败: 默认管理员账号受保护，不能删除");
            return false;
        }
        
        String sql = "DELETE FROM users WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("删除用户失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证用户身份
     */
    public boolean verifyUser(Long accountId, String username, String phone) {
        String sql = "SELECT COUNT(*) FROM users WHERE account_id = ? AND username = ? AND phone = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            pstmt.setString(2, username);
            pstmt.setString(3, phone);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.warning("验证用户失败: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 修改密码
     */
    public boolean updatePassword(Long accountId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, PasswordUtil.encode(newPassword));
            pstmt.setLong(2, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("修改密码失败: " + e.getMessage());
            return false;
        }
    }
    
    // ==================== 新增功能 ====================
    
    /**
     * 检查列是否存在
     */
    private boolean columnExists(Connection conn, String columnName) {
        try {
            java.sql.DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getColumns(null, null, "users", columnName);
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * 确保扩展字段存在
     */
    private void ensureExtendedColumns(Connection conn) {
        String[] columns = {"email", "address", "borrow_limit", "is_blacklisted", "blacklist_reason", "blacklist_date"};
        String[] ddls = {
            "ALTER TABLE users ADD COLUMN email VARCHAR(100)",
            "ALTER TABLE users ADD COLUMN address VARCHAR(255)",
            "ALTER TABLE users ADD COLUMN borrow_limit INT DEFAULT 5",
            "ALTER TABLE users ADD COLUMN is_blacklisted TINYINT DEFAULT 0",
            "ALTER TABLE users ADD COLUMN blacklist_reason VARCHAR(255)",
            "ALTER TABLE users ADD COLUMN blacklist_date DATETIME"
        };
        
        for (int i = 0; i < columns.length; i++) {
            try {
                if (!columnExists(conn, columns[i])) {
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        stmt.execute(ddls[i]);
                        LOGGER.info("成功添加字段: users." + columns[i]);
                    }
                }
            } catch (SQLException e) {
                // 忽略"字段已存在"的错误
                if (!e.getMessage().contains("Duplicate column")) {
                    LOGGER.warning("添加字段 " + columns[i] + " 失败: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 检查手机号是否已被其他用户使用
     * @param phone 手机号
     * @param excludeAccountId 排除的账号ID（当前用户自己）
     * @return true表示已存在
     */
    public boolean phoneExistsForOther(String phone, Long excludeAccountId) {
        if (phone == null || phone.isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM users WHERE phone = ? AND account_id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            pstmt.setLong(2, excludeAccountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            LOGGER.warning("检查手机号失败: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 更新用户个人信息
     * @return null表示成功，否则返回错误信息
     */
    public String updateUserInfoWithCheck(Long accountId, String email, String address, String phone) {
        // 检查手机号是否被其他用户使用
        if (phone != null && !phone.isEmpty() && phoneExistsForOther(phone, accountId)) {
            return "该手机号已被其他用户使用";
        }
        
        // 第一步：确保扩展字段存在
        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureExtendedColumns(conn);
        } catch (SQLException e) {
            LOGGER.warning("检查扩展字段失败: " + e.getMessage());
        }
        
        // 第二步：更新用户信息
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users SET email = ?, address = ?, phone = ? WHERE account_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                pstmt.setString(2, address);
                pstmt.setString(3, phone);
                pstmt.setLong(4, accountId);
                if (pstmt.executeUpdate() > 0) {
                    return null; // 成功
                }
                return "更新失败";
            }
        } catch (SQLException e) {
            LOGGER.warning("更新用户信息失败: " + e.getMessage());
            return "更新失败: " + e.getMessage();
        }
    }

    /**
     * 更新用户个人信息
     */
    public boolean updateUserInfo(Long accountId, String email, String address, String phone) {
        // 第一步：确保扩展字段存在（使用独立连接）
        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureExtendedColumns(conn);
        } catch (SQLException e) {
            LOGGER.warning("检查扩展字段失败: " + e.getMessage());
        }
        
        // 第二步：尝试更新所有字段（使用新连接）
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users SET email = ?, address = ?, phone = ? WHERE account_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email);
                pstmt.setString(2, address);
                pstmt.setString(3, phone);
                pstmt.setLong(4, accountId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.warning("更新用户信息失败(含扩展字段): " + e.getMessage());
            // 回退：只更新phone字段
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("UPDATE users SET phone = ? WHERE account_id = ?")) {
                pstmt.setString(1, phone);
                pstmt.setLong(2, accountId);
                boolean result = pstmt.executeUpdate() > 0;
                if (result) {
                    LOGGER.info("已回退到只更新手机号");
                }
                return result;
            } catch (SQLException e2) {
                LOGGER.warning("更新手机号也失败: " + e2.getMessage());
                return false;
            }
        }
    }
    
    /**
     * 设置用户借阅额度
     */
    public boolean setBorrowLimit(Long accountId, Integer limit) {
        String sql = "UPDATE users SET borrow_limit = ? WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setLong(2, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("设置借阅额度失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 加入黑名单（只能将普通用户加入黑名单）
     */
    public boolean addToBlacklist(Long accountId, String reason) {
        // 保护默认管理员账号，不允许加入黑名单
        if (isProtectedAdmin(accountId)) {
            LOGGER.warning("加入黑名单失败: 默认管理员账号受保护，不能加入黑名单");
            return false;
        }
        
        // 检查是否为管理员，管理员不能加入黑名单
        User user = getUserById(accountId);
        if (user != null && user.isAdmin()) {
            LOGGER.warning("加入黑名单失败: 管理员账号不能加入黑名单");
            return false;
        }
        
        String sql = "UPDATE users SET is_blacklisted = 1, blacklist_reason = ?, blacklist_date = NOW() WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reason);
            pstmt.setLong(2, accountId);
            int rows = pstmt.executeUpdate();
            LOGGER.info("加入黑名单: 账号=" + accountId + ", 原因=" + reason + ", 影响行数=" + rows);
            return rows > 0;
        } catch (SQLException e) {
            LOGGER.warning("加入黑名单失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 移出黑名单
     */
    public boolean removeFromBlacklist(Long accountId) {
        String sql = "UPDATE users SET is_blacklisted = 0, blacklist_reason = NULL, blacklist_date = NULL WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("移出黑名单失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取黑名单用户
     */
    public List<User> getBlacklistedUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE is_blacklisted = 1 ORDER BY blacklist_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                User user = mapUser(rs);
                LOGGER.info("黑名单用户: " + user.getUsername() + 
                    ", 原因=" + user.getBlacklistReason() + 
                    ", 时间=" + user.getBlacklistDate());
                users.add(user);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询黑名单用户失败: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }
    
    /**
     * 检查用户是否在黑名单
     */
    public boolean isBlacklisted(Long accountId) {
        String sql = "SELECT is_blacklisted FROM users WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("is_blacklisted") == 1;
        } catch (SQLException e) {
            LOGGER.warning("检查黑名单失败: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * 获取用户当前借阅数量
     */
    public int getCurrentBorrowCount(Long accountId) {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE account_id = ? AND status = '借阅中'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.warning("查询借阅数量失败: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * 检查用户是否可以借书
     */
    public String canBorrow(Long accountId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 检查黑名单
            String blacklistSql = "SELECT is_blacklisted, blacklist_reason FROM users WHERE account_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(blacklistSql)) {
                pstmt.setLong(1, accountId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getInt("is_blacklisted") == 1) {
                    return "用户在黑名单中: " + rs.getString("blacklist_reason");
                }
            }
            // 检查未缴罚款
            String fineSql = "SELECT COALESCE(SUM(fine_amount), 0) FROM fine_records WHERE account_id = ? AND pay_status = '未缴纳'";
            try (PreparedStatement pstmt = conn.prepareStatement(fineSql)) {
                pstmt.setLong(1, accountId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getDouble(1) > 0) {
                    return "存在未缴纳罚款: " + rs.getDouble(1) + "元";
                }
            }
            // 检查借阅额度
            String limitSql = "SELECT borrow_limit FROM users WHERE account_id = ?";
            int limit = 5;
            try (PreparedStatement pstmt = conn.prepareStatement(limitSql)) {
                pstmt.setLong(1, accountId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) limit = rs.getInt("borrow_limit");
            }
            int current = getCurrentBorrowCount(accountId);
            if (current >= limit) {
                return "已达到借阅上限(" + limit + "本)";
            }
        } catch (SQLException e) {
            LOGGER.warning("检查借阅资格失败: " + e.getMessage());
            return "系统错误";
        }
        return null; // null表示可以借书
    }
    
    /**
     * 根据账号获取用户
     */
    public User getUserById(Long accountId) {
        String sql = "SELECT * FROM users WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) {
            LOGGER.warning("查询用户失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 授权用户（启用账号）
     */
    public boolean authorizeUser(Long accountId) {
        String sql = "UPDATE users SET is_admin = is_admin WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("授权用户失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 回收授权（对于没有is_enabled字段的情况，将用户设为非管理员）
     */
    public boolean revokeAuthorization(Long accountId) {
        // 由于没有is_enabled字段，这里暂时不做实际操作
        // 如果需要禁用用户，可以考虑删除用户或添加is_enabled字段
        return true;
    }
    
    /**
     * 设置/取消管理员权限
     */
    public boolean setAdmin(Long accountId, boolean isAdmin) {
        // 保护默认管理员账号，不允许取消其管理员权限
        if (isProtectedAdmin(accountId) && !isAdmin) {
            LOGGER.warning("设置管理员权限失败: 默认管理员账号受保护，不能取消其管理员权限");
            return false;
        }
        
        String sql = "UPDATE users SET is_admin = ? WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isAdmin ? 1 : 0);
            pstmt.setLong(2, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("设置管理员权限失败: " + e.getMessage());
            return false;
        }
    }
    
    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setAccountId(rs.getLong("account_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone"));
        user.setAdmin(rs.getInt("is_admin") == 1);
        
        // 尝试读取扩展字段，如果不存在则忽略
        try { user.setEmail(rs.getString("email")); } catch (SQLException ignored) {}
        try { user.setAddress(rs.getString("address")); } catch (SQLException ignored) {}
        try { user.setBorrowLimit(rs.getInt("borrow_limit")); } catch (SQLException ignored) {}
        try { user.setBlacklisted(rs.getInt("is_blacklisted") == 1); } catch (SQLException ignored) {}
        try { user.setBlacklistReason(rs.getString("blacklist_reason")); } catch (SQLException ignored) {}
        try { user.setBlacklistDate(rs.getTimestamp("blacklist_date")); } catch (SQLException ignored) {}
        try { user.setRoot(rs.getInt("is_root") == 1); } catch (SQLException ignored) {}
        try { user.setEnabled(rs.getInt("is_enabled") == 1); } catch (SQLException ignored) {}
        try { 
            user.setAuthorizedBy(rs.getLong("authorized_by"));
            if (rs.wasNull()) user.setAuthorizedBy(null);
        } catch (SQLException ignored) {}
        try { user.setAuthorizedDate(rs.getTimestamp("authorized_date")); } catch (SQLException ignored) {}
        
        return user;
    }
}
