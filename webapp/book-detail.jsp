<%@ include file="WEB-INF/includes/auth-check.jspf" %>
<%
    request.setAttribute("currentPage", "books");
    String bookIdParam = request.getParameter("bookId");
    if (bookIdParam == null || bookIdParam.isEmpty()) {
        response.sendRedirect(request.getContextPath() + "/books.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <%@ include file="WEB-INF/includes/head.jspf" %>
    <title>图书详情 - 图书借阅管理系统</title>
    <style>
        .star-rating { direction: rtl; display: inline-flex; font-size: 1.5rem; }
        .star-rating input { display: none; }
        .star-rating label { color: #ddd; cursor: pointer; padding: 0 2px; }
        .star-rating input:checked ~ label,
        .star-rating label:hover,
        .star-rating label:hover ~ label { color: #ffc107; }
        .star-display { color: #ffc107; font-size: 1.2rem; }
        .star-display .bi-star { color: #ddd; }
        .review-card { border-left: 3px solid #667eea; }
    </style>
</head>
<body>
    <%@ include file="WEB-INF/includes/navbar-sidebar.jspf" %>

    <main class="main-content-area">
        <nav aria-label="breadcrumb" class="pt-3">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="books.jsp">图书查询</a></li>
                <li class="breadcrumb-item active" id="breadcrumbTitle">图书详情</li>
            </ol>
        </nav>

        <!-- 图书信息卡片 -->
        <div class="card border-0 shadow-sm rounded-3 mb-4">
            <div class="card-body p-4">
                <div class="row align-items-center">
                    <div class="col-md-8">
                        <h3 class="fw-bold mb-2" id="detailTitle"></h3>
                        <p class="text-muted mb-1"><i class="bi bi-person me-2"></i>作者：<span id="detailAuthor"></span></p>
                        <p class="text-muted mb-1"><i class="bi bi-building me-2"></i>出版社：<span id="detailPublisher"></span></p>
                        <p class="text-muted mb-1"><i class="bi bi-tag me-2"></i>分类：<span id="detailCategory"></span></p>
                        <p class="text-muted mb-3"><i class="bi bi-upc me-2"></i>ISBN：<span id="detailIsbn"></span></p>
                        <span id="detailStock" class="badge fs-6"></span>
                    </div>
                    <div class="col-md-4 text-center">
                        <div class="mb-2">
                            <span class="star-display" id="avgStars"></span>
                        </div>
                        <div class="text-muted small" id="avgRatingText"></div>
                        <button class="btn btn-primary mt-3" id="borrowBtn" onclick="borrowFromDetail()">
                            <i class="bi bi-bookmark-plus me-1"></i>借阅
                        </button>
                    </div>
                </div>
            </div>
        </div>

        <!-- 发表评论 -->
        <div class="card border-0 shadow-sm rounded-3 mb-4">
            <div class="card-header bg-white border-bottom fw-bold">
                <i class="bi bi-pencil-square me-2"></i>我的评价
            </div>
            <div class="card-body">
                <form id="reviewForm">
                    <div class="mb-3">
                        <label class="form-label">评分</label>
                        <div class="star-rating" id="starRating">
                            <input type="radio" id="star5" name="rating" value="5"><label for="star5" title="5星"><i class="bi bi-star-fill"></i></label>
                            <input type="radio" id="star4" name="rating" value="4"><label for="star4" title="4星"><i class="bi bi-star-fill"></i></label>
                            <input type="radio" id="star3" name="rating" value="3"><label for="star3" title="3星"><i class="bi bi-star-fill"></i></label>
                            <input type="radio" id="star2" name="rating" value="2"><label for="star2" title="2星"><i class="bi bi-star-fill"></i></label>
                            <input type="radio" id="star1" name="rating" value="1"><label for="star1" title="1星"><i class="bi bi-star-fill"></i></label>
                        </div>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">评论</label>
                        <textarea class="form-control" id="reviewContent" rows="3" placeholder="分享你的阅读感受...（需借阅过该书）"></textarea>
                    </div>
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-send me-1"></i>发表评价
                    </button>
                </form>
            </div>
        </div>

        <!-- 评论列表 -->
        <div class="card border-0 shadow-sm rounded-3">
            <div class="card-header bg-white border-bottom d-flex justify-content-between align-items-center">
                <span class="fw-bold"><i class="bi bi-chat-dots me-2"></i>读者评论</span>
                <span class="badge bg-primary" id="reviewCount">0条</span>
            </div>
            <div class="card-body" id="reviewsList">
                <p class="text-center text-muted py-4">加载中...</p>
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
    var bookId = parseInt('<%= bookIdParam %>');
    var currentUserId = null;

    document.addEventListener('DOMContentLoaded', function() {
        loadBookDetail();
        document.getElementById('reviewForm').addEventListener('submit', submitReview);
    });

    async function loadBookDetail() {
        var result = await API.getBookReviews(bookId);
        if (!result.success) {
            showToast('加载失败', 'danger');
            return;
        }

        // 图书信息
        if (result.book) {
            var b = result.book;
            document.getElementById('detailTitle').textContent = b.title;
            document.getElementById('detailAuthor').textContent = b.author || '-';
            document.getElementById('detailPublisher').textContent = b.publisher || '-';
            document.getElementById('detailCategory').textContent = b.categoryName || '-';
            document.getElementById('detailIsbn').textContent = b.isbn || '-';
            document.getElementById('breadcrumbTitle').textContent = b.title;

            var available = b.availableCopies > 0;
            var stockEl = document.getElementById('detailStock');
            stockEl.textContent = '可借 ' + b.availableCopies + ' / 共 ' + b.totalCopies + ' 册';
            stockEl.className = 'badge fs-6 ' + (available ? 'bg-success' : 'bg-secondary');

            var borrowBtn = document.getElementById('borrowBtn');
            if (!available) {
                borrowBtn.innerHTML = '<i class="bi bi-calendar-check me-1"></i>预约';
                borrowBtn.onclick = function() { reserveFromDetail(); };
            }
        }

        // 平均评分
        var avgRating = result.avgRating || 0;
        document.getElementById('avgStars').innerHTML = buildStars(avgRating);
        document.getElementById('avgRatingText').textContent = avgRating > 0 ? avgRating.toFixed(1) + ' 分' : '暂无评分';

        // 评论列表
        var reviews = result.data || [];
        document.getElementById('reviewCount').textContent = reviews.length + '条';
        renderReviews(reviews);

        // 获取当前用户ID（用于判断是否是自己的评论）
        var userResult = await API.getCurrentUser();
        if (userResult.success && userResult.data) {
            currentUserId = userResult.data.accountId;
        }
    }

    function renderReviews(reviews) {
        var container = document.getElementById('reviewsList');
        if (reviews.length === 0) {
            container.innerHTML = '<p class="text-center text-muted py-4">暂无评论，成为第一个评价的人吧！</p>';
            return;
        }
        container.innerHTML = '';
        reviews.forEach(function(r) {
            var date = r.reviewDate ? r.reviewDate.substring(0, 10) : '-';
            var delBtn = '';
            if (currentUserId && currentUserId == r.accountId) {
                delBtn = '<button class="btn btn-outline-danger btn-sm" onclick="deleteMyReview(' + r.reviewId + ')"><i class="bi bi-trash"></i></button>';
            }
            container.innerHTML +=
                '<div class="review-card bg-light rounded-3 p-3 mb-3">' +
                '<div class="d-flex justify-content-between align-items-start">' +
                '<div>' +
                '<span class="fw-bold">' + escapeHtml(r.username || '匿名') + '</span>' +
                '<span class="star-display ms-2">' + buildStars(r.rating) + '</span>' +
                '<small class="text-muted ms-2">' + date + '</small>' +
                '</div>' +
                delBtn +
                '</div>' +
                '<p class="mt-2 mb-0">' + escapeHtml(r.content || '(无文字)') + '</p>' +
                '</div>';
        });
    }

    async function submitReview(e) {
        e.preventDefault();
        var ratingInput = document.querySelector('input[name="rating"]:checked');
        if (!ratingInput) {
            showToast('请选择评分', 'warning');
            return;
        }
        var rating = parseInt(ratingInput.value);
        var content = document.getElementById('reviewContent').value;

        var btn = this.querySelector('button[type="submit"]');
        setBtnLoading(btn, true);
        var result = await API.addReview(bookId, rating, content);
        setBtnLoading(btn, false);

        if (result.success) {
            showToast('评价发布成功', 'success');
            document.getElementById('reviewContent').value = '';
            ratingInput.checked = false;
            loadBookDetail();
        } else {
            showToast(result.message || '评价失败', 'danger');
        }
    }

    async function deleteMyReview(reviewId) {
        showConfirm('确定删除此评价？', async function() {
            var result = await API.deleteReview(reviewId);
            if (result.success) {
                showToast('评价已删除', 'success');
                loadBookDetail();
            } else {
                showToast(result.message || '删除失败', 'danger');
            }
        });
    }

    function borrowFromDetail() {
        showConfirm('确定要借阅这本书吗？', async function() {
            var result = await API.borrowBook(bookId);
            if (result.success) {
                showToast('借阅成功', 'success');
                loadBookDetail();
            } else {
                showToast(result.message || '借阅失败', 'danger');
            }
        });
    }

    function reserveFromDetail() {
        console.log('[预约] 详情页点击预约, bookId=' + bookId);
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
                if (result.success) {
                    showToast('预约成功，排队等待中', 'success');
                    loadBookDetail();
                } else {
                    showToast(result.message || '预约失败', 'danger');
                }
            } catch (e) {
                console.error('[预约] 异常:', e);
                showToast('网络异常，请重试', 'danger');
            }
        });
    }

    function buildStars(rating) {
        var html = '';
        for (var i = 1; i <= 5; i++) {
            if (i <= Math.floor(rating)) {
                html += '<i class="bi bi-star-fill"></i>';
            } else if (i - 0.5 <= rating) {
                html += '<i class="bi bi-star-half"></i>';
            } else {
                html += '<i class="bi bi-star"></i>';
            }
        }
        return html;
    }
    </script>
</body>
</html>
