package util;

import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 数据库初始化工具
 * 用于创建扩展表和字段
 */
public class DatabaseInitializer {
    private static final Logger LOGGER = AppLogger.getLogger(DatabaseInitializer.class);

    
    /**
     * 检查列是否存在
     */
    private static boolean columnExists(Connection conn, String tableName, String columnName) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getColumns(null, null, tableName, columnName);
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查表是否存在
     */
    private static boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(null, null, tableName, null);
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 安全添加列（忽略已存在错误）
     */
    private static void safeAddColumn(Statement stmt, String table, String column, String definition) {
        try {
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            LOGGER.info("✓ 添加字段: " + table + "." + column);
        } catch (Exception e) {
            // 忽略"字段已存在"错误
            if (!e.getMessage().toLowerCase().contains("duplicate")) {
                LOGGER.warning("添加字段 " + table + "." + column + " 失败: " + e.getMessage());
            }
        }
    }
    
    public static void initializeExtendedTables() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            LOGGER.info("开始初始化扩展表...");
            
            // 1. 扩展用户表 - 使用安全添加方式
            safeAddColumn(stmt, "users", "email", "VARCHAR(100)");
            safeAddColumn(stmt, "users", "address", "VARCHAR(255)");
            safeAddColumn(stmt, "users", "borrow_limit", "INT DEFAULT 5");
            safeAddColumn(stmt, "users", "is_blacklisted", "TINYINT DEFAULT 0");
            safeAddColumn(stmt, "users", "blacklist_reason", "VARCHAR(255)");
            safeAddColumn(stmt, "users", "blacklist_date", "DATETIME");
            safeAddColumn(stmt, "users", "is_root", "TINYINT DEFAULT 0");
            safeAddColumn(stmt, "users", "is_enabled", "TINYINT DEFAULT 1");
            safeAddColumn(stmt, "users", "authorized_by", "BIGINT");
            safeAddColumn(stmt, "users", "authorized_date", "DATETIME");
            
            // 2. 扩展分类表
            safeAddColumn(stmt, "categories", "parent_id", "INT");
            safeAddColumn(stmt, "categories", "level", "INT DEFAULT 1");
            
            // 3. 创建预约表
            if (!tableExists(conn, "reservations")) {
                stmt.execute("CREATE TABLE reservations (" +
                    "reservation_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "account_id BIGINT NOT NULL," +
                    "book_id INT NOT NULL," +
                    "reservation_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "status VARCHAR(20) DEFAULT '等待中'," +
                    "notify_date DATETIME," +
                    "expire_date DATETIME)");
                LOGGER.info("✓ 创建表: reservations");
            }
            
            // 4. 创建评论表
            if (!tableExists(conn, "book_reviews")) {
                stmt.execute("CREATE TABLE book_reviews (" +
                    "review_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "account_id BIGINT NOT NULL," +
                    "book_id INT NOT NULL," +
                    "rating INT," +
                    "review_content TEXT," +
                    "review_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "is_visible TINYINT DEFAULT 1," +
                    "UNIQUE KEY unique_user_book_review (account_id, book_id))");
                LOGGER.info("✓ 创建表: book_reviews");
            }
            
            // 5. 创建罚款表
            if (!tableExists(conn, "fine_records")) {
                stmt.execute("CREATE TABLE fine_records (" +
                    "fine_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "account_id BIGINT NOT NULL," +
                    "record_id INT NOT NULL," +
                    "fine_amount DECIMAL(10,2) NOT NULL," +
                    "fine_reason VARCHAR(255)," +
                    "create_date DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "pay_status VARCHAR(20) DEFAULT '未缴纳'," +
                    "pay_date DATETIME)");
                LOGGER.info("✓ 创建表: fine_records");
            }
            
            // 6. 创建授权日志表
            if (!tableExists(conn, "authorization_logs")) {
                stmt.execute("CREATE TABLE authorization_logs (" +
                    "log_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "operator_id BIGINT NOT NULL," +
                    "target_id BIGINT NOT NULL," +
                    "action_type VARCHAR(20) NOT NULL," +
                    "action_detail VARCHAR(255)," +
                    "action_time DATETIME DEFAULT CURRENT_TIMESTAMP)");
                LOGGER.info("✓ 创建表: authorization_logs");
            }
            
            // 7. 创建操作日志表
            if (!tableExists(conn, "operation_logs")) {
                stmt.execute("CREATE TABLE operation_logs (" +
                    "log_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "account_id BIGINT NOT NULL," +
                    "username VARCHAR(50)," +
                    "operation_type VARCHAR(50) NOT NULL," +
                    "operation_detail VARCHAR(500)," +
                    "operation_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "ip_address VARCHAR(50))");
                LOGGER.info("✓ 创建表: operation_logs");
            }

            // 8. 更新现有管理员为已启用
            stmt.execute("UPDATE users SET is_enabled = 1 WHERE is_admin = 1");
            LOGGER.info("✓ 更新管理员状态");
            

            

        } catch (Exception e) {
            LOGGER.warning("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        initializeExtendedTables();
    }
}
