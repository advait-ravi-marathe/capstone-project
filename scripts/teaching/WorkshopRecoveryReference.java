import java.util.Map;

public class WorkshopRecoveryReference {
    record Checkpoint(boolean recoveryRequired, int practiceMinutes, boolean newWorkAdded) { }

    static Checkpoint assess(Map<String, Boolean> evidence, int practiceMinutes) {
        if (practiceMinutes < 0 || practiceMinutes > 85) {
            throw new IllegalArgumentException("practice minutes must be within 0..85");
        }
        boolean guardPassed = evidence != null && Boolean.TRUE.equals(evidence.get("guard-check"));
        return new Checkpoint(!guardPassed, practiceMinutes, false);
    }

    public static void main(String[] args) {
        expect(new Checkpoint(true, 60, false), Map.of("guard-check", false, "bench-clean", true), 60);
        expect(new Checkpoint(true, 60, false), Map.of(), 60);
        expect(new Checkpoint(false, 60, false), Map.of("guard-check", true), 60);
        expect(new Checkpoint(false, 0, false), Map.of("guard-check", true), 0);
        expect(new Checkpoint(true, 85, false), Map.of("guard-check", false), 85);
        reject(Map.of("guard-check", false), -1);
        reject(Map.of("guard-check", false), 86);
        System.out.println("PASS: required evidence, bounded practice, no new work");
    }

    private static void expect(Checkpoint expected, Map<String, Boolean> evidence, int minutes) {
        if (!expected.equals(assess(evidence, minutes))) {
            throw new AssertionError("Unexpected checkpoint for " + evidence + " at " + minutes);
        }
    }

    private static void reject(Map<String, Boolean> evidence, int minutes) {
        try {
            assess(evidence, minutes);
            throw new AssertionError("Out-of-range practice time accepted: " + minutes);
        } catch (IllegalArgumentException expected) {
            // Expected bounded-input rejection.
        }
    }
}
