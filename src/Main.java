import database.DatabaseConnection;
import util.DatabaseInitializer;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 图书借阅管理系统 - 数据库初始化入口
 * Web应用由Servlet容器自动初始化（AppInitListener），
 * 本类仅用于命令行手动执行数据库初始化。
 */
public class Main {
    private static final Logger LOGGER = AppLogger.getLogger(Main.class);

    public static void main(String[] args) {
        LOGGER.info("========================================");
        LOGGER.info("图书借阅管理系统 - 数据库初始化");
        LOGGER.info("========================================");

        try {
            DatabaseConnection.initializeDatabase();
            LOGGER.info("正在检查数据库扩展字段...");
            DatabaseInitializer.initializeExtendedTables();
            LOGGER.info("数据库初始化完成");
        } catch (Exception e) {
            LOGGER.warning("数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
        }

        LOGGER.info("========================================");
        LOGGER.info("初始化完毕。请通过浏览器访问Web应用。");
        LOGGER.info("========================================");
    }
}
