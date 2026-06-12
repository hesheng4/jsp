package util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页结果封装
 */
public class PageResult<T> {
    private List<T> data;
    private int page;
    private int size;
    private long total;
    private int totalPages;

    public PageResult(List<T> data, int page, int size, long total) {
        this.data = data;
        this.page = page;
        this.size = size;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / size);
    }

    public List<T> getData() { return data; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotal() { return total; }
    public int getTotalPages() { return totalPages; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("data", data);
        map.put("page", page);
        map.put("size", size);
        map.put("total", total);
        map.put("totalPages", totalPages);
        return map;
    }

    /**
     * 从请求参数中解析分页参数
     */
    public static int[] parsePageParams(Map<String, Object> params) {
        int page = 1;
        int size = 20;
        Object p = params.get("page");
        Object s = params.get("size");
        if (p instanceof Number) page = Math.max(1, ((Number) p).intValue());
        else if (p instanceof String) try { page = Math.max(1, Integer.parseInt((String) p)); } catch (NumberFormatException ignored) {}
        if (s instanceof Number) size = Math.min(100, Math.max(1, ((Number) s).intValue()));
        else if (s instanceof String) try { size = Math.min(100, Math.max(1, Integer.parseInt((String) s))); } catch (NumberFormatException ignored) {}
        return new int[]{page, size};
    }
}
