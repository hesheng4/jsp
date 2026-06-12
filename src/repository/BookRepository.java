package repository;

import model.entity.BookEntity;
import util.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class BookRepository extends BaseRepository<BookEntity> {

    public BookRepository() {
        super(BookEntity.class);
    }

    public List<BookEntity> findByCategoryId(Integer categoryId) {
        return findByField("categoryId", categoryId);
    }

    public List<BookEntity> searchByKeyword(String keyword) {
        try (EntityManager em = JpaUtil.createEntityManager()) {
            return em.createQuery(
                "SELECT b FROM BookEntity b WHERE b.title LIKE :kw OR b.author LIKE :kw OR b.isbn LIKE :kw",
                BookEntity.class)
                .setParameter("kw", "%" + keyword + "%")
                .getResultList();
        }
    }

    public List<BookEntity> advancedSearch(String title, String author, String publisher, String isbn, Integer categoryId) {
        StringBuilder jpql = new StringBuilder("SELECT b FROM BookEntity b WHERE 1=1");
        if (title != null && !title.isEmpty()) jpql.append(" AND b.title LIKE :title");
        if (author != null && !author.isEmpty()) jpql.append(" AND b.author LIKE :author");
        if (publisher != null && !publisher.isEmpty()) jpql.append(" AND b.publisher LIKE :publisher");
        if (isbn != null && !isbn.isEmpty()) jpql.append(" AND b.isbn LIKE :isbn");
        if (categoryId != null) jpql.append(" AND b.categoryId = :categoryId");

        try (EntityManager em = JpaUtil.createEntityManager()) {
            TypedQuery<BookEntity> query = em.createQuery(jpql.toString(), BookEntity.class);
            if (title != null && !title.isEmpty()) query.setParameter("title", "%" + title + "%");
            if (author != null && !author.isEmpty()) query.setParameter("author", "%" + author + "%");
            if (publisher != null && !publisher.isEmpty()) query.setParameter("publisher", "%" + publisher + "%");
            if (isbn != null && !isbn.isEmpty()) query.setParameter("isbn", "%" + isbn + "%");
            if (categoryId != null) query.setParameter("categoryId", categoryId);
            return query.getResultList();
        }
    }
}
