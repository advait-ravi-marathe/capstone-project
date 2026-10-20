package assignments.insurance;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Instant;

/** Learner-owned atomic selection/audit write; event data must remain minimal. */
public final class SelectionAuditService {
    private final DataSource dataSource;
    public SelectionAuditService(DataSource dataSource) { this.dataSource = dataSource; }

    /** Returns true only when a new selection and its audit event are committed. */
    public boolean recordFirstSelection(String resourceId, String actorId, Instant occurredAt) throws SQLException {
        // LEARNER TODO: insert selection and SELECTION_CREATED audit in one transaction.
        // Repeats append no event; rollback must remove both writes. Store no payload.
        throw new UnsupportedOperationException("Day 26 learner TODO: write minimal audit atomically");
    }
}
