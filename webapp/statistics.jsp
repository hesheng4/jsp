<%@ include file="WEB-INF/includes/auth-check.jspf" %>
<%@ page import="model.User" %>
<%
    request.setAttribute("currentPage", "statistics");
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
    <title>数据统计 - 图书借阅管理系统</title>
    <style>
        .stat-card { border: none; border-radius: 12px; transition: transform 0.2s; }
        .stat-card:hover { transform: translateY(-3px); }
        .stat-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 1.4rem; }
        .stat-value { font-size: 1.8rem; font-weight: 700; line-height: 1.2; }
        .stat-label { font-size: 0.85rem; color: #6c757d; }
        .rank-item { display: flex; align-items: center; padding: 10px 0; border-bottom: 1px solid #f0f0f0; }
        .rank-item:last-child { border-bottom: none; }
        .rank-num { width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 0.8rem; font-weight: 700; margin-right: 12px; flex-shrink: 0; }
        .rank-1 { background: linear-gradient(135deg, #f6d365, #fda085); color: #fff; }
        .rank-2 { background: linear-gradient(135deg, #a8caba, #5d4157); color: #fff; }
        .rank-3 { background: linear-gradient(135deg, #c2e9fb, #a1c4fd); color: #333; }
        .rank-other { background: #e9ecef; color: #6c757d; }
        .bar-chart { display: flex; align-items: flex-end; gap: 6px; height: 160px; padding-top: 10px; }
        .bar-col { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: flex-end; }
        .bar { width: 100%; border-radius: 4px 4px 0 0; min-height: 2px; transition: height 0.5s; }
        .bar-label { font-size: 0.7rem; color: #6c757d; margin-top: 4px; }
        .bar-value { font-size: 0.7rem; font-weight: 600; margin-bottom: 2px; }
    </style>
</head>
<body>
    <%@ include file="WEB-INF/includes/navbar-sidebar.jspf" %>

    <main class="main-content-area">
        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h2 class="h4 fw-bold"><i class="bi bi-bar-chart-line me-2"></i>数据统计</h2>
            <select class="form-select form-select-sm w-auto" id="yearSelect" onchange="loadMonthlyTrend()">
            </select>
        </div>

        <!-- 概览卡片 -->
        <div class="row g-3 mb-4" id="overviewCards">
            <div class="col-6 col-md-3"><div class="card stat-card shadow-sm p-3"><div class="d-flex align-items-center"><div class="stat-icon bg-primary bg-opacity-10 text-primary me-3"><i class="bi bi-book"></i></div><div><div class="stat-value" id="statTotalBooks">-</div><div class="stat-label">图书总册数</div></div></div></div></div>
            <div class="col-6 col-md-3"><div class="card stat-card shadow-sm p-3"><div class="d-flex align-items-center"><div class="stat-icon bg-success bg-opacity-10 text-success me-3"><i class="bi bi-people"></i></div><div><div class="stat-value" id="statTotalUsers">-</div><div class="stat-label">注册用户</div></div></div></div></div>
            <div class="col-6 col-md-3"><div class="card stat-card shadow-sm p-3"><div class="d-flex align-items-center"><div class="stat-icon bg-info bg-opacity-10 text-info me-3"><i class="bi bi-journal-check"></i></div><div><div class="stat-value" id="statActiveBorrows">-</div><div class="stat-label">借阅中</div></div></div></div></div>
            <div class="col-6 col-md-3"><div class="card stat-card shadow-sm p-3"><div class="d-flex align-items-center"><div class="stat-icon bg-warning bg-opacity-10 text-warning me-3"><i class="bi bi-exclamation-triangle"></i></div><div><div class="stat-value" id="statOverdueCount">-</div><div class="stat-label">逾期</div></div></div></div></div>
            <div class="col-6 col-md-3"><div class="card stat-card shadow-sm p-3"><div class="d-flex align-items-center"><div class="stat-icon bg-secondary bg-opacity-10 text-secondary me-3"><i class="bi bi-calendar-check"></i></div><div><div class="stat-value" id="statTodayBorrows">-</div><div class="stat-label">今日借阅</div></div></div></div></div>
            <div class="col-6 col-md-3"><div class="card stat-card shadow-sm p-3"><div class="d-flex align-items-center"><div class="stat-icon bg-danger bg-opacity-10 text-danger me-3"><i class="bi bi-cash-coin"></i></div><div><div class="stat-value" id="statUnpaidFines">-</div><div class="stat-label">未缴罚款</div></div></div></div></div>
            <div class="col-6 col-md-3"><div class="card stat-card shadow-sm p-3"><div class="d-flex align-items-center"><div class="stat-icon bg-primary bg-opacity-10 text-primary me-3"><i class="bi bi-bookshelf"></i></div><div><div class="stat-value" id="statBookTypes">-</div><div class="stat-label">图书种类</div></div></div></div></div>
            <div class="col-6 col-md-3"><div class="card stat-card shadow-sm p-3"><div class="d-flex align-items-center"><div class="stat-icon bg-success bg-opacity-10 text-success me-3"><i class="bi bi-box-arrow-in-down"></i></div><div><div class="stat-value" id="statAvailableBooks">-</div><div class="stat-label">可借图书</div></div></div></div></div>
        </div>

        <div class="row g-4 mb-4">
            <!-- 热门图书排行 -->
            <div class="col-lg-6">
                <div class="card border-0 shadow-sm rounded-3">
                    <div class="card-header bg-white border-bottom fw-bold"><i class="bi bi-fire me-2 text-danger"></i>热门图书 TOP10</div>
                    <div class="card-body p-3" id="popularBooksList"><p class="text-center text-muted py-3">加载中...</p></div>
                </div>
            </div>
            <!-- 用户借阅排行 -->
            <div class="col-lg-6">
                <div class="card border-0 shadow-sm rounded-3">
                    <div class="card-header bg-white border-bottom fw-bold"><i class="bi bi-trophy me-2 text-warning"></i>借阅达人 TOP10</div>
                    <div class="card-body p-3" id="topBorrowersList"><p class="text-center text-muted py-3">加载中...</p></div>
                </div>
            </div>
        </div>

        <div class="row g-4 mb-4">
            <!-- 分类借阅统计 -->
            <div class="col-lg-6">
                <div class="card border-0 shadow-sm rounded-3">
                    <div class="card-header bg-white border-bottom fw-bold"><i class="bi bi-pie-chart me-2 text-primary"></i>分类借阅统计</div>
                    <div class="card-body p-3" id="categoryStatsList"><p class="text-center text-muted py-3">加载中...</p></div>
                </div>
            </div>
            <!-- 月度借阅趋势 -->
            <div class="col-lg-6">
                <div class="card border-0 shadow-sm rounded-3">
                    <div class="card-header bg-white border-bottom fw-bold"><i class="bi bi-graph-up me-2 text-success"></i>月度借阅趋势</div>
                    <div class="card-body p-3" id="monthlyTrendChart"><p class="text-center text-muted py-3">加载中...</p></div>
                </div>
            </div>
        </div>

        <!-- 年度借阅趋势 -->
        <div class="card border-0 shadow-sm rounded-3 mb-4">
            <div class="card-header bg-white border-bottom fw-bold"><i class="bi bi-calendar-range me-2 text-info"></i>年度借阅趋势</div>
            <div class="card-body p-3" id="yearlyTrendChart"><p class="text-center text-muted py-3">加载中...</p></div>
        </div>
    </main>

    <div id="toast" class="toast align-items-center border-0 position-fixed top-0 start-50 translate-middle-x mt-3" role="alert" style="z-index:9999;">
        <div class="d-flex"><div class="toast-body" id="toastMessage"></div><button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>
    </div>

    <%@ include file="WEB-INF/includes/scripts.jspf" %>
    <script>
    var currentYear = new Date().getFullYear();

    document.addEventListener('DOMContentLoaded', function() {
        initYearSelect();
        loadOverview();
        loadPopularBooks();
        loadTopBorrowers();
        loadCategoryStatistics();
        loadMonthlyTrend();
        loadYearlyTrend();
    });

    function initYearSelect() {
        var sel = document.getElementById('yearSelect');
        var thisYear = new Date().getFullYear();
        for (var y = thisYear; y >= thisYear - 5; y--) {
            sel.innerHTML += '<option value="' + y + '"' + (y === currentYear ? ' selected' : '') + '>' + y + '年</option>';
        }
    }

    // ========== 概览 ==========
    async function loadOverview() {
        var result = await API.getStatisticsOverview();
        if (result.totalBooks !== undefined) {
            document.getElementById('statTotalBooks').textContent = result.totalBooks || 0;
            document.getElementById('statTotalUsers').textContent = result.totalUsers || 0;
            document.getElementById('statActiveBorrows').textContent = result.activeBorrows || 0;
            document.getElementById('statOverdueCount').textContent = result.overdueCount || 0;
            document.getElementById('statTodayBorrows').textContent = result.todayBorrows || 0;
            document.getElementById('statUnpaidFines').textContent = '¥' + (result.unpaidFines || 0).toFixed(2);
            document.getElementById('statBookTypes').textContent = result.bookTypes || 0;
            document.getElementById('statAvailableBooks').textContent = result.availableBooks || 0;
        }
    }

    // ========== 热门图书 ==========
    async function loadPopularBooks() {
        var result = await API.getPopularBooks();
        var container = document.getElementById('popularBooksList');
        if (!result.success || !result.data || result.data.length === 0) {
            container.innerHTML = '<p class="text-center text-muted py-3">暂无数据</p>';
            return;
        }
        var html = '';
        result.data.forEach(function(item, i) {
            var rankClass = i < 3 ? 'rank-' + (i + 1) : 'rank-other';
            html += '<div class="rank-item">' +
                '<div class="rank-num ' + rankClass + '">' + (i + 1) + '</div>' +
                '<div class="flex-grow-1">' +
                '<div class="fw-bold small">' + escapeHtml(item.title) + '</div>' +
                '<div class="text-muted" style="font-size:0.8rem;">' + escapeHtml(item.author || '-') + '</div>' +
                '</div>' +
                '<span class="badge bg-primary">' + item.borrowCount + ' 次</span>' +
                '</div>';
        });
        container.innerHTML = html;
    }

    // ========== 借阅达人 ==========
    async function loadTopBorrowers() {
        var result = await API.getTopBorrowers();
        var container = document.getElementById('topBorrowersList');
        if (!result.success || !result.data || result.data.length === 0) {
            container.innerHTML = '<p class="text-center text-muted py-3">暂无数据</p>';
            return;
        }
        var html = '';
        result.data.forEach(function(item, i) {
            var rankClass = i < 3 ? 'rank-' + (i + 1) : 'rank-other';
            html += '<div class="rank-item">' +
                '<div class="rank-num ' + rankClass + '">' + (i + 1) + '</div>' +
                '<div class="flex-grow-1">' +
                '<div class="fw-bold small">' + escapeHtml(item.username) + '</div>' +
                '<div class="text-muted" style="font-size:0.8rem;">账号: ' + item.accountId + '</div>' +
                '</div>' +
                '<span class="badge bg-success">' + item.borrowCount + ' 次</span>' +
                '</div>';
        });
        container.innerHTML = html;
    }

    // ========== 分类统计 ==========
    async function loadCategoryStatistics() {
        var result = await API.getCategoryStatistics();
        var container = document.getElementById('categoryStatsList');
        if (!result.success || !result.data || result.data.length === 0) {
            container.innerHTML = '<p class="text-center text-muted py-3">暂无数据</p>';
            return;
        }
        var maxCount = Math.max.apply(null, result.data.map(function(d) { return d.borrowCount; }));
        var html = '';
        result.data.forEach(function(item) {
            var pct = maxCount > 0 ? (item.borrowCount / maxCount * 100) : 0;
            html += '<div class="mb-2">' +
                '<div class="d-flex justify-content-between mb-1">' +
                '<span class="small fw-bold">' + escapeHtml(item.categoryName || '未分类') + '</span>' +
                '<span class="text-muted small">' + item.borrowCount + ' 次</span>' +
                '</div>' +
                '<div class="progress" style="height: 8px;">' +
                '<div class="progress-bar bg-primary" style="width: ' + pct + '%"></div>' +
                '</div></div>';
        });
        container.innerHTML = html;
    }

    // ========== 月度趋势 ==========
    async function loadMonthlyTrend() {
        var year = document.getElementById('yearSelect').value;
        var result = await API.getMonthlyTrend(year);
        var container = document.getElementById('monthlyTrendChart');
        if (!result.success || !result.data || result.data.length === 0) {
            container.innerHTML = '<p class="text-center text-muted py-3">暂无数据</p>';
            return;
        }
        var monthMap = {};
        result.data.forEach(function(d) { monthMap[d.month] = d.borrowCount; });
        var maxCount = Math.max.apply(null, result.data.map(function(d) { return d.borrowCount; }));
        var html = '<div class="bar-chart">';
        for (var m = 1; m <= 12; m++) {
            var count = monthMap[m] || 0;
            var h = maxCount > 0 ? (count / maxCount * 140) : 0;
            html += '<div class="bar-col">' +
                '<div class="bar-value">' + (count > 0 ? count : '') + '</div>' +
                '<div class="bar" style="height:' + h + 'px; background: linear-gradient(180deg, #667eea, #764ba2);"></div>' +
                '<div class="bar-label">' + m + '月</div>' +
                '</div>';
        }
        html += '</div>';
        container.innerHTML = html;
    }

    // ========== 年度趋势 ==========
    async function loadYearlyTrend() {
        var result = await API.getYearlyTrend();
        var container = document.getElementById('yearlyTrendChart');
        if (!result.success || !result.data || result.data.length === 0) {
            container.innerHTML = '<p class="text-center text-muted py-3">暂无数据</p>';
            return;
        }
        var maxCount = Math.max.apply(null, result.data.map(function(d) { return d.borrowCount; }));
        var html = '<div class="bar-chart" style="height:140px;">';
        result.data.forEach(function(item) {
            var h = maxCount > 0 ? (item.borrowCount / maxCount * 120) : 0;
            html += '<div class="bar-col">' +
                '<div class="bar-value">' + item.borrowCount + '</div>' +
                '<div class="bar" style="height:' + h + 'px; background: linear-gradient(180deg, #11998e, #38ef7d);"></div>' +
                '<div class="bar-label">' + item.year + '</div>' +
                '</div>';
        });
        html += '</div>';
        container.innerHTML = html;
    }
    </script>
</body>
</html>
