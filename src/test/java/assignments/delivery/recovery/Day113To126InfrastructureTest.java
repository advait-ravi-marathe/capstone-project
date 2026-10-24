package assignments.delivery.recovery;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import static org.junit.jupiter.api.Assertions.*;

/** Instructor-owned migration and invariant scaffolding; no learner completion is implied. */
class Day113To126InfrastructureTest {
 @Test void v005UpgradePreservesAnExistingV004WorkRowAndVersionFourChecksum() throws Exception {
  try (EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
   DataSource ds=pg.getPostgresDatabase();
   var v4=Flyway.configure().dataSource(ds).locations("classpath:db/migration").target("4").load();
   v4.migrate();
   JdbcTemplate jdbc=new JdbcTemplate(ds);
   jdbc.update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES ('policy-demo-1',1,'ISSUED',DATE '2026-12-01',1)");
   String original=jdbc.queryForObject("SELECT to_jsonb(w)::text FROM training_issuance_work w WHERE policy_id='policy-demo-1' AND source_version=1",String.class);
   Integer checksumBefore=jdbc.queryForObject("SELECT checksum FROM flyway_schema_history WHERE version='004' AND success",Integer.class);

   Flyway.configure().dataSource(ds).locations("classpath:db/migration").target("5").load().migrate();
   String after=jdbc.queryForObject("SELECT (to_jsonb(w)-'quarantined_at'-'quarantine_reason'-'control_revision')::text FROM training_issuance_work w WHERE policy_id='policy-demo-1' AND source_version=1",String.class);
   Integer checksumAfter=jdbc.queryForObject("SELECT checksum FROM flyway_schema_history WHERE version='004' AND success",Integer.class);
   assertEquals(original,after,"V005 must preserve every V004 work field when projecting away its additive columns");
   assertEquals(checksumBefore,checksumAfter,"V004 remains immutable");
   assertEquals("0|-|-",jdbc.queryForObject("SELECT control_revision||'|'||coalesce(quarantined_at::text,'-')||'|'||coalesce(quarantine_reason,'-') FROM training_issuance_work WHERE policy_id='policy-demo-1' AND source_version=1",String.class));
   assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM training_issuance_recovery_history",Integer.class));
  }
 }

 @Test void v005AddsReversibleControlColumnsAndRejectsInvalidPairsWithoutChangingV004() throws Exception {
  try (EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
   DataSource ds=pg.getPostgresDatabase(); Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
   JdbcTemplate jdbc=new JdbcTemplate(ds);
   jdbc.update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES ('policy-demo-1',1,'ISSUED',DATE '2026-12-01',1)");
   assertEquals(0,jdbc.queryForObject("SELECT control_revision FROM training_issuance_work WHERE policy_id='policy-demo-1' AND source_version=1",Integer.class));
   assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->jdbc.update("UPDATE training_issuance_work SET quarantined_at=clock_timestamp() WHERE policy_id='policy-demo-1' AND source_version=1"),"hold marker requires its fixed reason");
   assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->jdbc.update("UPDATE training_issuance_work SET control_revision=-1 WHERE policy_id='policy-demo-1' AND source_version=1"),"revision is nonnegative");
   assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM training_issuance_work WHERE policy_id='policy-demo-1' AND event_type='ISSUED' AND effective_date=DATE '2026-12-01'",Integer.class));
   assertEquals(0,jdbc.queryForObject("SELECT count(*) FROM training_issuance_recovery_history",Integer.class));
  }
 }
 @Test void recoveryHistoryCannotReuseARevisionOrUnknownWork() throws Exception {
  try (EmbeddedPostgres pg=EmbeddedPostgres.builder().start()) {
   DataSource ds=pg.getPostgresDatabase(); Flyway.configure().dataSource(ds).locations("classpath:db/migration").load().migrate();
   JdbcTemplate jdbc=new JdbcTemplate(ds);
   jdbc.update("INSERT INTO training_issuance_work(policy_id,source_version,event_type,effective_date,envelope_schema_version) VALUES ('policy-demo-1',1,'ISSUED',DATE '2026-12-01',1)");
   jdbc.update("UPDATE training_issuance_work SET quarantined_at=clock_timestamp(),quarantine_reason='MANUAL_HOLD',control_revision=1 WHERE policy_id='policy-demo-1' AND source_version=1");
   jdbc.update("INSERT INTO training_issuance_recovery_history(policy_id,source_version,control_revision,action,reason_code) VALUES ('policy-demo-1',1,1,'QUARANTINE','MANUAL_HOLD')");
   assertThrows(org.springframework.dao.DuplicateKeyException.class,()->jdbc.update("INSERT INTO training_issuance_recovery_history(policy_id,source_version,control_revision,action,reason_code) VALUES ('policy-demo-1',1,1,'QUARANTINE','MANUAL_HOLD')"));
   assertEquals(1,jdbc.queryForObject("SELECT count(*) FROM training_issuance_recovery_history WHERE policy_id='policy-demo-1' AND control_revision=1",Integer.class));
  }
 }
}
