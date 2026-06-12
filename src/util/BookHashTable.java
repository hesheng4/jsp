package util;

import model.Book;
import java.util.ArrayList;
import java.util.List;

/**
 * 图书哈希表 - 用于存储和排序图书副本
 * 使用链地址法解决哈希冲突
 */
public class BookHashTable {
    
    private static final int DEFAULT_CAPACITY = 101;
    private List<BookCopy>[] buckets;
    private int size;
    
    /**
     * 图书副本类 - 表示单本实体书
     */
    public static class BookCopy {
        public Book book;           // 图书信息
        public int copyIndex;       // 副本序号（1, 2, 3...）
        public boolean isBorrowed;  // 是否被借阅
        public int borrowCount;     // 该书的总借阅次数
        
        public BookCopy(Book book, int copyIndex, boolean isBorrowed, int borrowCount) {
            this.book = book;
            this.copyIndex = copyIndex;
            this.isBorrowed = isBorrowed;
            this.borrowCount = borrowCount;
        }
    }
    
    @SuppressWarnings("unchecked")
    public BookHashTable() {
        this.buckets = new List[DEFAULT_CAPACITY];
        for (int i = 0; i < DEFAULT_CAPACITY; i++) {
            buckets[i] = new ArrayList<>();
        }
        this.size = 0;
    }
    
    @SuppressWarnings("unchecked")
    public BookHashTable(int capacity) {
        capacity = nextPrime(Math.max(capacity, 11));
        this.buckets = new List[capacity];
        for (int i = 0; i < capacity; i++) {
            buckets[i] = new ArrayList<>();
        }
        this.size = 0;
    }
    
    /**
     * 哈希函数
     */
    private int hash(int bookId) {
        return Math.abs(bookId % buckets.length);
    }
    
    /**
     * 插入单个副本（链地址法解决冲突）
     */
    public void insert(BookCopy copy) {
        int index = hash(copy.book.getBookId());
        buckets[index].add(copy);
        size++;
    }
    
    /**
     * 将一本书按总册数拆分插入
     * @param book 图书信息
     * @param borrowCount 总借阅次数
     */
    public void insertBook(Book book, int borrowCount) {
        if (book == null || book.getBookId() == null) return;
        
        int totalCopies = book.getTotalCopies() != null ? book.getTotalCopies() : 0;
        int availableCopies = book.getAvailableCopies() != null ? book.getAvailableCopies() : 0;
        int borrowedCount = totalCopies - availableCopies; // 已借出数量
        
        // 为每个副本创建记录
        for (int i = 1; i <= totalCopies; i++) {
            // 前borrowedCount本是被借阅的，后面的是可借的
            boolean isBorrowed = i <= borrowedCount;
            BookCopy copy = new BookCopy(book, i, isBorrowed, borrowCount);
            insert(copy);
        }
    }
    
    /**
     * 获取所有副本，按规则排序：
     * 1. ID从小到大
     * 2. ID相同时，未借阅的排前面
     * 3. 借阅状态相同时，借阅次数高的排前面
     */
    public List<BookCopy> getAllSorted() {
        List<BookCopy> result = new ArrayList<>();
        
        // 从所有桶中收集
        for (List<BookCopy> bucket : buckets) {
            result.addAll(bucket);
        }
        
        // 排序
        result.sort((a, b) -> {
            // 1. ID从小到大
            int idCompare = Integer.compare(a.book.getBookId(), b.book.getBookId());
            if (idCompare != 0) return idCompare;
            
            // 2. 未借阅的排前面
            if (a.isBorrowed != b.isBorrowed) {
                return a.isBorrowed ? 1 : -1;
            }
            
            // 3. 借阅次数高的排前面
            int borrowCompare = Integer.compare(b.borrowCount, a.borrowCount);
            if (borrowCompare != 0) return borrowCompare;
            
            // 4. 副本序号小的在前
            return Integer.compare(a.copyIndex, b.copyIndex);
        });
        
        return result;
    }
    
    public int size() {
        return size;
    }
    
    private int nextPrime(int n) {
        if (n <= 2) return 2;
        if (n % 2 == 0) n++;
        while (!isPrime(n)) n += 2;
        return n;
    }
    
    private boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }
}
