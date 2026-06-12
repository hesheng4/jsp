<%@ include file="WEB-INF/includes/auth-check.jspf" %>
<%@ page import="model.User" %>
<%
    request.setAttribute("currentPage", "user-manage");
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
    <title>用户管理 - 图书借阅管理系统</title>
</head>
<body>
    <%@ include file="WEB-INF/includes/navbar-sidebar.jspf" %>

    <main class="main-content-area">
        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h2 class="h4 fw-bold"><i class="bi bi-people me-2"></i>用户管理</h2>
        </div>
        <div class="table-responsive">
            <table class="table table-hover table-striped align-middle bg-white rounded-3 shadow-sm">
                <thead class="table-light">
                    <tr>
                        <th>账号</th>
                        <th>用户名</th>
                        <th>手机号</th>
                        <th class="text-center">角色</th>
                        <th class="text-center">状态</th>
                        <th class="text-center">操作</th>
                    </tr>
                </thead>
                <tbody id="userManageTableBody"></tbody>
            </table>
        </div>
    </main>

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
    document.addEventListener('DOMContentLoaded', loadUsers);

    async function loadUsers() {
        var result = await API.getAllUsers();
        if (result.success) {
            var tbody = document.getElementById('userManageTableBody');
            tbody.innerHTML = '';
            result.data.forEach(function(u) {
                var isProtected = u.accountId === 100000000000;
                var role = u.isAdmin ? '<span class="badge bg-warning text-dark">管理员</span>' : '<span class="badge bg-secondary">普通用户</span>';
                var status = u.isBlacklisted ? '<span class="badge bg-danger">黑名单</span>' : '<span class="badge bg-success">正常</span>';
                var actions = '';
                if (isProtected) {
                    actions = '<span class="text-muted small">受保护</span>';
                } else {
                    actions = '<button class="btn btn-outline-info btn-sm" onclick="toggleAdmin(' + u.accountId + ',' + !u.isAdmin + ')"><i class="bi bi-person-' + (u.isAdmin ? 'dash' : 'check') + ' me-1"></i>' + (u.isAdmin ? '取消管理员' : '设为管理员') + '</button>';
                    if (!u.isAdmin) actions += ' <button class="btn btn-outline-' + (u.isBlacklisted ? 'success' : 'danger') + ' btn-sm" onclick="' + (u.isBlacklisted ? 'removeBlacklist' : 'addBlacklist') + '(' + u.accountId + ')"><i class="bi bi-' + (u.isBlacklisted ? 'unlock' : 'lock') + ' me-1"></i>' + (u.isBlacklisted ? '移出黑名单' : '加入黑名单') + '</button>';
                    actions += ' <button class="btn btn-outline-danger btn-sm" onclick="deleteUser(' + u.accountId + ')"><i class="bi bi-trash me-1"></i>删除</button>';
                }
                tbody.innerHTML +=
                    '<tr><td><code>' + u.accountId + '</code></td>' +
                    '<td>' + escapeHtml(u.username) + '</td>' +
                    '<td>' + escapeHtml(u.phone || '-') + '</td>' +
                    '<td class="text-center">' + role + '</td>' +
                    '<td class="text-center">' + status + '</td>' +
                    '<td class="text-center">' + actions + '</td></tr>';
            });
        }
    }

    async function toggleAdmin(accountId, isAdmin) {
        var result = await API.setAdmin(accountId, isAdmin);
        if (result.success) { showToast('权限设置成功', 'success'); loadUsers(); }
        else showToast(result.message || '操作失败', 'danger');
    }

    async function addBlacklist(accountId) {
        var reason = prompt('请输入黑名单原因：');
        if (reason === null || !reason.trim()) return;
        var result = await API.addToBlacklist(accountId, reason.trim());
        if (result.success) { showToast('已加入黑名单', 'success'); loadUsers(); }
        else showToast(result.message || '操作失败', 'danger');
    }

    async function removeBlacklist(accountId) {
        showConfirm('确定移出黑名单？', async function() {
            var result = await API.removeFromBlacklist(accountId);
            if (result.success) { showToast('已移出黑名单', 'success'); loadUsers(); }
            else showToast(result.message || '操作失败', 'danger');
        });
    }

    async function deleteUser(accountId) {
        showConfirm('确定删除此用户？不可撤销。', async function() {
            var result = await API.deleteUser(accountId);
            if (result.success) { showToast('删除成功', 'success'); loadUsers(); }
            else showToast(result.message || '删除失败', 'danger');
        });
    }
    </script>
</body>
</html>
