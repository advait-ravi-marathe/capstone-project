package coverstory.application;

import java.util.List;
import java.sql.Connection;

/** Learner seam for rehearsal only. No production migration is applied by this type. */
public final class RehearseCompatibleApplicationSchemaExpansion {
    public enum Status { VERIFIED, ROLLED_BACK, REJECTED_DESTRUCTIVE_DOWN, INVALID }
    public enum Rehearsal { ADDITIVE_EXPANSION, INJECTED_FAILURE, GUARDED_DOWN, ISOLATED_RESTORE }

    public record Revision(String applicationId, String revisionId, int revision, String quoteHash,
                           String revisionHash, String termsHash, String sourceVersion) { }
    public record PaymentReference(String paymentId, String applicationId, int revision) { }
    public record SchemaV1(List<Revision> revisions, List<PaymentReference> paymentReferences) { }
    public record MigrationEvidence(Status status, int revisionCount, int paymentReferenceCount,
                                    boolean legacyReaderCompatible, boolean immutableHashesPreserved,
                                    String explanation) { }

    /**
     * TODO(CAP-98): Draft and verify an additive migration against the disposable fixture only.
     * The learner must preserve v1 readers, roll back failed DDL and refuse a destructive down
     * when v2-only values exist. Never connect this seam to a production datasource.
     */
    public MigrationEvidence rehearseExpansion(Connection disposableConnection, SchemaV1 schemaV1,
                                                Rehearsal rehearsal, String migrationSql,
                                                String legacyReaderSql) {
        throw new UnsupportedOperationException("TODO(CAP-98): rehearse compatible schema expansion");
    }
}
