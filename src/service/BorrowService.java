package service;

import dao.BorrowDAO;
import dao.BookDAO;
import dao.UserDAO;
import dao.ReservationDAO;
import model.BorrowRecord;
import model.Book;
import java.util.List;

/**
 * 借阅业务逻辑层
 * 三层架构中的Service层
 */
public class BorrowService {
    private BorrowDAO borrowDAO = new BorrowDAO();
    private BookDAO bookDAO = new BookDAO();
    private UserDAO userDAO = new UserDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();
    
    /**
     * 借阅图书
     * @return null表示成功，否则返回错误信息
     */
    public String borrowBook(Long accountId, Integer bookId) {
        // 检查用户是否可以借书
        String canBorrow = userDAO.canBorrow(accountId);
        if (canBorrow != null) {
            return canBorrow;
        }
        
        // 检查图书是否存在且可借
        Book book = bookDAO.getBookById(bookId);
        if (book == null) {
            return "图书不存在";
        }
        if (book.getAvailableCopies() <= 0) {
            return "图书已全部借出";
        }
        
        // 执行借阅
        boolean success = borrowDAO.borrowBook(accountId, bookId);
        if (success) {
            // 如果有待取书的预约，标记为已完成
            reservationDAO.completeReservation(accountId, bookId);
            return null;
        }
        return "借阅失败，请稍后重试";
    }
    
    /**
     * 归还图书
     */
    public boolean returnBook(Integer recordId) {
        Integer bookId = borrowDAO.getBorrowBookId(recordId);
        boolean success = borrowDAO.returnBook(recordId);
        if (success && bookId != null) {
            // 通知预约队列中第一个用户
            reservationDAO.notifyFirstReservation(bookId);
        }
        return success;
    }
    
    /**
     * 续借图书
     * @return null表示成功，否则返回错误信息
     */
    public String renewBook(Integer recordId) {
        boolean success = borrowDAO.renewBook(recordId);
        if (!success) {
            return "续借失败，可能已续借过或记录不存在";
        }
        return null;
    }
    
    /**
     * 获取用户所有借阅记录
     */
    public List<BorrowRecord> getUserBorrowRecords(Long accountId) {
        return borrowDAO.getUserBorrowRecords(accountId);
    }
    
    /**
     * 获取用户当前借阅
     */
    public List<BorrowRecord> getUserActiveBorrows(Long accountId) {
        return borrowDAO.getUserActiveBorrows(accountId);
    }

    /**
     * 获取所有借阅记录（管理员）
     */
    public List<BorrowRecord> getAllBorrowRecords() {
        return borrowDAO.getAllBorrowRecords();
    }
}
