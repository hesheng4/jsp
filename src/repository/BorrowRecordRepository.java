package repository;

import model.entity.BorrowRecordEntity;
import util.JpaUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

public class BorrowRecordRepository extends BaseRepository<BorrowRecordEntity> {

    public BorrowRecordRepository() {
        super(BorrowRecordEntity.class);
    }

    public List<BorrowRecordEntity> findByAccountId(Long accountId) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery(
                "SELECT r FROM BorrowRecordEntity r WHERE r.accountId = :accountId ORDER BY r.borrowDate DESC",
                BorrowRecordEntity.class)
                .setParameter("accountId", accountId)
                .getResultList();
        }
    }

    public List<BorrowRecordEntity> findActiveByAccountId(Long accountId) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery(
                "SELECT r FROM BorrowRecordEntity r WHERE r.accountId = :accountId AND r.status = '借阅中' ORDER BY r.borrowDate DESC",
                BorrowRecordEntity.class)
                .setParameter("accountId", accountId)
                .getResultList();
        }
    }

    public List<BorrowRecordEntity> findAllActive() {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery(
                "SELECT r FROM BorrowRecordEntity r WHERE r.status = '借阅中' ORDER BY r.borrowDate DESC",
                BorrowRecordEntity.class)
                .getResultList();
        }
    }
}
