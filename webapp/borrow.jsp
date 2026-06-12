<%@ include file="WEB-INF/includes/auth-check.jspf" %>
<%
    request.setAttribute("currentPage", "borrow");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <%@ include file="WEB-INF/includes/head.jspf" %>
    <title>我的借阅 - 图书借阅管理系统</title>
</head>
<body>
    <%@ include file="WEB-INF/includes/navbar-sidebar.jspf" %>

    <main class="main-content-area">
        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h2 class="h4 fw-bold"><i class="bi bi-journal-text me-2"></i>我的借阅</h2>
        </div>

        <div id="unpaidFinesAlert" class="alert alert-info d-flex align-items-center justify-content-between mb-3" role="alert">
            <div>
                <i class="bi bi-hourglass-split me-2" id="finesIcon"></i>
                <span id="unpaidFinesText">罚款状态：加载中...</span>
            </div>
            <button class="btn btn-warning btn-sm fw-bold d-none" id="payAllBtn" onclick="payAllFines()">
                <i class="bi bi-cash-coin me-1"></i>一键缴纳
            </button>
        </div>

        <ul class="nav nav-tabs mb-3" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" data-bs-toggle="tab" data-bs-target="#activeBorrowTab" type="button">
                    <i class="bi bi-bookmark-check me-1"></i>当前借阅
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" data-bs-toggle="tab" data-bs-target="#historyBorrowTab" type="button">
                    <i class="bi bi-clock-history me-1"></i>借阅历史
                </button>
            </li>
            <li class="nav-item admin-only-nav" role="presentation" style="display:none;">
                <button class="nav-link" data-bs-toggle="tab" data-bs-target="#allBorrowTab" type="button" id="allBorrowTabBtn">
                    <i class="bi bi-table me-1"></i>全部借阅记录
                </button>
            </li>
            <li class="nav-item admin-only-nav" role="presentation" style="display:none;">
                <button class="nav-link" data-bs-toggle="tab" data-bs-target="#allFinesTab" type="button" id="allFinesTabBtn">
                    <i class="bi bi-cash-stack me-1"></i>全部罚款记录
                </button>
            </li>
        </ul>

        <div class="tab-content">
            <div class="tab-pane fade show active" id="activeBorrowTab">
                <div class="table-responsive">
                    <table class="table table-hover align-middle bg-white rounded-3 shadow-sm">
                        <thead class="table-light">
                            <tr>
                                <th>书名</th>
                                <th>借阅日期</th>
                                <th>到期日期</th>
                                <th>罚款</th>
                                <th>状态</th>
                                <th class="text-center">操作</th>
                            </tr>
                        </thead>
                        <tbody id="activeBorrowBody"></tbody>
                    </table>
                </div>
            </div>
            <div class="tab-pane fade" id="historyBorrowTab">
                <div class="table-responsive">
                    <table class="table table-hover align-middle bg-white rounded-3 shadow-sm">
                        <thead class="table-light">
                            <tr>
                                <th>书名</th>
                                <th>借阅日期</th>
                                <th>到期日期</th>
                                <th>归还日期</th>
                                <th>罚款</th>
                                <th>状态</th>
                            </tr>
                        </thead>
                        <tbody id="historyBorrowBody"></tbody>
                    </table>
                </div>
            </div>
            <div class="tab-pane fade" id="allBorrowTab">
                <div class="table-responsive">
                    <table class="table table-hover align-middle bg-white rounded-3 shadow-sm">
                        <thead class="table-light">
                            <tr>
                                <th>用户名</th>
                                <th>书名</th>
                                <th>借阅日期</th>
                                <th>到期日期</th>
                                <th>归还日期</th>
                                <th>罚款</th>
                                <th>状态</th>
                            </tr>
                        </thead>
                        <tbody id="allBorrowBody"></tbody>
                    </table>
                </div>
            </div>
            <div class="tab-pane fade" id="allFinesTab">
                <div class="table-responsive">
                    <table class="table table-hover align-middle bg-white rounded-3 shadow-sm">
                        <thead class="table-light">
                            <tr>
                                <th>ID</th>
                                <th>用户名</th>
                                <th>书名</th>
                                <th>罚款金额</th>
                                <th>原因</th>
                                <th>创建时间</th>
                                <th>状态</th>
                                <th>缴纳时间</th>
                            </tr>
                        </thead>
                        <tbody id="allFinesBody"></tbody>
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
        loadActiveBorrows();
        loadUnpaidFines();
        // 管理员显示全部借阅和全部罚款标签
        if (window._isAdmin) {
            document.querySelectorAll('.admin-only-nav').forEach(function(el) {
                el.style.display = '';
            });
        }
    });

    // ========== 罚款缴纳 ==========
    async function loadUnpaidFines() {
        var alertEl = document.getElementById('unpaidFinesAlert');
        var payBtn = document.getElementById('payAllBtn');
        var iconEl = document.getElementById('finesIcon');
        var textEl = document.getElementById('unpaidFinesText');

        // 同时查罚款记录和当前借阅
        var [fineResult, activeResult] = await Promise.all([
            API.getMyFines(),
            API.getActiveBorrows()
        ]);

        // 罚款记录中的未缴纳
        var unpaidRecords = (fineResult.success && fineResult.data)
            ? fineResult.data.filter(function(f) { return f.payStatus === '未缴纳'; })
            : [];
        var recordFines = unpaidRecords.reduce(function(sum, f) { return sum + f.fineAmount; }, 0);

        // 逾期借阅的预估罚款
        var overdueFines = 0;
        var overdueCount = 0;
        if (activeResult.success && activeResult.data) {
            var now = new Date();
            activeResult.data.forEach(function(r) {
                if (r.status === '借阅中' && new Date(r.dueDate) < now) {
                    var overdueDays = Math.floor((now - new Date(r.dueDate)) / (1000 * 60 * 60 * 24));
                    overdueFines += overdueDays * 0.1;
                    overdueCount++;
                }
            });
        }

        payBtn.classList.add('d-none');
        if (recordFines > 0) {
            alertEl.className = 'alert alert-warning d-flex align-items-center justify-content-between mb-3';
            iconEl.className = 'bi bi-exclamation-triangle-fill me-2';
            textEl.textContent = '未缴罚款：¥' + recordFines.toFixed(2) + '（共' + unpaidRecords.length + '笔）';
            payBtn.classList.remove('d-none');
        } else if (overdueFines > 0) {
            alertEl.className = 'alert alert-warning d-flex align-items-center justify-content-between mb-3';
            iconEl.className = 'bi bi-exclamation-triangle-fill me-2';
            textEl.textContent = '逾期' + overdueCount + '本书，预估罚款¥' + overdueFines.toFixed(2) + '（归还时确定）';
        } else {
            alertEl.className = 'alert alert-success d-flex align-items-center justify-content-between mb-3';
            iconEl.className = 'bi bi-check-circle-fill me-2';
            textEl.textContent = '罚款状态：无未缴罚款，无逾期';
        }
    }

    async function payAllFines() {
        showConfirm('确定要缴纳全部未缴罚款吗？', async function() {
            var result = await API.payAllFines();
            if (result.success) {
                showToast('罚款已全部缴纳', 'success');
                loadUnpaidFines();
            } else {
                showToast(result.message || '缴纳失败', 'danger');
            }
        });
    }

    function payFineRecord(recordId) {
        showConfirm('确定要缴纳这笔罚款吗？', async function() {
            var result = await API.payFineByRecord(recordId);
            if (result.success) {
                showToast('罚款已缴纳', 'success');
                loadBorrowHistory();
                loadUnpaidFines();
            } else {
                showToast(result.message || '缴纳失败', 'danger');
            }
        });
    }

    // 全部借阅记录（管理员）
    async function loadAllBorrowRecords() {
        var result = await API.getAllBorrowRecords();
        var tbody = document.getElementById('allBorrowBody');
        tbody.innerHTML = '';
        if (!result.success || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">暂无记录</td></tr>';
            return;
        }
        result.data.forEach(function(r) {
            var isOverdue = r.status === '借阅中' && new Date(r.dueDate) < new Date();
            var badge = r.status === '已归还'
                ? '<span class="badge bg-success">已归还</span>'
                : (isOverdue ? '<span class="badge bg-danger">已逾期</span>' : '<span class="badge bg-primary">借阅中</span>');
            var fineAmount = r.fineAmount || 0;
            if (isOverdue) {
                var now = new Date();
                var dueDate = new Date(r.dueDate);
                var overdueDays = Math.floor((now - dueDate) / (1000 * 60 * 60 * 24));
                fineAmount = overdueDays * 0.1;
            }
            tbody.innerHTML +=
                '<tr>' +
                '<td><strong>' + escapeHtml(r.accountName || '-') + '</strong></td>' +
                '<td>' + escapeHtml(r.bookTitle || '-') + '</td>' +
                '<td>' + (r.borrowDate || '-') + '</td>' +
                '<td>' + (r.dueDate || '-') + '</td>' +
                '<td>' + (r.returnDate || '-') + '</td>' +
                '<td>' + (fineAmount > 0 ? '<span class="text-danger fw-bold">¥' + fineAmount.toFixed(2) + '</span>' : '-') + '</td>' +
                '<td>' + badge + '</td></tr>';
        });
    }

    // 全部罚款记录（管理员）
    async function loadAllFines() {
        var result = await API.getAllFines();
        var tbody = document.getElementById('allFinesBody');
        tbody.innerHTML = '';
        if (!result.success || !result.data || result.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-4">暂无罚款记录</td></tr>';
            return;
        }
        result.data.forEach(function(f) {
            var statusBadge = f.payStatus === '已缴纳'
                ? '<span class="badge bg-success">已缴纳</span>'
                : '<span class="badge bg-danger">未缴纳</span>';
            var createDate = f.createDate ? f.createDate.substring(0, 19) : '-';
            var payDate = f.payDate ? f.payDate.substring(0, 19) : '-';
            tbody.innerHTML +=
                '<tr>' +
                '<td>' + f.fineId + '</td>' +
                '<td><strong>' + escapeHtml(f.username || '-') + '</strong></td>' +
                '<td>' + escapeHtml(f.bookTitle || '-') + '</td>' +
                '<td><span class="text-danger fw-bold">¥' + (f.fineAmount || 0).toFixed(2) + '</span></td>' +
                '<td>' + escapeHtml(f.fineReason || '-') + '</td>' +
                '<td>' + createDate + '</td>' +
                '<td>' + statusBadge + '</td>' +
                '<td>' + payDate + '</td>' +
                '</tr>';
        });
    }

    async function loadActiveBorrows() {
        var result = await API.getActiveBorrows();
        if (result.success) renderTable('activeBorrowBody', result.data, true);
    }

    async function loadBorrowHistory() {
        var result = await API.getBorrowRecords();
        if (result.success) renderTable('historyBorrowBody', result.data, false);
    }

    function renderTable(tbodyId, records, showActions) {
        var tbody = document.getElementById(tbodyId);
        tbody.innerHTML = '';
        if (records.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-4">暂无借阅记录</td></tr>';
            return;
        }
        records.forEach(function(r) {
            var isOverdue = r.status === '借阅中' && new Date(r.dueDate) < new Date();
            var badge = r.status === '已归还'
                ? '<span class="badge bg-success">已归还</span>'
                : (isOverdue ? '<span class="badge bg-danger">已逾期</span>' : '<span class="badge bg-primary">借阅中</span>');
            // 实时计算罚款：已归还用存储值，逾期中按天数算，正常借阅显示0
            var fineAmount = r.fineAmount || 0;
            if (isOverdue) {
                var now = new Date();
                var dueDate = new Date(r.dueDate);
                var overdueDays = Math.floor((now - dueDate) / (1000 * 60 * 60 * 24));
                fineAmount = overdueDays * 0.1;
            }
            var actions = '';
            if (showActions && r.status === '借阅中') {
                actions = '<button class="btn btn-success btn-sm" onclick="returnBook(' + r.recordId + ')"><i class="bi bi-arrow-return-left me-1"></i>归还</button>';
                if (r.renewCount < 1) actions += ' <button class="btn btn-outline-secondary btn-sm" onclick="renewBook(' + r.recordId + ')"><i class="bi bi-arrow-repeat me-1"></i>续借</button>';
            }
            // 历史记录中有罚款可缴纳
            if (!showActions && (r.fineAmount > 0)) {
                actions = '<button class="btn btn-outline-warning btn-sm" onclick="payFineRecord(' + r.recordId + ')"><i class="bi bi-cash-coin me-1"></i>缴纳罚款</button>';
            }
            if (showActions) {
                tbody.innerHTML +=
                    '<tr><td><strong>' + escapeHtml(r.bookTitle || '-') + '</strong></td>' +
                    '<td>' + (r.borrowDate || '-') + '</td><td>' + (r.dueDate || '-') + '</td>' +
                    '<td>' + (fineAmount > 0 ? '<span class="text-danger fw-bold">¥' + fineAmount.toFixed(2) + '</span>' : '-') + '</td>' +
                    '<td>' + badge + '</td><td class="text-center">' + (actions || '-') + '</td></tr>';
            } else {
                tbody.innerHTML +=
                    '<tr><td><strong>' + escapeHtml(r.bookTitle || '-') + '</strong></td>' +
                    '<td>' + (r.borrowDate || '-') + '</td><td>' + (r.dueDate || '-') + '</td>' +
                    '<td>' + (r.returnDate || '-') + '</td>' +
                    '<td>' + (fineAmount > 0 ? '<span class="text-danger fw-bold">¥' + fineAmount.toFixed(2) + '</span>' : '-') + '</td>' +
                    '<td>' + badge + '</td><td class="text-center">' + (actions || '-') + '</td></tr>';
            }
        });
    }

    async function returnBook(recordId) {
        showConfirm('确定要归还这本书吗？', async function() {
            var result = await API.returnBook(recordId);
            if (result.success) { showToast('归还成功', 'success'); loadActiveBorrows(); }
            else showToast(result.message || '归还失败', 'danger');
        });
    }

    async function renewBook(recordId) {
        showConfirm('确定要续借这本书吗？', async function() {
            var result = await API.renewBook(recordId);
            if (result.success) { showToast('续借成功，期限延长31天', 'success'); loadActiveBorrows(); }
            else showToast(result.message || '续借失败', 'danger');
        });
    }

    document.addEventListener('shown.bs.tab', function(e) {
        var targetId = e.target.getAttribute('data-bs-target');
        if (targetId === '#historyBorrowTab') loadBorrowHistory();
        if (targetId === '#allBorrowTab') loadAllBorrowRecords();
        if (targetId === '#allFinesTab') loadAllFines();
    });
    </script>
</body>
</html>
