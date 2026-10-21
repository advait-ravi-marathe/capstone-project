package assignments.learning;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/** Real PostgreSQL insert/repeat/rollback demonstration on toy tables only. */
class Day26AuditReferenceTest {
    @Test void firstChangeAuditsRepeatDoesNotAndAuditFailureRollsBack() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
            var ds=pg.getPostgresDatabase();
            try(Connection c=ds.getConnection();Statement s=c.createStatement()) {
                s.execute("CREATE TABLE teaching_cards(card_id TEXT PRIMARY KEY)");
                s.execute("CREATE TABLE teaching_audit(action TEXT NOT NULL,resource_id TEXT NOT NULL,actor_id TEXT NOT NULL,occurred_at TIMESTAMPTZ NOT NULL)");
            }
            assertTrue(activate(ds,"C7","S2",Instant.parse("2026-01-01T09:00:00Z")));
            assertEquals("1/1",counts(ds));
            assertFalse(activate(ds,"C7","S2",Instant.parse("2026-01-01T09:00:00Z")));
            assertEquals("1/1",counts(ds));
            try(Connection c=ds.getConnection();Statement s=c.createStatement()) {
                s.execute("CREATE FUNCTION teaching_fail_audit() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'fixture'; END $$");
                s.execute("CREATE TRIGGER teaching_fail BEFORE INSERT ON teaching_audit FOR EACH ROW EXECUTE FUNCTION teaching_fail_audit()");
            }
            assertThrows(SQLException.class,()->activate(ds,"C8","S2",Instant.parse("2026-01-01T09:01:00Z")));
            assertEquals("1/1",counts(ds));
            try(Connection c=ds.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT action,resource_id,actor_id,occurred_at FROM teaching_audit")) {
                assertTrue(r.next()); assertEquals("CARD_ACTIVATED",r.getString(1));assertEquals("C7",r.getString(2));assertEquals("S2",r.getString(3));assertEquals(Instant.parse("2026-01-01T09:00:00Z"),r.getTimestamp(4).toInstant()); assertFalse(r.next());
            }
            try(Connection c=ds.getConnection();PreparedStatement p=c.prepareStatement("SELECT count(*) FROM information_schema.columns WHERE table_schema=current_schema() AND table_name='teaching_audit'" );ResultSet r=p.executeQuery()){
                r.next();assertEquals(4,r.getInt(1),"the teaching audit stores exactly its four allowed fields");
            }
        }
    }
    private static boolean activate(javax.sql.DataSource ds,String id,String actor,Instant at)throws SQLException {
        try(Connection c=ds.getConnection()) {
            c.setAutoCommit(false);
            try {
                int n;
                try(PreparedStatement p=c.prepareStatement("INSERT INTO teaching_cards VALUES (?) ON CONFLICT DO NOTHING")){p.setString(1,id);n=p.executeUpdate();}
                if(n==1) try(PreparedStatement p=c.prepareStatement("INSERT INTO teaching_audit VALUES ('CARD_ACTIVATED',?,?,?)")){p.setString(1,id);p.setString(2,actor);p.setTimestamp(3,Timestamp.from(at));p.executeUpdate();}
                c.commit();return n==1;
            }catch(SQLException e){try{c.rollback();}catch(SQLException rollback){e.addSuppressed(rollback);}throw e;}
        }
    }
    private static String counts(javax.sql.DataSource ds)throws Exception {
        try(Connection c=ds.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT (SELECT count(*) FROM teaching_cards)||'/'||(SELECT count(*) FROM teaching_audit)")){r.next();return r.getString(1);}
    }
}
