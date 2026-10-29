import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Instructor control: serialize action lookup and the side effect as one operation. */
public class IdempotencyDrill {
    static final ConcurrentMap<String, Integer> completed = new ConcurrentHashMap<>();
    static final AtomicInteger balance = new AtomicInteger();
    static final AtomicInteger effects = new AtomicInteger();
    static synchronized int apply(String actionId, int delta) {
        Integer prior = completed.get(actionId);
        if (prior != null) return prior;
        int next = balance.addAndGet(delta);
        effects.incrementAndGet();
        completed.put(actionId, next);
        return next;
    }
    static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
    public static void main(String[] args) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> first = pool.submit(() -> apply("a-1", 5));
            Future<Integer> replay = pool.submit(() -> apply("a-1", 5));
            check(first.get(3, TimeUnit.SECONDS) == 5, "first result");
            check(replay.get(3, TimeUnit.SECONDS) == 5, "concurrent replay returns original result");
            check(balance.get() == 5 && effects.get() == 1, "exactly one side effect under concurrent replay");
            check(apply("a-1", 5) == 5 && effects.get() == 1, "later replay remains idempotent");
            System.out.println("PASS");
        } finally {
            pool.shutdownNow();
        }
    }
}
