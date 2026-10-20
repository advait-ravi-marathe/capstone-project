package assignments.delivery.recovery;

import assignments.delivery.JdbcIssuanceWorkStore;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.stream.Collectors;

/** Test child: a genuinely new JVM queries the same disposable PostgreSQL through production filtering. */
public final class ManualRecoveryProcessMain {
 private ManualRecoveryProcessMain() {}
 public static void main(String[] args) {
  PGSimpleDataSource ds=new PGSimpleDataSource(); ds.setURL(args[0]);
  JdbcTemplate jdbc=new JdbcTemplate(ds); var recovery=new JdbcManualRecoveryStore(jdbc);
  var guarded=new RecoveryAwareIssuanceWorkStore(new JdbcIssuanceWorkStore(jdbc),recovery,jdbc);
  System.out.println(guarded.findPending(20).stream().map(e->e.policyId()+":"+e.sourceVersion()).collect(Collectors.joining(",")));
 }
}
