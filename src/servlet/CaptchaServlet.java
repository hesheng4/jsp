package servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

/**
 * 验证码Servlet
 * 生成简单算术验证码，答案存储在Session中
 */
@WebServlet("/api/captcha")
public class CaptchaServlet extends HttpServlet {

    private static final Random RANDOM = new Random();
    private static final String SESSION_KEY = "captchaAnswer";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        int a = RANDOM.nextInt(20) + 1;
        int b = RANDOM.nextInt(20) + 1;
        int op = RANDOM.nextInt(3); // 0=+, 1=-, 2=*

        String question;
        int answer;
        switch (op) {
            case 1:
                // 确保结果为正
                if (a < b) { int t = a; a = b; b = t; }
                question = a + " - " + b + " = ?";
                answer = a - b;
                break;
            case 2:
                a = RANDOM.nextInt(9) + 1;
                b = RANDOM.nextInt(9) + 1;
                question = a + " × " + b + " = ?";
                answer = a * b;
                break;
            default:
                question = a + " + " + b + " = ?";
                answer = a + b;
        }

        // 存答案到Session
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_KEY, answer);

        PrintWriter out = response.getWriter();
        out.print("{\"question\":\"" + question + "\"}");
        out.flush();
    }

    /**
     * 验证用户输入的验证码
     * @return true=正确
     */
    public static boolean verify(HttpServletRequest request, int userAnswer) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Integer answer = (Integer) session.getAttribute(SESSION_KEY);
        if (answer == null) return false;
        // 验证后清除，防止重复使用
        session.removeAttribute(SESSION_KEY);
        return answer == userAnswer;
    }
}
