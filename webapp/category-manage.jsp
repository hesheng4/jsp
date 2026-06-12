<%@ include file="WEB-INF/includes/auth-check.jspf" %>
<%@ page import="model.User" %>
<%
    request.setAttribute("currentPage", "category-manage");
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
    <title>分类管理 - 图书借阅管理系统</title>
</head>
<body>
    <%@ include file="WEB-INF/includes/navbar-sidebar.jspf" %>

    <main class="main-content-area">
        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h2 class="h4 fw-bold"><i class="bi bi-tags me-2"></i>分类管理</h2>
            <button class="btn btn-primary" onclick="showAddCategoryModal()">
                <i class="bi bi-plus-lg me-1"></i>添加分类
            </button>
        </div>
        <div class="table-responsive">
            <table class="table table-hover table-striped align-middle bg-white rounded-3 shadow-sm">
                <thead class="table-light">
                    <tr>
                        <th>ID</th>
                        <th>分类名称</th>
                        <th>描述</th>
                        <th class="text-center">操作</th>
                    </tr>
                </thead>
                <tbody id="categoryTableBody"></tbody>
            </table>
        </div>
    </main>

    <!-- 添加分类弹窗 -->
    <div class="modal fade" id="categoryModal" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content border-0 shadow-lg rounded-3">
                <div class="modal-header bg-primary text-white">
                    <h5 class="modal-title fw-bold"><i class="bi bi-plus-circle me-2"></i>添加分类</h5>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body">
                    <form id="categoryForm" novalidate>
                        <div class="mb-3">
                            <label class="form-label">分类名称 <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="categoryName" required placeholder="请输入分类名称">
                            <div class="invalid-feedback">请输入分类名称</div>
                        </div>
                        <div class="mb-3">
                            <label class="form-label">描述</label>
                            <textarea class="form-control" id="categoryDesc" rows="3" placeholder="可选，输入分类描述"></textarea>
                        </div>
                    </form>
                </div>
                <div class="modal-footer border-0">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
                    <button type="button" class="btn btn-primary" id="categorySaveBtn">
                        <i class="bi bi-check-lg me-1"></i>保存
                    </button>
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
    document.addEventListener('DOMContentLoaded', function() {
        loadCategories();
        document.getElementById('categorySaveBtn').addEventListener('click', saveCategory);
        addInputValidation(document.getElementById('categoryName'), Validator.required, '请输入分类名称');
    });

    async function loadCategories() {
        var result = await API.getAllCategories();
        var tbody = document.getElementById('categoryTableBody');
        tbody.innerHTML = '';
        if (!result.success || !result.data || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">暂无分类</td></tr>';
            return;
        }
        result.data.forEach(function(c) {
            tbody.innerHTML +=
                '<tr>' +
                '<td>' + c.categoryId + '</td>' +
                '<td><strong>' + escapeHtml(c.categoryName) + '</strong></td>' +
                '<td>' + escapeHtml(c.description || '-') + '</td>' +
                '<td class="text-center">' +
                '<button class="btn btn-outline-danger btn-sm" onclick="deleteCategory(' + c.categoryId + ')"><i class="bi bi-trash me-1"></i>删除</button>' +
                '</td></tr>';
        });
    }

    function showAddCategoryModal() {
        document.getElementById('categoryForm').reset();
        document.getElementById('categoryForm').classList.remove('was-validated');
        bootstrap.Modal.getOrCreateInstance(document.getElementById('categoryModal')).show();
    }

    async function saveCategory() {
        var name = document.getElementById('categoryName').value.trim();
        if (!Validator.required(name)) {
            document.getElementById('categoryName').classList.add('is-invalid');
            showToast('请输入分类名称', 'warning');
            return;
        }
        var desc = document.getElementById('categoryDesc').value.trim();
        var btn = document.getElementById('categorySaveBtn');
        setBtnLoading(btn, true);
        var result = await API.addCategory(name, desc || null);
        setBtnLoading(btn, false);
        if (result.success) {
            showToast('分类添加成功', 'success');
            bootstrap.Modal.getInstance(document.getElementById('categoryModal')).hide();
            loadCategories();
        } else {
            showToast(result.message || '添加失败', 'danger');
        }
    }

    function deleteCategory(categoryId) {
        showConfirm('确定删除此分类？关联的图书将失去分类信息。', async function() {
            var result = await API.deleteCategory(categoryId);
            if (result.success) {
                showToast('分类删除成功', 'success');
                loadCategories();
            } else {
                showToast(result.message || '删除失败', 'danger');
            }
        });
    }
    </script>
</body>
</html>
