package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 密码加密工具 - SHA-256 + 盐值
 */
public class PasswordUtil {

    /**
     * 生成随机盐值
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 使用SHA-256 + 盐值加密密码
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256不可用", e);
        }
    }

    /**
     * 验证密码是否匹配
     */
    public static boolean verifyPassword(String password, String storedHash, String salt) {
        String computed = hashPassword(password, salt);
        return computed.equals(storedHash);
    }

    /**
     * 检查密码格式是否为旧版明文（无盐值）
     */
    public static boolean isLegacyPassword(String passwordField) {
        // 旧密码格式：纯文本，不含Base64的盐值分隔符
        return !passwordField.contains(":");
    }

    /**
     * 从存储格式中提取哈希和盐值
     * 存储格式：hash:salt
     */
    public static String[] splitStoredPassword(String stored) {
        if (stored == null) return new String[]{"", ""};
        String[] parts = stored.split(":", 2);
        if (parts.length == 2) return parts;
        return new String[]{stored, ""};
    }

    /**
     * 生成存储格式的密码字符串
     */
    public static String encode(String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        return hash + ":" + salt;
    }
}
