package service;

import dao.BookDAO;
import dao.BookReviewDAO;
import model.Book;
import util.BookHashTable;
import util.BPlusTree;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 图书业务逻辑层
 */
public class BookService {
    private BookDAO bookDAO = new BookDAO();
    private BookReviewDAO reviewDAO = new BookReviewDAO();
    
    /**
     * 添加图书
     */
    public boolean addBook(Book book) {
        if (book == null || book.getTitle() == null || book.getTitle().isEmpty()) {
            return false;
        }
        if (book.getAddDate() == null) {
            book.setAddDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (book.getTotalCopies() == null || book.getTotalCopies() < 1) {
            book.setTotalCopies(1);
        }
        if (book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getTotalCopies());
        }
        return bookDAO.addBook(book);
    }
    
    public boolean deleteBook(Integer bookId) {
        if (bookId == null) return false;
        BookDAO.BookStatistics stats = bookDAO.getBookStatistics(bookId);
        if (stats != null && stats.isCurrentlyBorrowed()) return false;
        return bookDAO.deleteBook(bookId);
    }
    
    public boolean updateBook(Book book) {
        if (book == null || book.getBookId() == null) return false;
        return bookDAO.updateBook(book);
    }
    
    public Book getBookById(Integer bookId) {
        return bookDAO.getBookById(bookId);
    }
    
    public List<Book> getAllBooks() {
        return bookDAO.getAllBooks();
    }
    
    public List<Book> getBooksByCategory(Integer categoryId) {
        return bookDAO.getBooksByCategory(categoryId);
    }
    
    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAllBooks();
        return bookDAO.searchBooks(keyword.trim());
    }
    
    public List<Book> advancedSearch(String title, String author, String publisher, 
                                      String isbn, Integer categoryId) {
        return bookDAO.advancedSearch(title, author, publisher, isbn, categoryId);
    }
    
    public BookDAO.BookStatistics getBookStatistics(Integer bookId) {
        return bookDAO.getBookStatistics(bookId);
    }

    /**
     * 使用哈希表获取图书副本排序列表
     * 按ID排序，可借优先，借阅次数高的优先
     */
    public List<BookHashTable.BookCopy> getBookCopiesSorted(Book book, int borrowCount) {
        BookHashTable table = new BookHashTable();
        table.insertBook(book, borrowCount);
        return table.getAllSorted();
    }

    /**
     * 使用B+树按评分排名图书
     * 评分高的排在前面
     */
    public List<Book> getBooksRankedByRating(int limit) {
        List<Book> allBooks = getAllBooks();
        BPlusTree<Book> tree = new BPlusTree<>();
        
        for (Book book : allBooks) {
            Double avgRating = reviewDAO.getBookAverageRating(book.getBookId());
            if (avgRating != null && avgRating > 0) {
                tree.insert(avgRating, book);
            }
        }
        
        List<Book> ranked = tree.getTopN(limit);
        // 如果评分图书不足，补充未评分图书
        if (ranked.size() < limit) {
            for (Book book : allBooks) {
                if (!ranked.contains(book)) {
                    ranked.add(book);
                    if (ranked.size() >= limit) break;
                }
            }
        }
        return ranked;
    }
}
