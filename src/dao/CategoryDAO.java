package dao;

import database.DatabaseConnection;
import model.Category;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 分类数据访问对象
 */
public class CategoryDAO {
    private static final Logger LOGGER = AppLogger.getLogger(CategoryDAO.class);

    
    /**
     * 获取所有分类
     */
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY category_id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                categories.add(mapCategory(rs));
            }
            
        } catch (SQLException e) {
            LOGGER.warning("查询分类失败: " + e.getMessage());
        }
        
        return categories;
    }
    
    /**
     * 根据ID获取分类
     */
    public Category getCategoryById(Integer categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, categoryId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapCategory(rs);
            }
            
        } catch (SQLException e) {
            LOGGER.warning("查询分类失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 添加分类
     */
    public boolean addCategory(Category category) {
        String sql = "INSERT INTO categories (category_name, description, parent_id, level) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getCategoryName());
            pstmt.setString(2, category.getDescription());
            if (category.getParentId() != null) {
                pstmt.setInt(3, category.getParentId());
                pstmt.setInt(4, category.getLevel() != null ? category.getLevel() : 2);
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
                pstmt.setInt(4, 1);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("添加分类失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 更新分类
     */
    public boolean updateCategory(Category category) {
        String sql = "UPDATE categories SET category_name = ?, description = ?, parent_id = ?, level = ? WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getCategoryName());
            pstmt.setString(2, category.getDescription());
            if (category.getParentId() != null) {
                pstmt.setInt(3, category.getParentId());
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            pstmt.setInt(4, category.getLevel() != null ? category.getLevel() : 1);
            pstmt.setInt(5, category.getCategoryId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.warning("更新分类失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 删除分类（需要先检查是否有图书使用该分类）
     */
    public boolean deleteCategory(Integer categoryId) {
        // 检查是否有图书使用该分类
        String checkSql = "SELECT COUNT(*) FROM books WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, categoryId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // 有图书使用该分类，不能删除
                }
            }
            // 检查是否有子分类
            String checkChildSql = "SELECT COUNT(*) FROM categories WHERE parent_id = ?";
            try (PreparedStatement checkChildStmt = conn.prepareStatement(checkChildSql)) {
                checkChildStmt.setInt(1, categoryId);
                ResultSet rs = checkChildStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // 有子分类，不能删除
                }
            }
            String sql = "DELETE FROM categories WHERE category_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, categoryId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.warning("删除分类失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取子分类
     */
    public List<Category> getChildCategories(Integer parentId) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE parent_id = ? ORDER BY category_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, parentId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                categories.add(mapCategory(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("查询子分类失败: " + e.getMessage());
        }
        return categories;
    }
    
    /**
     * 获取顶级分类
     */
    public List<Category> getRootCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT * FROM categories WHERE parent_id IS NULL ORDER BY category_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                categories.add(mapCategory(rs));
            }
        } catch (SQLException e) {
            LOGGER.warning("查询顶级分类失败: " + e.getMessage());
        }
        return categories;
    }
    
    /**
     * 获取分类树（递归获取所有子分类）
     */
    public List<Category> getCategoryTree() {
        List<Category> rootCategories = getRootCategories();
        for (Category root : rootCategories) {
            loadChildren(root);
        }
        return rootCategories;
    }
    
    private void loadChildren(Category parent) {
        List<Category> children = getChildCategories(parent.getCategoryId());
        parent.setChildren(children);
        for (Category child : children) {
            loadChildren(child);
        }
    }
    
    private Category mapCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setCategoryName(rs.getString("category_name"));
        category.setDescription(rs.getString("description"));
        try {
            int parentId = rs.getInt("parent_id");
            if (!rs.wasNull()) {
                category.setParentId(parentId);
            }
            category.setLevel(rs.getInt("level"));
        } catch (SQLException ignored) {}
        return category;
    }
}

