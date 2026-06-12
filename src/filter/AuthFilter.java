package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 登录认证过滤器
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = "/api/*")
public class AuthFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();

        // 允许登录、注册、验证、验证码接口无需认证
        if (requestURI.contains("/api/user/login") ||
            requestURI.contains("/api/user/register") ||
            requestURI.contains("/api/user/verify") ||
            requestURI.contains("/api/captcha")) {
            chain.doFilter(request, response);
            return;
        }
        
        // 检查Session中是否有用户信息
        HttpSession session = httpRequest.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=UTF-8");
            PrintWriter out = httpResponse.getWriter();
            out.print("{\"success\":false,\"message\":\"请先登录\"}");
            out.flush();
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
    }
}
