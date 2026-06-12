package servlet;

import model.BorrowRecord;
import model.User;
import service.BorrowService;
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
 * 借阅控制器Servlet
 */
@WebServlet("/api/borrow/*")
public class BorrowServlet extends HttpServlet {
    private BorrowService borrowService = new BorrowService();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json;charset=UTF-8");
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendError(response, "请先登录");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/records")) {
            // 获取用户所有借阅记录
            getUserBorrowRecords(response, user.getAccountId());
        } else if (pathInfo.equals("/active")) {
            // 获取用户当前借阅
            getUserActiveBorrows(response, user.getAccountId());
        } else if (pathInfo.equals("/all")) {
            // 管理员查看全部记录
            getAllBorrowRecords(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json;charset=UTF-8");
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            sendError(response, "请先登录");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        if (pathInfo == null) {
            sendError(response, "无效的请求路径");
            return;
        }
        
        switch (pathInfo) {
            case "/borrow":
                borrowBook(request, response, user);
                break;
            case "/return":
                returnBook(request, response);
                break;
            case "/renew":
                renewBook(request, response);
                break;
            default:
                sendError(response, "无效的请求路径");
        }
    }
    
    private void borrowBook(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        Map<String, Object> params = JsonUtil.parseRequest(request);
        Integer bookId = JsonUtil.getInteger(params, "bookId");
        
        if (bookId == null) {
            sendError(response, "请选择要借阅的图书");
            return;
        }
        
        String error = borrowService.borrowBook(user.getAccountId(), bookId);
        if (error == null) {
            AuthUtil.logOperation(request, "借阅", "借阅图书ID: " + bookId);
            sendSuccess(response, "借阅成功");
        } else {
            sendError(response, error);
        }
    }
    
    private void returnBook(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> params = JsonUtil.parseRequest(request);
        Integer recordId = JsonUtil.getInteger(params, "recordId");
        
        if (recordId == null) {
            sendError(response, "请选择要归还的借阅记录");
            return;
        }
        
        boolean success = borrowService.returnBook(recordId);
        if (success) {
            AuthUtil.logOperation(request, "归还", "归还记录ID: " + recordId);
            sendSuccess(response, "归还成功");
        } else {
            sendError(response, "归还失败");
        }
    }
    
    private void renewBook(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> params = JsonUtil.parseRequest(request);
        Integer recordId = JsonUtil.getInteger(params, "recordId");
        
        if (recordId == null) {
            sendError(response, "请选择要续借的借阅记录");
            return;
        }
        
        String error = borrowService.renewBook(recordId);
        if (error == null) {
            AuthUtil.logOperation(request, "续借", "续借记录ID: " + recordId);
            sendSuccess(response, "续借成功，借阅期限延长31天");
        } else {
            sendError(response, error);
        }
    }
    
    private void getUserBorrowRecords(HttpServletResponse response, Long accountId) throws IOException {
        List<BorrowRecord> records = borrowService.getUserBorrowRecords(accountId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", records.stream().map(this::recordToMap).toArray());
        sendJson(response, result);
    }
    
    private void getUserActiveBorrows(HttpServletResponse response, Long accountId) throws IOException {
        List<BorrowRecord> records = borrowService.getUserActiveBorrows(accountId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", records.stream().map(this::recordToMap).toArray());
        sendJson(response, result);
    }

    private void getAllBorrowRecords(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 仅管理员可查看
        if (AuthUtil.requireAdmin(request, response) == null) return;
        List<BorrowRecord> records = borrowService.getAllBorrowRecords();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", records.stream().map(this::recordToMap).toArray());
        sendJson(response, result);
    }

    private Map<String, Object> recordToMap(BorrowRecord record) {
        Map<String, Object> map = new HashMap<>();
        map.put("recordId", record.getRecordId());
        map.put("accountId", record.getAccountId());
        map.put("bookId", record.getBookId());
        map.put("bookTitle", record.getBookTitle());
        map.put("borrowDate", record.getBorrowDate() != null ? record.getBorrowDate().toString() : null);
        map.put("dueDate", record.getDueDate() != null ? record.getDueDate().toString() : null);
        map.put("returnDate", record.getReturnDate() != null ? record.getReturnDate().toString() : null);
        map.put("status", record.getStatus());
        map.put("renewCount", record.getRenewCount());
        map.put("fineAmount", record.getFineAmount());
        map.put("accountName", record.getAccountName());
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
