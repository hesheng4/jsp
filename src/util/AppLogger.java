package util;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * 统一日志工具类
 * 封装java.util.logging，配置根Logger使所有子Logger继承格式
 */
public class AppLogger {

    private static volatile boolean configured = false;

    /**
     * 获取指定类的Logger
     */
    public static Logger getLogger(Class<?> clazz) {
        initRootLogger();
        return Logger.getLogger(clazz.getSimpleName());
    }

    private static synchronized void initRootLogger() {
        if (configured) return;

        Logger rootLogger = Logger.getLogger("");
        // 移除默认的ConsoleHandler
        for (java.util.logging.Handler h : rootLogger.getHandlers()) {
            rootLogger.removeHandler(h);
        }

        // 添加自定义格式的ConsoleHandler
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        handler.setFormatter(new CompactFormatter());
        rootLogger.addHandler(handler);
        rootLogger.setLevel(Level.ALL);

        configured = true;
    }

    /**
     * 紧凑格式：[时间] [级别] 类名 - 消息
     */
    private static class CompactFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            return String.format("[%1$tT] [%2$-7s] %3$s - %4$s%n",
                    record.getMillis(),
                    record.getLevel().getName(),
                    record.getLoggerName(),
                    record.getMessage());
        }
    }
}
