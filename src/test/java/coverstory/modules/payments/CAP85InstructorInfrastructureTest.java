package coverstory.modules.payments;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import coverstory.modules.payments.api.CAP85Seam.PaymentBatchSummary;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Private PostgreSQL reference oracle. It does not invoke the learner TODO. */
class CAP85InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-85/synthetic-v1.json");
    private static final UUID A = id("85000000-0000-4000-8000-000000000001");
    private static final UUID B = id("85000000-0000-4000-8000-000000000002");
    private static final UUID BATCH = id("85000000-0000-4000-8000-000000000101");

    @Test void fixtureIsSyntheticAndRecordsAllFiveAcceptanceShapes() throws Exception {
        JsonNode root = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_OFFLINE_CONTRACT", root.path("classification").asText());
        assertFalse(root.path("actual_provider_call").asBoolean());
        assertEquals(5, root.path("cases").size());
        assertEquals(6, root.path("summary_field_order").size());
    }

    @Test void oneAggregateOracleCountsClassificationAndExactDurableLineage() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            schema(c); seed(c, true, 2);
            assertEquals(new PaymentBatchSummary(4,1,1,2,0,0), summarize(c,A,BATCH));
            try (Statement s=c.createStatement()) { s.executeUpdate("DELETE FROM payments.payment_effect WHERE source_row_id='85000000-0000-4000-8000-000000000002'"); }
            PaymentBatchSummary correct=summarize(c,A,BATCH);
            assertEquals(1,correct.missingInsertedEffectCount());
            // Test-only mutant witness: arithmetic classification totals stay balanced without lineage.
            assertNotEquals(correct,summarizeWithLineageDisabled(c,A,BATCH));
        }
    }

    @Test void missingOutcomeFailsClosedEvenWhenOtherCountsReconcile() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            schema(c); seed(c, false, 2);
            // The INVALID row is the one with its outcome removed, so it moves
            // from invalid_count to unaccounted_count; the total remains 4.
            assertEquals(new PaymentBatchSummary(4,0,1,2,1,0), summarize(c,A,BATCH));
        }
    }

    @Test void emptyBatchProducesExactlyOneAllZeroSummary() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            schema(c);
            assertEquals(new PaymentBatchSummary(0,0,0,0,0,0), summarize(c,A,BATCH));
        }
    }

    @Test void anotherOwnersRowsWithSameBatchIdCannotAffectTheSummary() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            schema(c); seed(c, true, 2);
            try (PreparedStatement p=c.prepareStatement("INSERT INTO payments.staged_callback VALUES (?,?,?,?)")) {
                for (int i=0;i<100;i++) { p.setObject(1,uuid(1000+i)); p.setObject(2,B); p.setObject(3,BATCH); p.setString(4,"b-hash"); p.addBatch(); }
                p.executeBatch();
            }
            assertEquals(4, summarize(c,A,BATCH).rawCount());
        }
    }

    @Test void balancedClassificationStillReportsAnInsertedRowWithoutItsEffect() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start(); Connection c = pg.getPostgresDatabase().getConnection()) {
            schema(c); seed(c, true, 1);
            assertEquals(new PaymentBatchSummary(4,1,1,2,0,1), summarize(c,A,BATCH));
        }
    }

    private static void schema(Connection c) throws Exception {
        String sql=Files.readString(Path.of("src/test/resources/db/schema/cap85_batch.sql"));
        try (Statement s=c.createStatement()) { for(String part:sql.split(";")) if(!part.isBlank()) s.execute(part); }
    }
    private static void seed(Connection c, boolean allOutcomes, int effects) throws Exception {
        String[] classes={"INSERTED","INSERTED","DUPLICATE","INVALID"};
        try (PreparedStatement row=c.prepareStatement("INSERT INTO payments.staged_callback VALUES (?,?,?,?)");
             PreparedStatement outcome=c.prepareStatement("INSERT INTO payments.callback_outcome VALUES (?,?,?,?)");
             PreparedStatement effect=c.prepareStatement("INSERT INTO payments.payment_effect VALUES (?,?,?,?)")) {
            for(int i=0;i<4;i++) {
                UUID rowId=uuid(i+1); row.setObject(1,rowId);row.setObject(2,A);row.setObject(3,BATCH);row.setString(4,"a-hash-"+i);row.executeUpdate();
                if(allOutcomes || i<3) {outcome.setObject(1,rowId);outcome.setObject(2,A);outcome.setObject(3,BATCH);outcome.setString(4,classes[i]);outcome.executeUpdate();}
                if(i<effects) {effect.setObject(1,uuid(200+i));effect.setObject(2,A);effect.setObject(3,rowId);effect.setLong(4,5000+i);effect.executeUpdate();}
            }
        }
    }
    /** Independent instructor SQL reference, one parameterized aggregate SELECT. */
    private static PaymentBatchSummary summarize(Connection c, UUID owner, UUID batch) throws Exception {
        String sql="""
            WITH owned AS (
              SELECT s.row_id, o.classification
              FROM payments.staged_callback s
              LEFT JOIN payments.callback_outcome o
                ON o.row_id=s.row_id AND o.owner_id=s.owner_id AND o.batch_id=s.batch_id
              WHERE s.owner_id=? AND s.batch_id=?
            )
            SELECT count(*)::bigint,
              count(*) FILTER (WHERE classification='INVALID')::bigint,
              count(*) FILTER (WHERE classification='DUPLICATE')::bigint,
              count(*) FILTER (WHERE classification='INSERTED')::bigint,
              count(*) FILTER (WHERE classification IS NULL)::bigint,
              count(*) FILTER (WHERE classification='INSERTED' AND NOT EXISTS (
                SELECT 1 FROM payments.payment_effect e
                WHERE e.owner_id=? AND e.source_row_id=owned.row_id))::bigint
            FROM owned
            """;
        try(PreparedStatement p=c.prepareStatement(sql)) { p.setObject(1,owner);p.setObject(2,batch);p.setObject(3,owner);try(ResultSet r=p.executeQuery()){assertTrue(r.next());return new PaymentBatchSummary(r.getLong(1),r.getLong(2),r.getLong(3),r.getLong(4),r.getLong(5),r.getLong(6));} }
    }
    private static PaymentBatchSummary summarizeWithLineageDisabled(Connection c,UUID owner,UUID batch) throws Exception {
        // A deliberate defective comparator that claims every INSERTED row has an effect.
        try(PreparedStatement p=c.prepareStatement("SELECT count(*)::bigint, count(*) FILTER(WHERE o.classification='INVALID')::bigint, count(*) FILTER(WHERE o.classification='DUPLICATE')::bigint, count(*) FILTER(WHERE o.classification='INSERTED')::bigint, count(*) FILTER(WHERE o.row_id IS NULL)::bigint, 0::bigint FROM payments.staged_callback s LEFT JOIN payments.callback_outcome o ON o.row_id=s.row_id AND o.owner_id=s.owner_id AND o.batch_id=s.batch_id WHERE s.owner_id=? AND s.batch_id=?")) { p.setObject(1,owner);p.setObject(2,batch);try(ResultSet r=p.executeQuery()){r.next();return new PaymentBatchSummary(r.getLong(1),r.getLong(2),r.getLong(3),r.getLong(4),r.getLong(5),r.getLong(6));} }
    }
    private static UUID id(String s){return UUID.fromString(s);} private static UUID uuid(int i){return UUID.fromString(String.format("85000000-0000-4000-8000-%012d",i));}
}
