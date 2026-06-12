<%@ include file="WEB-INF/includes/auth-check.jspf" %>
<%
    request.setAttribute("currentPage", "my-reviews");
%>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <%@ include file="WEB-INF/includes/head.jspf" %>
    <title>我的评论 - 图书借阅管理系统</title>
    <style>
        .star-display { color: #ffc107; font-size: 1rem; }
        .star-display .bi-star { color: #ddd; }
        .review-card { border-left: 3px solid #667eea; }
    </style>
</head>
<body>
    <%@ include file="WEB-INF/includes/navbar-sidebar.jspf" %>

    <main class="main-content-area">
        <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
            <h2 class="h4 fw-bold"><i class="bi bi-chat-dots me-2"></i>我的评论</h2>
        </div>
        <div id="reviewsList"></div>
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
    document.addEventListener('DOMContentLoaded', loadMyReviews);

    async function loadMyReviews() {
        var result = await API.getMyReviews();
        var container = document.getElementById('reviewsList');
        if (!result.success || !result.data || result.data.length === 0) {
            container.innerHTML = '<div class="text-center text-muted py-5"><i class="bi bi-chat-dots fs-1 d-block mb-3"></i><p>你还没有发表过评论</p><a href="books.jsp" class="btn btn-primary btn-sm">去图书查询</a></div>';
            return;
        }
        container.innerHTML = '';
        result.data.forEach(function(r) {
            var date = r.reviewDate ? r.reviewDate.substring(0, 10) : '-';
            container.innerHTML +=
                '<div class="card border-0 shadow-sm rounded-3 mb-3 review-card">' +
                '<div class="card-body">' +
                '<div class="d-flex justify-content-between align-items-start">' +
                '<div><h6 class="fw-bold mb-1">' + escapeHtml(r.bookTitle || '未知图书') + '</h6>' +
                '<span class="star-display">' + buildStars(r.rating) + '</span> ' +
                '<small class="text-muted ms-2">' + date + '</small></div>' +
                '<div class="d-flex gap-1">' +
                '<a class="btn btn-outline-secondary btn-sm" href="book-detail.jsp?bookId=' + r.bookId + '"><i class="bi bi-eye me-1"></i>查看</a>' +
                '<button class="btn btn-outline-danger btn-sm" onclick="deleteReview(' + r.reviewId + ')"><i class="bi bi-trash"></i></button>' +
                '</div></div>' +
                '<p class="mt-2 mb-0 text-muted">' + escapeHtml(r.content || '(无文字)') + '</p>' +
                '</div></div>';
        });
    }

    function buildStars(rating) {
        var html = '';
        for (var i = 1; i <= 5; i++) {
            html += i <= rating ? '<i class="bi bi-star-fill"></i>' : '<i class="bi bi-star"></i>';
        }
        return html;
    }

    function deleteReview(reviewId) {
        showConfirm('确定删除此评论？', async function() {
            var r = await API.deleteReview(reviewId);
            if (r.success) { showToast('评论已删除', 'success'); loadMyReviews(); }
            else showToast(r.message || '删除失败', 'danger');
        });
    }
    </script>
</body>
</html>
