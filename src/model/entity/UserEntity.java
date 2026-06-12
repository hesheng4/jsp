package model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "account_id")
    @NotNull(message = "账号不能为空")
    @Digits(integer = 12, fraction = 0, message = "账号必须为12位数字")
    private Long accountId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 20, message = "用户名长度2-20位")
    private String username;

    @Column(name = "password", nullable = false, length = 100)
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少6位")
    private String password;

    @Column(name = "phone", length = 11)
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin = false;

    @Column(name = "email", length = 100)
    @Email(message = "邮箱格式不正确")
    private String email;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "borrow_limit")
    private Integer borrowLimit = 5;

    @Column(name = "is_blacklisted")
    private Boolean isBlacklisted = false;

    @Column(name = "blacklist_reason", length = 255)
    private String blacklistReason;

    @Column(name = "blacklist_date")
    private java.sql.Timestamp blacklistDate;

    @Column(name = "is_root")
    private Boolean isRoot = false;

    @Column(name = "is_enabled")
    private Boolean isEnabled = false;

    @Column(name = "authorized_by")
    private Long authorizedBy;

    @Column(name = "authorized_date")
    private java.sql.Timestamp authorizedDate;

    public UserEntity() {}

    // Getters and Setters
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getBorrowLimit() { return borrowLimit; }
    public void setBorrowLimit(Integer borrowLimit) { this.borrowLimit = borrowLimit; }

    public Boolean getIsBlacklisted() { return isBlacklisted; }
    public void setIsBlacklisted(Boolean isBlacklisted) { this.isBlacklisted = isBlacklisted; }

    public String getBlacklistReason() { return blacklistReason; }
    public void setBlacklistReason(String blacklistReason) { this.blacklistReason = blacklistReason; }

    public java.sql.Timestamp getBlacklistDate() { return blacklistDate; }
    public void setBlacklistDate(java.sql.Timestamp blacklistDate) { this.blacklistDate = blacklistDate; }

    public Boolean getIsRoot() { return isRoot; }
    public void setIsRoot(Boolean isRoot) { this.isRoot = isRoot; }

    public Boolean getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }

    public Long getAuthorizedBy() { return authorizedBy; }
    public void setAuthorizedBy(Long authorizedBy) { this.authorizedBy = authorizedBy; }

    public java.sql.Timestamp getAuthorizedDate() { return authorizedDate; }
    public void setAuthorizedDate(java.sql.Timestamp authorizedDate) { this.authorizedDate = authorizedDate; }
}
