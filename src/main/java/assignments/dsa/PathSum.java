package assignments.dsa;

/** LeetCode 112 learner seam: decide whether a root-to-leaf path sums to target. */
public final class PathSum {
    public boolean hasPathSum(TreeNode root, int targetSum) {
        // LEARNER TODO Day 73: only root-to-leaf paths count; preserve signed values.
        throw new UnsupportedOperationException("DSA learner TODO: implement hasPathSum");
    }

    public record TreeNode(int val, TreeNode left, TreeNode right) { }
}
