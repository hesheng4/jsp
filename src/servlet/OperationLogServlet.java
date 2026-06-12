package servlet;

import dao.OperationLogDAO;
import model.OperationLog;
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
 * 操作日志控制器（管理员）
 */
@WebServlet("/api/logs")
public class OperationLogServlet extends HttpServlet {
    private OperationLogDAO operationLogDAO = new OperationLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        User user = AuthUtil.requireAdmin(request, response);
        if (user == null) return;

        List<OperationLog> logs = operationLogDAO.getAllLogs();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", logs.stream().map(this::logToMap).toArray());
        sendJson(response, result);
    }

    private Map<String, Object> logToMap(OperationLog log) {
        Map<String, Object> map = new HashMap<>();
        map.put("logId", log.getLogId());
        map.put("accountId", log.getAccountId());
        map.put("username", log.getUsername());
        map.put("operationType", log.getOperationType());
        map.put("operationDetail", log.getOperationDetail());
        map.put("operationTime", log.getOperationTime() != null ? log.getOperationTime().toString() : null);
        return map;
    }

    private void sendJson(HttpServletResponse response, Map<String, Object> data) throws IOException {
        PrintWriter out = response.getWriter();
        out.print(JsonUtil.toJson(data));
        out.flush();
    }
}
