package coverstory.modules.shortlist.refresh;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import static org.junit.jupiter.api.Assertions.*;

/** CAP-634/642 instructor schema harness: candidate SQL only, not a product persistence adapter. */
class CSM14CheckpointSchemaIntegrationTest {
    private static final String OWNER="00000000-0000-4000-8000-000000000001";
    private static final String LIST="00000000-0000-4000-8000-000000000011";

    private static void schema(Connection c) throws Exception {
        try(Statement st=c.createStatement()) {
            st.execute("CREATE SCHEMA shortlist");
            try(InputStream in=CSM14CheckpointSchemaIntegrationTest.class.getResourceAsStream("/db/schema/shortlist_checkpoint_refresh.sql")) {
                assertNotNull(in); st.execute(new String(in.readAllBytes(),StandardCharsets.UTF_8));
            }
        }
    }

    private static void revision(Connection c,long rev,Long parent,String checkpoint) throws Exception {
        try(PreparedStatement st=c.prepareStatement("INSERT INTO shortlist.checkpoint_revision(owner_id,shortlist_id,revision,parent_revision,checkpoint_id,scenario_id,slot_refs,source_digest_set_sha256) VALUES (?::uuid,?::uuid,?,?,?::uuid,'scenario-health-001','[{\"slotId\":\"s1\",\"productId\":\"00000000-0000-4000-8000-000000000101\",\"versionId\":\"v1\",\"sourceSha256\":\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"}]',decode(repeat('a',64),'hex'))")) {
            st.setString(1,OWNER);st.setString(2,LIST);st.setLong(3,rev);if(parent==null)st.setNull(4,java.sql.Types.BIGINT);else st.setLong(4,parent);st.setString(5,checkpoint);st.executeUpdate();
        }
    }

    @Test void checkpointHistoryHasOwnerBoundIdentityAndRollbackPreservesHead() throws Exception {
        assertPostgresRollbackPreservesHeadAndNoRevision();
    }

    static void assertPostgresRollbackPreservesHeadAndNoRevision() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start(); Connection c=pg.getPostgresDatabase().getConnection()) {
            try(Statement st=c.createStatement()) {
                st.execute("CREATE SCHEMA shortlist");
                try(InputStream in=CSM14CheckpointSchemaIntegrationTest.class.getResourceAsStream("/db/schema/shortlist_checkpoint_refresh.sql")) {
                    assertNotNull(in); st.execute(new String(in.readAllBytes(),StandardCharsets.UTF_8));
                }
                // Test-only stand-ins verify both required event inserts share the checkpoint transaction.
                st.execute("CREATE SCHEMA audit");st.execute("CREATE TABLE audit.required_event(owner_id UUID,shortlist_id UUID,revision BIGINT,event_key TEXT,PRIMARY KEY(owner_id,shortlist_id,revision,event_key))");
                st.execute("CREATE SCHEMA journey");st.execute("CREATE TABLE journey.required_event(owner_id UUID,shortlist_id UUID,revision BIGINT,event_key TEXT,PRIMARY KEY(owner_id,shortlist_id,revision,event_key))");
                st.execute("INSERT INTO shortlist.checkpoint_revision(owner_id,shortlist_id,revision,parent_revision,checkpoint_id,scenario_id,slot_refs,source_digest_set_sha256) VALUES ('00000000-0000-4000-8000-000000000001','00000000-0000-4000-8000-000000000011',1,NULL,'00000000-0000-4000-8000-000000000021','scenario-health-001','[{\"slotId\":\"s1\",\"productId\":\"00000000-0000-4000-8000-000000000101\",\"versionId\":\"v1\",\"sourceSha256\":\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"}]',decode(repeat('a',64),'hex'))");
                st.execute("INSERT INTO shortlist.checkpoint_head(owner_id,shortlist_id,revision) VALUES ('00000000-0000-4000-8000-000000000001','00000000-0000-4000-8000-000000000011',1)");
            }
            c.setAutoCommit(false);
            try(Statement st=c.createStatement()) {
                st.execute("INSERT INTO shortlist.checkpoint_revision(owner_id,shortlist_id,revision,parent_revision,checkpoint_id,scenario_id,slot_refs,source_digest_set_sha256) VALUES ('00000000-0000-4000-8000-000000000001','00000000-0000-4000-8000-000000000011',2,1,'00000000-0000-4000-8000-000000000022','scenario-health-001','[{\"slotId\":\"s1\",\"productId\":\"00000000-0000-4000-8000-000000000101\",\"versionId\":\"v2\",\"sourceSha256\":\"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb\"}]',decode(repeat('b',64),'hex'))");
                st.execute("UPDATE shortlist.checkpoint_head SET revision=2 WHERE owner_id='00000000-0000-4000-8000-000000000001' AND shortlist_id='00000000-0000-4000-8000-000000000011'");
                st.execute("INSERT INTO audit.required_event VALUES ('00000000-0000-4000-8000-000000000001','00000000-0000-4000-8000-000000000011',2,'CHECKPOINT_ACCEPTED')");
                st.execute("INSERT INTO journey.required_event VALUES ('00000000-0000-4000-8000-000000000001','00000000-0000-4000-8000-000000000011',2,'CHECKPOINT_ACCEPTED')");
            }
            c.rollback(); c.setAutoCommit(true);
            try(Statement st=c.createStatement(); var rs=st.executeQuery("SELECT h.revision,(SELECT count(*) FROM shortlist.checkpoint_revision r WHERE r.revision=2),(SELECT count(*) FROM audit.required_event),(SELECT count(*) FROM journey.required_event) FROM shortlist.checkpoint_head h")) {
                assertTrue(rs.next()); assertEquals(1,rs.getLong(1)); assertEquals(0,rs.getLong(2));assertEquals(0,rs.getLong(3));assertEquals(0,rs.getLong(4),"rollback removes both required event records with the revision/head");
            }
            c.setAutoCommit(false);try(Statement st=c.createStatement()){
                st.execute("INSERT INTO shortlist.checkpoint_revision(owner_id,shortlist_id,revision,parent_revision,checkpoint_id,scenario_id,slot_refs,source_digest_set_sha256) VALUES ('00000000-0000-4000-8000-000000000001','00000000-0000-4000-8000-000000000011',2,1,'00000000-0000-4000-8000-000000000022','scenario-health-001','[{\"slotId\":\"s1\",\"productId\":\"00000000-0000-4000-8000-000000000101\",\"versionId\":\"v2\",\"sourceSha256\":\"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb\"}]',decode(repeat('b',64),'hex'))");
                st.execute("UPDATE shortlist.checkpoint_head SET revision=2 WHERE owner_id='00000000-0000-4000-8000-000000000001' AND shortlist_id='00000000-0000-4000-8000-000000000011'");
                st.execute("INSERT INTO audit.required_event VALUES ('00000000-0000-4000-8000-000000000001','00000000-0000-4000-8000-000000000011',2,'CHECKPOINT_ACCEPTED')");
                st.execute("INSERT INTO journey.required_event VALUES ('00000000-0000-4000-8000-000000000001','00000000-0000-4000-8000-000000000011',2,'CHECKPOINT_ACCEPTED')");
            }c.commit();c.setAutoCommit(true);
            try(Statement st=c.createStatement();var rs=st.executeQuery("SELECT h.revision,(SELECT count(*) FROM shortlist.checkpoint_revision r WHERE r.revision=2),(SELECT count(*) FROM audit.required_event),(SELECT count(*) FROM journey.required_event) FROM shortlist.checkpoint_head h")){assertTrue(rs.next());assertEquals(2,rs.getLong(1));assertEquals(1,rs.getLong(2));assertEquals(1,rs.getLong(3));assertEquals(1,rs.getLong(4),"committed revision, head, audit and journey effects appear together");}
            assertThrows(Exception.class,()->{try(Statement st=c.createStatement()){st.execute("INSERT INTO shortlist.checkpoint_revision(owner_id,shortlist_id,revision,parent_revision,checkpoint_id,scenario_id,slot_refs,source_digest_set_sha256) VALUES ('00000000-0000-4000-8000-000000000001','00000000-0000-4000-8000-000000000011',1,NULL,'00000000-0000-4000-8000-000000000031','scenario-health-001','[]',decode(repeat('c',64),'hex'))");}});
        }
    }

    @Test void preparedTwoConnectionRaceHasOneRevisionWinnerWithoutSleeps() throws Exception {
        assertPostgresRaceHasOneWinner();
    }

    static void assertPostgresRaceHasOneWinner() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start(); Connection setup=pg.getPostgresDatabase().getConnection()) {
            schema(setup); revision(setup,1,null,"00000000-0000-4000-8000-000000000021");
            try(Statement st=setup.createStatement()){st.execute("INSERT INTO shortlist.checkpoint_head(owner_id,shortlist_id,revision) VALUES ('"+OWNER+"','"+LIST+"',1)");}
            CountDownLatch ready=new CountDownLatch(2), start=new CountDownLatch(1);
            try(ExecutorService pool=Executors.newFixedThreadPool(2)) {
                List<Future<Boolean>> results=new ArrayList<>();
                for(int i=0;i<2;i++) { final int n=i; results.add(pool.submit(()->{
                    try(Connection c=pg.getPostgresDatabase().getConnection()) {
                        c.setAutoCommit(false); ready.countDown(); start.await();
                        try {
                            revision(c,2,1L,"00000000-0000-4000-8000-00000000002"+(n+1));
                            try(PreparedStatement update=c.prepareStatement("UPDATE shortlist.checkpoint_head SET revision=2 WHERE owner_id=?::uuid AND shortlist_id=?::uuid AND revision=1")) {
                                update.setString(1,OWNER);update.setString(2,LIST);int won=update.executeUpdate();
                                if(won!=1){c.rollback();return false;}
                            }
                            c.commit();return true;
                        } catch(SQLException conflict){c.rollback();return false;}
                    }
                })); }
                ready.await();start.countDown();
                long winners=0;for(Future<Boolean> result:results)if(result.get())winners++;
                assertEquals(1,winners,"expected-revision/unique revision boundary should have one winner");
            }
            try(Statement st=setup.createStatement();ResultSet rs=st.executeQuery("SELECT h.revision,(SELECT count(*) FROM shortlist.checkpoint_revision r WHERE r.owner_id=h.owner_id AND r.shortlist_id=h.shortlist_id) FROM shortlist.checkpoint_head h")) {
                assertTrue(rs.next());assertEquals(2,rs.getLong(1));assertEquals(2,rs.getLong(2));
            }
        }
    }

    @Test void ownerBoundHistoryQueryReturnsStableTwentyRowPageFromSyntheticThousand() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start();Connection c=pg.getPostgresDatabase().getConnection()) {
            schema(c);
            try(Statement st=c.createStatement()) {
                st.execute("INSERT INTO shortlist.checkpoint_revision(owner_id,shortlist_id,revision,parent_revision,checkpoint_id,scenario_id,slot_refs,source_digest_set_sha256) " +
                        "SELECT '"+OWNER+"'::uuid,'"+LIST+"'::uuid,n,CASE WHEN n=1 THEN NULL ELSE n-1 END,('00000000-0000-4000-8000-'||lpad(n::text,12,'0'))::uuid,'scenario-health-001','[{\"slotId\":\"s1\",\"productId\":\"00000000-0000-4000-8000-000000000101\",\"versionId\":\"v1\",\"sourceSha256\":\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"}]',decode(repeat('a',64),'hex') FROM generate_series(1,1000) n");
                st.execute("INSERT INTO shortlist.checkpoint_head(owner_id,shortlist_id,revision) VALUES ('"+OWNER+"','"+LIST+"',1000)");
                st.execute("INSERT INTO shortlist.checkpoint_revision(owner_id,shortlist_id,revision,parent_revision,checkpoint_id,scenario_id,slot_refs,source_digest_set_sha256) " +
                        "SELECT '00000000-0000-4000-8000-000000000002'::uuid,'00000000-0000-4000-8000-000000000012'::uuid,n,CASE WHEN n=1 THEN NULL ELSE n-1 END,('00000000-0000-4000-8000-'||lpad((n+1000)::text,12,'0'))::uuid,'scenario-health-001','[{\"slotId\":\"s1\",\"productId\":\"00000000-0000-4000-8000-000000000101\",\"versionId\":\"v1\",\"sourceSha256\":\"bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb\"}]',decode(repeat('b',64),'hex') FROM generate_series(1,1000) n");
                st.execute("INSERT INTO shortlist.checkpoint_head(owner_id,shortlist_id,revision) VALUES ('00000000-0000-4000-8000-000000000002','00000000-0000-4000-8000-000000000012',1000)");
            }
            List<Long> actual=new ArrayList<>();
            try(PreparedStatement q=c.prepareStatement("SELECT revision FROM shortlist.checkpoint_revision WHERE owner_id=?::uuid AND shortlist_id=?::uuid AND revision < ? ORDER BY revision DESC LIMIT 20")) {
                q.setString(1,OWNER);q.setString(2,LIST);q.setLong(3,1001);try(ResultSet rs=q.executeQuery()){while(rs.next())actual.add(rs.getLong(1));}
            }
            assertEquals(20,actual.size());assertEquals(java.util.stream.LongStream.iterate(1000L,n->n-1).limit(20).boxed().toList(),actual);
            try(PreparedStatement q=c.prepareStatement("EXPLAIN (ANALYZE, BUFFERS) SELECT revision FROM shortlist.checkpoint_revision WHERE owner_id=?::uuid AND shortlist_id=?::uuid AND revision < ? ORDER BY revision DESC LIMIT 20")) {
                q.setString(1,OWNER);q.setString(2,LIST);q.setLong(3,1001);try(ResultSet rs=q.executeQuery()){
                    List<String> plan=new ArrayList<>();while(rs.next())plan.add(rs.getString(1));
                    assertTrue(plan.stream().anyMatch(line->line.contains("Limit")));
                    System.out.println("CSM14-11 prepared query plan:\n"+String.join("\n",plan));
                }
            }
        }
    }
}
