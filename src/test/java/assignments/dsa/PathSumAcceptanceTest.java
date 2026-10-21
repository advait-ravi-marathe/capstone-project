package assignments.dsa;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class PathSumAcceptanceTest {
    private final PathSum subject = new PathSum();
    private final PathSum.TreeNode example = new PathSum.TreeNode(5,
            new PathSum.TreeNode(4, new PathSum.TreeNode(11, new PathSum.TreeNode(7, null, null),
                    new PathSum.TreeNode(2, null, null)), null),
            new PathSum.TreeNode(8, new PathSum.TreeNode(13, null, null),
                    new PathSum.TreeNode(4, null, new PathSum.TreeNode(1, null, null))));

    @Test void countsOnlyRootToLeafPaths() {
        assertTrue(subject.hasPathSum(example, 22));
        assertTrue(subject.hasPathSum(example, 26));
        assertFalse(subject.hasPathSum(example, 5));
        assertFalse(subject.hasPathSum(null, 0));
    }

    @Test void handlesNegativeAndZeroValuesWithoutTreatingAnInternalNodeAsLeaf() {
        assertTrue(subject.hasPathSum(new PathSum.TreeNode(-2, null,
                new PathSum.TreeNode(-3, null, null)), -5));
        assertTrue(subject.hasPathSum(new PathSum.TreeNode(0,
                new PathSum.TreeNode(0, null, null), null), 0));
        assertFalse(subject.hasPathSum(new PathSum.TreeNode(0,
                new PathSum.TreeNode(0, null, null), null), 1));
    }
}
