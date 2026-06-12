package model;

import java.util.ArrayList;
import java.util.List;

/**
 * 图书分类模型类（支持层级结构）
 */
public class Category {
    private Integer categoryId;
    private String categoryName;
    private String description;
    private Integer parentId;      // 父分类ID
    private Integer level;         // 层级（1为顶级）
    private List<Category> children; // 子分类列表
    
    public Category() {
        this.children = new ArrayList<>();
    }
    
    public Category(Integer categoryId, String categoryName, String description) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.children = new ArrayList<>();
    }
    
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }
    
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    
    public List<Category> getChildren() { return children; }
    public void setChildren(List<Category> children) { this.children = children; }
    
    public void addChild(Category child) { this.children.add(child); }
}
