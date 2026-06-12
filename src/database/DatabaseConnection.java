package database;

import org.apache.commons.dbcp2.BasicDataSource;
import util.AppLogger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * 数据库连接池管理类
 * 使用 Apache DBCP2 连接池替代原生 DriverManager
 */
public class DatabaseConnection {
    private static final Logger LOGGER = AppLogger.getLogger(DatabaseConnection.class);

    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "library_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123580";

    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
            + "?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&allowPublicKeyRetrieval=true";

    private static BasicDataSource dataSource;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("MySQL JDBC驱动加载成功");
        } catch (ClassNotFoundException e) {
            LOGGER.warning("MySQL JDBC驱动未找到！");
            e.printStackTrace();
        }
    }

    private static synchronized void initDataSource() {
        if (dataSource != null) return;
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(DB_URL);
        dataSource.setUsername(DB_USER);
        dataSource.setPassword(DB_PASSWORD);
        // 连接池配置
        dataSource.setInitialSize(5);
        dataSource.setMaxTotal(20);
        dataSource.setMaxIdle(10);
        dataSource.setMinIdle(3);
        dataSource.setMaxWaitMillis(10000);
        dataSource.setValidationQuery("SELECT 1");
        dataSource.setTestOnBorrow(true);
        dataSource.setTestOnReturn(true);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        LOGGER.info("连接池初始化完成: maxTotal=20, initialSize=5");
    }

    /**
     * 获取数据库连接（从连接池）
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initDataSource();
        }
        return dataSource.getConnection();
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            initDataSource();
        }
        return dataSource;
    }

    public static boolean isDriverLoaded() {
        return true;
    }

    private static void createDatabaseIfNotExists() throws SQLException {
        String createDbUrl = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT
                + "?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&useUnicode=true&allowPublicKeyRetrieval=true";

        try (Connection conn = java.sql.DriverManager.getConnection(createDbUrl, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE DATABASE IF NOT EXISTS " + DB_NAME
                    + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            LOGGER.info("数据库 " + DB_NAME + " 创建成功或已存在");
        } catch (SQLException e) {
            if (e.getMessage().contains("Access denied")) {
                LOGGER.warning("MySQL访问被拒绝！请检查 DB_USER/DB_PASSWORD 配置。");
            }
            throw e;
        }
    }

    public static void initializeDatabase() throws SQLException {
        createDatabaseIfNotExists();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "account_id BIGINT PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password VARCHAR(100) NOT NULL, phone VARCHAR(11), is_admin TINYINT NOT NULL DEFAULT 0" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.execute("CREATE TABLE IF NOT EXISTS categories (" +
                    "category_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "category_name VARCHAR(50) NOT NULL UNIQUE, description TEXT" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.execute("CREATE TABLE IF NOT EXISTS books (" +
                    "book_id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(200) NOT NULL, " +
                    "author VARCHAR(100), isbn VARCHAR(50), category_id INT, " +
                    "total_copies INT NOT NULL DEFAULT 1, available_copies INT NOT NULL DEFAULT 1, " +
                    "publisher VARCHAR(100), publish_date VARCHAR(20), add_date VARCHAR(20), " +
                    "FOREIGN KEY (category_id) REFERENCES categories(category_id) ON DELETE SET NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.execute("CREATE TABLE IF NOT EXISTS borrow_records (" +
                    "record_id INT AUTO_INCREMENT PRIMARY KEY, account_id BIGINT NOT NULL, " +
                    "book_id INT NOT NULL, borrow_date DATE NOT NULL, due_date DATE, " +
                    "return_date DATE, status VARCHAR(20) NOT NULL DEFAULT '借阅中', " +
                    "renew_count INT NOT NULL DEFAULT 0, fine_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00, " +
                    "FOREIGN KEY (account_id) REFERENCES users(account_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.execute("CREATE TABLE IF NOT EXISTS operation_logs (" +
                    "log_id INT AUTO_INCREMENT PRIMARY KEY, account_id BIGINT, " +
                    "username VARCHAR(50), operation_type VARCHAR(50) NOT NULL, " +
                    "operation_detail TEXT, operation_time DATETIME NOT NULL, ip_address VARCHAR(50)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // 扩展字段容错
            try { stmt.execute("ALTER TABLE users ADD COLUMN email VARCHAR(100)"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN address VARCHAR(255)"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN borrow_limit INT DEFAULT 5"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN is_blacklisted TINYINT DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN blacklist_reason VARCHAR(255)"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN blacklist_date DATETIME"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN is_root TINYINT DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN is_enabled TINYINT DEFAULT 1"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN authorized_by BIGINT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE users ADD COLUMN authorized_date DATETIME"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE categories ADD COLUMN parent_id INT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE categories ADD COLUMN level INT DEFAULT 1"); } catch (SQLException ignored) {}

            stmt.execute("INSERT IGNORE INTO users (account_id, username, password, is_admin) " +
                    "VALUES (100000000000, 'admin', 'admin123', 1)");
            stmt.execute("INSERT IGNORE INTO categories (category_name, description) VALUES " +
                    "('文学','文学作品类'),('科技','科学技术类'),('历史','历史类'),('教育','教育类'),('艺术','艺术类')");

            LOGGER.info("数据库初始化成功！");
        } catch (SQLException e) {
            LOGGER.warning("数据库初始化失败: " + e.getMessage());
            throw e;
        }
    }

    public static void closeConnection() {
        if (dataSource != null) {
            try { dataSource.close(); } catch (SQLException e) { /* ignore */ }
        }
    }
}
