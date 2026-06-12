package service;

import dao.FineDAO;
import model.FineRecord;
import java.util.List;

/**
 * 罚款业务逻辑层
 */
public class FineService {
    private FineDAO fineDAO = new FineDAO();

    /**
     * 缴纳单笔罚款
     */
    public boolean payFine(Integer fineId) {
        return fineDAO.payFine(fineId);
    }

    /**
     * 批量缴纳用户所有未缴罚款
     */
    public boolean payAllFines(Long accountId) {
        return fineDAO.payAllFines(accountId);
    }

    /**
     * 获取用户未缴罚款总额
     */
    public Double getUnpaidFineAmount(Long accountId) {
        return fineDAO.getUnpaidFineAmount(accountId);
    }

    /**
     * 获取用户罚款记录
     */
    public List<FineRecord> getUserFines(Long accountId) {
        return fineDAO.getUserFines(accountId);
    }

    /**
     * 根据借阅记录创建并缴纳罚款
     */
    public String createAndPayByRecord(Integer recordId, Long accountId) {
        return fineDAO.createAndPayByRecord(recordId, accountId);
    }

    /**
     * 获取所有未缴罚款（管理员）
     */
    public List<FineRecord> getAllUnpaidFines() {
        return fineDAO.getAllUnpaidFines();
    }

    /**
     * 获取所有罚款记录（管理员）
     */
    public List<FineRecord> getAllFines() {
        return fineDAO.getAllFines();
    }
}
