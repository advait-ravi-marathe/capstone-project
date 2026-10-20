package assignments.delivery.recovery;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import assignments.delivery.JdbcIssuanceWorkStore;
import assignments.delivery.IssuanceSourceKey;
import java.sql.Connection;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class ManualRecoveryRestartAcceptanceTest {
 @Test void day123FreshJvmsProcessEligibleWorkAndReconcileCommittedReceipt() throws Exception {
  try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
   var ds=pg.getPostgresDatabase();Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();JdbcTemplate jdbc=new JdbcTemplate(ds);
   seed(jdbc,"recover-held-a");seed(jdbc,"recover-eligible-b");
   var raw=new JdbcIssuanceWorkStore(jdbc);var store=new JdbcManualRecoveryStore(jdbc);var guarded=new RecoveryAwareIssuanceWorkStore(raw,store,jdbc);
   var service=new ManualRecoveryService(store,guarded,new DataSourceTransactionManager(ds));
   var a=new IssuanceSourceKey("recover-held-a",1);var b=new IssuanceSourceKey("recover-eligible-b",1);
   String sourceA=sourceSnapshot(jdbc,"recover-held-a"),sourceB=sourceSnapshot(jdbc,"recover-eligible-b");
   service.quarantine(a,0,RecoveryReason.MANUAL_HOLD);
   String heldA=fullSnapshot(jdbc,"recover-held-a");
   Path output=Files.createTempFile("day123-recovery-child-",".log");Process processB=null,crashed=null,recovery=null;
   ExecutorService markerReader=Executors.newSingleThreadExecutor();
   try {
    processB=retryChild(pg.getJdbcUrl("postgres","postgres"),output,"retry","recover-eligible-b","0").start();
    assertTrue(processB.waitFor(10,TimeUnit.SECONDS),"eligible work child timeout");assertEquals(0,processB.exitValue(),Files.readString(output));
    assertTrue(Files.readString(output).contains("RECOVERY_RESULT=DELIVERED"));
    assertNull(jdbc.queryForObject("SELECT acknowledged_at FROM training_issuance_work WHERE policy_id='recover-held-a' AND source_version=1",java.time.OffsetDateTime.class));
    assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM training_issuance_delivery_receipt WHERE policy_id='recover-eligible-b'",Integer.class));
    assertEquals(1,jdbc.queryForObject("SELECT control_revision FROM training_issuance_work WHERE policy_id='recover-held-a'",Integer.class));
    assertEquals(heldA,fullSnapshot(jdbc,"recover-held-a"),"eligible B processing leaves held A and its history unchanged");
    assertEquals(sourceB,sourceSnapshot(jdbc,"recover-eligible-b"),"delivery never changes source issuance evidence");
    service.release(a,1,RecoveryReason.REVIEWED_FOR_RETRY);
    long processBPid=processB.pid();
    crashed=retryChild(pg.getJdbcUrl("postgres","postgres"),output,"receipt-commit-block","recover-held-a","2").start();
    long crashedPid=crashed.pid();assertNotEquals(processBPid,crashedPid);
    Process crashingProcess=crashed;
    Future<String> marker=markerReader.submit(()->readMarker(crashingProcess));
    assertEquals("RECEIPT_COMMITTED",marker.get(10,TimeUnit.SECONDS),"child must signal only after the real receipt transaction committed");
    assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM training_issuance_delivery_receipt WHERE policy_id='recover-held-a' AND source_version=1",Integer.class));
    String committedReceipt=jdbc.queryForObject("SELECT to_jsonb(r)::text FROM training_issuance_delivery_receipt r WHERE policy_id='recover-held-a' AND source_version=1",String.class);
    assertNull(jdbc.queryForObject("SELECT acknowledged_at FROM training_issuance_work WHERE policy_id='recover-held-a' AND source_version=1",java.time.OffsetDateTime.class));
    String pendingAfterReceipt=fullSnapshot(jdbc,"recover-held-a");
    terminate(crashed);crashed=null;
    recovery=retryChild(pg.getJdbcUrl("postgres","postgres"),output,"retry","recover-held-a","2").start();
    assertNotEquals(crashedPid,recovery.pid());
    assertTrue(recovery.waitFor(10,TimeUnit.SECONDS),"fresh JVM reconciliation timeout");assertEquals(0,recovery.exitValue(),Files.readString(output));
    assertTrue(Files.readString(output).contains("RECOVERY_RESULT=RECONCILED"));
    assertEquals(committedReceipt,jdbc.queryForObject("SELECT to_jsonb(r)::text FROM training_issuance_delivery_receipt r WHERE policy_id='recover-held-a' AND source_version=1",String.class));
    assertNotNull(jdbc.queryForObject("SELECT acknowledged_at FROM training_issuance_work WHERE policy_id='recover-held-a' AND source_version=1",java.time.OffsetDateTime.class));
    String reconciled=fullSnapshot(jdbc,"recover-held-a");
    assertEquals(pendingAfterReceipt.replace("ACK=NULL","ACK=SET"),reconciled,"fresh JVM reconciliation changes only acknowledgement; receipt, source, control history, and captured payload remain stable");
    assertEquals(sourceA,sourceSnapshot(jdbc,"recover-held-a"));
    assertEquals("QUARANTINE,RELEASE",jdbc.queryForObject("SELECT string_agg(action,',' ORDER BY control_revision) FROM training_issuance_recovery_history WHERE policy_id='recover-held-a'",String.class));
    assertEquals(2,jdbc.queryForObject("SELECT control_revision FROM training_issuance_work WHERE policy_id='recover-held-a'",Integer.class));
   } finally {terminate(processB);terminate(crashed);terminate(recovery);markerReader.shutdownNow();assertTrue(markerReader.awaitTermination(5,TimeUnit.SECONDS),"marker reader cleanup");Files.deleteIfExists(output);}
  }
 }
 @Test void freshJvmRespectsHoldAndRelease() throws Exception {
  try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
   var ds=pg.getPostgresDatabase();Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();JdbcTemplate jdbc=new JdbcTemplate(ds);
   seed(jdbc,"recover-held");seed(jdbc,"recover-live");jdbc.update("UPDATE training_issuance_work SET quarantined_at=clock_timestamp(),quarantine_reason='MANUAL_HOLD',control_revision=1 WHERE policy_id='recover-held'");
   Path log=Files.createTempFile("manual-recovery-child-",".log");Process child=null;Process after=null;
   try {
    child=child(pg.getJdbcUrl("postgres","postgres"),log).start();long firstPid=child.pid();assertTrue(child.waitFor(10,TimeUnit.SECONDS),"bounded JVM read watchdog");assertEquals(0,child.exitValue(),Files.readString(log));
    assertEquals("recover-live:1",Files.readString(log).trim());
    jdbc.update("UPDATE training_issuance_work SET quarantined_at=NULL,quarantine_reason=NULL,control_revision=2 WHERE policy_id='recover-held'");
    after=child(pg.getJdbcUrl("postgres","postgres"),log).start();long secondPid=after.pid();assertNotEquals(firstPid,secondPid,"recovery must use a distinct JVM");assertTrue(after.waitFor(10,TimeUnit.SECONDS),"fresh JVM after explicit release");assertEquals(0,after.exitValue(),Files.readString(log));
    assertTrue(Files.readString(log).contains("recover-held:1"));assertTrue(Files.readString(log).contains("recover-live:1"));
   } finally {terminate(child);terminate(after);Files.deleteIfExists(log);}
  }
 }
 private static ProcessBuilder child(String url,Path log) {
  return new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin","java").toString(),"-cp",System.getProperty("java.class.path"),ManualRecoveryProcessMain.class.getName(),url,"pending").redirectErrorStream(true).redirectOutput(log.toFile());
 }
 private static ProcessBuilder retryChild(String url,Path output,String mode,String id,String revision) {
  ProcessBuilder builder=new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin","java").toString(),"-cp",System.getProperty("java.class.path"),ManualRecoveryProcessMain.class.getName(),url,mode,id,"1",revision).redirectErrorStream(true);
  return mode.equals("receipt-commit-block")?builder.redirectOutput(ProcessBuilder.Redirect.PIPE):builder.redirectOutput(output.toFile());
 }
 private static String readMarker(Process process)throws Exception{try(var reader=new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))){return reader.readLine();}}
 private static void terminate(Process p) throws Exception {if(p==null)return;if(p.isAlive())p.destroyForcibly();assertTrue(p.waitFor(5,TimeUnit.SECONDS),"child process cleanup");assertFalse(p.isAlive());}
 private static void seed(JdbcTemplate j,String id){
  j.update("INSERT INTO training_policy_lifecycle(policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,annual_premium_rupees,currency,effective_date,status,version) VALUES (?,'owner-a',?,?,'home-secure-v1','training-rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',1)",id,"j-"+id,"q-"+id);
  j.update("INSERT INTO training_policy_lifecycle_event(policy_id,version,event_type,effective_date) VALUES (?,1,'ISSUED',DATE '2026-12-01')",id);
  j.update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES (?,1,'ISSUED',DATE '2026-12-01',1)",id);
 }
 private static String sourceSnapshot(JdbcTemplate j,String id){return j.queryForObject("SELECT coalesce((SELECT to_jsonb(p)::text FROM training_policy_lifecycle p WHERE policy_id=?),'{}')||'|'||coalesce((SELECT jsonb_agg(to_jsonb(e) ORDER BY version)::text FROM training_policy_lifecycle_event e WHERE policy_id=?),'[]')",String.class,id,id);}
 private static String fullSnapshot(JdbcTemplate j,String id){
  String work=j.queryForObject("SELECT 'REV='||control_revision||'|HOLD='||coalesce(quarantine_reason,'-')||'|ACK='||CASE WHEN acknowledged_at IS NULL THEN 'NULL' ELSE 'SET' END||'|CAPTURE='||captured_at::text FROM training_issuance_work WHERE policy_id=? AND source_version=1",String.class,id);
  String receipt=j.queryForObject("SELECT coalesce(to_jsonb(r)::text,'ABSENT') FROM (SELECT 1) x LEFT JOIN training_issuance_delivery_receipt r ON r.policy_id=? AND r.source_version=1",String.class,id);
  String history=j.queryForObject("SELECT coalesce(jsonb_agg(to_jsonb(h) ORDER BY control_revision)::text,'[]') FROM training_issuance_recovery_history h WHERE policy_id=? AND source_version=1",String.class,id);
  return work+"|RECEIPT="+receipt+"|HISTORY="+history;
 }
}
