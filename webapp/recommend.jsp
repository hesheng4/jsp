<%@ include file="WEB-INF/includes/auth-check.jspf" %>
<%
    request.setAttribute("currentPage", "recommend");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <%@ include file="WEB-INF/includes/head.jspf" %>
    <title>图书推荐 - 图书借阅管理系统</title>
    <style>
        .rec-card { transition: transform 0.2s, box-shadow 0.2s; border: none; }
        .rec-card:hover { transform: translateY(-4px); box-shadow: 0 8px 25px rgba(0,0,0,0.12) !important; }
        .rec-section-title { border-left: 4px solid #667eea; padding-left: 12px; }
        .rec-badge { font-size: 0.75rem; }
        .rec-empty { color: #adb5bd; padding: 40px 0; }
    </style>
</head>
<body>
    <%@ include file="WEB-INF/includes/navbar-sidebar.jspf" %>

    <main class="main-content-area">
        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h2 class="h4 fw-bold"><i class="bi bi-stars me-2"></i>图书推荐</h2>
        </div>

        <!-- 个性化推荐 -->
        <section class="mb-5" id="personalSection">
            <h5 class="rec-section-title fw-bold mb-3"><i class="bi bi-person-heart me-2"></i>为你推荐</h5>
            <p class="text-muted small mb-3">根据你的借阅历史，为你推荐可能感兴趣的图书</p>
            <div id="personalRec" class="row g-3"></div>
        </section>

        <!-- 高评分图书 -->
        <section class="mb-5" id="highRatedSection">
            <h5 class="rec-section-title fw-bold mb-3"><i class="bi bi-star-fill text-warning me-2"></i>高评分图书</h5>
            <p class="text-muted small mb-3">读者好评如潮的经典图书</p>
            <div id="highRatedRec" class="row g-3"></div>
        </section>

        <!-- 新书上架 -->
        <section class="mb-5" id="newBooksSection">
            <h5 class="rec-section-title fw-bold mb-3"><i class="bi bi-clock-history me-2"></i>新书上架</h5>
            <p class="text-muted small mb-3">最新入库的图书</p>
            <div id="newBooksRec" class="row g-3"></div>
        </section>
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
        loadPersonalRecommendations();
        loadHighRatedBooks();
        loadNewBooks();
    });

    async function loadPersonalRecommendations() {
        var result = await API.getPersonalRecommendations();
        var container = document.getElementById('personalRec');
        if (!result.success || !result.data || result.data.length === 0) {
            container.innerHTML = '<div class="col-12 rec-empty text-center"><i class="bi bi-inbox fs-1 d-block mb-2"></i>暂无推荐，去借阅几本书吧！</div>';
            return;
        }
        renderBookCards(container, result.data);
    }

    async function loadHighRatedBooks() {
        var result = await API.getHighRatedBooks();
        var container = document.getElementById('highRatedRec');
        if (!result.success || !result.data || result.data.length === 0) {
            container.innerHTML = '<div class="col-12 rec-empty text-center"><i class="bi bi-inbox fs-1 d-block mb-2"></i>暂无高评分图书</div>';
            return;
        }
        renderBookCards(container, result.data);
    }

    async function loadNewBooks() {
        var result = await API.getNewBooks();
        var container = document.getElementById('newBooksRec');
        if (!result.success || !result.data || result.data.length === 0) {
            container.innerHTML = '<div class="col-12 rec-empty text-center"><i class="bi bi-inbox fs-1 d-block mb-2"></i>暂无新书</div>';
            return;
        }
        renderBookCards(container, result.data);
    }

    function renderBookCards(container, books) {
        container.innerHTML = '';
        books.forEach(function(book) {
            var available = book.availableCopies > 0;
            var statusBadge = available
                ? '<span class="badge bg-success rec-badge">可借 ' + book.availableCopies + ' 册</span>'
                : '<span class="badge bg-secondary rec-badge">已借完</span>';
            var actionBtn = available
                ? '<button class="btn btn-primary btn-sm" onclick="borrowBook(' + book.bookId + ')"><i class="bi bi-bookmark-plus me-1"></i>借阅</button>'
                : '<button class="btn btn-outline-info btn-sm" onclick="reserveBook(' + book.bookId + ')"><i class="bi bi-calendar-check me-1"></i>预约</button>';
            container.innerHTML +=
                '<div class="col-sm-6 col-md-4 col-lg-3">' +
                '<div class="card rec-card shadow-sm rounded-3 h-100">' +
                '<div class="card-body d-flex flex-column">' +
                '<h6 class="card-title fw-bold mb-1">' + escapeHtml(book.title) + '</h6>' +
                '<p class="text-muted small mb-1"><i class="bi bi-person me-1"></i>' + escapeHtml(book.author || '未知作者') + '</p>' +
                '<p class="text-muted small mb-2"><i class="bi bi-building me-1"></i>' + escapeHtml(book.publisher || '未知出版社') + '</p>' +
                '<div class="mb-2">' + statusBadge +
                (book.categoryName ? ' <span class="badge bg-light text-dark rec-badge">' + escapeHtml(book.categoryName) + '</span>' : '') +
                '</div>' +
                '<div class="mt-auto d-flex gap-1">' +
                '<a class="btn btn-outline-secondary btn-sm flex-grow-1" href="book-detail.jsp?bookId=' + book.bookId + '"><i class="bi bi-info-circle me-1"></i>详情</a>' +
                actionBtn +
                '</div>' +
                '</div></div></div>';
        });
    }

    async function borrowBook(bookId) {
        showConfirm('确定要借阅这本书吗？', async function() {
            var result = await API.borrowBook(bookId);
            if (result.success) { showToast('借阅成功', 'success'); loadPersonalRecommendations(); loadHighRatedBooks(); loadNewBooks(); }
            else showToast(result.message || '借阅失败', 'danger');
        });
    }

    async function reserveBook(bookId) {
        showConfirm('图书已借完，确定要预约排队吗？', async function() {
            var result = await API.reserveBook(bookId);
            if (result.success) { showToast('预约成功，排队等待中', 'success'); }
            else showToast(result.message || '预约失败', 'danger');
        });
    }
    </script>
</body>
</html>
