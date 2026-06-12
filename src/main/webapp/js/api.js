/**
 * API请求封装
 */
const API = {
    baseUrl: '/api',
    
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
    async login(accountId, password) {
        return this.request('/user/login', {
            method: 'POST',
            body: { accountId, password }
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
    async getAllBooks() {
        return this.request('/book');
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
    }
};
