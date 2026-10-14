package coverstory.modules.payments;

import coverstory.modules.payments.api.CAP102Seam;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.sql.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class CAP102AcceptanceTest {
    private static final CAP102Seam.Owner A = owner("10200000-0000-4000-8000-000000000001");
    private static final CAP102Seam.Owner B = owner("10200000-0000-4000-8000-000000000002");
    private static Instant t(String value) { return Instant.parse("2026-12-18T" + value + "Z"); }
    private static CAP102Seam.Owner owner(String id) { return new CAP102Seam.Owner(UUID.fromString(id)); }
    private static CAP102Seam.Observation row(CAP102Seam.Owner o, Instant at, String id) { return new CAP102Seam.Observation(o,at,id,"sha256:synthetic-"+id); }
    private static CAP102Seam.TupleCursor start() { return new CAP102Seam.TupleCursor(t("10:00:00"),"x1"); }
    private static List<CAP102Seam.Observation> fixture() { return List.of(row(A,t("10:00:00"),"x1"),row(A,t("10:00:00"),"x2"),row(A,t("10:01:00"),"x3")); }
    private static Fake fake() { return new Fake(); }

    @Test void emitsX2AndX3AndCommitsLastTuple() {
        Fake f=fake(); var result=new CAP102Seam(f).ingestAfter(A,start(),new CAP102Seam.Rows(fixture()));
        assertEquals(List.of("x2","x3"),result.emitted().stream().map(CAP102Seam.Observation::id).toList());
        assertEquals(new CAP102Seam.TupleCursor(t("10:01:00"),"x3"),result.cursor()); assertEquals(2,f.stored.size());
        assertEquals(result.cursor(),f.cursor,"returned cursor must equal the cursor persisted by the adapter");
    }
    @Test void failureAtX3RollsBackRowsAndCursor() {
        Fake f=fake(); f.failId="x3"; expectExceptionOrTodo(IllegalStateException.class,()->new CAP102Seam(f).ingestAfter(A,start(),new CAP102Seam.Rows(fixture())));
        assertTrue(f.stored.isEmpty());assertEquals(start(),f.cursor);
    }
    @Test void rerunAtCommittedCursorHasNoDuplicateEffects() {
        Fake f=fake(); var seam=new CAP102Seam(f);var first=seam.ingestAfter(A,start(),new CAP102Seam.Rows(fixture()));
        var second=seam.ingestAfter(A,first.cursor(),new CAP102Seam.Rows(fixture()));assertTrue(second.emitted().isEmpty());assertEquals(2,f.stored.size());
    }
    @Test void staleCursorIsRejectedWithoutAppendingOrChangingStoredCursor() {
        Fake f=fake(); var seam=new CAP102Seam(f); var first=seam.ingestAfter(A,start(),new CAP102Seam.Rows(fixture()));
        expectExceptionOrTodo(IllegalStateException.class,()->seam.ingestAfter(A,start(),new CAP102Seam.Rows(fixture())));
        assertEquals(first.cursor(),f.cursor); assertEquals(2,f.stored.size());
    }

    @Test void learnerContractRunsThroughJdbcAdapterAndPersistsCursorWithRows() throws Exception {
        try (EmbeddedPostgres pg=EmbeddedPostgres.builder().start(); Connection c=pg.getPostgresDatabase().getConnection()) {
            for(String sql:Files.readString(Path.of("src/main/resources/db/schema/cap102_reconciliation.sql")).split(";")) if(!sql.isBlank()) try(Statement s=c.createStatement()){s.execute(sql);}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO payments.reconciliation_checkpoint VALUES (?,?,?)")){p.setObject(1,A.id());p.setTimestamp(2,Timestamp.from(start().timestamp()));p.setString(3,start().id());p.executeUpdate();}
            var seam=new CAP102Seam(new CAP102JdbcReconciliationAdapter(c,null));
            var batch=seam.ingestAfter(A,start(),new CAP102Seam.Rows(fixture()));
            assertEquals(List.of("x2","x3"),batch.emitted().stream().map(CAP102Seam.Observation::id).toList());
            assertEquals(batch.cursor(),readCursor(c),"cursor is durably updated in PostgreSQL with observations");
            assertEquals(2,count(c,"SELECT count(*) FROM payments.reconciliation_observation"));
            expectExceptionOrTodo(IllegalStateException.class,()->seam.ingestAfter(A,start(),new CAP102Seam.Rows(fixture())));
            assertEquals(batch.cursor(),readCursor(c)); assertEquals(2,count(c,"SELECT count(*) FROM payments.reconciliation_observation"));
        }
    }
    @Test void equalTimestampUsesLexicographicId() {
        Fake f=fake(); var result=new CAP102Seam(f).ingestAfter(A,start(),new CAP102Seam.Rows(List.of(row(A,t("10:00:00"),"x2"),row(A,t("10:00:00"),"x10"))));
        assertEquals(List.of("x10","x2"),result.emitted().stream().map(CAP102Seam.Observation::id).toList());
    }
    @Test void foreignOwnerOrNullOrderingKeyRejectsBeforeTransaction() {
        Fake f=fake();var seam=new CAP102Seam(f);
        expectExceptionOrTodo(IllegalArgumentException.class,()->seam.ingestAfter(A,start(),new CAP102Seam.Rows(List.of(row(B,t("10:02:00"),"foreign")))));
        expectExceptionOrTodo(IllegalArgumentException.class,()->seam.ingestAfter(A,start(),new CAP102Seam.Rows(List.of(row(A,null,"bad")))));
        expectExceptionOrTodo(IllegalArgumentException.class,()->seam.ingestAfter(A,start(),new CAP102Seam.Rows(List.of(row(A,t("10:02:00"),null)))));
        assertEquals(0,f.transactions);assertTrue(f.stored.isEmpty());assertEquals(start(),f.cursor);
    }
    private static final class Fake implements CAP102Seam.ReconciliationPort {
        CAP102Seam.TupleCursor cursor=start(); final List<CAP102Seam.Observation> stored=new ArrayList<>(); int transactions; String failId;
        @Override public CAP102Seam.CommittedBatch inTransaction(CAP102Seam.Owner owner,Function<CAP102Seam.ReconciliationTransaction,CAP102Seam.CommittedBatch> work) {
            transactions++; var oldCursor=cursor; var oldRows=new ArrayList<>(stored);
            try { return work.apply(new CAP102Seam.ReconciliationTransaction() {
                public CAP102Seam.TupleCursor lockCursor(CAP102Seam.Owner requested){if(!owner.equals(requested))throw new IllegalArgumentException();return cursor;}
                public void appendObservation(CAP102Seam.Observation value){if(value.id().equals(failId))throw new IllegalStateException("injected x3 failure");stored.add(value);}
                public void advanceCursor(CAP102Seam.Owner requested,CAP102Seam.TupleCursor expected,CAP102Seam.TupleCursor next){if(!owner.equals(requested)||!cursor.equals(expected))throw new IllegalStateException("cursor changed");cursor=next;}
            }); } catch(RuntimeException e){cursor=oldCursor;stored.clear();stored.addAll(oldRows);throw e;}
        }
    }

    private static CAP102Seam.TupleCursor readCursor(Connection c)throws Exception{
        try(PreparedStatement p=c.prepareStatement("SELECT updated_at,event_id FROM payments.reconciliation_checkpoint WHERE owner_id=?")){p.setObject(1,A.id());try(ResultSet r=p.executeQuery()){assertTrue(r.next());return new CAP102Seam.TupleCursor(r.getTimestamp(1).toInstant(),r.getString(2));}}
    }
    private static int count(Connection c,String sql)throws Exception{try(Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getInt(1);}}
    private static void expectExceptionOrTodo(Class<? extends RuntimeException> expected,Runnable action){try{action.run();fail("expected "+expected.getSimpleName());}catch(UnsupportedOperationException todo){throw todo;}catch(RuntimeException actual){assertTrue(expected.isInstance(actual),"unexpected exception: "+actual);}}

}
