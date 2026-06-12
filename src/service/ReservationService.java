package service;

import dao.BookDAO;
import dao.ReservationDAO;
import model.Book;
import model.Reservation;
import java.util.List;

/**
 * 预约业务逻辑层
 */
public class ReservationService {
    private ReservationDAO reservationDAO = new ReservationDAO();
    private BookDAO bookDAO = new BookDAO();

    /**
     * 创建预约
     * @return null 成功，否则返回错误信息
     */
    public String createReservation(Long accountId, Integer bookId) {
        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            return "图书不存在";
        }
        if (book.getAvailableCopies() > 0) {
            return "图书有可借副本，请直接借阅";
        }
        boolean success = reservationDAO.createReservation(accountId, bookId);
        if (!success) {
            return "预约失败，可能已预约过";
        }
        return null;
    }

    /**
     * 取消预约
     * @return null 成功，否则返回错误信息
     */
    public String cancelReservation(Integer reservationId, Long accountId) {
        boolean success = reservationDAO.cancelReservation(reservationId, accountId);
        return success ? null : "取消失败，预约记录不存在或状态不允许取消";
    }

    /**
     * 完成预约（用户借到书后调用）
     */
    public void completeReservation(Long accountId, Integer bookId) {
        reservationDAO.completeReservation(accountId, bookId);
    }

    /**
     * 通知预约队列中第一个用户（图书归还后调用）
     */
    public void notifyFirstReservation(Integer bookId) {
        reservationDAO.notifyFirstReservation(bookId);
    }

    /**
     * 获取用户预约列表
     */
    public List<Reservation> getUserReservations(Long accountId) {
        return reservationDAO.getUserReservations(accountId);
    }

    /**
     * 获取所有预约记录（管理员）
     */
    public List<Reservation> getAllReservations() {
        return reservationDAO.getAllReservations();
    }
}
