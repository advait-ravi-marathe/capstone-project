package capstone.coverstory.application;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Independent, side-effect-free specification witness for CAP-110. */
final class CAP110InstructorOracle {
    private CAP110InstructorOracle() { }

    record Config(boolean valid, String code, Map<String, Object> manifest) { }
    record Gate(boolean ready, List<String> blockers, boolean submissionPending, boolean dispatchCreated) { }

    private static final Set<String> ALLOWED_KEYS = Set.of("provider.mode", "timeoutMillis", "inputBudget", "outputBudget", "provider.apiKey");

    static Config validateConfig(Map<String, String> raw) {
        if (raw == null || raw.keySet().stream().anyMatch(key -> !ALLOWED_KEYS.contains(key)))
            return new Config(false, "STARTUP_REJECTED", Map.of());
        try {
            String mode = raw.getOrDefault("provider.mode", "");
            int timeout = Integer.parseInt(raw.getOrDefault("timeoutMillis", "0"));
            int input = Integer.parseInt(raw.getOrDefault("inputBudget", "0"));
            int output = Integer.parseInt(raw.getOrDefault("outputBudget", "0"));
            if (!mode.equals("mock") || timeout < 1 || timeout > 10_000 || input < 1 || input > 20_000 || output < 1 || output > 4_000)
                return new Config(false, "STARTUP_REJECTED", Map.of());
            Map<String, Object> manifest = new LinkedHashMap<>();
            manifest.put("providerMode", mode);
            manifest.put("timeoutMillis", timeout);
            manifest.put("inputBudget", input);
            manifest.put("outputBudget", output);
            manifest.put("secretConfigured", raw.containsKey("provider.apiKey") && !raw.get("provider.apiKey").isBlank());
            manifest.put("networkCallsAllowed", false);
            return new Config(true, "VALID", Map.copyOf(manifest));
        } catch (NumberFormatException invalidInteger) {
            return new Config(false, "STARTUP_REJECTED", Map.of());
        }
    }

    static Gate evaluate(boolean configValid, boolean owner, boolean fields, boolean disclosures,
                         boolean extractionConfirmed, boolean exactReview, boolean balanceReconciled,
                         boolean refundsReconciled, boolean explicitAction) {
        List<String> blockers = new ArrayList<>();
        if (!configValid) blockers.add("CONFIGURATION_INVALID");
        if (!owner) blockers.add("OWNER_UNVERIFIED");
        if (!fields) blockers.add("REQUIRED_FIELDS_INCOMPLETE");
        if (!disclosures) blockers.add("DISCLOSURES_INCOMPLETE");
        if (!extractionConfirmed) blockers.add("EXTRACTION_UNCONFIRMED");
        if (!exactReview) blockers.add("REVIEW_STALE_OR_MISMATCHED");
        if (!balanceReconciled) blockers.add("BALANCE_UNRECONCILED");
        if (!refundsReconciled) blockers.add("REFUND_UNRECONCILED");
        boolean ready = blockers.isEmpty();
        // A ready draft is still inert until an explicit user action. This oracle never
        // dispatches or calls an insurer, even when that action is represented in a fixture.
        return new Gate(ready, List.copyOf(blockers), false, false);
    }
}
