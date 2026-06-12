/**
 * API请求封装
 */
const API = {
    baseUrl: 'api',
    
    /**
     * 发送请求
     */
    async request(url, options = {}) {
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'same-origin'
        };
        
        const mergedOptions = { ...defaultOptions, ...options };
        if (options.body && typeof options.body === 'object') {
            mergedOptions.body = JSON.stringify(options.body);
        }
        
        try {
            const response = await fetch(this.baseUrl + url, mergedOptions);
            const data = await response.json();
            return data;
        } catch (error) {
            console.error('API请求错误:', error);
            return { success: false, message: '网络请求失败' };
        }
    },
    
    // ========== 用户相关 ==========
    
    /**
     * 登录
     */
    async login(accountId, password, captcha) {
        return this.request('/user/login', {
            method: 'POST',
            body: { accountId, password, captcha }
        });
    },
    
    /**
     * 注册
     */
    async register(accountId, username, password, phone) {
        return this.request('/user/register', {
            method: 'POST',
            body: { accountId, username, password, phone }
        });
    },
    
    /**
     * 退出登录
     */
    async logout() {
        return this.request('/user/logout', { method: 'POST' });
    },
    
    /**
     * 获取当前用户
     */
    async getCurrentUser() {
        return this.request('/user/current');
    },
    
    /**
     * 获取所有用户
     */
    async getAllUsers() {
        return this.request('/user');
    },
    
    /**
     * 更新用户信息
     */
    async updateUserInfo(email, address, phone) {
        return this.request('/user/updateInfo', {
            method: 'POST',
            body: { email, address, phone }
        });
    },
    
    /**
     * 修改密码
     */
    async updatePassword(oldPassword, newPassword) {
        return this.request('/user/updatePassword', {
            method: 'POST',
            body: { oldPassword, newPassword }
        });
    },
    
    /**
     * 设置管理员
     */
    async setAdmin(accountId, isAdmin) {
        return this.request('/user/setAdmin', {
            method: 'POST',
            body: { accountId, isAdmin }
        });
    },
    
    /**
     * 删除用户
     */
    async deleteUser(accountId) {
        return this.request('/user/' + accountId, { method: 'DELETE' });
    },
    
    /**
     * 加入黑名单
     */
    async addToBlacklist(accountId, reason) {
        return this.request('/user/addBlacklist', {
            method: 'POST',
            body: { accountId, reason }
        });
    },
    
    /**
     * 移出黑名单
     */
    async removeFromBlacklist(accountId) {
        return this.request('/user/removeBlacklist', {
            method: 'POST',
            body: { accountId }
        });
    },
    
    /**
     * 验证用户身份
     */
    async verifyUser(accountId, username, phone) {
        return this.request('/user/verify', {
            method: 'POST',
            body: { accountId, username, phone }
        });
    },
    
    /**
     * 重置密码
     */
    async resetPassword(accountId, newPassword) {
        return this.request('/user/resetPassword', {
            method: 'POST',
            body: { accountId, newPassword }
        });
    },
    
    // ========== 图书相关 ==========
    
    /**
     * 获取所有图书
     */
    async getAllBooks(page, size) {
        var params = [];
        if (page) params.push('page=' + page);
        if (size) params.push('size=' + size);
        var qs = params.length ? '?' + params.join('&') : '';
        return this.request('/book' + qs);
    },
    
    /**
     * 搜索图书
     */
    async searchBooks(keyword) {
        return this.request('/book?keyword=' + encodeURIComponent(keyword));
    },
    
    /**
     * 按分类获取图书
     */
    async getBooksByCategory(categoryId) {
        return this.request('/book?categoryId=' + categoryId);
    },

    /**
     * 高级搜索
     */
    async advancedSearch(title, author, publisher, isbn, categoryId) {
        var params = [];
        if (title) params.push('title=' + encodeURIComponent(title));
        if (author) params.push('author=' + encodeURIComponent(author));
        if (publisher) params.push('publisher=' + encodeURIComponent(publisher));
        if (isbn) params.push('isbn=' + encodeURIComponent(isbn));
        if (categoryId) params.push('categoryId=' + categoryId);
        return this.request('/book/search?' + params.join('&'));
    },
    
    /**
     * 添加图书
     */
    async addBook(book) {
        return this.request('/book', {
            method: 'POST',
            body: book
        });
    },
    
    /**
     * 更新图书
     */
    async updateBook(book) {
        return this.request('/book', {
            method: 'PUT',
            body: book
        });
    },
    
    /**
     * 删除图书
     */
    async deleteBook(bookId) {
        return this.request('/book/' + bookId, { method: 'DELETE' });
    },
    
    // ========== 借阅相关 ==========
    
    /**
     * 借阅图书
     */
    async borrowBook(bookId) {
        return this.request('/borrow/borrow', {
            method: 'POST',
            body: { bookId }
        });
    },
    
    /**
     * 归还图书
     */
    async returnBook(recordId) {
        return this.request('/borrow/return', {
            method: 'POST',
            body: { recordId }
        });
    },
    
    /**
     * 续借图书
     */
    async renewBook(recordId) {
        return this.request('/borrow/renew', {
            method: 'POST',
            body: { recordId }
        });
    },
    
    /**
     * 获取借阅记录
     */
    async getBorrowRecords() {
        return this.request('/borrow/records');
    },
    
    /**
     * 获取当前借阅
     */
    async getActiveBorrows() {
        return this.request('/borrow/active');
    },

    /**
     * 获取所有借阅记录（管理员）
     */
    async getAllBorrowRecords() {
        return this.request('/borrow/all');
    },
    
    // ========== 预约相关 ==========

    /**
     * 预约图书
     */
    async reserveBook(bookId) {
        return this.request('/reservation/reserve', {
            method: 'POST',
            body: { bookId }
        });
    },

    /**
     * 取消预约
     */
    async cancelReservation(reservationId) {
        return this.request('/reservation/cancel', {
            method: 'POST',
            body: { reservationId }
        });
    },

    /**
     * 获取我的预约列表
     */
    async getMyReservations() {
        return this.request('/reservation/list');
    },

    /**
     * 获取所有预约记录（管理员）
     */
    async getAllReservations() {
        return this.request('/reservation/all');
    },

    // ========== 罚款相关 ==========

    /**
     * 获取我的罚款记录
     */
    async getMyFines() {
        return this.request('/fine/list');
    },

    /**
     * 缴纳单笔罚款
     */
    async payFine(fineId) {
        return this.request('/fine/pay', {
            method: 'POST',
            body: { fineId }
        });
    },

    /**
     * 缴纳全部罚款
     */
    async payAllFines() {
        return this.request('/fine/payAll', {
            method: 'POST'
        });
    },

    /**
     * 获取所有未缴罚款（管理员）
     */
    async getAllUnpaidFines() {
        return this.request('/fine/all');
    },

    /**
     * 获取所有罚款记录（管理员）
     */
    async getAllFines() {
        return this.request('/fine/allFines');
    },

    /**
     * 通过借阅记录创建并缴纳罚款
     */
    async payFineByRecord(recordId) {
        return this.request('/fineext/payByRecord', {
            method: 'POST',
            body: { recordId }
        });
    },

    // ========== 验证码 ==========

    /**
     * 获取验证码题目
     */
    async getCaptcha() {
        return this.request('/captcha');
    },

    // ========== 评论相关 ==========

    /**
     * 获取图书评论和平均分
     */
    async getBookReviews(bookId) {
        return this.request('/review/book/' + bookId);
    },

    /**
     * 添加/更新评论
     */
    async addReview(bookId, rating, content) {
        return this.request('/review', {
            method: 'POST',
            body: { bookId, rating, content }
        });
    },

    /**
     * 删除自己的评论
     */
    async deleteReview(reviewId) {
        return this.request('/review/' + reviewId, { method: 'DELETE' });
    },

    // ========== 分类相关 ==========
    
    /**
     * 获取所有分类
     */
    async getAllCategories() {
        return this.request('/category');
    },
    
    /**
     * 添加分类
     */
    async addCategory(categoryName, description) {
        return this.request('/category', {
            method: 'POST',
            body: { categoryName, description }
        });
    },

    /**
     * 删除分类
     */
    async deleteCategory(categoryId) {
        return this.request('/category/' + categoryId, { method: 'DELETE' });
    },

    /**
     * 获取黑名单用户
     */
    async getBlacklistedUsers() {
        return this.request('/user/blacklist');
    },

    // ========== 推荐相关 ==========

    /**
     * 获取个性化推荐（基于借阅历史）
     */
    async getPersonalRecommendations() {
        return this.request('/recommend/personal');
    },

    /**
     * 获取高评分图书
     */
    async getHighRatedBooks() {
        return this.request('/recommend/highRated');
    },

    /**
     * 获取新书推荐
     */
    async getNewBooks() {
        return this.request('/recommend/new');
    },

    /**
     * 获取分类热门图书
     */
    async getPopularByCategory(categoryId) {
        return this.request('/recommend/category/' + categoryId);
    },

    // ========== 统计相关 ==========

    /**
     * 获取系统总览统计
     */
    async getStatisticsOverview() {
        return this.request('/statistics/overview');
    },

    /**
     * 获取热门图书排行
     */
    async getPopularBooks() {
        return this.request('/statistics/popularBooks');
    },

    /**
     * 获取用户借阅排行
     */
    async getTopBorrowers() {
        return this.request('/statistics/topBorrowers');
    },

    /**
     * 获取分类借阅统计
     */
    async getCategoryStatistics() {
        return this.request('/statistics/categoryStats');
    },

    /**
     * 获取月度借阅趋势
     */
    async getMonthlyTrend(year) {
        return this.request('/statistics/monthlyTrend?year=' + year);
    },

    /**
     * 获取年度借阅趋势
     */
    async getYearlyTrend() {
        return this.request('/statistics/yearlyTrend');
    },

    // ========== 授权管理 ==========

    async isRoot() {
        return this.request('/authorization/isRoot');
    },

    async getPendingUsers() {
        return this.request('/authorization/pending');
    },

    async getAuthorizedUsers() {
        return this.request('/authorization/authorized');
    },

    async getAdminUsers() {
        return this.request('/authorization/admins');
    },

    async getAuthorizationLogs() {
        return this.request('/authorization/logs');
    },

    async getAuthorizationStats() {
        return this.request('/authorization/stats');
    },

    async authorizeUser(targetId) {
        return this.request('/authorization/authorize', { method: 'POST', body: { targetId } });
    },

    async revokeAuthorization(targetId) {
        return this.request('/authorization/revoke', { method: 'POST', body: { targetId } });
    },

    async grantAdminAuth(targetId) {
        return this.request('/authorization/grantAdmin', { method: 'POST', body: { targetId } });
    },

    async revokeAdminAuth(targetId) {
        return this.request('/authorization/revokeAdmin', { method: 'POST', body: { targetId } });
    },

    // ========== 操作日志 ==========

    async getOperationLogs() {
        return this.request('/logs');
    },

    // ========== 我的评论 ==========

    async getMyReviews() {
        return this.request('/review/user');
    }
};
