/**
 * 主页面脚本 - Bootstrap 5
 */
let currentUser = null;
let categories = [];
let confirmCallback = null;

// ========== 初始化 ==========
document.addEventListener('DOMContentLoaded', async () => {
    await checkLoginStatus();
    if (!currentUser) return;

    buildSidebar();
    initForms();
    initBookSaveButton();
    await loadAllInitialData();
});

async function checkLoginStatus() {
    const result = await API.getCurrentUser();
    if (result.success && result.data) {
        currentUser = result.data;
        document.getElementById('welcomeText').innerHTML =
            '<i class="bi bi-person-circle me-1"></i>欢迎，' + currentUser.username;
        // 根据角色显示/隐藏管理员页面
        toggleAdminPages();
    } else {
        window.location.href = 'login.jsp';
    }
}

function toggleAdminPages() {
    document.querySelectorAll('.admin-only-page').forEach(el => {
        el.style.display = currentUser.isAdmin ? '' : 'none';
    });
    // 控制管理员菜单项（在buildSidebar中处理）
}

function buildSidebar() {
    const navItems = [
        { page: 'books',   icon: 'bi-search',       label: '图书查询', adminOnly: false },
        { page: 'borrow',  icon: 'bi-journal-text',  label: '我的借阅', adminOnly: false },
        { page: 'profile', icon: 'bi-person-gear',   label: '个人中心', adminOnly: false },
        { page: 'bookManage', icon: 'bi-bookshelf',  label: '图书管理', adminOnly: true },
        { page: 'userManage', icon: 'bi-people',     label: '用户管理', adminOnly: true },
    ];

    const buildNav = (containerId) => {
        const container = document.getElementById(containerId);
        if (!container) return;
        container.innerHTML = '';

        navItems.forEach(item => {
            if (item.adminOnly && !currentUser.isAdmin) return;

            const li = document.createElement('li');
            li.className = 'nav-item';
            li.innerHTML = `
                <a class="nav-link sidebar-link ${item.page === 'books' ? 'active' : ''}"
                   href="#" data-page="${item.page}">
                    <i class="bi ${item.icon} me-2"></i>${item.label}
                </a>
            `;
            li.querySelector('a').addEventListener('click', (e) => {
                e.preventDefault();
                switchPage(item.page);
                // 移动端关闭offcanvas
                const offcanvas = bootstrap.Offcanvas.getInstance(document.getElementById('sidebarOffcanvas'));
                if (offcanvas) offcanvas.hide();
            });
            container.appendChild(li);
        });
    };

    buildNav('sidebarNavDesktop');
    buildNav('sidebarNavMobile');
}

function switchPage(page) {
    // 更新侧边栏激活状态
    document.querySelectorAll('.sidebar-link').forEach(link => {
        link.classList.toggle('active', link.dataset.page === page);
    });
    // 切换内容区
    document.querySelectorAll('.content-page').forEach(p => p.classList.remove('active'));
    const target = document.getElementById(page + 'Page');
    if (target) target.classList.add('active');

    // 加载对应数据
    switch (page) {
        case 'books': loadBooks(); break;
        case 'borrow': loadActiveBorrows(); break;
        case 'profile': loadProfile(); break;
        case 'bookManage': loadBooksForManage(); break;
        case 'userManage': loadUsers(); break;
    }
}

async function loadAllInitialData() {
    await loadCategories();
    await loadBooks();
}

// ========== 分类 ==========
async function loadCategories() {
    const result = await API.getAllCategories();
    if (result.success) {
        categories = result.data;
        const filterSelect = document.getElementById('categoryFilter');
        const bookSelect = document.getElementById('bookCategory');

        const buildOptions = (select) => {
            select.innerHTML = '<option value="">' + (select === filterSelect ? '全部分类' : '请选择分类') + '</option>';
            categories.forEach(cat => {
                select.innerHTML += `<option value="${cat.categoryId}">${escapeHtml(cat.categoryName)}</option>`;
            });
        };

        buildOptions(filterSelect);
        buildOptions(bookSelect);
    }
}

// ========== 图书查询 ==========
async function loadBooks() {
    const result = await API.getAllBooks();
    if (result.success) renderBooksTable(result.data);
}

