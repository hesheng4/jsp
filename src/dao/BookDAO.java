package dao;

import database.DatabaseConnection;
import model.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 图书数据访问对象
 */
public class BookDAO {
    private static final Logger LOGGER = AppLogger.getLogger(BookDAO.class);

    
    /**
     * 添加图书
     */
    public boolean addBook(Book book) {
        String sql = "INSERT INTO books (title, author, isbn, category_id, total_copies, " +
                     "available_copies, publisher, publish_date, add_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setInt(4, book.getCategoryId());
            pstmt.setInt(5, book.getTotalCopies());
            pstmt.setInt(6, book.getAvailableCopies());
            pstmt.setString(7, book.getPublisher());
            pstmt.setString(8, book.getPublishDate());
            pstmt.setString(9, book.getAddDate());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            LOGGER.warning("添加图书失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除图书
     */
    public boolean deleteBook(Integer bookId) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bookId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            LOGGER.warning("删除图书失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 更新图书信息
     */
    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, category_id = ?, " +
                     "total_copies = ?, available_copies = ?, publisher = ?, publish_date = ? " +
                     "WHERE book_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, book.getTitle());
            pstmt.setString(2, book.getAuthor());
            pstmt.setString(3, book.getIsbn());
            pstmt.setInt(4, book.getCategoryId());
            pstmt.setInt(5, book.getTotalCopies());
            pstmt.setInt(6, book.getAvailableCopies());
            pstmt.setString(7, book.getPublisher());
            pstmt.setString(8, book.getPublishDate());
            pstmt.setInt(9, book.getBookId());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            LOGGER.warning("更新图书失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 根据ID查询图书
     */
    public Book getBookById(Integer bookId) {
        String sql = "SELECT b.*, c.category_name FROM books b " +
                     "LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "WHERE b.book_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Book book = new Book();
                book.setBookId(rs.getInt("book_id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setIsbn(rs.getString("isbn"));
                book.setCategoryId(rs.getInt("category_id"));
                book.setCategoryName(rs.getString("category_name"));
                book.setTotalCopies(rs.getInt("total_copies"));
                book.setAvailableCopies(rs.getInt("available_copies"));
                book.setPublisher(rs.getString("publisher"));
                book.setPublishDate(rs.getString("publish_date"));
                book.setAddDate(rs.getString("add_date"));
                return book;
            }
            
        } catch (SQLException e) {
            LOGGER.warning("查询图书失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 查询所有图书
     */
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.*, c.category_name FROM books b " +
                     "LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "ORDER BY b.book_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                Book book = new Book();
                book.setBookId(rs.getInt("book_id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setIsbn(rs.getString("isbn"));
                book.setCategoryId(rs.getInt("category_id"));
                book.setCategoryName(rs.getString("category_name"));
                book.setTotalCopies(rs.getInt("total_copies"));
                book.setAvailableCopies(rs.getInt("available_copies"));
                book.setPublisher(rs.getString("publisher"));
                book.setPublishDate(rs.getString("publish_date"));
                book.setAddDate(rs.getString("add_date"));
                books.add(book);
            }
            
        } catch (SQLException e) {
            LOGGER.warning("查询所有图书失败: " + e.getMessage());
        }
        
        return books;
    }
    
    /**
     * 根据分类查询图书
     */
    public List<Book> getBooksByCategory(Integer categoryId) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.*, c.category_name FROM books b " +
                     "LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "WHERE b.category_id = ? ORDER BY b.book_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Book book = new Book();
                book.setBookId(rs.getInt("book_id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setIsbn(rs.getString("isbn"));
                book.setCategoryId(rs.getInt("category_id"));
                book.setCategoryName(rs.getString("category_name"));
                book.setTotalCopies(rs.getInt("total_copies"));
                book.setAvailableCopies(rs.getInt("available_copies"));
                book.setPublisher(rs.getString("publisher"));
                book.setPublishDate(rs.getString("publish_date"));
                book.setAddDate(rs.getString("add_date"));
                books.add(book);
            }
            
        } catch (SQLException e) {
            LOGGER.warning("按分类查询图书失败: " + e.getMessage());
        }
        
        return books;
    }
    
    /**
     * 搜索图书（按标题或作者）
     */
    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT b.*, c.category_name FROM books b " +
                     "LEFT JOIN categories c ON b.category_id = c.category_id " +
                     "WHERE b.title LIKE ? OR b.author LIKE ? ORDER BY b.book_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Book book = new Book();
                book.setBookId(rs.getInt("book_id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setIsbn(rs.getString("isbn"));
                book.setCategoryId(rs.getInt("category_id"));
                book.setCategoryName(rs.getString("category_name"));
                book.setTotalCopies(rs.getInt("total_copies"));
                book.setAvailableCopies(rs.getInt("available_copies"));
                book.setPublisher(rs.getString("publisher"));
                book.setPublishDate(rs.getString("publish_date"));
                book.setAddDate(rs.getString("add_date"));
                books.add(book);
            }
            
        } catch (SQLException e) {
            LOGGER.warning("搜索图书失败: " + e.getMessage());
        }
        
        return books;
    }
    
    /**
     * 高级搜索（多条件组合查询）
     */
    public List<Book> advancedSearch(String title, String author, String publisher, String isbn, Integer categoryId) {
        List<Book> books = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT b.*, c.category_name FROM books b " +
                "LEFT JOIN categories c ON b.category_id = c.category_id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (title != null && !title.trim().isEmpty()) {
            sql.append(" AND b.title LIKE ?");
            params.add("%" + title.trim() + "%");
        }
        if (author != null && !author.trim().isEmpty()) {
            sql.append(" AND b.author LIKE ?");
            params.add("%" + author.trim() + "%");
        }
        if (publisher != null && !publisher.trim().isEmpty()) {
            sql.append(" AND b.publisher LIKE ?");
            params.add("%" + publisher.trim() + "%");
        }
        if (isbn != null && !isbn.trim().isEmpty()) {
            sql.append(" AND b.isbn LIKE ?");
            params.add("%" + isbn.trim() + "%");
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND b.category_id = ?");
            params.add(categoryId);
        }
        
        sql.append(" ORDER BY b.book_id");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) param);
                }
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Book book = new Book();
                book.setBookId(rs.getInt("book_id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setIsbn(rs.getString("isbn"));
                book.setCategoryId(rs.getInt("category_id"));
                book.setCategoryName(rs.getString("category_name"));
                book.setTotalCopies(rs.getInt("total_copies"));
                book.setAvailableCopies(rs.getInt("available_copies"));
                book.setPublisher(rs.getString("publisher"));
                book.setPublishDate(rs.getString("publish_date"));
                book.setAddDate(rs.getString("add_date"));
                books.add(book);
            }
            
        } catch (SQLException e) {
            LOGGER.warning("高级搜索失败: " + e.getMessage());
        }
        
        return books;
    }
    
