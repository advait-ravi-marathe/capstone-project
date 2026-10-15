package capstone.coverstory.support;

import capstone.coverstory.support.internal.ApplicationEvidenceToolSchema;
import java.util.*;

/** Instructor-owned contract oracle, intentionally independent of the learner TODO seam. */
final class CAP124ReferenceOracle {
    record Rejection(String code) { }
    static ApplicationEvidenceToolSchema.Result invoke(String principalOwner, Map<String, ?> args,
            ApplicationEvidenceToolSchema.ApplicationReadApi api) {
        if (principalOwner == null || principalOwner.isBlank()) return new ApplicationEvidenceToolSchema.Result("REJECTED", "UNAUTHENTICATED", List.of());
        if (args == null || !args.keySet().equals(Set.of("applicationId", "limit"))) return new ApplicationEvidenceToolSchema.Result("REJECTED", "SCHEMA_REJECTED", List.of());
        Object id = args.get("applicationId"), limit = args.get("limit");
        if (!(id instanceof String app) || !app.matches("app-[A-Z0-9]{1,12}")) return new ApplicationEvidenceToolSchema.Result("REJECTED", "SCHEMA_REJECTED", List.of());
        if (!(limit instanceof Integer n) || n < 1 || n > 50) return new ApplicationEvidenceToolSchema.Result("REJECTED", "SCHEMA_REJECTED", List.of());
        var rows = api.evidenceForOwner(principalOwner, app, n);
        return new ApplicationEvidenceToolSchema.Result("OK", "READ_ONLY", rows);
    }
}
