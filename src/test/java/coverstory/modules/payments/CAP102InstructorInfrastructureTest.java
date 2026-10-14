package coverstory.modules.payments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** Independent PostgreSQL reference oracle; it does not call the learner TODO. */
class CAP102InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-102/synthetic-v1.json");
    private static final UUID A = UUID.fromString("10200000-0000-4000-8000-000000000001");
    private static final UUID B = UUID.fromString("10200000-0000-4000-8000-000000000002");
    private record Row(UUID owner, Instant at, String id, String hash) { }
    private record Cursor(Instant at, String id) { }
    private record Batch(List<Row> rows, Cursor cursor) { }
    private static Instant t(String value) { return Instant.parse("2026-12-18T" + value + "Z"); }
    private static void setInstant(PreparedStatement p, int index, Instant value) throws SQLException {
        p.setTimestamp(index, Timestamp.from(value));
    }
    private static List<Row> fixtureRows() throws Exception {
        JsonNode nodes = JSON.readTree(Files.readString(FIXTURE)).path("observations");
        List<Row> rows = new ArrayList<>();
        for (JsonNode n : nodes) rows.add(new Row(n.path("owner").asText().equals("A") ? A : B,
                Instant.parse(n.path("timestamp").asText()), n.path("id").asText(), n.path("payload_hash").asText()));
        return rows;
    }
    private static void install(Connection c, Cursor cursor) throws Exception {
        try (Statement s = c.createStatement()) { s.execute(Files.readString(Path.of("src/main/resources/db/schema/cap102_reconciliation.sql"))); }
        try (PreparedStatement p = c.prepareStatement("INSERT INTO payments.reconciliation_checkpoint VALUES (?, ?, ?)")) {
            p.setObject(1,A); setInstant(p,2,cursor.at()); p.setString(3,cursor.id()); p.executeUpdate();
        }
    }
    private static Batch reference(Connection c, UUID owner, Cursor expected, List<Row> supplied, String failId) throws Exception {
        if (owner == null || expected == null || expected.at() == null || expected.id() == null) throw new IllegalArgumentException("invalid owner/cursor");
        for (Row row : supplied) if (row.owner() == null || !owner.equals(row.owner()) || row.at() == null || row.id() == null || row.id().isBlank())
            throw new IllegalArgumentException("invalid observation owner/order key");
        c.setAutoCommit(false);
        try {
            Cursor stored;
            try (PreparedStatement p=c.prepareStatement("SELECT updated_at,event_id FROM payments.reconciliation_checkpoint WHERE owner_id=? FOR UPDATE")) {
                p.setObject(1,owner); try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new IllegalStateException("missing checkpoint"); stored=new Cursor(r.getTimestamp(1).toInstant(),r.getString(2)); }
            }
            if (!stored.equals(expected)) throw new IllegalStateException("checkpoint changed");
            List<Row> chosen = supplied.stream().filter(r -> r.at().isAfter(expected.at()) || (r.at().equals(expected.at()) && r.id().compareTo(expected.id()) > 0))
                    .sorted(Comparator.comparing(Row::at).thenComparing(Row::id)).toList();
            Cursor next=expected;
            try (PreparedStatement p=c.prepareStatement("INSERT INTO payments.reconciliation_observation(owner_id,updated_at,event_id,payload_hash) VALUES (?,?,?,?)")) {
                for(Row row:chosen) {
                    p.setObject(1,owner); setInstant(p,2,row.at()); p.setString(3,row.id()); p.setString(4,row.hash()); p.executeUpdate();
                    if(row.id().equals(failId)) throw new SQLException("injected CAP102 x3 write failure");
                    next=new Cursor(row.at(),row.id());
                }
            }
            try(PreparedStatement p=c.prepareStatement("UPDATE payments.reconciliation_checkpoint SET updated_at=?,event_id=? WHERE owner_id=?")) {
                setInstant(p,1,next.at());p.setString(2,next.id());p.setObject(3,owner);assertEquals(1,p.executeUpdate());
            }
            c.commit(); return new Batch(chosen,next);
        } catch(Exception e) { c.rollback(); throw e; }
        finally { c.setAutoCommit(true); }
    }
    private static Cursor current(Connection c) throws Exception {
        try(PreparedStatement p=c.prepareStatement("SELECT updated_at,event_id FROM payments.reconciliation_checkpoint WHERE owner_id=?")) {
            p.setObject(1,A); try(ResultSet r=p.executeQuery()){assertTrue(r.next());return new Cursor(r.getTimestamp(1).toInstant(),r.getString(2));}
        }
    }
    private static int count(Connection c,String sql)throws Exception{try(Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getInt(1);}}

    @Test void fixtureAndPostgresSchemaAreSyntheticAndRunnable() throws Exception {
        JsonNode f=JSON.readTree(Files.readString(FIXTURE)); assertEquals("SYNTHETIC_OFFLINE_CONTRACT",f.path("classification").asText());
        assertFalse(f.path("payment_effects_created").asBoolean()); assertEquals(3,fixtureRows().size());
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start();Connection c=pg.getPostgresDatabase().getConnection()) {
            install(c,new Cursor(t("10:00:00"),"x1")); assertEquals(0,count(c,"SELECT count(*) FROM payments.reconciliation_observation"));
            assertEquals(1,count(c,"SELECT count(*) FROM payments.reconciliation_checkpoint"));
        }
    }
    @Test void tupleSelectionWritesX2X3AndRerunHasNoDuplicateObservation() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start();Connection c=pg.getPostgresDatabase().getConnection()) {
            install(c,new Cursor(t("10:00:00"),"x1"));
            Batch first=reference(c,A,new Cursor(t("10:00:00"),"x1"),fixtureRows(),null);
            assertEquals(List.of("x2","x3"),first.rows().stream().map(Row::id).toList());
            assertEquals(new Cursor(t("10:01:00"),"x3"),first.cursor()); assertEquals(first.cursor(),current(c)); assertEquals(2,count(c,"SELECT count(*) FROM payments.reconciliation_observation"));
            Batch second=reference(c,A,first.cursor(),fixtureRows(),null); assertTrue(second.rows().isEmpty()); assertEquals(first.cursor(),second.cursor()); assertEquals(2,count(c,"SELECT count(*) FROM payments.reconciliation_observation"));
        }
    }
    @Test void x3FailureRollsBackEarlierObservationAndCursor() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start();Connection c=pg.getPostgresDatabase().getConnection()) {
            Cursor old=new Cursor(t("10:00:00"),"x1");install(c,old);
            SQLException error=assertThrows(SQLException.class,()->reference(c,A,old,fixtureRows(),"x3")); assertTrue(error.getMessage().contains("injected CAP102"));
            assertEquals(old,current(c));assertEquals(0,count(c,"SELECT count(*) FROM payments.reconciliation_observation"));
        }
    }
    @Test void equalTimestampUsesIdTieAndTimestampOnlyDefectIsDetected() throws Exception {
        List<Row> tied=List.of(new Row(A,t("10:00:00"),"x1","h1"),new Row(A,t("10:00:00"),"x2","h2"));
        Cursor start=new Cursor(t("10:00:00"),"x1");
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start();Connection c=pg.getPostgresDatabase().getConnection()) {
            install(c,start); Batch result=reference(c,A,start,tied,null);
            assertEquals(List.of("x2"),result.rows().stream().map(Row::id).toList());
            assertEquals(new Cursor(t("10:00:00"),"x2"),result.cursor());
            assertEquals("x2",query(c,"SELECT event_id FROM payments.reconciliation_observation"));
        }
        assertEquals(List.of("x2"),tied.stream().filter(r->r.at().isAfter(start.at())||(r.at().equals(start.at())&&r.id().compareTo(start.id())>0)).sorted(Comparator.comparing(Row::at).thenComparing(Row::id)).map(Row::id).toList());
        assertTrue(tied.stream().filter(r->r.at().isAfter(start.at())).toList().isEmpty(),"timestamp-only mutant skips x2");
    }
    @Test void foreignOwnerAndNullOrderingKeysRejectBeforeAnyPersistence() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start();Connection c=pg.getPostgresDatabase().getConnection()) {
            Cursor old=new Cursor(t("10:00:00"),"x1");install(c,old);
            assertThrows(IllegalArgumentException.class,()->reference(c,A,old,List.of(new Row(B,t("10:02:00"),"foreign","h")),null));
            assertThrows(IllegalArgumentException.class,()->reference(c,A,old,List.of(new Row(A,null,"bad","h")),null));
            assertThrows(IllegalArgumentException.class,()->reference(c,A,old,List.of(new Row(A,t("10:02:00"),null,"h")),null));
            assertEquals(old,current(c));assertEquals(0,count(c,"SELECT count(*) FROM payments.reconciliation_observation"));
        }
    }
    private static String query(Connection c,String sql)throws Exception{try(Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){assertTrue(r.next());return r.getString(1);}}
}
