import java.util.List;

/**
 * Student-authored unit tests for Lab 8, written BEFORE the BST implementation.
 * Run with the dependency-free runner:
 *
 *     java -cp out LabTestRunner BSTEdgeCaseTest
 *
 * Each test documents its input/setup, the expected behavior, and why the case
 * is worth testing. While the tree methods are unimplemented TODO stubs, every
 * test here must FAIL; the same tests must ALL pass once the implementation is
 * complete (test-first evidence).
 */
public class BSTEdgeCaseTest {

    /**
     * Shared fixture: the exact lab tree built by inserting
     * [40, 20, 70, 10, 30, 55, 85, 60] in that order, with values "v<key>".
     */
    private BinarySearchTree<Integer, String> buildLabTree() {
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<>();
        int[] insertionOrder = {40, 20, 70, 10, 30, 55, 85, 60};
        for (int key : insertionOrder) {
            tree.put(key, "v" + key);
        }
        return tree;
    }

    /**
     * Edge case: empty tree.
     *
     * Input/Setup: a fresh BinarySearchTree with no inserts.
     * Expected: size() == 0, isEmpty() == true, get(anyKey) == null,
     *           delete(anyKey) == null, rangeSearch(any) == empty list.
     * Why: every operation dereferences the root; a naive or stubbed
     *      implementation throws NullPointerException here first, so this
     *      verifies the null-root guards before anything else.
     */
    public void testEmptyTree() {
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<>();
        LabTestRunner.assertEquals(0, tree.size(), "empty tree size");
        LabTestRunner.assertTrue(tree.isEmpty(), "empty tree isEmpty()");
        LabTestRunner.assertNull(tree.get(40), "get on an empty tree");
        LabTestRunner.assertNull(tree.delete(40), "delete on an empty tree");
        LabTestRunner.assertTrue(tree.rangeSearch(10, 85).isEmpty(), "rangeSearch on an empty tree");
    }

    /**
     * Edge case: inverted range bounds must return an empty collection.
     *
     * Input/Setup: the lab tree [40, 20, 70, 10, 30, 55, 85, 60].
     * Expected: rangeSearch(85, 10) returns an EMPTY list (not an exception and
     *           not partial data). Control: rangeSearch(30, 60) returns
     *           [v30, v40, v55, v60] in ascending order, inclusive on both ends.
     * Why: an inverted [max..min] range is the classic contract bug: stubs and
     *      buggy pruning either throw, return everything, or return garbage.
     *      The positive control proves the method works, so the empty result
     *      for inverted bounds is meaningful rather than an accident.
     */
    public void testRangeSearchInvertedBounds() {
        BinarySearchTree<Integer, String> tree = buildLabTree();
        LabTestRunner.assertTrue(tree.rangeSearch(85, 10).isEmpty(), "inverted bounds return empty");
        LabTestRunner.assertEquals(List.of("v30", "v40", "v55", "v60"),
                tree.rangeSearch(30, 60), "inclusive range [30,60] ascending");
    }

    /**
     * Edge case: two-child deletion where the successor has a right child.
     *
     * Input/Setup: the lab tree; delete(40). The in-order successor is 55
     *              (right child 70, then leftmost) and 55 has a right child 60
     *              that must be reattached to 70 after the splice.
     * Expected: delete returns "v40"; 40 is gone; size == 7; get(60) still
     *           works (successor's right child survived); get(55) works with
     *           no duplicate left behind; keys 10/20/30/70/85 intact;
     *           in-order keys remain sorted.
     * Why: copying the successor up without reattaching its right child
     *      silently loses node 60; get(60) is exactly the assertion that
     *      catches that mistake. This mirrors the trace in the lab report.
     */
    public void testDeleteTwoChildrenSuccessorHasRightChild() {
        BinarySearchTree<Integer, String> tree = buildLabTree();
        LabTestRunner.assertEquals("v40", tree.delete(40), "delete returns the removed value");
        LabTestRunner.assertNull(tree.get(40), "40 must be gone");
        LabTestRunner.assertEquals(7, tree.size(), "size after deleting the root");
        LabTestRunner.assertEquals("v60", tree.get(60), "successor's right child 60 must survive the splice");
        LabTestRunner.assertEquals("v55", tree.get(55), "55 promoted into the root position");
        LabTestRunner.assertNotNull(tree.get(70), "70 still present");
        LabTestRunner.assertNotNull(tree.get(10), "10 still present");
        LabTestRunner.assertNotNull(tree.get(20), "20 still present");
        LabTestRunner.assertNotNull(tree.get(30), "30 still present");
        LabTestRunner.assertNotNull(tree.get(85), "85 still present");
        LabTestRunner.assertEquals(List.of(10, 20, 30, 55, 60, 70, 85),
                tree.keysInOrder(), "in-order stays sorted after two-child delete");
    }

