-- =====================================================
-- 图书管理系统扩展功能 - 数据库脚本
-- =====================================================

-- 1. 图书预约表
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    book_id INT NOT NULL,
    reservation_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT '等待中',  -- 等待中、已通知、已取消、已完成
    notify_date DATETIME,                  -- 通知时间
    expire_date DATETIME,                  -- 预约过期时间（通知后3天内未借阅则过期）
    FOREIGN KEY (account_id) REFERENCES users(account_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id)
);

-- 2. 图书评论表
CREATE TABLE IF NOT EXISTS book_reviews (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    book_id INT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),  -- 1-5星评分
    review_content TEXT,
    review_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_visible TINYINT DEFAULT 1,  -- 是否可见（管理员可隐藏不当评论）
    FOREIGN KEY (account_id) REFERENCES users(account_id),
    FOREIGN KEY (book_id) REFERENCES books(book_id),
    UNIQUE KEY unique_user_book_review (account_id, book_id)  -- 每用户每本书只能评论一次
);

-- 3. 修改分类表，支持层级结构
ALTER TABLE categories ADD COLUMN IF NOT EXISTS parent_id INT DEFAULT NULL;
ALTER TABLE categories ADD COLUMN IF NOT EXISTS level INT DEFAULT 1;
ALTER TABLE categories ADD FOREIGN KEY (parent_id) REFERENCES categories(category_id);

-- 4. 扩展用户表
ALTER TABLE users ADD COLUMN IF NOT EXISTS email VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS address VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS borrow_limit INT DEFAULT 5;        -- 借阅额度
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_blacklisted TINYINT DEFAULT 0;  -- 黑名单标记
ALTER TABLE users ADD COLUMN IF NOT EXISTS blacklist_reason VARCHAR(255);     -- 黑名单原因
ALTER TABLE users ADD COLUMN IF NOT EXISTS blacklist_date DATETIME;           -- 加入黑名单时间
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_root TINYINT DEFAULT 0;         -- 是否为root用户
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_enabled TINYINT DEFAULT 0;      -- 账号是否启用（需root授权）
ALTER TABLE users ADD COLUMN IF NOT EXISTS authorized_by BIGINT;              -- 授权人账号ID
ALTER TABLE users ADD COLUMN IF NOT EXISTS authorized_date DATETIME;          -- 授权时间

-- 6. 授权记录表
CREATE TABLE IF NOT EXISTS authorization_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    operator_id BIGINT NOT NULL,          -- 操作人（root）账号ID
    target_id BIGINT NOT NULL,            -- 目标用户账号ID
    action_type VARCHAR(20) NOT NULL,     -- 操作类型：授权、回收、设为管理员、取消管理员
    action_detail VARCHAR(255),           -- 操作详情
    action_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (operator_id) REFERENCES users(account_id),
    FOREIGN KEY (target_id) REFERENCES users(account_id)
);

-- 5. 罚款记录表
CREATE TABLE IF NOT EXISTS fine_records (
    fine_id INT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    record_id INT NOT NULL,              -- 关联借阅记录
    fine_amount DECIMAL(10,2) NOT NULL,
    fine_reason VARCHAR(255),
    create_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    pay_status VARCHAR(20) DEFAULT '未缴纳',  -- 未缴纳、已缴纳
    pay_date DATETIME,
    FOREIGN KEY (account_id) REFERENCES users(account_id),
    FOREIGN KEY (record_id) REFERENCES borrow_records(record_id)
);

-- =====================================================
-- 触发器
-- =====================================================

