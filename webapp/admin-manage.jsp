<%@ include file="WEB-INF/includes/auth-check.jspf" %>
<%@ page import="model.User" %>
<%
    request.setAttribute("currentPage", "admin-manage");
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
    <title>授权管理 - 图书借阅管理系统</title>
</head>
<body>
    <%@ include file="WEB-INF/includes/navbar-sidebar.jspf" %>

    <main class="main-content-area">
        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h2 class="h4 fw-bold"><i class="bi bi-shield-lock me-2"></i>授权管理</h2>
        </div>

        <!-- 统计卡片 -->
        <div class="row g-3 mb-4" id="authStats">
            <div class="col-md-4"><div class="card shadow-sm p-3 text-center"><div class="fs-3 fw-bold text-warning" id="statPending">-</div><div class="text-muted small">待授权用户</div></div></div>
            <div class="col-md-4"><div class="card shadow-sm p-3 text-center"><div class="fs-3 fw-bold text-success" id="statAuthorized">-</div><div class="text-muted small">已授权用户</div></div></div>
            <div class="col-md-4"><div class="card shadow-sm p-3 text-center"><div class="fs-3 fw-bold text-info" id="statAdmins">-</div><div class="text-muted small">管理员</div></div></div>
        </div>

        <ul class="nav nav-tabs mb-3" role="tablist">
            <li class="nav-item"><button class="nav-link active" data-bs-toggle="tab" data-bs-target="#pendingTab"><i class="bi bi-hourglass-split me-1"></i>待授权</button></li>
            <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#authorizedTab"><i class="bi bi-check-circle me-1"></i>已授权</button></li>
            <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#adminTab"><i class="bi bi-person-gear me-1"></i>管理员</button></li>
            <li class="nav-item"><button class="nav-link" data-bs-toggle="tab" data-bs-target="#logsTab"><i class="bi bi-clock-history me-1"></i>授权记录</button></li>
        </ul>

        <div class="tab-content">
            <div class="tab-pane fade show active" id="pendingTab">
                <div class="table-responsive"><table class="table table-hover align-middle bg-white rounded-3 shadow-sm">
                    <thead class="table-light"><tr><th>账号</th><th>用户名</th><th>手机号</th><th class="text-center">操作</th></tr></thead>
                    <tbody id="pendingBody"></tbody>
                </table></div>
            </div>
            <div class="tab-pane fade" id="authorizedTab">
                <div class="table-responsive"><table class="table table-hover align-middle bg-white rounded-3 shadow-sm">
                    <thead class="table-light"><tr><th>账号</th><th>用户名</th><th>手机号</th><th class="text-center">操作</th></tr></thead>
                    <tbody id="authorizedBody"></tbody>
                </table></div>
            </div>
            <div class="tab-pane fade" id="adminTab">
                <div class="table-responsive"><table class="table table-hover align-middle bg-white rounded-3 shadow-sm">
                    <thead class="table-light"><tr><th>账号</th><th>用户名</th><th>手机号</th><th class="text-center">操作</th></tr></thead>
                    <tbody id="adminBody"></tbody>
                </table></div>
            </div>
            <div class="tab-pane fade" id="logsTab">
                <div class="table-responsive"><table class="table table-hover align-middle bg-white rounded-3 shadow-sm">
                    <thead class="table-light"><tr><th>操作人</th><th>目标用户</th><th>操作类型</th><th>详情</th><th>时间</th></tr></thead>
                    <tbody id="logsBody"></tbody>
                </table></div>
            </div>
        </div>
    </main>

    <div id="toast" class="toast align-items-center border-0 position-fixed top-0 start-50 translate-middle-x mt-3" role="alert" style="z-index:9999;">
        <div class="d-flex"><div class="toast-body" id="toastMessage"></div><button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>
    </div>
    <div class="modal fade" id="confirmModal" tabindex="-1">
        <div class="modal-dialog modal-dialog-centered modal-sm"><div class="modal-content border-0 shadow-lg rounded-3">
            <div class="modal-header border-0"><h6 class="modal-title fw-bold">确认操作</h6><button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>
            <div class="modal-body" id="confirmBody"></div>
            <div class="modal-footer border-0"><button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">取消</button><button type="button" class="btn btn-danger btn-sm" id="confirmBtn">确认</button></div>
        </div></div>
    </div>

    <%@ include file="WEB-INF/includes/scripts.jspf" %>
    <script>
    document.addEventListener('DOMContentLoaded', function() {
        loadStats();
        loadPending();
    });

    async function loadStats() {
        var result = await API.getAuthorizationStats();
        if (result.success && result.data) {
            document.getElementById('statPending').textContent = result.data.pending || 0;
            document.getElementById('statAuthorized').textContent = result.data.authorized || 0;
            document.getElementById('statAdmins').textContent = result.data.admins || 0;
        }
    }

    async function loadPending() {
        var result = await API.getPendingUsers();
        var tbody = document.getElementById('pendingBody');
        tbody.innerHTML = '';
        if (!result.success || !result.data || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">无待授权用户</td></tr>';
            return;
        }
        result.data.forEach(function(u) {
            tbody.innerHTML += '<tr><td><code>' + u.accountId + '</code></td><td>' + escapeHtml(u.username) + '</td><td>' + escapeHtml(u.phone || '-') + '</td><td class="text-center"><button class="btn btn-success btn-sm" onclick="doAuthorize(' + u.accountId + ')"><i class="bi bi-check-lg me-1"></i>授权</button></td></tr>';
        });
    }

    async function loadAuthorized() {
        var result = await API.getAuthorizedUsers();
        var tbody = document.getElementById('authorizedBody');
        tbody.innerHTML = '';
        if (!result.success || !result.data || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">无已授权用户</td></tr>';
            return;
        }
        result.data.forEach(function(u) {
            tbody.innerHTML += '<tr><td><code>' + u.accountId + '</code></td><td>' + escapeHtml(u.username) + '</td><td>' + escapeHtml(u.phone || '-') + '</td><td class="text-center"><button class="btn btn-outline-danger btn-sm" onclick="doRevoke(' + u.accountId + ')"><i class="bi bi-x-lg me-1"></i>回收</button></td></tr>';
        });
    }

    async function loadAdmins() {
        var result = await API.getAdminUsers();
        var tbody = document.getElementById('adminBody');
        tbody.innerHTML = '';
        if (!result.success || !result.data || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">无管理员</td></tr>';
            return;
        }
        result.data.forEach(function(u) {
            tbody.innerHTML += '<tr><td><code>' + u.accountId + '</code></td><td>' + escapeHtml(u.username) + '</td><td>' + escapeHtml(u.phone || '-') + '</td><td class="text-center"><button class="btn btn-outline-warning btn-sm" onclick="doRevokeAdmin(' + u.accountId + ')"><i class="bi bi-person-dash me-1"></i>撤销管理员</button></td></tr>';
        });
    }

    async function loadLogs() {
        var result = await API.getAuthorizationLogs();
        var tbody = document.getElementById('logsBody');
        tbody.innerHTML = '';
        if (!result.success || !result.data || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">暂无记录</td></tr>';
            return;
        }
        result.data.forEach(function(l) {
            var time = l.actionTime ? l.actionTime.substring(0, 19) : '-';
            tbody.innerHTML += '<tr><td>' + escapeHtml(l.operatorName || '-') + '</td><td>' + escapeHtml(l.targetName || '-') + '</td><td><span class="badge bg-primary">' + escapeHtml(l.actionType) + '</span></td><td>' + escapeHtml(l.actionDetail || '-') + '</td><td>' + time + '</td></tr>';
        });
    }

    function doAuthorize(targetId) {
        showConfirm('确定授权该用户？', async function() {
            var r = await API.authorizeUser(targetId);
            if (r.success) { showToast('授权成功', 'success'); loadStats(); loadPending(); loadAuthorized(); }
            else showToast(r.message || '失败', 'danger');
        });
    }

    function doRevoke(targetId) {
        showConfirm('确定回收该用户授权？', async function() {
            var r = await API.revokeAuthorization(targetId);
            if (r.success) { showToast('已回收', 'success'); loadStats(); loadPending(); loadAuthorized(); }
            else showToast(r.message || '失败', 'danger');
        });
    }

    function doRevokeAdmin(targetId) {
        showConfirm('确定撤销该用户的管理员权限？', async function() {
            var r = await API.revokeAdminAuth(targetId);
            if (r.success) { showToast('已撤销', 'success'); loadStats(); loadAdmins(); }
            else showToast(r.message || '失败', 'danger');
        });
    }

    document.addEventListener('shown.bs.tab', function(e) {
        var t = e.target.getAttribute('data-bs-target');
        if (t === '#authorizedTab') loadAuthorized();
        if (t === '#adminTab') loadAdmins();
        if (t === '#logsTab') loadLogs();
    });
    </script>
</body>
</html>
