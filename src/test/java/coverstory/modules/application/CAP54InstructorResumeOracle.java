package coverstory.modules.application;

import coverstory.modules.application.api.CAP54ResumeUseCase;
import coverstory.modules.application.api.CAP54Seam;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/** Independent test-only Application adapter over the disposable PostgreSQL fixture. */
final class CAP54InstructorResumeOracle implements CAP54ResumeUseCase {
    static final UUID OWNER_A = UUID.fromString("3c7d76dc-29dd-4f2c-a979-8c4a4000a001");
    static final UUID OWNER_B = UUID.fromString("3c7d76dc-29dd-4f2c-a979-8c4a4000a002");
    private final JdbcTemplate jdbc;
    private final AtomicInteger acceptedStateReads = new AtomicInteger();
    private final AtomicInteger repositoryCalls = new AtomicInteger();

    CAP54InstructorResumeOracle(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    int acceptedStateReads() { return acceptedStateReads.get(); }
    int repositoryCalls() { return repositoryCalls.get(); }
    void resetCounts() { acceptedStateReads.set(0); repositoryCalls.set(0); }

    @Override public CAP54Seam.Reply resume(String owner, UUID journeyId) {
        repositoryCalls.incrementAndGet();
        List<CAP54Seam.SafeSummary> rows = jdbc.query("""
                SELECT j.journey_id, a.status
                  FROM application.journey j
                  JOIN application.application_record a
                    ON a.application_id=j.application_id AND a.owner_id=j.owner_id AND a.revision=j.revision
                 WHERE j.owner_id=?::uuid AND j.journey_id=?::uuid
                """, (rs, row) -> new CAP54Seam.SafeSummary(UUID.fromString(rs.getString(1)), rs.getString(2), action(rs.getString(2))), owner, journeyId);
        if (rows.isEmpty()) return new CAP54Seam.Reply(404, "NOT_FOUND", null, null);
        acceptedStateReads.incrementAndGet();
        return new CAP54Seam.Reply(200, null, rows.getFirst(), null);
    }

    @Override public CAP54Seam.Reply list(String owner, int limit, String cursor) {
        repositoryCalls.incrementAndGet();
        if (limit < 1 || limit > 50) return new CAP54Seam.Reply(400, "INVALID_LIMIT", null, null);
        UUID before = null;
        if (cursor != null) {
            if (!cursor.startsWith(owner + ".")) return new CAP54Seam.Reply(400, "INVALID_CURSOR", null, null);
            try { before = UUID.fromString(cursor.substring(cursor.lastIndexOf('.') + 1)); }
            catch (RuntimeException invalid) { return new CAP54Seam.Reply(400, "INVALID_CURSOR", null, null); }
        }
        List<CAP54Seam.SafeSummary> rows = jdbc.query("""
                SELECT j.journey_id, a.status
                  FROM application.journey j
                  JOIN application.application_record a
                    ON a.application_id=j.application_id AND a.owner_id=j.owner_id AND a.revision=j.revision
                 WHERE j.owner_id=?::uuid AND (?::uuid IS NULL OR j.journey_id < ?::uuid)
                 ORDER BY j.journey_id LIMIT ?
                """, (rs, row) -> new CAP54Seam.SafeSummary(UUID.fromString(rs.getString(1)), rs.getString(2), action(rs.getString(2))),
                owner, before, before, limit);
        return new CAP54Seam.Reply(200, null, null, new CAP54Seam.Page(rows, null));
    }

    private static String action(String status) {
        return switch (status) {
            case "DRAFT" -> "CONTINUE_CHECKOUT";
            case "SUBMITTED_PENDING_DISPATCH" -> "WAIT_FOR_DISPATCH";
            default -> "REVIEW_STATUS";
        };
    }
}
