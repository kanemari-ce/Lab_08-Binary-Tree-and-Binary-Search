/**
 * One node of the binary search tree: a comparable key, the mapped value,
 * and links to the left and right subtrees.
 *
 * Fields are package-private on purpose: BinarySearchTree (same package) owns
 * the BST invariants (ordering, size, structural links) and mutates them
 * directly, keeping the node a plain data holder.
 */
public class BSTNode<K extends Comparable<K>, V> {

    K key;
    V value;
    BSTNode<K, V> left;
    BSTNode<K, V> right;

    public BSTNode(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
