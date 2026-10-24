import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;

/** Bounded CPU workload with one explicit, inspectable JFR phase event. */
public final class ProfilingWork {
    private static final long ITERATIONS = 500_000_000L;
    private static final long EXPECTED_CHECKSUM = 7_954_362_999L;

    @Name("lab.WorkPhase")
    @Label("Runtime SQL lab work phase")
    @Category({"Training", "Runtime SQL"})
    static final class WorkPhase extends Event {
        @Label("Workload") String workload;
        @Label("Iterations") long iterations;
        @Label("Checksum") long checksum;
    }

    private ProfilingWork() {}

    public static void main(String[] args) {
        WorkPhase event = new WorkPhase();
        event.workload = "cpu-bitcount-v1";
        event.iterations = ITERATIONS;
        event.begin();
        long checksum = 0;
        for (int i = 1; i <= ITERATIONS; i++) {
            checksum += Integer.bitCount(i * 31);
        }
        event.checksum = checksum;
        event.end();
        event.commit();
        if (checksum != EXPECTED_CHECKSUM) {
            throw new IllegalStateException("unexpected deterministic checksum: " + checksum);
        }
        System.out.println("iterations=" + ITERATIONS + " checksum=" + checksum);
    }
}
