package assignments.delivery.recovery;

import assignments.delivery.*;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Fixed disposable-PostgreSQL acceptance; TODO failures are learner-owned until witnessed separately. */
@Tag("learnerAcceptance")
class ManualRecoveryAcceptanceTest {
 @Test void day113ClassifiesEvidenceWithoutInventingDelivery() {
  RecoveryDecisionPolicy p=new RecoveryDecisionPolicy();
  IssuanceEnvelope e=envelope("decision-a");
  assertEquals(RecoveryRecommendation.NOT_FOUND,p.classify(RecoveryObservation.notFound(e.sourceKey())).recommendation());
  assertEquals(RecoveryRecommendation.ELIGIBLE_FOR_ONE_ATTEMPT,p.classify(row(e,0,null,null,ReceiptRelation.ABSENT)).recommendation());
  assertEquals(RecoveryRecommendation.RECONCILE_ACKNOWLEDGMENT,p.classify(row(e,0,null,null,ReceiptRelation.MATCHING)).recommendation());
  assertEquals(RecoveryRecommendation.INTEGRITY_VIOLATION,p.classify(row(e,0,null,null,ReceiptRelation.CONFLICTING)).recommendation());
  assertEquals(RecoveryRecommendation.ALREADY_COMPLETE,p.classify(row(e,0,java.time.Instant.EPOCH,null,ReceiptRelation.MATCHING)).recommendation());
  assertEquals(RecoveryRecommendation.INTEGRITY_VIOLATION,p.classify(row(e,0,java.time.Instant.EPOCH,null,ReceiptRelation.ABSENT)).recommendation());
  assertEquals(RecoveryRecommendation.QUARANTINED_NO_RECEIPT,p.classify(row(e,2,null,java.time.Instant.EPOCH,ReceiptRelation.ABSENT)).recommendation());
  assertEquals(RecoveryRecommendation.QUARANTINED_WITH_CONFLICT,p.classify(row(e,2,null,java.time.Instant.EPOCH,ReceiptRelation.CONFLICTING)).recommendation());
 }
 @Test void day115InspectionIsExactKeyReadOnlyAndConflictAware() throws Exception {
  try(Fixture f=new Fixture()) {
   f.insertWork("inspect-a",1,false);
   var before=f.fullSnapshot("inspect-a");
   RecoveryReadResult result=f.store.inspect(new IssuanceSourceKey("inspect-a",1));
   assertTrue(result.observation().workPresent()); assertEquals(ReceiptRelation.ABSENT,result.observation().receiptRelation());
   assertEquals(before,f.fullSnapshot("inspect-a")); assertTrue(result.supplementaryHistory().isEmpty());
   assertFalse(f.store.inspect(new IssuanceSourceKey("missing",1)).observation().workPresent());
   f.insertWork("inspect-matching",1,false);
   f.jdbc.update("INSERT INTO training_issuance_delivery_receipt(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES ('inspect-matching',1,'ISSUED',DATE '2026-12-01',1)");
   assertEquals(ReceiptRelation.MATCHING,f.store.inspect(new IssuanceSourceKey("inspect-matching",1)).observation().receiptRelation());
   f.insertWork("inspect-conflicting",1,false);
   f.jdbc.update("INSERT INTO training_issuance_delivery_receipt(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES ('inspect-conflicting',1,'ISSUED',DATE '2026-12-02',1)");
   var conflictBefore=f.jdbc.queryForObject("SELECT to_jsonb(r)::text FROM training_issuance_delivery_receipt r WHERE policy_id='inspect-conflicting'",String.class);
   assertEquals(ReceiptRelation.CONFLICTING,f.store.inspect(new IssuanceSourceKey("inspect-conflicting",1)).observation().receiptRelation());
   assertEquals(conflictBefore,f.jdbc.queryForObject("SELECT to_jsonb(r)::text FROM training_issuance_delivery_receipt r WHERE policy_id='inspect-conflicting'",String.class));
  }
 }
 @Test void day116QuarantineIsAtomicAndReceiptAware() throws Exception {
  try(Fixture f=new Fixture()) {
   f.insertWork("hold-a",1,false);
   String sourceBefore=f.sourceSnapshot("hold-a"),receiptBefore=f.receiptSnapshot("hold-a");
   RecoveryTransitionResult result=f.service.quarantine(new IssuanceSourceKey("hold-a",1),0,RecoveryReason.MANUAL_HOLD);
   assertEquals(1,result.controlRevision());
   assertEquals("1|MANUAL_HOLD",f.jdbc.queryForObject("SELECT control_revision||'|'||quarantine_reason FROM training_issuance_work WHERE policy_id='hold-a' AND source_version=1",String.class));
   assertEquals(1,f.jdbc.queryForObject("SELECT count(*) FROM training_issuance_recovery_history WHERE policy_id='hold-a' AND source_version=1 AND control_revision=1 AND action='QUARANTINE'",Integer.class));
   String heldSnapshot=f.fullSnapshot("hold-a");assertTrue(heldSnapshot.contains("MANUAL_HOLD"));assertEquals(sourceBefore,f.sourceSnapshot("hold-a"));assertEquals(receiptBefore,f.receiptSnapshot("hold-a"));
   assertThrows(RecoveryPreconditionException.class,()->f.service.quarantine(new IssuanceSourceKey("hold-a",1),1,RecoveryReason.MANUAL_HOLD));
   assertEquals(1,f.jdbc.queryForObject("SELECT count(*) FROM training_issuance_recovery_history WHERE policy_id='hold-a' AND source_version=1",Integer.class));
  }
 }
 @Test void day116ControlAndHistoryRollbackTogetherOnHistoryInsertFailure() throws Exception {
  try(Fixture f=new Fixture()) {
   f.insertWork("rollback-hold",1,false);
   f.jdbc.execute("CREATE FUNCTION reject_recovery_history() RETURNS trigger LANGUAGE plpgsql AS $$ BEGIN RAISE EXCEPTION 'injected history failure'; END $$");
   f.jdbc.execute("CREATE TRIGGER reject_recovery_history_insert BEFORE INSERT ON training_issuance_recovery_history FOR EACH ROW EXECUTE FUNCTION reject_recovery_history()");
   assertThrows(org.springframework.dao.DataAccessException.class,()->f.service.quarantine(new IssuanceSourceKey("rollback-hold",1),0,RecoveryReason.MANUAL_HOLD));
   assertEquals("0|-",f.jdbc.queryForObject("SELECT control_revision||'|'||coalesce(quarantine_reason,'-') FROM training_issuance_work WHERE policy_id='rollback-hold' AND source_version=1",String.class));
   assertEquals(0,f.jdbc.queryForObject("SELECT count(*) FROM training_issuance_recovery_history WHERE policy_id='rollback-hold'",Integer.class));
  }
 }
 @Test void day117ReleaseRequiresExpectedRevisionAndRejectsAba() throws Exception {
  try(Fixture f=new Fixture()) {
   f.insertWork("aba-a",1,false); var k=new IssuanceSourceKey("aba-a",1);String sourceBefore=f.sourceSnapshot("aba-a"),receiptBefore=f.receiptSnapshot("aba-a");
   f.service.quarantine(k,0,RecoveryReason.MANUAL_HOLD); f.service.release(k,1,RecoveryReason.REVIEWED_FOR_RETRY);
   String releasedSnapshot=f.fullSnapshot("aba-a");
   f.service.quarantine(k,2,RecoveryReason.OBSERVED_FAILURE);
   assertThrows(RecoveryPreconditionException.class,()->f.service.release(k,1,RecoveryReason.REVIEWED_FOR_RETRY));
   assertEquals("3|OBSERVED_FAILURE",f.jdbc.queryForObject("SELECT control_revision||'|'||quarantine_reason FROM training_issuance_work WHERE policy_id='aba-a' AND source_version=1",String.class));
   assertEquals(List.of("QUARANTINE","RELEASE","QUARANTINE"),f.jdbc.queryForList("SELECT action FROM training_issuance_recovery_history WHERE policy_id='aba-a' ORDER BY control_revision",String.class));
   assertNotEquals(releasedSnapshot,f.fullSnapshot("aba-a"));assertEquals(sourceBefore,f.sourceSnapshot("aba-a"));assertEquals(receiptBefore,f.receiptSnapshot("aba-a"));
  }
 }
 @Test void day118FiltersHoldsBeforeLimit() throws Exception {
  try(Fixture f=new Fixture()) {
   for(int i=0;i<25;i++) f.insertWork("held-%02d".formatted(i),1,true);
   f.insertWork("zz-eligible",1,false);
   RecoveryAwareIssuanceWorkStore guarded=new RecoveryAwareIssuanceWorkStore(f.raw,f.store,f.jdbc);
   String beforeValid=workingSetSnapshot(f.jdbc);List<IssuanceEnvelope> rows=guarded.findPending(1);
   assertEquals(1,rows.size()); assertEquals("zz-eligible",rows.getFirst().policyId());
   assertEquals(beforeValid,workingSetSnapshot(f.jdbc));String before=workingSetSnapshot(f.jdbc);
   for(int invalid:List.of(0,-1,21)) assertThrows(IllegalArgumentException.class,()->guarded.findPending(invalid),"limit must be in [1,20]");
   assertEquals(before,workingSetSnapshot(f.jdbc));
  }
 }
 @Test void day120RetryOnePreservesReceiptThenAckBoundaries() throws Exception {
  try(Fixture f=new Fixture()) {
   f.insertWork("retry-a",1,false);
   String sourceBefore=f.sourceSnapshot("retry-a"),historyBefore=f.historySnapshot("retry-a");
   assertEquals("DELIVERED",f.service.retryOne(new IssuanceSourceKey("retry-a",1),0));
   String timestamp=f.jdbc.queryForObject("SELECT received_at::text FROM training_issuance_delivery_receipt WHERE policy_id='retry-a' AND source_version=1",String.class);
   assertEquals("ALREADY_COMPLETE",f.service.retryOne(new IssuanceSourceKey("retry-a",1),0));
   assertEquals(timestamp,f.jdbc.queryForObject("SELECT received_at::text FROM training_issuance_delivery_receipt WHERE policy_id='retry-a' AND source_version=1",String.class));
   assertNotNull(f.jdbc.queryForObject("SELECT acknowledged_at FROM training_issuance_work WHERE policy_id='retry-a' AND source_version=1",java.time.OffsetDateTime.class));
   assertEquals(sourceBefore,f.sourceSnapshot("retry-a"));assertEquals(historyBefore,f.historySnapshot("retry-a"));
  }
 }
 @Test void day120ReceiptIsVisibleOnIndependentConnectionBeforeAcknowledgment() throws Exception {
  try(Fixture f=new Fixture()) {
   f.insertWork("boundary-a",1,false);
   int[] transactionPid={-1};
   IssuanceWorkStore observing=new IssuanceWorkStore() {
    public int captureCommittedIssuances(int limit){return f.guarded.captureCommittedIssuances(limit);}
    public List<IssuanceEnvelope> findPending(int limit){return f.guarded.findPending(limit);}
    public DeliveryReceiptResult acceptReceipt(IssuanceEnvelope e){
     try {var bound=org.springframework.jdbc.datasource.DataSourceUtils.getConnection(f.ds);try(var s=bound.createStatement();var pid=s.executeQuery("SELECT pg_backend_pid()")){assertTrue(pid.next());transactionPid[0]=pid.getInt(1);}}
     catch(java.sql.SQLException ex){throw new IllegalStateException(ex);}
     return f.guarded.acceptReceipt(e);
    }
    public boolean acknowledge(IssuanceEnvelope e){
     int visible; int observerPid;
     try(java.sql.Connection c=f.ds.getConnection();var s=c.createStatement()) {
      try(var pid=s.executeQuery("SELECT pg_backend_pid()")){assertTrue(pid.next());observerPid=pid.getInt(1);}
      try(var receipt=s.executeQuery("SELECT count(*) FROM training_issuance_delivery_receipt WHERE policy_id='boundary-a' AND source_version=1")){assertTrue(receipt.next());visible=receipt.getInt(1);}
     } catch(java.sql.SQLException ex){throw new IllegalStateException(ex);}
     assertNotEquals(transactionPid[0],observerPid,"raw DataSource connection must be independent of the transaction-bound connection");
     assertEquals(1,visible,"receipt commit is visible from an independent connection before ack begins");
     return f.guarded.acknowledge(e);
    }
   };
   ManualRecoveryService service=new ManualRecoveryService(f.store,observing,f.tx);
   assertEquals("DELIVERED",service.retryOne(new IssuanceSourceKey("boundary-a",1),0));
  }
 }
 @Test void day120AcknowledgmentFailureLeavesCommittedReceiptForLaterReconciliation() throws Exception {
  try(Fixture f=new Fixture()) {
   f.insertWork("ack-fails",1,false);
   IssuanceWorkStore failAck=new IssuanceWorkStore() {
    public int captureCommittedIssuances(int limit){return f.guarded.captureCommittedIssuances(limit);}
    public List<IssuanceEnvelope> findPending(int limit){return f.guarded.findPending(limit);}
    public DeliveryReceiptResult acceptReceipt(IssuanceEnvelope e){return f.guarded.acceptReceipt(e);}
    public boolean acknowledge(IssuanceEnvelope e){throw new IllegalStateException("injected ack failure after receipt commit");}
   };
   ManualRecoveryService service=new ManualRecoveryService(f.store,failAck,f.tx);
   assertThrows(IllegalStateException.class,()->service.retryOne(new IssuanceSourceKey("ack-fails",1),0));
   assertEquals(1,f.jdbc.queryForObject("SELECT count(*) FROM training_issuance_delivery_receipt WHERE policy_id='ack-fails' AND source_version=1",Integer.class));
   assertNull(f.jdbc.queryForObject("SELECT acknowledged_at FROM training_issuance_work WHERE policy_id='ack-fails' AND source_version=1",java.time.OffsetDateTime.class));
   assertEquals("RECONCILED",f.service.retryOne(new IssuanceSourceKey("ack-fails",1),0));
  }
 }
 @Test void day120RejectsAmbientBeforeInspection() throws Exception {
  try(Fixture f=new Fixture()) {
   f.insertWork("ambient-a",1,false);
   new TransactionTemplate(f.tx).executeWithoutResult(s->{
    RecoveryPreconditionException failure=assertThrows(RecoveryPreconditionException.class,()->f.service.retryOne(new IssuanceSourceKey("ambient-a",1),0));
    assertEquals(RecoveryPreconditionException.Code.AMBIENT_TRANSACTION,failure.code());
   });
   assertEquals("0|-",f.jdbc.queryForObject("SELECT control_revision||'|'||coalesce(quarantine_reason,'-') FROM training_issuance_work WHERE policy_id='ambient-a'",String.class));
  }
 }
 @Test void day121LostResponseRequiresFreshEvidence() throws Exception {
  try(Fixture f=new Fixture()) {
   f.insertWork("uncertain-a",1,false); IssuanceSourceKey k=new IssuanceSourceKey("uncertain-a",1);
   IssuanceWorkStore lostReplyAfterCommit=new IssuanceWorkStore() {
    public int captureCommittedIssuances(int limit){return f.guarded.captureCommittedIssuances(limit);}
    public List<IssuanceEnvelope> findPending(int limit){return f.guarded.findPending(limit);}
    public DeliveryReceiptResult acceptReceipt(IssuanceEnvelope e){
     DeliveryReceiptResult result=f.guarded.acceptReceipt(e);
     org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization(){
      @Override public void afterCommit(){throw new IllegalStateException("simulated lost reply after receipt commit");}
     });
     return result;
    }
    public boolean acknowledge(IssuanceEnvelope e){return f.guarded.acknowledge(e);}
   };
   ManualRecoveryService uncertain=new ManualRecoveryService(f.store,lostReplyAfterCommit,f.tx);
   assertThrows(RuntimeException.class,()->uncertain.retryOne(k,0),"afterCommit failure simulates lost receipt response after durable commit");
   assertEquals(1,f.jdbc.queryForObject("SELECT count(*) FROM training_issuance_delivery_receipt WHERE policy_id='uncertain-a' AND source_version=1",Integer.class));
   String original=f.jdbc.queryForObject("SELECT to_jsonb(r)::text FROM training_issuance_delivery_receipt r WHERE policy_id='uncertain-a' AND source_version=1",String.class);
   assertNull(f.jdbc.queryForObject("SELECT acknowledged_at FROM training_issuance_work WHERE policy_id='uncertain-a' AND source_version=1",java.time.OffsetDateTime.class));
   String ackBefore=f.jdbc.queryForObject("SELECT coalesce(acknowledged_at::text,'-') FROM training_issuance_work WHERE policy_id='uncertain-a' AND source_version=1",String.class);
   String workBefore=f.jdbc.queryForObject("SELECT (to_jsonb(w)-'acknowledged_at')::text FROM training_issuance_work w WHERE policy_id='uncertain-a' AND source_version=1",String.class);
   assertEquals("-",ackBefore);
   assertEquals("RECONCILED",f.service.retryOne(k,0));
   assertEquals(original,f.jdbc.queryForObject("SELECT to_jsonb(r)::text FROM training_issuance_delivery_receipt r WHERE policy_id='uncertain-a' AND source_version=1",String.class));
   assertEquals(workBefore,f.jdbc.queryForObject("SELECT (to_jsonb(w)-'acknowledged_at')::text FROM training_issuance_work w WHERE policy_id='uncertain-a' AND source_version=1",String.class));
   assertNotEquals("-",f.jdbc.queryForObject("SELECT coalesce(acknowledged_at::text,'-') FROM training_issuance_work WHERE policy_id='uncertain-a' AND source_version=1",String.class));
   assertEquals(ReceiptRelation.MATCHING,f.service.inspect(k).observation().receiptRelation());
  }
 }
 @Test void day121DatabaseUnavailableIsNotReportedAsAbsent() throws Exception {
  org.postgresql.ds.PGSimpleDataSource down=new org.postgresql.ds.PGSimpleDataSource();
  down.setURL("jdbc:postgresql://127.0.0.1:1/not_available?connectTimeout=1");
  JdbcTemplate jdbc=new JdbcTemplate(down);var store=new JdbcManualRecoveryStore(jdbc);
  var guarded=new RecoveryAwareIssuanceWorkStore(new JdbcIssuanceWorkStore(jdbc),store,jdbc);
  var service=new ManualRecoveryService(store,guarded,new DataSourceTransactionManager(down));
  RuntimeException unavailable=assertThrows(RuntimeException.class,()->service.inspect(new IssuanceSourceKey("db-down",1)));
  assertFalse(unavailable instanceof RecoveryPreconditionException,"database failure must not be collapsed into NOT_FOUND/ABSENT");
 }
 @Test void day121RetryRejectsAckWithoutReceiptAndConflictingReceipt() throws Exception {
  try(Fixture f=new Fixture()) {
   f.insertWork("ack-without-receipt",1,false);
   f.jdbc.update("UPDATE training_issuance_work SET acknowledged_at=clock_timestamp() WHERE policy_id='ack-without-receipt' AND source_version=1");
   RecoveryPreconditionException ackOnly=assertThrows(RecoveryPreconditionException.class,()->f.service.retryOne(new IssuanceSourceKey("ack-without-receipt",1),0));
   assertEquals(RecoveryPreconditionException.Code.ACK_WITHOUT_RECEIPT,ackOnly.code());
   assertEquals(0,f.jdbc.queryForObject("SELECT count(*) FROM training_issuance_delivery_receipt WHERE policy_id='ack-without-receipt'",Integer.class));
   f.insertWork("conflicting-receipt",1,false);
   f.jdbc.update("INSERT INTO training_issuance_delivery_receipt(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES ('conflicting-receipt',1,'ISSUED',DATE '2026-12-02',1)");
   String before=f.jdbc.queryForObject("SELECT to_jsonb(r)::text FROM training_issuance_delivery_receipt r WHERE policy_id='conflicting-receipt' AND source_version=1",String.class);
   RecoveryPreconditionException conflict=assertThrows(RecoveryPreconditionException.class,()->f.service.retryOne(new IssuanceSourceKey("conflicting-receipt",1),0));
   assertEquals(RecoveryPreconditionException.Code.RECEIPT_CONFLICT,conflict.code());
   assertEquals(before,f.jdbc.queryForObject("SELECT to_jsonb(r)::text FROM training_issuance_delivery_receipt r WHERE policy_id='conflicting-receipt' AND source_version=1",String.class));
   assertNull(f.jdbc.queryForObject("SELECT acknowledged_at FROM training_issuance_work WHERE policy_id='conflicting-receipt' AND source_version=1",java.time.OffsetDateTime.class));
  }
 }
 private static RecoveryObservation row(IssuanceEnvelope e,int revision,java.time.Instant ack,java.time.Instant hold,ReceiptRelation relation) {
  return new RecoveryObservation(e.sourceKey(),true,e,revision,ack,hold,hold==null?null:RecoveryReason.MANUAL_HOLD,relation);
 }
 private static IssuanceEnvelope envelope(String id){return new IssuanceEnvelope(id,1,"ISSUED",LocalDate.parse("2026-12-01"),1);}
 private static String workingSetSnapshot(JdbcTemplate j){return j.queryForObject("SELECT (SELECT coalesce(jsonb_agg(to_jsonb(w) ORDER BY policy_id,source_version),'[]'::jsonb)::text FROM training_issuance_work w)||'|'||(SELECT coalesce(jsonb_agg(to_jsonb(r) ORDER BY policy_id,source_version),'[]'::jsonb)::text FROM training_issuance_delivery_receipt r)",String.class);}
 private static final class Fixture implements AutoCloseable {
  final EmbeddedPostgres pg; final DataSource ds; final JdbcTemplate jdbc; final PlatformTransactionManager tx;
  final JdbcIssuanceWorkStore raw; final JdbcManualRecoveryStore store; final RecoveryAwareIssuanceWorkStore guarded; final ManualRecoveryService service;
  Fixture() throws Exception {
   pg=EmbeddedPostgres.builder().start(); ds=pg.getPostgresDatabase(); Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
   jdbc=new JdbcTemplate(ds); tx=new DataSourceTransactionManager(ds); raw=new JdbcIssuanceWorkStore(jdbc); store=new JdbcManualRecoveryStore(jdbc);
   guarded=new RecoveryAwareIssuanceWorkStore(raw,store,jdbc); service=new ManualRecoveryService(store,guarded,tx);
  }
  void insertWork(String id,int version,boolean held) {
   jdbc.update("INSERT INTO training_policy_lifecycle(policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,annual_premium_rupees,currency,effective_date,status,version) VALUES (?, 'owner-a', ?, ?, 'home-secure-v1','training-rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',1)",id,"journey-"+id,"quote-"+id);
   jdbc.update("INSERT INTO training_policy_lifecycle_event(policy_id,version,event_type,effective_date) VALUES (?,?,'ISSUED',DATE '2026-12-01')",id,version);
   jdbc.update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version,quarantined_at,quarantine_reason,control_revision) VALUES (?,?, 'ISSUED',DATE '2026-12-01',1,CASE WHEN ? THEN clock_timestamp() ELSE NULL END,CASE WHEN ? THEN 'MANUAL_HOLD' ELSE NULL END,CASE WHEN ? THEN 1 ELSE 0 END)",id,version,held,held,held);
  }
  String snapshot(String id,int v) {return jdbc.queryForObject("SELECT policy_id||'|'||source_version||'|'||event_type||'|'||effective_date||'|'||envelope_schema_version||'|'||control_revision||'|'||coalesce(quarantine_reason,'-')||'|'||coalesce(acknowledged_at::text,'-') FROM training_issuance_work WHERE policy_id=? AND source_version=?",String.class,id,v);}
  String sourceSnapshot(String id){return jdbc.queryForObject("SELECT (SELECT coalesce(jsonb_agg(to_jsonb(p) ORDER BY policy_id),'[]'::jsonb)::text FROM training_policy_lifecycle p WHERE policy_id=?)||'|'||(SELECT coalesce(jsonb_agg(to_jsonb(e) ORDER BY version),'[]'::jsonb)::text FROM training_policy_lifecycle_event e WHERE policy_id=?)",String.class,id,id);}
  String receiptSnapshot(String id){return jdbc.queryForObject("SELECT coalesce(jsonb_agg(to_jsonb(r) ORDER BY source_version),'[]'::jsonb)::text FROM training_issuance_delivery_receipt r WHERE policy_id=?",String.class,id);}
  String historySnapshot(String id){return jdbc.queryForObject("SELECT coalesce(jsonb_agg(to_jsonb(h) ORDER BY control_revision),'[]'::jsonb)::text FROM training_issuance_recovery_history h WHERE policy_id=?",String.class,id);}
  String fullSnapshot(String id){return sourceSnapshot(id)+"|WORK="+jdbc.queryForObject("SELECT coalesce(jsonb_agg(to_jsonb(w) ORDER BY source_version),'[]'::jsonb)::text FROM training_issuance_work w WHERE policy_id=?",String.class,id)+"|RECEIPT="+receiptSnapshot(id)+"|HISTORY="+historySnapshot(id);}
  public void close() throws Exception {pg.close();}
 }
}