async function searchBooks() {
    const keyword = document.getElementById('bookSearchInput').value;
    const categoryId = document.getElementById('categoryFilter').value;
    let result;
    if (keyword) result = await API.searchBooks(keyword);
    else if (categoryId) result = await API.getBooksByCategory(categoryId);
    else result = await API.getAllBooks();
    if (result.success) renderBooksTable(result.data);
}

function renderBooksTable(books) {
    const tbody = document.getElementById('booksTableBody');
    tbody.innerHTML = '';
    if (books.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">暂无图书</td></tr>';
        return;
    }
    books.forEach(book => {
        const available = book.availableCopies > 0;
        const badgeClass = available ? 'bg-success' : 'bg-secondary';
        tbody.innerHTML += `
            <tr>
                <td><strong>${escapeHtml(book.title)}</strong></td>
                <td>${escapeHtml(book.author || '-')}</td>
                <td>${escapeHtml(book.categoryName || '-')}</td>
                <td>${escapeHtml(book.publisher || '-')}</td>
                <td class="text-center"><span class="badge ${badgeClass}">${book.availableCopies}/${book.totalCopies}</span></td>
                <td class="text-center">
                    <button class="btn btn-primary btn-sm" onclick="borrowBook(${book.bookId})"
                        ${!available ? 'disabled' : ''}>
                        <i class="bi bi-bookmark-plus me-1"></i>${available ? '借阅' : '已借完'}
                    </button>
                </td>
            </tr>`;
    });
}

// ========== 借阅操作 ==========
async function borrowBook(bookId) {
    showConfirm('确定要借阅这本书吗？', async () => {
        const result = await API.borrowBook(bookId);
        if (result.success) {
            showToast('借阅成功', 'success');
            loadBooks();
        } else {
            showToast(result.message || '借阅失败', 'danger');
        }
    });
}

async function returnBook(recordId) {
    showConfirm('确定要归还这本书吗？', async () => {
        const result = await API.returnBook(recordId);
        if (result.success) {
            showToast('归还成功', 'success');
            loadActiveBorrows();
            loadBooks();
        } else {
            showToast(result.message || '归还失败', 'danger');
        }
    });
}

async function renewBook(recordId) {
    showConfirm('确定要续借这本书吗？', async () => {
        const result = await API.renewBook(recordId);
        if (result.success) {
            showToast('续借成功，借阅期限延长31天', 'success');
            loadActiveBorrows();
        } else {
            showToast(result.message || '续借失败', 'danger');
        }
    });
}

// ========== 借阅记录 ==========
async function loadActiveBorrows() {
    const result = await API.getActiveBorrows();
    if (result.success) {
        const tbody = document.getElementById('activeBorrowBody');
        renderBorrowTable(tbody, result.data, true);
    }
}

async function loadBorrowHistory() {
    const result = await API.getBorrowRecords();
    if (result.success) {
        const tbody = document.getElementById('historyBorrowBody');
        renderBorrowTable(tbody, result.data, false);
    }
}

function renderBorrowTable(tbody, records, showActions) {
    tbody.innerHTML = '';
    if (records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">暂无借阅记录</td></tr>';
        return;
    }
    records.forEach(record => {
        const isOverdue = record.status === '借阅中' && new Date(record.dueDate) < new Date();
        let statusBadge;
        if (record.status === '已归还') {
            statusBadge = '<span class="badge bg-success">已归还</span>';
        } else if (isOverdue) {
            statusBadge = '<span class="badge bg-danger">已逾期</span>';
        } else {
            statusBadge = '<span class="badge bg-primary">借阅中</span>';
        }

        let actions = '';
        if (showActions && record.status === '借阅中') {
            actions = `
                <button class="btn btn-success btn-sm" onclick="returnBook(${record.recordId})">
                    <i class="bi bi-arrow-return-left me-1"></i>归还
                </button>
                ${record.renewCount < 1 ? `
                <button class="btn btn-outline-secondary btn-sm" onclick="renewBook(${record.recordId})">
                    <i class="bi bi-arrow-repeat me-1"></i>续借
                </button>` : ''}`;
        }

        // 实时计算罚款：已归还用存储值，逾期中按天数算
        let fineAmount = record.fineAmount || 0;
        if (isOverdue) {
            const now = new Date();
            const dueDate = new Date(record.dueDate);
            const overdueDays = Math.floor((now - dueDate) / (1000 * 60 * 60 * 24));
            fineAmount = overdueDays * 0.1;
        }

        tbody.innerHTML += `
            <tr>
                <td><strong>${escapeHtml(record.bookTitle || '-')}</strong></td>
                <td>${record.borrowDate || '-'}</td>
                <td>${record.dueDate || '-'}</td>
                <td>${fineAmount > 0 ? '<span class="text-danger fw-bold">¥' + fineAmount.toFixed(2) + '</span>' : '-'}</td>
                <td>${statusBadge}</td>
                <td class="text-center">${actions || '-'}</td>
            </tr>`;
    });
}

