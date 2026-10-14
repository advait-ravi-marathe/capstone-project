package coverstory.modules.payments;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/** Test-only deterministic model: no broker, network, provider, or payment write. */
final class CAP93OutageOracle {
    record Snapshot(int pending, int uniqueCompleted, int businessEffects, int paymentAttempts) { }
    private record Job(String id, int businessEffects, int paymentAttempts) { }

    private final Map<String, Job> pending = new LinkedHashMap<>();
    private final Map<String, Job> completed = new LinkedHashMap<>();
    private Duration virtualTime = Duration.ZERO;
    private boolean providerAvailable;

    void acceptReadJob(String id) { pending.putIfAbsent(id, new Job(id, 0, 0)); }
    void advanceBy(Duration d) { virtualTime = virtualTime.plus(d); }
    Duration virtualTime() { return virtualTime; }
    void recover() { providerAvailable = true; }

    void deliver(String id) {
        if (!providerAvailable) return;
        Job job = pending.remove(id);
        if (job == null) return; // completed ID is a dedupe key; redelivery is inert
        completed.putIfAbsent(id, job);
    }

    Snapshot snapshot() {
        int effects = completed.values().stream().mapToInt(Job::businessEffects).sum();
        int paymentAttempts = completed.values().stream().mapToInt(Job::paymentAttempts).sum();
        return new Snapshot(pending.size(), completed.size(), effects, paymentAttempts);
    }
}
