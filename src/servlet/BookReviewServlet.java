package servlet;

import dao.BookDAO;
import dao.BookReviewDAO;
import model.Book;
import model.BookReview;
import model.User;
import util.AuthUtil;
import util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书评论控制器
 */
@WebServlet("/api/review/*")
public class BookReviewServlet extends HttpServlet {
    private BookReviewDAO reviewDAO = new BookReviewDAO();
    private BookDAO bookDAO = new BookDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(response, "请指定图书ID");
            return;
        }

        // /api/review/book/{bookId}
        if (pathInfo.startsWith("/book/")) {
            try {
                Integer bookId = Integer.parseInt(pathInfo.substring(6));
                getBookReviews(response, bookId);
            } catch (NumberFormatException e) {
                sendError(response, "无效的图书ID");
            }
        }
        // /api/review/user
        else if (pathInfo.equals("/user")) {
            getUserReviews(request, response);
        }
        else {
            sendError(response, "无效的请求路径");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // 添加或更新评论
            addOrUpdateReview(request, response);
        } else {
            sendError(response, "无效的请求路径");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String pathInfo = request.getPathInfo();

        if (pathInfo != null && pathInfo.length() > 1) {
            try {
                Integer reviewId = Integer.parseInt(pathInfo.substring(1));
                deleteReview(request, response, reviewId);
            } catch (NumberFormatException e) {
                sendError(response, "无效的评论ID");
            }
        } else {
            sendError(response, "请指定评论ID");
        }
    }

    /**
     * 获取图书的评论列表和平均评分
     */
    private void getBookReviews(HttpServletResponse response, Integer bookId) throws IOException {
        List<BookReview> reviews = reviewDAO.getBookReviews(bookId);
        Double avgRating = reviewDAO.getBookAverageRating(bookId);
        Book book = bookDAO.getBookById(bookId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("book", book != null ? bookToMap(book) : null);
        result.put("avgRating", avgRating);
        result.put("data", reviews.stream().map(this::reviewToMap).toArray());
        sendJson(response, result);
    }

    /**
     * 获取当前用户的评论
     */
    private void getUserReviews(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = AuthUtil.requireLogin(request, response);
        if (user == null) return;

        List<BookReview> reviews = reviewDAO.getUserReviews(user.getAccountId());
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", reviews.stream().map(this::reviewToMap).toArray());
        sendJson(response, result);
    }

    /**
     * 添加或更新评论（星评分1-5 + 文字）
     */
    private void addOrUpdateReview(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = AuthUtil.requireLogin(request, response);
        if (user == null) return;

        Map<String, Object> params = JsonUtil.parseRequest(request);
        Integer bookId = JsonUtil.getInteger(params, "bookId");
        Integer rating = JsonUtil.getInteger(params, "rating");
        String content = (String) params.get("content");

        if (bookId == null) {
            sendError(response, "请指定图书");
            return;
        }
        if (rating == null || rating < 1 || rating > 5) {
            sendError(response, "评分必须在1-5之间");
            return;
        }

        boolean success = reviewDAO.addReview(user.getAccountId(), bookId, rating, content != null ? content : "");
        if (success) {
            AuthUtil.logOperation(request, "评论评分", "评论图书ID: " + bookId + " 评分: " + rating + "星");
            sendSuccess(response, "评论发布成功");
        } else {
            sendError(response, "评论失败，请确认您已借阅过该书");
        }
    }

    /**
     * 删除自己的评论
     */
    private void deleteReview(HttpServletRequest request, HttpServletResponse response, Integer reviewId) throws IOException {
        User user = AuthUtil.requireLogin(request, response);
        if (user == null) return;

        boolean success = reviewDAO.deleteReview(reviewId, user.getAccountId());
        if (success) {
            AuthUtil.logOperation(request, "删除评论", "删除评论ID: " + reviewId);
            sendSuccess(response, "评论删除成功");
        } else {
            sendError(response, "删除失败，只能删除自己的评论");
        }
    }

    private Map<String, Object> bookToMap(Book book) {
        Map<String, Object> map = new HashMap<>();
        map.put("bookId", book.getBookId());
        map.put("title", book.getTitle());
        map.put("author", book.getAuthor());
        map.put("isbn", book.getIsbn());
        map.put("categoryName", book.getCategoryName());
        map.put("publisher", book.getPublisher());
        map.put("publishDate", book.getPublishDate());
        map.put("availableCopies", book.getAvailableCopies());
        map.put("totalCopies", book.getTotalCopies());
        return map;
    }

    private Map<String, Object> reviewToMap(BookReview r) {
        Map<String, Object> map = new HashMap<>();
        map.put("reviewId", r.getReviewId());
        map.put("accountId", r.getAccountId());
        map.put("bookId", r.getBookId());
        map.put("username", r.getUsername());
        map.put("rating", r.getRating());
        map.put("content", r.getReviewContent());
        map.put("reviewDate", r.getReviewDate() != null ? r.getReviewDate().toString() : null);
        return map;
    }

    private void sendJson(HttpServletResponse response, Map<String, Object> data) throws IOException {
        PrintWriter out = response.getWriter();
        out.print(JsonUtil.toJson(data));
        out.flush();
    }

    private void sendSuccess(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        sendJson(response, result);
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        sendJson(response, result);
    }
}