-- 触发器1: 图书归还后自动通知预约用户
DELIMITER //
CREATE TRIGGER IF NOT EXISTS after_book_return
AFTER UPDATE ON borrow_records
FOR EACH ROW
BEGIN
    DECLARE v_book_id INT;
    DECLARE v_available INT;
    DECLARE v_reservation_id INT;
    DECLARE v_account_id BIGINT;
    
    -- 检查是否是归还操作
    IF OLD.status = '借阅中' AND NEW.status = '已归还' THEN
        SET v_book_id = NEW.book_id;
        
        -- 获取可借数量
        SELECT available_copies INTO v_available FROM books WHERE book_id = v_book_id;
        
        -- 如果有可借图书，通知最早预约的用户
        IF v_available > 0 THEN
            SELECT reservation_id, account_id INTO v_reservation_id, v_account_id
            FROM reservations 
            WHERE book_id = v_book_id AND status = '等待中'
            ORDER BY reservation_date ASC
            LIMIT 1;
            
            IF v_reservation_id IS NOT NULL THEN
                UPDATE reservations 
                SET status = '已通知', 
                    notify_date = NOW(),
                    expire_date = DATE_ADD(NOW(), INTERVAL 3 DAY)
                WHERE reservation_id = v_reservation_id;
            END IF;
        END IF;
    END IF;
END//
DELIMITER ;

-- 触发器2: 借阅时检查用户是否在黑名单或有未缴罚款
DELIMITER //
CREATE TRIGGER IF NOT EXISTS before_borrow_check
BEFORE INSERT ON borrow_records
FOR EACH ROW
BEGIN
    DECLARE v_is_blacklisted TINYINT;
    DECLARE v_unpaid_fines DECIMAL(10,2);
    DECLARE v_current_borrows INT;
    DECLARE v_borrow_limit INT;
    
    -- 检查黑名单
    SELECT is_blacklisted, borrow_limit INTO v_is_blacklisted, v_borrow_limit
    FROM users WHERE account_id = NEW.account_id;
    
    IF v_is_blacklisted = 1 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '用户在黑名单中，禁止借阅';
    END IF;
    
    -- 检查未缴罚款
    SELECT COALESCE(SUM(fine_amount), 0) INTO v_unpaid_fines
    FROM fine_records 
    WHERE account_id = NEW.account_id AND pay_status = '未缴纳';
    
    IF v_unpaid_fines > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '存在未缴纳罚款，请先缴纳罚款';
    END IF;
    
    -- 检查借阅额度
    SELECT COUNT(*) INTO v_current_borrows
    FROM borrow_records 
    WHERE account_id = NEW.account_id AND status = '借阅中';
    
    IF v_current_borrows >= v_borrow_limit THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '已达到借阅上限';
    END IF;
END//
DELIMITER ;

-- 触发器3: 归还图书时自动生成罚款记录
DELIMITER //
CREATE TRIGGER IF NOT EXISTS after_return_create_fine
AFTER UPDATE ON borrow_records
FOR EACH ROW
BEGIN
    -- 如果是归还操作且有罚款
    IF OLD.status = '借阅中' AND NEW.status = '已归还' AND NEW.fine_amount > 0 THEN
        INSERT INTO fine_records (account_id, record_id, fine_amount, fine_reason)
        VALUES (NEW.account_id, NEW.record_id, NEW.fine_amount, '逾期归还罚款');
    END IF;
END//
DELIMITER ;

-- 触发器4: 逾期超过30天自动加入黑名单
DELIMITER //
CREATE TRIGGER IF NOT EXISTS check_overdue_blacklist
AFTER UPDATE ON borrow_records
FOR EACH ROW
BEGIN
    DECLARE v_overdue_days INT;
    
    IF NEW.status = '已归还' AND NEW.return_date > NEW.due_date THEN
        SET v_overdue_days = DATEDIFF(NEW.return_date, NEW.due_date);
        
        IF v_overdue_days > 30 THEN
            UPDATE users 
            SET is_blacklisted = 1,
                blacklist_reason = CONCAT('逾期', v_overdue_days, '天归还图书'),
                blacklist_date = NOW()
            WHERE account_id = NEW.account_id;
        END IF;
    END IF;
END//
DELIMITER ;

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
-- 初始化admin管理员用户（如果不存在）
-- 默认管理员账号: 100000000001, 用户名: admin, 密码: admin123
-- =====================================================
INSERT IGNORE INTO users (account_id, username, password, phone, is_admin, is_root, is_enabled, borrow_limit)
VALUES (100000000001, 'admin', 'admin123', '00000000000', 1, 1, 1, 999);
