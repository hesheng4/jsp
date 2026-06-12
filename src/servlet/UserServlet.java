package servlet;

import model.User;
import service.UserService;
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
 * 用户控制器Servlet
 */
@WebServlet("/api/user/*")
public class UserServlet extends HttpServlet {
    private UserService userService = new UserService();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json;charset=UTF-8");
        
        if (pathInfo == null || pathInfo.equals("/")) {
            // 获取所有用户
            getAllUsers(request, response);
        } else if (pathInfo.equals("/current")) {
            // 获取当前登录用户
            getCurrentUser(request, response);
        } else if (pathInfo.equals("/blacklist")) {
            // 获取黑名单用户
            getBlacklistedUsers(request, response);
        } else if (pathInfo.startsWith("/")) {
            // 根据ID获取用户
            getUserById(request, response, pathInfo.substring(1));
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json;charset=UTF-8");
        
        if (pathInfo == null) {
            sendError(response, "无效的请求路径");
            return;
        }
        
        switch (pathInfo) {
            case "/login":
                login(request, response);
                break;
            case "/register":
                register(request, response);
                break;
            case "/logout":
                logout(request, response);
                break;
            case "/updatePassword":
                updatePassword(request, response);
                break;
            case "/updateInfo":
                updateUserInfo(request, response);
                break;
            case "/setAdmin":
                setAdmin(request, response);
                break;
            case "/addBlacklist":
                addToBlacklist(request, response);
                break;
            case "/removeBlacklist":
                removeFromBlacklist(request, response);
                break;
            case "/verify":
                verifyUser(request, response);
                break;
            case "/resetPassword":
                resetPassword(request, response);
                break;
            default:
                sendError(response, "无效的请求路径");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 删除用户需要管理员权限
        if (AuthUtil.requireAdmin(request, response) == null) return;
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json;charset=UTF-8");
        
        if (pathInfo != null && pathInfo.length() > 1) {
            deleteUser(request, response, pathInfo.substring(1));
        } else {
            sendError(response, "无效的用户ID");
        }
    }
    
    private void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> params = JsonUtil.parseRequest(request);
        Long accountId = JsonUtil.getLong(params, "accountId");
        String password = (String) params.get("password");

        // 验证码校验
        Integer captchaAnswer = JsonUtil.getInteger(params, "captcha");
        if (captchaAnswer == null || !CaptchaServlet.verify(request, captchaAnswer)) {
            sendError(response, "验证码错误，请刷新后重试");
            return;
        }

        User user = userService.login(accountId, password);
        if (user != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            AuthUtil.logOperation(request, "登录", "用户登录成功");

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("data", userToMap(user));
            sendJson(response, result);
        } else {
            sendError(response, "账号或密码错误");
        }
    }
    