// 借阅历史标签切换时加载
document.addEventListener('shown.bs.tab', (e) => {
    if (e.target.id === 'historyBorrowTab') loadBorrowHistory();
});

// ========== 个人中心 ==========
function loadProfile() {
    if (!currentUser) return;
    document.getElementById('profileAccountId').value = currentUser.accountId;
    document.getElementById('profileUsername').value = currentUser.username;
    document.getElementById('profilePhone').value = currentUser.phone || '';
    document.getElementById('profileEmail').value = currentUser.email || '';
    document.getElementById('profileAddress').value = currentUser.address || '';
}

async function refreshCurrentUser() {
    const result = await API.getCurrentUser();
    if (result.success && result.data) {
        currentUser = result.data;
        loadProfile();
    }
}

// ========== 图书管理 ==========
async function loadBooksForManage() {
    const result = await API.getAllBooks();
    if (result.success) {
        const tbody = document.getElementById('bookManageTableBody');
        tbody.innerHTML = '';
        if (result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">暂无图书</td></tr>';
            return;
        }
        result.data.forEach(book => {
            tbody.innerHTML += `
                <tr>
                    <td>${book.bookId}</td>
                    <td><strong>${escapeHtml(book.title)}</strong></td>
                    <td>${escapeHtml(book.author || '-')}</td>
                    <td><code>${escapeHtml(book.isbn || '-')}</code></td>
                    <td>${escapeHtml(book.categoryName || '-')}</td>
                    <td class="text-center"><span class="badge bg-info">${book.availableCopies}/${book.totalCopies}</span></td>
                    <td class="text-center">
                        <button class="btn btn-outline-warning btn-sm" onclick="editBook(${book.bookId})">
                            <i class="bi bi-pencil me-1"></i>编辑
                        </button>
                        <button class="btn btn-outline-danger btn-sm" onclick="deleteBook(${book.bookId})">
                            <i class="bi bi-trash me-1"></i>删除
                        </button>
                    </td>
                </tr>`;
        });
    }
}

function showAddBookModal() {
    document.getElementById('bookModalTitle').innerHTML = '<i class="bi bi-plus-circle me-2"></i>添加图书';
    document.getElementById('bookForm').reset();
    document.getElementById('bookId').value = '';
    document.getElementById('bookForm').classList.remove('was-validated');
    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('bookModal'));
    modal.show();
}

async function editBook(bookId) {
    const result = await API.getAllBooks();
    if (result.success) {
        const book = result.data.find(b => b.bookId === bookId);
        if (book) {
            document.getElementById('bookModalTitle').innerHTML = '<i class="bi bi-pencil me-2"></i>编辑图书';
            document.getElementById('bookId').value = book.bookId;
            document.getElementById('bookTitle').value = book.title;
            document.getElementById('bookAuthor').value = book.author || '';
            document.getElementById('bookIsbn').value = book.isbn || '';
            document.getElementById('bookCategory').value = book.categoryId || '';
            document.getElementById('bookPublisher').value = book.publisher || '';
            document.getElementById('bookPublishDate').value = book.publishDate || '';
            document.getElementById('bookTotalCopies').value = book.totalCopies;
            document.getElementById('bookAvailableCopies').value = book.availableCopies;
            document.getElementById('bookForm').classList.remove('was-validated');
            const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('bookModal'));
            modal.show();
        }
    }
}

