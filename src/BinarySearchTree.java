import java.util.ArrayList;
import java.util.List;

/**
 * Binary search tree keyed by Comparable keys.
 *
 * Contract:
 *  - put(key, value): rejects null keys and null values; if the key already
 *    exists its value is replaced and the size does NOT change; otherwise the
 *    key is inserted as a new leaf and the size grows by one. Returns the old
 *    value, or null for a fresh key. BST ordering is maintained at all times.
 *  - get(key): returns the mapped value, or null when the key is absent.
 *  - rangeSearch(min, max): inclusive on both ends, values returned in
 *    ascending key order; inverted bounds (min > max) yield an empty list.
 *    Subtrees that cannot intersect [min, max] are pruned, never visited.
 *  - delete(key): removes the node with the given key and returns its value
 *    (null when absent). Three cases: leaf, one child, two children. The
 *    two-child case copies the in-order successor (the leftmost node of the
 *    right subtree) into the node, then splices the successor out,
 *    reattaching its right child to its parent.
 */
public class BinarySearchTree<K extends Comparable<K>, V> {

    private BSTNode<K, V> root;
    private int size;

    /** Communication channel from the recursive helpers back to put/delete. */
    private V replacedValue;
    private V removedValue;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public List<K> keysInOrder() {
        List<K> keys = new ArrayList<>();
        keysInOrder(root, keys);
        return keys;
    }

    private void keysInOrder(BSTNode<K, V> node, List<K> keys) {
        if (node == null) {
            return;
        }
        keysInOrder(node.left, keys);
        keys.add(node.key);
        keysInOrder(node.right, keys);
    }

    public V put(K key, V value) {
        if (key == null || value == null) {
            throw new IllegalArgumentException("put requires a non-null key and value");
        }
        replacedValue = null;
        root = put(root, key, value);
        return replacedValue;
    }

    private BSTNode<K, V> put(BSTNode<K, V> node, K key, V value) {
        if (node == null) {
            size++;
            return new BSTNode<>(key, value);
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = put(node.left, key, value);
        } else if (cmp > 0) {
            node.right = put(node.right, key, value);
        } else {
            replacedValue = node.value;
            node.value = value;
        }
        return node;
    }

    public V get(K key) {
        if (key == null) {
            throw new IllegalArgumentException("get requires a non-null key");
        }
        return get(root, key);
    }

    private V get(BSTNode<K, V> node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            return get(node.left, key);
        }
        if (cmp > 0) {
            return get(node.right, key);
        }
        return node.value;
    }

    public List<V> rangeSearch(K min, K max) {
        List<V> values = new ArrayList<>();
        if (min == null || max == null) {
            throw new IllegalArgumentException("rangeSearch requires non-null bounds");
        }
        if (min.compareTo(max) > 0) {
            return values;
        }
        rangeSearch(root, min, max, values);
        return values;
    }

    private void rangeSearch(BSTNode<K, V> node, K min, K max, List<V> values) {
        if (node == null) {
            return;
        }
        int cmpMin = min.compareTo(node.key);
        int cmpMax = max.compareTo(node.key);
        if (cmpMin < 0) {
            rangeSearch(node.left, min, max, values);
        }
        if (cmpMin <= 0 && cmpMax >= 0) {
            values.add(node.value);
        }
        if (cmpMax > 0) {
            rangeSearch(node.right, min, max, values);
        }
    }

    public V delete(K key) {
        if (key == null) {
            throw new IllegalArgumentException("delete requires a non-null key");
        }
        removedValue = null;
        root = delete(root, key);
        return removedValue;
    }

    private BSTNode<K, V> delete(BSTNode<K, V> node, K key) {
        if (node == null) {
            return null;
        }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) {
            node.left = delete(node.left, key);
            return node;
        }
        if (cmp > 0) {
            node.right = delete(node.right, key);
            return node;
        }
        removedValue = node.value;
        size--;
        if (node.left == null) {
            return node.right;
        }
        if (node.right == null) {
            return node.left;
        }
        BSTNode<K, V> successor = min(node.right);
        node.key = successor.key;
        node.value = successor.value;
        node.right = deleteMin(node.right);
        return node;
    }

    private BSTNode<K, V> min(BSTNode<K, V> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private BSTNode<K, V> deleteMin(BSTNode<K, V> node) {
        if (node.left == null) {
            return node.right;
        }
        node.left = deleteMin(node.left);
        return node;
    }
}
