package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CORS跨域过滤器
 */
@WebFilter(filterName = "CorsFilter", urlPatterns = "/api/*",
    initParams = @WebInitParam(name = "allowedOrigins", value = "*"))
public class CorsFilter implements Filter {

    // 允许的域名（生产环境应修改为具体域名）
    private String allowedOrigins = "*";
    // 允许的HTTP方法
    private String allowedMethods = "GET, POST, PUT, DELETE, OPTIONS";
    // 允许的请求头
    private String allowedHeaders = "Content-Type, Authorization, X-Requested-With";
    // 预检请求缓存时间（秒）
    private int maxAge = 3600;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String origins = filterConfig.getInitParameter("allowedOrigins");
        if (origins != null && !origins.isEmpty()) {
            allowedOrigins = origins;
        }
        String methods = filterConfig.getInitParameter("allowedMethods");
        if (methods != null && !methods.isEmpty()) {
            allowedMethods = methods;
        }
        String headers = filterConfig.getInitParameter("allowedHeaders");
        if (headers != null && !headers.isEmpty()) {
            allowedHeaders = headers;
        }
        String age = filterConfig.getInitParameter("maxAge");
        if (age != null && !age.isEmpty()) {
            try { maxAge = Integer.parseInt(age); } catch (NumberFormatException ignored) {}
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String origin = httpRequest.getHeader("Origin");

        // 设置CORS响应头
        if ("*".equals(allowedOrigins)) {
            httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        } else if (origin != null && allowedOrigins.contains(origin)) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
            httpResponse.setHeader("Vary", "Origin");
        }

        httpResponse.setHeader("Access-Control-Allow-Methods", allowedMethods);
        httpResponse.setHeader("Access-Control-Allow-Headers", allowedHeaders);
        httpResponse.setHeader("Access-Control-Max-Age", String.valueOf(maxAge));

        // 允许携带凭证（Cookie/Authorization）
        if (!"*".equals(allowedOrigins)) {
            httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        }

        // OPTIONS预检请求直接返回200
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
