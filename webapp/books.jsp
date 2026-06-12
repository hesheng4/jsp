<%@ include file="WEB-INF/includes/auth-check.jspf" %>
<%
    request.setAttribute("currentPage", "books");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <%@ include file="WEB-INF/includes/head.jspf" %>
    <title>图书查询 - 图书借阅管理系统</title>
</head>
<body>
    <%@ include file="WEB-INF/includes/navbar-sidebar.jspf" %>

    <!-- 主内容区 -->
    <main class="main-content-area">
        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h2 class="h4 fw-bold"><i class="bi bi-search me-2"></i>图书查询</h2>
        </div>

        <!-- 搜索栏 -->
        <div class="row g-3 mb-3">
            <div class="col-md-5">
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-search"></i></span>
                    <input type="text" class="form-control" id="bookSearchInput" placeholder="搜索书名或作者...">
                </div>
            </div>
            <div class="col-md-3">
                <select class="form-select" id="categoryFilter">
                    <option value="">全部分类</option>
                </select>
            </div>
            <div class="col-auto">
                <button class="btn btn-primary" onclick="searchBooks()">
                    <i class="bi bi-search me-1"></i>搜索
                </button>
            </div>
            <div class="col-auto">
                <button class="btn btn-outline-secondary" type="button" data-bs-toggle="collapse" data-bs-target="#advancedSearchPanel">
                    <i class="bi bi-sliders me-1"></i>高级搜索
                </button>
            </div>
        </div>

        <!-- 高级搜索面板 -->
        <div class="collapse mb-3" id="advancedSearchPanel">
            <div class="card border-0 shadow-sm rounded-3">
                <div class="card-body">
                    <div class="row g-3">
                        <div class="col-md-4">
                            <label class="form-label small fw-bold">书名</label>
                            <input type="text" class="form-control form-control-sm" id="advTitle" placeholder="输入书名">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label small fw-bold">作者</label>
                            <input type="text" class="form-control form-control-sm" id="advAuthor" placeholder="输入作者">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label small fw-bold">出版社</label>
                            <input type="text" class="form-control form-control-sm" id="advPublisher" placeholder="输入出版社">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label small fw-bold">ISBN</label>
                            <input type="text" class="form-control form-control-sm" id="advIsbn" placeholder="输入ISBN">
                        </div>
                        <div class="col-md-4">
                            <label class="form-label small fw-bold">分类</label>
                            <select class="form-select form-select-sm" id="advCategory">
                                <option value="">全部分类</option>
                            </select>
                        </div>
                        <div class="col-md-4 d-flex align-items-end gap-2">
                            <button class="btn btn-primary btn-sm" onclick="advancedSearch()"><i class="bi bi-search me-1"></i>搜索</button>
                            <button class="btn btn-outline-secondary btn-sm" onclick="clearAdvancedSearch()"><i class="bi bi-x-lg me-1"></i>清空</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 图书列表 -->
        <div class="table-responsive">
            <table class="table table-hover table-striped align-middle bg-white rounded-3 shadow-sm">
                <thead class="table-light">
                    <tr>
                        <th>书名</th>
                        <th>作者</th>
                        <th>分类</th>
                        <th>出版社</th>
                        <th class="text-center">可借/总数</th>
                        <th class="text-center">操作</th>
                    </tr>
                </thead>
                <tbody id="booksTableBody"></tbody>
            </table>
        </div>
    </main>

    <!-- Toast -->
    <div id="toast" class="toast align-items-center border-0 position-fixed top-0 start-50 translate-middle-x mt-3" role="alert" style="z-index:9999;">
        <div class="d-flex">
            <div class="toast-body" id="toastMessage"></div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>
    </div>

    <!-- 确认弹窗 -->
    <div class="modal fade" id="confirmModal" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered modal-sm">
            <div class="modal-content border-0 shadow-lg rounded-3">
                <div class="modal-header border-0">
                    <h6 class="modal-title fw-bold">确认操作</h6>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body" id="confirmBody"></div>
                <div class="modal-footer border-0">
                    <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">取消</button>
                    <button type="button" class="btn btn-danger btn-sm" id="confirmBtn">确认</button>
                </div>
            </div>
        </div>
    </div>

    <%@ include file="WEB-INF/includes/scripts.jspf" %>
    <script>
    // ========== 图书查询页 ==========
    var categories = [];

    document.addEventListener('DOMContentLoaded', function() {
        loadCategories();
        loadBooks();
    });

    async function loadCategories() {
        var result = await API.getAllCategories();
        if (result.success) {
            categories = result.data;
            var select = document.getElementById('categoryFilter');
            select.innerHTML = '<option value="">全部分类</option>';
            var advSelect = document.getElementById('advCategory');
            advSelect.innerHTML = '<option value="">全部分类</option>';
            categories.forEach(function(cat) {
                select.innerHTML += '<option value="' + cat.categoryId + '">' + escapeHtml(cat.categoryName) + '</option>';
                advSelect.innerHTML += '<option value="' + cat.categoryId + '">' + escapeHtml(cat.categoryName) + '</option>';
            });
        }
    }

    async function loadBooks() {
        var result = await API.getAllBooks();
        if (result.success) renderBooksTable(result.data);
    }

    async function searchBooks() {
        var keyword = document.getElementById('bookSearchInput').value;
        var categoryId = document.getElementById('categoryFilter').value;
        var result;
        if (keyword) result = await API.searchBooks(keyword);
        else if (categoryId) result = await API.getBooksByCategory(categoryId);
        else result = await API.getAllBooks();
        if (result.success) renderBooksTable(result.data);
    }

    function renderBooksTable(books) {
        var tbody = document.getElementById('booksTableBody');
        tbody.innerHTML = '';
        if (books.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">暂无图书</td></tr>';
            return;
        }
        books.forEach(function(book) {
            var available = book.availableCopies > 0;
            var badgeClass = available ? 'bg-success' : 'bg-secondary';
            var btn = available
                ? '<button class="btn btn-primary btn-sm" onclick="borrowBook(' + book.bookId + ')"><i class="bi bi-bookmark-plus me-1"></i>借阅</button>'
                : '<button class="btn btn-outline-info btn-sm" onclick="reserveBook(' + book.bookId + ')"><i class="bi bi-calendar-check me-1"></i>预约</button>';
            tbody.innerHTML +=
                '<tr>' +
                '<td><strong>' + escapeHtml(book.title) + '</strong></td>' +
                '<td>' + escapeHtml(book.author || '-') + '</td>' +
                '<td>' + escapeHtml(book.categoryName || '-') + '</td>' +
                '<td>' + escapeHtml(book.publisher || '-') + '</td>' +
                '<td class="text-center"><span class="badge ' + badgeClass + '">' + book.availableCopies + '/' + book.totalCopies + '</span></td>' +
                '<td class="text-center">' +
                '<a class="btn btn-outline-secondary btn-sm me-1" href="book-detail.jsp?bookId=' + book.bookId + '"><i class="bi bi-info-circle"></i> 详情</a>' +
                btn + '</td>' +
                '</tr>';
        });
    }

    async function borrowBook(bookId) {
        showConfirm('确定要借阅这本书吗？', async function() {
            var result = await API.borrowBook(bookId);
            if (result.success) { showToast('借阅成功', 'success'); loadBooks(); }
            else showToast(result.message || '借阅失败', 'danger');
        });
    }

    async function reserveBook(bookId) {
        console.log('[预约] 点击预约按钮, bookId=' + bookId);
        if (!API.reserveBook) {
            showToast('预约功能加载失败,请刷新页面(Ctrl+F5)', 'danger');
            console.error('[预约] API.reserveBook 不存在,请清除浏览器缓存');
            return;
        }
        showConfirm('图书已借完，确定要预约排队吗？', async function() {
            try {
                console.log('[预约] 发送预约请求...');
                var result = await API.reserveBook(bookId);
                console.log('[预约] 响应:', result);
                if (result.success) { showToast('预约成功，排队等待中', 'success'); loadBooks(); }
                else showToast(result.message || '预约失败', 'danger');
            } catch (e) {
                console.error('[预约] 异常:', e);
                showToast('网络异常，请重试', 'danger');
            }
        });
    }

    // 搜索框回车
    document.addEventListener('DOMContentLoaded', function() {
        var input = document.getElementById('bookSearchInput');
        if (input) input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') searchBooks();
        });
    });

    async function advancedSearch() {
        var title = document.getElementById('advTitle').value.trim();
        var author = document.getElementById('advAuthor').value.trim();
        var publisher = document.getElementById('advPublisher').value.trim();
        var isbn = document.getElementById('advIsbn').value.trim();
        var categoryId = document.getElementById('advCategory').value;
        if (!title && !author && !publisher && !isbn && !categoryId) {
            showToast('请至少填写一个搜索条件', 'warning');
            return;
        }
        var result = await API.advancedSearch(title, author, publisher, isbn, categoryId || null);
        if (result.success) renderBooksTable(result.data);
    }

    function clearAdvancedSearch() {
        document.getElementById('advTitle').value = '';
        document.getElementById('advAuthor').value = '';
        document.getElementById('advPublisher').value = '';
        document.getElementById('advIsbn').value = '';
        document.getElementById('advCategory').value = '';
        loadBooks();
    }
    </script>
</body>
</html>
