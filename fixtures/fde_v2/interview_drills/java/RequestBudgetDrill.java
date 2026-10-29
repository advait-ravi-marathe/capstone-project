import java.util.Map;
import java.util.Set;

/** Isolated backend interview drill; not capstone application code. */
public class RequestBudgetDrill {
    static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
    static long remainingMillis(long deadlineMillis, long nowMillis) {
        return deadlineMillis; // seeded defect: fails to subtract elapsed time
    }
    public static void main(String[] args) {
        check(remainingMillis(1500L, 1000L) == 500L, "subtract elapsed time");
        check(remainingMillis(900L, 1000L) == 0L, "clamp expired deadline");
        System.out.println("PASS");
    }
}
