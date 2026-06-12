package servlet;

import dao.RecommendationDAO;
import model.Book;
import model.User;
import util.AuthUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书推荐控制器
 */
@WebServlet("/api/recommend/*")
public class RecommendationServlet extends HttpServlet {
    private RecommendationDAO recommendationDAO = new RecommendationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String pathInfo = request.getPathInfo();

        User user = AuthUtil.requireLogin(request, response);
        if (user == null) return;

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/personal")) {
            getPersonalRecommendations(response, user.getAccountId());
        } else if (pathInfo.equals("/highRated")) {
            getHighRatedBooks(response);
        } else if (pathInfo.equals("/new")) {
            getNewBooks(response);
        } else if (pathInfo.startsWith("/category/")) {
            try {
                Integer categoryId = Integer.parseInt(pathInfo.substring(10));
                getPopularByCategory(response, categoryId);
            } catch (NumberFormatException e) {
                sendError(response, "无效的分类ID");
            }
        } else {
            sendError(response, "无效的请求路径");
        }
    }

    private void getPersonalRecommendations(HttpServletResponse response, Long accountId) throws IOException {
        List<Book> books = recommendationDAO.getRecommendationsByHistory(accountId, 10);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", books.stream().map(this::bookToMap).toArray());
        sendJson(response, result);
    }

    private void getHighRatedBooks(HttpServletResponse response) throws IOException {
        List<Book> books = recommendationDAO.getHighRatedBooks(10);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", books.stream().map(this::bookToMap).toArray());
        sendJson(response, result);
    }

    private void getNewBooks(HttpServletResponse response) throws IOException {
        List<Book> books = recommendationDAO.getNewBooks(10);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", books.stream().map(this::bookToMap).toArray());
        sendJson(response, result);
    }

    private void getPopularByCategory(HttpServletResponse response, Integer categoryId) throws IOException {
        List<Book> books = recommendationDAO.getPopularByCategory(categoryId, 10);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", books.stream().map(this::bookToMap).toArray());
        sendJson(response, result);
    }

    private Map<String, Object> bookToMap(Book book) {
        Map<String, Object> map = new HashMap<>();
        map.put("bookId", book.getBookId());
        map.put("title", book.getTitle());
        map.put("author", book.getAuthor());
        map.put("isbn", book.getIsbn());
        map.put("categoryId", book.getCategoryId());
        map.put("categoryName", book.getCategoryName());
        map.put("totalCopies", book.getTotalCopies());
        map.put("availableCopies", book.getAvailableCopies());
        map.put("publisher", book.getPublisher());
        map.put("publishDate", book.getPublishDate());
        return map;
    }

    private void sendJson(HttpServletResponse response, Map<String, Object> data) throws IOException {
        PrintWriter out = response.getWriter();
        out.print(util.JsonUtil.toJson(data));
        out.flush();
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        sendJson(response, result);
    }
}
