package listener;

import database.DatabaseConnection;
import util.DatabaseInitializer;
import util.JpaUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 应用初始化监听器
 * 在应用启动时初始化数据库
 */
@WebListener
public class AppInitListener implements ServletContextListener {
    private static final Logger LOGGER = AppLogger.getLogger(AppInitListener.class);

    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("========================================");
        LOGGER.info("图书借阅管理系统 - 正在启动...");
        LOGGER.info("========================================");
        
        try {
            // 初始化数据库
            DatabaseConnection.initializeDatabase();
            LOGGER.info("数据库连接初始化成功");
            
            // 初始化扩展表
            DatabaseInitializer.initializeExtendedTables();
            LOGGER.info("数据库扩展表初始化成功");
            
            LOGGER.info("========================================");
            LOGGER.info("系统启动完成！");
            LOGGER.info("========================================");
            
        } catch (Exception e) {
            LOGGER.warning("系统启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("图书借阅管理系统 - 正在关闭...");
        JpaUtil.close();
        DatabaseConnection.closeConnection();
        LOGGER.info("数据库连接已关闭");
    }
}
