package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一API响应封装工具
 */
public class ApiResponse {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Map<String, Object> success(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        return result;
    }

    public static Map<String, Object> success(String message, Object data) {
        Map<String, Object> result = success(message);
        result.put("data", data);
        return result;
    }

    public static Map<String, Object> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", data);
        return result;
    }

    public static Map<String, Object> error(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return result;
    }

    public static void send(HttpServletResponse response, Map<String, Object> data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(mapper.writeValueAsString(data));
        response.getWriter().flush();
    }

    public static void sendSuccess(HttpServletResponse response, String message) throws IOException {
        send(response, success(message));
    }

    public static void sendError(HttpServletResponse response, String message) throws IOException {
        send(response, error(message));
    }

    public static void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        send(response, error(message));
    }
}
