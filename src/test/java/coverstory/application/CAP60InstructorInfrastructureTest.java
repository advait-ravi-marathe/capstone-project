package coverstory.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/** Disposable PostgreSQL proof of the constraints/barrier harness, not the application command. */
class CAP60InstructorInfrastructureTest {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Path FIXTURE = Path.of("fixtures/coverstory/CAP-60/synthetic-v1.json");
    private static final Path DDL = Path.of("src/test/resources/coverstory/CAP-60/schema.sql");
    private static final UUID JOURNEY = UUID.fromString("60000000-0000-4000-8000-000000000001");
    private static final UUID OWNER = UUID.fromString("60000000-0000-4000-8000-000000000002");

    @Test void fixtureAndSchemaAreSyntheticAndExposeEveryRequiredFailurePoint() throws Exception {
        JsonNode f = JSON.readTree(Files.readString(FIXTURE));
        assertEquals("SYNTHETIC_POSTGRES_JOURNEY_RACE_CONTRACT", f.path("classification").asText());
        assertEquals(4, f.path("initial_version").asInt());
        assertEquals(5, f.path("expected_committed_version").asInt());
        assertEquals(5, f.path("barrier_timeout_seconds").asInt());
        assertEquals(10, f.path("same_key_repetitions").asInt());
        assertEquals(4, f.path("failure_injection_points").size());
        assertFalse(f.path("consumer_data").asBoolean());
        assertFalse(f.path("payment_or_policy_effects").asBoolean());
        assertTrue(Files.readString(DDL).contains("UNIQUE (journey_id, sequence)"));
        assertTrue(Files.readString(DDL).contains("PRIMARY KEY (journey_id, key_hash)"));
    }

