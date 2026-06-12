package dao;

import database.DatabaseConnection;
import model.FineRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 罚款记录数据访问对象
 */
public class FineDAO {
    private static final Logger LOGGER = AppLogger.getLogger(FineDAO.class);

    
    /**
     * 缴纳罚款
     */
    public boolean payFine(Integer fineId) {
        String sql = "UPDATE fine_records SET pay_status = '已缴纳', pay_date = NOW() WHERE fine_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, fineId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("缴纳罚款失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 批量缴纳用户所有罚款
     */
    public boolean payAllFines(Long accountId) {
        String sql = "UPDATE fine_records SET pay_status = '已缴纳', pay_date = NOW() WHERE account_id = ? AND pay_status = '未缴纳'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("批量缴纳罚款失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取用户未缴罚款总额
     */
    public Double getUnpaidFineAmount(Long accountId) {
        String sql = "SELECT COALESCE(SUM(fine_amount), 0) as total FROM fine_records WHERE account_id = ? AND pay_status = '未缴纳'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            LOGGER.warning("查询未缴罚款失败: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * 获取用户的罚款记录
     */
    public List<FineRecord> getUserFines(Long accountId) {
        List<FineRecord> list = new ArrayList<>();
        String sql = "SELECT f.*, b.title as book_title FROM fine_records f " +
                     "LEFT JOIN borrow_records br ON f.record_id = br.record_id " +
                     "LEFT JOIN books b ON br.book_id = b.book_id " +
                     "WHERE f.account_id = ? ORDER BY f.create_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapFineRecord(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("查询罚款记录失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 获取所有未缴罚款记录（管理员）
     */
    public List<FineRecord> getAllUnpaidFines() {
        List<FineRecord> list = new ArrayList<>();
        String sql = "SELECT f.*, u.username, b.title as book_title FROM fine_records f " +
                     "LEFT JOIN users u ON f.account_id = u.account_id " +
                     "LEFT JOIN borrow_records br ON f.record_id = br.record_id " +
                     "LEFT JOIN books b ON br.book_id = b.book_id " +
                     "WHERE f.pay_status = '未缴纳' ORDER BY f.create_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                FineRecord record = mapFineRecord(rs);
                record.setUsername(rs.getString("username"));
                list.add(record);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询所有未缴罚款失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 根据借阅记录创建罚款并缴纳（用于历史欠费补缴）
     * @return null 成功，否则返回错误信息
     */
    public String createAndPayByRecord(Integer recordId, Long accountId) {
        // 先检查是否已有未缴罚款记录
        String existSql = "SELECT fine_id, pay_status FROM fine_records WHERE record_id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement pstmt = conn.prepareStatement(existSql)) {
                pstmt.setInt(1, recordId);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    if ("未缴纳".equals(rs.getString("pay_status"))) {
                        // 直接缴纳
                        return payFine(rs.getInt("fine_id")) ? null : "缴纳失败";
                    }
                }
            }

            // 没有未缴纳记录，从 borrow_records 读取罚款金额创建新记录
            String borrowSql = "SELECT account_id, fine_amount FROM borrow_records WHERE record_id = ? AND status = '已归还'";
            try (PreparedStatement pstmt = conn.prepareStatement(borrowSql)) {
                pstmt.setInt(1, recordId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next() && rs.getDouble("fine_amount") > 0) {
                    if (rs.getLong("account_id") != accountId) {
                        return "无权操作他人罚款";
                    }
                    double fineAmount = rs.getDouble("fine_amount");
                    // 创建并缴纳
                    String insertSql = "INSERT INTO fine_records (account_id, record_id, fine_amount, fine_reason, pay_status, pay_date) VALUES (?, ?, ?, '逾期罚款', '已缴纳', NOW())";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setLong(1, accountId);
                        insertStmt.setInt(2, recordId);
                        insertStmt.setDouble(3, fineAmount);
                        return insertStmt.executeUpdate() > 0 ? null : "创建罚款记录失败";
                    }
                } else {
                    return "该借阅记录无逾期罚款";
                }
            }
        } catch (SQLException e) {
            LOGGER.warning("创建并缴纳罚款失败: " + e.getMessage());
            return "系统错误";
        }
    }

    /**
     * 检查用户是否有未缴罚款
     */
    public boolean hasUnpaidFines(Long accountId) {
        return getUnpaidFineAmount(accountId) > 0;
    }

    /**
     * 获取所有罚款记录（管理员）
     */
    public List<FineRecord> getAllFines() {
        List<FineRecord> list = new ArrayList<>();
        String sql = "SELECT f.*, u.username, b.title as book_title FROM fine_records f " +
                     "LEFT JOIN users u ON f.account_id = u.account_id " +
                     "LEFT JOIN borrow_records br ON f.record_id = br.record_id " +
                     "LEFT JOIN books b ON br.book_id = b.book_id " +
                     "ORDER BY f.create_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                FineRecord record = mapFineRecord(rs);
                record.setUsername(rs.getString("username"));
                list.add(record);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询所有罚款失败: " + e.getMessage());
        }
        return list;
    }
    
    private FineRecord mapFineRecord(ResultSet rs) throws SQLException {
        FineRecord f = new FineRecord();
        f.setFineId(rs.getInt("fine_id"));
        f.setAccountId(rs.getLong("account_id"));
        f.setRecordId(rs.getInt("record_id"));
        f.setFineAmount(rs.getDouble("fine_amount"));
        f.setFineReason(rs.getString("fine_reason"));
        f.setCreateDate(rs.getTimestamp("create_date"));
        f.setPayStatus(rs.getString("pay_status"));
        f.setPayDate(rs.getTimestamp("pay_date"));
        try { f.setBookTitle(rs.getString("book_title")); } catch (SQLException ignored) {}
        return f;
    }
}
