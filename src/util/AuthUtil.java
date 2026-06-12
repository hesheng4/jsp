package util;

import dao.OperationLogDAO;
import model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 认证授权工具类
 * 提供登录检查和权限检查的通用方法
 */
public class AuthUtil {

    /**
     * 要求用户已登录
     * @return User对象，未登录返回null并发送401错误
     */
    public static User requireLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "请先登录");
            return null;
        }
        return (User) session.getAttribute("user");
    }

    /**
     * 要求管理员权限
     * @return User对象（必须是管理员），否则返回null并发送403错误
     */
    public static User requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = requireLogin(request, response);
        if (user == null) return null;
        if (!user.isAdmin()) {
            sendError(response, HttpServletResponse.SC_FORBIDDEN, "权限不足，需要管理员权限");
            return null;
        }
        return user;
    }

    /**
     * 记录操作日志（从Session获取用户信息）
     */
    public static void logOperation(HttpServletRequest request, String type, String detail) {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) return;
            User user = (User) session.getAttribute("user");
            if (user == null) return;
            new OperationLogDAO().logOperation(user.getAccountId(), user.getUsername(), type, detail);
        } catch (Exception ignored) {}
    }

    /**
     * 记录操作日志（直接指定用户信息，用于注册等尚未登录的场景）
     */
    public static void logOperation(Long accountId, String username, String type, String detail) {
        try {
            new OperationLogDAO().logOperation(accountId, username, type, detail);
        } catch (Exception ignored) {}
    }

    private static void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.print("{\"success\":false,\"message\":\"" + message + "\"}");
        out.flush();
    }
}
