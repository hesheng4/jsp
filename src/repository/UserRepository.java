package repository;

import model.entity.UserEntity;
import util.JpaUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

public class UserRepository extends BaseRepository<UserEntity> {

    public UserRepository() {
        super(UserEntity.class);
    }

    public UserEntity findByAccountId(Long accountId) {
        return findSingleByField("accountId", accountId);
    }

    public UserEntity findByUsername(String username) {
        return findSingleByField("username", username);
    }

    public List<UserEntity> findBlacklisted() {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery("SELECT u FROM UserEntity u WHERE u.isBlacklisted = true", UserEntity.class)
                     .getResultList();
        }
    }

    public List<UserEntity> findPending() {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery("SELECT u FROM UserEntity u WHERE u.isEnabled = false AND u.isRoot = false", UserEntity.class)
                     .getResultList();
        }
    }

    public List<UserEntity> findAdmins() {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery("SELECT u FROM UserEntity u WHERE u.isAdmin = true AND u.isRoot = false", UserEntity.class)
                     .getResultList();
        }
    }

    public boolean existsByAccountId(Long accountId) {
        return findByAccountId(accountId) != null;
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username) != null;
    }
}
