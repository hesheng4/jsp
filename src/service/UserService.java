package service;

import dao.UserDAO;
import model.User;
import java.util.List;

/**
 * 用户业务逻辑层
 * 三层架构中的Service层
 */
public class UserService {
    private UserDAO userDAO = new UserDAO();
    
    /**
     * 用户登录
     */
    public User login(Long accountId, String password) {
        if (accountId == null || password == null || password.isEmpty()) {
            return null;
        }
        return userDAO.login(accountId, password);
    }
    
    /**
     * 用户注册
     */
    public boolean register(Long accountId, String username, String password, String phone) {
        // 验证账号格式（12位数字）
        if (accountId == null || accountId.toString().length() != 12) {
            return false;
        }
        // 验证手机号格式（11位数字）
        if (phone == null || !phone.matches("\\d{11}")) {
            return false;
        }
        // 验证用户名和密码
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        return userDAO.register(accountId, username, password, phone, false);
    }
    
    /**
     * 检查账号是否存在
     */
    public boolean accountExists(Long accountId) {
        return userDAO.accountExists(accountId);
    }
    
    /**
     * 检查用户名是否存在
     */
    public boolean usernameExists(String username) {
        return userDAO.usernameExists(username);
    }
    
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }
    
    /**
     * 根据ID获取用户
     */
    public User getUserById(Long accountId) {
        return userDAO.getUserById(accountId);
    }
    
    /**
     * 删除用户
     */
    public boolean deleteUser(Long accountId) {
        return userDAO.deleteUser(accountId);
    }
    
    /**
     * 修改密码
     */
    public boolean updatePassword(Long accountId, String oldPassword, String newPassword) {
        User user = userDAO.login(accountId, oldPassword);
        if (user == null) {
            return false;
        }
        return userDAO.updatePassword(accountId, newPassword);
    }
    
    /**
     * 更新用户信息
     */
    public String updateUserInfo(Long accountId, String email, String address, String phone) {
        return userDAO.updateUserInfoWithCheck(accountId, email, address, phone);
    }
    
    /**
     * 设置管理员权限
     */
    public boolean setAdmin(Long accountId, boolean isAdmin) {
        return userDAO.setAdmin(accountId, isAdmin);
    }
    
    /**
     * 加入黑名单
     */
    public boolean addToBlacklist(Long accountId, String reason) {
        return userDAO.addToBlacklist(accountId, reason);
    }
    
    /**
     * 移出黑名单
     */
    public boolean removeFromBlacklist(Long accountId) {
        return userDAO.removeFromBlacklist(accountId);
    }
    
    /**
     * 获取黑名单用户
     */
    public List<User> getBlacklistedUsers() {
        return userDAO.getBlacklistedUsers();
    }
    
    /**
     * 验证用户身份（找回密码）
     */
    public boolean verifyUser(Long accountId, String username, String phone) {
        return userDAO.verifyUser(accountId, username, phone);
    }
    
    /**
     * 重置密码
     */
    public boolean resetPassword(Long accountId, String newPassword) {
        return userDAO.updatePassword(accountId, newPassword);
    }
}
