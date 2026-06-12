<%@ include file="WEB-INF/includes/auth-check.jspf" %>
<%@ page import="model.User" %>
<%
    request.setAttribute("currentPage", "book-manage");
    User _u = (User) session.getAttribute("user");
    if (_u == null || !_u.isAdmin()) {
        response.sendRedirect(request.getContextPath() + "/books.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <%@ include file="WEB-INF/includes/head.jspf" %>
    <title>图书管理 - 图书借阅管理系统</title>
</head>
<body>
    <%@ include file="WEB-INF/includes/navbar-sidebar.jspf" %>

    <main class="main-content-area">
        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h2 class="h4 fw-bold"><i class="bi bi-bookshelf me-2"></i>图书管理</h2>
            <button class="btn btn-primary" onclick="showAddBookModal()">
                <i class="bi bi-plus-lg me-1"></i>添加图书
            </button>
        </div>
        <div class="table-responsive">
            <table class="table table-hover table-striped align-middle bg-white rounded-3 shadow-sm">
                <thead class="table-light">
                    <tr>
                        <th>ID</th>
                        <th>书名</th>
                        <th>作者</th>
                        <th>ISBN</th>
                        <th>分类</th>
                        <th class="text-center">库存</th>
                        <th class="text-center">操作</th>
                    </tr>
                </thead>
                <tbody id="bookManageTableBody"></tbody>
            </table>
        </div>
    </main>

    <!-- 图书弹窗 -->
    <div class="modal fade" id="bookModal" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered modal-lg">
            <div class="modal-content border-0 shadow-lg rounded-3">
                <div class="modal-header bg-primary text-white">
                    <h5 class="modal-title fw-bold" id="bookModalTitle"><i class="bi bi-plus-circle me-2"></i>添加图书</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <form id="bookForm" novalidate>
                        <input type="hidden" id="bookId">
                        <div class="row g-3">
                            <div class="col-md-6">
                                <label class="form-label">书名 <span class="text-danger">*</span></label>
                                <input type="text" class="form-control" id="bookTitle" required>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">作者</label>
                                <input type="text" class="form-control" id="bookAuthor">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">ISBN</label>
                                <input type="text" class="form-control" id="bookIsbn">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">分类</label>
                                <select class="form-select" id="bookCategory"><option value="">请选择分类</option></select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">出版社</label>
                                <input type="text" class="form-control" id="bookPublisher">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">出版日期</label>
                                <input type="date" class="form-control" id="bookPublishDate">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">总册数</label>
                                <input type="number" class="form-control" id="bookTotalCopies" min="1" value="1">
                            </div>
                            <div class="col-md-6">
                                <label class="form-label">可借册数</label>
                                <input type="number" class="form-control" id="bookAvailableCopies" min="0" value="1">
                            </div>
                        </div>
                    </form>
                </div>
                <div class="modal-footer border-0">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                    <button type="button" class="btn btn-primary" id="bookSaveBtn"><i class="bi bi-check-lg me-1"></i>保存</button>
                </div>
            </div>
        </div>
    </div>

    <!-- Toast + Confirm -->
    <div id="toast" class="toast align-items-center border-0 position-fixed top-0 start-50 translate-middle-x mt-3" role="alert" style="z-index:9999;">
        <div class="d-flex"><div class="toast-body" id="toastMessage"></div><button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>
    </div>
    <div class="modal fade" id="confirmModal" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered modal-sm">
            <div class="modal-content border-0 shadow-lg rounded-3">
                <div class="modal-header border-0"><h6 class="modal-title fw-bold">确认操作</h6><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
                <div class="modal-body" id="confirmBody"></div>
                <div class="modal-footer border-0"><button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">取消</button><button type="button" class="btn btn-danger btn-sm" id="confirmBtn">确认</button></div>
            </div>
        </div>
    </div>

    <%@ include file="WEB-INF/includes/scripts.jspf" %>
    <script>
    var categories = [];

    document.addEventListener('DOMContentLoaded', function() {
        loadCategories();
        loadBooks();
        document.getElementById('bookSaveBtn').addEventListener('click', saveBook);

        // 实时校验
        addInputValidation(document.getElementById('bookTitle'), Validator.required, '请输入书名');
        addInputValidation(document.getElementById('bookIsbn'), Validator.isbn, 'ISBN格式错误（10位或13位数字）');
        addInputValidation(document.getElementById('bookTotalCopies'), function(v) { return parseInt(v) > 0; }, '册数必须大于0');
        addInputValidation(document.getElementById('bookAvailableCopies'), function(v) { return parseInt(v) >= 0; }, '可借册数不能为负数');
    });

    async function loadCategories() {
        var result = await API.getAllCategories();
        if (result.success) {
            categories = result.data;
            var select = document.getElementById('bookCategory');
            select.innerHTML = '<option value="">请选择分类</option>';
            categories.forEach(function(c) {
                select.innerHTML += '<option value="' + c.categoryId + '">' + escapeHtml(c.categoryName) + '</option>';
            });
        }
    }

    async function loadBooks() {
        var result = await API.getAllBooks();
        if (result.success) {
            var tbody = document.getElementById('bookManageTableBody');
            tbody.innerHTML = '';
            if (result.data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">暂无图书</td></tr>';
                return;
            }
            result.data.forEach(function(b) {
                tbody.innerHTML +=
                    '<tr><td>' + b.bookId + '</td>' +
                    '<td><strong>' + escapeHtml(b.title) + '</strong></td>' +
                    '<td>' + escapeHtml(b.author || '-') + '</td>' +
                    '<td><code>' + escapeHtml(b.isbn || '-') + '</code></td>' +
                    '<td>' + escapeHtml(b.categoryName || '-') + '</td>' +
                    '<td class="text-center"><span class="badge bg-info">' + b.availableCopies + '/' + b.totalCopies + '</span></td>' +
                    '<td class="text-center">' +
                    '<button class="btn btn-outline-warning btn-sm" onclick="editBook(' + b.bookId + ')"><i class="bi bi-pencil me-1"></i>编辑</button> ' +
                    '<button class="btn btn-outline-danger btn-sm" onclick="deleteBook(' + b.bookId + ')"><i class="bi bi-trash me-1"></i>删除</button>' +
                    '</td></tr>';
            });
        }
    }

    function showAddBookModal() {
        document.getElementById('bookModalTitle').innerHTML = '<i class="bi bi-plus-circle me-2"></i>添加图书';
        document.getElementById('bookForm').reset();
        document.getElementById('bookId').value = '';
        document.getElementById('bookForm').classList.remove('was-validated');
        bootstrap.Modal.getOrCreateInstance(document.getElementById('bookModal')).show();
    }

    async function editBook(bookId) {
        var result = await API.getAllBooks();
        if (result.success) {
            var book = result.data.find(function(b) { return b.bookId === bookId; });
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
                bootstrap.Modal.getOrCreateInstance(document.getElementById('bookModal')).show();
            }
        }
    }

    async function saveBook() {
        var form = document.getElementById('bookForm');
        if (!form.checkValidity()) { form.classList.add('was-validated'); return; }

        // 自定义校验
        var title = document.getElementById('bookTitle').value.trim();
        var isbn = document.getElementById('bookIsbn').value.trim();
        var totalCopies = parseInt(document.getElementById('bookTotalCopies').value) || 0;
        var availableCopies = parseInt(document.getElementById('bookAvailableCopies').value) || 0;

        if (!Validator.required(title)) {
            document.getElementById('bookTitle').classList.add('is-invalid');
            showToast('请输入书名', 'warning');
            return;
        }
        if (isbn && !Validator.isbn(isbn)) {
            document.getElementById('bookIsbn').classList.add('is-invalid');
            showToast('ISBN格式错误（10位或13位数字）', 'warning');
            return;
        }
        if (totalCopies < 1) {
            document.getElementById('bookTotalCopies').classList.add('is-invalid');
            showToast('总册数至少为1', 'warning');
            return;
        }
        if (availableCopies < 0 || availableCopies > totalCopies) {
            document.getElementById('bookAvailableCopies').classList.add('is-invalid');
            showToast('可借册数必须在0至总册数之间', 'warning');
            return;
        }

        var bookId = document.getElementById('bookId').value;
        var book = {
            title: title, author: document.getElementById('bookAuthor').value,
            isbn: isbn, categoryId: parseInt(document.getElementById('bookCategory').value) || null,
            publisher: document.getElementById('bookPublisher').value,
            publishDate: document.getElementById('bookPublishDate').value,
            totalCopies: totalCopies, availableCopies: availableCopies
        };
        var btn = document.getElementById('bookSaveBtn');
        setBtnLoading(btn, true);
        var result;
        if (bookId) { book.bookId = parseInt(bookId); result = await API.updateBook(book); }
        else result = await API.addBook(book);
        setBtnLoading(btn, false);
        if (result.success) {
            showToast(bookId ? '更新成功' : '添加成功', 'success');
            bootstrap.Modal.getInstance(document.getElementById('bookModal')).hide();
            loadBooks();
        } else showToast(result.message || '操作失败', 'danger');
    }

    async function deleteBook(bookId) {
        showConfirm('确定删除此书？不可撤销。', async function() {
            var result = await API.deleteBook(bookId);
            if (result.success) { showToast('删除成功', 'success'); loadBooks(); }
            else showToast(result.message || '删除失败', 'danger');
        });
    }
    </script>
</body>
</html>
