package assignments.learning;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Controlled PostgreSQL unique-key race, independent of the learner writer. */
class Day25TwoTransactionReferenceTest {
    @Test void losingInsertRollsBackAndReadsCommittedWinnerOnFreshConnection() throws Exception {
        try (EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            var ds=pg.getPostgresDatabase();
            try(Connection c=ds.getConnection(); Statement s=c.createStatement()) {
                s.execute("CREATE TABLE teaching_bookings(room_id TEXT,slot_id TEXT,PRIMARY KEY(room_id,slot_id))");
            }
            Connection winner=ds.getConnection(); winner.setAutoCommit(false);
            try(PreparedStatement p=winner.prepareStatement("INSERT INTO teaching_bookings VALUES (?,?)")) { p.setString(1,"R2");p.setString(2,"S9");p.executeUpdate(); }
            CountDownLatch attempting=new CountDownLatch(1);
            AtomicInteger loserPid=new AtomicInteger();
            ExecutorService pool=Executors.newSingleThreadExecutor();
            Future<String> loser=pool.submit(()->{
                try(Connection c=ds.getConnection()) {
                    c.setAutoCommit(false);
                    try(Statement pid=c.createStatement();ResultSet r=pid.executeQuery("SELECT pg_backend_pid()")){r.next();loserPid.set(r.getInt(1));}
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO teaching_bookings VALUES (?,?)")) {
                        p.setString(1,"R2");p.setString(2,"S9");attempting.countDown();p.executeUpdate();c.commit();return "inserted";
                    } catch(SQLException conflict) {
                        if(!"23505".equals(conflict.getSQLState())) throw conflict;
                        c.rollback();
                    }
                }
                try(Connection fresh=ds.getConnection(); PreparedStatement p=fresh.prepareStatement("SELECT room_id,slot_id FROM teaching_bookings WHERE room_id=? AND slot_id=?")) {
                    p.setString(1,"R2");p.setString(2,"S9");try(ResultSet r=p.executeQuery()){if(!r.next()) throw new AssertionError("committed winner missing");return r.getString(1)+"/"+r.getString(2);}
                }
            });
            try {
                if(!attempting.await(5,TimeUnit.SECONDS)) throw new AssertionError("loser did not reach insert");
                long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
                boolean blockedOnWinner=false;
                while(System.nanoTime()<deadline && !blockedOnWinner) {
                    try(Connection observer=ds.getConnection();PreparedStatement p=observer.prepareStatement("SELECT wait_event_type FROM pg_stat_activity WHERE pid=?")) {
                        p.setInt(1,loserPid.get());try(ResultSet r=p.executeQuery()){blockedOnWinner=r.next() && "Lock".equals(r.getString(1));}
                    }
                    if(!blockedOnWinner) Thread.yield();
                }
                if(!blockedOnWinner) throw new AssertionError("loser did not enter a PostgreSQL lock wait on the held unique key");
                winner.commit();
                assertEquals("R2/S9",loser.get(10,TimeUnit.SECONDS));
                try(Connection fresh=ds.getConnection(); Statement s=fresh.createStatement();ResultSet r=s.executeQuery("SELECT count(*) FROM teaching_bookings")){r.next();assertEquals(1,r.getInt(1));}
            } finally { winner.close();pool.shutdownNow(); }
        }
    }
}