async function deleteBook(bookId) {
    showConfirm('确定要删除这本书吗？此操作不可撤销。', async () => {
        const result = await API.deleteBook(bookId);
        if (result.success) {
            showToast('删除成功', 'success');
            loadBooksForManage();
            loadBooks();
        } else {
            showToast(result.message || '删除失败', 'danger');
        }
    });
}

// ========== 用户管理 ==========
async function loadUsers() {
    const result = await API.getAllUsers();
    if (result.success) {
        const tbody = document.getElementById('userManageTableBody');
        tbody.innerHTML = '';
        result.data.forEach(user => {
            const isProtected = user.accountId === 100000000000;
            const roleBadge = user.isAdmin
                ? '<span class="badge bg-warning text-dark">管理员</span>'
                : '<span class="badge bg-secondary">普通用户</span>';
            const statusBadge = user.isBlacklisted
                ? '<span class="badge bg-danger">黑名单</span>'
                : '<span class="badge bg-success">正常</span>';

            let actions = '';
            if (isProtected) {
                actions = '<span class="text-muted small">受保护账号</span>';
            } else {
                actions = `
                    <button class="btn btn-outline-info btn-sm" onclick="toggleAdmin(${user.accountId}, ${!user.isAdmin})">
                        <i class="bi bi-person-${user.isAdmin ? 'dash' : 'check'} me-1"></i>${user.isAdmin ? '取消管理员' : '设为管理员'}
                    </button>`;
                if (!user.isAdmin) {
                    actions += `
                        <button class="btn btn-outline-${user.isBlacklisted ? 'success' : 'danger'} btn-sm ms-1"
                            onclick="${user.isBlacklisted ? 'removeBlacklist' : 'addBlacklist'}(${user.accountId})">
                            <i class="bi bi-${user.isBlacklisted ? 'unlock' : 'lock'} me-1"></i>${user.isBlacklisted ? '移出黑名单' : '加入黑名单'}
                        </button>`;
                }
                actions += `
                    <button class="btn btn-outline-danger btn-sm ms-1" onclick="deleteUser(${user.accountId})">
                        <i class="bi bi-trash me-1"></i>删除
                    </button>`;
            }

            tbody.innerHTML += `
                <tr>
                    <td><code>${user.accountId}</code></td>
                    <td>${escapeHtml(user.username)}</td>
                    <td>${escapeHtml(user.phone || '-')}</td>
                    <td class="text-center">${roleBadge}</td>
                    <td class="text-center">${statusBadge}</td>
                    <td class="text-center">${actions}</td>
                </tr>`;
        });
    }
}

async function toggleAdmin(accountId, isAdmin) {
    const result = await API.setAdmin(accountId, isAdmin);
    if (result.success) {
        showToast('权限设置成功', 'success');
        loadUsers();
    } else {
        showToast(result.message || '操作失败', 'danger');
    }
}

async function addBlacklist(accountId) {
    const reason = prompt('请输入加入黑名单的原因：');
    if (reason === null) return;
    if (!reason.trim()) {
        showToast('请输入原因', 'warning');
        return;
    }
    const result = await API.addToBlacklist(accountId, reason.trim());
    if (result.success) {
        showToast('已加入黑名单', 'success');
        loadUsers();
    } else {
        showToast(result.message || '操作失败', 'danger');
    }
}

async function removeBlacklist(accountId) {
    showConfirm('确定要将该用户移出黑名单吗？', async () => {
        const result = await API.removeFromBlacklist(accountId);
        if (result.success) {
            showToast('已移出黑名单', 'success');
            loadUsers();
        } else {
            showToast(result.message || '操作失败', 'danger');
        }
    });
}

async function deleteUser(accountId) {
    showConfirm('确定要删除该用户吗？此操作不可撤销。', async () => {
        const result = await API.deleteUser(accountId);
        if (result.success) {
            showToast('删除成功', 'success');
            loadUsers();
        } else {
            showToast(result.message || '删除失败', 'danger');
        }
    });
}

// ========== 退出 ==========
async function logout() {
    await API.logout();
    sessionStorage.removeItem('currentUser');
    window.location.href = 'login.jsp';
}