    @Test void independentConnectionsRaceSameKeySamePayloadTenTimes() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start()) {
            install(pg);
            ExecutorService pool = Executors.newFixedThreadPool(2);
            try {
                for (int i=0;i<10;i++) {
                    UUID journey = UUID.randomUUID(); seed(pg, journey);
                    CountDownLatch ready = new CountDownLatch(2), release = new CountDownLatch(1);
                    byte[] key = filled((byte) (i+1)), payload = filled((byte) (i+21));
                    Future<Outcome> a = pool.submit(() -> command(pg, journey, key, payload, 4, ready, release, 1));
                    Future<Outcome> b = pool.submit(() -> command(pg, journey, key, payload, 4, ready, release, 2));
                    assertTrue(ready.await(5, TimeUnit.SECONDS), "both PostgreSQL clients must reach the barrier");
                    release.countDown();
                    Outcome first = a.get(5, TimeUnit.SECONDS), second = b.get(5, TimeUnit.SECONDS);
                    assertEquals(1, List.of(first,second).stream().filter(o->!o.replay()&&o.status()==201).count());
                    assertEquals(1, List.of(first,second).stream().filter(o->o.replay()&&o.status()==201).count());
                    try (Connection c=pg.getPostgresDatabase().getConnection()) {
                        assertEquals(5, scalarLong(c, "SELECT version FROM cap60_harness.journey WHERE journey_id='"+journey+"'"));
                        assertEquals(1, scalarInt(c,"SELECT count(*) FROM cap60_harness.domain_effect WHERE journey_id='"+journey+"'"));
                        assertEquals(1, scalarInt(c,"SELECT count(*) FROM cap60_harness.audit_event WHERE journey_id='"+journey+"'"));
                        assertEquals(1, scalarInt(c,"SELECT count(*) FROM cap60_harness.step_event WHERE journey_id='"+journey+"'"));
                    }
                }
            } finally { pool.shutdownNow(); }
        }
    }

    @Test void differentCommandsAtVersionFourProduceOneWinnerAndOneConflict() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()){
            install(pg);UUID journey=UUID.randomUUID();seed(pg,journey);ExecutorService pool=Executors.newFixedThreadPool(2);
            try{
                CountDownLatch ready=new CountDownLatch(2),release=new CountDownLatch(1);
                Future<Outcome> a=pool.submit(()->command(pg,journey,filled((byte)51),filled((byte)61),4,ready,release,1));
                Future<Outcome> b=pool.submit(()->command(pg,journey,filled((byte)52),filled((byte)62),4,ready,release,2));
                assertTrue(ready.await(5,TimeUnit.SECONDS));release.countDown();
                List<Outcome> results=List.of(a.get(5,TimeUnit.SECONDS),b.get(5,TimeUnit.SECONDS));
                assertEquals(1,results.stream().filter(o->o.status()==201).count());
                assertEquals(1,results.stream().filter(o->o.status()==409).count());
                try(Connection c=pg.getPostgresDatabase().getConnection()){
                    assertEquals(5,scalarLong(c,"SELECT version FROM cap60_harness.journey WHERE journey_id='"+journey+"'"));
                    assertEquals(1,scalarInt(c,"SELECT count(*) FROM cap60_harness.domain_effect WHERE journey_id='"+journey+"'"));
                    assertEquals(1,scalarInt(c,"SELECT count(*) FROM cap60_harness.audit_event WHERE journey_id='"+journey+"'"));
                    assertEquals(1,scalarInt(c,"SELECT count(*) FROM cap60_harness.step_event WHERE journey_id='"+journey+"'"));
                }
            }finally{pool.shutdownNow();}
        }
    }

    @Test void rollbackLeavesVersionAndAllAssociatedRowsUntouched() throws Exception {
        try (EmbeddedPostgres pg = EmbeddedPostgres.builder().start()) {
            install(pg); seed(pg, JOURNEY);
            for (int failAfter=1; failAfter<=4; failAfter++) {
                try (Connection c=pg.getPostgresDatabase().getConnection()) {
                    c.setAutoCommit(false);
                    try {
                        int writes=0;
                        execute(c,"INSERT INTO cap60_harness.domain_effect VALUES ('60000000-0000-4000-8000-000000000011',?,?,5,'60000000-0000-4000-8000-000000000012')",JOURNEY,OWNER); failIf(++writes,failAfter);
                        execute(c,"INSERT INTO cap60_harness.audit_event VALUES ('60000000-0000-4000-8000-000000000013',?,'60000000-0000-4000-8000-000000000012','ADVANCED')",JOURNEY); failIf(++writes,failAfter);
                        try(PreparedStatement p=c.prepareStatement("UPDATE cap60_harness.journey SET version=5 WHERE journey_id=? AND version=4")){p.setObject(1,JOURNEY);assertEquals(1,p.executeUpdate());} failIf(++writes,failAfter);
                        execute(c,"INSERT INTO cap60_harness.step_event VALUES ('60000000-0000-4000-8000-000000000014',?,5,'60000000-0000-4000-8000-000000000012','ADVANCED')",JOURNEY); failIf(++writes,failAfter);
                        fail("injected failure point was not reached");
                    } catch (InjectedFailure expected) { c.rollback(); }
                }
                try(Connection c=pg.getPostgresDatabase().getConnection()){
                    assertEquals(4,scalarLong(c,"SELECT version FROM cap60_harness.journey WHERE journey_id='"+JOURNEY+"'"));
                    assertEquals(0,scalarInt(c,"SELECT count(*) FROM cap60_harness.domain_effect WHERE journey_id='"+JOURNEY+"'"));
                    assertEquals(0,scalarInt(c,"SELECT count(*) FROM cap60_harness.audit_event WHERE journey_id='"+JOURNEY+"'"));
                    assertEquals(0,scalarInt(c,"SELECT count(*) FROM cap60_harness.step_event WHERE journey_id='"+JOURNEY+"'"));
                }
            }
        }
    }

    @Test void journeyAndCausationUniquenessRejectsDuplicateStepEvents() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start();Connection c=pg.getPostgresDatabase().getConnection()){
            install(pg);seed(pg,JOURNEY);
            execute(c,"INSERT INTO cap60_harness.step_event VALUES ('60000000-0000-4000-8000-000000000014',?,5,'60000000-0000-4000-8000-000000000012','ADVANCED')",JOURNEY);
            assertThrows(SQLException.class,()->execute(c,"INSERT INTO cap60_harness.step_event VALUES ('60000000-0000-4000-8000-000000000015',?,5,'60000000-0000-4000-8000-000000000015','ADVANCED')",JOURNEY));
        }
    }

    @Test void idempotencyKeyHasOneStoredPayloadAndNoSecondEffectRow() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start();Connection c=pg.getPostgresDatabase().getConnection()){
            install(pg);seed(pg,JOURNEY);byte[] key=new byte[32], payload=new byte[32], changed=new byte[32];Arrays.fill(key,(byte)1);Arrays.fill(payload,(byte)2);Arrays.fill(changed,(byte)3);
            try(PreparedStatement p=c.prepareStatement("INSERT INTO cap60_harness.command_key VALUES (?, ?, ?, 200) ON CONFLICT (journey_id,key_hash) DO NOTHING")){
                p.setObject(1,JOURNEY);p.setBytes(2,key);p.setBytes(3,payload);assertEquals(1,p.executeUpdate());
                p.setObject(1,JOURNEY);p.setBytes(2,key);p.setBytes(3,payload);assertEquals(0,p.executeUpdate());
                p.setObject(1,JOURNEY);p.setBytes(2,key);p.setBytes(3,changed);assertEquals(0,p.executeUpdate());
            }
            assertEquals(1,scalarInt(c,"SELECT count(*) FROM cap60_harness.command_key WHERE journey_id='"+JOURNEY+"'"));
            try(PreparedStatement p=c.prepareStatement("SELECT payload_hash FROM cap60_harness.command_key WHERE journey_id=? AND key_hash=?")){p.setObject(1,JOURNEY);p.setBytes(2,key);try(ResultSet r=p.executeQuery()){assertTrue(r.next());assertArrayEquals(payload,r.getBytes(1));}}
        }
    }

    @Test void compositeOwnerReferenceRejectsForeignOwnerBeforeAnyEffectExists() throws Exception {
        try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start();Connection c=pg.getPostgresDatabase().getConnection()){
            install(pg);seed(pg,JOURNEY);
            UUID ownerB=UUID.fromString("60000000-0000-4000-8000-000000000003");
            assertThrows(SQLException.class,()->execute(c,"INSERT INTO cap60_harness.domain_effect VALUES ('60000000-0000-4000-8000-000000000021', ?, ?, 5, '60000000-0000-4000-8000-000000000022')",JOURNEY,ownerB));
            assertEquals(0,scalarInt(c,"SELECT count(*) FROM cap60_harness.domain_effect WHERE journey_id='"+JOURNEY+"'"));
        }
    }

    private static Outcome command(EmbeddedPostgres pg, UUID journey, byte[] key, byte[] payload, long expectedVersion, CountDownLatch ready, CountDownLatch release, int worker) throws Exception {
        try(Connection c=pg.getPostgresDatabase().getConnection()){
            c.setAutoCommit(false); ready.countDown();
            if(!release.await(5,TimeUnit.SECONDS))throw new TimeoutException("barrier release exceeded five seconds");
            int inserted;
            try(PreparedStatement p=c.prepareStatement("INSERT INTO cap60_harness.command_key VALUES (?, ?, ?, 201) ON CONFLICT (journey_id,key_hash) DO NOTHING")){
                p.setObject(1,journey);p.setBytes(2,key);p.setBytes(3,payload);inserted=p.executeUpdate();
            }
            if(inserted==0){
                try(PreparedStatement p=c.prepareStatement("SELECT payload_hash,stored_status FROM cap60_harness.command_key WHERE journey_id=? AND key_hash=?")){
                    p.setObject(1,journey);p.setBytes(2,key);try(ResultSet r=p.executeQuery()){if(!r.next())throw new SQLException("idempotency row disappeared");if(!Arrays.equals(payload,r.getBytes(1))){c.rollback();return new Outcome(409,false);}int status=r.getInt(2);c.commit();return new Outcome(status,true);}
                }
            }
            int changed;
            try(PreparedStatement p=c.prepareStatement("UPDATE cap60_harness.journey SET version=version+1,current_step='COMPARE' WHERE journey_id=? AND version=?")){p.setObject(1,journey);p.setLong(2,expectedVersion);changed=p.executeUpdate();}
            if(changed==0){try(PreparedStatement p=c.prepareStatement("UPDATE cap60_harness.command_key SET stored_status=409 WHERE journey_id=? AND key_hash=?")){p.setObject(1,journey);p.setBytes(2,key);p.executeUpdate();}c.commit();return new Outcome(409,false);}
            UUID causation=UUID.nameUUIDFromBytes((journey+":"+worker).getBytes());
            execute(c,"INSERT INTO cap60_harness.domain_effect VALUES (?, ?, ?, 5, ?)",UUID.randomUUID(),journey,OWNER,causation);
            execute(c,"INSERT INTO cap60_harness.audit_event VALUES (?, ?, ?, 'ADVANCED')",UUID.randomUUID(),journey,causation);
            execute(c,"INSERT INTO cap60_harness.step_event VALUES (?, ?, 5, ?, 'ADVANCED')",UUID.randomUUID(),journey,causation);
            c.commit();return new Outcome(201,false);
        }
    }
    private static void install(EmbeddedPostgres pg)throws Exception{try(Connection c=pg.getPostgresDatabase().getConnection();Statement s=c.createStatement()){s.execute(Files.readString(DDL));}}
    private static void seed(EmbeddedPostgres pg,UUID journey)throws Exception{try(Connection c=pg.getPostgresDatabase().getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO cap60_harness.journey VALUES (?, ?, 4, 'QUOTE_REVIEW')")){p.setObject(1,journey);p.setObject(2,OWNER);p.executeUpdate();}}
    private static void failIf(int writes,int failAfter)throws InjectedFailure{if(writes==failAfter)throw new InjectedFailure();}
    private static void execute(Connection c,String sql,Object...values)throws Exception{try(PreparedStatement p=c.prepareStatement(sql)){for(int i=0;i<values.length;i++)p.setObject(i+1,values[i]);p.executeUpdate();}}
    private static byte[] filled(byte value){byte[] b=new byte[32];Arrays.fill(b,value);return b;}
    private static int scalarInt(Connection c,String sql)throws Exception{try(Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){assertTrue(r.next());return r.getInt(1);}}
    private static long scalarLong(Connection c,String sql)throws Exception{try(Statement s=c.createStatement();ResultSet r=s.executeQuery(sql)){assertTrue(r.next());return r.getLong(1);}}
    private record Outcome(int status,boolean replay){}
    private static final class InjectedFailure extends SQLException { private InjectedFailure(){super("synthetic write failure injected");} }
}