    private void register(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> params = JsonUtil.parseRequest(request);
        Long accountId = JsonUtil.getLong(params, "accountId");
        String username = (String) params.get("username");
        String password = (String) params.get("password");
        String phone = (String) params.get("phone");
        
        // 验证账号格式
        if (accountId == null || accountId.toString().length() != 12) {
            sendError(response, "账号必须为12位数字");
            return;
        }
        
        // 检查账号是否已存在
        if (userService.accountExists(accountId)) {
            sendError(response, "账号已存在");
            return;
        }
        
        // 检查用户名是否已存在
        if (userService.usernameExists(username)) {
            sendError(response, "用户名已存在");
            return;
        }
        
        boolean success = userService.register(accountId, username, password, phone);
        if (success) {
            AuthUtil.logOperation(accountId, username, "注册", "新用户注册");
            sendSuccess(response, "注册成功");
        } else {
            sendError(response, "注册失败，请检查输入信息");
        }
    }
    
    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AuthUtil.logOperation(request, "退出", "用户退出登录");
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        sendSuccess(response, "退出成功");
    }
    
    private void getCurrentUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            User user = (User) session.getAttribute("user");
            // 刷新用户信息
            User freshUser = userService.getUserById(user.getAccountId());
            if (freshUser != null) {
                session.setAttribute("user", freshUser);
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", userToMap(freshUser));
                sendJson(response, result);
                return;
            }
        }
        sendError(response, "未登录");
    }
    
    private void getAllUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AuthUtil.requireAdmin(request, response) == null) return;
        List<User> users = userService.getAllUsers();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", users.stream().map(this::userToMap).toArray());
        sendJson(response, result);
    }
    
    private void getUserById(HttpServletRequest request, HttpServletResponse response, String id) throws IOException {
        if (AuthUtil.requireAdmin(request, response) == null) return;
        try {
            Long accountId = Long.parseLong(id);
            User user = userService.getUserById(accountId);
            if (user != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", userToMap(user));
                sendJson(response, result);
            } else {
                sendError(response, "用户不存在");
            }
        } catch (NumberFormatException e) {
            sendError(response, "无效的用户ID");
        }
    }
    
    private void deleteUser(HttpServletRequest request, HttpServletResponse response, String id) throws IOException {
        try {
            Long accountId = Long.parseLong(id);
            boolean success = userService.deleteUser(accountId);
            if (success) {
                AuthUtil.logOperation(request, "删除用户", "删除用户: 账号=" + accountId);
                sendSuccess(response, "删除成功");
            } else {
                sendError(response, "删除失败");
            }
        } catch (NumberFormatException e) {
            sendError(response, "无效的用户ID");
        }
    }
    
    private void updatePassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendError(response, "请先登录");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        Map<String, Object> params = JsonUtil.parseRequest(request);
        String oldPassword = (String) params.get("oldPassword");
        String newPassword = (String) params.get("newPassword");
        
        boolean success = userService.updatePassword(user.getAccountId(), oldPassword, newPassword);
        if (success) {
            AuthUtil.logOperation(request, "修改密码", "修改密码成功");
            sendSuccess(response, "密码修改成功");
        } else {
            sendError(response, "原密码错误");
        }
    }
    
    private void updateUserInfo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendError(response, "请先登录");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        Map<String, Object> params = JsonUtil.parseRequest(request);
        String email = (String) params.get("email");
        String address = (String) params.get("address");
        String phone = (String) params.get("phone");
        
        String error = userService.updateUserInfo(user.getAccountId(), email, address, phone);
        if (error == null) {
            AuthUtil.logOperation(request, "修改信息", "更新个人信息");
            sendSuccess(response, "信息更新成功");
        } else {
            sendError(response, error);
        }
    }
    
    private void setAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AuthUtil.requireAdmin(request, response) == null) return;
        Map<String, Object> params = JsonUtil.parseRequest(request);
        Long accountId = JsonUtil.getLong(params, "accountId");
        Boolean isAdmin = (Boolean) params.get("isAdmin");
        
        boolean success = userService.setAdmin(accountId, isAdmin != null && isAdmin);
        if (success) {
            AuthUtil.logOperation(request, "权限变更", "设置管理员权限: 账号=" + accountId + " admin=" + isAdmin);
            sendSuccess(response, "权限设置成功");
        } else {
            sendError(response, "权限设置失败");
        }
    }
    
    private void addToBlacklist(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AuthUtil.requireAdmin(request, response) == null) return;
        Map<String, Object> params = JsonUtil.parseRequest(request);
        Long accountId = JsonUtil.getLong(params, "accountId");
        String reason = (String) params.get("reason");
        
        boolean success = userService.addToBlacklist(accountId, reason);
        if (success) {
            AuthUtil.logOperation(request, "黑名单", "加入黑名单: 账号=" + accountId + " 原因=" + reason);
            sendSuccess(response, "已加入黑名单");
        } else {
            sendError(response, "操作失败");
        }
    }
    
    private void removeFromBlacklist(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AuthUtil.requireAdmin(request, response) == null) return;
        Map<String, Object> params = JsonUtil.parseRequest(request);
        Long accountId = JsonUtil.getLong(params, "accountId");
        
        boolean success = userService.removeFromBlacklist(accountId);
        if (success) {
            AuthUtil.logOperation(request, "黑名单", "移出黑名单: 账号=" + accountId);
            sendSuccess(response, "已移出黑名单");
        } else {
            sendError(response, "操作失败");
        }
    }
    
    private void getBlacklistedUsers(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AuthUtil.requireAdmin(request, response) == null) return;
        List<User> users = userService.getBlacklistedUsers();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", users.stream().map(this::userToMap).toArray());
        sendJson(response, result);
    }
    
    private void verifyUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> params = JsonUtil.parseRequest(request);
        Long accountId = JsonUtil.getLong(params, "accountId");
        String username = (String) params.get("username");
        String phone = (String) params.get("phone");
        
        boolean verified = userService.verifyUser(accountId, username, phone);
        if (verified) {
            sendSuccess(response, "验证成功");
        } else {
            sendError(response, "验证失败，信息不匹配");
        }
    }
    
    private void resetPassword(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AuthUtil.requireAdmin(request, response) == null) return;
        Map<String, Object> params = JsonUtil.parseRequest(request);
        Long accountId = JsonUtil.getLong(params, "accountId");
        String newPassword = (String) params.get("newPassword");
        
        boolean success = userService.resetPassword(accountId, newPassword);
        if (success) {
            AuthUtil.logOperation(request, "重置密码", "重置密码: 账号=" + accountId);
            sendSuccess(response, "密码重置成功");
        } else {
            sendError(response, "密码重置失败");
        }
    }
    
    private Map<String, Object> userToMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("accountId", user.getAccountId());
        map.put("username", user.getUsername());
        map.put("phone", user.getPhone());
        map.put("email", user.getEmail());
        map.put("address", user.getAddress());
        map.put("isAdmin", user.isAdmin());
        map.put("borrowLimit", user.getBorrowLimit());
        map.put("isBlacklisted", user.isBlacklisted());
        map.put("blacklistReason", user.getBlacklistReason());
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
