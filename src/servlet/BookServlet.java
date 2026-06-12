package servlet;

import model.Book;
import model.User;
import service.BookService;
import util.AuthUtil;
import util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书控制器Servlet
 */
@WebServlet("/api/book/*")
public class BookServlet extends HttpServlet {
    private BookService bookService = new BookService();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("application/json;charset=UTF-8");
        
        if (pathInfo == null || pathInfo.equals("/")) {
            String keyword = request.getParameter("keyword");
            String categoryId = request.getParameter("categoryId");
            String pageStr = request.getParameter("page");
            String sizeStr = request.getParameter("size");

            if (keyword != null && !keyword.isEmpty()) {
                searchBooks(response, keyword);
            } else if (categoryId != null && !categoryId.isEmpty()) {
                getBooksByCategory(response, Integer.parseInt(categoryId));
            } else if (pageStr != null) {
                int page = Math.max(1, Integer.parseInt(pageStr));
                int size = sizeStr != null ? Math.min(100, Math.max(1, Integer.parseInt(sizeStr))) : 20;
                getAllBooksPaginated(response, page, size);
            } else {
                getAllBooks(response);
            }
        } else if (pathInfo.equals("/search")) {
            // 高级搜索
            advancedSearch(request, response);
        } else if (pathInfo.startsWith("/")) {
            // 根据ID获取图书
            getBookById(response, pathInfo.substring(1));
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        // 添加图书需要管理员权限
        if (AuthUtil.requireAdmin(request, response) == null) return;
        addBook(request, response);
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        // 更新图书需要管理员权限
        if (AuthUtil.requireAdmin(request, response) == null) return;
        updateBook(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        // 删除图书需要管理员权限
        if (AuthUtil.requireAdmin(request, response) == null) return;

        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.length() > 1) {
            deleteBook(request, response, pathInfo.substring(1));
        } else {
            sendError(response, "无效的图书ID");
        }
    }
    
    private void getAllBooks(HttpServletResponse response) throws IOException {
        List<Book> books = bookService.getAllBooks();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", books.stream().map(this::bookToMap).toArray());
        sendJson(response, result);
    }

    private void getAllBooksPaginated(HttpServletResponse response, int page, int size) throws IOException {
        var repo = new repository.BookRepository();
        var pageResult = repo.findPage(page, size);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", pageResult.getData().stream().map(this::entityToMap).toArray());
        result.put("page", pageResult.getPage());
        result.put("size", pageResult.getSize());
        result.put("total", pageResult.getTotal());
        result.put("totalPages", pageResult.getTotalPages());
        sendJson(response, result);
    }

    private Map<String, Object> entityToMap(model.entity.BookEntity book) {
        Map<String, Object> map = new HashMap<>();
        map.put("bookId", book.getBookId());
        map.put("title", book.getTitle());
        map.put("author", book.getAuthor());
        map.put("isbn", book.getIsbn());
        map.put("categoryId", book.getCategoryId());
        map.put("categoryName", book.getCategory() != null ? book.getCategory().getCategoryName() : null);
        map.put("totalCopies", book.getTotalCopies());
        map.put("availableCopies", book.getAvailableCopies());
        map.put("publisher", book.getPublisher());
        map.put("publishDate", book.getPublishDate());
        map.put("addDate", book.getAddDate());
        return map;
    }
    