    /**
     * Contract: replacing an existing key must not grow the tree.
     *
     * Input/Setup: the lab tree; put(40, "root") over the existing key 40.
     * Expected: put returns the OLD value "v40", get(40) now yields "root",
     *           and size stays 8.
     * Why: a naive put that always creates a node (or always increments the
     *      size) silently corrupts the size contract and duplicates keys.
     */
    public void testPutReplaceKeepsSize() {
        BinarySearchTree<Integer, String> tree = buildLabTree();
        LabTestRunner.assertEquals("v40", tree.put(40, "root"), "put returns the replaced value");
        LabTestRunner.assertEquals("root", tree.get(40), "value replaced in place");
        LabTestRunner.assertEquals(8, tree.size(), "size must not change on replace");
    }

    /**
     * Contract: put must reject null keys and null values.
     *
     * Input/Setup: an empty tree; attempt put(null, "x") and put(1, null).
     * Expected: both throw IllegalArgumentException and the size stays 0.
     * Why: a null key would break compareTo during navigation, and a null
     *      value would make "absent" indistinguishable from "mapped to null".
     */
    public void testPutRejectsNullKeyAndValue() {
        BinarySearchTree<Integer, String> tree = new BinarySearchTree<>();
        LabTestRunner.assertThrows(IllegalArgumentException.class,
                () -> tree.put(null, "x"), "null key must be rejected");
        LabTestRunner.assertThrows(IllegalArgumentException.class,
                () -> tree.put(1, null), "null value must be rejected");
        LabTestRunner.assertEquals(0, tree.size(), "size unchanged after rejected puts");
    }

    /**
     * Delete case 1: node with no children (leaf).
     *
     * Input/Setup: the lab tree; delete(10) - 10 is a leaf under 20.
     * Expected: delete returns "v10"; get(10) == null; size == 7; the rest of
     *           the tree is untouched and in-order stays sorted.
     * Why: the simplest structural case; catches over-eager child reattachment
     *      and size-accounting bugs before the harder cases are examined.
     */
    public void testDeleteLeaf() {
        BinarySearchTree<Integer, String> tree = buildLabTree();
        LabTestRunner.assertEquals("v10", tree.delete(10), "delete returns the removed value");
        LabTestRunner.assertNull(tree.get(10), "leaf 10 is gone");
        LabTestRunner.assertEquals(7, tree.size(), "size after leaf delete");
        LabTestRunner.assertEquals(List.of(20, 30, 40, 55, 60, 70, 85),
                tree.keysInOrder(), "in-order sorted after leaf delete");
    }

    /**
     * Delete case 2: node with one child.
     *
     * Input/Setup: the lab tree; delete(55) - 55 has only a right child (60).
     * Expected: 55 is gone; 60 is reattached as 70's left child; size == 7;
     *           in-order keys stay sorted.
     * Why: verifies the parent adopts the surviving child - the same splice
     *      used when the successor itself is removed in the two-child case.
     */
    public void testDeleteOneChild() {
        BinarySearchTree<Integer, String> tree = buildLabTree();
        LabTestRunner.assertEquals("v55", tree.delete(55), "delete returns the removed value");
        LabTestRunner.assertNull(tree.get(55), "55 is gone");
        LabTestRunner.assertEquals("v60", tree.get(60), "60 reattached under 70");
        LabTestRunner.assertEquals(7, tree.size(), "size after one-child delete");
        LabTestRunner.assertEquals(List.of(10, 20, 30, 40, 60, 70, 85),
                tree.keysInOrder(), "in-order sorted after one-child delete");
    }
}