    /**
     * 获取图书借阅统计信息
     */
    public BookStatistics getBookStatistics(Integer bookId) {
        BookStatistics stats = new BookStatistics();
        stats.setBookId(bookId);
        
        // 查询总借阅次数
        String borrowCountSql = "SELECT COUNT(*) as total_count FROM borrow_records WHERE book_id = ?";
        // 查询当前是否被借阅
        String currentBorrowedSql = "SELECT COUNT(*) as current_count FROM borrow_records WHERE book_id = ? AND status = '借阅中'";
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            // 查询总借阅次数
            try (PreparedStatement pstmt = conn.prepareStatement(borrowCountSql)) {
                pstmt.setInt(1, bookId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    stats.setBorrowCount(rs.getInt("total_count"));
                }
            }
            
            // 查询当前是否被借阅
            try (PreparedStatement pstmt = conn.prepareStatement(currentBorrowedSql)) {
                pstmt.setInt(1, bookId);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    stats.setCurrentlyBorrowed(rs.getInt("current_count") > 0);
                }
            }
            
            return stats;
            
        } catch (SQLException e) {
            LOGGER.warning("查询图书统计失败: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * 图书统计信息内部类
     */
    public static class BookStatistics {
        private Integer bookId;
        private Integer borrowCount; // 借阅次数
        private boolean currentlyBorrowed; // 是否正在被借阅
        
        public Integer getBookId() {
            return bookId;
        }
        
        public void setBookId(Integer bookId) {
            this.bookId = bookId;
        }
        
        public Integer getBorrowCount() {
            return borrowCount;
        }
        
        public void setBorrowCount(Integer borrowCount) {
            this.borrowCount = borrowCount;
        }
        
        public boolean isCurrentlyBorrowed() {
            return currentlyBorrowed;
        }
        
        public void setCurrentlyBorrowed(boolean currentlyBorrowed) {
            this.currentlyBorrowed = currentlyBorrowed;
        }
    }
}

