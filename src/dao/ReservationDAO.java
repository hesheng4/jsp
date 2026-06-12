package dao;

import database.DatabaseConnection;
import model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 图书预约数据访问对象
 */
public class ReservationDAO {
    private static final Logger LOGGER = AppLogger.getLogger(ReservationDAO.class);

    
    /**
     * 创建预约（仅当图书不可借时）
     */
    public boolean createReservation(Long accountId, Integer bookId) {
        // 检查图书是否已无可借
        String checkSql = "SELECT available_copies FROM books WHERE book_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, bookId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt("available_copies") > 0) {
                    return false; // 还有可借图书，无需预约
                }
            }
            
            // 检查是否已预约
            String existSql = "SELECT COUNT(*) FROM reservations WHERE account_id = ? AND book_id = ? AND status = '等待中'";
            try (PreparedStatement existStmt = conn.prepareStatement(existSql)) {
                existStmt.setLong(1, accountId);
                existStmt.setInt(2, bookId);
                ResultSet rs = existStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // 已预约
                }
            }
            
            // 创建预约
            String sql = "INSERT INTO reservations (account_id, book_id, status) VALUES (?, ?, '等待中')";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, accountId);
                pstmt.setInt(2, bookId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.warning("创建预约失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 取消预约
     */
    public boolean cancelReservation(Integer reservationId, Long accountId) {
        String sql = "UPDATE reservations SET status = '已取消' WHERE reservation_id = ? AND account_id = ? AND status IN ('等待中', '已通知')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            pstmt.setLong(2, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("取消预约失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 完成预约（用户借到书后）
     */
    public boolean completeReservation(Long accountId, Integer bookId) {
        String sql = "UPDATE reservations SET status = '已完成' WHERE account_id = ? AND book_id = ? AND status = '已通知'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            pstmt.setInt(2, bookId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("完成预约失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 通知预约用户（图书归还后调用）
     */
    public boolean notifyReservation(Integer reservationId) {
        // 设置通知时间和过期时间（3天后过期）
        String sql = "UPDATE reservations SET status = '已通知', notify_date = NOW(), " +
                     "expire_date = DATE_ADD(NOW(), INTERVAL 3 DAY) WHERE reservation_id = ? AND status = '等待中'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("通知预约失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 通知图书的第一个预约用户
     */
    public boolean notifyFirstReservation(Integer bookId) {
        List<Reservation> queue = getBookReservations(bookId);
        if (!queue.isEmpty()) {
            return notifyReservation(queue.get(0).getReservationId());
        }
        return false;
    }
    
    /**
     * 获取用户的预约列表
     */
    public List<Reservation> getUserReservations(Long accountId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, b.title as book_title FROM reservations r " +
                     "LEFT JOIN books b ON r.book_id = b.book_id " +
                     "WHERE r.account_id = ? ORDER BY r.reservation_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapReservation(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("查询预约失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 获取用户已通知的预约（可以去借书了）
     */
    public List<Reservation> getNotifiedReservations(Long accountId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, b.title as book_title FROM reservations r " +
                     "LEFT JOIN books b ON r.book_id = b.book_id " +
                     "WHERE r.account_id = ? AND r.status = '已通知' AND r.expire_date > NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapReservation(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("查询已通知预约失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 获取图书的预约队列
     */
    public List<Reservation> getBookReservations(Integer bookId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, u.username FROM reservations r " +
                     "LEFT JOIN users u ON r.account_id = u.account_id " +
                     "WHERE r.book_id = ? AND r.status = '等待中' ORDER BY r.reservation_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Reservation r = mapReservation(rs);
                r.setUsername(rs.getString("username"));
                list.add(r);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询图书预约队列失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 获取所有预约记录（管理员用，显示用户名）
     */
    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.*, b.title as book_title, u.username FROM reservations r " +
                     "LEFT JOIN books b ON r.book_id = b.book_id " +
                     "LEFT JOIN users u ON r.account_id = u.account_id " +
                     "ORDER BY r.reservation_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapReservation(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("查询所有预约记录失败: " + e.getMessage());
        }
        return list;
    }

    /**
     * 处理过期预约
     */
    public int expireOverdueReservations() {
        String sql = "UPDATE reservations SET status = '已取消' WHERE status = '已通知' AND expire_date < NOW()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.warning("处理过期预约失败: " + e.getMessage());
            return 0;
        }
    }
    
    private Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setReservationId(rs.getInt("reservation_id"));
        r.setAccountId(rs.getLong("account_id"));
        r.setBookId(rs.getInt("book_id"));
        r.setReservationDate(rs.getTimestamp("reservation_date"));
        r.setStatus(rs.getString("status"));
        r.setNotifyDate(rs.getTimestamp("notify_date"));
        r.setExpireDate(rs.getTimestamp("expire_date"));
        try { r.setBookTitle(rs.getString("book_title")); } catch (SQLException ignored) {}
        try { r.setUsername(rs.getString("username")); } catch (SQLException ignored) {}
        return r;
    }
}
