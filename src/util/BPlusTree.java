package util;

import java.util.ArrayList;
import java.util.List;

/**
 * B+树实现 - 用于图书评分排序
 * 支持按评分（Double类型）进行排序，评分高的排在前面（降序）
 * @param <V> 值类型
 */
public class BPlusTree<V> {
    
    private static final int ORDER = 4; // B+树的阶数
    private Node root;
    private LeafNode firstLeaf; // 指向最左叶子节点，用于范围查询
    
    public BPlusTree() {
        this.root = null;
        this.firstLeaf = null;
    }
    
    /**
     * 插入键值对
     * @param key 评分（作为排序键）
     * @param value 关联的值（如图书对象）
     */
    public void insert(Double key, V value) {
        if (root == null) {
            // 树为空，创建第一个叶子节点
            LeafNode leaf = new LeafNode();
            leaf.insert(key, value);
            root = leaf;
            firstLeaf = leaf;
        } else {
            // 找到应该插入的叶子节点
            LeafNode leaf = findLeafNode(key);
            leaf.insert(key, value);
            
            // 检查是否需要分裂
            if (leaf.isOverflow()) {
                Node newNode = leaf.split();
                Double newKey = newNode.getFirstKey();
                insertIntoParent(leaf, newKey, newNode);
            }
        }
    }
    
    /**
     * 获取所有值，按评分降序排列
     * @return 降序排列的值列表
     */
    public List<V> getAllDescending() {
        List<V> result = new ArrayList<>();
        if (firstLeaf == null) return result;
        
        // 先收集所有数据
        List<Entry> allEntries = new ArrayList<>();
        LeafNode current = firstLeaf;
        while (current != null) {
            for (int i = 0; i < current.numKeys; i++) {
                allEntries.add(new Entry(current.keys[i], current.values[i]));
            }
            current = current.next;
        }
        
        // 按评分降序排序
        allEntries.sort((a, b) -> Double.compare(b.key, a.key));
        
        for (Entry entry : allEntries) {
            result.add((V) entry.value);
        }
        
        return result;
    }
    
    /**
     * 获取评分最高的N个值
     * @param n 数量
     * @return 评分最高的N个值
     */
    public List<V> getTopN(int n) {
        List<V> all = getAllDescending();
        if (all.size() <= n) return all;
        return all.subList(0, n);
    }
    
    /**
     * 获取评分在指定范围内的值
     * @param minRating 最低评分
     * @param maxRating 最高评分
     * @return 范围内的值列表（降序）
     */
    public List<V> getByRatingRange(Double minRating, Double maxRating) {
        List<V> result = new ArrayList<>();
        List<V> all = getAllDescending();
        
        for (int i = 0; i < all.size(); i++) {
            // 需要重新获取key来判断范围
            // 这里简化处理，遍历所有叶子节点
        }
        
        // 简化实现：遍历所有叶子节点
        LeafNode current = firstLeaf;
        List<Entry> entries = new ArrayList<>();
        while (current != null) {
            for (int i = 0; i < current.numKeys; i++) {
                Double key = current.keys[i];
                if (key >= minRating && key <= maxRating) {
                    entries.add(new Entry(key, current.values[i]));
                }
            }
            current = current.next;
        }
        
        // 降序排序
        entries.sort((a, b) -> Double.compare(b.key, a.key));
        for (Entry entry : entries) {
            result.add((V) entry.value);
        }
        
        return result;
    }
    
    /**
     * 清空树
     */
    public void clear() {
        root = null;
        firstLeaf = null;
    }
    
    /**
     * 获取树中元素数量
     */
    public int size() {
        int count = 0;
        LeafNode current = firstLeaf;
        while (current != null) {
            count += current.numKeys;
            current = current.next;
        }
        return count;
    }
    
    // ==================== 内部类 ====================
    
    /**
     * 键值对条目
     */
    private class Entry {
        Double key;
        Object value;
        
        Entry(Double key, Object value) {
            this.key = key;
            this.value = value;
        }
    }
    
    /**
     * 节点基类
     */
    private abstract class Node {
        Double[] keys;
        int numKeys;
        InternalNode parent;
        
        abstract Double getFirstKey();
        abstract boolean isOverflow();
        abstract Node split();
    }
    
    /**
     * 内部节点
     */
    private class InternalNode extends Node {
        Node[] children;
        
