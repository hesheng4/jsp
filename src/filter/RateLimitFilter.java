package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 请求频率限制过滤器
 */
@WebFilter(filterName = "RateLimitFilter", urlPatterns = "/api/*", initParams = {
    @WebInitParam(name = "maxRequestsPerMinute", value = "60"),
    @WebInitParam(name = "maxLoginPerMinute", value = "5")
})
public class RateLimitFilter implements Filter {

    // 每个IP每分钟最大请求数
    private int maxRequestsPerMinute = 60;
    // 登录接口每分钟最大尝试次数
    private int maxLoginPerMinute = 5;
    // 清理过期数据间隔（毫秒）
    private long cleanupIntervalMs = 60000;
    private long lastCleanup = System.currentTimeMillis();

    // IP → 请求计数
    private final ConcurrentHashMap<String, RequestCounter> counters = new ConcurrentHashMap<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String maxReq = filterConfig.getInitParameter("maxRequestsPerMinute");
        if (maxReq != null) maxRequestsPerMinute = Integer.parseInt(maxReq);
        String maxLogin = filterConfig.getInitParameter("maxLoginPerMinute");
        if (maxLogin != null) maxLoginPerMinute = Integer.parseInt(maxLogin);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 定期清理过期数据
        cleanupIfNeeded();

        String ip = getClientIp(httpRequest);
        String requestURI = httpRequest.getRequestURI();

        // 判断是否为登录请求
        boolean isLoginRequest = requestURI.contains("/api/user/login");

        int limit = isLoginRequest ? maxLoginPerMinute : maxRequestsPerMinute;
        String limitType = isLoginRequest ? "登录" : "API";

        RequestCounter counter = counters.computeIfAbsent(ip, k -> new RequestCounter());
        synchronized (counter) {
            long now = System.currentTimeMillis();
            if (now - counter.windowStart > 60000) {
                counter.windowStart = now;
                counter.count = 0;
            }
            counter.count++;

            if (counter.count > limit) {
                httpResponse.setStatus(429); // Too Many Requests
                httpResponse.setContentType("application/json;charset=UTF-8");
                PrintWriter out = httpResponse.getWriter();
                out.print("{\"success\":false,\"message\":\"" + limitType + "请求过于频繁，请稍后再试\"}");
                out.flush();
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private void cleanupIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCleanup > cleanupIntervalMs) {
            lastCleanup = now;
            counters.entrySet().removeIf(entry -> {
                synchronized (entry.getValue()) {
                    return now - entry.getValue().windowStart > 120000; // 2分钟过期
                }
            });
        }
    }

    private String getClientIp(HttpServletRequest request) {
        // 检查反向代理头
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @Override
    public void destroy() {
        counters.clear();
    }

    private static class RequestCounter {
        long windowStart = System.currentTimeMillis();
        int count = 0;
    }
}
