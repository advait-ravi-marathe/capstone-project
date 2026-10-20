package assignments.renewal;
import com.zaxxer.hikari.HikariConfig; import com.zaxxer.hikari.HikariDataSource; import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import assignments.insurance.PolicyRenewalFacts; import assignments.insurance.PolicyStatus;
import org.junit.jupiter.api.Tag; import org.junit.jupiter.api.Test; import java.sql.Connection; import java.util.ArrayList; import java.util.List; import java.util.Optional; import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
@Tag("learnerAcceptance") class PreviewPoolRecoveryAcceptanceTest {
 @Test void poolExhaustionTimesOutAndAppPermitsRecoverAfterFinallyRelease() throws Exception {
  try(EmbeddedPostgres pg=EmbeddedPostgres.builder().start()){
   HikariConfig cfg=new HikariConfig();cfg.setJdbcUrl(pg.getJdbcUrl("postgres","postgres"));cfg.setMaximumPoolSize(4);cfg.setMinimumIdle(0);cfg.setConnectionTimeout(300);cfg.setInitializationFailTimeout(-1);
   try(HikariDataSource ds=new HikariDataSource(cfg)){
    try(Connection c=ds.getConnection();var s=c.createStatement()){s.execute("CREATE TABLE preview_pool_fixture (policy_id TEXT PRIMARY KEY, owner_id TEXT NOT NULL)");s.executeUpdate("INSERT INTO preview_pool_fixture VALUES ('policy-demo','owner-demo')");}
    var holds=new ArrayList<Connection>();var gate=new PreviewAdmission(new PreviewCapacityPolicy(2,4,300));
    PolicyRenewalFacts expected=new PolicyRenewalFacts("policy-demo",java.time.LocalDate.parse("2026-01-01"),PolicyStatus.ISSUED,1,"home-secure-v1","training-rate-v1",new java.math.BigDecimal("10.00"),"INR");
    var store=new BoundedPolicyRenewalReadStore((p,o)->{try(Connection c=ds.getConnection();var q=c.prepareStatement("SELECT policy_id FROM preview_pool_fixture WHERE policy_id=? AND owner_id=?")){q.setString(1,p);q.setString(2,o);try(var rows=q.executeQuery()){return rows.next()?Optional.of(expected):Optional.empty();}}catch(java.sql.SQLException e){throw new org.springframework.jdbc.CannotGetJdbcConnectionException("read failed",e);}},gate,new PreviewMetrics(new io.micrometer.core.instrument.simple.SimpleMeterRegistry()));
    try{for(int i=0;i<4;i++)holds.add(ds.getConnection());assertThrows(PreviewUnavailableException.class,()->store.findByPolicyIdAndOwnerId("policy-demo","owner-demo"));assertEquals(2,gate.availablePermitsForTest());}
    finally{for(Connection c:holds)try{c.close();}catch(Exception ignored){}}
    assertEquals(Optional.of(expected),store.findByPolicyIdAndOwnerId("policy-demo","owner-demo"));assertEquals(2,gate.availablePermitsForTest());
   }
  }
 }
}