        @SuppressWarnings("unchecked")
        InternalNode() {
            this.keys = new Double[ORDER];
            this.children = (Node[]) new Object[ORDER + 1];
            this.numKeys = 0;
        }
        
        @Override
        Double getFirstKey() {
            return keys[0];
        }
        
        @Override
        boolean isOverflow() {
            return numKeys > ORDER - 1;
        }
        
        @Override
        Node split() {
            int midIndex = numKeys / 2;
            InternalNode sibling = new InternalNode();
            
            // 复制后半部分到兄弟节点
            sibling.numKeys = numKeys - midIndex - 1;
            for (int i = 0; i < sibling.numKeys; i++) {
                sibling.keys[i] = keys[midIndex + 1 + i];
                sibling.children[i] = children[midIndex + 1 + i];
                sibling.children[i].parent = sibling;
            }
            sibling.children[sibling.numKeys] = children[numKeys];
            sibling.children[sibling.numKeys].parent = sibling;
            
            numKeys = midIndex;
            
            return sibling;
        }
        
        void insertChild(Double key, Node child) {
            int pos = 0;
            while (pos < numKeys && keys[pos] < key) {
                pos++;
            }
            
            // 移动元素
            for (int i = numKeys; i > pos; i--) {
                keys[i] = keys[i - 1];
                children[i + 1] = children[i];
            }
            
            keys[pos] = key;
            children[pos + 1] = child;
            child.parent = this;
            numKeys++;
        }
        
        int getChildIndex(Node child) {
            for (int i = 0; i <= numKeys; i++) {
                if (children[i] == child) return i;
            }
            return -1;
        }
    }
    
    /**
     * 叶子节点
     */
    private class LeafNode extends Node {
        Object[] values;
        LeafNode next; // 指向下一个叶子节点
        LeafNode prev; // 指向上一个叶子节点
        
        LeafNode() {
            this.keys = new Double[ORDER];
            this.values = new Object[ORDER];
            this.numKeys = 0;
        }
        
        @Override
        Double getFirstKey() {
            return keys[0];
        }
        
        @Override
        boolean isOverflow() {
            return numKeys > ORDER - 1;
        }
        
        @Override
        Node split() {
            int midIndex = numKeys / 2;
            LeafNode sibling = new LeafNode();
            
            // 复制后半部分到兄弟节点
            sibling.numKeys = numKeys - midIndex;
            for (int i = 0; i < sibling.numKeys; i++) {
                sibling.keys[i] = keys[midIndex + i];
                sibling.values[i] = values[midIndex + i];
            }
            
            numKeys = midIndex;
            
            // 维护叶子节点链表
            sibling.next = this.next;
            sibling.prev = this;
            if (this.next != null) {
                this.next.prev = sibling;
            }
            this.next = sibling;
            
            return sibling;
        }
        
        void insert(Double key, Object value) {
            int pos = 0;
            while (pos < numKeys && keys[pos] < key) {
                pos++;
            }
            
            // 移动元素
            for (int i = numKeys; i > pos; i--) {
                keys[i] = keys[i - 1];
                values[i] = values[i - 1];
            }
            
            keys[pos] = key;
            values[pos] = value;
            numKeys++;
        }
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 查找应该插入的叶子节点
     */
    private LeafNode findLeafNode(Double key) {
        Node current = root;
        while (!(current instanceof BPlusTree.LeafNode)) {
            InternalNode internal = (InternalNode) current;
            int i = 0;
            while (i < internal.numKeys && key >= internal.keys[i]) {
                i++;
            }
            current = internal.children[i];
        }
        return (LeafNode) current;
    }
    
    /**
     * 向父节点插入新的键和子节点
     */
    private void insertIntoParent(Node left, Double key, Node right) {
        if (left.parent == null) {
            // 创建新的根节点
            InternalNode newRoot = new InternalNode();
            newRoot.keys[0] = key;
            newRoot.children[0] = left;
            newRoot.children[1] = right;
            newRoot.numKeys = 1;
            
            left.parent = newRoot;
            right.parent = newRoot;
            root = newRoot;
        } else {
            InternalNode parent = left.parent;
            parent.insertChild(key, right);
            
            if (parent.isOverflow()) {
                Double midKey = parent.keys[parent.numKeys / 2];
                Node newNode = parent.split();
                insertIntoParent(parent, midKey, newNode);
            }
        }
    }
}
