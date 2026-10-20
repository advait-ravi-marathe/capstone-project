package assignments.delivery.recovery;

import assignments.delivery.*;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class ManualRecoveryConcurrencyAcceptanceTest {
 @Test void sharedWorkRowLockSerializesBothOrders() throws Exception {
  try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
   var ds=pg.getPostgresDatabase(); Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
   JdbcTemplate jdbc=new JdbcTemplate(ds); var tx=new DataSourceTransactionManager(ds);
   seed(jdbc,"race-a");
   String sourceBefore=sourceSnapshot(jdbc,"race-a"),immutableWorkBefore=immutableWorkSnapshot(jdbc,"race-a"),receiptBefore=receiptSnapshot(jdbc,"race-a");
   List<String> historyBefore=historyRows(jdbc,"race-a");
   var store=new JdbcManualRecoveryStore(jdbc); var raw=new JdbcIssuanceWorkStore(jdbc); var guarded=new RecoveryAwareIssuanceWorkStore(raw,store,jdbc);
   CountDownLatch holderLocked=new CountDownLatch(1), releaseHolder=new CountDownLatch(1), waiterStarted=new CountDownLatch(1);
   ExecutorService pool=Executors.newFixedThreadPool(2);
   try {
    Future<?> first=pool.submit(()->new TransactionTemplate(tx).executeWithoutResult(s->{store.quarantine(new IssuanceSourceKey("race-a",1),0,RecoveryReason.MANUAL_HOLD); holderLocked.countDown(); await(releaseHolder);}));
    assertTrue(holderLocked.await(5,TimeUnit.SECONDS),"lock holder must acquire work row");
    Future<?> waiter=pool.submit(()->{waiterStarted.countDown(); try {new TransactionTemplate(tx).executeWithoutResult(s->guarded.acceptReceipt(new IssuanceEnvelope("race-a",1,"ISSUED",LocalDate.parse("2026-12-01"),1))); fail("quarantined work must refuse a receipt");} catch(RecoveryPreconditionException expected){assertEquals(RecoveryPreconditionException.Code.QUARANTINED,expected.code());}});
    assertTrue(waiterStarted.await(5,TimeUnit.SECONDS));
    awaitLockWait(jdbc);
    releaseHolder.countDown(); first.get(5,TimeUnit.SECONDS); waiter.get(5,TimeUnit.SECONDS);
    assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM training_issuance_delivery_receipt WHERE policy_id='race-a' AND source_version=1",Integer.class));
    assertEquals(sourceBefore,sourceSnapshot(jdbc,"race-a"));
    assertEquals(immutableWorkBefore,immutableWorkSnapshot(jdbc,"race-a"),"quarantine cannot alter captured identity, envelope, ack, or timestamp fields");
    assertEquals(receiptBefore,receiptSnapshot(jdbc,"race-a"),"receipt state remains exactly unchanged");
    assertHistoryAppend(jdbc,"race-a",historyBefore,1,"QUARANTINE","MANUAL_HOLD");
    assertEquals("1|MANUAL_HOLD|0",jdbc.queryForObject("SELECT w.control_revision||'|'||w.quarantine_reason||'|'||(SELECT count(*) FROM training_issuance_delivery_receipt r WHERE r.policy_id=w.policy_id AND r.source_version=w.source_version) FROM training_issuance_work w WHERE policy_id='race-a'",String.class));
   } finally {releaseHolder.countDown(); pool.shutdownNow(); assertTrue(pool.awaitTermination(5,TimeUnit.SECONDS));}
  }
 }
 @Test void receiptCommittedBeforeWaitingQuarantineIsSeenByFreshStatement() throws Exception {
  try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
   var ds=pg.getPostgresDatabase(); Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
   JdbcTemplate jdbc=new JdbcTemplate(ds); var tx=new DataSourceTransactionManager(ds);
   seed(jdbc,"race-b");
   String workBefore=workSnapshot(jdbc,"race-b"),sourceBefore=sourceSnapshot(jdbc,"race-b");
   List<String> historyBefore=historyRows(jdbc,"race-b");
   String[] receiptBeforeCommit={null};
   var store=new JdbcManualRecoveryStore(jdbc); var svc=new ManualRecoveryService(store,new RecoveryAwareIssuanceWorkStore(new JdbcIssuanceWorkStore(jdbc),store,jdbc),tx);
   CountDownLatch receiptLocked=new CountDownLatch(1), commitReceipt=new CountDownLatch(1);
   ExecutorService pool=Executors.newFixedThreadPool(2);
   try {
    Future<?> receipt=pool.submit(()->new TransactionTemplate(tx).executeWithoutResult(s->{
      jdbc.queryForObject("SELECT policy_id FROM training_issuance_work WHERE policy_id='race-b' AND source_version=1 FOR UPDATE",String.class);
      jdbc.update("INSERT INTO training_issuance_delivery_receipt(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES ('race-b',1,'ISSUED',DATE '2026-12-01',1)");
      receiptBeforeCommit[0]=jdbc.queryForObject("SELECT to_jsonb(r)::text FROM training_issuance_delivery_receipt r WHERE policy_id='race-b' AND source_version=1",String.class);
      receiptLocked.countDown(); await(commitReceipt);
    }));
    assertTrue(receiptLocked.await(5,TimeUnit.SECONDS));
    Future<Throwable> quarantine=pool.submit(()->{try{svc.quarantine(new IssuanceSourceKey("race-b",1),0,RecoveryReason.MANUAL_HOLD);return null;}catch(Throwable e){return e;}});
    awaitLockWait(jdbc);
    commitReceipt.countDown(); receipt.get(5,TimeUnit.SECONDS);
    Throwable result=quarantine.get(5,TimeUnit.SECONDS);
    assertInstanceOf(RecoveryPreconditionException.class,result);
    assertEquals(RecoveryPreconditionException.Code.RECEIPT_PRESENT,((RecoveryPreconditionException)result).code());
    assertEquals(0,jdbc.queryForObject("SELECT control_revision FROM training_issuance_work WHERE policy_id='race-b' AND source_version=1",Integer.class));
    assertEquals(workBefore,workSnapshot(jdbc,"race-b"),"refused quarantine must preserve the full work payload and control markers");
    assertEquals(historyBefore,historyRows(jdbc,"race-b"),"refused quarantine cannot append or rewrite any history row");
    assertNotNull(receiptBeforeCommit[0],"full receipt including timestamp is captured before holder release");
    assertEquals(receiptBeforeCommit[0],receiptSnapshot(jdbc,"race-b"),"receipt payload and original timestamp survive the waiting quarantine");
    assertEquals(sourceBefore,sourceSnapshot(jdbc,"race-b"));
   } finally {commitReceipt.countDown();pool.shutdownNow();assertTrue(pool.awaitTermination(5,TimeUnit.SECONDS));}
  }
 }
 private static void await(CountDownLatch latch){try{if(!latch.await(5,TimeUnit.SECONDS))throw new AssertionError("bounded latch watchdog");}catch(InterruptedException e){Thread.currentThread().interrupt();throw new AssertionError(e);}}
 private static void seed(JdbcTemplate jdbc,String id) {
  jdbc.update("INSERT INTO training_policy_lifecycle(policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,annual_premium_rupees,currency,effective_date,status,version) VALUES (?,'owner-a',?,?,'home-secure-v1','training-rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',1)",id,"j-"+id,"q-"+id);
  jdbc.update("INSERT INTO training_policy_lifecycle_event(policy_id,version,event_type,effective_date) VALUES (?,1,'ISSUED',DATE '2026-12-01')",id);
  jdbc.update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES (?,1,'ISSUED',DATE '2026-12-01',1)",id);
 }
 private static String sourceSnapshot(JdbcTemplate j,String id){return j.queryForObject("SELECT (SELECT coalesce(jsonb_agg(to_jsonb(p) ORDER BY policy_id),'[]'::jsonb)::text FROM training_policy_lifecycle p WHERE policy_id=?)||'|'||(SELECT coalesce(jsonb_agg(to_jsonb(e) ORDER BY version),'[]'::jsonb)::text FROM training_policy_lifecycle_event e WHERE policy_id=?)||'|'||(SELECT coalesce(jsonb_agg(to_jsonb(i) ORDER BY owner_id,client_request_id),'[]'::jsonb)::text FROM training_policy_idempotency i WHERE policy_id=?)",String.class,id,id,id);}
 private static String fullSnapshot(JdbcTemplate j,String id){return j.queryForObject("SELECT (SELECT coalesce(jsonb_agg(to_jsonb(w) ORDER BY source_version),'[]'::jsonb)::text FROM training_issuance_work w WHERE policy_id=?)||'|'||(SELECT coalesce(jsonb_agg(to_jsonb(r) ORDER BY source_version),'[]'::jsonb)::text FROM training_issuance_delivery_receipt r WHERE policy_id=?)||'|'||(SELECT coalesce(jsonb_agg(to_jsonb(h) ORDER BY control_revision),'[]'::jsonb)::text FROM training_issuance_recovery_history h WHERE policy_id=?)",String.class,id,id,id);}
 private static String immutableWorkSnapshot(JdbcTemplate j,String id){return j.queryForObject("SELECT coalesce(jsonb_agg(to_jsonb(w)-'control_revision'-'quarantined_at'-'quarantine_reason' ORDER BY source_version),'[]'::jsonb)::text FROM training_issuance_work w WHERE policy_id=?",String.class,id);}
 private static String workSnapshot(JdbcTemplate j,String id){return j.queryForObject("SELECT coalesce(jsonb_agg(to_jsonb(w) ORDER BY source_version),'[]'::jsonb)::text FROM training_issuance_work w WHERE policy_id=?",String.class,id);}
 private static List<String> historyRows(JdbcTemplate j,String id){return j.queryForList("SELECT to_jsonb(h)::text FROM training_issuance_recovery_history h WHERE policy_id=? ORDER BY source_version,control_revision",String.class,id);}
 private static String receiptSnapshot(JdbcTemplate j,String id){return j.queryForObject("SELECT coalesce((SELECT to_jsonb(r)::text FROM training_issuance_delivery_receipt r WHERE policy_id=? AND source_version=1),'ABSENT')",String.class,id);}
 private static void assertHistoryAppend(JdbcTemplate j,String id,List<String> before,int revision,String action,String reason){List<String> after=historyRows(j,id);assertEquals(before.size()+1,after.size());assertEquals(before,after.subList(0,before.size()));assertEquals(revision+"|"+action+"|"+reason+"|true",j.queryForObject("SELECT control_revision||'|'||action||'|'||reason_code||'|'||(decided_at IS NOT NULL) FROM training_issuance_recovery_history WHERE policy_id=? AND source_version=1 AND control_revision=?",String.class,id,revision));}
 private static void awaitLockWait(JdbcTemplate jdbc) throws InterruptedException {
  long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(5);
  while(System.nanoTime()<deadline) {
   Integer waiters=jdbc.queryForObject("SELECT count(*) FROM pg_stat_activity WHERE datname=current_database() AND wait_event_type='Lock' AND query ILIKE '%FOR UPDATE%'",Integer.class);
   if(waiters!=null&&waiters>0)return;
   Thread.sleep(20); // bounded poll of the actual PostgreSQL lock wait, not a guessed ordering delay
  }
  fail("competing transaction never appeared as a PostgreSQL row-lock waiter");
 }
}
