<%@ include file="WEB-INF/includes/auth-check.jspf" %>
<%
    request.setAttribute("currentPage", "reservation");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <%@ include file="WEB-INF/includes/head.jspf" %>
    <title>我的预约 - 图书借阅管理系统</title>
</head>
<body>
    <%@ include file="WEB-INF/includes/navbar-sidebar.jspf" %>

    <main class="main-content-area">
        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h2 class="h4 fw-bold"><i class="bi bi-calendar-check me-2"></i>我的预约</h2>
        </div>

        <ul class="nav nav-tabs mb-3" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" data-bs-toggle="tab" data-bs-target="#myReservTab" type="button">
                    <i class="bi bi-list-ul me-1"></i>我的预约
                </button>
            </li>
            <li class="nav-item admin-only-nav" role="presentation" style="display:none;">
                <button class="nav-link" data-bs-toggle="tab" data-bs-target="#allReservTab" type="button" id="allReservTabBtn">
                    <i class="bi bi-table me-1"></i>全部预约记录
                </button>
            </li>
        </ul>

        <div class="tab-content">
            <div class="tab-pane fade show active" id="myReservTab">
                <div class="table-responsive">
                    <table class="table table-hover align-middle bg-white rounded-3 shadow-sm">
                        <thead class="table-light">
                            <tr>
                                <th>书名</th>
                                <th>预约日期</th>
                                <th>状态</th>
                                <th>通知日期</th>
                                <th>过期日期</th>
                                <th class="text-center">操作</th>
                            </tr>
                        </thead>
                        <tbody id="myReservBody"></tbody>
                    </table>
                </div>
            </div>
            <div class="tab-pane fade" id="allReservTab">
                <div class="table-responsive">
                    <table class="table table-hover align-middle bg-white rounded-3 shadow-sm">
                        <thead class="table-light">
                            <tr>
                                <th>用户名</th>
                                <th>书名</th>
                                <th>预约日期</th>
                                <th>状态</th>
                                <th>通知日期</th>
                                <th>过期日期</th>
                            </tr>
                        </thead>
                        <tbody id="allReservBody"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </main>

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
        loadMyReservations();
        if (window._isAdmin) {
            document.querySelector('.admin-only-nav').style.display = '';
        }
    });

    async function loadMyReservations() {
        var result = await API.getMyReservations();
        var tbody = document.getElementById('myReservBody');
        tbody.innerHTML = '';
        if (!result.success || !result.data || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">暂无预约记录</td></tr>';
            return;
        }
        result.data.forEach(function(r) {
            var statusHtml = getStatusBadge(r.status);
            var actions = getReservActions(r);
            tbody.innerHTML +=
                '<tr>' +
                '<td><strong>' + escapeHtml(r.bookTitle || '-') + '</strong></td>' +
                '<td>' + (r.reservationDate ? r.reservationDate.substring(0, 19) : '-') + '</td>' +
                '<td>' + statusHtml + '</td>' +
                '<td>' + (r.notifyDate ? r.notifyDate.substring(0, 19) : '-') + '</td>' +
                '<td>' + (r.expireDate ? r.expireDate.substring(0, 19) : '-') + '</td>' +
                '<td class="text-center">' + actions + '</td></tr>';
        });
    }

    async function loadAllReservations() {
        var result = await API.getAllReservations();
        var tbody = document.getElementById('allReservBody');
        tbody.innerHTML = '';
        if (!result.success || !result.data || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">暂无预约记录</td></tr>';
            return;
        }
        result.data.forEach(function(r) {
            tbody.innerHTML +=
                '<tr>' +
                '<td><strong>' + escapeHtml(r.username || '-') + '</strong></td>' +
                '<td>' + escapeHtml(r.bookTitle || '-') + '</td>' +
                '<td>' + (r.reservationDate ? r.reservationDate.substring(0, 19) : '-') + '</td>' +
                '<td>' + getStatusBadge(r.status) + '</td>' +
                '<td>' + (r.notifyDate ? r.notifyDate.substring(0, 19) : '-') + '</td>' +
                '<td>' + (r.expireDate ? r.expireDate.substring(0, 19) : '-') + '</td></tr>';
        });
    }

    function getStatusBadge(status) {
        var map = {
            '等待中': 'bg-warning text-dark',
            '已通知': 'bg-success',
            '已取消': 'bg-secondary',
            '已完成': 'bg-info'
        };
        return '<span class="badge ' + (map[status] || 'bg-secondary') + '">' + escapeHtml(status) + '</span>';
    }

    function getReservActions(r) {
        if (r.status === '等待中') {
            return '<button class="btn btn-outline-danger btn-sm" onclick="cancelReserv(' + r.reservationId + ')"><i class="bi bi-x-circle me-1"></i>取消预约</button>';
        }
        if (r.status === '已通知') {
            var href = window._ctxPath + '/book-detail.jsp?bookId=' + r.bookId;
            return '<a class="btn btn-success btn-sm" href="' + href + '"><i class="bi bi-bookmark-plus me-1"></i>去借阅</a>';
        }
        return '-';
    }

    function cancelReserv(reservationId) {
        showConfirm('确定要取消此预约吗？', async function() {
            var result = await API.cancelReservation(reservationId);
            if (result.success) { showToast('预约已取消', 'success'); loadMyReservations(); }
            else showToast(result.message || '取消失败', 'danger');
        });
    }

    document.addEventListener('shown.bs.tab', function(e) {
        var targetId = e.target.getAttribute('data-bs-target');
        if (targetId === '#allReservTab') loadAllReservations();
    });
    </script>
</body>
</html>
