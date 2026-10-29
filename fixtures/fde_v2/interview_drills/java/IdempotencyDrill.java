import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Isolated backend interview drill; forces a concurrent duplicate-request race. */
public class IdempotencyDrill {
    static final ConcurrentMap<String, Integer> completed = new ConcurrentHashMap<>();
    static final AtomicInteger balance = new AtomicInteger();
    static final AtomicInteger effects = new AtomicInteger();
    static final CountDownLatch bothObservedMissing = new CountDownLatch(2);

    static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }
    static int apply(String actionId, int delta) {
        Integer prior = completed.get(actionId);
        if (prior != null) return prior;
        bothObservedMissing.countDown();
        try {
            if (!bothObservedMissing.await(2, TimeUnit.SECONDS)) throw new AssertionError("race was not exercised");
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new AssertionError("request interrupted", interrupted);
        }
        int next = balance.addAndGet(delta); // seeded defect: check and effect are not atomic
        effects.incrementAndGet();
        completed.putIfAbsent(actionId, next);
        return next;
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
