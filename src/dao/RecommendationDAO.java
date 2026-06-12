package dao;

import database.DatabaseConnection;
import model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 图书推荐数据访问对象
 */
public class RecommendationDAO {
    private static final Logger LOGGER = AppLogger.getLogger(RecommendationDAO.class);

    
    /**
     * 基于借阅历史推荐相似图书（同分类的热门图书）
     */
    public List<Book> getRecommendationsByHistory(Long accountId, int limit) {
        List<Book> list = new ArrayList<>();
        // 获取用户借阅过的分类，推荐同分类中用户未借阅过的热门图书
        String sql = "SELECT b.*, c.category_name, COUNT(br2.record_id) as popularity " +
                     "FROM books b " +
                     "LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "LEFT JOIN borrow_records br2 ON b.book_id = br2.book_id " +
                     "WHERE b.category_id IN (" +
                     "    SELECT DISTINCT b2.category_id FROM borrow_records br " +
                     "    JOIN books b2 ON br.book_id = b2.book_id WHERE br.account_id = ?" +
                     ") AND b.book_id NOT IN (" +
                     "    SELECT book_id FROM borrow_records WHERE account_id = ?" +
                     ") AND b.available_copies > 0 " +
                     "GROUP BY b.book_id " +
                     "ORDER BY popularity DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, accountId);
            pstmt.setLong(2, accountId);
            pstmt.setInt(3, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapBook(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("获取推荐图书失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 基于分类推荐热门图书
     */
    public List<Book> getPopularByCategory(Integer categoryId, int limit) {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT b.*, c.category_name, COUNT(br.record_id) as popularity " +
                     "FROM books b " +
                     "LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "LEFT JOIN borrow_records br ON b.book_id = br.book_id " +
                     "WHERE b.category_id = ? " +
                     "GROUP BY b.book_id " +
                     "ORDER BY popularity DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            pstmt.setInt(2, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapBook(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("获取分类热门图书失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 获取高评分图书推荐
     */
    public List<Book> getHighRatedBooks(int limit) {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT b.*, c.category_name, AVG(r.rating) as avg_rating " +
                     "FROM books b " +
                     "LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "JOIN book_reviews r ON b.book_id = r.book_id " +
                     "WHERE r.is_visible = 1 " +
                     "GROUP BY b.book_id HAVING COUNT(r.review_id) >= 1 " +
                     "ORDER BY avg_rating DESC, COUNT(r.review_id) DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapBook(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("获取高评分图书失败: " + e.getMessage());
        }
        return list;
    }
    
    /**
     * 获取新书推荐
     */
    public List<Book> getNewBooks(int limit) {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT b.*, c.category_name FROM books b " +
                     "LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "ORDER BY b.add_date DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(mapBook(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("获取新书失败: " + e.getMessage());
        }
        return list;
    }
    
    private Book mapBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookId(rs.getInt("book_id"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setCategoryId(rs.getInt("category_id"));
        book.setTotalCopies(rs.getInt("total_copies"));
        book.setAvailableCopies(rs.getInt("available_copies"));
        book.setPublisher(rs.getString("publisher"));
        book.setPublishDate(rs.getString("publish_date"));
        book.setAddDate(rs.getString("add_date"));
        try { book.setCategoryName(rs.getString("category_name")); } catch (SQLException ignored) {}
        return book;
    }
}
