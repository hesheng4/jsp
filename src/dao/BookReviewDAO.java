package dao;

import database.DatabaseConnection;
import model.BookReview;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 图书评论数据访问对象
 */
public class BookReviewDAO {
    private static final Logger LOGGER = AppLogger.getLogger(BookReviewDAO.class);

    
    /**
     * 添加评论（需要借阅过该书）
     */
    public boolean addReview(Long accountId, Integer bookId, Integer rating, String content) {
        // 检查是否借阅过
        String checkSql = "SELECT COUNT(*) FROM borrow_records WHERE account_id = ? AND book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setLong(1, accountId);
                checkStmt.setInt(2, bookId);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next() || rs.getInt(1) == 0) {
                    return false; // 未借阅过，不能评论
                }
            }
            
            String sql = "INSERT INTO book_reviews (account_id, book_id, rating, review_content) VALUES (?, ?, ?, ?) " +
                         "ON DUPLICATE KEY UPDATE rating = ?, review_content = ?, review_date = NOW()";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setLong(1, accountId);
                pstmt.setInt(2, bookId);
                pstmt.setInt(3, rating);
                pstmt.setString(4, content);
                pstmt.setInt(5, rating);
                pstmt.setString(6, content);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.warning("添加评论失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除评论
     */
    public boolean deleteReview(Integer reviewId, Long accountId) {
        String sql = "DELETE FROM book_reviews WHERE review_id = ? AND account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reviewId);
            pstmt.setLong(2, accountId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("删除评论失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 管理员隐藏/显示评论
     */
    public boolean toggleVisibility(Integer reviewId, boolean visible) {
        String sql = "UPDATE book_reviews SET is_visible = ? WHERE review_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, visible ? 1 : 0);
            pstmt.setInt(2, reviewId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("修改评论可见性失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取图书的所有评论
     */
    public List<BookReview> getBookReviews(Integer bookId) {
        List<BookReview> list = new ArrayList<>();
        String sql = "SELECT r.*, u.username FROM book_reviews r " +
                     "LEFT JOIN users u ON r.account_id = u.account_id " +
                     "WHERE r.book_id = ? AND r.is_visible = 1 ORDER BY r.review_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapReview(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("查询图书评论失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 获取图书平均评分
     */
    public Double getBookAverageRating(Integer bookId) {
        String sql = "SELECT AVG(rating) as avg_rating FROM book_reviews WHERE book_id = ? AND is_visible = 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            LOGGER.warning("查询平均评分失败: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * 获取用户的所有评论
     */
    public List<BookReview> getUserReviews(Long accountId) {
        List<BookReview> list = new ArrayList<>();
        String sql = "SELECT r.*, b.title as book_title FROM book_reviews r " +
                     "LEFT JOIN books b ON r.book_id = b.book_id " +
                     "WHERE r.account_id = ? ORDER BY r.review_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                BookReview review = mapReview(rs);
                review.setBookTitle(rs.getString("book_title"));
                list.add(review);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询用户评论失败: " + e.getMessage());
        }
        return list;
    }
    
    private BookReview mapReview(ResultSet rs) throws SQLException {
        BookReview r = new BookReview();
        r.setReviewId(rs.getInt("review_id"));
        r.setAccountId(rs.getLong("account_id"));
        r.setBookId(rs.getInt("book_id"));
        r.setRating(rs.getInt("rating"));
        r.setReviewContent(rs.getString("review_content"));
        r.setReviewDate(rs.getTimestamp("review_date"));
        r.setVisible(rs.getInt("is_visible") == 1);
        try { r.setUsername(rs.getString("username")); } catch (SQLException ignored) {}
        return r;
    }
    
    /**
     * 获取所有评论（管理员用）
     */
    public List<BookReview> getAllReviews() {
        List<BookReview> list = new ArrayList<>();
        String sql = "SELECT r.*, u.username, b.title as book_title FROM book_reviews r " +
                     "LEFT JOIN users u ON r.account_id = u.account_id " +
                     "LEFT JOIN books b ON r.book_id = b.book_id " +
                     "ORDER BY r.review_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                BookReview review = mapReview(rs);
                review.setBookTitle(rs.getString("book_title"));
                list.add(review);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询所有评论失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 管理员删除评论
     */
    public boolean deleteReviewByAdmin(Integer reviewId) {
        String sql = "DELETE FROM book_reviews WHERE review_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reviewId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("管理员删除评论失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取评论统计数据
     */
    public Map<String, Object> getReviewStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 总评论数
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM book_reviews");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("totalReviews", rs.getInt(1));
            }
            // 可见评论数
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM book_reviews WHERE is_visible = 1");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("visibleReviews", rs.getInt(1));
            }
            // 隐藏评论数
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM book_reviews WHERE is_visible = 0");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("hiddenReviews", rs.getInt(1));
            }
            // 平均评分
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT AVG(rating) FROM book_reviews WHERE is_visible = 1");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("avgRating", rs.getDouble(1));
            }
            // 今日评论数
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM book_reviews WHERE DATE(review_date) = CURDATE()");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("todayReviews", rs.getInt(1));
            }
            // 被评论图书数
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(DISTINCT book_id) FROM book_reviews");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("reviewedBooks", rs.getInt(1));
            }
            // 评论用户数
            try (PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(DISTINCT account_id) FROM book_reviews");
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) stats.put("reviewUsers", rs.getInt(1));
            }
        } catch (SQLException e) {
            LOGGER.warning("查询评论统计失败: " + e.getMessage());
        }
        return stats;
    }
    
    /**
     * 获取评分分布
     */
    public List<Map<String, Object>> getRatingDistribution() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT rating, COUNT(*) as count FROM book_reviews WHERE is_visible = 1 GROUP BY rating ORDER BY rating DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("rating", rs.getInt("rating"));
                map.put("count", rs.getInt("count"));
                list.add(map);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询评分分布失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 获取活跃评论用户排行
     */
    public List<Map<String, Object>> getTopReviewers(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT r.account_id, u.username, COUNT(*) as review_count, AVG(r.rating) as avg_rating " +
                     "FROM book_reviews r LEFT JOIN users u ON r.account_id = u.account_id " +
                     "GROUP BY r.account_id, u.username ORDER BY review_count DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("accountId", rs.getLong("account_id"));
                map.put("username", rs.getString("username"));
                map.put("reviewCount", rs.getInt("review_count"));
                map.put("avgRating", rs.getDouble("avg_rating"));
                list.add(map);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询活跃评论用户失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 获取热评图书排行
     */
    public List<Map<String, Object>> getMostReviewedBooks(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT r.book_id, b.title, COUNT(*) as review_count, AVG(r.rating) as avg_rating " +
                     "FROM book_reviews r LEFT JOIN books b ON r.book_id = b.book_id " +
                     "WHERE r.is_visible = 1 GROUP BY r.book_id, b.title ORDER BY review_count DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("bookId", rs.getInt("book_id"));
                map.put("title", rs.getString("title"));
                map.put("reviewCount", rs.getInt("review_count"));
                map.put("avgRating", rs.getDouble("avg_rating"));
                list.add(map);
            }
        } catch (SQLException e) {
            LOGGER.warning("查询热评图书失败: " + e.getMessage());
        }
        return list;
    }
}
