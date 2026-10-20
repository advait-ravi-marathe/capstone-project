package assignments.local;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/** Real executable JAR, explicit local CLI, and PostgreSQL state. No new Chromium/UI claim. */
@Tag("learnerAcceptance")
class LocalPackagedManualRecoveryAcceptanceTest {
 @Test void packagedManualRecoveryPreservesFullSnapshots() throws Exception {
  try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
   var ds=pg.getPostgresDatabase();Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();JdbcTemplate jdbc=new JdbcTemplate(ds);
   jdbc.update("INSERT INTO training_policy_lifecycle(policy_id,owner_id,journey_id,quote_version_id,product_version,rate_version,annual_premium_rupees,currency,effective_date,status,version) VALUES ('packaged-recovery','owner-a','journey-r','quote-r','home-secure-v1','training-rate-v1',125.00,'INR',DATE '2026-12-01','ISSUED',1)");
   jdbc.update("INSERT INTO training_policy_lifecycle_event(policy_id,version,event_type,effective_date) VALUES ('packaged-recovery',1,'ISSUED',DATE '2026-12-01')");
   jdbc.update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES ('packaged-recovery',1,'ISSUED',DATE '2026-12-01',1)");
   Path jar=Path.of("target/capstone-starter-1.0-SNAPSHOT.jar").toAbsolutePath();
   String initial=snapshot(pg); String immutableSource=sourceSnapshot(pg);
   assertEquals(0,command(jar,pg,"quarantine","packaged-recovery","0","MANUAL_HOLD").exitCode());
   assertEquals(immutableSource,sourceSnapshot(pg),"quarantine cannot change source policy/event/idempotency payloads");
   assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM training_issuance_recovery_history WHERE policy_id='packaged-recovery' AND control_revision=1 AND action='QUARANTINE'",Integer.class));
   String held=snapshot(pg);assertNotEquals(initial,held);
   var inspection=command(jar,pg,"inspect","packaged-recovery",null,null);assertEquals(0,inspection.exitCode(),inspection.output());assertTrue(inspection.output().contains("MANUAL_HOLD"));assertEquals(held,snapshot(pg));
   assertEquals(0,command(jar,pg,"release","packaged-recovery","1","REVIEWED_FOR_RETRY").exitCode());
   assertEquals(immutableSource,sourceSnapshot(pg),"release changes only recovery control metadata/history");
   assertEquals(0,command(jar,pg,"retry","packaged-recovery","2",null).exitCode());
   assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM training_issuance_delivery_receipt WHERE policy_id='packaged-recovery' AND source_version=1",Integer.class));
   String receipt=jdbc.queryForObject("SELECT to_jsonb(r)::text FROM training_issuance_delivery_receipt r WHERE policy_id='packaged-recovery' AND source_version=1",String.class);
   assertNotNull(jdbc.queryForObject("SELECT acknowledged_at FROM training_issuance_work WHERE policy_id='packaged-recovery' AND source_version=1",java.time.OffsetDateTime.class));
   String completed=snapshot(pg);assertEquals(2,jdbc.queryForObject("SELECT count(*) FROM training_issuance_recovery_history WHERE policy_id='packaged-recovery'",Integer.class));
   assertEquals(immutableSource,sourceSnapshot(pg),"retry leaves immutable issuance source evidence unchanged");
   var replay=command(jar,pg,"retry","packaged-recovery","2",null);assertEquals(0,replay.exitCode(),replay.output());assertEquals(completed,snapshot(pg));
   assertEquals(receipt,jdbc.queryForObject("SELECT to_jsonb(r)::text FROM training_issuance_delivery_receipt r WHERE policy_id='packaged-recovery' AND source_version=1",String.class));
   var conflict=run(jar,pg,List.of("--coverstory.recovery.action=inspect","--coverstory.recovery.policy-id=packaged-recovery","--coverstory.recovery.source-version=1","--coverstory.delivery.run-once"));
   assertNotEquals(0,conflict.exitCode());assertEquals(completed,snapshot(pg),"mutually exclusive delivery/recovery flags fail before writes");
  }
 }
 private static Result command(Path jar,EmbeddedPostgres pg,String action,String key,String rev,String reason)throws Exception{
  var args=new java.util.ArrayList<>(List.of("--coverstory.recovery.action="+action,"--coverstory.recovery.policy-id="+key,"--coverstory.recovery.source-version=1"));if(rev!=null)args.add("--coverstory.recovery.revision="+rev);if(reason!=null)args.add("--coverstory.recovery.reason="+reason);return run(jar,pg,args);
 }
 private static Result run(Path jar,EmbeddedPostgres pg,List<String> options)throws Exception{
  Path log=Files.createTempFile("manual-recovery-",".log");var cmd=new java.util.ArrayList<String>(List.of(Path.of(System.getProperty("java.home"),"bin","java").toString(),"-jar",jar.toString(),"--spring.profiles.active=local","--spring.datasource.url="+pg.getJdbcUrl("postgres","postgres"),"--spring.datasource.username=postgres","--spring.datasource.password="));cmd.addAll(options);Process p=new ProcessBuilder(cmd).redirectErrorStream(true).redirectOutput(log.toFile()).start();
  try{assertTrue(p.waitFor(30,TimeUnit.SECONDS),"packaged CLI watchdog: "+Files.readString(log));return new Result(p.exitValue(),Files.readString(log));}finally{if(p.isAlive()){p.destroyForcibly();assertTrue(p.waitFor(5,TimeUnit.SECONDS),"CLI child termination");}Files.deleteIfExists(log);}
 }
 private static String snapshot(EmbeddedPostgres pg)throws Exception{try(Connection c=pg.getPostgresDatabase().getConnection();Statement s=c.createStatement();var r=s.executeQuery("""
  SELECT (SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY policy_id),'[]'::jsonb)::text FROM training_policy_lifecycle t)||'|'||
   (SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY policy_id,version),'[]'::jsonb)::text FROM training_policy_lifecycle_event t)||'|'||
   (SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY owner_id,client_request_id),'[]'::jsonb)::text FROM training_policy_idempotency t)||'|'||
   (SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY policy_id,source_version),'[]'::jsonb)::text FROM training_issuance_work t)||'|'||
   (SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY policy_id,source_version),'[]'::jsonb)::text FROM training_issuance_delivery_receipt t)||'|'||
   (SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY policy_id,source_version,control_revision),'[]'::jsonb)::text FROM training_issuance_recovery_history t)""")){assertTrue(r.next());return r.getString(1);}}
 private static String sourceSnapshot(EmbeddedPostgres pg)throws Exception{try(Connection c=pg.getPostgresDatabase().getConnection();Statement s=c.createStatement();var r=s.executeQuery("""
  SELECT (SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY policy_id),'[]'::jsonb)::text FROM training_policy_lifecycle t)||'|'||
   (SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY policy_id,version),'[]'::jsonb)::text FROM training_policy_lifecycle_event t)||'|'||
   (SELECT coalesce(jsonb_agg(to_jsonb(t) ORDER BY owner_id,client_request_id),'[]'::jsonb)::text FROM training_policy_idempotency t)
  """)){assertTrue(r.next());return r.getString(1);}}
 private record Result(int exitCode,String output){}
}
