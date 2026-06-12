package servlet;

import model.Reservation;
import model.User;
import service.ReservationService;
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
 * 预约控制器Servlet
 */
@WebServlet("/api/reservation/*")
public class ReservationServlet extends HttpServlet {
    private ReservationService reservationService = new ReservationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json;charset=UTF-8");

        User user = AuthUtil.requireLogin(request, response);
        if (user == null) return;

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
            getUserReservations(response, user.getAccountId());
        } else if (pathInfo.equals("/all")) {
            getAllReservations(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json;charset=UTF-8");

        User user = AuthUtil.requireLogin(request, response);
        if (user == null) return;

        if (pathInfo == null) {
            sendError(response, "无效的请求路径");
            return;
        }

        switch (pathInfo) {
            case "/reserve":
                createReservation(request, response, user);
                break;
            case "/cancel":
                cancelReservation(request, response, user);
                break;
            default:
                sendError(response, "无效的请求路径");
        }
    }

    private void createReservation(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        Map<String, Object> params = JsonUtil.parseRequest(request);
        Integer bookId = JsonUtil.getInteger(params, "bookId");

        if (bookId == null) {
            sendError(response, "请选择要预约的图书");
            return;
        }

        String error = reservationService.createReservation(user.getAccountId(), bookId);
        if (error == null) {
            AuthUtil.logOperation(request, "预约", "预约图书ID: " + bookId);
            sendSuccess(response, "预约成功，排队等待中");
        } else {
            sendError(response, error);
        }
    }

    private void cancelReservation(HttpServletRequest request, HttpServletResponse response, User user) throws IOException {
        Map<String, Object> params = JsonUtil.parseRequest(request);
        Integer reservationId = JsonUtil.getInteger(params, "reservationId");

        if (reservationId == null) {
            sendError(response, "请选择要取消的预约");
            return;
        }

        String error = reservationService.cancelReservation(reservationId, user.getAccountId());
        if (error == null) {
            AuthUtil.logOperation(request, "取消预约", "取消预约ID: " + reservationId);
            sendSuccess(response, "预约已取消");
        } else {
            sendError(response, error);
        }
    }

    private void getUserReservations(HttpServletResponse response, Long accountId) throws IOException {
        List<Reservation> records = reservationService.getUserReservations(accountId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", records.stream().map(this::recordToMap).toArray());
        sendJson(response, result);
    }

    private void getAllReservations(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (AuthUtil.requireAdmin(request, response) == null) return;
        List<Reservation> records = reservationService.getAllReservations();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", records.stream().map(this::recordToMap).toArray());
        sendJson(response, result);
    }

    private Map<String, Object> recordToMap(Reservation r) {
        Map<String, Object> map = new HashMap<>();
        map.put("reservationId", r.getReservationId());
        map.put("accountId", r.getAccountId());
        map.put("bookId", r.getBookId());
        map.put("bookTitle", r.getBookTitle());
        map.put("username", r.getUsername());
        map.put("reservationDate", r.getReservationDate() != null ? r.getReservationDate().toString() : null);
        map.put("status", r.getStatus());
        map.put("notifyDate", r.getNotifyDate() != null ? r.getNotifyDate().toString() : null);
        map.put("expireDate", r.getExpireDate() != null ? r.getExpireDate().toString() : null);
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
