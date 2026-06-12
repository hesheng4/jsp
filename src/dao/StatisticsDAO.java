package dao;

import database.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 借阅统计数据访问对象
 */
public class StatisticsDAO {
    private static final Logger LOGGER = AppLogger.getLogger(StatisticsDAO.class);

    
    /**
     * 热门图书排行榜（按借阅次数）
     */
    public List<Map<String, Object>> getPopularBooks(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT b.book_id, b.title, b.author, COUNT(br.record_id) as borrow_count " +
                     "FROM books b LEFT JOIN borrow_records br ON b.book_id = br.book_id " +
                     "GROUP BY b.book_id, b.title, b.author " +
                     "ORDER BY borrow_count DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("bookId", rs.getInt("book_id"));
                map.put("title", rs.getString("title"));
                map.put("author", rs.getString("author"));
                map.put("borrowCount", rs.getInt("borrow_count"));
                list.add(map);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询热门图书失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 用户借阅排行
     */
    public List<Map<String, Object>> getTopBorrowers(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT u.account_id, u.username, COUNT(br.record_id) as borrow_count " +
                     "FROM users u LEFT JOIN borrow_records br ON u.account_id = br.account_id " +
                     "GROUP BY u.account_id, u.username " +
                     "ORDER BY borrow_count DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("accountId", rs.getLong("account_id"));
                map.put("username", rs.getString("username"));
                map.put("borrowCount", rs.getInt("borrow_count"));
                list.add(map);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询用户借阅排行失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 分类借阅统计
     */
    public List<Map<String, Object>> getCategoryStatistics() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT c.category_id, c.category_name, COUNT(br.record_id) as borrow_count " +
                     "FROM categories c " +
                     "LEFT JOIN books b ON c.category_id = b.category_id " +
                     "LEFT JOIN borrow_records br ON b.book_id = br.book_id " +
                     "GROUP BY c.category_id, c.category_name " +
                     "ORDER BY borrow_count DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("categoryId", rs.getInt("category_id"));
                map.put("categoryName", rs.getString("category_name"));
                map.put("borrowCount", rs.getInt("borrow_count"));
                list.add(map);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询分类统计失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 月度借阅趋势
     */
    public List<Map<String, Object>> getMonthlyTrend(int year) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT MONTH(borrow_date) as month, COUNT(*) as borrow_count " +
                     "FROM borrow_records WHERE YEAR(borrow_date) = ? " +
                     "GROUP BY MONTH(borrow_date) ORDER BY month";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, year);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("month", rs.getInt("month"));
                map.put("borrowCount", rs.getInt("borrow_count"));
                list.add(map);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询月度趋势失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 年度借阅趋势
     */
    public List<Map<String, Object>> getYearlyTrend() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT YEAR(borrow_date) as year, COUNT(*) as borrow_count " +
                     "FROM borrow_records GROUP BY YEAR(borrow_date) ORDER BY year";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("year", rs.getInt("year"));
                map.put("borrowCount", rs.getInt("borrow_count"));
                list.add(map);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询年度趋势失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 系统总览统计
     */
    public Map<String, Object> getOverviewStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 图书总数（所有图书册数的总和）
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COALESCE(SUM(total_copies), 0) FROM books");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("totalBooks", rs.getInt(1));
            }
            // 图书种类数
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM books");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("bookTypes", rs.getInt(1));
            }
            // 可借图书数量（所有可借册数的总和）
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COALESCE(SUM(available_copies), 0) FROM books");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("availableBooks", rs.getInt(1));
            }
            // 用户总数（所有账号）
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM users");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("totalUsers", rs.getInt(1));
            }
            // 借阅中数量
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM borrow_records WHERE status = '借阅中'");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("activeBorrows", rs.getInt(1));
            }
            // 逾期数量
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM borrow_records WHERE status = '借阅中' AND due_date < CURDATE()");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("overdueCount", rs.getInt(1));
            }
            // 今日借阅
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM borrow_records WHERE DATE(borrow_date) = CURDATE()");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("todayBorrows", rs.getInt(1));
            }
            // 未缴罚款总额
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COALESCE(SUM(fine_amount), 0) FROM fine_records WHERE pay_status = '未缴纳'");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("unpaidFines", rs.getDouble(1));
            }
        } catch (SQLException e) {
            LOGGER.warning("查询系统统计失败: " + e.getMessage());
        }
        return stats;
    }
}
