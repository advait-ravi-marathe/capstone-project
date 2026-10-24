/** Small deterministic local workload for a Java Flight Recorder rehearsal. */
public final class ProfilingWork {
    private ProfilingWork() {}

    public static void main(String[] args) {
        long checksum = 0;
        for (int outer = 1; outer <= 200_000; outer++) {
            checksum += Integer.bitCount(outer * 31);
        }
        if (checksum != 2_225_131L) {
            throw new IllegalStateException("unexpected deterministic checksum: " + checksum);
        }
        System.out.println("checksum=" + checksum);
    }
}
