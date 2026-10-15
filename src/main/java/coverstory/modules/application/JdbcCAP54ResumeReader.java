package coverstory.modules.application;

import coverstory.modules.application.api.CAP54Seam;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Application-owned reads over its journey/application tables only. */
public final class JdbcCAP54ResumeReader implements CAP54Seam.OwnedJourneyReader, CAP54Seam.ApplicationStateReader {
    private final JdbcTemplate jdbc;

    public JdbcCAP54ResumeReader(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public Optional<CAP54Seam.JourneyLink> findOwned(String ownerId, UUID journeyId) {
        return jdbc.query("""
                SELECT j.journey_id, j.application_id, j.revision
                  FROM application.journey j
                  JOIN application.application_record a
                    ON a.application_id=j.application_id AND a.owner_id=j.owner_id AND a.revision=j.revision
                 WHERE j.owner_id=?::uuid AND j.journey_id=?::uuid
                """, (rs, row) -> new CAP54Seam.JourneyLink(
                    UUID.fromString(rs.getString("journey_id")), UUID.fromString(rs.getString("application_id")), rs.getLong("revision")),
                ownerId, journeyId).stream().findFirst();
    }

    @Override public CAP54Seam.Page listOwned(String ownerId, int limit, String cursor) {
        if (cursor != null && !CAP54Cursor.belongsTo(cursor, ownerId)) return new CAP54Seam.Page(List.of(), null);
        UUID before = cursor == null ? null : CAP54Cursor.journey(cursor);
        List<CAP54Seam.SafeSummary> rows = jdbc.query("""
                SELECT j.journey_id, a.status
                  FROM application.journey j
                  JOIN application.application_record a
                    ON a.application_id=j.application_id AND a.owner_id=j.owner_id AND a.revision=j.revision
                 WHERE j.owner_id=?::uuid AND (?::uuid IS NULL OR j.journey_id < ?::uuid)
                 ORDER BY j.journey_id LIMIT ?
                """, (rs, row) -> {
                    String status = rs.getString("status");
                    return new CAP54Seam.SafeSummary(UUID.fromString(rs.getString("journey_id")), status, action(status));
                }, ownerId, before, before, Math.max(1, Math.min(50, limit)));
        String next = rows.size() == limit && !rows.isEmpty()
                ? CAP54Cursor.encode(ownerId, rows.get(rows.size()-1).journeyId()) : null;
        return new CAP54Seam.Page(rows, next);
    }

    @Override public Optional<CAP54Seam.AuthoritativeState> load(UUID applicationId, long revision) {
        return jdbc.query("SELECT status FROM application.application_record WHERE application_id=? AND revision=?",
                (rs, row) -> state(rs.getString("status")), applicationId, revision).stream().findFirst();
    }

    public static String action(String status) {
        return switch (status) {
            case "DRAFT" -> "CONTINUE_CHECKOUT";
            case "AWAITING_PAYMENT" -> "WAIT_FOR_PAYMENT";
            case "ONBOARDING" -> "CONTINUE_ONBOARDING";
            case "SUBMITTED_PENDING_DISPATCH" -> "WAIT_FOR_DISPATCH";
            default -> "REVIEW_STATUS";
        };
    }

    private static CAP54Seam.AuthoritativeState state(String status) {
        return new CAP54Seam.AuthoritativeState(status, status.equals("ONBOARDING") || status.equals("SUBMITTED_PENDING_DISPATCH"),
                false);
    }

    static final class CAP54Cursor {
        private CAP54Cursor() { }
        static String encode(String owner, UUID journey) { return owner + "." + journey; }
        static boolean belongsTo(String cursor, String owner) { return cursor.startsWith(owner + ".") && journey(cursor) != null; }
        static UUID journey(String cursor) {
            try { return UUID.fromString(cursor.substring(cursor.lastIndexOf('.') + 1)); }
            catch (RuntimeException invalid) { return null; }
        }
    }
}
