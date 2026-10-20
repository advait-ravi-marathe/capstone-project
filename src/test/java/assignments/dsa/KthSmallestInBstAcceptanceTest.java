package assignments.dsa;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class KthSmallestInBstAcceptanceTest {
    @Test void returnsOneBasedInorderRanksAcrossSubtrees() {
        var root = new KthSmallestInBst.TreeNode(5,
                new KthSmallestInBst.TreeNode(3, new KthSmallestInBst.TreeNode(2,
                        new KthSmallestInBst.TreeNode(1, null, null), null),
                        new KthSmallestInBst.TreeNode(4, null, null)),
                new KthSmallestInBst.TreeNode(6, null, null));
        var subject = new KthSmallestInBst();
        assertEquals(1, subject.kthSmallest(root, 1));
        assertEquals(3, subject.kthSmallest(root, 3));
        assertEquals(6, subject.kthSmallest(root, 6));
    }
}
