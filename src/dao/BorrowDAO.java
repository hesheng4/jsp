package dao;

import database.DatabaseConnection;
import model.BorrowRecord;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 借阅记录数据访问对象
 */
public class BorrowDAO {
    private static final Logger LOGGER = AppLogger.getLogger(BorrowDAO.class);

    
    /**
     * 借阅图书
     */
    public boolean borrowBook(Long accountId, Integer bookId) {
        // 检查图书是否可借
        String checkSql = "SELECT available_copies FROM books WHERE book_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 检查可借数量
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, bookId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (!rs.next() || rs.getInt("available_copies") <= 0) {
                    return false; // 图书不存在或不可借
                }
            }
            
            // 减少可借数量
            String updateSql = "UPDATE books SET available_copies = available_copies - 1 WHERE book_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, bookId);
                updateStmt.executeUpdate();
            }
            
            // 插入借阅记录（借阅期限31天）
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_MONTH, 31);
            Date dueDate = new Date(cal.getTimeInMillis());
            
            String insertSql = "INSERT INTO borrow_records (account_id, book_id, borrow_date, due_date, status, renew_count, fine_amount) " +
                              "VALUES (?, ?, ?, ?, '借阅中', 0, 0.00)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setLong(1, accountId);
                insertStmt.setInt(2, bookId);
                insertStmt.setDate(3, new Date(System.currentTimeMillis()));
                insertStmt.setDate(4, dueDate);
                return insertStmt.executeUpdate() > 0;
            }
            
        } catch (SQLException e) {
            LOGGER.warning("借阅图书失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 归还图书（逾期罚款由数据库触发器自动计算）
     */
    public boolean returnBook(Integer recordId) {
        // 触发器 trg_calculate_fine 自动算罚款
        // 触发器 trg_increase_available 自动增加可借数
        // 触发器 trg_create_fine_record 自动写入罚款记录
        String sql = "UPDATE borrow_records SET return_date = CURDATE(), status = '已归还' WHERE record_id = ? AND status = '借阅中'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recordId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("归还图书失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 续借图书（每次续借31天，每本书只能续借一次）
     */
    public boolean renewBook(Integer recordId) {
        String checkSql = "SELECT renew_count, due_date FROM borrow_records WHERE record_id = ? AND status = '借阅中'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setInt(1, recordId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (!rs.next()) {
                return false; // 记录不存在或已归还
            }
            
            int renewCount = rs.getInt("renew_count");
            if (renewCount >= 1) {
                return false; // 已经续借过，不能再续借
            }
            
            // 续借31天
            Date currentDueDate = rs.getDate("due_date");
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(currentDueDate);
            cal.add(java.util.Calendar.DAY_OF_MONTH, 31);
            Date newDueDate = new Date(cal.getTimeInMillis());
            
            String updateSql = "UPDATE borrow_records SET due_date = ?, renew_count = renew_count + 1 WHERE record_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setDate(1, newDueDate);
                updateStmt.setInt(2, recordId);
                return updateStmt.executeUpdate() > 0;
            }
            
        } catch (SQLException e) {
            LOGGER.warning("续借图书失败: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * 根据记录ID获取借阅的图书ID
     */
    public Integer getBorrowBookId(Integer recordId) {
        String sql = "SELECT book_id FROM borrow_records WHERE record_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recordId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("book_id");
            }
        } catch (SQLException e) {
            LOGGER.warning("查询借阅图书ID失败: " + e.getMessage());
        }
        return null;
    }

    private void mapRecord(BorrowRecord record, ResultSet rs) throws SQLException {
        record.setRecordId(rs.getInt("record_id"));
        record.setAccountId(rs.getLong("account_id"));
        record.setBookId(rs.getInt("book_id"));
        record.setBookTitle(rs.getString("book_title"));
        record.setBorrowDate(rs.getDate("borrow_date"));
        record.setDueDate(rs.getDate("due_date"));
        record.setReturnDate(rs.getDate("return_date"));
        record.setStatus(rs.getString("status"));
        record.setRenewCount(rs.getInt("renew_count"));
        record.setFineAmount(rs.getDouble("fine_amount"));
        try { record.setAccountName(rs.getString("account_name")); } catch (SQLException ignored) {}
    }

    /**
     * 获取用户所有借阅记录（含已归还）
     */
    public List<BorrowRecord> getUserBorrowRecords(Long accountId) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT br.*, b.title as book_title FROM borrow_records br " +
                     "LEFT JOIN books b ON br.book_id = b.book_id " +
                     "WHERE br.account_id = ? ORDER BY br.record_id DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                BorrowRecord record = new BorrowRecord();
                mapRecord(record, rs);
                records.add(record);
            }

        } catch (SQLException e) {
            LOGGER.warning("查询借阅记录失败: " + e.getMessage());
        }

        return records;
    }

    /**
     * 获取用户正在借阅的图书
     */
    public List<BorrowRecord> getUserActiveBorrows(Long accountId) {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT br.*, b.title as book_title FROM borrow_records br " +
                     "LEFT JOIN books b ON br.book_id = b.book_id " +
                     "WHERE br.account_id = ? AND br.status = '借阅中' ORDER BY br.record_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                BorrowRecord record = new BorrowRecord();
                mapRecord(record, rs);
                records.add(record);
            }

        } catch (SQLException e) {
            LOGGER.warning("查询借阅记录失败: " + e.getMessage());
        }

        return records;
    }

    /**
     * 获取所有借阅记录（管理员用，显示用户名）
     */
    public List<BorrowRecord> getAllBorrowRecords() {
        List<BorrowRecord> records = new ArrayList<>();
        String sql = "SELECT br.*, b.title as book_title, u.username as account_name " +
                     "FROM borrow_records br " +
                     "LEFT JOIN books b ON br.book_id = b.book_id " +
                     "LEFT JOIN users u ON br.account_id = u.account_id " +
                     "ORDER BY br.record_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                BorrowRecord record = new BorrowRecord();
                mapRecord(record, rs);
                records.add(record);
            }

        } catch (SQLException e) {
            LOGGER.warning("查询所有借阅记录失败: " + e.getMessage());
        }

        return records;
    }
}

