package servlet;

import model.FineRecord;
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
import java.util.List;
import java.util.Map;

/**
 * 罚款控制器Servlet
 */
@WebServlet("/api/fine/*")
public class FineServlet extends HttpServlet {
    private FineService fineService = new FineService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json;charset=UTF-8");

        User user = AuthUtil.requireLogin(request, response);
        if (user == null) return;

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
            getUserFines(response, user.getAccountId());
        } else if (pathInfo.equals("/all")) {
            getAllUnpaidFines(request, response);
        } else if (pathInfo.equals("/allFines")) {
            getAllFines(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json;charset=UTF-8");

        User user = AuthUtil.requireLogin(request, response);
        if (user == null) return;

        if (pathInfo == null) {
            sendError(response, "无效的请求路径");
            return;
        }

        switch (pathInfo) {
            case "/pay":
                payFine(request, response, user);
                break;
            case "/payAll":
                payAllFines(request, response, user);
                break;
            case "/payByRecord":
                payByRecord(request, response, user);
                break;
            default:
                sendError(response, "无效的请求路径");
        }
    }

    private void payFine(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        Map<String, Object> params = JsonUtil.parseRequest(request);
        Integer fineId = JsonUtil.getInteger(params, "fineId");

        if (fineId == null) {
            sendError(response, "请选择要缴纳的罚款记录");
            return;
        }

        boolean success = fineService.payFine(fineId);
        if (success) {
            AuthUtil.logOperation(request, "缴纳罚款", "缴纳罚款记录ID: " + fineId);
            sendSuccess(response, "罚款缴纳成功");
        } else {
            sendError(response, "缴纳失败");
        }
    }

    private void payAllFines(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        boolean success = fineService.payAllFines(user.getAccountId());
        if (success) {
            AuthUtil.logOperation(request, "缴纳罚款", "批量缴纳所有罚款");
            sendSuccess(response, "全部罚款已缴纳");
        } else {
            sendError(response, "缴纳失败，可能没有未缴罚款");
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
            AuthUtil.logOperation(request, "缴纳罚款", "通过借阅记录缴纳罚款，recordId: " + recordId);
            sendSuccess(response, "罚款已缴纳");
        } else {
            sendError(response, error);
        }
    }

    private void getUserFines(HttpServletResponse response, Long accountId) throws IOException {
        List<FineRecord> records = fineService.getUserFines(accountId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", records.stream().map(this::recordToMap).toArray());
        sendJson(response, result);
    }

    private void getAllUnpaidFines(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AuthUtil.requireAdmin(request, response) == null) return;
        List<FineRecord> records = fineService.getAllUnpaidFines();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", records.stream().map(this::recordToMap).toArray());
        sendJson(response, result);
    }

    private void getAllFines(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AuthUtil.requireAdmin(request, response) == null) return;
        List<FineRecord> records = fineService.getAllFines();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", records.stream().map(this::recordToMap).toArray());
        sendJson(response, result);
    }

    private Map<String, Object> recordToMap(FineRecord f) {
        Map<String, Object> map = new HashMap<>();
        map.put("fineId", f.getFineId());
        map.put("accountId", f.getAccountId());
        map.put("recordId", f.getRecordId());
        map.put("username", f.getUsername());
        map.put("bookTitle", f.getBookTitle());
        map.put("fineAmount", f.getFineAmount());
        map.put("fineReason", f.getFineReason());
        map.put("createDate", f.getCreateDate() != null ? f.getCreateDate().toString() : null);
        map.put("payStatus", f.getPayStatus());
        map.put("payDate", f.getPayDate() != null ? f.getPayDate().toString() : null);
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
