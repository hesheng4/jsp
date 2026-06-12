<%@ include file="WEB-INF/includes/auth-check.jspf" %>
<%@ page import="model.User" %>
<%
    request.setAttribute("currentPage", "log-manage");
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
    <title>操作日志 - 图书借阅管理系统</title>
</head>
<body>
    <%@ include file="WEB-INF/includes/navbar-sidebar.jspf" %>

    <main class="main-content-area">
        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h2 class="h4 fw-bold"><i class="bi bi-clock-history me-2"></i>操作日志</h2>
            <div>
                <input type="text" class="form-control form-control-sm" id="logSearchInput" placeholder="搜索用户/操作类型..." style="width:220px;display:inline-block;">
            </div>
        </div>
        <div class="table-responsive">
            <table class="table table-hover align-middle bg-white rounded-3 shadow-sm">
                <thead class="table-light">
                    <tr>
                        <th>ID</th>
                        <th>用户</th>
                        <th>操作类型</th>
                        <th>详情</th>
                        <th>时间</th>
                    </tr>
                </thead>
                <tbody id="logsBody"></tbody>
            </table>
        </div>
    </main>

    <div id="toast" class="toast align-items-center border-0 position-fixed top-0 start-50 translate-middle-x mt-3" role="alert" style="z-index:9999;">
        <div class="d-flex"><div class="toast-body" id="toastMessage"></div><button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>
    </div>

    <%@ include file="WEB-INF/includes/scripts.jspf" %>
    <script>
    var allLogs = [];

    document.addEventListener('DOMContentLoaded', function() {
        loadLogs();
        document.getElementById('logSearchInput').addEventListener('input', filterLogs);
    });

    async function loadLogs() {
        var result = await API.getOperationLogs();
        var tbody = document.getElementById('logsBody');
        tbody.innerHTML = '';
        if (!result.success || !result.data || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">暂无操作日志</td></tr>';
            return;
        }
        allLogs = result.data;
        renderLogs(allLogs);
    }

    function renderLogs(logs) {
        var tbody = document.getElementById('logsBody');
        tbody.innerHTML = '';
        logs.forEach(function(l) {
            var time = l.operationTime ? l.operationTime.substring(0, 19) : '-';
            var typeBadge = getTypeBadge(l.operationType);
            tbody.innerHTML += '<tr><td>' + l.logId + '</td><td><strong>' + escapeHtml(l.username || '-') + '</strong><br><small class="text-muted">' + (l.accountId || '') + '</small></td><td>' + typeBadge + '</td><td>' + escapeHtml(l.operationDetail || '-') + '</td><td>' + time + '</td></tr>';
        });
    }

    function getTypeBadge(type) {
        var map = {
            '登录': 'bg-success', '退出': 'bg-secondary', '注册': 'bg-info',
            '借阅': 'bg-primary', '归还': 'bg-success', '续借': 'bg-warning text-dark',
            '预约': 'bg-info', '取消预约': 'bg-secondary',
            '添加图书': 'bg-primary', '更新图书': 'bg-warning text-dark', '删除图书': 'bg-danger',
            '评论评分': 'bg-info', '删除评论': 'bg-secondary',
            '缴纳罚款': 'bg-danger', '修改密码': 'bg-warning text-dark', '修改信息': 'bg-secondary',
            '权限变更': 'bg-danger', '黑名单': 'bg-danger', '重置密码': 'bg-danger',
            '添加分类': 'bg-primary', '删除分类': 'bg-danger'
        };
        return '<span class="badge ' + (map[type] || 'bg-secondary') + '">' + escapeHtml(type) + '</span>';
    }

    function filterLogs() {
        var keyword = document.getElementById('logSearchInput').value.toLowerCase();
        if (!keyword) { renderLogs(allLogs); return; }
        var filtered = allLogs.filter(function(l) {
            return (l.username && l.username.toLowerCase().indexOf(keyword) >= 0) ||
                   (l.operationType && l.operationType.toLowerCase().indexOf(keyword) >= 0) ||
                   (l.operationDetail && l.operationDetail.toLowerCase().indexOf(keyword) >= 0);
        });
        renderLogs(filtered);
    }
    </script>
</body>
</html>
