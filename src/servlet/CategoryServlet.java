package servlet;

import dao.CategoryDAO;
import model.Category;
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
 * 分类控制器Servlet
 */
@WebServlet("/api/category/*")
public class CategoryServlet extends HttpServlet {
    private CategoryDAO categoryDAO = new CategoryDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        getAllCategories(response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        // 添加分类需要管理员权限
        if (AuthUtil.requireAdmin(request, response) == null) return;
        addCategory(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        // 删除分类需要管理员权限
        if (AuthUtil.requireAdmin(request, response) == null) return;
        String pathInfo = request.getPathInfo();
        
        if (pathInfo != null && pathInfo.length() > 1) {
            deleteCategory(request, response, pathInfo.substring(1));
        } else {
            sendError(response, "无效的分类ID");
        }
    }
    
    private void getAllCategories(HttpServletResponse response) throws IOException {
        List<Category> categories = categoryDAO.getAllCategories();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", categories.stream().map(this::categoryToMap).toArray());
        sendJson(response, result);
    }
    
    private void addCategory(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> params = JsonUtil.parseRequest(request);
        String name = (String) params.get("categoryName");
        String description = (String) params.get("description");
        
        if (name == null || name.trim().isEmpty()) {
            sendError(response, "分类名称不能为空");
            return;
        }
        
        Category category = new Category();
        category.setCategoryName(name);
        category.setDescription(description);
        
        boolean success = categoryDAO.addCategory(category);
        if (success) {
            AuthUtil.logOperation(request, "添加分类", "添加分类: " + name);
            sendSuccess(response, "分类添加成功");
        } else {
            sendError(response, "分类添加失败");
        }
    }
    
    private void deleteCategory(HttpServletRequest request, HttpServletResponse response, String id) throws IOException {
        try {
            Integer categoryId = Integer.parseInt(id);
            boolean success = categoryDAO.deleteCategory(categoryId);
            if (success) {
                AuthUtil.logOperation(request, "删除分类", "删除分类ID: " + categoryId);
                sendSuccess(response, "分类删除成功");
            } else {
                sendError(response, "分类删除失败");
            }
        } catch (NumberFormatException e) {
            sendError(response, "无效的分类ID");
        }
    }
    
    private Map<String, Object> categoryToMap(Category category) {
        Map<String, Object> map = new HashMap<>();
        map.put("categoryId", category.getCategoryId());
        map.put("categoryName", category.getCategoryName());
        map.put("description", category.getDescription());
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
