package model;

import java.sql.Timestamp;

/**
 * 用户模型类
 * 账号使用泛型限制为整型长度为12
 */
public class User {
    private Account<Long> account; // 12位整数账号（使用泛型限制）
    private Long accountId; // 用于数据库存储的账号ID
    private String username;
    private String password;
    private String phone; // 11位手机号
    private boolean isAdmin; // true为管理员，false为普通用户
    
    // 新增字段
    private String email;
    private String address;
    private Integer borrowLimit;      // 借阅额度（默认5本）
    private boolean isBlacklisted;    // 是否在黑名单
    private String blacklistReason;   // 黑名单原因
    private Timestamp blacklistDate;  // 加入黑名单时间
    private boolean isRoot;           // 是否为root用户（超级管理员）
    private boolean isEnabled;        // 账号是否启用
    private Long authorizedBy;        // 授权人账号ID
    private Timestamp authorizedDate; // 授权时间
    
    public User() {
        this.borrowLimit = 5;
        this.isEnabled = false; // 默认未授权
    }
    
    public User(Long accountId, String username, String password, boolean isAdmin) {
        this.accountId = accountId;
        this.account = new Account<>(accountId);
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.borrowLimit = 5;
        this.isEnabled = false;
    }
    
    public Account<Long> getAccount() { return account; }
    public void setAccount(Account<Long> account) {
        this.account = account;
        this.accountId = account.toLong();
    }
    
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
        this.account = new Account<>(accountId);
    }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public Integer getBorrowLimit() { return borrowLimit; }
    public void setBorrowLimit(Integer borrowLimit) { this.borrowLimit = borrowLimit; }
    
    public boolean isBlacklisted() { return isBlacklisted; }
    public void setBlacklisted(boolean blacklisted) { isBlacklisted = blacklisted; }
    
    public String getBlacklistReason() { return blacklistReason; }
    public void setBlacklistReason(String blacklistReason) { this.blacklistReason = blacklistReason; }
    
    public Timestamp getBlacklistDate() { return blacklistDate; }
    public void setBlacklistDate(Timestamp blacklistDate) { this.blacklistDate = blacklistDate; }
    
    public boolean isRoot() { return isRoot; }
    public void setRoot(boolean root) { isRoot = root; }
    
    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }
    
    public Long getAuthorizedBy() { return authorizedBy; }
    public void setAuthorizedBy(Long authorizedBy) { this.authorizedBy = authorizedBy; }
    
    public Timestamp getAuthorizedDate() { return authorizedDate; }
    public void setAuthorizedDate(Timestamp authorizedDate) { this.authorizedDate = authorizedDate; }
}
