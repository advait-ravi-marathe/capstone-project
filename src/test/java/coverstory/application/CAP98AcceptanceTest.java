package coverstory.application;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Learner contract only; current failures are the explicit CAP-98 TODO. */
@Tag("learnerAcceptance")
class CAP98AcceptanceTest {
    private final RehearseCompatibleApplicationSchemaExpansion seam =
            new RehearseCompatibleApplicationSchemaExpansion();
    private final RehearseCompatibleApplicationSchemaExpansion.SchemaV1 schema =
            new RehearseCompatibleApplicationSchemaExpansion.SchemaV1(
            List.of(new RehearseCompatibleApplicationSchemaExpansion.Revision("owner-a", "rev-1", 1,
                    "a".repeat(64), "b".repeat(64), null)),
            List.of(new RehearseCompatibleApplicationSchemaExpansion.PaymentReference("payment-1", "rev-1")));

    @Test void nullableAdditiveSourceVersionKeepsV1ReaderAndPaymentReferences() {
        var result = seam.rehearseExpansion(schema, "ALTER TABLE application_revision ADD COLUMN source_version text NULL",
                "SELECT revision_id, revision, quote_hash, revision_hash FROM application_revision");
        assertEquals(RehearseCompatibleApplicationSchemaExpansion.Status.VERIFIED, result.status());
        assertEquals(1, result.revisionCount());
        assertEquals(1, result.paymentReferenceCount());
        assertTrue(result.legacyReaderCompatible());
        assertTrue(result.immutableHashesPreserved());
    }

    @Test void failedMigrationRollsBackSchemaAndRows() {
        var result = seam.rehearseExpansion(schema,
                "ALTER TABLE application_revision ADD COLUMN source_version text NULL; -- inject failure before commit",
                "SELECT revision_id, revision, quote_hash, revision_hash FROM application_revision");
        assertEquals(RehearseCompatibleApplicationSchemaExpansion.Status.ROLLED_BACK, result.status());
        assertEquals(1, result.revisionCount());
        assertEquals(1, result.paymentReferenceCount());
    }

    @Test void downMigrationRefusesToEraseV2OnlyValues() {
        var result = seam.rehearseExpansion(schema,
                "ALTER TABLE application_revision ADD COLUMN source_version text NULL; seed v2-only value",
                "SELECT revision_id, revision, quote_hash, revision_hash FROM application_revision");
        assertEquals(RehearseCompatibleApplicationSchemaExpansion.Status.REJECTED_DESTRUCTIVE_DOWN, result.status());
    }

    @Test void isolatedRestoreMustMatchCountsForeignKeysAndImmutableHashes() {
        var result = seam.rehearseExpansion(schema,
                "RESTORE SYNTHETIC SNAPSHOT INTO ISOLATED DATABASE THEN VERIFY",
                "SELECT revision_id, revision, quote_hash, revision_hash FROM application_revision");
        assertEquals(RehearseCompatibleApplicationSchemaExpansion.Status.VERIFIED, result.status());
        assertTrue(result.immutableHashesPreserved());
        assertEquals(1, result.revisionCount());
        assertEquals(1, result.paymentReferenceCount());
    }
}
