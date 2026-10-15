package coverstory.modules.catalog;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;
import coverstory.modules.catalog.api.CAP57Seam;
import coverstory.modules.catalog.api.CAP57Seam.PreparedVersion;
import coverstory.modules.catalog.api.CAP57Seam.ExportRow;
import coverstory.modules.catalog.api.CAP57Seam.PublishResult;
import coverstory.modules.catalog.api.CAP57Seam.Outcome;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP57AcceptanceTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-57/synthetic-v1.json");
    private CAP57TestDatabase testDatabase;
    private EmbeddedPostgres postgres;

    @BeforeEach void startFreshPostgresAndLoadSyntheticPreparedVersions() throws Exception {
        testDatabase = CAP57TestDatabase.fresh();
        postgres = testDatabase.postgres();
    }

    @AfterEach void stopPostgres() throws Exception { if (testDatabase != null) testDatabase.close(); }

    @Test void publishesOnlyTheSelectedVersionAndExportSurvivesServiceRestart() throws Exception {
        PreparedVersion selected = version(0);
        PublishResult result = seam(noFailure()).publish(selected);
        assertEquals(201, result.statusCode());
        assertEquals(Outcome.PUBLISHED, result.outcome());
        CAP57Seam restartedService = seam(noFailure());
        List<ExportRow> export = restartedService.exportPublished();
        assertEquals(1, export.size());
        assertRowMatches(export.get(0), selected);
    }

    @Test void repeatedSameProductVersionAndHashIsDuplicateWithoutSourceRewrite() throws Exception {
        PreparedVersion selected = version(0);
        assertEquals(Outcome.PUBLISHED, seam(noFailure()).publish(selected).outcome());
        PublishResult duplicate = seam(noFailure()).publish(selected);
        assertEquals(Outcome.DUPLICATE, duplicate.outcome());
        assertEquals(1, seam(noFailure()).exportPublished().size());
        assertRowMatches(seam(noFailure()).exportPublished().get(0), selected);
    }

    @Test void changedRestrictionsHashConflictsAndLeavesPriorTermsSelected() throws Exception {
        PreparedVersion selected = version(0);
        assertEquals(Outcome.PUBLISHED, seam(noFailure()).publish(selected).outcome());
        PreparedVersion changed = withChangedRestrictions(selected);
        PublishResult conflict = seam(noFailure()).publish(changed);
        assertEquals(409, conflict.statusCode());
        assertEquals(Outcome.CONFLICT, conflict.outcome());
        assertRowMatches(seam(noFailure()).exportPublished().get(0), selected);
    }

    @Test void injectedPreCommitSqlFailureLeavesNoPartialPublication() throws Exception {
        CAP57Seam failing = seam(() -> { throw new java.sql.SQLException("synthetic injected failure"); });
        PublishResult result = failing.publish(version(0));
        assertEquals(503, result.statusCode());
        assertEquals(Outcome.FAILED, result.outcome());
        assertEquals("PUBLICATION_UNAVAILABLE", result.safeCode());
        assertTrue(seam(noFailure()).exportPublished().isEmpty());
    }

    private CAP57Seam seam(CAP57Seam.BeforeCommit hook) {
        return new CAP57Seam(postgres.getPostgresDatabase(), hook);
    }

    private static CAP57Seam.BeforeCommit noFailure() { return () -> { }; }

    private static PreparedVersion version(int index) throws Exception {
        JsonNode row = JSON.readTree(Files.readString(FIXTURE)).path("prepared_versions").get(index);
        return new PreparedVersion(java.util.UUID.fromString(row.path("product_id").asText()),
                java.util.UUID.fromString(row.path("version_id").asText()),
                java.util.HexFormat.of().parseHex(row.path("content_sha256").asText()),
                row.path("amount_minor").asLong(), row.path("currency").asText());
    }

    private static PreparedVersion withChangedRestrictions(PreparedVersion original) throws Exception {
        JsonNode row = JSON.readTree(Files.readString(FIXTURE)).path("prepared_versions").get(0);
        Map<String, Object> canonical = new TreeMap<>();
        canonical.put("product_id", original.productId().toString());
        canonical.put("version_id", original.versionId().toString());
        canonical.put("source_version", row.path("source_version").asText());
        Map<String, Object> restrictions = new TreeMap<>();
        restrictions.put("exclusions", List.of("synthetic-exclusion-A"));
        restrictions.put("waiting_period_months", 0);
        canonical.put("restrictions", restrictions);
        canonical.put("amount_minor", original.amountMinor());
        canonical.put("currency", original.currency());
        byte[] changedHash = MessageDigest.getInstance("SHA-256").digest(
                JSON.writeValueAsString(canonical).getBytes(StandardCharsets.UTF_8));
        return new PreparedVersion(original.productId(), original.versionId(), changedHash,
                original.amountMinor(), original.currency());
    }

    private static void assertRowMatches(ExportRow row, PreparedVersion expected) {
        assertEquals(expected.productId(), row.productId());
        assertEquals(expected.versionId(), row.versionId());
        assertArrayEquals(expected.contentSha256(), row.contentSha256());
        assertEquals(expected.amountMinor(), row.amountMinor());
        assertEquals(expected.currency(), row.currency().trim());
    }

}
