import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Independent Day 57 example: named evidence flags gate a fictional kiosk rehearsal. */
public class WorkshopReadinessReference {
    enum Decision { REHEARSE, RECOVER }
    private static final Set<String> REQUIRED = Set.of("catalog-read", "owner-filter");

    static Decision assess(Map<String, Boolean> evidence) {
        if (evidence == null) return Decision.RECOVER;
        for (String key : REQUIRED) {
            if (!Boolean.TRUE.equals(evidence.get(key))) return Decision.RECOVER;
        }
        return Decision.REHEARSE;
    }

    static void expect(Decision expected, Map<String, Boolean> input) {
        Map<String, Boolean> before = input == null ? null : new HashMap<>(input);
        if (assess(input) != expected) throw new AssertionError("Wrong readiness decision: " + input);
        if (input != null && !input.equals(before)) throw new AssertionError("Evidence was changed");
    }

    public static void main(String[] args) {
        expect(Decision.RECOVER, null);
        expect(Decision.RECOVER, Map.of());
        expect(Decision.RECOVER, Map.of("catalog-read", true));
        expect(Decision.RECOVER, Map.of("owner-filter", true));
        expect(Decision.RECOVER, Map.of("catalog-read", true, "owner-filter", false));
        expect(Decision.RECOVER, Map.of("packaged-smoke", true, "ui-built", true));
        var nullable = new HashMap<String, Boolean>();
        nullable.put("catalog-read", true);
        nullable.put("owner-filter", null);
        expect(Decision.RECOVER, nullable);
        expect(Decision.REHEARSE, Map.of("catalog-read", true, "owner-filter", true));
        System.out.println("PASS: named prerequisites, missing/failed/null cases, evidence unchanged");
    }
}
