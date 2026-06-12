package servlet;

import dao.AuthorizationDAO;
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
import java.util.List;
import java.util.Map;

/**
 * 授权管理控制器（仅root用户）
 */
@WebServlet("/api/authorization/*")
public class AuthorizationServlet extends HttpServlet {
    private AuthorizationDAO authorizationDAO = new AuthorizationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String pathInfo = request.getPathInfo();

        User user = AuthUtil.requireAdmin(request, response);
        if (user == null) return;

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/pending")) {
            getPendingUsers(response);
        } else if (pathInfo.equals("/authorized")) {
            getAuthorizedUsers(response);
        } else if (pathInfo.equals("/admins")) {
            getAdminUsers(response);
        } else if (pathInfo.equals("/logs")) {
            getAuthorizationLogs(response);
        } else if (pathInfo.equals("/stats")) {
            getAuthorizationStats(response);
        } else if (pathInfo.equals("/isRoot")) {
            checkIsRoot(response, user.getAccountId());
        } else {
            sendError(response, "无效的请求路径");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        String pathInfo = request.getPathInfo();

        User user = AuthUtil.requireAdmin(request, response);
        if (user == null) return;

        Map<String, Object> params = JsonUtil.parseRequest(request);
        Long targetId = JsonUtil.getLong(params, "targetId");

        if (pathInfo == null) {
            sendError(response, "无效的请求路径");
            return;
        }

        switch (pathInfo) {
            case "/authorize":
                authorizeUser(response, user.getAccountId(), targetId);
                break;
            case "/revoke":
                revokeAuthorization(response, user.getAccountId(), targetId);
                break;
            case "/grantAdmin":
                grantAdmin(response, user.getAccountId(), targetId);
                break;
            case "/revokeAdmin":
                revokeAdmin(response, user.getAccountId(), targetId);
                break;
            default:
                sendError(response, "无效的请求路径");
        }
    }

    private void checkIsRoot(HttpServletResponse response, Long accountId) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("isRoot", authorizationDAO.isRoot(accountId));
        sendJson(response, result);
    }

    private void getPendingUsers(HttpServletResponse response) throws IOException {
        List<User> users = authorizationDAO.getPendingUsers();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", users.stream().map(this::userToMap).toArray());
        sendJson(response, result);
    }

    private void getAuthorizedUsers(HttpServletResponse response) throws IOException {
        List<User> users = authorizationDAO.getAuthorizedUsers();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", users.stream().map(this::userToMap).toArray());
        sendJson(response, result);
    }

    private void getAdminUsers(HttpServletResponse response) throws IOException {
        List<User> users = authorizationDAO.getAdminUsers();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", users.stream().map(this::userToMap).toArray());
        sendJson(response, result);
    }

    private void getAuthorizationLogs(HttpServletResponse response) throws IOException {
        List<Map<String, Object>> logs = authorizationDAO.getAuthorizationLogs(100);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", logs.toArray());
        sendJson(response, result);
    }

    private void getAuthorizationStats(HttpServletResponse response) throws IOException {
        Map<String, Integer> stats = authorizationDAO.getAuthorizationStats();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", stats);
        sendJson(response, result);
    }

    private void authorizeUser(HttpServletResponse response, Long rootId, Long targetId) throws IOException {
        if (targetId == null) { sendError(response, "请选择用户"); return; }
        boolean success = authorizationDAO.authorizeUser(rootId, targetId);
        if (success) {
            sendSuccess(response, "已授权启用");
        } else {
            sendError(response, "授权失败，可能非root用户或用户不存在");
        }
    }

    private void revokeAuthorization(HttpServletResponse response, Long rootId, Long targetId) throws IOException {
        if (targetId == null) { sendError(response, "请选择用户"); return; }
        boolean success = authorizationDAO.revokeAuthorization(rootId, targetId);
        if (success) {
            sendSuccess(response, "已回收授权");
        } else {
            sendError(response, "回收失败");
        }
    }

    private void grantAdmin(HttpServletResponse response, Long rootId, Long targetId) throws IOException {
        if (targetId == null) { sendError(response, "请选择用户"); return; }
        boolean success = authorizationDAO.grantAdmin(rootId, targetId);
        if (success) {
            sendSuccess(response, "已授予管理员权限");
        } else {
            sendError(response, "操作失败");
        }
    }

    private void revokeAdmin(HttpServletResponse response, Long rootId, Long targetId) throws IOException {
        if (targetId == null) { sendError(response, "请选择用户"); return; }
        boolean success = authorizationDAO.revokeAdmin(rootId, targetId);
        if (success) {
            sendSuccess(response, "已撤销管理员权限");
        } else {
            sendError(response, "操作失败");
        }
    }

    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("accountId", user.getAccountId());
        map.put("username", user.getUsername());
        map.put("phone", user.getPhone());
        map.put("isAdmin", user.isAdmin());
        map.put("isEnabled", user.isEnabled());
        map.put("isRoot", user.isRoot());
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
