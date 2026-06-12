package filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 全局异常处理过滤器
 */
@WebFilter(filterName = "ExceptionFilter", urlPatterns = "/api/*")
public class ExceptionFilter implements Filter {
    private static final Logger LOGGER = Logger.getLogger(ExceptionFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "未处理异常", e);

            HttpServletResponse httpResponse = (HttpServletResponse) response;
            if (!httpResponse.isCommitted()) {
                httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpResponse.setContentType("application/json;charset=UTF-8");

                String msg = "{\"success\":false,\"message\":\"服务器内部错误\"}";
                httpResponse.getWriter().print(msg);
                httpResponse.getWriter().flush();
            }
        }
    }
}
