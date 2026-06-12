package servlet;

import model.User;
import service.FineService;
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
 * 罚款扩展控制器 - 补缴等操作
 */
@WebServlet("/api/fineext/*")
public class FineExtServlet extends HttpServlet {
    private FineService fineService = new FineService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json;charset=UTF-8");

        User user = AuthUtil.requireLogin(request, response);
        if (user == null) return;

        if ("/payByRecord".equals(pathInfo)) {
            payByRecord(request, response, user);
        } else {
            sendError(response, "无效的请求路径");
        }
    }

    private void payByRecord(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        Map<String, Object> params = JsonUtil.parseRequest(request);
        Integer recordId = JsonUtil.getInteger(params, "recordId");

        if (recordId == null) {
            sendError(response, "请选择借阅记录");
            return;
        }

        String error = fineService.createAndPayByRecord(recordId, user.getAccountId());
        if (error == null) {
            AuthUtil.logOperation(request, "缴纳罚款", "通过借阅记录缴纳罚款: " + recordId);
            sendSuccess(response, "罚款已缴纳");
        } else {
            sendError(response, error);
        }
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