    private void getBookById(HttpServletResponse response, String id) throws IOException {
        try {
            Integer bookId = Integer.parseInt(id);
            Book book = bookService.getBookById(bookId);
            if (book != null) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", bookToMap(book));
                sendJson(response, result);
            } else {
                sendError(response, "图书不存在");
            }
        } catch (NumberFormatException e) {
            sendError(response, "无效的图书ID");
        }
    }
    
    private void getBooksByCategory(HttpServletResponse response, Integer categoryId) throws IOException {
        List<Book> books = bookService.getBooksByCategory(categoryId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", books.stream().map(this::bookToMap).toArray());
        sendJson(response, result);
    }
    
    private void searchBooks(HttpServletResponse response, String keyword) throws IOException {
        List<Book> books = bookService.searchBooks(keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", books.stream().map(this::bookToMap).toArray());
        sendJson(response, result);
    }
    
    private void advancedSearch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String publisher = request.getParameter("publisher");
        String isbn = request.getParameter("isbn");
        String categoryIdStr = request.getParameter("categoryId");
        Integer categoryId = null;
        if (categoryIdStr != null && !categoryIdStr.isEmpty()) {
            categoryId = Integer.parseInt(categoryIdStr);
        }
        
        List<Book> books = bookService.advancedSearch(title, author, publisher, isbn, categoryId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", books.stream().map(this::bookToMap).toArray());
        sendJson(response, result);
    }
    
    private void addBook(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> params = JsonUtil.parseRequest(request);
        
        Book book = new Book();
        book.setTitle((String) params.get("title"));
        book.setAuthor((String) params.get("author"));
        book.setIsbn((String) params.get("isbn"));
        book.setCategoryId(JsonUtil.getInteger(params, "categoryId"));
        book.setTotalCopies(JsonUtil.getInteger(params, "totalCopies"));
        book.setAvailableCopies(JsonUtil.getInteger(params, "availableCopies"));
        book.setPublisher((String) params.get("publisher"));
        book.setPublishDate((String) params.get("publishDate"));
        
        boolean success = bookService.addBook(book);
        if (success) {
            AuthUtil.logOperation(request, "添加图书", "添加图书: " + book.getTitle());
            sendSuccess(response, "图书添加成功");
        } else {
            sendError(response, "图书添加失败");
        }
    }
    
    private void updateBook(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> params = JsonUtil.parseRequest(request);
        
        Book book = new Book();
        book.setBookId(JsonUtil.getInteger(params, "bookId"));
        book.setTitle((String) params.get("title"));
        book.setAuthor((String) params.get("author"));
        book.setIsbn((String) params.get("isbn"));
        book.setCategoryId(JsonUtil.getInteger(params, "categoryId"));
        book.setTotalCopies(JsonUtil.getInteger(params, "totalCopies"));
        book.setAvailableCopies(JsonUtil.getInteger(params, "availableCopies"));
        book.setPublisher((String) params.get("publisher"));
        book.setPublishDate((String) params.get("publishDate"));
        
        boolean success = bookService.updateBook(book);
        if (success) {
            AuthUtil.logOperation(request, "更新图书", "更新图书ID: " + book.getBookId());
            sendSuccess(response, "图书更新成功");
        } else {
            sendError(response, "图书更新失败");
        }
    }
    
    private void deleteBook(HttpServletRequest request, HttpServletResponse response, String id) throws IOException {
        try {
            Integer bookId = Integer.parseInt(id);
            boolean success = bookService.deleteBook(bookId);
            if (success) {
                AuthUtil.logOperation(request, "删除图书", "删除图书ID: " + bookId);
                sendSuccess(response, "图书删除成功");
            } else {
                sendError(response, "删除失败，图书可能正在被借阅");
            }
        } catch (NumberFormatException e) {
            sendError(response, "无效的图书ID");
        }
    }
    
    private Map<String, Object> bookToMap(Book book) {
        Map<String, Object> map = new HashMap<>();
        map.put("bookId", book.getBookId());
        map.put("title", book.getTitle());
        map.put("author", book.getAuthor());
        map.put("isbn", book.getIsbn());
        map.put("categoryId", book.getCategoryId());
        map.put("categoryName", book.getCategoryName());
        map.put("totalCopies", book.getTotalCopies());
        map.put("availableCopies", book.getAvailableCopies());
        map.put("publisher", book.getPublisher());
        map.put("publishDate", book.getPublishDate());
        map.put("addDate", book.getAddDate());
        return map;
    }
    
    private void sendJson(HttpServletResponse response, Map<String, Object> data) throws IOException {
        PrintWriter out = response.getWriter();
        out.print(JsonUtil.toJson(data));
        out.flush();
    }
    
    private void sendSuccess(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        sendJson(response, result);
    }
    
    private void sendError(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        sendJson(response, result);
    }
}
