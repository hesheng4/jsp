package repository;

import util.JpaUtil;
import util.PageResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * 通用JPA Repository基类 - 消除重复CRUD代码
 */
public abstract class BaseRepository<T> {
    private final Class<T> entityClass;

    protected BaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T findById(Object id) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.find(entityClass, id);
        }
    }

    public List<T> findAll() {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            TypedQuery<T> query = em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass);
            return query.getResultList();
        }
    }

    public List<T> findWithLimit(int limit) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            TypedQuery<T> query = em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass);
            query.setMaxResults(limit);
            return query.getResultList();
        }
    }

    public T save(T entity) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            em.getTransaction().begin();
            T merged = em.merge(entity);
            em.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void persist(T entity) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(entity);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void remove(Object id) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            em.getTransaction().begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public long count() {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                     .getSingleResult();
        }
    }

    public List<T> findByField(String fieldName, Object value) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            TypedQuery<T> query = em.createQuery(
                "SELECT e FROM " + entityClass.getSimpleName() + " e WHERE e." + fieldName + " = :value",
                entityClass);
            query.setParameter("value", value);
            return query.getResultList();
        }
    }

    public T findSingleByField(String fieldName, Object value) {
        List<T> results = findByField(fieldName, value);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 分页查询
     */
    public PageResult<T> findPage(int page, int size) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            long total = em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                           .getSingleResult();
            List<T> data = em.createQuery("SELECT e FROM " + entityClass.getSimpleName() + " e", entityClass)
                             .setFirstResult((page - 1) * size)
                             .setMaxResults(size)
                             .getResultList();
            return new PageResult<>(data, page, size, total);
        }
    }
}