// ========== 表单处理 ==========
function initForms() {
    // 个人信息表单
    document.getElementById('profileForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('profileEmail').value;
        const address = document.getElementById('profileAddress').value;
        const phone = document.getElementById('profilePhone').value;

        if (phone && !/^\d{11}$/.test(phone)) {
            showToast('手机号格式不正确', 'warning');
            return;
        }

        const result = await API.updateUserInfo(email, address, phone);
        if (result.success) {
            showToast('信息更新成功', 'success');
            refreshCurrentUser();
        } else {
            showToast(result.message || '更新失败', 'danger');
        }
    });

    // 修改密码表单
    document.getElementById('passwordForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const oldPassword = document.getElementById('oldPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        const confirmNewPassword = document.getElementById('confirmNewPassword').value;

        if (newPassword !== confirmNewPassword) {
            showToast('两次输入的新密码不一致', 'warning');
            return;
        }
        if (newPassword.length < 6) {
            showToast('新密码至少6位', 'warning');
            return;
        }

        const result = await API.updatePassword(oldPassword, newPassword);
        if (result.success) {
            showToast('密码修改成功', 'success');
            document.getElementById('passwordForm').reset();
        } else {
            showToast(result.message || '密码修改失败', 'danger');
        }
    });
}

function initBookSaveButton() {
    document.getElementById('bookSaveBtn').addEventListener('click', async () => {
        const form = document.getElementById('bookForm');
        if (!form.checkValidity()) {
            form.classList.add('was-validated');
            return;
        }

        const bookId = document.getElementById('bookId').value;
        const book = {
            title: document.getElementById('bookTitle').value,
            author: document.getElementById('bookAuthor').value,
            isbn: document.getElementById('bookIsbn').value,
            categoryId: parseInt(document.getElementById('bookCategory').value) || null,
            publisher: document.getElementById('bookPublisher').value,
            publishDate: document.getElementById('bookPublishDate').value,
            totalCopies: parseInt(document.getElementById('bookTotalCopies').value) || 1,
            availableCopies: parseInt(document.getElementById('bookAvailableCopies').value) || 1
        };

        const btn = document.getElementById('bookSaveBtn');
        setBtnLoading(btn, true);

        let result;
        if (bookId) {
            book.bookId = parseInt(bookId);
            result = await API.updateBook(book);
        } else {
            result = await API.addBook(book);
        }

        setBtnLoading(btn, false);

        if (result.success) {
            showToast(bookId ? '图书更新成功' : '图书添加成功', 'success');
            bootstrap.Modal.getInstance(document.getElementById('bookModal')).hide();
            loadBooksForManage();
            loadBooks();
        } else {
            showToast(result.message || '操作失败', 'danger');
        }
    });
}

// ========== 确认弹窗 ==========
function showConfirm(message, callback) {
    document.getElementById('confirmBody').textContent = message;
    confirmCallback = callback;
    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('confirmModal'));
    modal.show();
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('confirmBtn').addEventListener('click', async () => {
        if (confirmCallback) {
            await confirmCallback();
            confirmCallback = null;
        }
        bootstrap.Modal.getInstance(document.getElementById('confirmModal')).hide();
    });
});

// ========== 工具函数 ==========
function setBtnLoading(btn, loading) {
    if (loading) {
        btn.disabled = true;
        btn.dataset.origHtml = btn.innerHTML;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>处理中...';
    } else {
        btn.disabled = false;
        btn.innerHTML = btn.dataset.origHtml;
    }
}

function showToast(message, type) {
    const toastEl = document.getElementById('toast');
    const msgEl = document.getElementById('toastMessage');

    toastEl.className = 'toast align-items-center border-0 position-fixed top-0 start-50 translate-middle-x mt-3';
    const typeMap = {
        success: 'text-bg-success',
        danger: 'text-bg-danger',
        warning: 'text-bg-warning',
        info: 'text-bg-info'
    };
    toastEl.classList.add(typeMap[type] || 'text-bg-info');

    msgEl.textContent = message;
    const toast = bootstrap.Toast.getOrCreateInstance(toastEl, { delay: 3000 });
    toast.show();
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
