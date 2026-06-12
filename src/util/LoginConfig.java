package util;

import java.io.*;
import java.util.Properties;
import util.AppLogger;
import java.util.logging.Logger;

/**
 * 登录配置工具类 - 用于记住登录信息
 */
public class LoginConfig {
    private static final Logger LOGGER = AppLogger.getLogger(LoginConfig.class);

    private static final String CONFIG_FILE = "login_config.properties";
    
    /**
     * 保存登录信息
     */
    public static void saveLoginInfo(Long accountId, String password, boolean rememberMe) {
        Properties props = new Properties();
        
        if (rememberMe) {
            props.setProperty("accountId", accountId.toString());
            props.setProperty("password", password);
            props.setProperty("rememberMe", "true");
        } else {
            props.setProperty("rememberMe", "false");
            props.remove("accountId");
            props.remove("password");
        }
        
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "Login Configuration");
        } catch (IOException e) {
            LOGGER.warning("保存登录信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 读取登录信息
     */
    public static LoginInfo loadLoginInfo() {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);
        
        if (!file.exists()) {
            return null;
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);
            
            String rememberMe = props.getProperty("rememberMe", "false");
            if ("true".equals(rememberMe)) {
                String accountIdStr = props.getProperty("accountId");
                String password = props.getProperty("password");
                
                if (accountIdStr != null && password != null) {
                    try {
                        Long accountId = Long.parseLong(accountIdStr);
                        return new LoginInfo(accountId, password);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warning("读取登录信息失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 清除登录信息
     */
    public static void clearLoginInfo() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            file.delete();
        }
    }
    
    /**
     * 登录信息内部类
     */
    public static class LoginInfo {
        private Long accountId;
        private String password;
        
        public LoginInfo(Long accountId, String password) {
            this.accountId = accountId;
            this.password = password;
        }
        
        public Long getAccountId() {
            return accountId;
        }
        
        public String getPassword() {
            return password;
        }
    }
}

