package coverstory.modules.catalog.api;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/** CAP-57 transaction seam for the catalog module's prepared version and export. */
public final class CAP57Seam {
    public record PreparedVersion(UUID productId, UUID versionId, byte[] contentSha256,
                                  long amountMinor, String currency) { }
    public record ExportRow(UUID productId, UUID versionId, byte[] contentSha256,
                            long amountMinor, String currency) { }
    public enum Outcome { PUBLISHED, DUPLICATE, CONFLICT, NOT_FOUND, FAILED }
    public record PublishResult(int statusCode, Outcome outcome, ExportRow exportRow, String safeCode) { }

    /** Testable failure boundary; production wiring supplies a no-op implementation. */
    @FunctionalInterface
    public interface BeforeCommit {
        void run() throws SQLException;
    }

    private final DataSource dataSource;
    private final BeforeCommit beforeCommit;

    public CAP57Seam(DataSource dataSource, BeforeCommit beforeCommit) {
        this.dataSource = dataSource;
        this.beforeCommit = beforeCommit;
    }

    public PublishResult publish(PreparedVersion version) {
        // LEARNER TODO: compare this immutable prepared version, atomically
        // activate it and write its deterministic export state, return a
        // duplicate without rewriting source rows, conflict on changed terms,
        // and roll back all writes on pre-commit failure.
        throw new UnsupportedOperationException("CAP-57 learner TODO: implement catalog publication transaction");
    }

    /** Contract entry point: the caller supplies the authenticated editor principal, never a request-body identity. */
    public PublishResult publish(UUID trustedEditorId, PreparedVersion version) {
        // LEARNER TODO: implement the same atomic publication contract as
        // publish(PreparedVersion), attributing the audit row to trustedEditorId.
        // Keep all writes, including the required publication audit, in one transaction.
        throw new UnsupportedOperationException("CAP-57 learner TODO: implement actor-attributed publication transaction");
    }

    public List<ExportRow> exportPublished() throws SQLException {
        // LEARNER TODO: read only published IDs, stored content hashes, integer
        // minor-unit amounts and currency in stable product/version order.
        throw new UnsupportedOperationException("CAP-57 learner TODO: implement deterministic catalog export");
    }
}
