package model;

/**
 * 账号泛型类
 * 限制账号为12位整数类型
 */
public class Account<T extends Number> {
    private T accountId;
    
    public Account(T accountId) {
        if (!isValidAccount(accountId)) {
            throw new IllegalArgumentException("账号必须是12位整数！");
        }
        this.accountId = accountId;
    }
    
    /**
     * 验证账号是否为12位整数
     */
    private boolean isValidAccount(T accountId) {
        if (accountId == null) {
            return false;
        }
        
        String accountStr = accountId.toString();
        // 检查是否为12位数字
        if (accountStr.length() != 12) {
            return false;
        }
        
        // 检查是否全为数字
        try {
            Long.parseLong(accountStr);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public T getAccountId() {
        return accountId;
    }
    
    public void setAccountId(T accountId) {
        if (!isValidAccount(accountId)) {
            throw new IllegalArgumentException("账号必须是12位整数！");
        }
        this.accountId = accountId;
    }
    
    @Override
    public String toString() {
        return accountId.toString();
    }
    
    /**
     * 转换为Long类型（用于数据库存储）
     */
    public Long toLong() {
        return Long.parseLong(accountId.toString());
    }
}

