-- =====================================================
-- 图书管理系统扩展功能 - 简化版数据库脚本
-- 执行此脚本前请确保已有基础表：users, books, borrow_records, categories
-- =====================================================

-- 1. 扩展用户表（添加新字段）
-- 注意：如果字段已存在会报错，可以忽略
ALTER TABLE users ADD COLUMN email VARCHAR(100);
ALTER TABLE users ADD COLUMN address VARCHAR(255);
ALTER TABLE users ADD COLUMN borrow_limit INT DEFAULT 5;
ALTER TABLE users ADD COLUMN is_blacklisted TINYINT DEFAULT 0;
ALTER TABLE users ADD COLUMN blacklist_reason VARCHAR(255);
ALTER TABLE users ADD COLUMN blacklist_date DATETIME;
ALTER TABLE users ADD COLUMN is_root TINYINT DEFAULT 0;
ALTER TABLE users ADD COLUMN is_enabled TINYINT DEFAULT 1;
ALTER TABLE users ADD COLUMN authorized_by BIGINT;
ALTER TABLE users ADD COLUMN authorized_date DATETIME;

-- 2. 扩展分类表（支持层级）
ALTER TABLE categories ADD COLUMN IF NOT EXISTS parent_id INT;
ALTER TABLE categories ADD COLUMN IF NOT EXISTS level INT DEFAULT 1;

-- 3. 图书预约表
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    book_id INT NOT NULL,
    reservation_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT '等待中',
    notify_date DATETIME,
    expire_date DATETIME,
    FOREIGN KEY (account_id) REFERENCES users(account_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE
);

-- 4. 图书评论表
CREATE TABLE IF NOT EXISTS book_reviews (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    book_id INT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    review_content TEXT,
    review_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_visible TINYINT DEFAULT 1,
    FOREIGN KEY (account_id) REFERENCES users(account_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_book_review (account_id, book_id)
);

-- 5. 罚款记录表
CREATE TABLE IF NOT EXISTS fine_records (
    fine_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    record_id INT NOT NULL,
    fine_amount DECIMAL(10,2) NOT NULL,
    fine_reason VARCHAR(255),
    create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    pay_status VARCHAR(20) DEFAULT '未缴纳',
    pay_date DATETIME,
    FOREIGN KEY (account_id) REFERENCES users(account_id) ON DELETE CASCADE,
    FOREIGN KEY (record_id) REFERENCES borrow_records(record_id) ON DELETE CASCADE
);

-- 6. 授权记录表
CREATE TABLE IF NOT EXISTS authorization_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NOT NULL,
    target_id BIGINT NOT NULL,
    action_type VARCHAR(20) NOT NULL,
    action_detail VARCHAR(255),
    action_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (operator_id) REFERENCES users(account_id) ON DELETE CASCADE,
    FOREIGN KEY (target_id) REFERENCES users(account_id) ON DELETE CASCADE
);

-- =====================================================
-- 索引优化
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_reservations_book_status ON reservations(book_id, status);
CREATE INDEX IF NOT EXISTS idx_reservations_account ON reservations(account_id);
CREATE INDEX IF NOT EXISTS idx_reviews_book ON book_reviews(book_id);
CREATE INDEX IF NOT EXISTS idx_reviews_account ON book_reviews(account_id);
CREATE INDEX IF NOT EXISTS idx_fines_account_status ON fine_records(account_id, pay_status);
CREATE INDEX IF NOT EXISTS idx_borrow_date ON borrow_records(borrow_date);
CREATE INDEX IF NOT EXISTS idx_auth_logs_operator ON authorization_logs(operator_id);
CREATE INDEX IF NOT EXISTS idx_auth_logs_target ON authorization_logs(target_id);

-- =====================================================
-- 初始化数据
-- =====================================================

-- 将现有管理员设为已启用
UPDATE users SET is_enabled = 1 WHERE is_admin = 1;

-- 创建admin管理员用户（如果不存在）
INSERT IGNORE INTO users (account_id, username, password, phone, is_admin, is_root, is_enabled, borrow_limit)
VALUES (100000000001, 'admin', 'admin123', '00000000000', 1, 1, 1, 999);

-- =====================================================
-- 完成提示
-- =====================================================
SELECT '扩展表创建完成！' AS message;
