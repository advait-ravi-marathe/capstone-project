package coverstory.application;

import java.util.List;

/** Learner seam for rehearsal only. No production migration is applied by this type. */
public final class RehearseCompatibleApplicationSchemaExpansion {
    public enum Status { VERIFIED, ROLLED_BACK, REJECTED_DESTRUCTIVE_DOWN, INVALID }

    public record Revision(String ownerId, String revisionId, int revision, String quoteHash,
                           String termsHash, String sourceVersion) { }
    public record PaymentReference(String paymentId, String revisionId) { }
    public record SchemaV1(List<Revision> revisions, List<PaymentReference> paymentReferences) { }
    public record MigrationEvidence(Status status, int revisionCount, int paymentReferenceCount,
                                    boolean legacyReaderCompatible, boolean immutableHashesPreserved,
                                    String explanation) { }

    /**
     * TODO(CAP-98): Draft and verify an additive migration against the disposable fixture only.
     * The learner must preserve v1 readers, roll back failed DDL and refuse a destructive down
     * when v2-only values exist. Never connect this seam to a production datasource.
     */
    public MigrationEvidence rehearseExpansion(SchemaV1 schemaV1, String migrationSql,
                                                String legacyReaderSql) {
        throw new UnsupportedOperationException("TODO(CAP-98): rehearse compatible schema expansion");
    }
}
