package util;

import jakarta.persistence.EntityManager;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * JPA 事务工具 - 简化事务管理
 */
public class TransactionUtil {

    /**
     * 执行写操作（自动提交/回滚）
     */
    public static void execute(Consumer<EntityManager> action) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            em.getTransaction().begin();
            action.accept(em);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("事务执行失败", e);
        } finally {
            em.close();
        }
    }

    /**
     * 执行查询操作（只读，自动关闭）
     */
    public static <T> T query(Function<EntityManager, T> action) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return action.apply(em);
        } finally {
            em.close();
        }
    }

    /**
     * 执行查询操作，返回结果后自动关闭
     */
    public static <T> T queryInTransaction(Function<EntityManager, T> action) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            em.getTransaction().begin();
            T result = action.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("事务查询失败", e);
        } finally {
            em.close();
        }
    }
}
