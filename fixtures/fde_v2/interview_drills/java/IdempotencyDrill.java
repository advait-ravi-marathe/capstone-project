import java.util.Map;
import java.util.Set;

/** Isolated backend interview drill; not capstone application code. */
public class IdempotencyDrill {
    static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
    static int apply(Map<String, Integer> state, Set<String> completed, String actionId, int delta) {
        int next = state.getOrDefault("balance", 0) + delta; // seeded defect: replay applies twice
        state.put("balance", next);
        completed.add(actionId);
        return next;
    }
    public static void main(String[] args) {
        Map<String, Integer> state = new java.util.HashMap<>();
        Set<String> completed = new java.util.HashSet<>();
        check(apply(state, completed, "a-1", 5) == 5, "first action");
        check(apply(state, completed, "a-1", 5) == 5, "replay returns prior state");
        check(state.get("balance") == 5, "one side effect only");
        System.out.println("PASS");
    }
}
