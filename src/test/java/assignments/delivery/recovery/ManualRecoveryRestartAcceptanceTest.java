package assignments.delivery.recovery;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

@Tag("learnerAcceptance")
class ManualRecoveryRestartAcceptanceTest {
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
  return new ProcessBuilder(Path.of(System.getProperty("java.home"),"bin","java").toString(),"-cp",System.getProperty("java.class.path"),ManualRecoveryProcessMain.class.getName(),url).redirectErrorStream(true).redirectOutput(log.toFile());
 }
 private static void terminate(Process p) throws Exception {if(p==null)return;if(p.isAlive())p.destroyForcibly();assertTrue(p.waitFor(5,TimeUnit.SECONDS),"child process cleanup");assertFalse(p.isAlive());}
 private static void seed(JdbcTemplate j,String id){
  j.update("INSERT INTO training_policy_lifecycle(policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,annual_premium_rupees,currency,effective_date,status,version) VALUES (?,'owner-a',?,?,'home-secure-v1','training-rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',1)",id,"j-"+id,"q-"+id);
  j.update("INSERT INTO training_policy_lifecycle_event(policy_id,version,event_type,effective_date) VALUES (?,1,'ISSUED',DATE '2026-12-01')",id);
  j.update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES (?,1,'ISSUED',DATE '2026-12-01',1)",id);
 }
}
