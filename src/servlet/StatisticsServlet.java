package servlet;

import dao.StatisticsDAO;
import model.User;
import util.AuthUtil;
import util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据统计控制器（管理员）
 */
@WebServlet("/api/statistics/*")
public class StatisticsServlet extends HttpServlet {
    private StatisticsDAO statisticsDAO = new StatisticsDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String pathInfo = request.getPathInfo();

        User user = AuthUtil.requireAdmin(request, response);
        if (user == null) return;

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/overview")) {
            getOverview(response);
        } else if (pathInfo.equals("/popularBooks")) {
            getPopularBooks(response);
        } else if (pathInfo.equals("/topBorrowers")) {
            getTopBorrowers(response);
        } else if (pathInfo.equals("/categoryStats")) {
            getCategoryStatistics(response);
        } else if (pathInfo.equals("/monthlyTrend")) {
            String yearStr = request.getParameter("year");
            int year = yearStr != null ? Integer.parseInt(yearStr) : java.time.Year.now().getValue();
            getMonthlyTrend(response, year);
        } else if (pathInfo.equals("/yearlyTrend")) {
            getYearlyTrend(response);
        } else {
            sendError(response, "无效的请求路径");
        }
    }

    private void getOverview(HttpServletResponse response) throws IOException {
        Map<String, Object> stats = statisticsDAO.getOverviewStatistics();
        stats.put("success", true);
        sendJson(response, stats);
    }

    private void getPopularBooks(HttpServletResponse response) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", statisticsDAO.getPopularBooks(10));
        sendJson(response, result);
    }

    private void getTopBorrowers(HttpServletResponse response) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", statisticsDAO.getTopBorrowers(10));
        sendJson(response, result);
    }

    private void getCategoryStatistics(HttpServletResponse response) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", statisticsDAO.getCategoryStatistics());
        sendJson(response, result);
    }

    private void getMonthlyTrend(HttpServletResponse response, int year) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("year", year);
        result.put("data", statisticsDAO.getMonthlyTrend(year));
        sendJson(response, result);
    }

    private void getYearlyTrend(HttpServletResponse response) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", statisticsDAO.getYearlyTrend());
        sendJson(response, result);
    }

    private void sendJson(HttpServletResponse response, Map<String, Object> data) throws IOException {
        PrintWriter out = response.getWriter();
        out.print(JsonUtil.toJson(data));
        out.flush();
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        sendJson(response, result);
    }
}
