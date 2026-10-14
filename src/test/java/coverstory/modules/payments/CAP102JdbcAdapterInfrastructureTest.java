package coverstory.modules.payments;

import coverstory.modules.payments.api.CAP102Seam;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Setup checks for the instructor-owned JDBC adapter reused by learner acceptance. */
class CAP102JdbcAdapterInfrastructureTest {
    private static final CAP102Seam.Owner A = new CAP102Seam.Owner(java.util.UUID.fromString("10200000-0000-4000-8000-000000000001"));
    private static final CAP102Seam.TupleCursor START = new CAP102Seam.TupleCursor(Instant.parse("2026-12-18T10:00:00Z"),"x1");

    @Test void adapterPersistsObservationsAndCursorUnderOneTransactionAndLocksTheOwnerCheckpoint() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start();Connection c=pg.getPostgresDatabase().getConnection()) {
            setup(c);
            var adapter=new CAP102JdbcReconciliationAdapter(c,null);
            var next=new CAP102Seam.TupleCursor(Instant.parse("2026-12-18T10:01:00Z"),"x3");
            var x2=row("x2",Instant.parse("2026-12-18T10:00:00Z"));var x3=row("x3",next.timestamp());
            var result=adapter.inTransaction(A,tx->{
                var locked=tx.lockCursor(A);assertEquals(START,locked);
                tx.appendObservation(x2);tx.appendObservation(x3);
                tx.advanceCursor(A,locked,next);
                return new CAP102Seam.CommittedBatch(List.of(x2,x3),next);
            });
            assertEquals(next,result.cursor());assertEquals(next,current(c));
            assertEquals(2,count(c,"SELECT count(*) FROM payments.reconciliation_observation"));
            assertEquals(2,count(c,"SELECT count(*) FROM payments.reconciliation_observation WHERE owner_id='10200000-0000-4000-8000-000000000001'"));
        }
    }

    @Test void injectedWriteFailureRollsBackBothRowsAndCheckpoint() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start();Connection c=pg.getPostgresDatabase().getConnection()) {
            setup(c);
            var adapter=new CAP102JdbcReconciliationAdapter(c,"x3");
            var x2=row("x2",START.timestamp());var x3=row("x3",Instant.parse("2026-12-18T10:01:00Z"));
            assertThrows(IllegalStateException.class,()->adapter.inTransaction(A,tx->{
                var locked=tx.lockCursor(A);tx.appendObservation(x2);tx.appendObservation(x3);
                tx.advanceCursor(A,locked,new CAP102Seam.TupleCursor(x3.timestamp(),"x3"));
                return new CAP102Seam.CommittedBatch(List.of(x2,x3),new CAP102Seam.TupleCursor(x3.timestamp(),"x3"));
            }));
            assertEquals(START,current(c));assertEquals(0,count(c,"SELECT count(*) FROM payments.reconciliation_observation"));
        }
    }

    private static void setup(Connection c)throws Exception{
        for(String sql:Files.readString(Path.of("src/main/resources/db/schema/cap102_reconciliation.sql")).split(";"))if(!sql.isBlank())try(Statement s=c.createStatement()){s.execute(sql);}
        try(PreparedStatement p=c.prepareStatement("INSERT INTO payments.reconciliation_checkpoint VALUES (?,?,?)")){p.setObject(1,A.id());p.setTimestamp(2,Timestamp.from(START.timestamp()));p.setString(3,START.id());p.executeUpdate();}
    }
    private static CAP102Seam.Observation row(String id,Instant at){return new CAP102Seam.Observation(A,at,id,"sha256:synthetic-"+id);}
    private static CAP102Seam.TupleCursor current(Connection c)throws Exception{try(PreparedStatement p=c.prepareStatement("SELECT updated_at,event_id FROM payments.reconciliation_checkpoint WHERE owner_id=?")){p.setObject(1,A.id());try(ResultSet r=p.executeQuery()){assertTrue(r.next());return new CAP102Seam.TupleCursor(r.getTimestamp(1).toInstant(),r.getString(2));}}}
    private static int count(Connection c,String sql)throws Exception{try(Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){r.next();return r.getInt(1);}}
}
